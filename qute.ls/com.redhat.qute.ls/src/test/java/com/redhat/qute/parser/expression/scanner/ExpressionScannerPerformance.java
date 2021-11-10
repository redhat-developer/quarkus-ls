/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.expression.scanner;

import com.redhat.qute.parser.scanner.Scanner;

/**
 * This utility class is used to check the memory usage of {@link XMLScanner},
 * loading the large nasa.xml file
 * 
 * @author Angelo ZERR
 *
 */
public class ExpressionScannerPerformance {

	public static void main(String[] args) {
		String text = "foo.bar";
		// Continuously parses the large nasa.xml file with the XML scanner
		//while (true) {
			long start = System.currentTimeMillis();
			Scanner<TokenType, ScannerState> scanner = ExpressionScanner.createScanner(text);
			TokenType token = scan(scanner);
			while (token != TokenType.EOS) {
				token = scan(scanner);
			}
			System.err.println("Parsed in " + (System.currentTimeMillis() - start) + " ms.");
		//}
	}

	private static TokenType scan(Scanner<TokenType, ScannerState> scanner) {
		TokenType token = scanner.scan();
		System.err.println(scanner.getTokenType() + ": " + scanner.getTokenText());
		return token;
	}
}
