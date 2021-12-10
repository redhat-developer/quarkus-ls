/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.settings;

import com.redhat.qute.ls.commons.client.CommandCapabilities;

/**
 * A wrapper around LSP {@link CommandCapabilities}.
 */
public class QuteCommandCapabilities {
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
	 * See {@link com.redhat.qute.ls.commons.client.CommandKind}
	 *
	 * @param commandKind the command kind to check for
	 * @return <code>true</code> if the client supports the <code>commandKind</code>
	 *         commandKind. Otherwise, returns <code>false</code>
	 */
	public boolean isCommandSupported(String commandKind) {
		return capabilities != null && capabilities.isSupported(commandKind);
	}
}