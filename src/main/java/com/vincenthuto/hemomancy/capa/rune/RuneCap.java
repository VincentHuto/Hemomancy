package com.vincenthuto.hemomancy.capa.rune;

import java.util.concurrent.Callable;

public class RuneCap {


	public static class IRuneFactory implements Callable<IRune> {

		@Override
		public IRune call() {
			return () -> RuneType.OVERRIDE;
		}
	}
}
