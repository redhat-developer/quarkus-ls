package com.redhat.qute.jdt.internal.extensions.renarde;

import static com.redhat.qute.jdt.internal.extensions.renarde.RenardeUtils.isRenardeProject;

import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.jdt.template.project.IProjectFeatureProvider;

public class RenardeProjectFeatureProvider implements IProjectFeatureProvider {

	@Override
	public void collectProjectFeatures(IJavaProject javaProject, Set<ProjectFeature> projectFeatures) {
		if (isRenardeProject(javaProject)) {
			projectFeatures.add(ProjectFeature.Renarde);
		}

	}

}
