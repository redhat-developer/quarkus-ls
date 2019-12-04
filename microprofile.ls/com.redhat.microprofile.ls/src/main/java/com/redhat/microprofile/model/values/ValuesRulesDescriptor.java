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

/**
 * Descriptor which declares list of values rules {@link ValuesRule}.
 * 
 * @author Angelo ZERR
 *
 */
public class ValuesRulesDescriptor {

	private List<ValuesDefinition> definitions;

	private List<ValuesRule> rules;

	public List<ValuesDefinition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<ValuesDefinition> definitions) {
		this.definitions = definitions;
	}

	/**
	 * Returns the values rules list.
	 * 
	 * @return the values rules list.
	 */
	public List<ValuesRule> getRules() {
		return rules;
	}

	/**
	 * Set the values rules list.
	 * 
	 * @param rules the values rules list.
	 */
	public void setRules(List<ValuesRule> rules) {
		this.rules = rules;
	}

}
