package com.vincenthuto.hemomancy.gui.radial;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.container.CharmGourdMenu;
import com.vincenthuto.hemomancy.event.KeyBindEvents;
import com.vincenthuto.hemomancy.init.KeyBindInit;
import com.vincenthuto.hemomancy.item.VasculariumCharmItem;
import com.vincenthuto.hemomancy.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.UpdateCurrentManipPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RadialCharmScreen extends Screen {
	@SubscribeEvent
	public static void overlayEvent(RenderGuiOverlayEvent.Pre event) {
		if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type())
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof RadialCharmScreen) {
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

	public RadialCharmScreen(IRunesItemHandler invIn) {
		super(Component.literal("RADIAL MENU"));
		inv = invIn;
		this.mc = Minecraft.getInstance();

		this.vascCharmEquipped = inv.getStackInSlot(CharmGourdMenu.CHARM_SLOT_INDEX);
		if (inv.getStackInSlot(CharmGourdMenu.GOURD_SLOT_INDEX).getItem() instanceof BloodGourdItem gourd) {
			this.gourdEquipped = gourd;
		}

		menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost() {
			@Override
			public Font getFontRenderer() {
				return font;
			}

			@Override
			public ItemRenderer getItemRenderer() {
				return RadialCharmScreen.this.getItemRenderer();
			}

			@Override
			public Screen getScreen() {
				return RadialCharmScreen.this;
			}

			@Override
			public void renderTooltip(PoseStack matrixStack, Component component, int mouseX, int mouseY) {
				RadialCharmScreen.this.renderTooltip(matrixStack, component, mouseX, mouseY);
			}

			@Override
			public void renderTooltip(PoseStack matrixStack, ItemStack stack, int mouseX, int mouseY) {
				RadialCharmScreen.this.renderTooltip(matrixStack, stack, mouseX, mouseY);
			}

		}) {
			@Override
			public void onClickOutside() {
				close();
			}
		};
	}

	private ItemRenderer getItemRenderer() {
		return itemRenderer;
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
	public void removed() {
		super.removed();
		KeyBindEvents.wipeOpen();
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if (this.needsRecheckStacks) {
			this.cachedMenuItems.clear();

			IKnownManipulations manips = mc.player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(NullPointerException::new);

			for (int i = 0; i < manips.getKnownManips().size(); i++) {
				BloodManipulation c = (BloodManipulation) manips.getKnownManips().keySet().toArray()[i];
				BlitRadialMenuItem item = new BlitRadialMenuItem(this.menu, i,
						Hemomancy.rloc("textures/item/memory_" + c.getName() + ".png"), 0, 0,
						16, 16, 16, 16, Component.literal(c.getProperName())) {
					@Override
					public boolean onClick() {
						PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new UpdateCurrentManipPacket(getSlot()));

						RadialCharmScreen.this.menu.close();
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

			List<Component> textComponents = new ArrayList<>();
			textComponents.add(Component.literal("No Known Manipulations"));

			this.menu.setCentralText(textComponents);
		} else if (gourdEquipped != null) {

			List<Component> textComponents = new ArrayList<>();
			if (inv != null) {
				IBloodVolume bloodVolume = inv.getStackInSlot(CharmGourdMenu.GOURD_SLOT_INDEX)
						.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(NullPointerException::new);
				IBloodVolume volCap = mc.player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				textComponents.add(Component.literal(Double.toString(bloodVolume.getBloodVolume())));
				textComponents.add(Component.literal("Self: " + volCap.getBloodVolume() + "ml"));
				textComponents.add(Component.literal("Gourd: " + bloodVolume.getBloodVolume() + "ml"));

			}
			this.menu.setCentralText(textComponents);
		} else {
			this.menu.setCentralText(null);

		}
		this.menu.draw(matrixStack, partialTicks, mouseX, mouseY);
	}

	@Override // tick
	public void tick() {
		super.tick();
		menu.tick();
		if (menu.isClosed()) {
			Minecraft.getInstance().setScreen(null);
			KeyBindEvents.wipeOpen();
		}
		if (!menu.isReady()) {
			return;
		}
		if (!(vascCharmEquipped.getItem() instanceof VasculariumCharmItem)) {
			Minecraft.getInstance().setScreen(null);
		}
		if (!KeyBindEvents.isKeyDown(KeyBindInit.openVascCharmMenu)) {
			this.processClick(false);
		}
	}
}
