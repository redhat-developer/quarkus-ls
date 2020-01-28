package com.redhat.microprofile.commons;

import java.util.List;

public class ProjectLabelInfoEntry {
	private final String uri;
	private final List<String> labels;

	public ProjectLabelInfoEntry(String uri, List<String> labels) {
		this.uri = uri;
		this.labels = labels;
	}

	public String getUri() {
		return uri;
	}

	public List<String> getLabels() {
		return labels;
	}
}