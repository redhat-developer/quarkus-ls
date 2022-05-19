/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.inlayhint.InlayHintASTVistor;
import com.redhat.qute.settings.QuteInlayHintSettings;

/**
 * Qute inlay hint support.
 *
 * @author Angelo ZERR
 *
 */
public class QuteInlayHint {

	private static final Logger LOGGER = Logger.getLogger(QuteInlayHint.class.getName());

	private static CompletableFuture<List<InlayHint>> NO_INLAY_HINT = CompletableFuture
			.completedFuture(Collections.emptyList());

	private final JavaDataModelCache javaCache;

	public QuteInlayHint(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	public CompletableFuture<List<InlayHint>> getInlayHint(Template template, Range range,
			QuteInlayHintSettings inlayHintSettings, ResolvingJavaTypeContext resolvingJavaTypeContext,
			CancelChecker cancelChecker) {
		QuteInlayHintSettings settings = inlayHintSettings != null ? inlayHintSettings : QuteInlayHintSettings.DEFAULT;
		if (!settings.isEnabled()) {
			return NO_INLAY_HINT;
		}
		return javaCache.getDataModelTemplate(template) //
				.thenApply(templateDataModel -> {
					// get range offsets
					int startOffset = -1;
					int endOffset = -1;
					if (range != null) {
						try {
							startOffset = template.offsetAt(range.getStart());
							endOffset = template.offsetAt(range.getEnd());
						} catch (BadLocationException e) {
							LOGGER.log(Level.SEVERE, "Error while getting offsets view port range", e);
						}
					}
					cancelChecker.checkCanceled();
					InlayHintASTVistor visitor = new InlayHintASTVistor(javaCache, startOffset, endOffset, settings,
							resolvingJavaTypeContext, cancelChecker);
					template.accept(visitor);
					cancelChecker.checkCanceled();
					return visitor.getInlayHints();
				});
	}

}
