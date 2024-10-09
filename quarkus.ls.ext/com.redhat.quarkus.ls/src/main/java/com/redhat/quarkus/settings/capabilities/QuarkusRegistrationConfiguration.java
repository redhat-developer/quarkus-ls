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

import java.util.List;

import org.eclipse.lsp4j.DocumentFilter;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.TextDocumentRegistrationOptions;
import org.eclipse.lsp4mp.MicroProfileLanguageIds;
import org.eclipse.lsp4mp.settings.capabilities.IMicroProfileRegistrationConfiguration;

import com.redhat.quarkus.QuarkusLanguageIds;

/**
 * Specific Quarkus LSP Registration configuration
 *
 */
public class QuarkusRegistrationConfiguration implements IMicroProfileRegistrationConfiguration {

	@Override
	public void configure(Registration registration) {
		if (registration.getRegisterOptions() instanceof TextDocumentRegistrationOptions) {
			List<DocumentFilter> documentSelector = ((TextDocumentRegistrationOptions) registration
					.getRegisterOptions()).getDocumentSelector();
			if (documentSelector != null && documentSelector.stream().anyMatch(filter -> filter != null
					&& MicroProfileLanguageIds.MICROPROFILE_PROPERTIES.equals(filter.getLanguage()))) {
				// Add "quarkus-properties" in the list of filter which contains
				// "microprofile-properties"
				documentSelector.add(new DocumentFilter(QuarkusLanguageIds.QUARKUS_PROPERTIES, null, null));
			}
		}
	}

}
