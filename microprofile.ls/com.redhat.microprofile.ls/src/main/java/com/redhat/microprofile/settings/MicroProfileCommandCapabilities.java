/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.settings;

import com.redhat.microprofile.ls.commons.client.CommandCapabilities;

/**
 * A wrapper around LSP {@link CommandCapabilities}.
 */
public class MicroProfileCommandCapabilities {
	private CommandCapabilities capabilities;

	public void setCapabilities(CommandCapabilities capabilities) {
		this.capabilities = capabilities;
	}

	public CommandCapabilities getCapabilities() {
		return capabilities;
	}
	
	/**
	 * Returns <code>true</code> if the client supports the <code>commandKind</code>
	 * command. Otherwise, returns <code>false</code>.
	 * 
	 * See {@link com.redhat.microprofile.ls.commons.client.CommandKind}
	 *
	 * @param commandKind the command kind to check for
	 * @return <code>true</code> if the client supports the <code>commandKind</code>
	 *         commandKind. Otherwise, returns <code>false</code>
	 */
	public boolean isCommandSupported(String commandKind) {
		return capabilities != null && capabilities.isSupported(commandKind);
	}
}