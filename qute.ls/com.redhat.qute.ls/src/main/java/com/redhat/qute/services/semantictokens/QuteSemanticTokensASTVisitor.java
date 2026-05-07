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
package com.redhat.qute.services.semantictokens;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.CaseSection;
import com.redhat.qute.parser.template.sections.CustomSection;
import com.redhat.qute.parser.template.sections.EachSection;
import com.redhat.qute.parser.template.sections.ElseSection;
import com.redhat.qute.parser.template.sections.ForSection;
import com.redhat.qute.parser.template.sections.FragmentSection;
import com.redhat.qute.parser.template.sections.IfSection;
import com.redhat.qute.parser.template.sections.IncludeSection;
import com.redhat.qute.parser.template.sections.InsertSection;
import com.redhat.qute.parser.template.sections.IsSection;
import com.redhat.qute.parser.template.sections.LetSection;
import com.redhat.qute.parser.template.sections.SetSection;
import com.redhat.qute.parser.template.sections.SwitchSection;
import com.redhat.qute.parser.template.sections.WhenSection;
import com.redhat.qute.parser.template.sections.WithSection;

/**
 * AST visitor used to collect semantic tokens for user tags and insert
 * sections.
 *
 * @author Angelo ZERR
 *
 */
public class QuteSemanticTokensASTVisitor extends ASTVisitor {

	private final int startOffset;
	private final int endOffset;
	private final CancelChecker cancelChecker;
	private final List<TokenData> tokens;
	private final Template template;

	public QuteSemanticTokensASTVisitor(int startOffset, int endOffset, Template template,
			CancelChecker cancelChecker) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.template = template;
		this.cancelChecker = cancelChecker;
		this.tokens = new ArrayList<>();
	}

	public List<TokenData> getTokens() {
		return tokens;
	}

	@Override
	public boolean visit(CustomSection node) {
		if (template.getProject() == null) {
			return true;
		}

		// Check if this is a user tag (there's a matching tag file in /tags/)
		boolean isUserTag = template.getProject().findUserTag(node.getTag()) != null;
		if (isUserTag) {
			return addSemanticTokensForSection(node, QuteSemanticTokenType.USER_TAG);
		}

		// Check if this is an insert slot (e.g., {#body} that corresponds to {#insert body})
		boolean isInsertSlot = template.getProject().findInsertSlotParameter(node, node.getTag()) != null;
		if (isInsertSlot) {
			return addSemanticTokensForSection(node, QuteSemanticTokenType.INSERT_SLOT);
		}

		// Not a user tag nor an insert slot, don't add any token
		return true;
	}

	@Override
	public boolean visit(InsertSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(ForSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(EachSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(IfSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(ElseSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(LetSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(SetSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(WithSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(SwitchSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(CaseSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(WhenSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(IsSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(FragmentSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	@Override
	public boolean visit(IncludeSection node) {
		return addSemanticTokensForSection(node, QuteSemanticTokenType.CORE_TAG);
	}

	/**
	 * Add tokens for a section's start and end tags.
	 */
	private boolean addSemanticTokensForSection(Section node, QuteSemanticTokenType tokenType) {
		cancelChecker.checkCanceled();

		// Add token for start tag name (e.g., {#let from '{#let name="value" /}')
		int startTagStart = node.getStartTagOpenOffset();
		int startTagNameEnd = node.getStartTagNameCloseOffset();
		if (isInRange(startTagStart, startTagNameEnd)) {
			addToken(startTagStart, startTagNameEnd, tokenType);
		}

		// Add token for closing part of start tag (} or /})
		if (node.isSelfClosed()) {
			// Self-closed tag: color /} (2 characters)
			// getStartTagCloseOffset() points to '/'
			int closingStart = node.getStartTagCloseOffset();
			int closingEnd = node.getStartTagCloseOffset() + 2;
			if (isInRange(closingStart, closingEnd)) {
				addToken(closingStart, closingEnd, tokenType);
			}
		} else {
			// Regular tag: color } (1 character)
			// getStartTagCloseOffset() points to '}'
			int closingStart = node.getStartTagCloseOffset();
			int closingEnd = node.getStartTagCloseOffset() + 1;
			if (isInRange(closingStart, closingEnd)) {
				addToken(closingStart, closingEnd, tokenType);
			}
		}

		// Add token for end tag if present (e.g., {/let})
		if (node.isClosed() && node.hasEndTag()) {
			int endTagStart = node.getEndTagOpenOffset();
			int endTagEnd = node.getEndTagCloseOffset() + 1;
			if (isInRange(endTagStart, endTagEnd)) {
				addToken(endTagStart, endTagEnd, tokenType);
			}
		}

		return true;
	}

	private boolean isInRange(int start, int end) {
		return start >= startOffset && end <= endOffset;
	}

	private void addToken(int start, int end, QuteSemanticTokenType tokenType) {
		try {
			Position startPos = template.positionAt(start);
			int length = end - start;
			tokens.add(new TokenData(startPos.getLine(), startPos.getCharacter(), length, tokenType));
		} catch (BadLocationException e) {
			// Ignore invalid positions
		}
	}

	/**
	 * Data class to hold semantic token information before delta encoding.
	 */
	public static class TokenData {
		public final int line;
		public final int startChar;
		public final int length;
		public final String tokenType;
		public final int modifiers;

		public TokenData(int line, int startChar, int length, QuteSemanticTokenType tokenType) {
			this.line = line;
			this.startChar = startChar;
			this.length = length;
			this.tokenType = tokenType.getTokenType();
			this.modifiers = tokenType.getModifiers();
		}
	}
}
