package com.redhat.qute.project.user_tags;

import java.util.Set;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.project.MockProjectQuteLanguageServer;
import com.redhat.qute.project.renarde.RenardeProject;

public class RenardeProjectQuteLanguageServer extends MockProjectQuteLanguageServer {

	public RenardeProjectQuteLanguageServer() {
		super(RenardeProject.PROJECT_URI, Set.of(ProjectFeature.Renarde));
	}

}
