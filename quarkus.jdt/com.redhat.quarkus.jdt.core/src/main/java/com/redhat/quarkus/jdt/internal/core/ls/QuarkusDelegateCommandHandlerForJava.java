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

import static com.redhat.quarkus.jdt.internal.core.utils.ArgumentUtils.getBoolean;
import static com.redhat.quarkus.jdt.internal.core.utils.ArgumentUtils.getFirst;
import static com.redhat.quarkus.jdt.internal.core.utils.ArgumentUtils.getInt;
import static com.redhat.quarkus.jdt.internal.core.utils.ArgumentUtils.getString;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Position;

import com.redhat.quarkus.commons.QuarkusJavaCodeLensParams;
import com.redhat.quarkus.commons.QuarkusJavaHoverInfo;
import com.redhat.quarkus.commons.QuarkusJavaHoverParams;
import com.redhat.quarkus.jdt.core.JDTQuarkusManagerForJava;

/**
 * Quarkus delegate command handler for Java file.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusDelegateCommandHandlerForJava implements IDelegateCommandHandler {

	private static final String JAVA_CODELENS_COMMAND_ID = "quarkus.java.codeLens";
	private static final String JAVA_HOVER_COMMAND_ID = "quarkus.java.hover";

	public QuarkusDelegateCommandHandlerForJava() {
	}

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress) throws Exception {
		switch (commandId) {
		case JAVA_CODELENS_COMMAND_ID:
			return getCodeLensForJava(arguments, commandId, progress);
		case JAVA_HOVER_COMMAND_ID:
			return getHoverForJava(arguments, commandId, progress);
		default:
			throw new UnsupportedOperationException(String.format("Unsupported command '%s'!", commandId));
		}
	}

	/**
	 * Returns the code lenses for the given Java file.
	 * 
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return the code lenses for the given Java file.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private static List<? extends CodeLens> getCodeLensForJava(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		// Create java code lens parameter<O
		QuarkusJavaCodeLensParams params = createQuarkusJavaCodeLensParams(arguments, commandId);
		// return code lenses from the lens parameter
		return JDTQuarkusManagerForJava.getInstance().codeLens(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	/**
	 * Create java code lens parameter from the given arguments map.
	 * 
	 * @param arguments
	 * @param commandId
	 * 
	 * @return java code lens parameter
	 */
	private static QuarkusJavaCodeLensParams createQuarkusJavaCodeLensParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be call with one QuarkusJavaCodeLensParams argument!", commandId));
		}
		String javaFileUri = getString(obj, "uri");
		if (javaFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be call with required QuarkusJavaCodeLensParams.uri (java URI)!", commandId));
		}
		QuarkusJavaCodeLensParams params = new QuarkusJavaCodeLensParams(javaFileUri);
		params.setUrlCodeLensEnabled(getBoolean(obj, "urlCodeLensEnabled"));
		params.setCheckServerAvailable(getBoolean(obj, "checkServerAvailable"));
		params.setOpenURICommand(getString(obj, "openURICommand"));
		params.setLocalServerPort(getInt(obj, "localServerPort"));
		return params;
	}
	
	/**
	 * Returns the <code>QuarkusJavaHoverInfo</code> for the hover described
	 * in <code>arguments</code>
	 * 
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	private static QuarkusJavaHoverInfo getHoverForJava(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		// Create java hover parameter
		QuarkusJavaHoverParams params = createQuarkusJavaHoverParams(arguments, commandId);
		// return hover info from hover parameter
		return JDTQuarkusManagerForJava.getInstance().hover(params, JDTUtilsLSImpl.getInstance(), monitor);
	}
	
	/**
	 * Returns the java hover parameters from the given arguments map.
	 * 
	 * @param arguments
	 * @param commandId
	 * 
	 * @return the java hover parameters
	 */
	private static QuarkusJavaHoverParams createQuarkusJavaHoverParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be call with one QuarkusJavaHoverParams argument!", commandId));
		}
		String javaFileUri = getString(obj, "uri");
		if (javaFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be call with required QuarkusJavaHoverParams.uri (java URI)!", commandId));
		}

		Map<String, Object> hoverPosition = (Map<String, Object>) obj.get("position");
		int line = getInt(hoverPosition, "line");
		int character = getInt(hoverPosition, "character");

		return new QuarkusJavaHoverParams(javaFileUri, new Position(line, character));
	}


}
