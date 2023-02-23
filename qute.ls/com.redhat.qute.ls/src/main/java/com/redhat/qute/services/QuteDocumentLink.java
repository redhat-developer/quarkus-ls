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
package com.redhat.qute.services;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.IncludeSection;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Qute document link support.
 *
 * @author Angelo ZERR
 *
 */
public class QuteDocumentLink {

	public CompletableFuture<List<DocumentLink>> findDocumentLinks(Template template, CancelChecker cancelChecker) {
		QuteProject project = template.getProject();
		if (project != null) {
			return CompletableFuture.completedFuture(findDocumentLinksSync(template, cancelChecker));
		}
		return template.getProjectFuture()
				.thenApply(p -> {
					if (p == null) {
						return Collections.emptyList();
					}
					return findDocumentLinksSync(template, cancelChecker);
				});
	}

	public List<DocumentLink> findDocumentLinksSync(Template template, CancelChecker cancelChecker) {
		List<DocumentLink> links = new ArrayList<>();
		findDocumentLinks(template, template, links, cancelChecker);
		return links;
	}

	private void findDocumentLinks(Node node, Template template, List<DocumentLink> links,
			CancelChecker cancelChecker) {
		List<Node> children = node.getChildren();
		for (Node child : children) {
			cancelChecker.checkCanceled();
			if (child.getKind() == NodeKind.Section) {
				Section section = (Section) child;
				if (section.getSectionKind() == SectionKind.INCLUDE) {
					// #include section case:
					IncludeSection includeSection = (IncludeSection) section;
					// {#include base.qute.html}
					// In this case 'base.qute.html' is a document link
					Parameter includedTemplateId = includeSection.getParameterAtIndex(0);
					if (includedTemplateId != null) {
						Range range = QutePositionUtility.createRange(includedTemplateId.getStart(),
								includedTemplateId.getEnd(), template);
						if (range != null) {
							Path templateFile = includeSection.getReferencedTemplateFile();
							if (templateFile != null) {
								String target = templateFile.toUri().toString();
								links.add(new DocumentLink(range, target != null ? target : ""));
							}
						}
					}
				}
			}
			findDocumentLinks(child, template, links, cancelChecker);
		}
	}
}
