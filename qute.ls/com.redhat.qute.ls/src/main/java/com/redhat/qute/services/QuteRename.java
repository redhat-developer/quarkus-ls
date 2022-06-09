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
package com.redhat.qute.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.QuteSearchUtils;

/**
 * Qute rename support.
 *
 */
class QuteRename {

	private static final Logger LOGGER = Logger.getLogger(QuteRename.class.getName());

	public WorkspaceEdit doRename(Template template, Position position, String newText, CancelChecker cancelChecker) {
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
			List<TextEdit> textEdits = new ArrayList<>();
			for (Range r : ranges) {
				textEdits.add(new TextEdit(r, newText));
			}
			return createWorkspaceEdit(template.getUri(), textEdits);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteRename, the client provided Position is at a BadLocation", e);
			return null;
		}
	}

	private static WorkspaceEdit createWorkspaceEdit(String documentURI, List<TextEdit> textEdits) {
		Map<String, List<TextEdit>> changes = new HashMap<>();
		changes.put(documentURI, textEdits);
		return new WorkspaceEdit(changes);
	}

}