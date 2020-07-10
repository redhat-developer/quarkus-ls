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

import java.util.List;

import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Specific capabilities for the `CommandKind`.
 * 
 * @see https://github.com/microsoft/language-server-protocol/issues/788
 */
@SuppressWarnings("all")
public class CommandKindCapabilities {
	/**
	 * The commands kind values the client supports. When this property exists the
	 * client also guarantees that it will handle values outside its set gracefully
	 * and falls back to a default value when unknown.
	 * 
	 * If this property is not present the client only supports the commands kinds
	 * from `File` to `Array` as defined in the initial version of the protocol.
	 */
	private List<String> valueSet;

	public CommandKindCapabilities() {
	}

	public CommandKindCapabilities(final List<String> valueSet) {
		this.valueSet = valueSet;
	}

	/**
	 * The commands kind values the client supports. When this property exists the
	 * client also guarantees that it will handle values outside its set gracefully
	 * and falls back to a default value when unknown.
	 * 
	 * If this property is not present the client only supports the commands kinds
	 * from `File` to `Array` as defined in the initial version of the protocol.
	 */
	@Pure
	public List<String> getValueSet() {
		return this.valueSet;
	}

	/**
	 * The commands kind values the client supports. When this property exists the
	 * client also guarantees that it will handle values outside its set gracefully
	 * and falls back to a default value when unknown.
	 * 
	 * If this property is not present the client only supports the commands kinds
	 * from `File` to `Array` as defined in the initial version of the protocol.
	 */
	public void setValueSet(final List<String> valueSet) {
		this.valueSet = valueSet;
	}

	@Override
	@Pure
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("valueSet", this.valueSet);
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
		CommandKindCapabilities other = (CommandKindCapabilities) obj;
		if (this.valueSet == null) {
			if (other.valueSet != null)
				return false;
		} else if (!this.valueSet.equals(other.valueSet))
			return false;
		return true;
	}

	@Override
	@Pure
	public int hashCode() {
		return 31 * 1 + ((this.valueSet == null) ? 0 : this.valueSet.hashCode());
	}
}