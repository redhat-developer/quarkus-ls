package com.redhat.qute.commons;

import java.nio.file.Path;
import java.util.List;

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

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public Path getBasePath() {
		if (basePath == null && baseDir != null) {
			basePath = FileUtils.createPath(baseDir);
		}
		return basePath;
	}

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

	public boolean isIncluded(String uri) {
		Path path = FileUtils.createPath(uri);
		return isIncluded(path);
	}

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
