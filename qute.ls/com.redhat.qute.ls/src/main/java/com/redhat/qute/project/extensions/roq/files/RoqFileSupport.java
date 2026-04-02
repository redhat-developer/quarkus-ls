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
package com.redhat.qute.project.extensions.roq.files;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Predicate;

import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.extensions.roq.RoqProjectExtension;

/**
 * Base class for layout, theme-layout, image files support.
 */
public abstract class RoqFileSupport {

	protected static final Predicate<Path> IMAGE_FILE_FILTER = p -> isImage(p);

	protected static final Predicate<Path> LAYOUT_FILE_FILTER = p -> !isImage(p);

	protected static final String HTML_EXTENSION = ".html";

	private static final Set<String> IMAGE_EXTENSIONS = Set.of(//
			"jpg", "jpeg", "png", "gif", "webp", //
			"bmp", "tiff", "tif", "svg", "ico", //
			"avif", "heic", "heif");

	@FunctionalInterface
	public static interface FileCollector {

		void collect(Path baseFolder, Path file, String templateId, boolean binary, String origin);
	}

	// Templates folders
	public static final String TEMPLATES_FOLDER = "templates";
	public static final String PUBLIC_IMAGES_FOLDER = "public/images";

	// Layout folders
	public static final String LAYOUTS_FOLDER = "layouts";
	public static final String THEME_LAYOUTS_FOLDER = "theme-layouts";
	public static final String THEME_LAYOUTS_FOLDER_WITH_SLASH = THEME_LAYOUTS_FOLDER + "/";

	private final RoqProjectExtension roq;

	public RoqFileSupport(RoqProjectExtension roq) {
		this.roq = roq;
	}

	protected void collectFiles(Path parent, Path dir, RoqFileSupport.FileCollector collector,
			Predicate<Path> filterFile) {
		if (dir != null && Files.isDirectory(dir)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
				for (Path file : stream) {
					if (Files.isDirectory(file)) {
						collectFiles(parent, file, collector, filterFile);
					} else {
						if (filterFile.test(file)) {
							collector.collect(parent, file, null, false, null);
						}
					}
				}
			} catch (IOException e) {
				// Silently ignore - data directory may not exist yet
			}
		}
	}

	public ExtendedDataModelProject getDataModelProject() {
		return roq.getDataModelProject();
	}

	public RoqProjectExtension getRoq() {
		return roq;
	}

	/**
	 * Returns true if the given path points to a file with an image extension. Does
	 * not check if the file actually exists on disk.
	 *
	 * @param path the file path to check
	 * @return true if the file has an image extension, false otherwise
	 */
	private static boolean isImage(Path path) {
		if (path == null) {
			return false;
		}
		String fileName = path.getFileName() != null ? path.getFileName().toString() : "";
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
			return false;
		}
		String extension = fileName.substring(dotIndex + 1).toLowerCase();
		return IMAGE_EXTENSIONS.contains(extension);
	}

}
