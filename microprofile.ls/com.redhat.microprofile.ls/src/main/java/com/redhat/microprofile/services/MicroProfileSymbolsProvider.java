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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.microprofile.model.Node;
import com.redhat.microprofile.model.PropertiesModel;
import com.redhat.microprofile.model.Property;
import com.redhat.microprofile.model.PropertyKey;
import com.redhat.microprofile.model.Node.NodeType;
import com.redhat.microprofile.utils.PositionUtils;

/**
 * The Quarkus symbols provider
 * 
 * @author Angelo ZERR
 *
 */
class MicroProfileSymbolsProvider {

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

	/**
	 * Returns document symbol list for the given properties model.
	 * 
	 * @param document      the properties model document
	 * @param cancelChecker the cancel checker
	 * @return document symbol list for the given properties model.
	 */
	public List<DocumentSymbol> findDocumentSymbols(PropertiesModel document, CancelChecker cancelChecker) {
		List<DocumentSymbol> symbols = new ArrayList<>();
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
					String[] paths = name.split("[.]");
					DocumentSymbol symbol = null;
					for (String path : paths) {
						symbol = getSymbol(path, property, symbol != null ? symbol.getChildren() : symbols);
					}
					symbol.setKind(SymbolKind.Property);
					String value = property.getPropertyValue();
					if (value != null) {
						symbol.setDetail(value);
					}
				}
			}
		}
		return symbols;
	}

	private static DocumentSymbol getSymbol(String path, Property property, List<DocumentSymbol> children) {
		for (DocumentSymbol child : children) {
			if (path.equals(child.getName())) {
				return child;
			}
		}
		Range range = getSymbolRange(property);
		DocumentSymbol symbol = new DocumentSymbol(path, SymbolKind.Package, range, range);
		symbol.setChildren(new ArrayList<>());
		children.add(symbol);
		return symbol;
	}

	private static String getSymbolName(Property property) {
		PropertyKey key = property.getKey();
		if (key == null) {
			return null;
		}
		return key.getText();
	}

	private static Range getSymbolRange(Property property) {
		return PositionUtils.createRange(property);
	}

	private static SymbolKind getSymbolKind(Property property) {
		return SymbolKind.Property;
	}

}
