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

import org.eclipse.lsp4j.ConfigurationItem;


/**
 * Class representing a change to a client's config.
 */
public class ConfigurationItemEdit extends ConfigurationItem {
	private ConfigurationItemEditType editType;
	private Object value;

	/**
	 * 
	 * @param section   config section to change
	 * @param operation type of change
	 * @param value     the value for the change
	 */
	public ConfigurationItemEdit(String section, ConfigurationItemEditType editType, Object value) {
		super.setSection(section);
		this.editType = editType;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigurationItemEdit other = (ConfigurationItemEdit) obj;
		if (editType == null) {
			if (other.editType != null)
				return false;
		} else if (!editType.equals(other.editType))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((editType == null) ? 0 : editType.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
}