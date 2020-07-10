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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertyDefinitionParams;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemHint.ValueHint;
import org.eclipse.lsp4mp.ls.api.MicroProfilePropertyDefinitionProvider;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.model.PropertyKey;
import org.eclipse.lsp4mp.model.PropertyValue;
import org.eclipse.lsp4mp.model.Node.NodeType;
import org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils;
import org.eclipse.lsp4mp.utils.PositionUtils;

/**
 * The MicroProfile definition.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileDefinition {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileDefinition.class.getName());

	/**
	 * Returns as promise the Java field definition location of the property at the
	 * given <code>position</code> of the given application.properties
	 * <code>document</code>.
	 * 
	 * @param document              the properties model.
	 * @param position              the position where definition was triggered
	 * @param projectInfo           the MicroProfile project info
	 * @param provider              the MicroProfile property definition provider.
	 * @param definitionLinkSupport true if {@link LocationLink} must be returned
	 *                              and false otherwise.
	 * @return as promise the Java field definition location of the property at the
	 *         given <code>position</code> of the given application.properties
	 *         <code>document</code>.
	 */
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> findDefinition(
			PropertiesModel document, Position position, MicroProfileProjectInfo projectInfo,
			MicroProfilePropertyDefinitionProvider provider, boolean definitionLinkSupport) {

		try {
			Node node = document.findNodeAt(position);
			if (node == null) {
				return CompletableFuture.completedFuture(getEmptyDefinition(definitionLinkSupport));
			}

			// Get the property at the given position
			PropertyKey key = getPropertyKey(node);

			if (key == null) {
				return CompletableFuture.completedFuture(getEmptyDefinition(definitionLinkSupport));
			}

			// Get metatada of the property
			ItemMetadata item = MicroProfilePropertiesUtils.getProperty(key.getPropertyName(), projectInfo);
			if (item == null) {
				return CompletableFuture.completedFuture(getEmptyDefinition(definitionLinkSupport));
			}

			MicroProfilePropertyDefinitionParams definitionParams = getPropertyDefinitionParams(document, item,
					projectInfo, node);
			if (definitionParams == null) {
				return CompletableFuture.completedFuture(getEmptyDefinition(definitionLinkSupport));
			}
			return provider.getPropertyDefinition(definitionParams).thenApply(target -> {
				if (target == null) {
					return getEmptyDefinition(definitionLinkSupport);
				}
				if (definitionLinkSupport) {
					// Use document link
					LocationLink link = new LocationLink(target.getUri(), target.getRange(), target.getRange(),
							PositionUtils.createRange(node));
					return Either.forRight(Collections.singletonList(link));
				}
				// Use simple location
				return Either.forLeft(Collections.singletonList(target));
			});

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In MicroProfileDefinition, position error", e);
		}
		return CompletableFuture.completedFuture(getEmptyDefinition(definitionLinkSupport));
	}

	private static Either<List<? extends Location>, List<? extends LocationLink>> getEmptyDefinition(
			boolean definitionLinkSupport) {
		return definitionLinkSupport ? Either.forRight(Collections.emptyList())
				: Either.forLeft(Collections.emptyList());
	}

	private static MicroProfilePropertyDefinitionParams getPropertyDefinitionParams(PropertiesModel document,
			ItemMetadata item, MicroProfileProjectInfo projectInfo, Node node) {

		if (node.getNodeType() != NodeType.PROPERTY_KEY && node.getNodeType() != NodeType.PROPERTY_VALUE) {
			return null;
		}

		MicroProfilePropertyDefinitionParams definitionParams = new MicroProfilePropertyDefinitionParams();

		String sourceType = null;
		String sourceField = null;

		switch (node.getNodeType()) {
		case PROPERTY_KEY: {
			sourceType = item.getSourceType();
			sourceField = item.getSourceField();
			break;
		}
		case PROPERTY_VALUE: {
			sourceType = item.getHintType();
			sourceField = ((PropertyValue) node).getValue();
			// for the case of property which uses kebab_case, we must get the real value of
			// the Java enumeration
			// Ex: for quarkus.datasource.transaction-isolation-level = read-uncommitted
			// the real value of Java enumeration 'read-uncommitted' is 'READ_UNCOMMITTED'
			ItemHint itemHint = projectInfo.getHint(sourceType);
			if (itemHint != null) {
				ValueHint realValue = itemHint.getValue(sourceField, item.getConverterKinds());
				if (realValue != null) {
					sourceField = realValue.getValue();
				}
			}
			break;
		}
		default:
			return null;
		}

		// Find definition (class, field of class, method of class, enum) only when
		// metadata
		// contains source type
		if (sourceType == null) {
			return null;
		}

		definitionParams.setSourceType(sourceType);
		definitionParams.setSourceField(sourceField);
		definitionParams.setUri(document.getDocumentURI());
		definitionParams.setSourceMethod(item.getSourceMethod());

		return definitionParams;
	}

	private static PropertyKey getPropertyKey(Node node) {
		if (node == null) {
			return null;
		}
		switch (node.getNodeType()) {
		case PROPERTY_KEY:
			return (PropertyKey) node;
		case PROPERTY_VALUE:
			return ((Property) node.getParent()).getKey();
		default:
			return null;
		}
	}
}
