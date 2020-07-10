/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.core.providers;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.jdt.core.IProjectLabelProvider;
import org.eclipse.lsp4mp.jdt.core.utils.JDTMicroProfileUtils;;

/**
 * Provides a MicroProfile-specific label to a project if the project is a
 * MicroProfile project
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfileProjectLabelProvider implements IProjectLabelProvider {
	
	public static String MICROPROFILE_LABEL = "microprofile";

	@Override
	public List<String> getProjectLabels(IJavaProject project) throws JavaModelException {
		if (JDTMicroProfileUtils.isMicroProfileProject(project)) {
			return Collections.singletonList(MICROPROFILE_LABEL);
		};
		return Collections.emptyList();
	}
}
