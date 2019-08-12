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
 * Quarkus shared settings.
 * 
 * @author Angelo ZERR
 *
 */
public class SharedSettings {

	private final QuarkusCompletionSettings completionSettings;
	private final QuarkusHoverSettings hoverSettings;

	public SharedSettings() {
		this.completionSettings = new QuarkusCompletionSettings();
		this.hoverSettings = new QuarkusHoverSettings();
	}

	/**
	 * Returns the completion settings.
	 * 
	 * @return the completion settings.
	 */
	public QuarkusCompletionSettings getCompletionSettings() {
		return completionSettings;
	}

	/**
	 * Returns the hover settings.
	 * 
	 * @return the hover settings.
	 */
	public QuarkusHoverSettings getHoverSettings() {
		return hoverSettings;
	}
}
