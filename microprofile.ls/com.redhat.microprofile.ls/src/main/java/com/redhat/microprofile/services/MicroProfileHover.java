/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.services;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.omg.CORBA.portable.ValueInputStream;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.metadata.ConfigurationMetadata;
import com.redhat.microprofile.commons.metadata.ItemHint;
import com.redhat.microprofile.commons.metadata.ItemHint.ValueHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.ls.commons.BadLocationException;
import com.redhat.microprofile.model.Node;
import com.redhat.microprofile.model.PropertiesModel;
import com.redhat.microprofile.model.Property;
import com.redhat.microprofile.model.PropertyKey;
import com.redhat.microprofile.model.PropertyValue;
import com.redhat.microprofile.model.values.ValuesRulesManager;
import com.redhat.microprofile.settings.MicroProfileHoverSettings;
import com.redhat.microprofile.utils.DocumentationUtils;
import com.redhat.microprofile.utils.MicroProfilePropertiesUtils;
import com.redhat.microprofile.utils.PositionUtils;

/**
 * Retrieves hover documentation and creating Hover object
 */
class MicroProfileHover {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileHover.class.getName());

	/**
	 * Returns Hover object for the currently hovered token
	 * 
	 * @param document           the properties model document
	 * @param position           the hover position
	 * @param projectInfo        the Quarkus project information
	 * @param valuesRulesManager manager for values rules
	 * @param hoverSettings      the hover settings
	 * @return Hover object for the currently hovered token
	 */
	public Hover doHover(PropertiesModel document, Position position, MicroProfileProjectInfo projectInfo,
			ValuesRulesManager valuesRulesManager, MicroProfileHoverSettings hoverSettings) {

		Node node = null;
		int offset = -1;
		try {
			node = document.findNodeAt(position);
			offset = document.offsetAt(position);
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
		case PROPERTY_VALUE:
			// no hover documentation
			return getPropertyValueHover(node, projectInfo, valuesRulesManager, hoverSettings);
		case PROPERTY_KEY:
			PropertyKey key = (PropertyKey) node;
			if (key.isBeforeProfile(offset)) {
				// hover documentation on profile
				return getProfileHover(key, hoverSettings);
			} else {
				// hover documentation on property key
				return getPropertyKeyHover(key, projectInfo, hoverSettings);
			}

		default:
			return null;
		}
	}

	/**
	 * Returns the documentation hover for the property key's profile, for the
	 * property key represented by <code>key</code>
	 * 
	 * Returns null if property key represented by <code>key</code> does not have a
	 * profile
	 * 
	 * @param key           the property key
	 * @param hoverSettings the hover settings
	 * @return the documentation hover for the property key's profile
	 */
	private static Hover getProfileHover(PropertyKey key, MicroProfileHoverSettings hoverSettings) {
		boolean markdownSupported = hoverSettings.isContentFormatSupported(MarkupKind.MARKDOWN);
		for (ValueHint profile : QuarkusModel.DEFAULT_PROFILES.getValues()) {
			if (profile.getValue().equals(key.getProfile())) {
				MarkupContent markupContent = DocumentationUtils.getDocumentation(profile, markdownSupported);
				Hover hover = new Hover();
				hover.setContents(markupContent);
				hover.setRange(getProfileHoverRange(key));
				return hover;
			}
		}
		return null;
	}

	/**
	 * Returns the documentation hover for property key represented by the property
	 * key <code>key</code>
	 * 
	 * @param key           the property key
	 * @param offset        the hover offset
	 * @param projectInfo   the Quarkus project information
	 * @param hoverSettings the hover settings
	 * @return the documentation hover for property key represented by token
	 */
	private static Hover getPropertyKeyHover(PropertyKey key, MicroProfileProjectInfo projectInfo,
			MicroProfileHoverSettings hoverSettings) {
		boolean markdownSupported = hoverSettings.isContentFormatSupported(MarkupKind.MARKDOWN);
		// retrieve Quarkus property from the project information
		String propertyName = key.getPropertyName();

		ItemMetadata item = MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo);
		if (item != null) {
			// Quarkus property, found, display her documentation as hover
			MarkupContent markupContent = DocumentationUtils.getDocumentation(item, key.getProfile(),
					markdownSupported);
			Hover hover = new Hover();
			hover.setContents(markupContent);
			hover.setRange(PositionUtils.createRange(key));
			return hover;
		}
		return null;
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
	private static Hover getPropertyValueHover(Node node, MicroProfileProjectInfo projectInfo,
			ValuesRulesManager valuesRulesManager, MicroProfileHoverSettings hoverSettings) {
		PropertyValue value = ((PropertyValue) node);
		boolean markdownSupported = hoverSettings.isContentFormatSupported(MarkupKind.MARKDOWN);
		// retrieve Quarkus property from the project information
		String propertyValue = value.getValue();
		if (propertyValue == null || propertyValue.isEmpty()) {
			return null;
		}
		String propertyName = ((Property) (value.getParent())).getPropertyName();
		ItemMetadata item = MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo);
		ValueHint enumItem = getValueHint(propertyValue, item, projectInfo, valuesRulesManager, value.getOwnerModel());
		if (enumItem != null) {
			// Quarkus property enumeration item, found, display her documentation as hover
			MarkupContent markupContent = DocumentationUtils.getDocumentation(enumItem, markdownSupported);
			Hover hover = new Hover();
			hover.setContents(markupContent);
			hover.setRange(PositionUtils.createRange(node));
			return hover;
		}
		return null;
	}

	/**
	 * Returns the hover range covering the %profilename in <code>key</code> Returns
	 * range of <code>key</code> if <code>key</code> does not provide a profile
	 * 
	 * @param key the property key
	 * @return the hover range covering the %profilename in <code>key</code>
	 */
	private static Range getProfileHoverRange(PropertyKey key) {
		Range range = PositionUtils.createRange(key);

		if (key.getProfile() == null) {
			return range;
		}

		String profile = key.getProfile();
		Position endPosition = range.getEnd();
		endPosition.setCharacter(range.getStart().getCharacter() + profile.length() + 1);
		range.setEnd(endPosition);
		return range;
	}

	private static ValueHint getValueHint(String propertyValue, ItemMetadata metadata,
			ConfigurationMetadata configuration, ValuesRulesManager valuesRulesManager, PropertiesModel model) {
		if (metadata == null) {
			return null;
		}
		ItemHint enumItem = configuration.getHint(metadata);
		if (enumItem  != null) {
			ValueHint valueHint = enumItem .getValue(propertyValue);
			if (valueHint != null) {
				return valueHint;
			}
		}		
		return valuesRulesManager.getValueHint(propertyValue, metadata, model);
	}
}