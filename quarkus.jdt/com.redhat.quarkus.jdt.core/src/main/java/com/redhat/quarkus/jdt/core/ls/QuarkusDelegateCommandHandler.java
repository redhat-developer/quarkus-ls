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

import com.redhat.quarkus.jdt.core.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.jdt.core.JDTQuarkusManager;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;

/**
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusDelegateCommandHandler implements IDelegateCommandHandler {

	public static final String COMMAND_ID = "com.redhat.jdtls.quarkus.quarkus.jdt.ls.samplecommand";

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress) throws Exception {
		if (COMMAND_ID.equals(commandId)) {
			String projectName = (String) arguments.get(0);
			return JDTQuarkusManager.getInstance().getQuarkusProjectInfo(projectName);
		}
		throw new UnsupportedOperationException(String.format("Unsupported command '%s'!", commandId));
	}

}
