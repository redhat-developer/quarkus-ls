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

import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.QUARKUS_PROPERTIES_CHANGED_COMMAND;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.managers.IBuildSupport;
import org.eclipse.lsp4j.Location;

import com.redhat.quarkus.commons.QuarkusPropertiesScope;
import com.redhat.quarkus.jdt.core.DocumentationConverter;
import com.redhat.quarkus.jdt.core.IQuarkusPropertiesChangedListener;
import com.redhat.quarkus.jdt.core.JDTQuarkusManager;
import com.redhat.quarkus.jdt.internal.core.QuarkusPropertiesListenerManager;

/**
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusDelegateCommandHandler implements IDelegateCommandHandler {

	public static final String PROJECT_INFO_COMMAND_ID = "quarkus.java.projectInfo";

	public static final String PROPERTY_DEFINITION_COMMAND_ID = "quarkus.java.propertyDefinition";

	private static final IQuarkusPropertiesChangedListener LISTENER = (event) -> {
		JavaLanguageServerPlugin.getInstance().getClientConnection()
				.executeClientCommand(QUARKUS_PROPERTIES_CHANGED_COMMAND, event);
	};

	/**
	 * Markdown is supported as a content format.
	 */
	public static final String MARKDOWN = "markdown";

	public QuarkusDelegateCommandHandler() {
		// Add a classpath changed listener to execute client command
		// "quarkusTools.classpathChanged"
		QuarkusPropertiesListenerManager.getInstance().addQuarkusPropertiesChangedListener(LISTENER);
	}

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress) throws Exception {
		if (PROJECT_INFO_COMMAND_ID.equals(commandId)) {
			return getQuarkusProjectInfo(arguments, commandId, progress);
		} else if (PROPERTY_DEFINITION_COMMAND_ID.equals(commandId)) {
			return findDeclaredQuarkusProperty(arguments, commandId, progress);
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
		Map<String, Object> obj = getFirst(arguments);
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
		Number scope = (Number) obj.get("scope");
		QuarkusPropertiesScope propertiesScope = QuarkusPropertiesScope.forValue(scope.intValue());
		// Get converter to use for JavaDoc
		String[] documentationFormat = (String[]) obj.get("documentationFormat");
		DocumentationConverter converter = getDocumentationConverter(documentationFormat);
		return JDTQuarkusManager.getInstance().getQuarkusProjectInfo(file, propertiesScope, converter, progress);
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

	private static Location findDeclaredQuarkusProperty(List<Object> arguments, String commandId,
			IProgressMonitor progress) throws CoreException {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be call with one QuarkusPropertyDefinitionParams argument!", commandId));
		}
		// Get project name from the application.properties URI
		String applicationPropertiesUri = (String) obj.get("uri");
		if (applicationPropertiesUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be call with required QuarkusPropertyDefinitionParams.uri (application.properties URI)!",
					commandId));
		}
		IFile file = JDTUtils.findFile(applicationPropertiesUri);
		if (file == null) {
			throw new UnsupportedOperationException(
					String.format("Cannot find IFile for '%s'", applicationPropertiesUri));
		}
		String propertySource = (String) obj.get("propertySource");
		if (propertySource == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be call with required QuarkusPropertyDefinitionParams.propertySource!",
					commandId));
		}
		return findQuarkusPropertyLocation(file, propertySource, progress);
	}

	public static Location findQuarkusPropertyLocation(IFile file, String propertySource, IProgressMonitor progress)
			throws JavaModelException, CoreException {
		IMember fieldOrMethod = JDTQuarkusManager.getInstance().findDeclaredQuarkusProperty(file, propertySource, progress);
		if (fieldOrMethod != null) {
			IClassFile classFile = fieldOrMethod.getClassFile();
			if (classFile != null) {
				// Try to download source if required
				Optional<IBuildSupport> bs = JavaLanguageServerPlugin.getProjectsManager()
						.getBuildSupport(file.getProject());
				if (bs.isPresent()) {
					bs.get().discoverSource(classFile, progress);
				}
			}
			return JDTUtils.toLocation(fieldOrMethod);
		}
		return null;
	}

	private static Map<String, Object> getFirst(List<Object> arguments) {
		return arguments.isEmpty() ? null : (Map<String, Object>) arguments.get(0);
	}

}
