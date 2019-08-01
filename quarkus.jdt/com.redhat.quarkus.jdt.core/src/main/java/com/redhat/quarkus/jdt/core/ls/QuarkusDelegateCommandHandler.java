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

import com.redhat.quarkus.commons.QuarkusProjectInfoParams;
import com.redhat.quarkus.jdt.core.DocumentationConverter;
import com.redhat.quarkus.jdt.core.JDTQuarkusManager;

/**
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusDelegateCommandHandler implements IDelegateCommandHandler {

	public static final String PROJECT_INFO_COMMAND_ID = "quarkus.java.projectInfo";

	/**
	 * Markdown is supported as a content format.
	 */
	public static final String MARKDOWN = "markdown";

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress) throws Exception {
		if (PROJECT_INFO_COMMAND_ID.equals(commandId)) {
			return getQuarkusProjectInfo(arguments, commandId, progress);
		}
		throw new UnsupportedOperationException(String.format("Unsupported command '%s'!", commandId));
	}

	/**
	 * Returns the Quarkus project information
	 * 
	 * @param arguments
	 * @param commandId
	 * @param progress
	 * @return
	 */
	private static Object getQuarkusProjectInfo(List<Object> arguments, String commandId, IProgressMonitor progress) {
		Object firstArgs = arguments.size() > 0 ? arguments.get(0) : null;
		if (!(firstArgs instanceof QuarkusProjectInfoParams)) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be call with one QuarkusProjectInfoParams argument!", commandId));
		}
		QuarkusProjectInfoParams params = (QuarkusProjectInfoParams) firstArgs;
		// Get project name from the application.properties URI
		String applicationPropertiesUri = params.getUri();
		if (applicationPropertiesUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be call with required QuarkusProjectInfoParams.uri (application.properties URI)!",
					commandId));
		}
		IFile file = JDTUtils.findFile(applicationPropertiesUri);
		if (file == null) {
			throw new UnsupportedOperationException(
					String.format("Cannot find IFile for '%s'", applicationPropertiesUri));
		}
		String projectName = file.getProject().getName();
		// Get converter to use for JavaDoc

		return JDTQuarkusManager.getInstance().getQuarkusProjectInfo(projectName,
				getDocumentationConverter(params.getDocumentationFormat()), progress);
	}

	/**
	 * Returns the proper documentation converter according the given
	 * <code>documentationFormat</code>.
	 * 
	 * @param documentationFormat the documentation format.
	 * @return the proper documentation converter according the given
	 *         <code>documentationFormat</code>.
	 */
	private static DocumentationConverter getDocumentationConverter(List<String> documentationFormat) {
		if (documentationFormat != null && documentationFormat.contains(MARKDOWN)) {
			// TODO manage MARKDOWN converter
		}
		return DocumentationConverter.DEFAULT_CONVERTER;
	}

}
