/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.extensions.ollama;

import org.eclipse.lsp4mp.extensions.ExtendedMicroProfileProjectInfo;
import org.eclipse.lsp4mp.extensions.ItemMetadataProvider;
import org.eclipse.lsp4mp.extensions.ItemMetadataProviderFactory;

/**
 * Factory for creating {@link ItemMetadataProvider} instance to collect
 * available Ollama models.
 * 
 * @author Angelo ZERR
 *
 */
public class OllamaItemMetadataProviderFactory implements ItemMetadataProviderFactory {

	@Override
	public ItemMetadataProvider create(ExtendedMicroProfileProjectInfo projectInfo) {
		return new OllamaItemMetadataProvider(projectInfo);
	}
}
