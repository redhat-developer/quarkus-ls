package com.redhat.qute.jdt.internal.extensions.roq;

import org.eclipse.jdt.core.IJavaProject;

import com.redhat.qute.jdt.utils.JDTTypeUtils;

public class RoqUtils {

	public static boolean isRoqProject(IJavaProject javaProject) {
		return JDTTypeUtils.findType(javaProject, RoqJavaConstants.SITE_CLASS) != null;
	}
}
