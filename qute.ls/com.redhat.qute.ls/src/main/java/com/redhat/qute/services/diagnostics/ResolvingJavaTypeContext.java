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
package com.redhat.qute.services.diagnostics;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import com.redhat.qute.parser.template.Template;

/**
 * Resolving java type context host completable future of Java type to resolved.
 * 
 * @author Angelo ZERR
 *
 */
public class ResolvingJavaTypeContext extends ArrayList<CompletableFuture<?>> {

	private static final long serialVersionUID = 1L;

	private boolean projectResolved;

	private boolean dataModelTemplateResolved;

	public ResolvingJavaTypeContext(Template template) {
		projectResolved = template.getProjectUri() != null;
		if (projectResolved) {
			// Get or load the data model template, data model defined
			//
			// 1) with @CheckedTemplate --> see
			// https://quarkus.io/guides/qute-reference#typesafe_templates
			//
			// 2) or of Template field --> see
			// https://quarkus.io/guides/qute-reference#resteasy_integration
			CompletableFuture<?> f = template.getDataModelTemplate();
			dataModelTemplateResolved = f.isDone();
			if (!dataModelTemplateResolved) {
				super.add(f);
			}
		}
	}

	/**
	 * Returns true if the Qute project has been resolved and false otherwise.
	 * 
	 * @return true if the Qute project has been resolved and false otherwise.
	 */
	public boolean isProjectResolved() {
		return projectResolved;
	}

	/**
	 * Returns true if the data model template is resolved and false otherwise.
	 * 
	 * @return true if the data model template is resolved and false otherwise.
	 */
	public boolean isDataModelTemplateResolved() {
		return dataModelTemplateResolved;
	}
	
	@Override
	public boolean add(CompletableFuture<?> e) {
		if (super.contains(e)) {
			return true;
		}
		return super.add(e);
	}
}
