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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.redhat.quarkus.jdt.internal.core.utils.JDTQuarkusUtils;

/**
 * This class tracks the classpath changed of all Java project and execute the
 * "quarkusTools.classpathChanged" command on client side with array of projects
 * which have classpath changed.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusClasspathListenerManager {

	private static final QuarkusClasspathListenerManager INSTANCE = new QuarkusClasspathListenerManager();

	public static QuarkusClasspathListenerManager getInstance() {
		return INSTANCE;
	}

	private class QuarkusListener implements IElementChangedListener {

		@Override
		public void elementChanged(ElementChangedEvent event) {
			if (listeners.isEmpty()) {
				return;
			}
			// Collect project names which have classpatch changed.
			Collection<String> projectsToUpdate = new HashSet<String>();
			processDelta(event.getDelta(), projectsToUpdate);
			if (!projectsToUpdate.isEmpty()) {
				// execute on client side the "quarkusTools.classpathChanged" command with the
				// given
				// project names.
				QuarkusClasspathListenerManager.this.classpathChanged(projectsToUpdate);
			}
		}

		private void processDeltaChildren(IJavaElementDelta delta, Collection<String> projectsToUpdate) {
			for (IJavaElementDelta c : delta.getAffectedChildren()) {
				processDelta(c, projectsToUpdate);
			}
		}

		private void processDelta(IJavaElementDelta delta, Collection<String> projectsToUpdate) {
			IJavaElement element = delta.getElement();
			switch (element.getElementType()) {
			case IJavaElement.JAVA_MODEL:
				processDeltaChildren(delta, projectsToUpdate);
				break;
			case IJavaElement.JAVA_PROJECT:
				if (isCreatedOrDeleted(delta) || isClasspathChanged(delta.getFlags())) {
					IJavaProject project = (IJavaProject) element;
					projectsToUpdate.add(JDTQuarkusUtils.getProjectURI(project));
				}
				break;
			default:
				break;
			}
		}

		private boolean isCreatedOrDeleted(IJavaElementDelta delta) {
			int kind = delta.getKind();
			return kind == IJavaElementDelta.ADDED || kind == IJavaElementDelta.REMOVED;
		}

		private boolean isClasspathChanged(int flags) {
			return 0 != (flags & (IJavaElementDelta.F_CLASSPATH_CHANGED | IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED
					| IJavaElementDelta.F_CLOSED | IJavaElementDelta.F_OPENED));
		}

	}

	private QuarkusListener quarkusListener;

	private final Set<IClasspathChangedListener> listeners;

	private QuarkusClasspathListenerManager() {
		listeners = new HashSet<>();
	}

	/**
	 * Add the given classpath changed listener.
	 * 
	 * @param listener the listener to add
	 */
	public void addClasspathChangedListener(IClasspathChangedListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Remove the given classpath changed listener.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeClasspathChangedListener(IClasspathChangedListener listener) {
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
	}

	/**
	 * Destroy the classpath listener manager.
	 */
	public synchronized void destroy() {
		if (quarkusListener != null) {
			JavaCore.removeElementChangedListener(quarkusListener);
			this.quarkusListener = null;
		}
	}

	/**
	 * Callback when classpath changed for the given project locations.
	 * 
	 * @param projectsToUpdate
	 */
	private void classpathChanged(Collection<String> projectsToUpdate) {
		for (IClasspathChangedListener listener : listeners) {
			listener.classpathChanged(projectsToUpdate);
		}
	}
}
