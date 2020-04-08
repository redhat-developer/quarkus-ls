/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.commons;

import java.util.List;

/**
 * MicroProfile Java Project labels
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileJavaProjectLabelsParams {

	private String uri;

	private List<String> types;

	/**
	 * Returns the Java file uri.
	 * 
	 * @return the Java file uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the Java file uri.
	 * 
	 * @param uri the Java file uri.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Returns the Java types list to check.
	 * 
	 * <p>
	 * If the owner Java project of the Java file URI contains some type in the
	 * classpath, it will return the type as label in
	 * {@link ProjectLabelInfoEntry#getLabels()}
	 * </p>
	 * 
	 * @return the Java types list to check
	 */
	public List<String> getTypes() {
		return types;
	}

	/**
	 * Set the Java types list to check.
	 * 
	 * @param types the Java types list to check.
	 */
	public void setTypes(List<String> types) {
		this.types = types;
	}
}
