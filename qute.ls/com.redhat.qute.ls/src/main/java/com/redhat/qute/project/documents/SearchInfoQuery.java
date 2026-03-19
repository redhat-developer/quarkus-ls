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
package com.redhat.qute.project.documents;

/**
 * Search query to collect template informations:
 * 
 * <ul>
 * <li>insert parameters declared in a Qute template</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class SearchInfoQuery {

	public static final String ALL = "@all";

	private String insertParameter;

	private String sectionTag;

	private String fragmentId;

	/**
	 * Set the insert parameter name to search in the Qute template where search is
	 * done.
	 * 
	 * If insert parameter is set with {@link SearchInfoQuery#ALL} it will collect
	 * all insert parameters declared in the template.
	 * 
	 * @param insertParameter the insert parameter name to find.
	 */
	public void setInsertParameter(String insertParameter) {
		this.insertParameter = insertParameter;
	}

	/**
	 * Returns the insert parameter name to search in the Qute template where search
	 * is done.
	 * 
	 * @return the insert parameter name to search in the Qute template where search
	 *         is done.
	 */
	public String getInsertParameter() {
		return insertParameter;
	}

	/**
	 * Returns the custom section tag name to search in the Qute template where
	 * search is done.
	 * 
	 * @return the custom section tag name to search in the Qute template where
	 *         search is done.
	 */
	public String getSectionTag() {
		return sectionTag;
	}

	/**
	 * Set the custom section tag name to search in the Qute template where search
	 * is done.
	 * 
	 * If section tag is set with {@link SearchInfoQuery#ALL} it will collect all
	 * custom sections declared in the template.
	 * 
	 * @param sectionTag the custom section tag name to find.
	 */
	public void setSectionTag(String sectionTag) {
		this.sectionTag = sectionTag;
	}

	/**
	 * Returns the fragment id to search in the Qute template where search is done.
	 * 
	 * @return the fragment id to search in the Qute template where search is done.
	 */
	public String getFragmentId() {
		return fragmentId;
	}

	/**
	 * Set the fragment id to search in the Qute template where search is done.
	 * 
	 * If fragment id is set with {@link SearchInfoQuery#ALL} it will collect all
	 * fragments declared in the template.
	 * 
	 * @param sectionTag the custom section tag name to find.
	 */
	public void setFragmentId(String fragmentId) {
		this.fragmentId = fragmentId;
	}
}