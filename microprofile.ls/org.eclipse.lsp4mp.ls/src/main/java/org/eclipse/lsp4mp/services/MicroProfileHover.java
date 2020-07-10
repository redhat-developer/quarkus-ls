/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.services;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.metadata.ConfigurationMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemHint.ValueHint;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.model.PropertyKey;
import org.eclipse.lsp4mp.model.PropertyValue;
import org.eclipse.lsp4mp.model.values.ValuesRulesManager;
import org.eclipse.lsp4mp.settings.MicroProfileHoverSettings;
import org.eclipse.lsp4mp.utils.DocumentationUtils;
import org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils;
import org.eclipse.lsp4mp.utils.PositionUtils;

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
	 * @param projectInfo        the MicroProfile project information
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
			LOGGER.log(Level.SEVERE, "MicroProfileHover, position error", e);
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
	 * @param projectInfo   the MicroProfile project information
	 * @param hoverSettings the hover settings
	 * @return the documentation hover for property key represented by token
	 */
	private static Hover getPropertyKeyHover(PropertyKey key, MicroProfileProjectInfo projectInfo,
			MicroProfileHoverSettings hoverSettings) {
		boolean markdownSupported = hoverSettings.isContentFormatSupported(MarkupKind.MARKDOWN);
		// retrieve MicroProfile property from the project information
		String propertyName = key.getPropertyName();

		ItemMetadata item = MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo);
		if (item != null) {
			// MicroProfile property, found, display her documentation as hover
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
	 * @param projectInfo   the MicroProfile project information
	 * @param hoverSettings the hover settings
	 * @return the documentation hover for property key represented by token
	 */
	private static Hover getPropertyValueHover(Node node, MicroProfileProjectInfo projectInfo,
			ValuesRulesManager valuesRulesManager, MicroProfileHoverSettings hoverSettings) {
		PropertyValue value = ((PropertyValue) node);
		boolean markdownSupported = hoverSettings.isContentFormatSupported(MarkupKind.MARKDOWN);
		// retrieve MicroProfile property from the project information
		String propertyValue = value.getValue();
		if (propertyValue == null || propertyValue.isEmpty()) {
			return null;
		}
		String propertyName = ((Property) (value.getParent())).getPropertyName();
		ItemMetadata item = MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo);
		ValueHint enumItem = getValueHint(propertyValue, item, projectInfo, valuesRulesManager, value.getOwnerModel());
		if (enumItem != null) {
			// MicroProfile property enumeration item, found, display its documentation as hover
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
		if (enumItem != null) {
			ValueHint valueHint = enumItem.getValue(propertyValue, metadata.getConverterKinds());
			if (valueHint != null) {
				return valueHint;
			}
		}
		return valuesRulesManager.getValueHint(propertyValue, metadata, model);
	}
}