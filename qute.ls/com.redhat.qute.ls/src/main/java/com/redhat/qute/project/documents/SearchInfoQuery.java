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

	public void setSectionTag(String sectionTag) {
		this.sectionTag = sectionTag;
	}
	
	public String getSectionTag() {
		return sectionTag;
	}
}