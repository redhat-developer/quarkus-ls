/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Data model template host informations about the expected data model
 * (parameters) for a given template.
 * 
 * @author Angelo ZERR
 *
 * @param <T> data model parameter.
 */
public class DataModelTemplate<T extends DataModelParameter> {

	private String templateUri;

	private String sourceType;

	private String sourceMethod;

	private String sourceField;
	
	private List<T> parameters;

	private transient Map<String, T> parametersMap;

	/**
	 * Returns the template Uri.
	 * 
	 * @return the template Uri.
	 */
	public String getTemplateUri() {
		return templateUri;
	}

	/**
	 * Set the template Uri.
	 * 
	 * @param templateUri the template Uri.
	 */
	public void setTemplateUri(String templateUri) {
		this.templateUri = templateUri;
	}

	/**
	 * Returns the Java source type where this data model template is defined.
	 * 
	 * @return the Java source type where this data model template is defined.
	 */
	public String getSourceType() {
		return sourceType;
	}

	/**
	 * Set the Java source type where this data model template is defined.
	 * 
	 * @param sourceType the Java source type where this data model template is
	 *                   defined.
	 */
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * Returns the Java source method where this data model template is defined and
	 * null otherwise.
	 * 
	 * @return the Java source method where this data model template is defined and
	 *         null otherwise.
	 */
	public String getSourceMethod() {
		return sourceMethod;
	}

	/**
	 * Set the Java source method where this data model template is defined and null
	 * otherwise.
	 * 
	 * @param sourceMethod the Java source method where this data model template is
	 *                     defined and null otherwise.
	 */
	public void setSourceMethod(String sourceMethod) {
		this.sourceMethod = sourceMethod;
	}

	/**
	 * Returns the Java source field where this data model template is defined and
	 * null otherwise.
	 * 
	 * @return the Java source field where this data model template is defined and
	 *         null otherwise.
	 */
	public String getSourceField() {
		return sourceField;
	}

	/**
	 * Set the Java source field where this data model template is defined and null
	 * otherwise.
	 * 
	 * @param sourceField the Java source field where this data model template is
	 *                    defined and null otherwise.
	 */
	public void setSourceField(String sourceField) {
		this.sourceField = sourceField;
	}

	/**
	 * Returns the list of data model parameters.
	 * 
	 * @return the list of data model parameters.
	 */
	public List<T> getParameters() {
		return parameters;
	}

	/**
	 * Set the list of data model parameters.
	 * 
	 * @param parameters the list of data model parameters.
	 */
	public void setParameters(List<T> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Returns the parameter from the given key and null otherwise.
	 * 
	 * @param key the parameter key.
	 * 
	 * @return the parameter from the given key and null otherwise.
	 */
	public T getParameter(String key) {
		List<T> parameters = getParameters();
		if (parameters == null) {
			return null;
		}
		return getParametersMap().get(key);
	}

	public void addParameter(T parameter) {
		if (parameters == null) {
			parameters = new ArrayList<>();
		}
		parameters.add(parameter);
		getParametersMap().put(parameter.getKey(), parameter);
	}

	private Map<String, T> getParametersMap() {
		if (parametersMap == null) {
			parametersMap = parameters.stream()
					.collect(Collectors.toMap(DataModelParameter::getKey, Function.identity()));
		}
		return parametersMap;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("templateUri", this.templateUri);
		b.add("sourceType", this.sourceType);
		b.add("sourceMethod", this.sourceMethod);
		b.add("sourceField", this.sourceField);
		b.add("parameters", this.parameters);
		return b.toString();
	}
}
