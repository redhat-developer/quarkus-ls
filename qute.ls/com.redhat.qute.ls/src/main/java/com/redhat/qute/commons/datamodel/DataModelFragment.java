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
package com.redhat.qute.commons.datamodel;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Data model fragment hosts informations about the expected data model
 * (parameters) for a given fragment.
 *
 * @param <T> data model parameter.
 * 
 * @see <a href=
 *      "https://quarkus.io/guides/qute-reference#fragments">Fragments</a>
 * @see <a href=
 *      "https://quarkus.io/guides/qute-reference#type_safe_fragments">Type-safe
 *      Fragments</a>
 * 
 * @author Angelo ZERR
 */
public class DataModelFragment<T extends DataModelParameter> extends DataModelBaseTemplate<T> {

	private String id;

	/**
	 * Returns the fragment id.
	 * 
	 * @return the fragment id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the fragment id.
	 * 
	 * @param id the fragment id
	 */
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("id", this.id);
		b.add("parameters", this.getParameters());
		return b.toString();
	}
}
