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

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.TextDocument;

/**
 * The Quarkus completions
 * 
 * @author Angelo ZERR
 *
 */
class QuarkusCompletions {

	public CompletionList doComplete(TextDocument document, Position position, QuarkusProjectInfo projectInfo,
			CancelChecker cancelChecker) {
		CompletionList list = new CompletionList();
		for (ExtendedConfigDescriptionBuildItem property : projectInfo.getProperties()) {
			CompletionItem item = new CompletionItem(property.getPropertyName());
			list.getItems().add(item);
		}
		return list;
	}
}
