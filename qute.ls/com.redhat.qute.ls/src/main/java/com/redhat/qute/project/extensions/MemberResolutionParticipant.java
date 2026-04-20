/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions;

import java.util.List;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.template.Template;

/**
 * Participant for providing additional types for member resolution.
 *
 * <p>
 * This allows extensions to augment type resolution by providing additional
 * types where members should be searched. This is useful for contextual
 * augmentation scenarios, such as:
 * </p>
 * <ul>
 * <li>Adding YAML front matter properties to a base type (e.g.,
 * {@code page.data.social} where {@code social} is defined in YAML)</li>
 * <li>Providing dynamically computed properties based on context</li>
 * </ul>
 *
 * <h3>Example:</h3>
 * <p>
 * For Roq, when resolving {@code {page.data.social}}:
 * </p>
 * <ol>
 * <li>Standard resolution finds {@code data} property on {@code NormalPage},
 * returns {@code JsonObject}</li>
 * <li>{@code getAdditionalTypes} is called for {@code social} on
 * {@code JsonObject}</li>
 * <li>RoqProjectExtension analyzes the context (YAML front matter) and returns
 * types with YAML properties</li>
 * <li>Member {@code social} is found in the additional types</li>
 * </ol>
 *
 * @author Angelo ZERR
 */
public interface MemberResolutionParticipant extends BaseParticpant {

	/**
	 * Returns additional types where members should be searched.
	 *
	 * <p>
	 * This method is called when standard member resolution (Java reflection +
	 * value resolvers) fails to find a member. Extensions can analyze the context
	 * and provide additional types to search.
	 * </p>
	 *
	 * <h3>Parameters:</h3>
	 * <ul>
	 * <li>{@code baseType} - The resolved type of {@code previousPart} (e.g., JsonObject for page.data)</li>
	 * <li>{@code previousPart} - The part before the one being resolved (e.g., "data" in page.data.layout)</li>
	 * <li>{@code part} - The part being resolved (may be null during completion, e.g., page.data.|)</li>
	 * <li>{@code template} - The template being analyzed</li>
	 * </ul>
	 *
	 * <h3>Examples:</h3>
	 * <pre>
	 * {page.data.|}       → baseType=JsonObject, previousPart=data, part=null, template=...
	 * {page.data.layout}  → baseType=JsonObject, previousPart=data, part=layout, template=...
	 * </pre>
	 *
	 * @param baseType the base type where the member is being searched
	 * @param previousPart the previous part in the expression chain (never null)
	 * @param part the part representing the member being resolved (may be null during completion)
	 * @param template the template being analyzed (may be null in some contexts)
	 * @return list of additional types to search, or null if no additional types
	 */
	List<ResolvedJavaTypeInfo> getAdditionalTypes(ResolvedJavaTypeInfo baseType, Part previousPart, Part part, Template template);

}
