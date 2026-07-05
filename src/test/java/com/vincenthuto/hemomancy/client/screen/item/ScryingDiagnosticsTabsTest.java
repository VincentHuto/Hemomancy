package com.vincenthuto.hemomancy.client.screen.item;

import java.util.List;

public final class ScryingDiagnosticsTabsTest {
	private ScryingDiagnosticsTabsTest() {
	}

	public static void main(String[] args) {
		tabsUseRequestedOrderAndLabels();
		tabDescriptorsMarkOnlyTheActiveTab();
	}

	private static void tabsUseRequestedOrderAndLabels() {
		List<ScryingDiagnosticsTabs.Tab> tabs = ScryingDiagnosticsTabs.tabs();

		assertEquals(3, tabs.size(), "tab count");
		assertEquals(ScryingDiagnosticsTabs.Tab.BLOOD, tabs.get(0), "first tab");
		assertEquals("Blood", tabs.get(0).label(), "blood label");
		assertEquals(ScryingDiagnosticsTabs.Tab.MANIPULATIONS, tabs.get(1), "second tab");
		assertEquals("Manipulations", tabs.get(1).label(), "manipulations label");
		assertEquals(ScryingDiagnosticsTabs.Tab.TENDENCY, tabs.get(2), "third tab");
		assertEquals("Tendency", tabs.get(2).label(), "tendency label");
	}

	private static void tabDescriptorsMarkOnlyTheActiveTab() {
		var descs = ScryingDiagnosticsTabs.descriptors(ScryingDiagnosticsTabs.Tab.MANIPULATIONS);

		assertEquals(3, descs.size(), "descriptor count");
		assertEquals(false, descs.get(0).active(), "blood inactive");
		assertEquals(true, descs.get(1).active(), "manipulations active");
		assertEquals(false, descs.get(2).active(), "tendency inactive");
		assertEquals("Manipulations", descs.get(1).label(), "active label");
	}

	private static void assertEquals(Object expected, Object actual, String label) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
