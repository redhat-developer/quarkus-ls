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
package com.redhat.qute.project.extensions.renarde;

import com.redhat.qute.parser.validator.IQuteErrorCode;

/**
 * Error codes for Renarde-specific validation diagnostics.
 * 
 * @author Angelo ZERR
 */
public enum RenardeErrorCode implements IQuteErrorCode {

	/**
	 * Error code for unknown message keys in the m: namespace.
	 * 
	 * <p>
	 * Raised when a message key referenced in a template (e.g., {m:main.login})
	 * does not exist in any messages.properties file.
	 * </p>
	 */
	RenardeMessages("Unknown message ''{0}''.");

	private final String rawMessage;

	RenardeErrorCode(String rawMessage) {
		this.rawMessage = rawMessage;
	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public String toString() {
		return getCode();
	}

	@Override
	public String getRawMessage() {
		return rawMessage;
	}

}