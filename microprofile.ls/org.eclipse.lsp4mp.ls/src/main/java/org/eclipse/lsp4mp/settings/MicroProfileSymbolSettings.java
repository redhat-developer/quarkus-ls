/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4mp.settings;

/**
 * MicroProfile symbol settings.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileSymbolSettings {

	private boolean showAsTree;

	public MicroProfileSymbolSettings() {
		showAsTree = true;
	}

	public boolean isShowAsTree() {
		return showAsTree;
	}

	public void setShowAsTree(boolean showAsTree) {
		this.showAsTree = showAsTree;
	}
}