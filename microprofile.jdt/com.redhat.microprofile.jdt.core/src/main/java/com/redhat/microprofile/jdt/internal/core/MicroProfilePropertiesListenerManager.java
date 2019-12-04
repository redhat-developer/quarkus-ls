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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.core.IMicroProfilePropertiesChangedListener;
import com.redhat.microprofile.jdt.internal.core.utils.JDTMicroProfileUtils;

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

	private static final MicroProfilePropertiesListenerManager INSTANCE = new MicroProfilePropertiesListenerManager();

	public static MicroProfilePropertiesListenerManager getInstance() {
		return INSTANCE;
	}

	private class MicroProfileListener
			implements IElementChangedListener, IResourceChangeListener, IResourceDeltaVisitor {

		private static final String JAVA_FILE_EXTENSION = "java";

		@Override
		public void elementChanged(ElementChangedEvent event) {
			if (listeners.isEmpty()) {
				return;
			}
			// Collect project names which have classpath changed.
			MicroProfilePropertiesChangeEvent quarkusEvent = processDelta(event.getDelta(), null);
			if (quarkusEvent != null) {
				// execute on client side the "quarkusTools.classpathChanged" command with the
				// given
				// project names.
				for (IMicroProfilePropertiesChangedListener listener : listeners) {
					listener.propertiesChanged(quarkusEvent);
				}
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
					if (event == null) {
						event = new MicroProfilePropertiesChangeEvent();
						event.setType(MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);
						event.setProjectURIs(new HashSet<String>());
					}
					IJavaProject project = (IJavaProject) element;
					event.getProjectURIs().add(JDTMicroProfileUtils.getProjectURI(project));
				}
				break;
			default:
				break;
			}
			return event;
		}

		private boolean isCreatedOrDeleted(IJavaElementDelta delta) {
			int kind = delta.getKind();
			return kind == IJavaElementDelta.ADDED || kind == IJavaElementDelta.REMOVED;
		}

		private boolean isClasspathChanged(int flags) {
			return 0 != (flags & (IJavaElementDelta.F_CLASSPATH_CHANGED | IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED
					| IJavaElementDelta.F_CLOSED | IJavaElementDelta.F_OPENED));
		}

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			switch (event.getType()) {
			case IResourceChangeEvent.POST_CHANGE:
				IResourceDelta resourceDelta = event.getDelta();
				if (resourceDelta != null) {
					try {
						resourceDelta.accept(this);
					} catch (CoreException e) {
						if (LOGGER.isLoggable(Level.SEVERE)) {
							LOGGER.log(Level.SEVERE, "Error while tracking save of Java file", e);
						}
					}
				}
				break;
			}
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource == null) {
				return false;
			}
			switch (resource.getType()) {
			case IResource.ROOT:
			case IResource.PROJECT:
			case IResource.FOLDER:
				return resource.isAccessible();
			case IResource.FILE:
				IFile file = (IFile) resource;
				if (isJavaFile(file)) {
					// A Java file has been saved
					MicroProfilePropertiesChangeEvent event = new MicroProfilePropertiesChangeEvent();
					event.setType(MicroProfilePropertiesScope.ONLY_SOURCES);
					event.setProjectURIs(new HashSet<String>());
					event.getProjectURIs().add(JDTMicroProfileUtils.getProjectURI(file.getProject()));
					// IMPORTANT: The LSP notification 'microprofile/propertiesChanged' must be
					// executed
					// in background otherwise it breaks everything (JDT LS for Java completion,
					// hover, etc are broken)
					CompletableFuture.runAsync(() -> {
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
					});
				}
			}
			return false;
		}

		private boolean isJavaFile(IFile file) {
			return JAVA_FILE_EXTENSION.equals(file.getFileExtension());
		}

	}

	private MicroProfileListener microprofileListener;

	private final Set<IMicroProfilePropertiesChangedListener> listeners;

	private MicroProfilePropertiesListenerManager() {
		listeners = new HashSet<>();
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
		JavaCore.addElementChangedListener(microprofileListener);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(microprofileListener,
				IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * Destroy the classpath listener manager.
	 */
	public synchronized void destroy() {
		if (microprofileListener != null) {
			JavaCore.removeElementChangedListener(microprofileListener);
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(microprofileListener);
			this.microprofileListener = null;
		}
	}

}
