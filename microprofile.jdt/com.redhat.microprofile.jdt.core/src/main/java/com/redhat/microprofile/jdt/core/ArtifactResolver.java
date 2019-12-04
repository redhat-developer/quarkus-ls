package com.redhat.microprofile.jdt.core;

import com.redhat.microprofile.jdt.internal.core.MavenArtifactResolver;

/**
 * Artifact resolver API
 *
 */
public interface ArtifactResolver {

	public static final ArtifactResolver DEFAULT_ARTIFACT_RESOLVER = new MavenArtifactResolver();

	String getArtifact(String groupId, String artifactId, String version);

	String getSources(String groupId, String artifactId, String version);
}