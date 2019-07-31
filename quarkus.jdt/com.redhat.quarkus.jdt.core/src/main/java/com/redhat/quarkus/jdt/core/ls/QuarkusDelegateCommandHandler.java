/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.core.ls;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JDTUtils;

import com.redhat.quarkus.jdt.core.JDTQuarkusManager;

/**
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusDelegateCommandHandler implements IDelegateCommandHandler {

	public static final String PROJECT_INFO_COMMAND_ID = "quarkus.java.projectInfo";

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress) throws Exception {
		if (PROJECT_INFO_COMMAND_ID.equals(commandId)) {
			String applicationPropertiesUri = arguments.size() > 0 ? (String) arguments.get(0) : null;
			if (applicationPropertiesUri == null) {
				throw new UnsupportedOperationException(String.format(
						"Command '%s' must be call with one String argument (application.properties URI)!", commandId));
			}
			IFile file = JDTUtils.findFile(applicationPropertiesUri);
			if (file == null) {
				throw new UnsupportedOperationException(
						String.format("Cannot find IFile for '%s'", applicationPropertiesUri));
			}
			String projectName = file.getProject().getName();
			return JDTQuarkusManager.getInstance().getQuarkusProjectInfo(projectName);
		}
		throw new UnsupportedOperationException(String.format("Unsupported command '%s'!", commandId));
	}

}
