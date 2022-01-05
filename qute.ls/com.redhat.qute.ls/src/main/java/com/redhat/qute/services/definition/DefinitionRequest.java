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
package com.redhat.qute.services.definition;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.AbstractPositionRequest;

/**
 * Definition request.
 * 
 * @author Angelo ZERR
 *
 */
public class DefinitionRequest extends AbstractPositionRequest {

	public DefinitionRequest(Template template, Position position) throws BadLocationException {
		super(template, position);
	}

	@Override
	protected Node doFindNodeAt(Template template, int offset) {
		return template.findNodeAt(offset);
	}
	
	@Override
	protected boolean isIncludeAfterStartExpression() {
		return true;
	}
}
