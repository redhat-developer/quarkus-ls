/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.settings;

import org.eclipse.lsp4j.CodeActionCapabilities;

/**
 * Represents the settings for code actions.
 * 
 * @author datho7561
 */
public class QuteCodeActionSettings {

	private CodeActionCapabilities codeActionCapabilities;

	/**
	 * Returns the code action capabilities.
	 * 
	 * @return the code action capabilities
	 */
	public CodeActionCapabilities getCapabtilities() {
		return this.codeActionCapabilities;
	}

	/**
	 * Sets the code action capabilities.
	 * 
	 * @param codeActionCapabilities the code action capabilities
	 */
	public void setCapabilities(CodeActionCapabilities codeActionCapabilities) {
		this.codeActionCapabilities = codeActionCapabilities;
	}

	/**
	 * Returns true if both codeAction/resolve and codeAction#data are supported.
	 * 
	 * @return true if both codeAction/resolve and codeAction#data are supported
	 */
	public boolean isResolveSupported() {
		return codeActionCapabilities != null && codeActionCapabilities.getResolveSupport() != null
				&& codeActionCapabilities.getResolveSupport().getProperties() != null
				&& codeActionCapabilities.getResolveSupport().getProperties().contains("edit")
				&& codeActionCapabilities.getDataSupport() != null
				&& codeActionCapabilities.getDataSupport().booleanValue();
	}

}
