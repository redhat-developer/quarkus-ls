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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.CodeActionFactory;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.RangeOffset;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.UserTagUtils;

/**
 * Code actions for {@link QuteErrorCode#MissingRequiredParameter}.
 *
 */
public class QuteCodeActionForMissingParameters extends AbstractQuteCodeAction {

	private static final Logger LOGGER = Logger.getLogger(QuteCodeActionForMissingParameters.class.getName());

	@Override
	public void doCodeActions(CodeActionRequest request, List<CompletableFuture<Void>> codeActionResolveFutures,
			List<CodeAction> codeActions) {
		try {
			Node node = request.getCoveredNode();
			if (node == null) {
				return;
			}
			Section section = node.getKind() == NodeKind.Section ? (Section) node : null;
			if (section == null) {
				return;
			}
			Template template = request.getTemplate();

			List<String> requiredUserTagParamNames = template.getProject().findUserTag(section.getTag())
					.getRequiredParameterNames();

			int startRangeOffset = section.getStartTagNameCloseOffset();

			// collect missing paramters
			for (Parameter param : section.getParameters()) {
				if (requiredUserTagParamNames.contains(param.getName())) {
					requiredUserTagParamNames.remove(param.getName());
				}
				startRangeOffset = param.getEnd();
			}

			Range range = QutePositionUtility.createRange(
					new RangeOffset(startRangeOffset, section.getStartTagCloseOffset()), template);
			CodeAction insertAllInputs = CodeActionFactory.replace("Insert required parameters",
					range, generateParameters(requiredUserTagParamNames), template.getTextDocument(),
					request.getDiagnostic());
			codeActions.add(insertAllInputs);

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of generate missing parameters code action failed", e);
		}
	}

	private static String generateParameters(List<String> paramNames) throws BadLocationException {
		StringBuilder str = new StringBuilder();
		for (String paramName : paramNames) {
			str.append(" ");
			if (paramName.equals(UserTagUtils.IT_OBJECT_PART_NAME)){
				str.append("\"\"");
			} else {
				str.append(String.format("%s=\"%s\"", paramName, paramName));
			}
		}
		return str.toString();
	}

}
