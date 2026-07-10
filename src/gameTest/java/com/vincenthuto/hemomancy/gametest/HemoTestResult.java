package com.vincenthuto.hemomancy.gametest;

public record HemoTestResult(boolean passed, String message) {
	public static HemoTestResult pass(String message) {
		return new HemoTestResult(true, message);
	}

	public static HemoTestResult fail(String message) {
		return new HemoTestResult(false, message);
	}
}
