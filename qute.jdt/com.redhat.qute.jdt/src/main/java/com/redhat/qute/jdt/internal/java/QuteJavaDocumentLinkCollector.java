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
package com.redhat.qute.jdt.internal.java;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.TemplatePathInfo;

/**
 * Report document link for opening/creating Qute template for:
 * 
 * <ul>
 * <li>declared method which have class annotated with @CheckedTemplate.</li>
 * <li>declared field which have Template as type.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class QuteJavaDocumentLinkCollector extends AbstractQuteTemplateLinkCollector {

	private static final String QUTE_DOCUMENT_LINK_OPEN_URI_MESSAGE = "Open `{0}`";

	private static final String QUTE_DOCUMENT_LINK_GENERATE_TEMPLATE_MESSAGE = "Create `{0}`";

	private final List<DocumentLink> links;

	public QuteJavaDocumentLinkCollector(ITypeRoot typeRoot, List<DocumentLink> links, IJDTUtils utils,
			IProgressMonitor monitor) {
		super(typeRoot, utils, monitor);
		this.links = links;
	}

	@Override
	protected void collectTemplateLink(ASTNode fieldOrMethod, ASTNode locationAnnotation, TypeDeclaration type,
			String className, String fieldOrMethodName, String location, IFile templateFile,
			TemplatePathInfo templatePathInfo) throws JavaModelException {
		if (!templatePathInfo.isValid()) {
			// It is an empty fragment which is not valid, don't generate a document link.
			return;
		}
		String templateUri = templateFile.getLocationURI().toString();
		String tooltip = getTooltip(templateFile, templatePathInfo.getTemplateUri());
		Range range = createRange(locationAnnotation != null ? locationAnnotation : fieldOrMethod);
		DocumentLink link = new DocumentLink(range, templateUri, null, tooltip);
		links.add(link);
	}

	private static String getTooltip(IFile templateFile, String templateFilePath) {
		if (templateFile.exists()) {
			return MessageFormat.format(QUTE_DOCUMENT_LINK_OPEN_URI_MESSAGE, templateFilePath);
		}
		return MessageFormat.format(QUTE_DOCUMENT_LINK_GENERATE_TEMPLATE_MESSAGE, templateFilePath);
	}

}
