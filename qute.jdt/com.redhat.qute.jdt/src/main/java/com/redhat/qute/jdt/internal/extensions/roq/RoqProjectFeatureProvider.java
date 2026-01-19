package com.redhat.qute.jdt.internal.extensions.roq;

import static com.redhat.qute.jdt.internal.extensions.roq.RoqUtils.isRoqProject;

import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.jdt.template.project.IProjectFeatureProvider;

public class RoqProjectFeatureProvider implements IProjectFeatureProvider {

	@Override
	public void collectProjectFeatures(IJavaProject javaProject, Set<ProjectFeature> projectFeatures) {
		if (isRoqProject(javaProject)) {
			projectFeatures.add(ProjectFeature.Roq);
		}
	}

}
