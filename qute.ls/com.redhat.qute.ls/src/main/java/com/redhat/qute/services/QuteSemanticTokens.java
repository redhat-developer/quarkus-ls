/*******************************************************************************
* Copyright (c) 2025 Red Hat Inc. and others.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.semantictokens.QuteSemanticTokenType;
import com.redhat.qute.services.semantictokens.QuteSemanticTokensASTVisitor;
import com.redhat.qute.services.semantictokens.QuteSemanticTokensASTVisitor.TokenData;
import com.redhat.qute.settings.SharedSettings;

/**
 * Qute semantic tokens support.
 *
 * @author Angelo ZERR
 *
 */
public class QuteSemanticTokens {

	public QuteSemanticTokens() {
	}

	/**
	 * Returns semantic tokens for the full document.
	 *
	 * @param template      the template
	 * @param settings      the shared settings
	 * @param cancelChecker the cancel checker
	 * @return semantic tokens for the full document
	 */
	public CompletableFuture<SemanticTokens> getSemanticTokensFull(Template template, SharedSettings settings,
			CancelChecker cancelChecker) {
		try {
			int startOffset = 0;
			int endOffset = template.getEnd();

			// Create and run visitor
			QuteSemanticTokensASTVisitor visitor = new QuteSemanticTokensASTVisitor(startOffset, endOffset, template,
					cancelChecker);
			template.accept(visitor);

			// Encode tokens in LSP format
			List<TokenData> tokens = visitor.getTokens();
			List<Integer> data = encodeTokens(tokens);

			SemanticTokens semanticTokens = new SemanticTokens(data);
			return CompletableFuture.completedFuture(semanticTokens);
		} catch (Exception e) {
			return CompletableFuture.completedFuture(new SemanticTokens(Collections.emptyList()));
		}
	}

	/**
	 * Encodes tokens in LSP delta format.
	 *
	 * Each token is represented by 5 integers: - deltaLine: line delta from
	 * previous token - deltaStartChar: character delta from previous token (or from
	 * line start if on new line) - length: token length - tokenType: index in
	 * TOKEN_TYPES - tokenModifiers: bitmask of modifiers
	 *
	 * @param tokens the tokens to encode
	 * @return the encoded data
	 */
	private List<Integer> encodeTokens(List<TokenData> tokens) {
		if (tokens.isEmpty()) {
			return Collections.emptyList();
		}

		// Sort tokens by line and character
		tokens.sort((a, b) -> {
			int lineDiff = Integer.compare(a.line, b.line);
			if (lineDiff != 0) {
				return lineDiff;
			}
			return Integer.compare(a.startChar, b.startChar);
		});

		List<Integer> data = new ArrayList<>(tokens.size() * 5);
		int prevLine = 0;
		int prevChar = 0;

		for (TokenData token : tokens) {
			int deltaLine = token.line - prevLine;
			int deltaChar = (deltaLine == 0) ? (token.startChar - prevChar) : token.startChar;

			int tokenTypeIndex = QuteSemanticTokenType.TOKEN_TYPES.indexOf(token.tokenType);
			if (tokenTypeIndex == -1) {
				// Unknown token type, skip
				continue;
			}

			data.add(deltaLine);
			data.add(deltaChar);
			data.add(token.length);
			data.add(tokenTypeIndex);
			data.add(token.modifiers);

			prevLine = token.line;
			prevChar = token.startChar;
		}

		return data;
	}
}
