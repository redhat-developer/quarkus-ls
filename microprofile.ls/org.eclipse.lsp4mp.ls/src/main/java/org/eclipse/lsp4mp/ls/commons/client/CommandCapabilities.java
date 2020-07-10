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

import org.eclipse.lsp4j.DynamicRegistrationCapabilities;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Extended capabilities for client commands.
 * 
 * @author Angelo ZERR
 */
@SuppressWarnings("all")
public class CommandCapabilities extends DynamicRegistrationCapabilities {
	/**
	 * Specific capabilities for the `CommandKind` in the `textDocument/commands`
	 * request.
	 */
	private CommandKindCapabilities commandsKind;

	public CommandCapabilities() {
	}

	public CommandCapabilities(final Boolean dynamicRegistration) {
		super(dynamicRegistration);
	}

	public CommandCapabilities(final CommandKindCapabilities commandsKind) {
		this.commandsKind = commandsKind;
	}

	public CommandCapabilities(final CommandKindCapabilities commandsKind,
			final Boolean dynamicRegistration) {
		super(dynamicRegistration);
		this.commandsKind = commandsKind;
	}

	public boolean isSupported(String command) {
		return commandsKind.getValueSet().contains(command);
	}

	/**
	 * Specific capabilities for the `CommandKind` in the `textDocument/commands`
	 * request.
	 */
	@Pure
	public CommandKindCapabilities getCommandKind() {
		return this.commandsKind;
	}

	/**
	 * Specific capabilities for the `CommandKind` in the `textDocument/commands`
	 * request.
	 */
	public void setCommandKind(final CommandKindCapabilities commandsKind) {
		this.commandsKind = commandsKind;
	}

	@Override
	@Pure
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("commandsKind", this.commandsKind);
		b.add("dynamicRegistration", getDynamicRegistration());
		return b.toString();
	}

	@Override
	@Pure
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (!super.equals(obj))
			return false;
		CommandCapabilities other = (CommandCapabilities) obj;
		if (this.commandsKind == null) {
			if (other.commandsKind != null)
				return false;
		} else if (!this.commandsKind.equals(other.commandsKind))
			return false;
		return true;
	}

	@Override
	@Pure
	public int hashCode() {
		return 31 * super.hashCode() + ((this.commandsKind == null) ? 0 : this.commandsKind.hashCode());
	}
}