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
import java.util.HashSet;
import java.util.Set;

/**
 * Template root path which hosts Qute templates (ex :
 * src/main/resources/templates).
 */
public class TemplateRootPath {

	public static final String RESOURCE_DIR = "${resources-dir}/";

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

	private boolean onlyTags;

	private boolean namespacedTagSupported;

	private Boolean altExprSyntax;

	private transient Path basePath;

	private transient Path tagsDir;

	public TemplateRootPath() {

	}

	public TemplateRootPath(String baseDir) {
		this(baseDir, null);
	}

	public TemplateRootPath(String baseDir, String origin) {
		this(baseDir, false, true, origin);
	}

	public TemplateRootPath(String baseDir, boolean onlyTags, boolean namespacedTagSupported, String origin) {
		setBaseDir(baseDir);
		setOnlyTags(onlyTags);
		setNamespacedTagSupported(namespacedTagSupported);
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

	public boolean isOnlyTags() {
		return onlyTags;
	}

	public void setOnlyTags(boolean onlyTags) {
		this.onlyTags = onlyTags;
	}

	public boolean isNamespacedTagSupported() {
		return namespacedTagSupported;
	}

	public void setNamespacedTagSupported(boolean namespacedTagSupported) {
		this.namespacedTagSupported = namespacedTagSupported;
	}

	/**
	 * Returns the alternative expression syntax configuration from .qute file.
	 *
	 * @return true if enabled via .qute, false if disabled via .qute, null if no .qute file
	 */
	public Boolean getAltExprSyntax() {
		return altExprSyntax;
	}

	/**
	 * Sets the alternative expression syntax configuration from .qute file.
	 *
	 * @param altExprSyntax true to enable, false to disable, null if no .qute file
	 */
	public void setAltExprSyntax(Boolean altExprSyntax) {
		this.altExprSyntax = altExprSyntax;
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

	/**
	 * Concatenates path segments, ensuring each segment is separated by a {@code /}
	 * and the result ends with a trailing {@code /}.
	 *
	 * @param segments the path segments to concatenate
	 * @return the concatenated path ending with {@code /}
	 */
	public static String getPath(String... segments) {
		StringBuilder path = new StringBuilder();
		for (String segment : segments) {
			if (segment == null || segment.isEmpty()) {
				continue;
			}
			if (path.length() > 0 && path.charAt(path.length() - 1) != '/') {
				path.append('/');
			}
			path.append(segment.startsWith("/") ? segment.substring(1) : segment);
		}
		if (path.length() > 0 && path.charAt(path.length() - 1) != '/') {
			path.append('/');
		}
		return path.toString();
	}

	/**
	 * Resolves the given path segments against the project folder or source
	 * folders.
	 *
	 * <p>
	 * The path segments are first concatenated using {@link #getPath(String...)}.
	 * If the resulting path starts with {@value #RESOURCE_DIR}, the prefix is
	 * stripped and the remaining path is resolved against each source folder,
	 * returning one resolved path per source folder. Otherwise, the path is
	 * resolved against the project folder.
	 *
	 * @param projectFolder the root project folder
	 * @param sourceFolders the set of source folders (e.g. src/main/resources)
	 * @param paths         the path segments to concatenate and resolve
	 * @return the set of resolved paths, or an empty set if {@code paths} is null
	 *         or empty
	 */
	public static Set<String> resolvePath(String projectFolder, Set<String> sourceFolders, String... paths) {
		if (paths == null || paths.length == 0) {
			return Set.of();
		}
		Set<String> result = new HashSet<>();
		collectPaths(projectFolder, sourceFolders, getPath(paths), result);
		return result;
	}

	/**
	 * Resolves the given path segments against the project folder or source
	 * folders, returning a single resolved path corresponding to the first
	 * available source folder.
	 *
	 * @param projectFolder the root project folder
	 * @param sourceFolders the set of source folders (e.g. src/main/resources)
	 * @param paths         the path segments to concatenate and resolve
	 * @return the first resolved path, or null if {@code paths} is null or empty
	 */
	public static String resolveSinglePath(String projectFolder, Set<String> sourceFolders, String... paths) {
		if (paths == null || paths.length == 0) {
			return null;
		}
		return resolveSinglePath(projectFolder, sourceFolders.stream().findFirst().orElse(null), getPath(paths));
	}

	/**
	 * Collects resolved paths into the given set by resolving {@code path} against
	 * the project folder or each source folder.
	 *
	 * @param projectFolder the root project folder
	 * @param sourceFolders the set of source folders
	 * @param path          the already-concatenated path to resolve
	 * @param result        the set to collect resolved paths into
	 */
	private static void collectPaths(String projectFolder, Set<String> sourceFolders, String path, Set<String> result) {
		if (path.startsWith(RESOURCE_DIR)) {
			// Strip the ${resources-dir}/ prefix and resolve against each source folder
			String relativePath = path.substring(RESOURCE_DIR.length());
			for (String sourceFolder : sourceFolders) {
				result.add(getPath(sourceFolder, relativePath));
			}
		} else {
			// No ${resources-dir}/ prefix, resolve directly against the project folder
			result.add(getPath(projectFolder, path));
		}
	}

	/**
	 * Resolves a single already-concatenated path against the project folder or a
	 * single source folder.
	 *
	 * @param projectFolder the root project folder
	 * @param sourceFolder  the source folder, or null if none available
	 * @param path          the already-concatenated path to resolve
	 * @return the resolved path, or null if {@code sourceFolder} is null and path
	 *         starts with {@value #RESOURCE_DIR}
	 */
	public static String resolveSinglePath(String projectFolder, String sourceFolder, String path) {
		if (path.startsWith(RESOURCE_DIR)) {
			// Strip the ${resources-dir}/ prefix and resolve against the source folder
			String relativePath = path.substring(RESOURCE_DIR.length());
			return sourceFolder != null ? getPath(sourceFolder, relativePath) : null;
		}
		// No ${resources-dir}/ prefix, resolve directly against the project folder
		return getPath(projectFolder, path);
	}
}
