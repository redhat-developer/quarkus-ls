package com.redhat.microprofile.jdt.core;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.redhat.microprofile.jdt.internal.core.MavenArtifactResolver;

/**
 * Artifact resolver API
 *
 */
public interface ArtifactResolver {

	public static final String CLASSIFIER_JAVADOC = "javadoc";
	public static final String CLASSIFIER_SOURCES = "sources";

	public static class Artifact {

		private final String groupId;
		private final String artifactId;
		private final String version;
		private final String classifier;

		public Artifact(String groupId, String artifactId, String version) {
			this(groupId, artifactId, version, null);
		}

		public Artifact(String groupId, String artifactId, String version, String classifier) {
			super();
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
			this.classifier = classifier;
		}

		public String getGroupId() {
			return groupId;
		}

		public String getArtifactId() {
			return artifactId;
		}

		public String getVersion() {
			return version;
		}

		public String getClassifier() {
			return classifier;
		}
	}

	public static final ArtifactResolver DEFAULT_ARTIFACT_RESOLVER = new MavenArtifactResolver();

	String getArtifact(Artifact artifact, IProgressMonitor monitor);

	List<Artifact> getDependencies(Artifact artifact, IProgressMonitor monitor);
}