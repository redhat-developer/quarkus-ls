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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.LineIndentInfo;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.expression.NamespacePart;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.ParameterDeclaration.JavaTypeRangeOffset;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.project.TemplateInfoProvider;
import com.redhat.qute.project.datamodel.ExtendedDataModelParameter;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;

public class Template extends Node {

	private String projectUri;

	private final TextDocument textDocument;

	private CancelChecker cancelChecker;

	private QuteProjectRegistry projectRegistry;

	private String templateId;

	private TemplateInfoProvider templateInfoProvider;

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

	public LineIndentInfo lineIndentInfo(int lineNumber) throws BadLocationException {
		checkCanceled();
		return textDocument.lineIndentInfo(lineNumber);
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
	 * Returns the class name found from:
	 * 
	 * <ul>
	 * <li>- from parameter declaration.</li>
	 * <li>- from @CheckedTemplate.</li>
	 * </ul>
	 * 
	 * @param partName the part name.
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

	/**
	 * Returns the template configuration.
	 * 
	 * @return the template configuration.
	 */
	public TemplateConfiguration getConfiguration() {
		TemplateConfiguration configuration = null;
		QuteProject project = getProject();
		if (project != null) {
			configuration = project.getTemplateConfiguration();
		}
		return configuration != null ? configuration : TemplateConfiguration.DEFAULT;
	}

	/**
	 * Returns the class name found from the namespace and null otherwise.
	 * 
	 * Ex : {data:item}, {inject:bean}
	 * 
	 * @param objectPart the object part which have a namespace.
	 * 
	 * @return the class name found from the namespace and null otherwise.
	 */
	public JavaTypeInfoProvider findWithNamespace(ObjectPart objectPart) {
		String namespace = objectPart.getNamespace();
		if (namespace == null) {
			return null;
		}
		if (NamespacePart.DATA_NAMESPACE.equals(namespace)) {
			// {data:item}
			// Try to find the class name
			// - from parameter declaration
			// - from @CheckedTemplate
			return findInInitialDataModel(objectPart);
		}
		if (projectRegistry == null || getProjectUri() == null) {
			return null;
		}
		// {inject:bean}
		return (JavaTypeInfoProvider) projectRegistry
				.findJavaElementWithNamespace(namespace, objectPart.getPartName(), getProjectUri()).getNow(null);
	}
	
	public Set<String> getJavaTypesSupportedInNativeMode() {
		Set<String> javaTypesSupportedInNativeMode = new HashSet<>();
		// From parameter declaration
		for (Node node : super.getChildren()) {
			if (node.getKind() == NodeKind.ParameterDeclaration) {
				ParameterDeclaration parameter = (ParameterDeclaration) node;
				List<JavaTypeRangeOffset> classNameRanges = parameter.getJavaTypeNameRanges();
				for (RangeOffset classNameRange : classNameRanges) {
					String className = this.getText(classNameRange);
					javaTypesSupportedInNativeMode.add(className);
				}
			}
		} 	
		return javaTypesSupportedInNativeMode;
	}

	public JavaTypeInfoProvider findGlobalVariables(ObjectPart objectPart) {
		String namespace = objectPart.getNamespace();
		if (namespace != null) {
			return null;
		}
		if (projectRegistry == null || getProjectUri() == null) {
			return null;
		}
		// {inject:bean}
		return (JavaTypeInfoProvider) projectRegistry
				.findGlobalVariableJavaElement(objectPart.getPartName(), getProjectUri()).getNow(null);
	}
	
	@Override
	protected void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor, getChildren());
		}
		visitor.endVisit(this);
	}

	public void setTemplateInfoProvider(TemplateInfoProvider templateInfoProvider) {
		this.templateInfoProvider = templateInfoProvider;
	}

	public CompletableFuture<ProjectInfo> getProjectFuture() {
		if (templateInfoProvider == null) {
			return CompletableFuture.completedFuture(null);
		}
		return templateInfoProvider.getProjectInfoFuture();
	}
}
