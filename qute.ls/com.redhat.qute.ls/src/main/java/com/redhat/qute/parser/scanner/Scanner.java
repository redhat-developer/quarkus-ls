/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.scanner;

/**
 * Scanner API.
 *
 */
public interface Scanner<T, S> {

	T scan();

	T getTokenType();

	/**
	 * Starting offset position of the current token
	 * 
	 * @return int of token's start offset
	 */
	int getTokenOffset();

	int getTokenLength();

	/**
	 * Ending offset position of the current token
	 * 
	 * @return int of token's end offset
	 */
	int getTokenEnd();

	String getTokenText();

	String getTokenError();

	S getScannerState();
}
