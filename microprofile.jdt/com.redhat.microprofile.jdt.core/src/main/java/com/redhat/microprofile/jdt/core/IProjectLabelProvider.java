/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Project label provider API
 * 
 * @author dakwon
 *
 */
public interface IProjectLabelProvider {
	
	/**
	 * Returns a list of project labels ("maven", "microprofile", etc.) for the given project
	 * @param project the project to get labels for
	 * @return a list of project labels for the given project
	 * @throws JavaModelException
	 */
	List<String> getProjectLabels(IJavaProject project) throws JavaModelException;
}
