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
package com.redhat.qute.services.hover;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.AbstractPositionRequest;
import com.redhat.qute.settings.QuteHoverSettings;
import com.redhat.qute.settings.SharedSettings;

/**
 * Hover request.
 * 
 * @author Angelo ZERR
 *
 */
public class HoverRequest extends AbstractPositionRequest {

	private final SharedSettings settings;

	public HoverRequest(Template template, Position position, SharedSettings settings) throws BadLocationException {
		super(template, position);
		this.settings = settings;
	}

	@Override
	protected Node doFindNodeAt(Template template, int offset) {
		return template.findNodeAt(offset);
	}
	
	@Override
	protected boolean isIncludeAfterStartExpression() {
		return true;
	}

	public boolean canSupportMarkupKind(String kind) {
		QuteHoverSettings hoverSettings = settings.getHoverSettings();
		return settings != null && hoverSettings.getCapabilities() != null
				&& hoverSettings.getCapabilities().getContentFormat() != null
				&& hoverSettings.getCapabilities().getContentFormat().contains(kind);
	}
}
