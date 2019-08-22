/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.internal.core.ls;

import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.QUARKUS_CLASSPATH_CHANGED_COMMAND;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

import com.redhat.quarkus.jdt.core.DocumentationConverter;
import com.redhat.quarkus.jdt.core.JDTQuarkusManager;
import com.redhat.quarkus.jdt.internal.core.IClasspathChangedListener;
import com.redhat.quarkus.jdt.internal.core.QuarkusClasspathListenerManager;

/**
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusDelegateCommandHandler implements IDelegateCommandHandler {

	public static final String PROJECT_INFO_COMMAND_ID = "quarkus.java.projectInfo";

	private static final IClasspathChangedListener LISTENER = (projectsToUpdate) -> {
		JavaLanguageServerPlugin.getInstance().getClientConnection()
				.executeClientCommand(QUARKUS_CLASSPATH_CHANGED_COMMAND, projectsToUpdate);
	};

	/**
	 * Markdown is supported as a content format.
	 */
	public static final String MARKDOWN = "markdown";

	public QuarkusDelegateCommandHandler() {
		// Add a classpath changed listener to execute client command
		// "quarkusTools.classpathChanged"
		QuarkusClasspathListenerManager.getInstance().addClasspathChangedListener(LISTENER);
	}

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
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private static Object getQuarkusProjectInfo(List<Object> arguments, String commandId, IProgressMonitor progress)
			throws JavaModelException, CoreException {
		Map<String, Object> obj = arguments.size() > 0 ? (Map<String, Object>) arguments.get(0) : null;
		if (obj == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be call with one QuarkusProjectInfoParams argument!", commandId));
		}
		// Get project name from the application.properties URI
		String applicationPropertiesUri = (String) obj.get("uri");
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
		String[] documentationFormat = (String[]) obj.get("documentationFormat");
		DocumentationConverter converter = getDocumentationConverter(documentationFormat);
		return JDTQuarkusManager.getInstance().getQuarkusProjectInfo(projectName, converter, progress);
	}

	/**
	 * Returns the proper documentation converter according the given
	 * <code>documentationFormat</code>.
	 * 
	 * @param documentationFormat the documentation format.
	 * @return the proper documentation converter according the given
	 *         <code>documentationFormat</code>.
	 */
	private static DocumentationConverter getDocumentationConverter(String[] documentationFormat) {
		if (documentationFormat != null) {
			for (String format : documentationFormat) {
				if (format.equals(MARKDOWN)) {
					// TODO manage MARKDOWN converter
				}
			}
		}
		return DocumentationConverter.DEFAULT_CONVERTER;
	}

}
