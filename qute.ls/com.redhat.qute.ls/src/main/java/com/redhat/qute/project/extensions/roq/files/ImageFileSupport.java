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

import java.nio.file.Files;
import java.nio.file.Path;

import com.redhat.qute.parser.template.sections.TemplatePath;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.extensions.roq.RoqProjectExtension;

/**
 * Image files support.
 */
public class ImageFileSupport extends RoqFileSupport {

	private static final String HTTPS_SHEME = "https:";
	private static final String HTTP_SHEME = "http:";

	public ImageFileSupport(RoqProjectExtension roq) {
		super(roq);
	}

	public void collectImages(Path filePath, ExtendedDataModelProject dataModelProject,
			RoqFileSupport.FileCollector collector) {
		// 1. collect images from the folder of the given file path
		Path imagesFolder = filePath.getParent();
		collectFiles(imagesFolder, imagesFolder, collector, IMAGE_FILE_FILTER);

		// 2. collect images from public/images
		Path projectFolder = dataModelProject.getProjectFolder();
		if (projectFolder != null) {
			imagesFolder = projectFolder.resolve(PUBLIC_IMAGES_FOLDER);
			collectFiles(imagesFolder, imagesFolder, collector, IMAGE_FILE_FILTER);
		}
	}

	public TemplatePath getImagePath(Path filePath, String imageFilePath) {
		imageFilePath = imageFilePath.trim();
		if (imageFilePath.isEmpty()) {
			return null;
		}

		if (isHttpImagePath(imageFilePath)) {
			String imageUrl = imageFilePath;
			// We consider that any http:, https: image url are valid.
			return new TemplatePath(imageUrl, null, true);
		}

		if (imageFilePath.charAt(0) == '/') {
			imageFilePath = imageFilePath.substring(1);
		}

		// 1. Check if image exists in the folder of the given file path
		Path imagesFolder = filePath.getParent();
		Path imagesPath = imagesFolder.resolve(imageFilePath);
		if (Files.exists(imagesPath)) {
			return new TemplatePath(imagesPath, imageFilePath, true);
		}

		// 2. Check if image exists in the public.images folder
		var dataModelProject = getDataModelProject();
		Path projectFolder = dataModelProject.getProjectFolder();
		imagesFolder = projectFolder.resolve(PUBLIC_IMAGES_FOLDER);
		Path publicImagePath = imagesFolder.resolve(imageFilePath);
		return new TemplatePath(publicImagePath, imageFilePath);
	}

	private static boolean isHttpImagePath(String imagePath) {
		return imagePath != null && (imagePath.startsWith(HTTP_SHEME) || imagePath.startsWith(HTTPS_SHEME));
	}
}
