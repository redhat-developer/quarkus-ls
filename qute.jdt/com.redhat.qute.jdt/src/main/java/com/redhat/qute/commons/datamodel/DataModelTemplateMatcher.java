/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons.datamodel;

import java.util.List;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Data model template matcher used to inject data parameters with patterns.
 */
public class DataModelTemplateMatcher {

	private List<String> includes;

	public DataModelTemplateMatcher() {

	}

	public DataModelTemplateMatcher(List<String> includes) {
		setIncludes(includes);
	}

	public List<String> getIncludes() {
		return includes;
	}

	public void setIncludes(List<String> includes) {
		this.includes = includes;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("includes", this.includes);
		return b.toString();
	}
}
