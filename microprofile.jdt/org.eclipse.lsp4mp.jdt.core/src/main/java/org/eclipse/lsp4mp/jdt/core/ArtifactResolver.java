package org.eclipse.lsp4mp.jdt.core;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4mp.jdt.internal.core.MavenArtifactResolver;

/**
 * Artifact resolver API
 *
 */
public interface ArtifactResolver {

	public static final String CLASSIFIER_JAVADOC = "javadoc";
	public static final String CLASSIFIER_SOURCES = "sources";

	public static class Artifact {

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
			result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
			result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
			result = prime * result + ((version == null) ? 0 : version.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Artifact other = (Artifact) obj;
			if (artifactId == null) {
				if (other.artifactId != null)
					return false;
			} else if (!artifactId.equals(other.artifactId))
				return false;
			if (classifier == null) {
				if (other.classifier != null)
					return false;
			} else if (!classifier.equals(other.classifier))
				return false;
			if (groupId == null) {
				if (other.groupId != null)
					return false;
			} else if (!groupId.equals(other.groupId))
				return false;
			if (version == null) {
				if (other.version != null)
					return false;
			} else if (!version.equals(other.version))
				return false;
			return true;
		}

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

	Set<Artifact> getDependencies(Artifact artifact, IProgressMonitor monitor);
}