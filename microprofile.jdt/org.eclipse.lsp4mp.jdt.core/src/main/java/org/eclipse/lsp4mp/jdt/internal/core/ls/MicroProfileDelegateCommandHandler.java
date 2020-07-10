/*******************************************************************************
* Copyright (c) 2019-2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.core.ls;

import static org.eclipse.lsp4mp.jdt.internal.core.ls.ArgumentUtils.getFirst;
import static org.eclipse.lsp4mp.jdt.internal.core.ls.ArgumentUtils.getString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfoParams;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.commons.MicroProfilePropertyDefinitionParams;
import org.eclipse.lsp4mp.jdt.core.PropertiesManager;

/**
 * JDT LS delegate command handler for application.properties file.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileDelegateCommandHandler extends AbstractMicroProfileDelegateCommandHandler {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileDelegateCommandHandler.class.getName());

	private static final String PROJECT_INFO_COMMAND_ID = "microprofile/projectInfo";

	private static final String PROPERTY_DEFINITION_COMMAND_ID = "microprofile/propertyDefinition";

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
	 * Returns the MicroProfile project information
	 * 
	 * @param arguments
	 * @param commandId
	 * @param progress
	 * @return
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private static MicroProfileProjectInfo getMicroProfileProjectInfo(List<Object> arguments, String commandId,
			IProgressMonitor progress) throws Exception {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one MicroProfileProjectInfoParams argument!", commandId));
		}
		// Get project name from the application.properties URI
		String applicationPropertiesUri = getString(obj, "uri");
		if (applicationPropertiesUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required MicroProfileProjectInfoParams.uri (application.properties URI)!",
					commandId));
		}
		List<Number> scopesIndex = (List<Number>) obj.get("scopes");
		List<MicroProfilePropertiesScope> scopes = new ArrayList<>();
		for (Number scopeIndex : scopesIndex) {
			scopes.add(MicroProfilePropertiesScope.forValue(scopeIndex.intValue()));
		}
		DocumentFormat documentFormat = DocumentFormat.PlainText;
		Number documentFormatIndex = (Number) obj.get("documentFormat");
		if (documentFormatIndex != null) {
			documentFormat = DocumentFormat.forValue(documentFormatIndex.intValue());
		}
		MicroProfileProjectInfoParams params = new MicroProfileProjectInfoParams(applicationPropertiesUri);
		params.setScopes(scopes);
		params.setDocumentFormat(documentFormat);

		// Execute the getMicroProfileProjectInfo in a Job to benefit with progress
		// monitor
		final MicroProfileProjectInfo[] projectInfo = new MicroProfileProjectInfo[1];
		Job job = Job.create("MicroProfile properties collector", monitor -> {
			projectInfo[0] = PropertiesManager.getInstance().getMicroProfileProjectInfo(params,
					JDTUtilsLSImpl.getInstance(), monitor);
		});
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, "Error while joining MicroProfile properties collector job", e);
		}

		Exception jobException = (Exception) job.getResult().getException();
		if (jobException != null) {
			if (jobException.getCause() != null) {
				throw (Exception) jobException.getCause();
			}
			throw jobException;
		}
		;

		return projectInfo[0];
	}

	private static Location findDeclaredProperty(List<Object> arguments, String commandId, IProgressMonitor progress)
			throws CoreException {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with one MicroProfilePropertyDefinitionParams argument!", commandId));
		}
		// Get project name from the application.properties URI
		String applicationPropertiesUri = getString(obj, "uri");
		if (applicationPropertiesUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required MicroProfilePropertyDefinitionParams.uri (application.properties URI)!",
					commandId));
		}
		String sourceType = getString(obj, "sourceType");
		if (sourceType == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required MicroProfilePropertyDefinitionParams.sourceType!",
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
