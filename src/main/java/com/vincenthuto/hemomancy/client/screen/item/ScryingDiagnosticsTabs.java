package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class ScryingDiagnosticsTabs {
	private ScryingDiagnosticsTabs() {
	}

	public static List<Tab> tabs() {
		return Arrays.asList(Tab.values());
	}

	public static List<ScreenDrawUtils.TabDesc> descriptors(Tab activeTab) {
		return descriptors(activeTab, Component::getString);
	}

	static List<ScreenDrawUtils.TabDesc> descriptors(Tab activeTab, Function<Component, String> resolve) {
		List<ScreenDrawUtils.TabDesc> descs = new ArrayList<>();
		for (Tab tab : Tab.values()) {
			descs.add(new ScreenDrawUtils.TabDesc(resolve.apply(tab.label()), tab.color(), tab == activeTab));
		}
		return descs;
	}

	public enum Tab {
		BLOOD("screen.hemomancy.scrying_diagnostics_tabs.blood", 0xFFE15A5A),
		MANIPULATIONS("screen.hemomancy.scrying_diagnostics_tabs.manipulations", 0xFFE6C65C),
		TENDENCY("screen.hemomancy.scrying_diagnostics_tabs.tendency", 0xFF86D986);

		private final String labelKey;
		private final int color;

		Tab(String labelKey, int color) {
			this.labelKey = labelKey;
			this.color = color;
		}

		public Component label() {
			return Component.translatable(labelKey);
		}

		public int color() {
			return color;
		}
	}
}
