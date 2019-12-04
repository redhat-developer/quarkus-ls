/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.model.values;

import java.util.List;

import com.redhat.microprofile.commons.metadata.ItemHint.ValueHint;

/**
 * Definition for values. A values definition gives the capability to share
 * values between several {@link PropertyMatcher}.
 * 
 * @author Angelo ZERR
 *
 */
public class ValuesDefinition {

	private String id;

	private List<ValueHint> values;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ValueHint> getValues() {
		return values;
	}

	public void setValues(List<ValueHint> values) {
		this.values = values;
	}

}
