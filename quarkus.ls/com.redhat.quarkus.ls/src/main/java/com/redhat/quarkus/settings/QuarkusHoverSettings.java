/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.settings;

import org.eclipse.lsp4j.HoverCapabilities;

/**
 * A wrapper around LSP {@link HoverCapabilities}.
 *
 */
public class QuarkusHoverSettings {

	private HoverCapabilities capabilities;

	public void setCapabilities(HoverCapabilities capabilities) {
		this.capabilities = capabilities;
	}

	public HoverCapabilities getCapabilities() {
		return capabilities;
	}

	/**
	 * Returns <code>true</code> if the client support the given documentation
	 * format and <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the client support the given documentation
	 *         format and <code>false</code> otherwise.
	 */
	public boolean isContentFormatSupported(String documentationFormat) {
		return capabilities.getContentFormat() != null
				&& capabilities.getContentFormat().contains(documentationFormat);
	}

}
