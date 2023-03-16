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
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.QuteSearchUtils;
import com.redhat.qute.utils.StringUtils;

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
			if (findReferencesForInsertParameter(node, locations, cancelChecker)) {
				return locations;
			}

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

	private boolean findReferencesForInsertParameter(Node node, List<Location> locations, CancelChecker cancelChecker) {
		if (node == null) {
			return false;
		}
		if (node.getKind() != NodeKind.Parameter) {
			return false;
		}
		Parameter parameter = (Parameter) node;
		Section section = parameter.getOwnerSection();
		if (section == null) {
			return false;
		}
		if (!Section.isInsertSection(section)) {
			return false;
		}
		String tag = parameter.getValue();
		if (StringUtils.isEmpty(tag)) {
			return false;
		}
		QuteProject project =  node.getOwnerTemplate().getProject();
		if (project == null) {
			return false;
		}
		
		List<Section> sections = project.findSectionsByTag(tag);
		for (Section matchedSection : sections) {
			Template targetDocument = matchedSection.getOwnerTemplate();
			Range targetRange = QutePositionUtility.selectStartTagName(matchedSection);
			Location location = new Location(targetDocument.getUri(), targetRange);
			locations.add(location);
		}
		return true;
	}

}