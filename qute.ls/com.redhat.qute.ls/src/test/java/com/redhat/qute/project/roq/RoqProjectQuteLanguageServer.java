package com.redhat.qute.project.roq;

import java.util.Set;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.project.MockProjectQuteLanguageServer;

public class RoqProjectQuteLanguageServer extends MockProjectQuteLanguageServer {

	public RoqProjectQuteLanguageServer() {
		super(RoqProject.PROJECT_URI, Set.of(ProjectFeature.Renarde));
	}

}
