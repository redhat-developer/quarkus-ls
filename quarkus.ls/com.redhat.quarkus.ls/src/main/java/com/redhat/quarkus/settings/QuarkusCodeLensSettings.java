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

/**
 * Quarkus code lens settings.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusCodeLensSettings {

	private boolean urlCodeLensEnabled;

	public boolean isUrlCodeLensEnabled() {
		return urlCodeLensEnabled;
	}

	public void setUrlCodeLensEnabled(boolean urlCodeLensEnabled) {
		this.urlCodeLensEnabled = urlCodeLensEnabled;
	}

}
