/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.core.IMicroProfilePropertiesChangedListener;
import com.redhat.microprofile.jdt.core.utils.JDTMicroProfileUtils;

/**
 * This class tracks :
 * 
 * <ul>
 * <li>the classpath changed of all Java project</li>
 * <li>java sources changed on save</li>
 * </ul>
 * 
 * In this case it executes the "microprofile/propertiesChanged" command on
 * client side with array of project URIs which have classpath/sources changed.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfilePropertiesListenerManager {

	private static final Logger LOGGER = Logger.getLogger(MicroProfilePropertiesListenerManager.class.getName());

	private static final MicroProfilePropertiesListenerManager INSTANCE = new MicroProfilePropertiesListenerManager(
			true);

	public static MicroProfilePropertiesListenerManager getInstance() {
		return INSTANCE;
	}

	private class MicroProfileListener implements IElementChangedListener {

		@Override
		public void elementChanged(ElementChangedEvent event) {
			if (listeners.isEmpty()) {
				return;
			}
			// Collect project names which have classpath changed.
			MicroProfilePropertiesChangeEvent mpEvent = processDelta(event.getDelta(), null);
			if (mpEvent != null) {
				doFireEvent(mpEvent);
			}
		}

		private MicroProfilePropertiesChangeEvent processDeltaChildren(IJavaElementDelta delta,
				MicroProfilePropertiesChangeEvent event) {
			for (IJavaElementDelta c : delta.getAffectedChildren()) {
				event = processDelta(c, event);
			}
			return event;
		}

		private MicroProfilePropertiesChangeEvent processDelta(IJavaElementDelta delta,
				MicroProfilePropertiesChangeEvent event) {
			IJavaElement element = delta.getElement();
			switch (element.getElementType()) {
			case IJavaElement.JAVA_MODEL:
				event = processDeltaChildren(delta, event);
				break;
			case IJavaElement.JAVA_PROJECT:
				if (isCreatedOrDeleted(delta) || isClasspathChanged(delta.getFlags())) {
					// At project has been created, deleted or classpath has been changed.:
					// -> microprofile/propertiesChanged must be fired with 'sources' and
					// 'dependencies' as type
					if (event == null) {
						event = new MicroProfilePropertiesChangeEvent();
						event.setType(MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);
						event.setProjectURIs(new HashSet<String>());
					}
					IJavaProject project = (IJavaProject) element;
					event.getProjectURIs().add(JDTMicroProfileUtils.getProjectURI(project));
				} else {
					event = processDeltaChildren(delta, event);
				}
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				IPackageFragmentRoot root = (IPackageFragmentRoot) element;
				if (isPackageSource(root) && isJavaFilesAffected(delta)) {
					// At least one Java source file has been updated and saved:
					// -> microprofile/propertiesChanged must be fired with 'sources' as type
					if (event == null) {
						event = new MicroProfilePropertiesChangeEvent();
						event.setType(MicroProfilePropertiesScope.ONLY_SOURCES);
						event.setProjectURIs(new HashSet<String>());
					}
					IJavaProject project = element.getJavaProject();
					event.getProjectURIs().add(JDTMicroProfileUtils.getProjectURI(project));
				}
				break;
			default:
				break;
			}
			return event;
		}

		/**
		 * Returns true if the given package root is a source folder and false
		 * otherwise.
		 * 
		 * @param root the package root.
		 * @return true if the given package root is a source folder and false
		 *         otherwise.
		 */
		private boolean isPackageSource(IPackageFragmentRoot root) {
			try {
				return root.getKind() == IPackageFragmentRoot.K_SOURCE;
			} catch (JavaModelException e) {
				return false;
			}
		}

		/**
		 * Returns true if there is at least one java source file which has changed and
		 * saved and false otherwise.
		 * 
		 * @param delta the java element delta.
		 * @return true if there is at least one java source file which has changed and
		 *         saved and false otherwise.
		 */
		private boolean isJavaFilesAffected(IJavaElementDelta delta) {
			if (delta.getElement().getElementType() == IJavaElement.COMPILATION_UNIT
					&& (delta.getFlags() & IJavaElementDelta.F_CONTENT) != 0) {
				return true;
			}
			for (IJavaElementDelta c : delta.getAffectedChildren()) {
				if (isJavaFilesAffected(c)) {
					return true;
				}
			}
			return false;
		}

		private boolean isCreatedOrDeleted(IJavaElementDelta delta) {
			int kind = delta.getKind();
			return kind == IJavaElementDelta.ADDED || kind == IJavaElementDelta.REMOVED;
		}

		private boolean isClasspathChanged(int flags) {
			return 0 != (flags & (IJavaElementDelta.F_CLASSPATH_CHANGED | IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED
					| IJavaElementDelta.F_CLOSED | IJavaElementDelta.F_OPENED));
		}

		private void doFireEvent(MicroProfilePropertiesChangeEvent event) {
			if (!async) {
				fireEvent(event);
			} else {
				// IMPORTANT: The LSP notification 'microprofile/propertiesChanged' must be
				// executed
				// in background otherwise it breaks everything (JDT LS for Java completion,
				// hover, etc are broken)
				CompletableFuture.runAsync(() -> {
					fireEvent(event);
				});
			}
		}

		private void fireEvent(MicroProfilePropertiesChangeEvent event) {
			for (IMicroProfilePropertiesChangedListener listener : listeners) {
				try {
					listener.propertiesChanged(event);
				} catch (Exception e) {
					if (LOGGER.isLoggable(Level.SEVERE)) {
						LOGGER.log(Level.SEVERE,
								"Error while sending LSP 'microprofile/propertiesChanged' notification", e);
					}
				}
			}
		}

	}

	private MicroProfileListener microprofileListener;

	private final Set<IMicroProfilePropertiesChangedListener> listeners;

	private boolean async;

	MicroProfilePropertiesListenerManager(boolean async) {
		this.listeners = new HashSet<>();
		this.async = async;
	}

	/**
	 * Add the given MicroProfile properties changed listener.
	 * 
	 * @param listener the listener to add
	 */
	public void addMicroProfilePropertiesChangedListener(IMicroProfilePropertiesChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Remove the given MicroProfile properties changed listener.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeMicroProfilePropertiesChangedListener(IMicroProfilePropertiesChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Initialize the classpath listener manager.
	 */
	public synchronized void initialize() {
		if (microprofileListener != null) {
			return;
		}
		this.microprofileListener = new MicroProfileListener();
		JavaCore.addElementChangedListener(microprofileListener, ElementChangedEvent.POST_CHANGE);
	}

	/**
	 * Destroy the classpath listener manager.
	 */
	public synchronized void destroy() {
		if (microprofileListener != null) {
			JavaCore.removeElementChangedListener(microprofileListener);
			this.microprofileListener = null;
		}
	}

}
