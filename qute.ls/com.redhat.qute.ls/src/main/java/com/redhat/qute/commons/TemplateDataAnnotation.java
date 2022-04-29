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
package com.redhat.qute.commons;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * '@io.quarkus.qute.TemplateData' annotation.
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateDataAnnotation {

	/**
	 * If set to true do not automatically analyze superclasses.
	 */
	private Boolean ignoreSuperclasses;

	public boolean isIgnoreSuperclasses() {
		return ignoreSuperclasses != null && ignoreSuperclasses.booleanValue();
	}

	public void setIgnoreSuperclasses(Boolean ignoreSuperclasses) {
		this.ignoreSuperclasses = ignoreSuperclasses;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("ignoreSuperclasses", this.isIgnoreSuperclasses());
		return b.toString();
	}
}
