/**
 *  Copyright (c) 2018 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */

package org.eclipse.lsp4mp.settings.capabilities;

import static org.eclipse.lsp4mp.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_CODELENS_OPTIONS;
import static org.eclipse.lsp4mp.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_COMPLETION_OPTIONS;

import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;

/**
 * All default capabilities of this server
 */
public class ServerCapabilitiesInitializer {

	private ServerCapabilitiesInitializer() {
	}

	/**
	 * Returns all server capabilities (with default values) that aren't dynamic.
	 * 
	 * A service's dynamic capability is indicated by the client.
	 * 
	 * @param clientCapabilities
	 * @return ServerCapabilities object
	 */
	public static ServerCapabilities getNonDynamicServerCapabilities(ClientCapabilitiesWrapper clientCapabilities) {

		ServerCapabilities serverCapabilities = new ServerCapabilities();
		serverCapabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
		serverCapabilities.setHoverProvider(!clientCapabilities.isHoverDynamicRegistered());
		serverCapabilities.setDocumentFormattingProvider(!clientCapabilities.isFormattingDynamicRegistered());
		serverCapabilities.setDocumentRangeFormattingProvider(
			!clientCapabilities.isRangeFormattingDynamicRegistered());
		if (!clientCapabilities.isCompletionDynamicRegistrationSupported()) {
			serverCapabilities.setCompletionProvider(DEFAULT_COMPLETION_OPTIONS);
		}
		serverCapabilities
				.setDocumentSymbolProvider(!clientCapabilities.isDocumentSymbolDynamicRegistrationSupported());
		serverCapabilities.setDefinitionProvider(!clientCapabilities.isDefinitionDynamicRegistered());
		if (!clientCapabilities.isCodeLensDynamicRegistered()) {
			serverCapabilities.setCodeLensProvider(DEFAULT_CODELENS_OPTIONS);
		}
		return serverCapabilities;
	}
}