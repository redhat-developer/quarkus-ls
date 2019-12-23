/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package com.redhat.microprofile.jdt.internal.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.embedder.ICallable;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenExecutionContext;
import org.eclipse.m2e.core.internal.MavenPluginActivator;

/**
 * Utility to locate jars from the local Maven repository.
 *
 * @author Snjeza
 * @author Fred Bricon
 *
 */
public class DependencyUtil {

	public static File getArtifact(String groupId, String artifactId, String version, String classifier,
			IProgressMonitor monitor) throws FileNotFoundException, CoreException {
		ArtifactKey key = new ArtifactKey(groupId, artifactId, version, classifier);
		File archive = getLocalArtifactFile(key);
		if (archive == null) {
			Artifact artifact = MavenPlugin.getMaven().resolve(key.getGroupId(), key.getArtifactId(), key.getVersion(),
					"jar", key.getClassifier(), null, monitor);
			if (artifact == null) {
				throw new FileNotFoundException("Unable to find " + key);
			}
			archive = getLocalArtifactFile(key);
		}
		return archive;
	}

	// From org.eclipse.m2e.jdt.internal.BuildPathManager#getAttachedArtifactFile
	private static File getLocalArtifactFile(ArtifactKey a) {
		// can't use Maven resolve methods since they mark artifacts as not-found even
		// if they could be resolved remotely
		IMaven maven = MavenPlugin.getMaven();
		try {
			ArtifactRepository localRepository = maven.getLocalRepository();
			String relPath = maven.getArtifactPath(localRepository, a.getGroupId(), a.getArtifactId(), a.getVersion(),
					"jar", //$NON-NLS-1$
					a.getClassifier());
			File file = new File(localRepository.getBasedir(), relPath).getCanonicalFile();
			if (file.canRead() && file.isFile()) {
				return file;
			}
		} catch (CoreException | IOException ex) {
			// fall through
		}
		return null;
	}

	public static List<com.redhat.microprofile.jdt.core.ArtifactResolver.Artifact> getDependencies(String groupId,
			String artifactId, String version, IProgressMonitor monitor) throws CoreException {
		org.eclipse.aether.artifact.Artifact artifact = new DefaultArtifact(groupId, artifactId, null, version);
		ICallable<ArtifactDescriptorResult> callable = new ICallable<ArtifactDescriptorResult>() {
			public ArtifactDescriptorResult call(IMavenExecutionContext context, IProgressMonitor monitor)
					throws CoreException {
				return resolveDescriptor(artifact, context.getRepositorySession());
			}
		};

		ArtifactDescriptorResult result = MavenPlugin.getMaven().execute(callable, monitor);
		if (result != null) {
			return result.getDependencies().stream()
					.map(dep -> new com.redhat.microprofile.jdt.core.ArtifactResolver.Artifact(
							dep.getArtifact().getGroupId(), dep.getArtifact().getArtifactId(),
							dep.getArtifact().getVersion(), dep.getArtifact().getClassifier()))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private static ArtifactDescriptorResult resolveDescriptor(org.eclipse.aether.artifact.Artifact artifact,
			RepositorySystemSession repoSession) {
		try {
			RepositorySystem repoSystem = MavenPluginActivator.getDefault().getRepositorySystem();
			repoSession = new DefaultRepositorySystemSession(repoSession);
			return repoSystem.readArtifactDescriptor(repoSession,
					new ArtifactDescriptorRequest().setArtifact(artifact));
		} catch (ArtifactDescriptorException e) {
			// throw new AppModelResolverException("Failed to read descriptor of " +
			// artifact, e);
			return null;
		}
	}
}