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

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Data model template host informations about the expected data model
 * (parameters) for a given template.
 * 
 * @param <T> data model parameter.
 * 
 * @author Angelo ZERR
 */
public class DataModelTemplate<T extends DataModelParameter> extends DataModelBaseTemplate<T> {

	private String templateUri;

	private String sourceField;

	private List<DataModelFragment<T>> fragments;

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
	 * Add the given fragment.
	 * 
	 * @param fragment the fragment to add.
	 */
	public void addFragment(DataModelFragment<T> fragment) {
		if (fragments == null) {
			fragments = new ArrayList<>();
		}
		fragments.add(fragment);
	}

	/**
	 * Returns list of fragments and null otherwise.
	 * 
	 * @return list of fragments and null otherwise.
	 */
	public List<DataModelFragment<T>> getFragments() {
		return fragments;
	}

	/**
	 * Set the fragment list.
	 * 
	 * @param fragments the fragment list.
	 */
	public void setFragments(List<DataModelFragment<T>> fragments) {
		this.fragments = fragments;
	}

	/**
	 * Returns the fragment identified by the given id <code>fragmentId</code> and
	 * null otherwise.
	 * 
	 * @param fragmentId the fragment id.
	 * 
	 * @return the fragment identified by the given id <code>fragmentId</code> and
	 *         null otherwise.
	 */
	public DataModelFragment<T> getFragment(String fragmentId) {
		if (fragmentId == null) {
			return null;
		}
		List<DataModelFragment<T>> fragments = getFragments();
		if (fragments == null || fragments.isEmpty()) {
			return null;
		}
		for (DataModelFragment<T> fragment : fragments) {
			if (fragmentId.equals(fragment.getId())) {
				return fragment;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("templateUri", this.templateUri);
		b.add("sourceType", this.getSourceType());
		b.add("sourceMethod", this.getSourceMethod());
		b.add("sourceField", this.sourceField);
		b.add("parameters", this.getParameters());
		b.add("fragments", this.getFragments());
		return b.toString();
	}
}
