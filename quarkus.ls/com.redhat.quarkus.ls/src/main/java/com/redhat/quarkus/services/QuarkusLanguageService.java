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

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.settings.QuarkusCompletionSettings;
import com.redhat.quarkus.settings.QuarkusHoverSettings;

/**
 * The Quarkus language service.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusLanguageService {

	private final QuarkusCompletions completions;
	private final QuarkusHover hover;

	public QuarkusLanguageService() {
		this.completions = new QuarkusCompletions();
		this.hover = new QuarkusHover();
	}

	public CompletionList doComplete(TextDocument document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusCompletionSettings completionSettings, CancelChecker cancelChecker) {
		return completions.doComplete(document, position, projectInfo, completionSettings, cancelChecker);
	}

	public Hover doHover(TextDocument document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusHoverSettings hoverSettings) {
		return hover.doHover(document, position, projectInfo, hoverSettings);
	}
}
