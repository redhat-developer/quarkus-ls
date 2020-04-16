/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.ls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.ProjectLabelInfoEntry;
import com.redhat.microprofile.ls.commons.snippets.ISnippetContext;
import com.redhat.microprofile.ls.commons.snippets.Snippet;
import com.redhat.microprofile.ls.commons.snippets.SnippetRegistry;
import com.redhat.microprofile.snippets.SnippetContextForJava;

/**
 * test for Java snippet registry.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaTextDocumentSnippetRegistryTest {

	private static JavaTextDocumentSnippetRegistry registry = new JavaTextDocumentSnippetRegistry();

	@Test
	public void haveJavaSnippets() {
		Assert.assertFalse("Tests has MicroProfile Java snippets", registry.getSnippets().isEmpty());
	}

	@Test
	public void mpMetricsSnippets() {
		Optional<Snippet> metricSnippet = findByPrefix("@Metric", registry);
		Assert.assertTrue("Tests has @Metric Java snippets", metricSnippet.isPresent());

		ISnippetContext<?> context = metricSnippet.get().getContext();
		Assert.assertNotNull("@Metric snippet has context", context);
		Assert.assertTrue("@Metric snippet context is Java context", context instanceof SnippetContextForJava);

		ProjectLabelInfoEntry projectInfo = new ProjectLabelInfoEntry("", new ArrayList<>());
		boolean match = ((SnippetContextForJava) context).isMatch(projectInfo);
		Assert.assertFalse("Project has no org.eclipse.microprofile.metrics.annotation.Metric type", match);

		ProjectLabelInfoEntry projectInfo2 = new ProjectLabelInfoEntry("",
				Arrays.asList("org.eclipse.microprofile.metrics.annotation.Metric"));
		boolean match2 = ((SnippetContextForJava) context).isMatch(projectInfo2);
		Assert.assertTrue("Project has org.eclipse.microprofile.metrics.annotation.Metric type", match2);
	}

	@Test
	public void mpOpenAPISnippets() {
		Optional<Snippet> apiResponseSnippet = findByPrefix("@APIResponse", registry);
		Assert.assertTrue("Tests has @APIResponse Java snippets", apiResponseSnippet.isPresent());

		ISnippetContext<?> context = apiResponseSnippet.get().getContext();
		Assert.assertNotNull("@APIResponse snippet has context", context);
		Assert.assertTrue("@APIResponse snippet context is Java context", context instanceof SnippetContextForJava);

		ProjectLabelInfoEntry projectInfo = new ProjectLabelInfoEntry("", new ArrayList<>());
		boolean match = ((SnippetContextForJava) context).isMatch(projectInfo);
		Assert.assertFalse("Project has no org.eclipse.microprofile.openapi.annotations.responses.APIResponse type", match);

		ProjectLabelInfoEntry projectInfo2 = new ProjectLabelInfoEntry("",
				Arrays.asList("org.eclipse.microprofile.openapi.annotations.responses.APIResponse"));
		boolean match2 = ((SnippetContextForJava) context).isMatch(projectInfo2);
		Assert.assertTrue("Project has org.eclipse.microprofile.openapi.annotations.responses.APIResponse type", match2);
	}

	@Test
	public void mpFaultToleranceSnippets() {
		Optional<Snippet> fallbackSnippet = findByPrefix("@Fallback", registry);
		Assert.assertTrue("Tests has @Fallback Java snippets", fallbackSnippet.isPresent());

		ISnippetContext<?> context = fallbackSnippet.get().getContext();
		Assert.assertNotNull("@Fallback snippet has context", context);
		Assert.assertTrue("@Fallback snippet context is Java context", context instanceof SnippetContextForJava);

		ProjectLabelInfoEntry projectInfo = new ProjectLabelInfoEntry("", new ArrayList<>());
		boolean match = ((SnippetContextForJava) context).isMatch(projectInfo);
		Assert.assertFalse("Project has no org.eclipse.microprofile.faulttolerance.Fallback type", match);

		ProjectLabelInfoEntry projectInfo2 = new ProjectLabelInfoEntry("",
				Arrays.asList("org.eclipse.microprofile.faulttolerance.Fallback"));
		boolean match2 = ((SnippetContextForJava) context).isMatch(projectInfo2);
		Assert.assertTrue("Project has org.eclipse.microprofile.faulttolerance.Fallback type", match2);
	}

	private static Optional<Snippet> findByPrefix(String prefix, SnippetRegistry registry) {
		return registry.getSnippets().stream().filter(snippet -> snippet.getPrefixes().contains(prefix)).findFirst();
	}
}
