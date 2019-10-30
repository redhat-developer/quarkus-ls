/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.model.values;

import java.util.List;
import java.util.stream.Collectors;

import com.redhat.quarkus.commons.EnumItem;
import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.model.PropertiesModel;

/**
 * Values rule to manage values for some property. A values rule is composed by
 * 
 * <ul>
 * <li>a matcher to know if a given property match the values rule</li>
 * <li>the list of values {@link EnumItem}.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class ValuesRule {

	private PropertyMatcher matcher;

	private List<EnumItem> values;

	private List<String> valuesRef;

	private transient boolean valuesCleaned;

	public PropertyMatcher getMatcher() {
		return matcher;
	}

	public void setMatcher(PropertyMatcher matcher) {
		this.matcher = matcher;
	}

	public void setValues(List<EnumItem> values) {
		this.values = values;
		this.valuesCleaned = false;
	}

	public List<EnumItem> getValues() {
		cleanValues();
		return values;
	}

	private void cleanValues() {
		if (valuesCleaned || values == null || values.isEmpty()) {
			return;
		}
		try {
			values = values.stream().filter(e -> e.getName() != null && !e.getName().isEmpty())
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
	public boolean match(ExtendedConfigDescriptionBuildItem metadata, PropertiesModel model) {
		return getMatcher().match(metadata, model);
	}

}
