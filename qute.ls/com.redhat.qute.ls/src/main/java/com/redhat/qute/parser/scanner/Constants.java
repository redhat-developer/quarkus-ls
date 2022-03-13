/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.scanner;

import java.util.regex.Pattern;

public class Constants {

	public final static int _EXL = "!".codePointAt(0);
	public final static int _MIN = "-".codePointAt(0);
	public final static int _UDS = "_".codePointAt(0);
	public final static int _DDT = ":".codePointAt(0);
	public final static int _DOT = ".".codePointAt(0);
	public final static int _LAN = "<".codePointAt(0);
	public final static int _RAN = ">".codePointAt(0);
	public final static int _FSL = "/".codePointAt(0);
	public final static int _EQS = "=".codePointAt(0);
	public final static int _CMA = ",".codePointAt(0);
	public final static int _DQO = "\"".codePointAt(0);
	public final static int _SQO = "\"".codePointAt(0);
	public final static int _SIQ = "\'".codePointAt(0);
	public final static int _NWL = "\n".codePointAt(0);
	public final static int _CAR = "\r".codePointAt(0);
	public final static int _LFD = "\f".codePointAt(0);
	public final static int _WSP = " ".codePointAt(0);
	public final static int _TAB = "\t".codePointAt(0);
	public final static int _OSB = "[".codePointAt(0);
	public final static int _CSB = "]".codePointAt(0);
	public final static int _ORB = "(".codePointAt(0);
	public final static int _CRB = ")".codePointAt(0);
	public final static int _OCB = "{".codePointAt(0);
	public final static int _CCB = "}".codePointAt(0);
	public final static int _CVL = "C".codePointAt(0);
	public final static int _DVL = "D".codePointAt(0);
	public final static int _AVL = "A".codePointAt(0);
	public final static int _TVL = "T".codePointAt(0);
	public final static int _OVL = "O".codePointAt(0);
	public final static int _YVL = "Y".codePointAt(0);
	public final static int _PVL = "P".codePointAt(0);
	public final static int _EVL = "E".codePointAt(0);
	public final static int _LVL = "L".codePointAt(0);
	public final static int _MVL = "M".codePointAt(0);
	public final static int _NVL = "N".codePointAt(0);
	public final static int _IVL = "I".codePointAt(0);
	public final static int _SVL = "S".codePointAt(0);
	public final static int _QMA = "?".codePointAt(0);
	public final static int _XVL = "x".codePointAt(0);
	public final static int _mVL = "m".codePointAt(0);
	public final static int _lVL = "l".codePointAt(0);
	public final static int _PCT = "%".codePointAt(0);
	public final static int _AST = "*".codePointAt(0);
	public final static int _PLS = "+".codePointAt(0);

}
