/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.injection;

public class InjectionMetadata {
	private final String languageId;
	private final InjectionMode mode;

	public InjectionMetadata(String languageId, InjectionMode mode) {
		this.languageId = languageId;
		this.mode = mode;
	}

	public String getLanguageId() {
		return languageId;
	}

	public InjectionMode getMode() {
		return mode;
	}

	/**
	 * Returns true if this is an embedded injection (no Qute parsing)
	 */
	public boolean isEmbedded() {
		return mode == InjectionMode.EMBEDDED;
	}

	/**
	 * Returns true if this is a templated injection (Qute parsing allowed)
	 */
	public boolean isTemplated() {
		return mode == InjectionMode.TEMPLATED;
	}
}