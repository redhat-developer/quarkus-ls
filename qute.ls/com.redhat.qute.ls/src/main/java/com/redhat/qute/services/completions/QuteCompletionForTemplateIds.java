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
package com.redhat.qute.services.completions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Qute completion for template ids in the {#include |}
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionForTemplateIds {

	private static final Comparator<QuteTextDocument> TEMPLATE_ID_COMPARATOR = (d1, d2) -> {
		String templateId1 = d1.getTemplateId();
		String templateId2 = d2.getTemplateId();
		String[] paths1 = templateId1.split("/");
		String[] paths2 = templateId2.split("/");
		if (paths1.length < paths2.length) {
			return -1;
		} else if (paths1.length > paths2.length) {
			return 1;
		} else {
			for (int i = 0; i < paths1.length; i++) {
				String path1 = paths1[i];
				String path2 = paths2[i];
				if (!path1.equals(path2)) {
					return path1.compareTo(path2);
				}
			}
			return 0;
		}
	};

	public CompletableFuture<CompletionList> doCompleteTemplateId(CompletionRequest completionRequest,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			CancelChecker cancelChecker) {
		CompletionList list = new CompletionList();
		list.setItems(new ArrayList<>());
		Template template = completionRequest.getTemplate();
		QuteProject project = template.getProject();
		if (project != null) {
			String currentTemplateId = template.getTemplateId();
			Range range = getReplaceRange(completionRequest.getNode(), completionRequest.getOffset(),
					template);

			// Add all templates from the Qute project
			List<QuteTextDocument> documents = new ArrayList<>(project.getDocuments());
			// Add all templates from the Qute project dependencies
			for (QuteProject projectDependency : project.getProjectDependencies()) {
				documents.addAll(projectDependency.getDocuments());
			}

			// Sort template ids to show at first the root files of the
			// src/main/resources/templates folder (ex : base,main).
			Collections.sort(documents, TEMPLATE_ID_COMPARATOR);

			// To keep the sort in LSP we generates a basic sort text by adding the 'a'
			// letter.
			StringBuilder sortText = new StringBuilder();

			/*
			 * Map with:
			 * - key: template if with short syntax (ex : base)
			 * - value: list of document which matches the short syntax (ex :base.html,
			 * base.json,base.txt)
			 */
			Map<String, List<QuteTextDocument>> templateIds = createTemplateIds(
					documents, currentTemplateId);
			for (Map.Entry<String, List<QuteTextDocument>> ids : templateIds.entrySet()) {
				String sort = sortText.append("a").toString();
				List<QuteTextDocument> documentsForId = ids.getValue();
				if (documentsForId.size() == 1) {
					// One document (ex : base.html) matches the short syntax temple id (ex : base)
					// here completion shows 'base' short syntax.
					addTemplateId(ids.getKey(), documentsForId.get(0), range, sort, list);
				} else {
					// Several documents (ex : base.html, base.txt) matches the short syntax temple
					// id (ex : base)
					// here we generate a completion per document by using the template id of the
					// document (ex : base.html, base.txt).
					for (QuteTextDocument document : documentsForId) {
						addTemplateId(document.getTemplateId(), document, range, sort, list);
					}
				}
			}
		}
		return CompletableFuture.completedFuture(list);
	}

	private Map<String, List<QuteTextDocument>> createTemplateIds(List<QuteTextDocument> documents,
			String currentTemplateId) {
		Map<String, List<QuteTextDocument>> templateIds = new LinkedHashMap<>();
		for (QuteTextDocument document : documents) {
			if (!document.isUserTag()) {
				// The document is not an user tag, it can be used in {#include | }
				String templateId = document.getTemplateId();
				if (!templateId.equals(currentTemplateId)) {
					// The template id is different from the template id where completion has been
					// triggered
					int index = templateId.lastIndexOf('.');
					String shortSyntax = index != -1 ? templateId.substring(0, index) : templateId;
					List<QuteTextDocument> documentsForId = templateIds.get(shortSyntax);
					if (documentsForId == null) {
						documentsForId = new ArrayList<>();
						templateIds.put(shortSyntax, documentsForId);
					}
					documentsForId.add(document);
				}
			}
		}
		return templateIds;
	}

	private Range getReplaceRange(Node node, int offset, Template template) {
		if (node == null) {
			return QutePositionUtility.createRange(offset, offset, template);
		}
		return QutePositionUtility.createRange(node);
	}

	private void addTemplateId(String templateId, QuteTextDocument template, Range range, String sortText,
			CompletionList list) {
		CompletionItem item = new CompletionItem();
		item.setLabel(templateId);
		item.setFilterText(templateId);
		item.setSortText(sortText);
		item.setKind(CompletionItemKind.Field);
		TextEdit textEdit = new TextEdit(range, templateId);
		item.setTextEdit(Either.forLeft(textEdit));
		list.getItems().add(item);
	}

}
