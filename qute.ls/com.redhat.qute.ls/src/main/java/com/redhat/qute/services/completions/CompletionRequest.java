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
package com.redhat.qute.services.completions;

import org.eclipse.lsp4j.InsertTextMode;
import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.AbstractPositionRequest;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;

public class CompletionRequest extends AbstractPositionRequest {

	private final QuteCompletionSettings completionSettings;

	public CompletionRequest(Template template, Position position, QuteCompletionSettings completionSettings,
			QuteFormattingSettings formattingSettings) throws BadLocationException {
		super(template, position);
		this.completionSettings = completionSettings;
	}

	@Override
	protected Node doFindNodeAt(Template template, int offset) {
		return template.findNodeBefore(offset);
	}

	public boolean canSupportMarkupKind(String kind) {
		return completionSettings.canSupportMarkupKind(kind);
	}

	public boolean isCompletionSnippetsSupported() {
		return completionSettings.isCompletionSnippetsSupported();
	}

	public boolean isInsertTextModeAdjustIndentationSupported() {
		return completionSettings.isInsertTextModeAdjustIndentationSupported();
	}

	public InsertTextMode getDefaultInsertTextMode() {
		return completionSettings.getDefaultInsertTextMode();
	}

}
