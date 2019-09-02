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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.quarkus.model.Node;
import com.redhat.quarkus.model.Node.NodeType;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.Property;
import com.redhat.quarkus.model.PropertyKey;
import com.redhat.quarkus.utils.PositionUtils;

/**
 * The Quarkus symbols provider
 * 
 * @author Angelo ZERR
 *
 */
class QuarkusSymbolsProvider {

	/**
	 * Returns symbol information list for the given properties model.
	 * 
	 * @param document      the properties model document
	 * @param cancelChecker the cancel checker
	 * @return symbol information list for the given properties model.
	 */
	public List<SymbolInformation> findSymbolInformations(PropertiesModel document, CancelChecker cancelChecker) {
		List<SymbolInformation> symbols = new ArrayList<>();
		for (Node node : document.getChildren()) {
			if (cancelChecker != null) {
				cancelChecker.checkCanceled();
			}
			if (node.getNodeType() == NodeType.PROPERTY) {
				// It's a property (not a comments)
				Property property = (Property) node;
				String name = getSymbolName(property);
				if (name != null && !name.isEmpty()) {
					// The property is not an empty line
					Range range = getSymbolRange(property);
					Location location = new Location(document.getDocumentURI(), range);
					SymbolInformation symbol = new SymbolInformation(name, getSymbolKind(property), location);
					symbols.add(symbol);
				}
			}
		}
		return symbols;
	}

	private static String getSymbolName(Property property) {
		PropertyKey key = property.getKey();
		if (key == null) {
			return null;
		}
		return key.getText();
	}

	private static Range getSymbolRange(Property property) {
		return PositionUtils.createRange(property.getStart(), property.getEnd(), property.getDocument());
	}

	private static SymbolKind getSymbolKind(Property property) {
		return SymbolKind.Property;
	}

}
