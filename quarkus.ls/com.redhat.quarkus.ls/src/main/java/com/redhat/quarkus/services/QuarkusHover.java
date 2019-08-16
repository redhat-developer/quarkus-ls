/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.model.Node;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.PropertyKey;
import com.redhat.quarkus.settings.QuarkusHoverSettings;
import com.redhat.quarkus.utils.DocumentationUtils;
import com.redhat.quarkus.utils.PositionUtils;

/**
 * Retreives hover documentation and creating Hover object
 */
class QuarkusHover {

	private static final Logger LOGGER = Logger.getLogger(QuarkusCompletions.class.getName());

	/**
	 * Returns Hover object for the currently hovered token
	 * 
	 * @param document      the properties model document
	 * @param position      the hover position
	 * @param projectInfo   the Quarkus project information
	 * @param hoverSettings the hover settings
	 * @return Hover object for the currently hovered token
	 */
	public Hover doHover(PropertiesModel document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusHoverSettings hoverSettings) {

		Node node = null;
		try {
			node = document.findNodeAt(position);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusHover, position error", e);
			return null;
		}
		if (node == null) {
			return null;
		}

		switch (node.getNodeType()) {
		case COMMENTS:
			// no hover documentation
			return null;
		case ASSIGN:
		case PROPERTY_VALUE:
			// no hover documentation
			return null;
		case PROPERTY_KEY:
			// hover documentation on property key
			return getPropertyKeyHover(node, projectInfo, hoverSettings);
		default:
			return null;
		}
	}

	/**
	 * Returns the documentation hover for property key represented by the property
	 * key <code>node</code>
	 * 
	 * @param node          the property key node
	 * @param projectInfo   the Quarkus project information
	 * @param hoverSettings the hover settings
	 * @return the documentation hover for property key represented by token
	 */
	private static Hover getPropertyKeyHover(Node node, QuarkusProjectInfo projectInfo,
			QuarkusHoverSettings hoverSettings) {
		String propertyName = ((PropertyKey) node).getText();
		boolean markdownSupported = hoverSettings.isContentFormatSupported(MarkupKind.MARKDOWN);
		// retrieve Quarkus property from the project information
		ExtendedConfigDescriptionBuildItem property = projectInfo.getProperty(propertyName);
		if (property != null) {
			// Quarkus property, found, display her documentation as hover
			MarkupContent markupContent = DocumentationUtils.getDocumentation(property, markdownSupported);
			Hover hover = new Hover();
			hover.setContents(markupContent);
			hover.setRange(PositionUtils.createRange(node.getStart(), node.getEnd(), node.getDocument()));
			return hover;
		}
		return null;
	}
}