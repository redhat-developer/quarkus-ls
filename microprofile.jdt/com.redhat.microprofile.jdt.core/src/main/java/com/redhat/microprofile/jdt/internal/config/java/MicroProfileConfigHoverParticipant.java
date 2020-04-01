/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.config.java;

import static com.redhat.microprofile.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION;
import static com.redhat.microprofile.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE;
import static com.redhat.microprofile.jdt.core.MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_NAME;
import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.getAnnotation;
import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.util.Ranges;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.jdt.core.java.hover.IJavaHoverParticipant;
import com.redhat.microprofile.jdt.core.java.hover.JavaHoverContext;
import com.redhat.microprofile.jdt.core.project.JDTMicroProfileProject;
import com.redhat.microprofile.jdt.core.project.JDTMicroProfileProjectManager;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.core.utils.JDTTypeUtils;

/**
 *
 * MicroProfile Config Hover
 * 
 * @author Angelo ZERR
 * 
 * @See https://github.com/eclipse/microprofile-config
 *
 */
public class MicroProfileConfigHoverParticipant implements IJavaHoverParticipant {

	@Override
	public boolean isAdaptedForHover(JavaHoverContext context, IProgressMonitor monitor) throws JavaModelException {
		// Hover is done only if microprofile-config is on the classpath
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, CONFIG_PROPERTY_ANNOTATION) != null;
	}

	@Override
	public Hover collectHover(JavaHoverContext context, IProgressMonitor monitor) throws CoreException {
		IJavaElement hoverElement = context.getHoverElement();
		if (hoverElement.getElementType() != IJavaElement.FIELD && hoverElement.getElementType() != IJavaElement.LOCAL_VARIABLE) {
			return null;
		}

		ITypeRoot typeRoot = context.getTypeRoot();
		IJDTUtils utils = context.getUtils();

		Position hoverPosition = context.getHoverPosition();
		IAnnotatable hoverField = (IAnnotatable) hoverElement;

		IAnnotation annotation = getAnnotation(hoverField, CONFIG_PROPERTY_ANNOTATION);

		if (annotation == null) {
			return null;
		}

		String annotationSource = ((ISourceReference) annotation).getSource();
		String propertyKey = getAnnotationMemberValue(annotation, CONFIG_PROPERTY_ANNOTATION_NAME);

		if (propertyKey == null) {
			return null;
		}

		ISourceRange r = ((ISourceReference) annotation).getSourceRange();
		int offset = annotationSource.indexOf(propertyKey);
		final Range propertyKeyRange = utils.toRange(typeRoot, r.getOffset() + offset, propertyKey.length());

		if (hoverPosition.equals(propertyKeyRange.getEnd())
				|| !Ranges.containsPosition(propertyKeyRange, hoverPosition)) {
			return null;
		}

		IJavaProject javaProject = typeRoot.getJavaProject();

		if (javaProject == null) {
			return null;
		}

		JDTMicroProfileProject mpProject = JDTMicroProfileProjectManager.getInstance().getJDTMicroProfileProject(javaProject);
		Set<String> matchingKeys =getMatchingKeys(propertyKey, mpProject);
		
		MarkupContent documentation = null;
		if (matchingKeys.size() == 0) {
			String propertyValue = getAnnotationMemberValue(annotation, CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE);
			if (propertyValue != null && propertyValue.length() == 0) {
				propertyValue = null;
			}
			documentation = getDocumentation(propertyKey, propertyValue,
					context.getDocumentFormat(), context.isSurroundEqualsWithSpaces());
		} else {
			documentation = getDocumentation(matchingKeys, mpProject,
					context.getDocumentFormat(), context.isSurroundEqualsWithSpaces());
		}
		return new Hover(documentation, propertyKeyRange);
	}
	
	public static Set<String> getMatchingKeys(String keyToMatch, JDTMicroProfileProject project) {
		Set<String> result = new HashSet<String>();
		Set<String> allKeys = project.getPropertyKeys();
		for (String key: allKeys) {
			if (key.endsWith(keyToMatch)) {
				result.add(key);
			}
		}
		return result;
	}
	
	/**
	 * Returns documentation about the provided keys in <code>propertyKeys<code>
	 * 
	 * @param propertyKeys   the property keys to create documentation for
	 * @param project        the project
	 * @param documentFormat the document format
	 * @param insertSpacing  true if spacing should be inserted around the equals
	 *                       sign and false otherwise
	 * @return documentation about the provided keys in <code>propertyKeys<code>
	 * @throws JavaModelException
	 */
	public static MarkupContent getDocumentation(Set<String> propertyKeys, JDTMicroProfileProject project,
			DocumentFormat documentFormat, boolean insertSpacing) throws JavaModelException {
		StringBuilder content = new StringBuilder();
		boolean markdown = DocumentFormat.Markdown.equals(documentFormat);
		
		List<String> sortedKeys = propertyKeys.stream().sorted((o1, o2)->{
			if (o1.charAt(0) != '%') return -1;
			if (o2.charAt(0) != '%') return 1;
			return o1.compareTo(o2);
		}).collect(Collectors.toList());
		for (String key: sortedKeys) {
			String value = project.getProperty(key, null);
			if (value != null) {
				buildDocumentation(key, value, markdown, insertSpacing, content);
			}
		}
		
		return new MarkupContent(markdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, content.toString());
		
	}
	
	/**
	 * Returns documentation about the provided <code>propertyKey</code>'s value,
	 * <code>propertyValue</code>
	 * 
	 * @param propertyKey    the property key
	 * @param propertyValue  the property key's value
	 * @param documentFormat the document format
	 * @param insertSpacing  true if spacing should be inserted around the equals
	 *                       sign and false otherwise
	 * @return
	 */
	public static MarkupContent getDocumentation(String propertyKey, String propertyValue,
			DocumentFormat documentFormat, boolean insertSpacing) {
		StringBuilder content = new StringBuilder();
		boolean markdown = DocumentFormat.Markdown.equals(documentFormat);
		buildDocumentation(propertyKey, propertyValue, markdown, insertSpacing, content);
		return new MarkupContent(markdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, content.toString());
	}
	
	private static void buildDocumentation(String propertyKey,
			String propertyValue, boolean markdownSupported, boolean insertSpacing, StringBuilder content) {
		
		if (content.length() > 0) {
			content.append("  \n");
		}
		
		if (markdownSupported) {
			content.append("`");
		}

		content.append(propertyKey);

		if (propertyValue == null) {
			if (markdownSupported) {
				content.append("`");
			}
			content.append(" is not set.");
		} else {
			if (insertSpacing) {
				content.append(" = ");
			} else {
				content.append("=");
			}
			content.append(propertyValue);
			if (markdownSupported) {
				content.append("`");
			}
		}
	}
}
