/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.codeactions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.jaxrs.RestParam;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.CodeActionFactory;
import com.redhat.qute.ls.commons.LineIndentInfo;
import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.RangeOffset;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.JavaMemberResult;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Code actions for {@link QuteErrorCode#MissingExpectedInput}.
 *
 */
public class QuteCodeActionForMissingInputs extends AbstractQuteCodeAction {

	private static final Logger LOGGER = Logger.getLogger(QuteCodeActionForMissingInputs.class.getName());

	private static final String DEFAULT_INDENTATION = "  ";

	public QuteCodeActionForMissingInputs(JavaDataModelCache javaCache) {
		super(javaCache);
	}

	@Override
	public void doCodeActions(CodeActionRequest request, List<CompletableFuture<Void>> codeActionResolveFutures,
			List<CodeAction> codeActions) {
		try {
			Node node = request.getCoveredNode();
			if (node == null) {
				return;
			}
			Section section = node instanceof Section ? (Section) node : null;
			if (section == null) {
				return;
			}
			Template template = request.getTemplate();
			Diagnostic diagnostic = request.getDiagnostic();
			String projectUri = template.getProjectUri();

			int methodOffset = section.getParameters().get(0).getEnd() - 2;
			MethodPart methodPart = request.doFindNodeAt(template, methodOffset) instanceof MethodPart
					? (MethodPart) request.doFindNodeAt(template, methodOffset)
					: null;
			if (methodPart == null) {
				return;
			}

			ResolvedJavaTypeInfo baseResolvedType = request.getJavaTypeOfCoveredNode(javaCache);
			JavaMemberResult result = javaCache.findMethod(baseResolvedType, methodPart.getNamespace(),
					methodPart.getPartName(), new ArrayList<>(), javaCache.getJavaTypeFilter(projectUri,
							request.getSharedSettings().getNativeSettings()).isInNativeMode(),
					projectUri);
			if (result == null) {
				return;
			}

			Collection<RestParam> restParams = ((JavaMethodInfo) result.getMember()).getRestParameters();
			List<String> requiredInputNames = new ArrayList<>();
			List<String> allInputNames = new ArrayList<>();

			for (RestParam param : restParams) {
				if (param.isRequired()) {
					requiredInputNames.add(param.getName());
				}
				allInputNames.add(param.getName());
			}

			Range range = QutePositionUtility.createRange(
					new RangeOffset(section.getStartTagCloseOffset() + 1, section.getEndTagOpenOffset()), template);

			CodeAction insertRequiredInputs = CodeActionFactory.replace("Insert required input forms",
					range, generateInput(template, requiredInputNames, range), template.getTextDocument(), diagnostic);
			codeActions.add(insertRequiredInputs);

			CodeAction insertAllInputs = CodeActionFactory.replace("Insert all input forms",
					range, generateInput(template, allInputNames, range), template.getTextDocument(), diagnostic);
			codeActions.add(insertAllInputs);

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of generate missing inputs code action failed", e);
		}
	}

	private static String generateInput(Template template, List<String> inputs, Range range)
			throws BadLocationException {
		StringBuilder str = new StringBuilder();
		int lineNumber = range.getStart().getLine();
		LineIndentInfo indentInfo = template.lineIndentInfo(lineNumber);
		String lineDelimiter = indentInfo.getLineDelimiter();
		String whitespacesIndent = indentInfo.getWhitespacesIndent();
		for (String input : inputs) {
			str.append(lineDelimiter);
			str.append(whitespacesIndent);
			str.append(findIndentation(template, range));
			str.append(String.format("<input name=\"%s\" >", input));
		}
		str.append(lineDelimiter);
		str.append(whitespacesIndent);
		return str.toString();
	}

	// Temporary solution to the lack of formatting support for Qute
	private static String findIndentation(Template template, Range range) throws BadLocationException {
		for (int i = 0; i < range.getEnd().getLine(); i++) {
			String indent = template.lineIndentInfo(i).getWhitespacesIndent();
			if (!indent.isEmpty() && (!(indent.charAt(0) == ' ')
					|| (indent.length() == 2 || indent.length() == 4 || indent.length() == 6
							|| indent.length() == 8))) {
				return indent;
			}
		}
		return DEFAULT_INDENTATION;
	}

}
