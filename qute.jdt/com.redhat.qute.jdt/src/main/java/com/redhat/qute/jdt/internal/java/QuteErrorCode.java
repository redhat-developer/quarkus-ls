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
package com.redhat.qute.jdt.internal.java;

public enum QuteErrorCode implements IQuteErrorCode {

	NoMatchingTemplate("No template matching the path {0} could be found for: {1}"),
	FragmentNotDefined("Fragment [{0}] not defined in template {1}");
	
	private final String rawMessage;

	QuteErrorCode(String rawMessage) {
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
