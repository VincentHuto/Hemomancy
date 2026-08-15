package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.gui.GuiGraphics;
import org.junit.jupiter.api.Test;

class ProgressTabRegistryTest {
	private enum Tab { SKILLS, MATERIALS }

	@Test
	void tabsAreConstructedAndInitializedOnlyWhenFirstActivated() {
		EnumMap<Tab, FakeTab> created = new EnumMap<>(Tab.class);
		ProgressTabRegistry<Tab> registry = new ProgressTabRegistry<>(Tab.class, tab -> {
			FakeTab controller = new FakeTab();
			created.put(tab, controller);
			return controller;
		});

		assertFalse(registry.isInstantiated(Tab.SKILLS));
		registry.activate(Tab.SKILLS, null);
		registry.activate(Tab.SKILLS, null);

		assertEquals(1, created.get(Tab.SKILLS).initializations);
		assertFalse(registry.isInstantiated(Tab.MATERIALS));
	}

	@Test
	void resizeReinitializesOnlyTheActiveTabAndCloseVisitsOnlyCreatedTabs() {
		EnumMap<Tab, FakeTab> created = new EnumMap<>(Tab.class);
		ProgressTabRegistry<Tab> registry = new ProgressTabRegistry<>(Tab.class, tab -> {
			FakeTab controller = new FakeTab();
			created.put(tab, controller);
			return controller;
		});
		registry.activate(Tab.SKILLS, null);
		registry.invalidateInitializations();

		registry.activate(Tab.SKILLS, null);
		registry.close();

		assertEquals(2, created.get(Tab.SKILLS).initializations);
		assertEquals(1, created.get(Tab.SKILLS).closes);
		assertFalse(created.containsKey(Tab.MATERIALS));
		assertTrue(registry.isInstantiated(Tab.SKILLS));
	}

	@Test
	void repeatedScreenLifecyclesCloseEveryInstantiatedController() {
		AtomicInteger closes = new AtomicInteger();
		for (int cycle = 0; cycle < 20; cycle++) {
			ProgressTabRegistry<Tab> registry = new ProgressTabRegistry<>(Tab.class, tab -> new FakeTab() {
				@Override public void onClose() {
					super.onClose();
					closes.incrementAndGet();
				}
			});
			registry.activate(Tab.SKILLS, null);
			registry.close();
		}

		assertEquals(20, closes.get());
	}

	private static class FakeTab implements IProgressTab {
		private int initializations;
		private int closes;

		@Override public void onInit(ProgressScreenContext ctx) { initializations++; }
		@Override public void onClose() { closes++; }
		@Override public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, float partial) {}
		@Override public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {}
		@Override public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {}
		@Override public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) { return false; }
		@Override public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) { return false; }
		@Override public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) { return false; }
		@Override public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) { return false; }
		@Override public PanZoomState getPanZoomState() { return null; }
	}
}
