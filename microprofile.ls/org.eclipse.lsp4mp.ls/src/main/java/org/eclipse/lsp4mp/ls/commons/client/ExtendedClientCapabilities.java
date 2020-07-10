/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls.commons.client;

import org.eclipse.lsp4mp.ls.commons.client.CommandCapabilities;

/**
 * Extended client capabilities not defined by the LSP.
 * 
 * @author Angelo ZERR
 */
public class ExtendedClientCapabilities {

	private CommandCapabilities commands;

	public CommandCapabilities getCommands() {
		return commands;
	}

	public void setCommands(CommandCapabilities commands) {
		this.commands = commands;
	}

}