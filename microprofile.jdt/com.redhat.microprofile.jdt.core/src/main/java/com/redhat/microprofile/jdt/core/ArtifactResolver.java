package com.redhat.microprofile.jdt.core;

import org.eclipse.core.runtime.IProgressMonitor;

import com.redhat.microprofile.jdt.internal.core.MavenArtifactResolver;

/**
 * Artifact resolver API
 *
 */
public interface ArtifactResolver {

	public static final ArtifactResolver DEFAULT_ARTIFACT_RESOLVER = new MavenArtifactResolver();

	default String getArtifact(String groupId, String artifactId, String version, IProgressMonitor monitor) {
		return getArtifact(groupId, artifactId, version, null, monitor);
	}

	String getArtifact(String groupId, String artifactId, String version, String classifier, IProgressMonitor monitor);

	String getSources(String groupId, String artifactId, String version, IProgressMonitor monitor);
}