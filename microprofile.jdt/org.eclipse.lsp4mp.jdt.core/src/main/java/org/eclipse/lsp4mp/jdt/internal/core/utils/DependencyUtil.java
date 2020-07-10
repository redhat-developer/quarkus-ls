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
package org.eclipse.lsp4mp.jdt.internal.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.internal.MavenPluginActivator;

/**
 * Utility to locate jars from the local Maven repository.
 *
 * @author Snjeza
 * @author Fred Bricon
 *
 */
public class DependencyUtil {

	private static final String JAR_FILE_TYPE = "jar";
	private static final String POM_FILE_TYPE = "pom";

	public static File getArtifact(String groupId, String artifactId, String version, String classifier,
			IProgressMonitor monitor) throws FileNotFoundException, CoreException {
		ArtifactKey key = new ArtifactKey(groupId, artifactId, version, classifier);
		// Download pom
		File pomFile = getLocalArtifactFile(key, POM_FILE_TYPE);
		if (pomFile == null) {
			Artifact pom = MavenPlugin.getMaven().resolve(key.getGroupId(), key.getArtifactId(), key.getVersion(),
					POM_FILE_TYPE, null, null, monitor);
			if (pom == null) {
				throw new FileNotFoundException("Unable to find pom file for " + key);// Should it be just a warning?
			}
		}
		// Download jar
		File archive = getLocalArtifactFile(key, JAR_FILE_TYPE);
		if (archive == null) {
			Artifact artifact = MavenPlugin.getMaven().resolve(key.getGroupId(), key.getArtifactId(), key.getVersion(),
					JAR_FILE_TYPE, key.getClassifier(), null, monitor);
			if (artifact == null) {
				throw new FileNotFoundException("Unable to find " + key);
			}
			archive = getLocalArtifactFile(key, JAR_FILE_TYPE);
		}
		return archive;
	}

	// From org.eclipse.m2e.jdt.internal.BuildPathManager#getAttachedArtifactFile
	private static File getLocalArtifactFile(ArtifactKey a, String type) {
		// can't use Maven resolve methods since they mark artifacts as not-found even
		// if they could be resolved remotely
		IMaven maven = MavenPlugin.getMaven();
		try {
			ArtifactRepository localRepository = maven.getLocalRepository();
			String relPath = maven.getArtifactPath(localRepository, a.getGroupId(), a.getArtifactId(), a.getVersion(),
					type, a.getClassifier());
			File file = new File(localRepository.getBasedir(), relPath).getCanonicalFile();
			if (file.canRead() && file.isFile()) {
				return file;
			}
		} catch (CoreException | IOException ex) {
			// fall through
		}
		return null;
	}

	public static Set<org.eclipse.lsp4mp.jdt.core.ArtifactResolver.Artifact> getDependencies(String groupId,
			String artifactId, String version, IProgressMonitor monitor) throws CoreException {
		org.eclipse.aether.artifact.Artifact artifact = new DefaultArtifact(groupId, artifactId, null, version);
		CollectResult result = MavenPlugin.getMaven().execute((context, progress) -> {
			try {
				return collectDependencies(artifact, context.getRepositorySession());
			} catch (ArtifactDescriptorException | DependencyCollectionException e) {
				ArtifactKey key = new ArtifactKey(groupId, artifactId, version, null);
				throw new CoreException(new Status(IStatus.ERROR, MicroProfileCorePlugin.PLUGIN_ID,
						"Error while collecting dependencies for " + key, e));
			}
		}, monitor);
		if (result != null) {
			Set<org.eclipse.lsp4mp.jdt.core.ArtifactResolver.Artifact> dependencies = new HashSet<>();
			result.getRoot().accept(new DependencyVisitor() {

				@Override
				public boolean visitLeave(DependencyNode node) {
					org.eclipse.aether.artifact.Artifact dep = node.getDependency().getArtifact();
					dependencies.add(new org.eclipse.lsp4mp.jdt.core.ArtifactResolver.Artifact(dep.getGroupId(),
							dep.getArtifactId(), dep.getVersion(), dep.getClassifier()));
					return true;
				}

				@Override
				public boolean visitEnter(DependencyNode node) {
					return true;
				}
			});
			return dependencies;
		}
		return Collections.emptySet();
	}

	private static CollectResult collectDependencies(org.eclipse.aether.artifact.Artifact artifact,
			RepositorySystemSession repoSession) throws ArtifactDescriptorException, DependencyCollectionException {
		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRoot(new Dependency(artifact, JavaScopes.RUNTIME));
		RepositorySystem repoSystem = MavenPluginActivator.getDefault().getRepositorySystem();
		repoSession = new DefaultRepositorySystemSession(repoSession);
		return repoSystem.collectDependencies(repoSession, collectRequest);
	}
}