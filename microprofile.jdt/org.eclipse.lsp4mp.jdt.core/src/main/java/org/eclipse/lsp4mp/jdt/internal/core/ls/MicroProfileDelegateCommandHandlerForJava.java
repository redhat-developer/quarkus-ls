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

import static org.eclipse.lsp4mp.jdt.internal.core.ls.ArgumentUtils.getBoolean;
import static org.eclipse.lsp4mp.jdt.internal.core.ls.ArgumentUtils.getCodeActionContext;
import static org.eclipse.lsp4mp.jdt.internal.core.ls.ArgumentUtils.getFirst;
import static org.eclipse.lsp4mp.jdt.internal.core.ls.ArgumentUtils.getInt;
import static org.eclipse.lsp4mp.jdt.internal.core.ls.ArgumentUtils.getPosition;
import static org.eclipse.lsp4mp.jdt.internal.core.ls.ArgumentUtils.getRange;
import static org.eclipse.lsp4mp.jdt.internal.core.ls.ArgumentUtils.getString;
import static org.eclipse.lsp4mp.jdt.internal.core.ls.ArgumentUtils.getStringList;
import static org.eclipse.lsp4mp.jdt.internal.core.ls.ArgumentUtils.getTextDocumentIdentifier;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeActionParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeLensParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaHoverParams;
import org.eclipse.lsp4mp.jdt.core.PropertiesManagerForJava;

/**
 * JDT LS delegate command handler for Java file..
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileDelegateCommandHandlerForJava extends AbstractMicroProfileDelegateCommandHandler {

	private static final String JAVA_CODEACTION_COMMAND_ID = "microprofile/java/codeAction";
	private static final String JAVA_CODELENS_COMMAND_ID = "microprofile/java/codeLens";
	private static final String JAVA_DIAGNOSTICS_COMMAND_ID = "microprofile/java/diagnostics";
	private static final String JAVA_HOVER_COMMAND_ID = "microprofile/java/hover";

	public MicroProfileDelegateCommandHandlerForJava() {
	}

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress) throws Exception {
		switch (commandId) {
		case JAVA_CODEACTION_COMMAND_ID:
			return getCodeActionForJava(arguments, commandId, progress);
		case JAVA_CODELENS_COMMAND_ID:
			return getCodeLensForJava(arguments, commandId, progress);
		case JAVA_DIAGNOSTICS_COMMAND_ID:
			return getDiagnosticsForJava(arguments, commandId, progress);
		case JAVA_HOVER_COMMAND_ID:
			return getHoverForJava(arguments, commandId, progress);
		default:
			throw new UnsupportedOperationException(String.format("Unsupported command '%s'!", commandId));
		}
	}

	/**
	 * Returns the code action for the given Java file.
	 * 
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return the code action for the given Java file.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private static List<? extends CodeAction> getCodeActionForJava(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		// Create java code action parameter
		MicroProfileJavaCodeActionParams params = createMicroProfileJavaCodeActionParams(arguments, commandId);
		// Return code lenses from the lens parameter
		return PropertiesManagerForJava.getInstance().codeAction(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	/**
	 * Create java code action parameter from the given arguments map.
	 * 
	 * @param arguments
	 * @param commandId
	 * 
	 * @return java code action parameter
	 */
	private static MicroProfileJavaCodeActionParams createMicroProfileJavaCodeActionParams(List<Object> arguments,
			String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with one MicroProfileJavaCodeActionParams argument!", commandId));
		}
		TextDocumentIdentifier texdDocumentIdentifier = getTextDocumentIdentifier(obj, "textDocument");
		if (texdDocumentIdentifier == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required MicroProfileJavaCodeActionParams.texdDocumentIdentifier",
					commandId));
		}
		Range range = getRange(obj, "range");
		CodeActionContext context = getCodeActionContext(obj, "context");
		MicroProfileJavaCodeActionParams params = new MicroProfileJavaCodeActionParams();
		params.setTextDocument(texdDocumentIdentifier);
		params.setRange(range);
		params.setContext(context);
		return params;
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
		// Create java code lens parameter
		MicroProfileJavaCodeLensParams params = createMicroProfileJavaCodeLensParams(arguments, commandId);
		// Return code lenses from the lens parameter
		return PropertiesManagerForJava.getInstance().codeLens(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	/**
	 * Create java code lens parameter from the given arguments map.
	 * 
	 * @param arguments
	 * @param commandId
	 * 
	 * @return java code lens parameter
	 */
	private static MicroProfileJavaCodeLensParams createMicroProfileJavaCodeLensParams(List<Object> arguments,
			String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one MicroProfileJavaCodeLensParams argument!", commandId));
		}
		String javaFileUri = getString(obj, "uri");
		if (javaFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required MicroProfileJavaCodeLensParams.uri (java URI)!",
					commandId));
		}
		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams(javaFileUri);
		params.setUrlCodeLensEnabled(getBoolean(obj, "urlCodeLensEnabled"));
		params.setCheckServerAvailable(getBoolean(obj, "checkServerAvailable"));
		params.setOpenURICommand(getString(obj, "openURICommand"));
		params.setLocalServerPort(getInt(obj, "localServerPort"));
		return params;
	}

	/**
	 * 
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return
	 * @throws JavaModelException
	 */
	private static List<PublishDiagnosticsParams> getDiagnosticsForJava(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException {
		// Create java diagnostics parameter
		MicroProfileJavaDiagnosticsParams params = createMicroProfileJavaDiagnosticsParams(arguments, commandId);
		// Return diagnostics from parameter
		return PropertiesManagerForJava.getInstance().diagnostics(params, JDTUtilsLSImpl.getInstance(), monitor);

	}

	/**
	 * Returns the java diagnostics parameters from the given arguments map.
	 * 
	 * @param arguments
	 * @param commandId
	 * 
	 * @return the java diagnostics parameters
	 */
	private static MicroProfileJavaDiagnosticsParams createMicroProfileJavaDiagnosticsParams(List<Object> arguments,
			String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with one MicroProfileJavaDiagnosticsParams argument!", commandId));
		}
		List<String> javaFileUri = getStringList(obj, "uris");
		if (javaFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required MicroProfileJavaDiagnosticsParams.uri (java URIs)!",
					commandId));
		}
		return new MicroProfileJavaDiagnosticsParams(javaFileUri);
	}

	/**
	 * Returns the <code>MicroProfileJavaHoverInfo</code> for the hover described in
	 * <code>arguments</code>
	 * 
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	private static Hover getHoverForJava(List<Object> arguments, String commandId, IProgressMonitor monitor)
			throws JavaModelException, CoreException {
		// Create java hover parameter
		MicroProfileJavaHoverParams params = createMicroProfileJavaHoverParams(arguments, commandId);
		// Return hover info from hover parameter
		return PropertiesManagerForJava.getInstance().hover(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	/**
	 * Returns the java hover parameters from the given arguments map.
	 * 
	 * @param arguments
	 * @param commandId
	 * 
	 * @return the java hover parameters
	 */
	private static MicroProfileJavaHoverParams createMicroProfileJavaHoverParams(List<Object> arguments,
			String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one MicroProfileJavaHoverParams argument!", commandId));
		}
		String javaFileUri = getString(obj, "uri");
		if (javaFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required MicroProfileJavaHoverParams.uri (java URI)!", commandId));
		}

		Position hoverPosition = getPosition(obj, "position");
		DocumentFormat documentFormat = DocumentFormat.PlainText;
		Number documentFormatIndex = (Number) obj.get("documentFormat");
		if (documentFormatIndex != null) {
			documentFormat = DocumentFormat.forValue(documentFormatIndex.intValue());
		}
		return new MicroProfileJavaHoverParams(javaFileUri, hoverPosition, documentFormat);
	}
}
