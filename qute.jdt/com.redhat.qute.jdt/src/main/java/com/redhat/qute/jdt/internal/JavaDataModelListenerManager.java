/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal;

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

import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.jdt.IJavaDataModelChangedListener;
import com.redhat.qute.jdt.utils.JDTQuteProjectUtils;

/**
 * This class tracks :
 *
 * <ul>
 * <li>the classpath changed of all Java project</li>
 * <li>java sources changed on save</li>
 * </ul>
 *
 * In this case it executes the "JavaDataModelChangeEvent" command on
 * client side with array of project URIs which have classpath/sources changed.
 *
 * @author Angelo ZERR
 *
 */
public class JavaDataModelListenerManager {

	private static final Logger LOGGER = Logger.getLogger(JavaDataModelListenerManager.class.getName());

	private static final JavaDataModelListenerManager INSTANCE = new JavaDataModelListenerManager();

	public static JavaDataModelListenerManager getInstance() {
		return INSTANCE;
	}

	private class QuteListener
			implements IElementChangedListener, IResourceChangeListener, IResourceDeltaVisitor {

		private static final String JAVA_FILE_EXTENSION = "java";

		@Override
		public void elementChanged(ElementChangedEvent event) {
			if (listeners.isEmpty()) {
				return;
			}
			// Collect project names which have classpath changed.
			JavaDataModelChangeEvent mpEvent = processDelta(event.getDelta(), null);
			if (mpEvent != null) {
				fireAsyncEvent(mpEvent);
			}
		}

		private JavaDataModelChangeEvent processDeltaChildren(IJavaElementDelta delta,
				JavaDataModelChangeEvent event) {
			for (IJavaElementDelta c : delta.getAffectedChildren()) {
				event = processDelta(c, event);
			}
			return event;
		}

		private JavaDataModelChangeEvent processDelta(IJavaElementDelta delta,
				JavaDataModelChangeEvent event) {
			IJavaElement element = delta.getElement();
			switch (element.getElementType()) {
			case IJavaElement.JAVA_MODEL:
				event = processDeltaChildren(delta, event);
				break;
			case IJavaElement.JAVA_PROJECT:
				if (isCreatedOrDeleted(delta) || isClasspathChanged(delta.getFlags())) {
					if (event == null) {
						event = new JavaDataModelChangeEvent();
						//event.setType(MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);
						event.setProjectURIs(new HashSet<String>());
					}
					IJavaProject project = (IJavaProject) element;
					event.getProjectURIs().add(JDTQuteProjectUtils.getProjectUri(project));
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
				if (isJavaFile(file) && isFileContentChanged(delta)) {
					// A Java file has been saved
					JavaDataModelChangeEvent event = new JavaDataModelChangeEvent();
					event.setProjectURIs(new HashSet<String>());
					event.getProjectURIs().add(JDTQuteProjectUtils.getProjectURI(file.getProject()));
					fireAsyncEvent(event);
				}
			}
			return false;
		}

		private void fireAsyncEvent(JavaDataModelChangeEvent event) {
			// IMPORTANT: The LSP notification 'qute/javaDataModelChanged' must be
			// executed
			// in background otherwise it breaks everything (JDT LS for Java completion,
			// hover, etc are broken)
			CompletableFuture.runAsync(() -> {
				for (IJavaDataModelChangedListener listener : listeners) {
					try {
						listener.dataModelChanged(event);
					} catch (Exception e) {
						if (LOGGER.isLoggable(Level.SEVERE)) {
							LOGGER.log(Level.SEVERE,
									"Error while sending LSP 'qute/javaDataModelChanged' notification", e);
						}
					}
				}
			});
		}

		private boolean isJavaFile(IFile file) {
			return JAVA_FILE_EXTENSION.equals(file.getFileExtension());
		}

		private boolean isFileContentChanged(IResourceDelta delta) {
			return (delta.getKind() == IResourceDelta.CHANGED && (delta.getFlags() & IResourceDelta.CONTENT) != 0);
		}

	}

	private QuteListener quteListener;

	private final Set<IJavaDataModelChangedListener> listeners;

	private JavaDataModelListenerManager() {
		listeners = new HashSet<>();
	}

	/**
	 * Add the given MicroProfile properties changed listener.
	 *
	 * @param listener the listener to add
	 */
	public void addJavaDataModelChangedListener(IJavaDataModelChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Remove the given MicroProfile properties changed listener.
	 *
	 * @param listener the listener to remove
	 */
	public void removeJavaDataModelChangedListener(IJavaDataModelChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Initialize the classpath listener manager.
	 */
	public synchronized void initialize() {
		if (quteListener != null) {
			return;
		}
		this.quteListener = new QuteListener();
		JavaCore.addElementChangedListener(quteListener);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(quteListener,
				IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * Destroy the classpath listener manager.
	 */
	public synchronized void destroy() {
		if (quteListener != null) {
			JavaCore.removeElementChangedListener(quteListener);
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(quteListener);
			this.quteListener = null;
		}
	}

}
