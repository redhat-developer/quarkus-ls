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
package com.redhat.qute.parser.template;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.project.datamodel.ExtendedDataModelParameter;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;

public class Template extends Node {

	private String projectUri;

	private final TextDocument textDocument;

	private CancelChecker cancelChecker;

	private QuteProjectRegistry projectRegistry;

	private String templateId;

	Template(TextDocument textDocument) {
		super(0, textDocument.getText().length());
		this.textDocument = textDocument;
		super.setClosed(true);
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Template;
	}

	public String getNodeName() {
		return "#template";
	}

	@Override
	public Template getOwnerTemplate() {
		return this;
	}

	public void setCancelChecker(CancelChecker cancelChecker) {
		this.cancelChecker = cancelChecker;
	}

	public CancelChecker getCancelChecker() {
		return cancelChecker;
	}

	public Position positionAt(int offset) throws BadLocationException {
		checkCanceled();
		return textDocument.positionAt(offset);
	}

	public int offsetAt(Position position) throws BadLocationException {
		checkCanceled();
		return textDocument.offsetAt(position);
	}

	public String lineText(int lineNumber) throws BadLocationException {
		checkCanceled();
		return textDocument.lineText(lineNumber);
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String lineDelimiter(int lineNumber) throws BadLocationException {
		checkCanceled();
		return textDocument.lineDelimiter(lineNumber);
	}

	public void checkCanceled() {
		if (cancelChecker != null) {
			cancelChecker.checkCanceled();
		}
	}

	public String getUri() {
		return textDocument.getUri();
	}

	public String getText() {
		return textDocument.getText();
	}

	public TextDocument getTextDocument() {
		return textDocument;
	}

	public String getText(RangeOffset range) {
		return getText(range.getStart(), range.getEnd());
	}

	public String getText(int start, int end) {
		String text = getText();
		return text.substring(start, end);
	}

	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	public String getProjectUri() {
		return projectUri;
	}

	public Path getTemplateBaseDir() {
		QuteProject project = getProject();
		if (project == null) {
			return null;
		}
		return project.getTemplateBaseDir();
	}

	public QuteProject getProject() {
		return projectRegistry != null ? projectRegistry.getProject(projectUri) : null;
	}

	public void setProjectRegistry(QuteProjectRegistry projectRegistry) {
		this.projectRegistry = projectRegistry;
	}

	/**
	 * Try to find the class name
	 * <ul>
	 * <li>- from parameter declaration.</li>
	 * <li>- from @CheckedTemplate.</li>
	 * </ul>
	 * 
	 * @param partName
	 * @return
	 */
	public JavaTypeInfoProvider findInInitialDataModel(Part part) {
		if (part.getPartKind() == PartKind.Object) {
			String partName = part.getPartName();
			// Try to find the class name from parameter declaration
			JavaTypeInfoProvider parameter = findInParameterDeclarationByAlias(partName);
			if (parameter != null) {
				return parameter;
			}
			// Try to find the class name from @CheckedTemplate
			return getParameterDataModel(partName).getNow(null);
		}
		return null;
	}

	public ParameterDeclaration findInParameterDeclarationByAlias(String alias) {
		Optional<ParameterDeclaration> result = super.getChildren().stream() //
				.filter(n -> n.getKind() == NodeKind.ParameterDeclaration) //
				.filter(parameter -> alias.equals(((ParameterDeclaration) parameter).getAlias())) //
				.map(n -> ((ParameterDeclaration) n)) //
				.findFirst();
		if (result.isPresent()) {
			return result.get();
		}
		return null;
	}

	public CompletableFuture<ExtendedDataModelParameter> getParameterDataModel(String parameterName) {
		return getDataModelTemplate(). //
				thenApply(dataModel -> {
					return dataModel != null ? dataModel.getParameter(parameterName) : null;
				});
	}

	public CompletableFuture<ExtendedDataModelTemplate> getDataModelTemplate() {
		if (projectRegistry == null || getProjectUri() == null) {
			return CompletableFuture.completedFuture(null);
		}
		return projectRegistry.getDataModelTemplate(this);
	}
}
