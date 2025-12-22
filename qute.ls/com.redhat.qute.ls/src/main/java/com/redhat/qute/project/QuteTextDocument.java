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
package com.redhat.qute.project;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.documents.SearchInfoQuery;

/**
 * Qute text document of a project which are opend and closed.
 *
 * @author Angelo ZERR
 *
 */
public interface QuteTextDocument {

	/**
	 * Returns the current parsed template.
	 *
	 * @return the current parsed template
	 */
	Template getTemplate();

	/**
	 * Returns the owner project information of the template.
	 *
	 * @return the owner project information of the template.
	 */
	CompletableFuture<ProjectInfo> getProjectInfoFuture();

	/**
	 * Returns the owner project of the template.
	 *
	 * @return the owner project of the template.
	 */
	QuteProject getProject();

	/**
	 * Returns the template id.
	 *
	 * @return the template id.
	 */
	String getTemplateId();

	/**
	 * Returns the insert parameter list with the given name
	 * <code>insertParamater</code> ({#insert name}) declared in the template.
	 * 
	 * @param insertParamater the name of the insert parameter. If the name is
	 *                        equals to {@link SearchInfoQuery#ALL}, the methods
	 *                        will returns all declared insert parameters.
	 * 
	 * @return the insert parameter list with the given name
	 *         <code>insertParamater</code> ({#insert name}) declared in the
	 *         template.
	 */
	List<Parameter> findInsertTagParameter(String insertParameter);

	/**
	 * Returns list of section with the given section tag name declared in all
	 * templates of the project and an empty list otherwise.
	 * 
	 * @param tag the section tag name.
	 * 
	 * @return list of section with the given section tag name declared in all
	 *         templates of the project and an empty list otherwise.
	 */
	List<Section> findSectionsByTag(String tag);

	/**
	 * Returns the file template uri.
	 * 
	 * @return the file template uri.
	 */
	String getUri();

	/**
	 * Returns true if the document is opened and false otherwise.
	 * 
	 * @return true if the document is opened and false otherwise.
	 */
	boolean isOpened();

	/**
	 * Returns true if the document is an user tag and false otherwise.
	 * 
	 * @return true if the document is an user tag and false otherwise.
	 */
	default boolean isUserTag() {
		String templateId = getTemplateId();
		return templateId != null && templateId.startsWith("tags/");
	}

	default void save() {
		
	}
}