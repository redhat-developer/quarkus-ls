package org.acme.sample;

public class SomeService {

	record RunnerState(Status status) {

		public enum Status {
			dead, alive, saved, inactive
		}
	}

}
