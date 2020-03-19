/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core.java;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.Assert;

import com.redhat.microprofile.commons.MicroProfileJavaCodeActionParams;
import com.redhat.microprofile.commons.MicroProfileJavaDiagnosticsParams;
import com.redhat.microprofile.jdt.core.PropertiesManagerForJava;
import com.redhat.microprofile.jdt.core.java.diagnostics.IJavaErrorCode;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;

/**
 * MicroProfile assert for java files for JUnit tests.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileForJavaAssert {

	// ------------------- CodeAction assert

	public static MicroProfileJavaCodeActionParams createCodeActionParams(String uri, Diagnostic d) {
		TextDocumentIdentifier textDocument = new TextDocumentIdentifier(uri);
		Range range = d.getRange();
		CodeActionContext context = new CodeActionContext();
		context.setDiagnostics(Arrays.asList(d));
		MicroProfileJavaCodeActionParams codeActionParams = new MicroProfileJavaCodeActionParams(textDocument, range,
				context);
		codeActionParams.setResourceOperationSupported(true);
		return codeActionParams;
	}
	
	public static void assertJavaCodeAction(MicroProfileJavaCodeActionParams params, IJDTUtils utils,
			CodeAction... expected) throws JavaModelException {
		List<? extends CodeAction> actual = PropertiesManagerForJava.getInstance().codeAction(params, utils,
				new NullProgressMonitor());
		assertCodeActions(actual != null && actual.size() > 0 ? actual : Collections.emptyList(), expected);
	}

	public static void assertCodeActions(List<? extends CodeAction> actual, CodeAction... expected) {
		actual.stream().forEach(ca -> {
			// we don't want to compare title, etc
			ca.setCommand(null);
			ca.setKind(null);
			if (ca.getDiagnostics() != null) {
				ca.getDiagnostics().forEach(d -> {
					d.setSeverity(null);
					d.setMessage("");
					d.setSource(null);
				});
			}
		});

		Assert.assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {			
			Assert.assertEquals("Assert title [" + i + "]", expected[i].getTitle(), actual.get(i).getTitle());
			Assert.assertEquals("Assert edit [" + i + "]", expected[i].getEdit(), actual.get(i).getEdit());
		}
	}

	public static CodeAction ca(String uri, String title, Diagnostic d, TextEdit... te) {
		CodeAction codeAction = new CodeAction();
		codeAction.setTitle(title);
		codeAction.setDiagnostics(Arrays.asList(d));

		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(uri, 0);

		TextDocumentEdit textDocumentEdit = new TextDocumentEdit(versionedTextDocumentIdentifier, Arrays.asList(te));
		WorkspaceEdit workspaceEdit = new WorkspaceEdit(Arrays.asList(Either.forLeft(textDocumentEdit)));
		workspaceEdit.setChanges(Collections.emptyMap());
		codeAction.setEdit(workspaceEdit);
		return codeAction;
	}

	public static TextEdit te(int startLine, int startCharacter, int endLine, int endCharacter, String newText) {
		TextEdit textEdit = new TextEdit();
		textEdit.setNewText(newText);
		textEdit.setRange(r(startLine, startCharacter, endLine, endCharacter));
		return textEdit;
	}

	// Assert for diagnostics

	public static Diagnostic d(int line, int startCharacter, int endCharacter, String message,
			DiagnosticSeverity severity, final String source, IJavaErrorCode code) {
		return d(line, startCharacter, line, endCharacter, message, severity, source, code);
	}

	public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, String message,
			DiagnosticSeverity severity, final String source, IJavaErrorCode code) {
		// Diagnostic on 1 line
		return new Diagnostic(r(startLine, startCharacter, endLine, endCharacter), message, severity, source,
				code != null ? code.getCode() : null);
	}

	public static Range r(int startLine, int startCharacter, int endLine, int endCharacter) {
		return new Range(new Position(startLine, startCharacter), new Position(endLine, endCharacter));
	}

	public static void assertJavaDiagnostics(MicroProfileJavaDiagnosticsParams params, IJDTUtils utils,
			Diagnostic... expected) throws JavaModelException {
		List<PublishDiagnosticsParams> actual = PropertiesManagerForJava.getInstance().diagnostics(params, utils,
				new NullProgressMonitor());
		assertDiagnostics(
				actual != null && actual.size() > 0 ? actual.get(0).getDiagnostics() : Collections.emptyList(),
				expected);
	}

	public static void assertDiagnostics(List<Diagnostic> actual, Diagnostic... expected) {
		assertDiagnostics(actual, Arrays.asList(expected), false);
	}

	public static void assertDiagnostics(List<Diagnostic> actual, List<Diagnostic> expected, boolean filter) {
		List<Diagnostic> received = actual;
		final boolean filterMessage;
		if (expected != null && !expected.isEmpty()
				&& (expected.get(0).getMessage() == null || expected.get(0).getMessage().isEmpty())) {
			filterMessage = true;
		} else {
			filterMessage = false;
		}
		if (filter) {
			received = actual.stream().map(d -> {
				Diagnostic simpler = new Diagnostic(d.getRange(), "");
				simpler.setCode(d.getCode());
				if (filterMessage) {
					simpler.setMessage(d.getMessage());
				}
				return simpler;
			}).collect(Collectors.toList());
		}
		Assert.assertEquals("Unexpected diagnostics:\n" + actual, expected, received);
	}

}
