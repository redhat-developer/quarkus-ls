/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons;

import java.nio.file.Path;
import java.util.List;

/**
 * Template root path which hosts Qute templates (ex :
 * src/main/resources/templates).
 */
public class TemplateRootPath {

	public static final String TAGS_DIR = "tags";

	public static class FilePath {

		private boolean folder;
		private String path;

		public boolean isFolder() {
			return folder;
		}

		public void setFolder(boolean folder) {
			this.folder = folder;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

	}

	private String baseDir;

	private String origin;

	private transient Path basePath;

	private transient Path tagsDir;

	private List<FilePath> includes;

	private List<FilePath> excludes;

	public TemplateRootPath() {

	}

	public TemplateRootPath(String baseDir) {
		this(baseDir, null);
	}

	public TemplateRootPath(String baseDir, String origin) {
		setBaseDir(baseDir);
		setOrigin(origin);
	}

	public String getBaseDir() {
		return baseDir;
	}

	/**
	 * Returns the origin Quarkus extension (ex : core, roq, etc) which generates
	 * this template root path.
	 * 
	 * @return the origin Quarkus extension (ex : core, roq, etc) which generates
	 *         this template root path.
	 */
	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	/**
	 * Returns the base path of the template root path (ex
	 * :src/main/resources/templates) and null otherwise.
	 * 
	 * @return the base path of the template root path (ex
	 *         :src/main/resources/templates) and null otherwise.
	 */
	public Path getBasePath() {
		if (basePath == null && baseDir != null) {
			basePath = FileUtils.createPath(baseDir);
		}
		return basePath;
	}

	/**
	 * Returns the 'tags' dir path of the template root path (ex
	 * :src/main/resources/templates/tags) and null otherwise.
	 * 
	 * @return the 'tags' dir path of the template root path (ex
	 *         :src/main/resources/templates/tags) and null otherwise.
	 */
	public Path getTagsDir() {
		if (tagsDir != null) {
			return tagsDir;
		}
		Path basePath = getBasePath();
		if (basePath != null) {
			this.tagsDir = basePath.resolve(TAGS_DIR);
		}
		return tagsDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public List<FilePath> getIncludes() {
		return includes;
	}

	public void setIncludes(List<FilePath> includes) {
		this.includes = includes;
	}

	public List<FilePath> getExcludes() {
		return excludes;
	}

	public void setExcludes(List<FilePath> excludes) {
		this.excludes = excludes;
	}

	/**
	 * Returns true if the given uri is included in the template root path and
	 * false otherwise.
	 * 
	 * @param path the file path.
	 * @return true if the given uri is included in the template root path and
	 *         false otherwise.
	 */
	public boolean isIncluded(String uri) {
		Path path = FileUtils.createPath(uri);
		return isIncluded(path);
	}

	/**
	 * Returns true if the given path is included in the template root path and
	 * false otherwise.
	 * 
	 * @param path the file path.
	 * @return true if the given path is included in the template root path and
	 *         false otherwise.
	 */
	public boolean isIncluded(Path path) {
		if (path == null) {
			return false;
		}
		Path basePath = getBasePath();
		if (basePath == null) {
			return false;
		}
		return path.startsWith(basePath);
	}
}
