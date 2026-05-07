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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lsp4j.SemanticTokenModifiers;
import org.eclipse.lsp4j.SemanticTokenTypes;

/**
 * Qute semantic token types.
 *
 * @author Angelo ZERR
 *
 */
public enum QuteSemanticTokenType {

	USER_TAG(SemanticTokenTypes.Method, SemanticTokenModifiers.Declaration),
	CORE_TAG(SemanticTokenTypes.Keyword),
	INSERT_SLOT(SemanticTokenTypes.TypeParameter);

	private final String tokenType;
	private final String[] modifiersList;
	private int modifiersBitmask = -1;

	QuteSemanticTokenType(String tokenType, String... modifiers) {
		this.tokenType = tokenType;
		this.modifiersList = modifiers != null ? modifiers : new String[0];
	}

	public String getTokenType() {
		return tokenType;
	}

	public int getModifiers() {
		if (modifiersBitmask == -1) {
			modifiersBitmask = calculateModifiersBitmask();
		}
		return modifiersBitmask;
	}

	/**
	 * Calculates the bitmask for this token type's modifiers.
	 *
	 * @return the bitmask
	 */
	private int calculateModifiersBitmask() {
		if (modifiersList.length == 0) {
			return 0;
		}

		int bitmask = 0;
		for (String modifier : modifiersList) {
			int index = TOKEN_MODIFIERS.indexOf(modifier);
			if (index >= 0) {
				bitmask |= (1 << index);
			}
		}
		return bitmask;
	}

	/**
	 * Semantic token types legend.
	 */
	public static final List<String> TOKEN_TYPES = Arrays.stream(values())
			.map(QuteSemanticTokenType::getTokenType)
			.toList();

	/**
	 * Semantic token modifiers legend.
	 */
	public static final List<String> TOKEN_MODIFIERS = collectTokenModifiers();

	/**
	 * Collects all unique modifiers used across all token types.
	 *
	 * @return list of all modifiers
	 */
	private static List<String> collectTokenModifiers() {
		Set<String> allModifiers = new LinkedHashSet<>();
		for (QuteSemanticTokenType type : values()) {
			allModifiers.addAll(Arrays.asList(type.modifiersList));
		}
		return List.copyOf(allModifiers);
	}
}
