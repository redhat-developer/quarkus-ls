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
package com.redhat.quarkus.settings.capabilities;

import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_FORMATTING;
import static com.redhat.microprofile.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_RANGE_FORMATTING;

import org.eclipse.lsp4j.DocumentFilter;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.TextDocumentRegistrationOptions;

import com.redhat.microprofile.settings.capabilities.IMicroProfileRegistrationConfiguration;

/**
 * Specific Quarkus LSP Registration configuration
 *
 */
public class QuarkusRegistrationConfiguration implements IMicroProfileRegistrationConfiguration {

	@Override
	public void configure(Registration registration) {
		switch (registration.getMethod()) {
		case TEXT_DOCUMENT_FORMATTING:
		case TEXT_DOCUMENT_RANGE_FORMATTING:
			// add "quarkus-properties" as language document filter for formatting
			((TextDocumentRegistrationOptions) registration.getRegisterOptions()).getDocumentSelector()
					.add(new DocumentFilter("quarkus-properties", null, null));
			break;
		default:
			break;

		}

	}

}
