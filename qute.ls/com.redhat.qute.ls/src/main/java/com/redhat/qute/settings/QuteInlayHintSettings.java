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

	public QuteInlayHintSettings() {
		setEnabled(true);
		setShowSectionParameterType(true);
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

	public void update(QuteInlayHintSettings newInlayHint) {
		this.setEnabled(newInlayHint.isEnabled());
		this.setShowSectionParameterType(newInlayHint.isShowSectionParameterType());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + (showSectionParameterType ? 1231 : 1237);
		return result;
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
		if (enabled != other.enabled)
			return false;
		if (showSectionParameterType != other.showSectionParameterType)
			return false;
		return true;
	}

}
