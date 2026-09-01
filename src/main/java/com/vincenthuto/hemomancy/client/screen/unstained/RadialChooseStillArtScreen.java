package com.vincenthuto.hemomancy.client.screen.unstained;

import com.google.common.collect.Lists;
import com.vincenthuto.hemomancy.client.event.ClientEvents;
import com.vincenthuto.hemomancy.client.screen.radial.GenericRadialMenu;
import com.vincenthuto.hemomancy.client.screen.radial.IRadialMenuHost;
import com.vincenthuto.hemomancy.client.screen.radial.RadialMenuItem;
import com.vincenthuto.hemomancy.client.screen.radial.TextRadialMenuItem;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.unstained.UpdateSelectedStillArtPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RadialChooseStillArtScreen extends Screen {
	private final Minecraft mc;
	private final GenericRadialMenu menu;
	private boolean needsRecheckArts = true;
	private final List<TextRadialMenuItem> cachedMenuItems = Lists.newArrayList();

	public RadialChooseStillArtScreen() {
		super(Component.literal("STILL ARTS"));
		this.mc = Minecraft.getInstance();
		this.menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost() {
			@Override
			public void renderTooltip(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY) {
				graphics.renderTooltip(font, stack, mouseX, mouseY);
			}

			@Override
			public Screen getScreen() {
				return RadialChooseStillArtScreen.this;
			}

			@Override
			public Font getFontRenderer() {
				return font;
			}
		}) {
			@Override
			public void onClickOutside() {
				close();
			}
		};
		this.menu.backgroundColor = 0x4FDEEAF2;
		this.menu.backgroundColorHover = 0x7FFFFFFF;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		processClick();
		return super.mouseReleased(mouseX, mouseY, button);
	}

	private void processClick() {
		menu.clickItem();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		if (!canUseStillArts()) {
			Minecraft.getInstance().setScreen(null);
			return;
		}
		if (needsRecheckArts) {
			rebuildMenuItems();
		}
		if (cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible)) {
			menu.setCentralText(Component.translatable("hemomancy.still_art.radial.empty"));
		}
		menu.draw(graphics, partialTicks, mouseX, mouseY);
	}

	private void rebuildMenuItems() {
		cachedMenuItems.clear();
		if (mc.player == null) {
			menu.setCentralText(Component.empty());
			return;
		}

		HemoCapabilityAccess.getKnownStillArts(mc.player).ifPresent(known -> {
			StillArt selected = known.getSelectedArt();
			String selectedName = selected.getName();
			if (selected != StillArt.BLANK && selected.isUnlockedFor(mc.player)) {
				menu.setCentralText(Component.translatable("hemomancy.still_art.radial.selected",
						selected.getProperName()));
			} else {
				menu.setCentralText(Component.translatable("hemomancy.still_art.radial.prompt"));
			}
			for (StillArt art : known.getKnownArts()) {
				if (!art.isUnlockedFor(mc.player)) {
					continue;
				}
				TextRadialMenuItem item = new TextRadialMenuItem(menu,
						Component.literal(shortLabel(art)),
						art.getName().equals(selectedName) ? 0xFFFFFFFF : 0xFFBFEFFF) {
					@Override
					public boolean onClick() {
						PacketHandler.sendToServer(new UpdateSelectedStillArtPacket(art.getName()));
						RadialChooseStillArtScreen.this.menu.close();
						return true;
					}
				};
				MutableComponent central = Component.literal(art.getProperName());
				central.append(Component.literal("\n" + art.getRequiredStage().getTitle()));
				if (art.getName().equals(selectedName)) {
					central.append(Component.literal("\n"))
							.append(Component.translatable("hemomancy.still_art.radial.current"));
				}
				item.setCentralText(central);
				item.setVisible(true);
				cachedMenuItems.add(item);
			}
		});
		menu.clear();
		menu.addAll(cachedMenuItems);
		needsRecheckArts = false;
	}

	private static String shortLabel(StillArt art) {
		String[] parts = art.getProperName().split(" ");
		if (parts.length == 1) {
			return parts[0].substring(0, Math.min(3, parts[0].length()));
		}
		StringBuilder label = new StringBuilder();
		for (String part : parts) {
			if (!part.isEmpty()) {
				label.append(part.charAt(0));
			}
		}
		return label.toString();
	}

	private boolean canUseStillArts() {
		return mc.player != null && HemoCapabilityAccess.getUnstainedProgress(mc.player)
				.map(progress -> progress.hasClarityUnlocked())
				.orElse(false);
	}

	@Override
	public void tick() {
		super.tick();
		if (!canUseStillArts()) {
			Minecraft.getInstance().setScreen(null);
			return;
		}
		menu.tick();
		if (menu.isClosed()) {
			Minecraft.getInstance().setScreen(null);
		}
		if (!menu.isReady()) {
			return;
		}
		if (!ClientEvents.isKeyDown(ClientEvents.selectStillArt)) {
			processClick();
		}
	}
}
