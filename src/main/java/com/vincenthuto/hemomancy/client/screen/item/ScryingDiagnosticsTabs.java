package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ScryingDiagnosticsTabs {
	private ScryingDiagnosticsTabs() {
	}

	public static List<Tab> tabs() {
		return Arrays.asList(Tab.values());
	}

	public static List<ScreenDrawUtils.TabDesc> descriptors(Tab activeTab) {
		List<ScreenDrawUtils.TabDesc> descs = new ArrayList<>();
		for (Tab tab : Tab.values()) {
			descs.add(new ScreenDrawUtils.TabDesc(tab.label(), tab.color(), tab == activeTab));
		}
		return descs;
	}

	public enum Tab {
		BLOOD("Blood", 0xFFE15A5A),
		MANIPULATIONS("Manipulations", 0xFFE6C65C),
		TENDENCY("Tendency", 0xFF86D986);

		private final String label;
		private final int color;

		Tab(String label, int color) {
			this.label = label;
			this.color = color;
		}

		public String label() {
			return label;
		}

		public int color() {
			return color;
		}
	}
}
