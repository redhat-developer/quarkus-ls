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
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.RESOLVERS_SIZE;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.template.TemplateFileTextDocumentService;
import com.redhat.qute.project.MockQuteProjectRegistry;
import com.redhat.qute.project.user_tags.QuteUserTagsProject;
import com.redhat.qute.services.QuteLanguageService;
import com.redhat.qute.settings.SharedSettings;

/**
 * Tests for Qute completion in user tag section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInUserTagTest {

	@Test
	public void specialKeys() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{|}";

		// In qute template
		testCompletionFor(template, //
				RESOLVERS_SIZE + 1, //
				c("item", "item", r(1, 1, 1, 1)), //
				c("inject:bean", "inject:bean", r(1, 1, 1, 1)), //
				c("inject:plexux", "inject:plexux", r(1, 1, 1, 1)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(1, 1, 1, 1)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(1, 1, 1, 1)), //
				c("GLOBAL", "GLOBAL", r(1, 1, 1, 1)), //
				c("VARCHAR_SIZE", "VARCHAR_SIZE", r(1, 1, 1, 1)), //
				c("uri:Login", "uri:Login", r(1, 1, 1, 1)), //
				c("msg:hello_name(name : String) : String", "msg:hello_name(${1:name})$0", r(1, 1, 1, 1)), //
				c("msg2:hello() : String", "msg2:hello", r(1, 1, 1, 1)), //
				c("bundle", "bundle", r(1, 1, 1, 1)));

		// In user tag
		testCompletionFor(template, //
				"src/main/resources/templates/tags/form.html", //
				"tags/form", //
				RESOLVERS_SIZE /* item, inject:bean, config:getConfigProperty */ + 3 /* it, nested-content, _args */
						+ 1 /*
							 * global variables
							 */, //
				c("item", "item", r(1, 1, 1, 1)), //
				c("inject:bean", "inject:bean", r(1, 1, 1, 1)), //
				c("inject:plexux", "inject:plexux", r(1, 1, 1, 1)), //
				c("config:*(propertyName : String) : Object", "config:propertyName", r(1, 1, 1, 1)),
				c("config:property(propertyName : String) : Object", "config:property(propertyName)", r(1, 1, 1, 1)), //
				c("GLOBAL", "GLOBAL", r(1, 1, 1, 1)), //
				c("VARCHAR_SIZE", "VARCHAR_SIZE", r(1, 1, 1, 1)), //
				c("it", "it", r(1, 1, 1, 1)), //
				c("nested-content", "nested-content", r(1, 1, 1, 1)), //
				c("_args", "_args", r(1, 1, 1, 1)), //
				c("uri:Login", "uri:Login", r(1, 1, 1, 1)), //
				c("msg:hello_name(name : String) : String", "msg:hello_name(name)", r(1, 1, 1, 1)), //
				c("msg2:hello() : String", "msg2:hello", r(1, 1, 1, 1)), //
				c("bundle", "bundle", r(1, 1, 1, 1)));

	}

	@Test
	public void test() throws InterruptedException, ExecutionException {

		MockQuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		QuteUserTagsProject tagsProject = (QuteUserTagsProject) projectRegistry
				.getProject(new ProjectInfo(QuteUserTagsProject.PROJECT_URI, null,
						List.of(new TemplateRootPath("file:///foo/src/main/resources/templates")), //
						Collections.emptySet()));
		tagsProject.registerUserTag("user", "{name}");

		ProjectInfo projectInfo = tagsProject.getProjectInfo();

		TemplateFileTextDocumentService service = new TemplateFileTextDocumentService(
				new QuteLanguageService(projectRegistry), new QuteProjectInfoProvider() {

					@Override
					public CompletableFuture<Collection<ProjectInfo>> getProjects() {
						return CompletableFuture.completedFuture(List.of(projectInfo));
					}

					@Override
					public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
						return CompletableFuture.completedFuture(projectInfo);
					}
				}, null, new SharedSettings());

		DidOpenTextDocumentParams params2 = new DidOpenTextDocumentParams(new TextDocumentItem(
				"file:///foo/src/main/resources/templates/for.html", "qute-html", 1, "{#user name=\"FOO\" /}"));
		service.didOpen(params2);

		TextDocumentItem userTagItem = new TextDocumentItem("file:///foo/src/main/resources/templates/tags/user.html",
				"qute-html", 1, "{name.charAt(0)}");
		DidOpenTextDocumentParams params = new DidOpenTextDocumentParams(userTagItem);
		service.didOpen(params);

		CompletionParams completionParams = new CompletionParams(
				new TextDocumentIdentifier("file:///foo/src/main/resources/templates/tags/user.html"),
				new Position(0, 2));
		Either<List<CompletionItem>, CompletionList> result = service.completion(completionParams).get();
		System.err.println(result);

	}

}