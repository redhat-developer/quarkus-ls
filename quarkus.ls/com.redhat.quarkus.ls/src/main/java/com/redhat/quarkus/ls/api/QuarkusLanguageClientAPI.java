/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.ls.api;

import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Quarkus language client API.
 * 
 * @author Angelo ZERR
 *
 */
public interface QuarkusLanguageClientAPI extends LanguageClient, QuarkusProjectInfoProvider,
		QuarkusPropertyDefinitionProvider, QuarkusJavaCodeLensProvider, QuarkusJavaHoverProvider {

}
