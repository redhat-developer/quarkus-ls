/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.codeactions;

import org.eclipse.lsp4j.Diagnostic;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.api.QuteTemplateGenerateMissingJavaMember;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.diagnostics.DiagnosticDataFactory;
import com.redhat.qute.services.diagnostics.JavaBaseTypeOfPartData;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Code action request.
 * 
 * @author Angelo ZERR
 *
 */
public class CodeActionRequest {

	private final Template template;

	private final Diagnostic diagnostic;

	private final QuteTemplateGenerateMissingJavaMember resolver;
	private final SharedSettings sharedSettings;

	private Node coveredNode;

	private ResolvedJavaTypeInfo resolvedType;

	public CodeActionRequest(Template template, Diagnostic diagnostic, QuteTemplateGenerateMissingJavaMember resolver,
			SharedSettings sharedSettings) {
		this.template = template;
		this.diagnostic = diagnostic;
		this.resolver = resolver;
		this.sharedSettings = sharedSettings;
	}

	public Template getTemplate() {
		return template;
	}

	public Diagnostic getDiagnostic() {
		return diagnostic;
	}

	public QuteTemplateGenerateMissingJavaMember getResolver() {
		return resolver;
	}

	public SharedSettings getSharedSettings() {
		return sharedSettings;
	}

	/**
	 * Returns the covered node by the diagnostic and null otherwise.
	 * 
	 * @return the covered node by the diagnostic and null otherwise.
	 * 
	 * @throws BadLocationException
	 */
	public Node getCoveredNode() throws BadLocationException {
		if (coveredNode == null) {
			int offset = template.offsetAt(diagnostic.getRange().getEnd());
			Node node = template.findNodeBefore(offset);
			if (node == null) {
				return null;
			}
			coveredNode = QutePositionUtility.findBestNode(offset, node);
		}
		return coveredNode;

	}

	/**
	 * Returns the Java base type of the covered node and null otherwise.
	 * 
	 * @param javaCache the Java data model cache.
	 * 
	 * @return the Java base type of the covered node and null otherwise.
	 */
	public ResolvedJavaTypeInfo getJavaTypeOfCoveredNode(JavaDataModelCache javaCache) {
		if (resolvedType == null) {
			JavaBaseTypeOfPartData data = DiagnosticDataFactory.getJavaBaseTypeOfPartData(diagnostic);
			if (data == null) {
				return null;
			}
			String signature = data.getSignature();
			String projectUri = template.getProjectUri();
			resolvedType = javaCache.resolveJavaType(signature, projectUri).getNow(null);
		}
		return resolvedType;
	}

}
