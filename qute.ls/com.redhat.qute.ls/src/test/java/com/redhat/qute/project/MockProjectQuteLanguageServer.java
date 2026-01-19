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
package com.redhat.qute.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.TextDocumentIdentifier;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.utils.IOUtils;

public class MockProjectQuteLanguageServer extends MockQuteLanguageServer {

	private final String projectUri;

	private final Set<ProjectFeature> features;
	private Path projectFolder;

	private final Path resourcesFolder;

	private final Path templatesPath;

	private final ProjectInfo projectInfo;

	public MockProjectQuteLanguageServer(String projectUri) {
		this(projectUri, Collections.emptySet());
	}

	public MockProjectQuteLanguageServer(String projectUri, Set<ProjectFeature> features) {
		this.projectUri = projectUri;
		this.features = features;
		this.projectFolder = FileUtils.createPath("src/test/resources/projects/" + projectUri);
		this.resourcesFolder = projectFolder.resolve("src/main/resources");
		this.templatesPath = resourcesFolder.resolve("templates");
		this.projectInfo = createProject();
	}

	public TextDocumentIdentifier didOpenFle(String fileName) throws IOException {
		return didOpenWithContent(fileName, null);
	}

	public TextDocumentIdentifier didOpenWithContent(String fileName, String fileContent) throws IOException {
		Path filePath = getFilePath(fileName);
		String fileUri = FileUtils.toUri(filePath);
		if (fileContent == null) {
			fileContent = Files.exists(filePath) ? IOUtils.getContent(filePath) : "";
		}
		return super.didOpen(fileUri, fileContent);
	}

	public void didChangeFile(String fileName, String text, int line, int startCharacter, int endCharacter) {
		Path filePath = getFilePath(fileName);
		String fileUri = FileUtils.toUri(filePath);
		super.didChange(fileUri, text, line, startCharacter, endCharacter);
	}

	public void didSaveFile(String fileName) {
		Path filePath = getFilePath(fileName);
		String fileUri = FileUtils.toUri(filePath);
		super.didSave(fileUri);
		super.changeFile(filePath, null);
	}

	public Hover hoverFile(String fileName, int line, int character) {
		Path filePath = getFilePath(fileName);
		String fileUri = FileUtils.toUri(filePath);
		return super.hover(fileUri, line, character);
	}

	public CompletionList completionFile(String fileName, int line, int character) {
		Path filePath = getFilePath(fileName);
		String fileUri = FileUtils.toUri(filePath);
		return super.completion(fileUri, line, character);
	}

	public List<? extends Location> referencesFile(String fileName, int line, int character) {
		Path filePath = getFilePath(fileName);
		String fileUri = FileUtils.toUri(filePath);
		return super.references(fileUri, line, character);
	}

	public Path getFilePath(String fileName) {
		return getTemplatesPath().resolve(fileName);
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
		return CompletableFuture.completedFuture(projectInfo);
	}

	@Override
	public CompletableFuture<Collection<ProjectInfo>> getProjects() {
		Collection<ProjectInfo> projects = Arrays.asList(createProject());
		return CompletableFuture.completedFuture(projects);
	}

	private ProjectInfo createProject() {
		return new ProjectInfo(projectUri, //
				FileUtils.toUri(projectFolder), //
				Collections.emptyList(), //
				List.of(new TemplateRootPath(FileUtils.toUri(templatesPath))), //
				Set.of(FileUtils.toUri(resourcesFolder)), //
				features);
	}

	private Path getTemplatesPath() {
		return templatesPath;
	}

	public void changeFile(String fileName, String content) throws Exception {
		Path filePath = getFilePath(fileName);
		super.changeFile(filePath, content);
	}

	public void createFile(String fileName) throws Exception {
		Path filePath = getFilePath(fileName);
		super.createFile(filePath);
	}

	public void deleteFile(String fileName) throws Exception {
		Path filePath = getFilePath(fileName);
		super.deleteFile(filePath);
	}

}
