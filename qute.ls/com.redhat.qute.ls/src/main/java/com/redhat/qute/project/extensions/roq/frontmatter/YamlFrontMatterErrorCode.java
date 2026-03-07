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
package com.redhat.qute.project.extensions.roq.frontmatter;

import com.redhat.qute.parser.validator.IQuteErrorCode;

/**
 * Error codes for YamlFrontMatter-specific validation diagnostics.
 * 
 * @author Angelo ZERR
 */
public enum YamlFrontMatterErrorCode implements IQuteErrorCode {

	ImageNotFound("Image not found: `{0}`."), //
	InvalidImagePath("Invalid image path `{0}: `{1}`."), //

	LayoutNotFound("Layout not found: `{0}`."), //
	InvalidLayoutPath("Invalid layout path `{0}: `{1}`.");

	private final String rawMessage;

	YamlFrontMatterErrorCode(String rawMessage) {
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