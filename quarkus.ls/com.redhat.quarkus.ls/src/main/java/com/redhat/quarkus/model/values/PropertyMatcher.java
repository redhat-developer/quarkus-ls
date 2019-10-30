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

import java.util.Collection;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.model.PropertiesModel;

/**
 * The property matcher of a values rule.
 * 
 * @author Angelo ZERR
 *
 */
public class PropertyMatcher {

	private Collection<String> names;

	private Collection<String> types;

	public Collection<String> getNames() {
		return names;
	}

	public void setNames(Collection<String> names) {
		this.names = names;
	}

	public Collection<String> getTypes() {
		return types;
	}

	public void setTypes(Collection<String> types) {
		this.types = types;
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
		String propertyName = metadata.getPropertyName();
		String propertyType = metadata.getType();
		if (names != null && types != null) {
			return matchName(propertyName) && matchType(propertyType);
		} else if (names != null) {
			return matchName(propertyName);
		} else if (types != null) {
			return matchType(propertyType);
		}
		// TODO: manage dependency properties (properties coming from the model).
		return false;
	}

	private boolean matchName(String propertyName) {
		return names.contains(propertyName);
	}

	private boolean matchType(String propertyType) {
		return types.contains(propertyType);
	}
}