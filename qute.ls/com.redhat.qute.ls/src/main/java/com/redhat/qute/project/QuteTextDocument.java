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

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.parser.injection.InjectionDetector;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.CustomSection;
import com.redhat.qute.parser.template.sections.FragmentSection;
import com.redhat.qute.project.documents.SearchInfoQuery;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.project.usages.UsagesRegistry;

/**
 * Qute text document of a project which are opend and closed.
 *
 * @author Angelo ZERR
 *
 */
public interface QuteTextDocument {

	public static class Key<T> {

		private final String name;

		public Key(String name) {
			this.name = name;

		}

		public static <T> Key<T> create(String name) {
			return new Key<>(name);
		}
	}

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
	List<CustomSection> findCustomSectionsByTag(String tag);

	/**
	 * Returns list of fragment section with the given fragment id declared in all
	 * templates of the project and an empty list otherwise.
	 * 
	 * @param fragmentId the fragment id.
	 * 
	 * @return list of fragment section with the given fragment id declared in all
	 *         templates of the project and an empty list otherwise.
	 */
	List<FragmentSection> findFragmentSectionById(String fragmentId);

	/**
	 * Returns the file template uri.
	 * 
	 * @return the file template uri.
	 */
	String getUri();

	Path getTemplatePath();

	/**
	 * Returns true if the document is opened and false otherwise.
	 * 
	 * @return true if the document is opened and false otherwise.
	 */
	boolean isOpened();

	default boolean isBinary() {
		return false;
	}

	/**
	 * Returns true if the document is an user tag and false otherwise.
	 * 
	 * @return true if the document is an user tag and false otherwise.
	 */
	default boolean isUserTag() {
		String templateId = getTemplateId();
		return templateId != null && templateId.startsWith(getUserTagsFolder());
	}

	default void save() {

	}

	Collection<InjectionDetector> getInjectionDetectors();

	UserTag getUserTag();

	default String getUserTagName() {
		if (!isUserTag()) {
			return null;
		}
		String templateId = getTemplateId();
		if (templateId != null) {
			QuteProject project = getProject();
			TemplateRootPath rootPath = project != null ? project.findTemplateRootPathFor(getTemplatePath()) : null;
			boolean namespacedTagSupported = rootPath != null ? rootPath.isNamespacedTagSupported() : true;
			String tagName = null;
			if (namespacedTagSupported) {
				// ex : {#roq-default/hero
				tagName = templateId.substring(getUserTagsFolder().length());
			} else {
				// ex : {#bundle
				tagName = getTemplatePath().getFileName().toString();
			}
			int index = tagName.lastIndexOf('.');
			if (index != -1) {
				return tagName.substring(0, index);
			}
			return tagName;
		}
		return null;
	}

	default String getUserTagsFolder() {
		return "tags/";
	}

	String getOrigin();

	String getRelativePath();

	default String getProperty(String name) {
		return null;
	}

	TemplateRootPath getTemplateRootPath();

	/**
	 * Re-parse template from the current template content and update usages
	 * {@link UsagesRegistry} (call of user tag parameters, include parameters).
	 */
	default void reparseTemplate() {
		// Do nothing
	}

	Character getExpressionCommand();

	<T> T getUserData(Key<T> key);

	<T> void putUserData(Key<T> key, T data);
}