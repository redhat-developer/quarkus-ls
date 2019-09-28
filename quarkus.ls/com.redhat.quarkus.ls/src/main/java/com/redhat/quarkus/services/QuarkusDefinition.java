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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusPropertyDefinitionParams;
import com.redhat.quarkus.ls.api.QuarkusPropertyDefinitionProvider;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.model.Node;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.PropertyKey;
import com.redhat.quarkus.utils.PositionUtils;
import com.redhat.quarkus.utils.QuarkusPropertiesUtils;

/**
 * The Quarkus definition.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusDefinition {

	private static final Logger LOGGER = Logger.getLogger(QuarkusDefinition.class.getName());

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
			PropertiesModel document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusPropertyDefinitionProvider provider, boolean definitionLinkSupport) {
		Node node = null;
		try {
			node = document.findNodeAt(position);
			// Get the property at the given position
			PropertyKey key = getPropertyKey(node);
			if (key != null) {
				String propertyName = key.getPropertyName();
				// Get metatada of the property
				ExtendedConfigDescriptionBuildItem item = QuarkusPropertiesUtils.getProperty(propertyName, projectInfo);
				if (item != null) {
					// Get Java field definition from the given property source
					String propertySource = item.getSource();
					QuarkusPropertyDefinitionParams definitionParams = new QuarkusPropertyDefinitionParams();
					definitionParams.setUri(document.getDocumentURI());
					definitionParams.setPropertySource(propertySource);
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
			LOGGER.log(Level.SEVERE, "In QuarkusDefinition, position error", e);
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
