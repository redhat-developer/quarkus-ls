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
import org.eclipse.lsp4j.Position;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.api.QuteTemplateJavaTextEditProvider;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.services.AbstractPositionRequest;
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
public class CodeActionRequest extends AbstractPositionRequest {

	private final Template template;

	private final Diagnostic diagnostic;

	private final QuteTemplateJavaTextEditProvider javaTextEditProvider;
	private final SharedSettings sharedSettings;

	private Node coveredNode;

	private ResolvedJavaTypeInfo resolvedType;

	public CodeActionRequest(Template template, Position position, Diagnostic diagnostic,
			QuteTemplateJavaTextEditProvider javaTextEditProvider,
			SharedSettings sharedSettings) throws BadLocationException {
		super(template, position);
		this.template = template;
		this.diagnostic = diagnostic;
		this.javaTextEditProvider = javaTextEditProvider;
		this.sharedSettings = sharedSettings;
	}

	public Template getTemplate() {
		return template;
	}

	public Diagnostic getDiagnostic() {
		return diagnostic;
	}

	public QuteTemplateJavaTextEditProvider getTextEditProvider() {
		return javaTextEditProvider;
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
	 * @return the Java base type of the covered node and null otherwise.
	 */
	public ResolvedJavaTypeInfo getJavaTypeOfCoveredNode() {
		if (resolvedType == null) {
			JavaBaseTypeOfPartData data = DiagnosticDataFactory.getJavaBaseTypeOfPartData(diagnostic);
			if (data == null) {
				return null;
			}
			QuteProject project = template.getProject();
			if (project == null) {
				return null;
			}
			String signature = data.getSignature();
			resolvedType = project.resolveJavaTypeSync(signature);
		}
		return resolvedType;
	}

	@Override
	protected Node doFindNodeAt(Template template, int offset) {
		Node node = template.findNodeBefore(offset);
		if (node == null) {
			return null;
		}
		Node coveredNodeAtOffset = QutePositionUtility.findBestNode(offset, node);
		return coveredNodeAtOffset;
	}

}
