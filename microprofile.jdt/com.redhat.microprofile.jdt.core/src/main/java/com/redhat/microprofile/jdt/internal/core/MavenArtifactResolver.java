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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;

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
	public String getArtifact(Artifact artifact, IProgressMonitor monitor) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();
		String classifier = artifact.getClassifier();
		File jarFile = null;
		try {
			jarFile = DependencyUtil.getArtifact(groupId, artifactId, version, classifier, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Maven artifact JAR (groupId=" + groupId + ", artifactId=" + artifactId
					+ ", version=" + version + ") download failed.", e);
			return null;
		}
		return jarFile != null ? jarFile.toString() : null;
	}

	@Override
	public List<Artifact> getDependencies(Artifact artifact, IProgressMonitor monitor) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();
		try {
			return DependencyUtil.getDependencies(groupId, artifactId, version, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Maven artifact JAR dependencies (groupId=" + groupId + ", artifactId="
					+ artifactId + ", version=" + version + ") failed.", e);
			return Collections.emptyList();
		}
	}
}