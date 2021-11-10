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
package com.redhat.qute.project.indexing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.Position;

import com.redhat.qute.parser.scanner.Scanner;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.scanner.ScannerState;
import com.redhat.qute.parser.template.scanner.TemplateScanner;
import com.redhat.qute.parser.template.scanner.TokenType;

public class QuteTemplateIndex {

	private final static List<String> nativeTags = Arrays.asList("each", "for", "if", "else", "set", "let", "elseif");

	private final String templateId;

	private final Path path;
	private List<QuteIndex> indexes;
	private FilePositionMap filePositionMap;

	private boolean dirty;

	public QuteTemplateIndex(Path path, String templateId) {
		this.path = path;
		this.templateId = templateId;
		this.indexes = Collections.emptyList();
	}

	public void collect() throws IOException {
		this.filePositionMap = null;
		indexes = new ArrayList<>();
		String template = Files.readString(path);
		collect(template, indexes);
	}

	private void collect(String template, List<QuteIndex> indexes) {
		Scanner<TokenType, ScannerState> scanner = TemplateScanner.createScanner(template);
		TokenType token = scanner.scan();
		String lastTag = null;
		int lastTokenOffset = -1;
		SectionKind lastSectionKind = null;
		while (token != TokenType.EOS) {

			switch (token) {
			case StartTag: {
				String tag = scanner.getTokenText();
				if (SectionKind.INCLUDE.name().toLowerCase().equals(tag)) {
					lastTag = tag;
					lastTokenOffset = scanner.getTokenOffset();
					lastSectionKind = SectionKind.INCLUDE;
				} else if (SectionKind.INSERT.name().toLowerCase().equals(tag)) {
					lastTag = tag;
					lastTokenOffset = scanner.getTokenOffset();
					lastSectionKind = SectionKind.INSERT;
				} else if (isCustomTag(tag)) {
					collectIndex(scanner.getTokenOffset(), SectionKind.CUSTOM, scanner.getTokenText(), null, template,
							indexes);
				}
				break;
			}
			case ParameterTag: {
				if (lastSectionKind != null) {
					String parameter = scanner.getTokenText();
					collectIndex(scanner.getTokenOffset(), lastSectionKind, lastTag, parameter, template, indexes);
					lastTokenOffset = -1;
					lastSectionKind = null;
					lastTag = null;
				}
				break;
			}
			case Whitespace: {
				break;
			}
			default:
				if (lastSectionKind != null) {
					collectIndex(lastTokenOffset, lastSectionKind, lastTag, null, template, indexes);
					lastTokenOffset = -1;
					lastSectionKind = null;
					lastTag = null;
				}
			}

			token = scanner.scan();
		}
	}

	private void collectIndex(int tokenOffset, SectionKind sectionKind, String tag, String parameter, String template,
			List<QuteIndex> indexes) {
		Position position = getFilePositionMap(template).getLineCharacterPositionForOffset(tokenOffset);
		QuteIndex index = new QuteIndex(tag, parameter, position, sectionKind, this);
		indexes.add(index);
	}

	public String getTemplateId() {
		return templateId;
	}

	public List<QuteIndex> getIndexes() {
		return indexes;
	}

	public Path getPath() {
		return path;
	}

	private FilePositionMap getFilePositionMap(String template) {
		if (filePositionMap == null) {
			filePositionMap = new DefaultFilePositionMap(template);
		}
		return filePositionMap;
	}

	private static boolean isCustomTag(String tag) {
		return !nativeTags.contains(tag);
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public boolean isDirty() {
		return dirty;
	}
}
