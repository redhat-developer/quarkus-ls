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
package com.redhat.qute.settings.capabilities;

import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_CODELENS_OPTIONS;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_CODE_ACTION_OPTIONS;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_COMPLETION_OPTIONS;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_DOCUMENT_LINK_OPTIONS;

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
		serverCapabilities.setDocumentHighlightProvider(!clientCapabilities.isDocumentHighlightDynamicRegistered());
		serverCapabilities.setDefinitionProvider(!clientCapabilities.isDefinitionDynamicRegistered());
		if (!clientCapabilities.isDocumentLinkDynamicRegistered()) {
			serverCapabilities.setDocumentLinkProvider(DEFAULT_DOCUMENT_LINK_OPTIONS);
		}
		serverCapabilities
				.setDocumentSymbolProvider(!clientCapabilities.isDocumentSymbolDynamicRegistrationSupported());
		serverCapabilities.setHoverProvider(!clientCapabilities.isHoverDynamicRegistered());
		if (!clientCapabilities.isCodeActionDynamicRegistered()) {
			serverCapabilities.setCodeActionProvider(DEFAULT_CODE_ACTION_OPTIONS);
		}
		if (!clientCapabilities.isCompletionDynamicRegistrationSupported()) {
			serverCapabilities.setCompletionProvider(DEFAULT_COMPLETION_OPTIONS);
		}
		if (!clientCapabilities.isCodeLensDynamicRegistered()) {
			serverCapabilities.setCodeLensProvider(DEFAULT_CODELENS_OPTIONS);
		}
		serverCapabilities.setReferencesProvider(!clientCapabilities.isReferencesDynamicRegistrationSupported());
		serverCapabilities.setLinkedEditingRangeProvider(!clientCapabilities.isLinkedEditingRangeDynamicRegistered());
		serverCapabilities.setInlayHintProvider(!clientCapabilities.isInlayHintDynamicRegistered());
		serverCapabilities.setRenameProvider(!clientCapabilities.isRenameDynamicRegistered());
		return serverCapabilities;
	}
}