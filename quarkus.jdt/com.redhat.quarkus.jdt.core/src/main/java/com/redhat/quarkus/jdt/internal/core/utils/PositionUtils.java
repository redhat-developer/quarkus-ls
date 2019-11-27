package com.redhat.quarkus.jdt.internal.core.utils;

import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Position;


public class PositionUtils {
	public static int positionToInteger(ISourceReference source, Position position) throws JavaModelException {
		String string = source.getSource();

		int newlineCount = position.getLine();
		int index = 0;

		while (newlineCount > 0 && index < string.length()) {
			if (String.valueOf(string.charAt(index)).equals(System.lineSeparator())) {
				newlineCount--;
			}

			index ++;
		}

		if (index >= string.length()) {
			return -1;
		}

		int intPosition = index + position.getCharacter();

		return intPosition < string.length() ? intPosition : -1;
	}

}