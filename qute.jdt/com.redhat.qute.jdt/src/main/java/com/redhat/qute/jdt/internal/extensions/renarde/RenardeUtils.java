package com.redhat.qute.jdt.internal.extensions.renarde;

import org.eclipse.jdt.core.IJavaProject;

import com.redhat.qute.jdt.utils.JDTTypeUtils;

public class RenardeUtils {

	public static boolean isRenardeProject(IJavaProject javaProject) {
		return JDTTypeUtils.findType(javaProject, RenardeJavaConstants.RENARDE_CONTROLLER_TYPE) != null;
	}
}
