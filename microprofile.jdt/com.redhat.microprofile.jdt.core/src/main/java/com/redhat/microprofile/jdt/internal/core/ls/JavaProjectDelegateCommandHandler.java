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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;

import com.redhat.microprofile.jdt.core.ProjectLabelManager;

/**
 * Delegate command handler for Java project information
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
