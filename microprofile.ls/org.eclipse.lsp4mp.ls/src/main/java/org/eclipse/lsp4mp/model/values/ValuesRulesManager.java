/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.model.values;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemHint.ValueHint;
import org.eclipse.lsp4mp.model.PropertiesModel;

import com.google.gson.Gson;

/**
 * Values rules manager.
 * 
 * @author Angelo ZERR
 *
 */
public class ValuesRulesManager {

	private final Map<String, List<ValueHint>> definitionsMap;
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
							List<ValueHint> values = new ArrayList<>();
							List<ValueHint> staticValues = rule.getValues();
							if (staticValues != null) {
								values.addAll(staticValues);
							}
							List<ValueHint> refValues = definitionsMap.get(ref);
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
	 * Returns the values {@link ValueHint} from the given metadata property and
	 * null otherwise.
	 * 
	 * @param metadata the metadata property to match
	 * @param model    the properties model
	 * @return the values {@link ValueHint} from the given metadata property and
	 *         null otherwise.
	 */
	public List<ValueHint> getValues(ItemMetadata metadata, PropertiesModel model) {
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
	public boolean isValidEnum(ItemMetadata metadata, PropertiesModel model, String propertyValue) {
		List<ValueHint> enums = getValues(metadata, model);
		return enums == null || getValueHint(propertyValue, metadata, model) != null; 
	}

	/**
	 * Returns the {@link ValueHint} from the given property value for the given
	 * metadata property and null otherwise.
	 * 
	 * @param metadata the metadata property to match
	 * @param model    the properties model
	 * @return the {@link ValueHint} from the given property value for the given
	 *         metadata property and null otherwise.
	 */
	public ValueHint getValueHint(String propertyValue, ItemMetadata metadata, PropertiesModel model) {
		List<ValueHint> enums = getValues(metadata, model);
		return getValue(propertyValue, enums);
	}
	
	private ValueHint getValue(String value, List<ValueHint> values) {
		if (values == null || value == null) {
			return null;
		}
		for (ValueHint valueHint : values) {
			if (value.equals(valueHint.getValue())) {
				return valueHint;
			}
		}
		return null;
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
