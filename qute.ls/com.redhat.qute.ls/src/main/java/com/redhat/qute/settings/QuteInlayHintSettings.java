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

import java.util.Objects;

/**
 * Qute inlay hint settings.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteInlayHintSettings {

	public static final QuteInlayHintSettings DEFAULT = new QuteInlayHintSettings();

	private boolean enabled;

	private boolean showSectionParameterType;

	private boolean showSectionParameterDefaultValue;

	private boolean showMessages;

	public QuteInlayHintSettings() {
		setEnabled(true);
		setShowSectionParameterType(true);
		setShowSectionParameterDefaultValue(true);
		setShowMessages(true);
	}

	/**
	 * Returns true if inlay hint support is enabled and false otherwise.
	 * 
	 * @return true if inlay hint support is enabled and false otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns true if section parameter type must be shown as inlay hint and false
	 * otherwise.
	 * 
	 * @return true if section parameter type must be shown as inlay hint and false
	 *         otherwise.
	 */
	public boolean isShowSectionParameterType() {
		return showSectionParameterType;
	}

	public void setShowSectionParameterType(boolean showSectionParameterType) {
		this.showSectionParameterType = showSectionParameterType;
	}

	public boolean isShowSectionParameterDefaultValue() {
		return showSectionParameterDefaultValue;
	}

	public void setShowSectionParameterDefaultValue(boolean showSectionParameterDefaultValue) {
		this.showSectionParameterDefaultValue = showSectionParameterDefaultValue;
	}

	public boolean isShowMessages() {
		return showMessages;
	}

	public void setShowMessages(boolean showMessages) {
		this.showMessages = showMessages;
	}

	public void update(QuteInlayHintSettings newInlayHint) {
		this.setEnabled(newInlayHint.isEnabled());
		this.setShowSectionParameterType(newInlayHint.isShowSectionParameterType());
		this.setShowSectionParameterDefaultValue(newInlayHint.isShowSectionParameterDefaultValue());
		this.setShowMessages(newInlayHint.isShowMessages());
	}

	@Override
	public int hashCode() {
		return Objects.hash(enabled, showMessages, showSectionParameterDefaultValue, showSectionParameterType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuteInlayHintSettings other = (QuteInlayHintSettings) obj;
		return enabled == other.enabled && showMessages == other.showMessages
				&& showSectionParameterDefaultValue == other.showSectionParameterDefaultValue
				&& showSectionParameterType == other.showSectionParameterType;
	}

}
