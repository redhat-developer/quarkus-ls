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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.redhat.quarkus.commons.EnumItem;
import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.model.PropertiesModel;

/**
 * Values rules manager.
 * 
 * @author Angelo ZERR
 *
 */
public class ValuesRulesManager {

	private final Map<String, List<EnumItem>> definitionsMap;
	private final List<ValuesRule> rules;
	private boolean withDefault;

	public ValuesRulesManager(boolean withDefault) {
		this.definitionsMap = new HashMap<>();
		this.rules = new ArrayList<>();
		this.withDefault = withDefault;
	}

	private void load(InputStream in) {
		ValuesRulesDescriptor descriptor = new Gson().fromJson(new InputStreamReader(in), ValuesRulesDescriptor.class);
		if (descriptor.getDefinitions() != null) {
			registerDefinitions(descriptor.getDefinitions());
		}
		if (descriptor.getRules() != null) {
			registerRules(descriptor.getRules());
		}
	}

	public void registerDefinitions(List<ValuesDefinition> definitions) {
		definitions.forEach(def -> definitionsMap.put(def.getId(), def.getValues()));
	}

	public void registerRules(List<ValuesRule> rules) {
		this.rules.addAll(rules);
		rules.stream() //
				.filter(rule -> rule.getValuesRef() != null) //
				.forEach(rule -> rule.getValuesRef() //
						.forEach(ref -> {
							List<EnumItem> values = new ArrayList<>();
							List<EnumItem> staticValues = rule.getValues();
							if (staticValues != null) {
								values.addAll(staticValues);
							}
							List<EnumItem> refValues = definitionsMap.get(ref);
							if (refValues != null) {
								values.addAll(refValues);
							}
							rule.setValues(values); //
						}) //
				);
	}

	public void unregisterRules(List<ValuesRule> rules) {
		this.rules.removeAll(rules);
	}

	/**
	 * Returns the values {@link EnumItem} from the given metadata property and null
	 * otherwise.
	 * 
	 * @param metadata the metadata property to match
	 * @param model    the properties model
	 * @return the values {@link EnumItem} from the given metadata property and null
	 *         otherwise.
	 */
	public List<EnumItem> getValues(ExtendedConfigDescriptionBuildItem metadata, PropertiesModel model) {
		for (ValuesRule rule : getRules()) {
			if (rule.match(metadata, model)) {
				return rule.getValues();
			}
		}
		return null;
	}

	/**
	 * Returns true if the given value is valid for the given metadata property and
	 * false otherwise.
	 * 
	 * @param metadata      the metadata property to match
	 * @param model         the properties model
	 * @param propertyValue the property value to validate
	 * @return true if the given value is valid for the given metadata property and
	 *         false otherwise.
	 */
	public boolean isValidEnum(ExtendedConfigDescriptionBuildItem metadata, PropertiesModel model,
			String propertyValue) {
		List<EnumItem> enums = getValues(metadata, model);
		return ExtendedConfigDescriptionBuildItem.isValidEnum(propertyValue, enums);
	}

	/**
	 * Returns the {@link EnumItem} from the given property value for the given
	 * metadata property and null otherwise.
	 * 
	 * @param metadata the metadata property to match
	 * @param model    the properties model
	 * @return the {@link EnumItem} from the given property value for the given
	 *         metadata property and null otherwise.
	 */
	public EnumItem getEnumItem(String propertyValue, ExtendedConfigDescriptionBuildItem metadata,
			PropertiesModel model) {
		List<EnumItem> enums = getValues(metadata, model);
		return ExtendedConfigDescriptionBuildItem.getEnumItem(propertyValue, enums);
	}

	private List<ValuesRule> getRules() {
		if (isDefaultNotLoaded()) {
			synchronized (rules) {
				if (isDefaultNotLoaded()) {
					load(ValuesRulesManager.class.getResourceAsStream("quarkus-values-rules.json"));
				}
			}
		}
		return rules;
	}

	private boolean isDefaultNotLoaded() {
		return withDefault && rules.isEmpty();
	}
}
