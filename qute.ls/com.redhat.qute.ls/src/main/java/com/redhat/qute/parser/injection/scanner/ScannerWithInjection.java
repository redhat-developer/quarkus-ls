package com.redhat.qute.parser.injection.scanner;

import com.redhat.qute.parser.injection.InjectionMetadata;
import com.redhat.qute.parser.scanner.Scanner;

public interface ScannerWithInjection<T, S> extends Scanner<T, S> {

	InjectionMetadata getInjectionMetadata();

}
