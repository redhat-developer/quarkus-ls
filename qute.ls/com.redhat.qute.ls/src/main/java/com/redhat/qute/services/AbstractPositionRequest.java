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

import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Abstract class for position request.
 *
 * @author Angelo ZERR
 */
public abstract class AbstractPositionRequest {

	private final Template template;
	private final int offset;
	private final Node node;

	public AbstractPositionRequest(Template template, Position position) throws BadLocationException {
		this.template = template;
		this.offset = template.offsetAt(position);
		this.node = findNodeAt(template, offset);
		if (node == null) {
			throw new BadLocationException("node is null at offset " + offset);
		}
	}

	protected final Node findNodeAt(Template template, int offset) {
		Node node = doFindNodeAt(template, offset);
		if (node == null) {
			return null;
		}
		return QutePositionUtility.findBestNode(offset, node, isIncludeAfterStartExpression());
	}

	/**
	 * Returns true if the node after a start expression must be included and false
	 * otherwise.
	 * 
	 * @return true if the node after a start expression must be included and false
	 *         otherwise.
	 */
	protected boolean isIncludeAfterStartExpression() {
		return false;
	}

	protected abstract Node doFindNodeAt(Template template, int offset);

	public Template getTemplate() {
		return template;
	}

	public int getOffset() {
		return offset;
	}

	public Node getNode() {
		return node;
	}

}
