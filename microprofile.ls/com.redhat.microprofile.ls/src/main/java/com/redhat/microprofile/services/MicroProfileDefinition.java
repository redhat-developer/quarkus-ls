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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertyDefinitionParams;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.ls.api.MicroProfilePropertyDefinitionProvider;
import com.redhat.microprofile.ls.commons.BadLocationException;
import com.redhat.microprofile.model.Node;
import com.redhat.microprofile.model.PropertiesModel;
import com.redhat.microprofile.model.PropertyKey;
import com.redhat.microprofile.utils.MicroProfilePropertiesUtils;
import com.redhat.microprofile.utils.PositionUtils;

/**
 * The Quarkus definition.
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
	 * @param projectInfo           the Quarkus properties
	 * @param provider              the Quarkus property definition provider.
	 * @param definitionLinkSupport true if {@link LocationLink} must be returned
	 *                              and false otherwise.
	 * @return as promise the Java field definition location of the property at the
	 *         given <code>position</code> of the given application.properties
	 *         <code>document</code>.
	 */
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> findDefinition(
			PropertiesModel document, Position position, MicroProfileProjectInfo projectInfo,
			MicroProfilePropertyDefinitionProvider provider, boolean definitionLinkSupport) {
		Node node = null;
		try {
			node = document.findNodeAt(position);
			// Get the property at the given position
			PropertyKey key = getPropertyKey(node);
			if (key != null) {
				String propertyName = key.getPropertyName();
				// Get metatada of the property
				ItemMetadata item = MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo);
				if (item != null && item.getSourceType() != null) {
					// Find definition (class, field of class, method of class) only when metadata
					// contains source type (class or interface)
					MicroProfilePropertyDefinitionParams definitionParams = new MicroProfilePropertyDefinitionParams();
					definitionParams.setUri(document.getDocumentURI());
					definitionParams.setSourceType(item.getSourceType());
					definitionParams.setSourceField(item.getSourceField());
					definitionParams.setSourceMethod(item.getSourceMethod());
					return provider.getPropertyDefinition(definitionParams).thenApply(target -> {
						if (target == null) {
							return null;
						}
						if (definitionLinkSupport) {
							// Use document link
							LocationLink link = new LocationLink(target.getUri(), target.getRange(), target.getRange(),
									PositionUtils.createRange(key));
							return Either.forRight(Collections.singletonList(link));
						}
						// Use simple location
						return Either.forLeft(Collections.singletonList(target));
					});
				}
			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In MicroProfileDefinition, position error", e);
		}
		return CompletableFuture.completedFuture(definitionLinkSupport ? Either.forRight(Collections.emptyList())
				: Either.forLeft(Collections.emptyList()));
	}

	private static PropertyKey getPropertyKey(Node node) {
		if (node == null) {
			return null;
		}
		switch (node.getNodeType()) {
		case PROPERTY_KEY:
			return (PropertyKey) node;
		default:
			return null;
		}
	}
}
