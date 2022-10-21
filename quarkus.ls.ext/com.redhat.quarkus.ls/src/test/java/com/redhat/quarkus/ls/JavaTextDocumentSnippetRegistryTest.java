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
package com.redhat.quarkus.ls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.lsp4mp.ls.java.JavaTextDocumentSnippetRegistry;
import org.eclipse.lsp4mp.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4mp.ls.commons.snippets.ISnippetContext;
import org.eclipse.lsp4mp.ls.commons.snippets.Snippet;
import org.eclipse.lsp4mp.ls.commons.snippets.SnippetRegistry;
import org.eclipse.lsp4mp.snippets.SnippetContextForJava;

/**
 * test for Java snippet registry.
 *
 * @author Angelo ZERR
 *
 */
public class JavaTextDocumentSnippetRegistryTest {

  @Test
  public void javaSnippets() {
    JavaTextDocumentSnippetRegistry registry = new JavaTextDocumentSnippetRegistry();
    Assert.assertFalse("Tests has Quarkus Java snippets", registry.getSnippets().isEmpty());

    Optional<Snippet> qrtcSnippet = findByPrefix("qtrc", registry);
    Assert.assertTrue("Tests has Quarkus - new test resource class (qtrc) snippets", qrtcSnippet.isPresent());
    
    Optional<Snippet> qitrcSnippet = findByPrefix("qitrc", registry);
    Assert.assertTrue("Tests has Quarkus - new integration test resource class (qitrc) snippet", qrtcSnippet.isPresent());

    ISnippetContext<?> context = qrtcSnippet.get().getContext();
    Assert.assertNotNull("Quarkus - new test resource class (qtrc) snippet has context", context);

    Assert.assertTrue("Quarkus - new test resource class (qtrc) snippet context is Java context", context instanceof SnippetContextForJava);

    ProjectLabelInfoEntry projectInfo = new ProjectLabelInfoEntry("", "", new ArrayList<>());
    boolean match = ((SnippetContextForJava) context).isMatch(projectInfo);
    Assert.assertFalse("Project has no io.quarkus.test.junit.QuarkusTest type", match);

    ProjectLabelInfoEntry projectInfo2 = new ProjectLabelInfoEntry("", "", Arrays.asList("io.quarkus.test.junit.QuarkusTest"));
    boolean match2 = ((SnippetContextForJava) context).isMatch(projectInfo2);
    Assert.assertTrue("Project has io.quarkus.test.junit.QuarkusTest type", match2);
  }

  private static Optional<Snippet> findByPrefix(String prefix, SnippetRegistry registry) {
    return registry.getSnippets().stream().filter(snippet -> snippet.getPrefixes().contains(prefix)).findFirst();
  }
}