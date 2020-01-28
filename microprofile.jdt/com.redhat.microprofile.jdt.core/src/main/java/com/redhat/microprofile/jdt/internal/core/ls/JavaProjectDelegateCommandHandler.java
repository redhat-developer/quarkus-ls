/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core.ls;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.ProjectUtils;

import com.redhat.microprofile.jdt.core.ProjectLabelDefinition;
import com.redhat.microprofile.jdt.core.ProjectLabelManager;
import com.redhat.microprofile.jdt.core.utils.JDTMicroProfileUtils;
import com.redhat.microprofile.jdt.internal.core.ProjectLabelRegistry;


/**
 * Delegate command handler to determine project types in the workspace
 * 
 */
public class JavaProjectDelegateCommandHandler implements IDelegateCommandHandler {

	private static final String PROJECT_LABELS_COMMAND_ID = "microprofile/java/projectLabels";

	public JavaProjectDelegateCommandHandler() {
	}

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress) throws Exception {
		switch (commandId) {
		case PROJECT_LABELS_COMMAND_ID:
			return ProjectLabelManager.getInstance().getProjectLabelInfo();
		default:
			throw new UnsupportedOperationException(String.format("Unsupported command '%s'!", commandId));
		}
	}
}
