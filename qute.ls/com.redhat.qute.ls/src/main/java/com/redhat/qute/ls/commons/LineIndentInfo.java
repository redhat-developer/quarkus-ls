/*******************************************************************************
 * Copyright (c) 2022 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr. - initial API and implementation
 *
 *******************************************************************************/
package com.redhat.qute.ls.commons;

/**
 * Line indent information.
 * 
 * @author Angelo ZERR
 *
 */
public class LineIndentInfo {

	private final String lineDelimiter;
	private final String whitespacesIndent;

	public LineIndentInfo(String lineDelimiter, String whitespacesIndent) {
		this.lineDelimiter = lineDelimiter;
		this.whitespacesIndent = whitespacesIndent;
	}

	/**
	 * Returns the line delimiter of a given line number of {@link TextDocument}.
	 * 
	 * @return the line delimiter of a given line number of {@link TextDocument}.
	 */
	public String getLineDelimiter() {
		return lineDelimiter;
	}

	/**
	 * Returns the whitespace indent of a given line number of {@link TextDocument}.
	 * 
	 * @return the whitespace indent of a given line number of {@link TextDocument}.
	 */
	public String getWhitespacesIndent() {
		return whitespacesIndent;
	}

}
