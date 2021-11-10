/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.ls.commons.client;

/**
 * Extended client capabilities not defined by the LSP.
 * 
 * @author Angelo ZERR
 */
public class ExtendedClientCapabilities {

	private CommandCapabilities commands;

	private boolean shouldLanguageServerExitOnShutdown;

	public CommandCapabilities getCommands() {
		return commands;
	}

	public void setCommands(CommandCapabilities commands) {
		this.commands = commands;
	}

	/**
	 * Sets the boolean permitting language server to exit on client shutdown()
	 * request, without waiting for client to call exit()
	 *
	 * @param shouldLanguageServerExitOnShutdown
	 */
	public void setShouldLanguageServerExitOnShutdown(boolean shouldLanguageServerExitOnShutdown) {
		this.shouldLanguageServerExitOnShutdown = shouldLanguageServerExitOnShutdown;
	}

	/**
	 * Returns true if the client should exit on shutdown() request and avoid
	 * waiting for an exit() request
	 *
	 * @return true if the language server should exit on shutdown() request
	 */
	public boolean shouldLanguageServerExitOnShutdown() {
		return shouldLanguageServerExitOnShutdown;
	}
}