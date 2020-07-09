/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.providers;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.microprofile.jdt.core.IProjectLabelProvider;
import com.redhat.microprofile.jdt.internal.quarkus.JDTQuarkusUtils;;

/**
 * Provides a Quarkus-specific label to a project if the project is a
 * Quarkus project
 *
 * @author dakwon
 *
 */
public class QuarkusProjectLabelProvider implements IProjectLabelProvider {
	
	public static String QUARKUS_LABEL = "quarkus";

	@Override
	public List<String> getProjectLabels(IJavaProject project) throws JavaModelException {
		if (JDTQuarkusUtils.isQuarkusProject(project)) {
			return Collections.singletonList(QUARKUS_LABEL);
		};
		return Collections.emptyList();
	}
}
