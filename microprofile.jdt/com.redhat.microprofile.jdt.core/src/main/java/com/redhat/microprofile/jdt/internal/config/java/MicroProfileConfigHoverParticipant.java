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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
		Map<String, String> matchingKeys = getKeyValueMapWithProfiles(propertyKey, annotation, mpProject);
		return new Hover(getDocumentation(matchingKeys, context.getDocumentFormat(), context.isSurroundEqualsWithSpaces()), propertyKeyRange);
	}
	
	
	/**
	 * Returns a map with property keys and values, where keys include <code>propertyKey</code>
	 * and its value. The map also includes <code>propertyKey</code> with profiles and its
	 * respective values.
	 * 
	 * For example if <code>propertyKey</code> is "greeting.message", here
	 * is a possible resulting map:
	 * 
	 * "greeting.message" -> "hello"
	 * "%dev.greeting.message"-> "hi"
	 * "%prod.greeting.message"-> "good morning"
	 * 
	 * @param propertyKey the property key
	 * @param annotation  the annotation
	 * @param project     the project
	 * @return
	 * @throws JavaModelException
	 */
	public static Map<String, String> getKeyValueMapWithProfiles(String propertyKey, IAnnotation annotation,
			JDTMicroProfileProject project) throws JavaModelException {
		Map<String, String> result = new HashMap<String, String>();
		String propertyValue = project.getProperty(propertyKey, null);
		if (propertyValue == null) {
			propertyValue = getAnnotationMemberValue(annotation, CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE);
			if (propertyValue != null && propertyValue.length() == 0) {
				propertyValue = null;
			}
		}
		
		result.put(propertyKey, propertyValue);
		
		Set<String> matchingKeysWithProfile = new HashSet<String>();
		Set<String> allKeys = project.getPropertyKeys();
		for (String key: allKeys) {
			if (!key.equals(propertyKey) && key.charAt(0) == '%' && key.endsWith(propertyKey)) {
				//check if key is keyToMatch with profile
				String beforeMatch = key.substring(0, key.indexOf(propertyKey) - 1);
				if (beforeMatch.indexOf('.') == -1) {
					matchingKeysWithProfile.add(key);
				}
			}
		}
		
		for (String key: matchingKeysWithProfile.stream().sorted().collect(Collectors.toList())) {
			String value = project.getProperty(key, null);
			if (value != null) {
				result.put(key, value);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns documentation about the property keys and values provided
	 * in <code>propertyMap</code>
	 * 
	 * @param propertyMap    the map containing property keys and values
	 * @param documentFormat the document format
	 * @param insertSpacing  true if spacing should be inserted around the equals
	 *                       sign and false otherwise
	 *
	 * @return documentation about the property keys and values provided
	 * in <code>propertyMap</code>
	 */
	public static MarkupContent getDocumentation(Map<String, String> propertyMap,
			DocumentFormat documentFormat, boolean insertSpacing) {
		StringBuilder content = new StringBuilder();
		boolean markdown = DocumentFormat.Markdown.equals(documentFormat);
		buildDocumentation(propertyMap, markdown, insertSpacing, content);
		return new MarkupContent(markdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, content.toString());
	}
	
	private static void buildDocumentation(Map<String, String> propertyMap,
			boolean markdownSupported, boolean insertSpacing, StringBuilder content) {
		
		for (Map.Entry<String, String> property : propertyMap.entrySet()) {

			if (content.length() > 0) {
				content.append("  \n");
			}
			
			if (markdownSupported) {
				content.append("`");
			}
			
			
			content.append(property.getKey());

			if (property.getValue() == null) {
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
				content.append(property.getValue());
				if (markdownSupported) {
					content.append("`");
				}
			}
		}
	}
}
