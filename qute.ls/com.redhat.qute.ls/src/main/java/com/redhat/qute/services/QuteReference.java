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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.QuteSearchUtils;

class QuteReference {

	private static final Logger LOGGER = Logger.getLogger(QuteReference.class.getName());

	public List<? extends Location> findReferences(Template template, Position position, ReferenceContext context,
			CancelChecker cancelChecker) {
		try {
			int offset = template.offsetAt(position);
			Node node = template.findNodeAt(offset);
			if (node == null) {
				return Collections.emptyList();
			}
			node = QutePositionUtility.findBestNode(offset, node);

			List<Location> locations = new ArrayList<>();
			QuteSearchUtils.searchReferencedObjects(node, offset, //
					(n, range) -> {
						Template targetDocument = n.getOwnerTemplate();
						Range targetRange = QutePositionUtility.createRange(n.getStart(), n.getEnd(), targetDocument);
						Location location = new Location(targetDocument.getUri(), targetRange);
						locations.add(location);
					}, false, cancelChecker);
			return locations;
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteReference the client provided Position is at a BadLocation", e);
			return Collections.emptyList();
		}
	}

}
