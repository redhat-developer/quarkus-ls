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
package com.redhat.qute.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionMetadata;
import com.redhat.qute.parser.template.Template;

/**
 * User tag utilities.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#user_tags
 *
 */
public class UserTagUtils {

	private static final Map<String, SectionMetadata> SPECIAL_KEYS;

	static {
		SPECIAL_KEYS = new HashMap<String, SectionMetadata>();
		register(new SectionMetadata("it", Object.class.getName(),
				"`it` is a special key that is replaced with the first unnamed parameter of the tag."));
		register(new SectionMetadata("nested-content", Object.class.getName(),
				"`nested-content` is a special key that will be replaced by the content of the tag"));
	}

	private static void register(SectionMetadata metadata) {
		SPECIAL_KEYS.put(metadata.getName(), metadata);
	}

	public static final String TAGS_DIR = "tags";

	public static boolean isUserTag(Template template) {
		String templateId = template.getTemplateId();
		return templateId != null && templateId.startsWith(TAGS_DIR);
	}

	/**
	 * Returns the template id of the given tag name.
	 * 
	 * @param tagName the tag name(ex : form)
	 * 
	 * @return the template id of the given tag name.
	 */
	public static String getTemplateId(String tagName) {
		return TAGS_DIR + '/' + tagName;
	}

	/**
	 * Returns the user tag name (ex : form) from the given uri (ex :
	 * file://C:/form.html, form.html).
	 * 
	 * @param uri the URI
	 * 
	 * @return the user tag name (ex : form) from the given uri (ex :
	 *         file://C:/form.html, form.html).
	 */
	public static String getUserTagName(String uri) {
		int index = uri.lastIndexOf('/');
		String fileName = index != -1 ? uri.substring(index + 1, uri.length()) : uri;
		return fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
	}

	/**
	 * Collect user tag parameters from the given template.
	 * 
	 * @param template      the Qute template.
	 * @param collector     the object part collector.
	 * @param cancelChecker the cancel checker.
	 */
	public static void collectUserTagParameters(Template template, Consumer<ObjectPart> collector,
			CancelChecker cancelChecker) {
		collectUserTagParameters(template, template, new HashSet<String>(), collector, cancelChecker);
	}

	private static void collectUserTagParameters(Node parent, Template template, Set<String> extistingObjectParts,
			Consumer<ObjectPart> collector, CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();
		if (parent.getKind() == NodeKind.Expression) {
			Expression expression = (Expression) parent;
			collectUserTagParameters(expression, extistingObjectParts, collector, cancelChecker);
			return;
		} else if (parent.getKind() == NodeKind.Section) {
			Section section = (Section) parent;
			List<Parameter> parameters = section.getParameters();
			for (Parameter parameter : parameters) {
				if (parameter.isCanHaveExpression()) {
					Expression expression = parameter.getJavaTypeExpression();
					collectUserTagParameters(expression, extistingObjectParts, collector, cancelChecker);
				}
			}
		}
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			collectUserTagParameters(node, template, extistingObjectParts, collector, cancelChecker);
		}
	}

	private static void collectUserTagParameters(Expression expression, Set<String> extistingObjectParts,
			Consumer<ObjectPart> collector, CancelChecker cancelChecker) {
		ObjectPart objectPart = expression.getObjectPart();
		if (objectPart != null && expression.getNamespacePart() == null) {
			String partName = objectPart.getPartName();
			if (!extistingObjectParts.contains(partName)) {
				extistingObjectParts.add(partName);
				collector.accept(objectPart);
			}
		}
	}

	/**
	 * Returns the special keys ('it' and 'nested-content') allowed in an user tag.
	 * 
	 * @return the special keys ('it' and 'nested-content') allowed in an user tag.
	 */
	public static Collection<SectionMetadata> getSpecialKeys() {
		return SPECIAL_KEYS.values();
	}

	/**
	 * Return the special key from the given object part name and null otherwise.
	 * 
	 * @param objectName the object part name.
	 * 
	 * @return the special key from the given object part name and null otherwise.
	 */
	public static SectionMetadata getSpecialKey(String objectName) {
		return SPECIAL_KEYS.get(objectName);
	}
}
