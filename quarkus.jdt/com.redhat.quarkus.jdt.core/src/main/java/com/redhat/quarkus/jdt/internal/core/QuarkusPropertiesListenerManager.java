/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.internal.core;

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

import com.redhat.quarkus.commons.QuarkusPropertiesChangeEvent;
import com.redhat.quarkus.commons.QuarkusPropertiesScope;
import com.redhat.quarkus.jdt.core.IQuarkusPropertiesChangedListener;
import com.redhat.quarkus.jdt.internal.core.utils.JDTQuarkusUtils;

/**
 * This class tracks :
 * 
 * <ul>
 * <li>the classpath changed of all Java project</li>
 * <li>java sources changed on save</li>
 * </ul>
 * 
 * In this case it executes the "quarkusTools.quarkusPropertiesChanged" command
 * on client side with array of project URIs which have classpath/sources
 * changed.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusPropertiesListenerManager {

	private static final Logger LOGGER = Logger.getLogger(QuarkusPropertiesListenerManager.class.getName());

	private static final QuarkusPropertiesListenerManager INSTANCE = new QuarkusPropertiesListenerManager();

	public static QuarkusPropertiesListenerManager getInstance() {
		return INSTANCE;
	}

	private class QuarkusListener implements IElementChangedListener, IResourceChangeListener, IResourceDeltaVisitor {

		private static final String JAVA_FILE_EXTENSION = "java";

		@Override
		public void elementChanged(ElementChangedEvent event) {
			if (listeners.isEmpty()) {
				return;
			}
			// Collect project names which have classpath changed.
			QuarkusPropertiesChangeEvent quarkusEvent = processDelta(event.getDelta(), null);
			if (quarkusEvent != null) {
				// execute on client side the "quarkusTools.classpathChanged" command with the
				// given
				// project names.
				for (IQuarkusPropertiesChangedListener listener : listeners) {
					listener.quarkusPropertiesChanged(quarkusEvent);
				}
			}
		}

		private QuarkusPropertiesChangeEvent processDeltaChildren(IJavaElementDelta delta,
				QuarkusPropertiesChangeEvent event) {
			for (IJavaElementDelta c : delta.getAffectedChildren()) {
				event = processDelta(c, event);
			}
			return event;
		}

		private QuarkusPropertiesChangeEvent processDelta(IJavaElementDelta delta, QuarkusPropertiesChangeEvent event) {
			IJavaElement element = delta.getElement();
			switch (element.getElementType()) {
			case IJavaElement.JAVA_MODEL:
				event = processDeltaChildren(delta, event);
				break;
			case IJavaElement.JAVA_PROJECT:
				if (isCreatedOrDeleted(delta) || isClasspathChanged(delta.getFlags())) {
					if (event == null) {
						event = new QuarkusPropertiesChangeEvent();
						event.setType(QuarkusPropertiesScope.classpath);
						event.setProjectURIs(new HashSet<String>());
					}
					IJavaProject project = (IJavaProject) element;
					event.getProjectURIs().add(JDTQuarkusUtils.getProjectURI(project));
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
					// A Java file was saved
					QuarkusPropertiesChangeEvent quarkusEvent = new QuarkusPropertiesChangeEvent();
					quarkusEvent.setType(QuarkusPropertiesScope.sources);
					quarkusEvent.setProjectURIs(new HashSet<String>());
					quarkusEvent.getProjectURIs().add(JDTQuarkusUtils.getProjectURI(file.getProject()));
					// IMPORTANT: The LSP notification 'quarkusPropertiesChanged' must be executed
					// in background otherwise it breaks everything (JDT LS for Java completion,
					// hover, etc are broken)
					CompletableFuture.runAsync(() -> {
						for (IQuarkusPropertiesChangedListener listener : listeners) {
							try {
								listener.quarkusPropertiesChanged(quarkusEvent);
							} catch (Exception e) {
								if (LOGGER.isLoggable(Level.SEVERE)) {
									LOGGER.log(Level.SEVERE,
											"Error while sending LSP 'quarkusPropertiesChanged' notification", e);
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

	private QuarkusListener quarkusListener;

	private final Set<IQuarkusPropertiesChangedListener> listeners;

	private QuarkusPropertiesListenerManager() {
		listeners = new HashSet<>();
	}

	/**
	 * Add the given quarkus properties changed listener.
	 * 
	 * @param listener the listener to add
	 */
	public void addQuarkusPropertiesChangedListener(IQuarkusPropertiesChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Remove the given quarkus properties changed listener.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeQuarkusPropertiesChangedListener(IQuarkusPropertiesChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Initialize the classpath listener manager.
	 */
	public synchronized void initialize() {
		if (quarkusListener != null) {
			return;
		}
		this.quarkusListener = new QuarkusListener();
		JavaCore.addElementChangedListener(quarkusListener);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(quarkusListener, IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * Destroy the classpath listener manager.
	 */
	public synchronized void destroy() {
		if (quarkusListener != null) {
			JavaCore.removeElementChangedListener(quarkusListener);
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(quarkusListener);
			this.quarkusListener = null;
		}
	}

}
