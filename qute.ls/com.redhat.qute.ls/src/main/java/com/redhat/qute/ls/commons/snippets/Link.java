/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.ls.commons.snippets;

/**
 * Link used for documentation.
 * 
 * @author Angelo ZERR
 *
 */
public class Link {

	private final String url;
	private final String label;

	public Link(String url, String label) {
		this.url = url;
		this.label = label;
	}

	public String getUrl() {
		return url;
	}

	public String getLabel() {
		return label;
	}

}
