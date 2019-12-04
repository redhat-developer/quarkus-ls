/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.microprofile.jdt.core.ArtifactResolver;
import com.redhat.microprofile.jdt.internal.core.utils.DependencyUtil;

/**
 * Maven artifact resolver used to download JAR and JAR sources with maven.
 * 
 * @author Angelo ZERR
 *
 */
public class MavenArtifactResolver implements ArtifactResolver {

	private static final Logger LOGGER = Logger.getLogger(MavenArtifactResolver.class.getName());

	@Override
	public String getArtifact(String groupId, String artifactId, String version) {
		File jarFile = null;
		try {
			jarFile = DependencyUtil.getArtifact(groupId, artifactId, version, null);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Maven artifact JAR (groupId=" + groupId + ", artifactId=" + artifactId
					+ ", version=" + version + ") download failed.", e);
			return null;
		}
		return jarFile != null ? jarFile.toString() : null;
	}

	@Override
	public String getSources(String groupId, String artifactId, String version) {
		File jarFile = null;
		try {
			jarFile = DependencyUtil.getSources(groupId, artifactId, version);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Maven artifact JAR Sources (groupId=" + groupId + ", artifactId=" + artifactId
					+ ", version=" + version + ") download failed.", e);
			return null;
		}
		return jarFile != null ? jarFile.toString() : null;
	}

}
