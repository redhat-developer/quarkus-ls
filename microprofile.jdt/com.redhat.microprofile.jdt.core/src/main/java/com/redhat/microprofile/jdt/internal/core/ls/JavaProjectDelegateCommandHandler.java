/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core.ls;

import static com.redhat.microprofile.jdt.internal.core.ls.ArgumentUtils.getFirst;
import static com.redhat.microprofile.jdt.internal.core.ls.ArgumentUtils.getString;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.redhat.microprofile.commons.MicroProfileJavaProjectLabelsParams;
import com.redhat.microprofile.jdt.core.ProjectLabelManager;

/**
 * Delegate command handler for Java project information
 * 
 */
public class JavaProjectDelegateCommandHandler extends AbstractMicroProfileDelegateCommandHandler {

	private static final String PROJECT_LABELS_COMMAND_ID = "microprofile/java/projectLabels";
	private static final String WORKSPACE_LABELS_COMMAND_ID = "microprofile/java/workspaceLabels";

	public JavaProjectDelegateCommandHandler() {
	}

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress) throws Exception {
		switch (commandId) {
		case PROJECT_LABELS_COMMAND_ID:
			return getProjectLabelInfo(arguments, commandId, progress);
		case WORKSPACE_LABELS_COMMAND_ID:
			return ProjectLabelManager.getInstance().getProjectLabelInfo();
		default:
			throw new UnsupportedOperationException(String.format("Unsupported command '%s'!", commandId));
		}
	}

	private static Object getProjectLabelInfo(List<Object> arguments, String commandId, IProgressMonitor monitor) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with one MicroProfileJavaProjectLabelsParams argument!", commandId));
		}
		// Get project name from the java file URI
		String javaFileUri = getString(obj, "uri");
		if (javaFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required MicroProfileJavaProjectLabelsParams.uri (java file URI)!",
					commandId));
		}
		MicroProfileJavaProjectLabelsParams params = new MicroProfileJavaProjectLabelsParams();
		params.setUri(javaFileUri);
		return ProjectLabelManager.getInstance().getProjectLabelInfo(params, JDTUtilsLSImpl.getInstance(), monitor);
	}
}
