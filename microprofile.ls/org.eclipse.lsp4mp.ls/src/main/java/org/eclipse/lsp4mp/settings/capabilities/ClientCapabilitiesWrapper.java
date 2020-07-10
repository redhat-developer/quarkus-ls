/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others. All rights reserved. This program
 * and the accompanying materials which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.lsp4mp.settings.capabilities;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.DynamicRegistrationCapabilities;
import org.eclipse.lsp4j.ResourceOperationKind;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4mp.ls.commons.client.ExtendedClientCapabilities;

/**
 * Determines if a client supports a specific capability dynamically
 */
public class ClientCapabilitiesWrapper {

	private boolean v3Supported;

	private ClientCapabilities capabilities;

	private final ExtendedClientCapabilities extendedCapabilities;

	public ClientCapabilitiesWrapper() {
		this(new ClientCapabilities(), null);
	}

	public ClientCapabilitiesWrapper(ClientCapabilities capabilities, ExtendedClientCapabilities extendedCapabilities) {
		this.capabilities = capabilities;
		this.v3Supported = capabilities != null ? capabilities.getTextDocument() != null : false;
		this.extendedCapabilities = extendedCapabilities;
	}

	/**
	 * IMPORTANT
	 * 
	 * This should be up to date with all Server supported capabilities
	 * 
	 */

	public boolean isCodeActionDynamicRegistered() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getCodeAction());
	}

	public boolean isCodeLensDynamicRegistered() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getCodeLens());
	}

	public boolean isCompletionDynamicRegistrationSupported() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getCompletion());
	}

	public boolean isHoverDynamicRegistered() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getHover());
	}

	public boolean isDocumentSymbolDynamicRegistrationSupported() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getDocumentSymbol());
	}

	public boolean isDefinitionDynamicRegistered() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getDefinition());
	}

	public boolean isFormattingDynamicRegistered() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getFormatting());
	}

	public boolean isRangeFormattingDynamicRegistered() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getRangeFormatting());
	}

	private boolean isDynamicRegistrationSupported(DynamicRegistrationCapabilities capability) {
		return capability != null && capability.getDynamicRegistration() != null
				&& capability.getDynamicRegistration().booleanValue();
	}

	public TextDocumentClientCapabilities getTextDocument() {
		return this.capabilities.getTextDocument();
	}

	public ExtendedClientCapabilities getExtendedCapabilities() {
		return extendedCapabilities;
	}
	
	public boolean isResourceOperationSupported() {
		//@formatter:off
		return capabilities.getWorkspace() != null
				&& capabilities.getWorkspace().getWorkspaceEdit() != null
				&& capabilities.getWorkspace().getWorkspaceEdit().getResourceOperations() != null
				&& capabilities.getWorkspace().getWorkspaceEdit().getResourceOperations().contains(ResourceOperationKind.Create)
				&& capabilities.getWorkspace().getWorkspaceEdit().getResourceOperations().contains(ResourceOperationKind.Rename)
				&& capabilities.getWorkspace().getWorkspaceEdit().getResourceOperations().contains(ResourceOperationKind.Delete);
		//@formatter:on
	}
}