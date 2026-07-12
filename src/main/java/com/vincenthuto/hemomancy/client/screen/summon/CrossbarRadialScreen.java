package com.vincenthuto.hemomancy.client.screen.summon;

import com.vincenthuto.hemomancy.client.event.ClientEvents;
import com.vincenthuto.hemomancy.client.screen.radial.GenericRadialMenu;
import com.vincenthuto.hemomancy.client.screen.radial.IRadialMenuHost;
import com.vincenthuto.hemomancy.client.screen.radial.TextRadialMenuItem;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.summon.PacketCrossbarRadialAction;
import com.vincenthuto.hemomancy.common.summon.PuppeteerCommandMode;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CrossbarRadialScreen extends Screen {
	private static final int SELECTED_COLOR = 0x9F7A0D0D;
	private static final int DISABLED_COLOR = 0x7F3F1010;

	private final ItemStack crossbar;
	private final UUID crossbarId;
	private final GenericRadialMenu menu;
	private boolean selectionHandled;

	public CrossbarRadialScreen(ItemStack crossbar, UUID crossbarId) {
		super(Component.translatable("screen.hemomancy.crossbar_radial.title"));
		this.crossbar = crossbar;
		this.crossbarId = crossbarId;
		this.menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost() {
			@Override public Screen getScreen() { return CrossbarRadialScreen.this; }
			@Override public Font getFontRenderer() { return font; }
			@Override public void renderTooltip(GuiGraphics graphics, ItemStack stack, int x, int y) {
				graphics.renderTooltip(font, stack, x, y);
			}
		}) {
			@Override public void onClickOutside() { close(); }
		};
		menu.setRadii(34, 70, 78, 108);
	}

	@Override
	protected void init() {
		super.init();
		menu.clear();
		buildItems();
	}

	private void buildItems() {
		List<TextRadialMenuItem> modes = new ArrayList<>();
		for (PuppeteerCommandMode mode : PuppeteerCommandMode.values()) {
			TextRadialMenuItem item = new TextRadialMenuItem(menu,
					Component.translatable("screen.hemomancy.crossbar_radial.mode." + mode.serializedName())) {
				@Override public boolean onClick() {
					PacketHandler.sendToServer(new PacketCrossbarRadialAction(crossbarId,
							PacketCrossbarRadialAction.Action.SET_MODE, mode.serializedName()));
					menu.close();
					return true;
				}
			};
			item.setCentralText(Component.translatable(
					"screen.hemomancy.crossbar_radial.mode." + mode.serializedName() + ".desc"));
			if (MarionetteCrossbarItem.getCommandMode(crossbar) == mode) item.setBackgroundColor(SELECTED_COLOR);
			item.setTextLayout(0.8f, 48);
			item.setVisible(true);
			modes.add(item);
		}
		menu.addAllInner(modes);

		if (minecraft != null && minecraft.player != null
				&& SkillPointHelper.getSkeinTranspositionLevel(minecraft.player) > 0) {
			List<TextRadialMenuItem> shapes = new ArrayList<>();
			for (PuppeteerSummonDefinition definition : PuppeteerSummonDefinitions.all()) {
				shapes.add(createShapeItem(definition));
			}
			menu.addAll(shapes);
		}
	}

	private TextRadialMenuItem createShapeItem(PuppeteerSummonDefinition definition) {
		String selected = MarionetteCrossbarItem.getSelectedSummonName(crossbar);
		Component reason = unavailableReason(definition, selected);
		boolean enabled = reason == null && !definition.name().equals(selected);
		TextRadialMenuItem item = new TextRadialMenuItem(menu, Component.translatable(definition.translationKey())) {
			@Override public boolean onClick() {
				if (!enabled) {
					menu.close();
					return false;
				}
				PacketHandler.sendToServer(new PacketCrossbarRadialAction(crossbarId,
						PacketCrossbarRadialAction.Action.HOT_SWAP, definition.name()));
				menu.close();
				return true;
			}
		};
		if (reason != null) {
			item.setCentralText(reason.copy());
			item.setBackgroundColor(DISABLED_COLOR);
		} else if (definition.name().equals(selected)) {
			item.setCentralText(Component.translatable("screen.hemomancy.crossbar_radial.current"));
			item.setBackgroundColor(SELECTED_COLOR);
		}
		item.setTextLayout(0.72f, 54);
		item.setVisible(true);
		return item;
	}

	private Component unavailableReason(PuppeteerSummonDefinition definition, String selected) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) return Component.translatable("screen.hemomancy.crossbar_radial.unavailable");
		boolean known = HemoCapabilityAccess.getKnownSummons(minecraft.player)
				.map(value -> value.isKnown(definition)).orElse(false);
		if (!known) return Component.translatable("screen.hemomancy.crossbar_radial.missing_trial");
		if (definition.name().equals(selected)) return null;
		int cost = MarionetteCrossbarItem.summonThreadCost(minecraft.player, definition);
		if (MarionetteCrossbarItem.getThread(crossbar) < cost) {
			return Component.translatable("screen.hemomancy.crossbar_radial.low_thread", cost);
		}
		List<Mob> active = MarionetteCrossbarItem.activeSummonsForOwner(minecraft.player);
		int shaped = (int) active.stream().filter(body -> !BoundSummonBehavior.isClaimedWill(body)).count();
		int matching = MarionetteCrossbarItem.activeSummonsForCrossbar(minecraft.player, crossbarId, selected).size();
		int projectedShaped = PuppeteerSummonRules.projectedShapedCount(shaped, matching, 1);
		int shapedCap = PuppeteerSummonRules.activeSummonCap(SkillPointHelper.getPuppetSkeinLevel(minecraft.player));
		int projectedTotal = active.size() - matching + 1;
		int totalCap = shapedCap + BoundSummonBehavior.claimedWillBonusCap(minecraft.player);
		if (projectedTotal > totalCap) {
			return Component.translatable("screen.hemomancy.crossbar_radial.total_cap", totalCap);
		}
		if (projectedShaped > shapedCap) {
			return Component.translatable("screen.hemomancy.crossbar_radial.cap", shapedCap);
		}
		return null;
	}

	@Override public boolean isPauseScreen() { return false; }

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		menu.setCentralText(Component.translatable("screen.hemomancy.crossbar_radial.center",
				MarionetteCrossbarItem.getThread(crossbar), MarionetteCrossbarItem.getThreadCapacity(crossbar),
				Component.translatable("entity.hemomancy." + MarionetteCrossbarItem.getSelectedSummonName(crossbar)),
				Component.translatable("screen.hemomancy.crossbar_radial.mode."
						+ MarionetteCrossbarItem.getCommandMode(crossbar).serializedName())));
		menu.draw(graphics, partialTick, mouseX, mouseY);
	}

	@Override public void tick() {
		super.tick();
		menu.tick();
		if (menu.isClosed()) Minecraft.getInstance().setScreen(null);
		else if (!ClientEvents.isKeyDown(Minecraft.getInstance().options.keyUse)) finishSelectionOnce();
	}

	@Override public boolean mouseReleased(double x, double y, int button) {
		finishSelectionOnce();
		return true;
	}

	private void finishSelectionOnce() {
		if (selectionHandled) return;
		selectionHandled = true;
		menu.clickItem();
	}

	@Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256) {
			selectionHandled = true;
			menu.close();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
