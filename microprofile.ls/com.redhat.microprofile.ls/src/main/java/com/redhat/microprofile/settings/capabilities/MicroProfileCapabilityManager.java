/**
 *  Copyright (c) 2019-2020 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package com.redhat.microprofile.settings.capabilities;

import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.CODE_ACTION_ID;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.CODE_LENS_ID;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.COMPLETION_ID;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_COMPLETION_OPTIONS;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.DEFINITION_ID;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.DOCUMENT_SYMBOL_ID;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.FORMATTING_ID;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.HOVER_ID;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.RANGE_FORMATTING_ID;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_CODE_ACTION;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_CODE_LENS;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_COMPLETION;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_DEFINITION;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_DOCUMENT_SYMBOL;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_FORMATTING;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_HOVER;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_RANGE_FORMATTING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.DocumentFilter;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.TextDocumentRegistrationOptions;
import org.eclipse.lsp4j.services.LanguageClient;

import com.redhat.microprofile.ls.commons.client.ExtendedClientCapabilities;

/**
 * Manages dynamic capabilities
 */
public class MicroProfileCapabilityManager {

	private final Set<String> registeredCapabilities = new HashSet<>(3);
	private final LanguageClient languageClient;

	private ClientCapabilitiesWrapper clientWrapper;
	private TextDocumentRegistrationOptions formattingRegistrationOptions;

	private final List<IMicroProfileRegistrationConfiguration> registrationConfigurations;
	private boolean registrationConfigurationsInitialized;

	public MicroProfileCapabilityManager(LanguageClient languageClient) {
		this.languageClient = languageClient;
		this.registrationConfigurations = new ArrayList<>();
	}

	/**
	 * Registers all dynamic capabilities that the server does not support client
	 * side preferences turning on/off
	 */
	public void initializeCapabilities() {
		if (this.getClientCapabilities().isCodeActionDynamicRegistered()) {
			registerCapability(CODE_ACTION_ID, TEXT_DOCUMENT_CODE_ACTION);
		}
		if (this.getClientCapabilities().isCodeLensDynamicRegistered()) {
			registerCapability(CODE_LENS_ID, TEXT_DOCUMENT_CODE_LENS);
		}
		if (this.getClientCapabilities().isCompletionDynamicRegistrationSupported()) {
			registerCapability(COMPLETION_ID, TEXT_DOCUMENT_COMPLETION, DEFAULT_COMPLETION_OPTIONS);
		}
		if (this.getClientCapabilities().isHoverDynamicRegistered()) {
			registerCapability(HOVER_ID, TEXT_DOCUMENT_HOVER);
		}
		if (this.getClientCapabilities().isDocumentSymbolDynamicRegistrationSupported()) {
			registerCapability(DOCUMENT_SYMBOL_ID, TEXT_DOCUMENT_DOCUMENT_SYMBOL);
		}
		if (this.getClientCapabilities().isDefinitionDynamicRegistered()) {
			registerCapability(DEFINITION_ID, TEXT_DOCUMENT_DEFINITION);
		}
		if (this.getClientCapabilities().isFormattingDynamicRegistered()) {
			// The MP language server manages properties and Java files, but for formatting
			// and range formatting
			// feature, only properties file are supported.
			// We need to inform to the client that only properties are supported for format
			// feature with register options:
			/**
			 * <pre>
			 * "registerOptions": {
			 *  "documentSelector": [
			 *      { "language": "microprofile-properties" },
			 *      { "language": "quarkus-properties" }
			 *  ]
			 * }
			 * </pre>
			 */
			registerCapability(FORMATTING_ID, TEXT_DOCUMENT_FORMATTING, getFormattingRegistrationOptions());
		}
		if (this.getClientCapabilities().isFormattingDynamicRegistered()) {
			registerCapability(RANGE_FORMATTING_ID, TEXT_DOCUMENT_RANGE_FORMATTING, getFormattingRegistrationOptions());
		}
	}

	private TextDocumentRegistrationOptions getFormattingRegistrationOptions() {
		if (formattingRegistrationOptions == null) {
			List<DocumentFilter> documentSelector = new ArrayList<>();
			documentSelector.add(new DocumentFilter("microprofile-properties", null, null));
			formattingRegistrationOptions = new TextDocumentRegistrationOptions(documentSelector);
		}
		return formattingRegistrationOptions;
	}

	public void setClientCapabilities(ClientCapabilities clientCapabilities,
			ExtendedClientCapabilities extendedClientCapabilities) {
		this.clientWrapper = new ClientCapabilitiesWrapper(clientCapabilities, extendedClientCapabilities);
	}

	public ClientCapabilitiesWrapper getClientCapabilities() {
		if (this.clientWrapper == null) {
			this.clientWrapper = new ClientCapabilitiesWrapper();
		}
		return this.clientWrapper;
	}

	public Set<String> getRegisteredCapabilities() {
		return registeredCapabilities;
	}

	private void registerCapability(String id, String method) {
		registerCapability(id, method, null);
	}

	private void registerCapability(String id, String method, Object options) {
		if (registeredCapabilities.add(id)) {
			Registration registration = new Registration(id, method, options);
			RegistrationParams registrationParams = new RegistrationParams(Collections.singletonList(registration));
			getRegistrationConfigurations().forEach(config -> {
				config.configure(registration);
			});
			languageClient.registerCapability(registrationParams);
		}
	}

	/**
	 * Returns list of registration configuration contributed with Java SPI.
	 * 
	 * @return list of registration configuration contributed with Java SPI.
	 */
	private List<IMicroProfileRegistrationConfiguration> getRegistrationConfigurations() {
		if (!registrationConfigurationsInitialized) {
			initializeRegistrationConfigurations();
		}
		return registrationConfigurations;
	}

	private synchronized void initializeRegistrationConfigurations() {
		if (registrationConfigurationsInitialized) {
			return;
		}
		ServiceLoader<IMicroProfileRegistrationConfiguration> extensions = ServiceLoader
				.load(IMicroProfileRegistrationConfiguration.class);
		extensions.forEach(extension -> {
			this.registrationConfigurations.add(extension);
		});
		registrationConfigurationsInitialized = true;
	}

}