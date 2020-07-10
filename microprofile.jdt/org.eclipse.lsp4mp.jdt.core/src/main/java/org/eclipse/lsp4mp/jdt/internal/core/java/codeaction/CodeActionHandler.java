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
package org.eclipse.lsp4mp.jdt.internal.core.java.codeaction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.manipulation.CoreASTProvider;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeActionParams;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.ExtendedCodeAction;
import org.eclipse.lsp4mp.jdt.core.java.codeaction.JavaCodeActionContext;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.core.java.JavaFeaturesRegistry;
import org.eclipse.lsp4mp.jdt.internal.core.java.corrections.DiagnosticsHelper;

/**
 * Code action handler.
 * 
 * @author Angelo ZERR
 *
 */
public class CodeActionHandler {

	/**
	 * 
	 * @param params
	 * @param utils
	 * @param monitor
	 * @return
	 */
	public List<? extends CodeAction> codeAction(MicroProfileJavaCodeActionParams params, IJDTUtils utils,
			IProgressMonitor monitor) {
		// Get the compilation unit
		String uri = params.getUri();
		ICompilationUnit unit = utils.resolveCompilationUnit(uri);
		if (unit == null) {
			return Collections.emptyList();
		}

		// Prepare the code action invocation context
		int start = DiagnosticsHelper.getStartOffset(unit, params.getRange(), utils);
		int end = DiagnosticsHelper.getEndOffset(unit, params.getRange(), utils);
		JavaCodeActionContext context = new JavaCodeActionContext(unit, start, end - start, utils, params);
		context.setASTRoot(getASTRoot(unit, monitor));

		// Collect the available code action kinds
		List<String> codeActionKinds = new ArrayList<>();
		if (params.getContext().getOnly() != null && !params.getContext().getOnly().isEmpty()) {
			codeActionKinds.addAll(params.getContext().getOnly());
		} else {
			List<String> defaultCodeActionKinds = Arrays.asList(//
					CodeActionKind.QuickFix, //
					CodeActionKind.Refactor, //
					// JavaCodeActionKind.QUICK_ASSIST,
					CodeActionKind.Source);
			codeActionKinds.addAll(defaultCodeActionKinds);
		}

		List<CodeAction> codeActions = new ArrayList<>();
		Map<String, List<JavaCodeActionDefinition>> forDiagnostics = new HashMap<>();

		// Loop for each code action kinds to process the proper code actions
		for (String codeActionKind : codeActionKinds) {
			// Get list of code action definition for the given kind
			List<JavaCodeActionDefinition> codeActionDefinitions = JavaFeaturesRegistry.getInstance()
					.getJavaCodeActionDefinitions(codeActionKind).stream()
					.filter(definition -> definition.isAdaptedForCodeAction(context, monitor))
					.collect(Collectors.toList());
			if (codeActionDefinitions != null) {
				// Loop for each code action definition
				for (JavaCodeActionDefinition definition : codeActionDefinitions) {
					String forDiagnostic = definition.getTargetDiagnostic();
					if (forDiagnostic != null) {
						// The code action definition is for a given diagnostic code (QuickFix), store
						// it
						List<JavaCodeActionDefinition> definitionsFor = forDiagnostics.get(forDiagnostic);
						if (definitionsFor == null) {
							definitionsFor = new ArrayList<>();
							forDiagnostics.put(forDiagnostic, definitionsFor);
						}
						definitionsFor.add(definition);
					} else {
						// Collect the code actions
						codeActions.addAll(definition.getCodeActions(context, null, monitor));
					}
				}
			}
		}

		if (!forDiagnostics.isEmpty()) {
			// It exists code action to fix diagnostics, loop for each diagnostics
			params.getContext().getDiagnostics().forEach(diagnostic -> {
				String code = getCode(diagnostic);
				if (code != null) {
					// Try to get code action definition registered with the "for" source#code
					String key = diagnostic.getSource() + "#" + code;
					List<JavaCodeActionDefinition> definitionsFor = forDiagnostics.get(key);
					if (definitionsFor == null) {
						// Try to get code action definition registered with the "for" code
						definitionsFor = forDiagnostics.get(code);
					}
					if (definitionsFor != null) {
						for (JavaCodeActionDefinition definition : definitionsFor) {
							// Collect the code actions to fix the given diagnostic
							codeActions.addAll(definition.getCodeActions(context, diagnostic, monitor));
						}
					}
				}
			});
		}
		// sort code actions by relevant
		ExtendedCodeAction.sort(codeActions);
		return codeActions;
	}

	private static CompilationUnit getASTRoot(ICompilationUnit unit, IProgressMonitor monitor) {
		return CoreASTProvider.getInstance().getAST(unit, CoreASTProvider.WAIT_YES, monitor);
	}

	private static String getCode(Diagnostic diagnostic) {
		Object code = null;
		try {
			Field f = diagnostic.getClass().getDeclaredField("code");
			f.setAccessible(true);
			code = f.get(diagnostic);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getCodeString(code);
	}

	private static String getCodeString(Object codeObject) {
		if (codeObject instanceof String) {
			return ((String) codeObject);
		}
		@SuppressWarnings("unchecked")
		Either<String, Number> code = (Either<String, Number>) codeObject;
		if (code == null || code.isRight()) {
			return null;
		}
		return code.getLeft();
	}
}
