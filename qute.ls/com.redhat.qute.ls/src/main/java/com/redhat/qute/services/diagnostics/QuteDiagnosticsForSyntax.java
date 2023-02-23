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
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.services.diagnostics.DiagnosticDataFactory.createDiagnostic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.tags.UserTag;

import io.quarkus.qute.Engine;
import io.quarkus.qute.EngineBuilder;
import io.quarkus.qute.TemplateException;
import io.quarkus.qute.TemplateNode.Origin;
import io.quarkus.qute.UserTagSectionHelper;

/**
 * Qute syntax validation done with the real Qute parser.
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsForSyntax {

	private static final Range LEFT_TOP_RANGE = new Range(new Position(0, 0), new Position(0, 0));

	/**
	 * Validate Qute syntax for the given template.
	 *
	 * @param template    the Qute template.
	 * @param diagnostics the diagnostics to update.
	 *
	 */
	public void validateWithRealQuteParser(Template template, List<Diagnostic> diagnostics) {
		EngineBuilder engineBuilder = Engine.builder().addDefaults();
		String templateContent = template.getText();
		try {
			QuteProject project = template.getProject();
			if (project != null) {
				// To avoid having error with the real Qute parser, we will section helper with
				// user tags:
				// - Source tags
				Collection<UserTag> sourceTags = new ArrayList<>(project.getSourceUserTags());
				addUserTag(sourceTags, engineBuilder);
				// - Binary tags
				Collection<UserTag> binaryTags = project.getBinaryUserTags().getNow(Collections.emptyList());
				addUserTag(binaryTags, engineBuilder);
			}
			Engine engine = engineBuilder.build();
			engine.parse(templateContent);
		} catch (TemplateException e) {
			String message = e.getMessage();
			if (message.contains("no section helper found for")) {
				// Ignore error "no section helper found for" which is managed with
				// QuteDiagnostic to highlight the section start correctly.
				return;
			}
			Range range = createRange(e, template);
			Diagnostic diagnostic = createDiagnostic(range, message, DiagnosticSeverity.Error,
					QuteErrorCode.SyntaxError);
			diagnostics.add(diagnostic);
		}
	}

	private static void addUserTag(Collection<UserTag> tags, EngineBuilder engineBuilder) {
		for (UserTag userTag : tags) {
			String tagName = userTag.getName();
			String tagTemplateId = userTag.getTemplateId();
			engineBuilder.addSectionHelper(new UserTagSectionHelper.Factory(tagName, tagTemplateId));
		}
	}

	private static Range createRange(TemplateException e, Template template) {
		Origin origin = e.getOrigin();
		if (origin == null) {
			return LEFT_TOP_RANGE;
		}
		int line = e.getOrigin().getLine() - 1;
		Position start = new Position(line, e.getOrigin().getLineCharacterStart() - 1);
		Position end = new Position(line, e.getOrigin().getLineCharacterEnd() - 1);
		return new Range(start, end);
	}
}
