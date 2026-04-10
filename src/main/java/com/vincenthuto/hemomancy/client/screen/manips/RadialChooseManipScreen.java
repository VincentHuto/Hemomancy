package com.vincenthuto.hemomancy.client.screen.manips;

import java.util.List;

import com.google.common.collect.Lists;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.ClientEvents;
import com.vincenthuto.hemomancy.client.screen.radial.BlitRadialMenuItem;
import com.vincenthuto.hemomancy.client.screen.radial.GenericRadialMenu;
import com.vincenthuto.hemomancy.client.screen.radial.IRadialMenuHost;
import com.vincenthuto.hemomancy.client.screen.radial.RadialMenuItem;
import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.menu.CharmGourdMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.UpdateCurrentManipPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RadialChooseManipScreen extends Screen {
	@SubscribeEvent
	public static void overlayEvent(RenderGuiOverlayEvent.Pre event) {
		if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type())
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof RadialChooseManipScreen) {
			event.setCanceled(true);
		}
	}

	private ItemStack vascCharmEquipped;
	private BloodGourdItem gourdEquipped;
	private IRunesItemHandler inv;

	private Minecraft mc;
	private boolean needsRecheckStacks = true;
	private final List<BlitRadialMenuItem> cachedMenuItems = Lists.newArrayList();

	private final GenericRadialMenu menu;

	public RadialChooseManipScreen(IRunesItemHandler invIn) {
		super(Component.literal("RADIAL MENU"));
		inv = invIn;
		this.mc = Minecraft.getInstance();

		this.vascCharmEquipped = inv.getStackInSlot(CharmGourdMenu.CHARM_SLOT_INDEX);
		if (inv.getStackInSlot(CharmGourdMenu.GOURD_SLOT_INDEX).getItem() instanceof BloodGourdItem gourd) {
			this.gourdEquipped = gourd;
		}
		menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost() {
			@Override
			public void renderTooltip(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY) {
				graphics.renderTooltip(font, stack, mouseX, mouseY);
			}

			@Override
			public Screen getScreen() {
				return RadialChooseManipScreen.this;
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
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
		processClick(true);
		return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
	}

	protected void processClick(boolean triggeredByMouse) {
		menu.clickItem();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		if (this.needsRecheckStacks) {
			this.cachedMenuItems.clear();

			IKnownManipulations manips = mc.player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(NullPointerException::new);

			// Only show manipulations that are currently memorized (equipped) at the Mnemonic Reliquary
			List<BloodManipulation> allManips = manips.getManipList();
			List<String> equippedNames = manips.getEquippedManipNames();

			for (int i = 0; i < allManips.size(); i++) {
				BloodManipulation c = allManips.get(i);
				if (!equippedNames.contains(c.getName())) {
					continue;
				}
				final int slot = i; // index into the full known manip list for the packet
				BlitRadialMenuItem item = new BlitRadialMenuItem(this.menu, slot,
						Hemomancy.rloc("textures/item/memories/memory_" + c.getName() + "_overlay.png"),
						Hemomancy.rloc("textures/item/memories/memory_blank.png"),
						0, 0, 16, 16, 16, 16,
						Component.literal(c.getProperName())) {
					@Override
					public boolean onClick() {
						PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new UpdateCurrentManipPacket(slot));
						RadialChooseManipScreen.this.menu.close();
						return true;
					}
				};
				item.setVisible(true);
				this.cachedMenuItems.add(item);
			}
			this.menu.clear();
			this.menu.addAll(this.cachedMenuItems);
			this.needsRecheckStacks = false;
		}
		if (this.cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible)) {
			this.menu.setCentralText(Component.literal("No Memorized Manipulations"));
		} else if (gourdEquipped != null) {

			MutableComponent textComponents = MutableComponent.create(ComponentContents.EMPTY);
			if (inv != null) {
				IBloodVolume bloodVolume = inv.getStackInSlot(CharmGourdMenu.GOURD_SLOT_INDEX)
						.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(NullPointerException::new);
				IBloodVolume volCap = mc.player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				textComponents.append(Component.literal("Self: " + volCap.getBloodVolume() + "ml"));
				textComponents.append(Component.literal("Gourd: " + bloodVolume.getBloodVolume() + "ml"));

			}
			this.menu.setCentralText(textComponents);
		} else {
			this.menu.setCentralText(Component.empty());

		}
		this.menu.draw(graphics, partialTicks, mouseX, mouseY);
	}

	@Override // tick
	public void tick() {
		super.tick();
		menu.tick();
		if (menu.isClosed()) {
			Minecraft.getInstance().setScreen(null);
		}
		if (!menu.isReady()) {
			return;
		}
		if (!(vascCharmEquipped.getItem() instanceof VasculariumCharmItem)) {
			Minecraft.getInstance().setScreen(null);
		}
		if (!ClientEvents.isKeyDown(ClientEvents.openVascCharmMenu)) {
			this.processClick(false);
		}
	}
}
