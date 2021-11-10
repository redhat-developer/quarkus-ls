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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.LinkedEditingRanges;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.QuteSearchUtils;

/**
 * Qute linked editing support.
 *
 */
class QuteLinkedEditing {

	private static final Logger LOGGER = Logger.getLogger(QuteLinkedEditing.class.getName());

	public LinkedEditingRanges findLinkedEditingRanges(Template template, Position position,
			CancelChecker cancelChecker) {
		try {
			int offset = template.offsetAt(position);
			Node node = template.findNodeAt(offset);
			if (node == null) {
				return null;
			}
			node = QutePositionUtility.findBestNode(offset, node);

			List<Range> ranges = new ArrayList<>();
			QuteSearchUtils.searchReferencedObjects(node, offset, //
					(n, range) -> ranges.add(range), true, cancelChecker);
			if (ranges.size() <= 1) {
				return null;
			}
			return new LinkedEditingRanges(ranges);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteLinkedEditing the client provided Position is at a BadLocation", e);
			return null;
		}
	}

}
