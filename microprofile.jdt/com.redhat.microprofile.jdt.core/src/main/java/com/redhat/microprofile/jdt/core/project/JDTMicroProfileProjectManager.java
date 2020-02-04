/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.project;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.microprofile.jdt.internal.core.FakeJavaProject;

/**
 * {@link JDTMicroProfileProject} manager.
 * 
 * @author Angelo ZERR
 *
 */
public class JDTMicroProfileProjectManager {

	private static final JDTMicroProfileProjectManager INSTANCE = new JDTMicroProfileProjectManager();

	public static JDTMicroProfileProjectManager getInstance() {
		return INSTANCE;
	}

	private final Map<IJavaProject, JDTMicroProfileProject> projects;

	private JDTMicroProfileProjectManager() {
		this.projects = new HashMap<>();
	}

	public JDTMicroProfileProject getJDTMicroProfileProject(IJavaProject project) throws JavaModelException {
		IJavaProject javaProject = FakeJavaProject.getRealJavaProject(project);
		JDTMicroProfileProject info = projects.get(javaProject);
		if (info == null) {
			info = new JDTMicroProfileProject(javaProject);
			projects.put(javaProject, info);
		}
		return info;
	}
}
