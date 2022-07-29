/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.CodeActionFactory;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Code actions for {@link QuteErrorCode#UndefinedSectionTag}.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCodeActionForUndefinedSectionTag extends AbstractQuteCodeAction {

	private static final String UNDEFINED_SECTION_TAG_CODEACTION_TITLE = "Create the user tag file `{0}`.";

	private static final Logger LOGGER = Logger.getLogger(QuteCodeActionForUndefinedSectionTag.class.getName());

	public QuteCodeActionForUndefinedSectionTag(JavaDataModelCache javaCache) {
		super(javaCache);
	}

	@Override
	public void doCodeActions(CodeActionRequest request, List<CompletableFuture<Void>> codeActionResolveFutures,
			List<CodeAction> codeActions) {
		try {
			Template template = request.getTemplate();
			Diagnostic diagnostic = request.getDiagnostic();
			QuteProject project = template.getProject();
			if (project == null) {
				return;
			}
			String tagName = null;
			Node node = request.getCoveredNode();
			if (node.getKind() == NodeKind.Section) {
				Section section = (Section) node;
				tagName = section.getTag();
			}
			if (tagName == null) {
				return;
			}

			// TODO : use a settings to know the preferred file extension
			String preferedFileExtension = ".html";
			String tagFileUri = project.getTagsDir().resolve(tagName + preferedFileExtension).toUri().toString();
			String title = MessageFormat.format(UNDEFINED_SECTION_TAG_CODEACTION_TITLE, tagName);
			CodeAction createUserTagFile = CodeActionFactory.createFile(title, tagFileUri, "", diagnostic);
			codeActions.add(createUserTagFile);

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of undefined user tag code action failed", e);
		}
	}

}
