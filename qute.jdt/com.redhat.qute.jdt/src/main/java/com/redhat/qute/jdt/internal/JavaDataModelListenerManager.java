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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;

import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent.ProjectChangeInfo;
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
 * In this case it executes the "JavaDataModelChangeEvent" command on client
 * side with array of project URIs which have classpath/sources changed.
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

	private class QuteListener implements IElementChangedListener {

		@Override
		public void elementChanged(ElementChangedEvent event) {
			if (listeners.isEmpty()) {
				return;
			}
			// Collect project which have some Java changes:
			// - create/delete Java project
			// - classpath changed
			// update, delete, add Java file
			Map<IJavaProject, ProjectChangeInfo> changedProjects = new HashMap<>();
			processDelta(event.getDelta(), changedProjects);
			if (changedProjects.isEmpty()) {
				// No changes
				return;
			}

			// Send qute/dataModelChanged event:
			// [Trace - 6:41:45 PM] Sending notification 'qute/dataModelChanged'.
			// Params: {
			// "projects": [
			// {
			// "uri": "quarkus-roq-frontmatter",
			// "sources": [
			// "io.quarkiverse.roq.frontmatter.runtime.model.NormalPage"
			// ]
			// }
			// ]
			JavaDataModelChangeEvent mpEvent = new JavaDataModelChangeEvent();
			mpEvent.setProjects(new HashSet(changedProjects.values()));
			fireAsyncEvent(mpEvent);
		}

		private void processDelta(IJavaElementDelta delta, Map<IJavaProject, ProjectChangeInfo> changedProjects) {
			IJavaElement element = delta.getElement();
			switch (element.getElementType()) {
			case IJavaElement.JAVA_MODEL:
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			case IJavaElement.PACKAGE_FRAGMENT:
				processDeltaChildren(delta, changedProjects);
				break;
			case IJavaElement.JAVA_PROJECT:
				if (isCreatedOrDeleted(delta) || isClasspathChanged(delta.getFlags())) {
					// a project is created/deleted
					// or a classpath changed
					IJavaProject project = (IJavaProject) element;
					getProjectInfo(project, changedProjects);
				}
				processDeltaChildren(delta, changedProjects);
				break;
			case IJavaElement.COMPILATION_UNIT:
				if (shouldReportJavaFileAsChanged(delta)) {
					ICompilationUnit compilationUnit = (ICompilationUnit) element;
					String fullyQualifiedName = getQualifiedName(compilationUnit);
					ProjectChangeInfo projectChangeInfo = getProjectInfo(compilationUnit.getJavaProject(),
							changedProjects);
					Set<String> sources = projectChangeInfo.getSources();
					if (sources == null) {
						sources = new HashSet<String>();
						projectChangeInfo.setSources(sources);
					}
					sources.add(fullyQualifiedName);
				}
				break;
			default:
				break;
			}
		}

		private static boolean shouldReportJavaFileAsChanged(IJavaElementDelta delta) {
			boolean javaFileCreated = (delta.getFlags() == 0 && delta.getKind() == IJavaElementDelta.ADDED);
			if (javaFileCreated) {
				// XXX.java[+]: {}
				return true;
			}
			boolean javaFileDeleted = (delta.getFlags() == 0 && delta.getKind() == IJavaElementDelta.REMOVED);
			if (javaFileDeleted) {
				// XXX.java[-]: {}
				return true;
			}
			// When Java file is saved, the ElementChangedEvent contains 3 delta which call
			// this
			// IJavaElement.COMPILATION_UNIT case:
			// - PRIMARY_RESOURCE
			// - PRIMARY_WORKING_COPY
			// - PRIMARY_WORKING_COPY
			boolean javaFileSaved = (delta.getFlags() & IJavaElementDelta.F_PRIMARY_RESOURCE) != 0;
			if (javaFileSaved) {
				// [Working copy] XXX.java[*]: {PRIMARY RESOURCE}
				return true;
			}
			return false;
		}

		private void processDeltaChildren(IJavaElementDelta delta,
				Map<IJavaProject, ProjectChangeInfo> changedProjects) {
			for (IJavaElementDelta c : delta.getAffectedChildren()) {
				processDelta(c, changedProjects);
			}
		}

		private static String getQualifiedName(ICompilationUnit cu) {
			String packageName = "";
			if (cu.getParent() instanceof IPackageFragment) {
				packageName = ((IPackageFragment) cu.getParent()).getElementName();
			}
			String typeName = cu.getElementName();
			if (typeName.endsWith(".java")) {
				typeName = typeName.substring(0, typeName.length() - ".java".length());
			}
			return packageName.isEmpty() ? typeName : packageName + "." + typeName;
		}

		private ProjectChangeInfo getProjectInfo(IJavaProject project,
				Map<IJavaProject, ProjectChangeInfo> changedProjects) {
			ProjectChangeInfo projectInfo = changedProjects.get(project);
			if (projectInfo == null) {
				projectInfo = new ProjectChangeInfo(JDTQuteProjectUtils.getProjectUri(project));
				changedProjects.put(project, projectInfo);
			}
			return projectInfo;
		}

		private boolean isCreatedOrDeleted(IJavaElementDelta delta) {
			int kind = delta.getKind();
			return kind == IJavaElementDelta.ADDED || kind == IJavaElementDelta.REMOVED;
		}

		private boolean isClasspathChanged(int flags) {
			return 0 != (flags & (IJavaElementDelta.F_CLASSPATH_CHANGED | IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED
					| IJavaElementDelta.F_CLOSED | IJavaElementDelta.F_OPENED));
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
							LOGGER.log(Level.SEVERE, "Error while sending LSP 'qute/javaDataModelChanged' notification",
									e);
						}
					}
				}
			});
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
	}

	/**
	 * Destroy the classpath listener manager.
	 */
	public synchronized void destroy() {
		if (quteListener != null) {
			JavaCore.removeElementChangedListener(quteListener);
			this.quteListener = null;
		}
	}

}
