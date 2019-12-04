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

import static com.redhat.microprofile.jdt.internal.core.ls.ArgumentUtils.getFirst;
import static com.redhat.microprofile.jdt.internal.core.ls.ArgumentUtils.getString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.Location;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfileProjectInfoParams;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.commons.MicroProfilePropertyDefinitionParams;
import com.redhat.microprofile.jdt.core.IMicroProfilePropertiesChangedListener;
import com.redhat.microprofile.jdt.core.PropertiesManager;
import com.redhat.microprofile.jdt.internal.core.MicroProfilePropertiesListenerManager;

/**
 * JDT LS delegate command handler for application.properties file.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileDelegateCommandHandler implements IDelegateCommandHandler {

	private static final String PROJECT_INFO_COMMAND_ID = "microprofile/projectInfo";

	private static final String PROPERTY_DEFINITION_COMMAND_ID = "microprofile/propertyDefinition";

	/**
	 * MicroProfile client commands
	 */
	private static final String MICROPROFILE_PROPERTIES_CHANGED_COMMAND = "microprofile/propertiesChanged";

	private static final IMicroProfilePropertiesChangedListener LISTENER = (event) -> {
		JavaLanguageServerPlugin.getInstance().getClientConnection()
				.executeClientCommand(MICROPROFILE_PROPERTIES_CHANGED_COMMAND, event);
	};

	public MicroProfileDelegateCommandHandler() {
		// Add a classpath changed listener to execute client command
		// "quarkusTools.classpathChanged"
		MicroProfilePropertiesListenerManager.getInstance().addMicroProfilePropertiesChangedListener(LISTENER);
	}

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress) throws Exception {
		switch (commandId) {
		case PROJECT_INFO_COMMAND_ID:
			return getMicroProfileProjectInfo(arguments, commandId, progress);
		case PROPERTY_DEFINITION_COMMAND_ID:
			return findDeclaredProperty(arguments, commandId, progress);
		default:
			throw new UnsupportedOperationException(String.format("Unsupported command '%s'!", commandId));
		}
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
	private static MicroProfileProjectInfo getMicroProfileProjectInfo(List<Object> arguments, String commandId,
			IProgressMonitor progress) throws JavaModelException, CoreException {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be call with one MicroProfileProjectInfoParams argument!", commandId));
		}
		// Get project name from the application.properties URI
		String applicationPropertiesUri = getString(obj, "uri");
		if (applicationPropertiesUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be call with required MicroProfileProjectInfoParams.uri (application.properties URI)!",
					commandId));
		}
		List<Number> scopesIndex = (List<Number>) obj.get("scopes");
		List<MicroProfilePropertiesScope> scopes = new ArrayList<>();
		for (Number scopeIndex : scopesIndex) {
			scopes.add(MicroProfilePropertiesScope.forValue(scopeIndex.intValue()));
		}
		MicroProfileProjectInfoParams params = new MicroProfileProjectInfoParams(applicationPropertiesUri);
		params.setScopes(scopes);
		return PropertiesManager.getInstance().getMicroProfileProjectInfo(params, JDTUtilsLSImpl.getInstance(),
				progress);
	}

	private static Location findDeclaredProperty(List<Object> arguments, String commandId, IProgressMonitor progress)
			throws CoreException {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be call with one MicroProfilePropertyDefinitionParams argument!", commandId));
		}
		// Get project name from the application.properties URI
		String applicationPropertiesUri = getString(obj, "uri");
		if (applicationPropertiesUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be call with required MicroProfilePropertyDefinitionParams.uri (application.properties URI)!",
					commandId));
		}
		String sourceType = getString(obj, "sourceType");
		if (sourceType == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be call with required MicroProfilePropertyDefinitionParams.sourceType!",
					commandId));
		}
		String sourceField = getString(obj, "sourceField");
		String sourceMethod = getString(obj, "sourceMethod");

		MicroProfilePropertyDefinitionParams params = new MicroProfilePropertyDefinitionParams();
		params.setUri(applicationPropertiesUri);
		params.setSourceType(sourceType);
		params.setSourceField(sourceField);
		params.setSourceMethod(sourceMethod);
		return PropertiesManager.getInstance().findPropertyLocation(params, JDTUtilsLSImpl.getInstance(), progress);
	}

}
