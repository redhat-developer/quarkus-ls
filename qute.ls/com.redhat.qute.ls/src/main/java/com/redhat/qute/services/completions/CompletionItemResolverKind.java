/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions;

import org.eclipse.lsp4j.CompletionItem;

/**
 * Represents what resolver is needed to resolve a {@link CompletionItem}.
 * 
 * @author Angelo ZERR
 */
public enum CompletionItemResolverKind {

	UpdateOrphanEndTagSection(1);

	private final int value;

	CompletionItemResolverKind(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static CompletionItemResolverKind forValue(int value) {
		CompletionItemResolverKind[] allValues = CompletionItemResolverKind.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

}
