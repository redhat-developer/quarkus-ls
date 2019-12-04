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
import java.util.stream.Collectors;

import com.redhat.microprofile.commons.metadata.ItemHint.ValueHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.model.PropertiesModel;

/**
 * Values rule to manage values for some property. A values rule is composed by
 * 
 * <ul>
 * <li>a matcher to know if a given property match the values rule</li>
 * <li>the list of values {@link ValueHint}.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class ValuesRule {

	private PropertyMatcher matcher;

	private List<ValueHint> values;

	private List<String> valuesRef;

	private transient boolean valuesCleaned;

	public PropertyMatcher getMatcher() {
		return matcher;
	}

	public void setMatcher(PropertyMatcher matcher) {
		this.matcher = matcher;
	}

	public void setValues(List<ValueHint> values) {
		this.values = values;
		this.valuesCleaned = false;
	}

	public List<ValueHint> getValues() {
		cleanValues();
		return values;
	}

	private void cleanValues() {
		if (valuesCleaned || values == null || values.isEmpty()) {
			return;
		}
		try {
			values = values.stream().filter(e -> e.getValue() != null && !e.getValue().isEmpty())
					.collect(Collectors.toList());
		} finally {
			valuesCleaned = true;
		}
	}

	public List<String> getValuesRef() {
		return valuesRef;
	}

	/**
	 * Returns true if the given metadata property match the property matcher and
	 * false otherwise.
	 * 
	 * @param metadata the metadata property to match
	 * @param model    the properties model
	 * @return true if the given metadata property match the property matcher and
	 *         false otherwise.
	 */
	public boolean match(ItemMetadata metadata, PropertiesModel model) {
		return getMatcher().match(metadata, model);
	}

}
