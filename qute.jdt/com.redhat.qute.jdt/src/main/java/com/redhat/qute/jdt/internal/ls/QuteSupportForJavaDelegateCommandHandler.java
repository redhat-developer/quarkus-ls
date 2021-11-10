/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.ls;

import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getFirst;
import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getString;
import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getStringList;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import com.redhat.qute.commons.QuteJavaCodeLensParams;
import com.redhat.qute.commons.QuteJavaDiagnosticsParams;
import com.redhat.qute.commons.QuteJavaDocumentLinkParams;
import com.redhat.qute.jdt.QuteSupportForJava;

/**
 * JDT LS commands used by Java files for Qute support.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteSupportForJavaDelegateCommandHandler extends AbstractQuteDelegateCommandHandler {

	private static final String QUTE_JAVA_CODELENS_COMMAND_ID = "qute/java/codeLens";

	private static final String QUTE_JAVA_DIAGNOSTICS_COMMAND_ID = "qute/java/diagnostics";

	private static final String QUTE_JAVA_DOCUMENT_LINK_COMMAND_ID = "qute/java/documentLink";

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		switch (commandId) {
		case QUTE_JAVA_CODELENS_COMMAND_ID:
			return getCodeLensForJava(arguments, commandId, monitor);
		case QUTE_JAVA_DIAGNOSTICS_COMMAND_ID:
			return getDiagnosticsForJava(arguments, commandId, monitor);
		case QUTE_JAVA_DOCUMENT_LINK_COMMAND_ID:
			return getDocumentLinkForJava(arguments, commandId, monitor);
		default:
			return null;
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
		QuteJavaCodeLensParams params = createQuteJavaCodeLensParams(arguments, commandId);
		// Return code lenses from the lens parameter
		return QuteSupportForJava.getInstance().codeLens(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	/**
	 * Create java code lens parameter from the given arguments map.
	 *
	 * @param arguments
	 * @param commandId
	 *
	 * @return java code lens parameter
	 */
	private static QuteJavaCodeLensParams createQuteJavaCodeLensParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be called with one QuteJavaCodeLensParams argument!", commandId));
		}
		String javaFileUri = getString(obj, "uri");
		if (javaFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteJavaCodeLensParams.uri (java URI)!", commandId));
		}
		return new QuteJavaCodeLensParams(javaFileUri);
	}

	/**
	 * Returns the diagnostics for the given Java files.
	 *
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return the diagnostics for the given Java files.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private static List<PublishDiagnosticsParams> getDiagnosticsForJava(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		QuteJavaDiagnosticsParams params = createQuteJavaDiagnosticsParams(arguments, commandId);
		// Return code lenses from diagnostics parameter
		return QuteSupportForJava.getInstance().diagnostics(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	/**
	 * Create java diagnostics parameter from the given arguments map.
	 *
	 * @param arguments
	 * @param commandId
	 *
	 * @return java code lens parameter
	 */
	private static QuteJavaDiagnosticsParams createQuteJavaDiagnosticsParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one QuteJavaDiagnosticsParams argument!", commandId));
		}
		List<String> javaFileUris = getStringList(obj, "uris");
		if (javaFileUris == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteJavaDiagnosticsParams.uris (java URIs)!",
					commandId));
		}
		return new QuteJavaDiagnosticsParams(javaFileUris);
	}

	/**
	 * Returns the document link for the given Java file.
	 *
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return the code lenses for the given Java file.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private static List<DocumentLink> getDocumentLinkForJava(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		QuteJavaDocumentLinkParams params = createQuteJavaDocumentLinkParams(arguments, commandId);
		// Return document link from the document link parameter
		return QuteSupportForJava.getInstance().documentLink(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	/**
	 * Create java document link parameter from the given arguments map.
	 *
	 * @param arguments
	 * @param commandId
	 *
	 * @return java document link parameter
	 */
	private static QuteJavaDocumentLinkParams createQuteJavaDocumentLinkParams(List<Object> arguments,
			String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one QuteJavaDocumentLinkParams argument!", commandId));
		}
		String javaFileUri = getString(obj, "uri");
		if (javaFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteJavaDocumentLinkParams.uri (java URI)!", commandId));
		}
		return new QuteJavaDocumentLinkParams(javaFileUri);
	}

}
