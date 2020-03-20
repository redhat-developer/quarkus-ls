/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.faulttolerance;

import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertHints;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertHintsDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.h;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.vh;

import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;
import com.redhat.microprofile.jdt.internal.faulttolerance.MicroProfileFaultToleranceConstants;

/**
 * Test collection of MicroProfile properties for MicroProfile Fault Tolerance
 * annotations
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileFaultToleranceTest extends BasePropertiesManagerTest {

	@Test
	public void microprofileFaultTolerance() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.microprofile_fault_tolerance, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,

				// <classname>/<annotation>/<parameter>
				p(null, "org.acme.MyClient/Retry/maxRetries", "int", " *  **Returns:**" + System.lineSeparator() + //
						"    " + System.lineSeparator() + //
						"     *  The max number of retries. -1 means retry forever. The value must be greater than or equal to -1.",
						false, "org.acme.MyClient", null, null, 0, "3"),

				// <classname>/<methodname>/<annotation>/<parameter>
				p(null, "org.acme.MyClient/serviceA/Retry/maxRetries", "int",
						" *  **Returns:**" + System.lineSeparator() + //
								"    " + System.lineSeparator() + //
								"     *  The max number of retries. -1 means retry forever. The value must be greater than or equal to -1.",
						false, "org.acme.MyClient", null, "serviceA()V", 0, "90"),

				p(null, "org.acme.MyClient/serviceA/Retry/delay", "long",
						"The delay between retries. Defaults to 0. The value must be greater than or equal to 0."
								+ System.lineSeparator() + //
								"" + System.lineSeparator() + //
								" *  **Returns:**" + System.lineSeparator() + //
								"    " + System.lineSeparator() + //
								"     *  the delay time",
						false, "org.acme.MyClient", null, "serviceA()V", 0, "0"),

				// <annotation>
				// -> <annotation>/enabled
				p(null, "Asynchronous/enabled", "boolean", "Enabling the policy", false,
						"org.eclipse.microprofile.faulttolerance.Asynchronous", null, null, 0, "true"),

				// <annotation>/<parameter>
				p(null, "Bulkhead/value", "int",
						"Specify the maximum number of concurrent calls to an instance. The value must be greater than 0. Otherwise, `org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException` occurs."
								+ System.lineSeparator() + //
								"" + System.lineSeparator() + //
								" *  **Returns:**" + System.lineSeparator() + //
								"    " + System.lineSeparator() + //
								"     *  the limit of the concurrent calls",
						false, "org.eclipse.microprofile.faulttolerance.Bulkhead", null, "value()I", 0, "10"),

				p(null, "MP_Fault_Tolerance_NonFallback_Enabled", "boolean",
						MicroProfileFaultToleranceConstants.MP_FAULT_TOLERANCE_NONFALLBACK_ENABLED_DESCRIPTION, false,
						null, null, null, 0, "false")

		);

		assertPropertiesDuplicate(infoFromClasspath);

		assertHints(infoFromClasspath, h("java.time.temporal.ChronoUnit", null, true, "java.time.temporal.ChronoUnit", //
				vh("NANOS", null, null), //
				vh("MICROS", null, null), //
				vh("MILLIS", null, null), //
				vh("SECONDS", null, null), //
				vh("MINUTES", null, null), //
				vh("HALF_DAYS", null, null), //
				vh("DAYS", null, null), //
				vh("WEEKS", null, null), //
				vh("MONTHS", null, null), //
				vh("YEARS", null, null), //
				vh("DECADES", null, null), //
				vh("CENTURIES", null, null), //
				vh("MILLENNIA", null, null), //
				vh("ERAS", null, null), //
				vh("FOREVER", null, null)) //
		);

		assertHintsDuplicate(infoFromClasspath);
	}

}
