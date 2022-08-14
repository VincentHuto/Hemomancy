package com.vincenthuto.hemomancy.gui.radial;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.charm.CharmFinder;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.event.RadialClientEvents;
import com.vincenthuto.hemomancy.gui.radial.item.BlitRadialMenuItem;
import com.vincenthuto.hemomancy.gui.radial.item.RadialMenuItem;
import com.vincenthuto.hemomancy.init.KeyBindInit;
import com.vincenthuto.hemomancy.init.ManipulationInit;
import com.vincenthuto.hemomancy.item.VasculariumCharmItem;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.UpdateCurrentManipPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RadialCharmScreen extends Screen {
	private ItemStack stackEquipped;
	private IItemHandler inventory;
	private Minecraft mc;

	private boolean keyCycleBeforeL = false;
	private boolean keyCycleBeforeR = false;

	private boolean needsRecheckStacks = true;
	private final List<BlitRadialMenuItem> cachedMenuItems = Lists.newArrayList();
	private final GenericRadialMenu menu;

	private ItemRenderer getItemRenderer() {
		return itemRenderer;
	}

	public RadialCharmScreen(ItemStack getter) {
		super(new TextComponent("RADIAL MENU"));

		this.mc = Minecraft.getInstance();

		this.stackEquipped = getter;
		inventory = stackEquipped.getCount() > 0
				? stackEquipped.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(null)
				: null;
		menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost() {
			@Override
			public void renderTooltip(PoseStack matrixStack, ItemStack stack, int mouseX, int mouseY) {
				RadialCharmScreen.this.renderTooltip(matrixStack, stack, mouseX, mouseY);
			}

			@Override
			public void renderTooltip(PoseStack matrixStack, Component component, int mouseX, int mouseY) {
				RadialCharmScreen.this.renderTooltip(matrixStack, component, mouseX, mouseY);
			}

			@Override
			public Screen getScreen() {
				return RadialCharmScreen.this;
			}

			@Override
			public Font getFontRenderer() {
				return font;
			}

			@Override
			public ItemRenderer getItemRenderer() {
				return RadialCharmScreen.this.getItemRenderer();
			}

		}) {
			@Override
			public void onClickOutside() {
				close();
			}
		};
	}

	@SubscribeEvent
	public static void overlayEvent(RenderGameOverlayEvent.Pre event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.LAYER) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof RadialCharmScreen) {
			event.setCanceled(true);
		}
	}

	@Override // removed
	public void removed() {
		super.removed();
		RadialClientEvents.wipeOpen();
	}

	@Override // tick
	public void tick() {
		super.tick();
		menu.tick();
		if (menu.isClosed()) {
			Minecraft.getInstance().setScreen(null);
			RadialClientEvents.wipeOpen();
		}
		if (!menu.isReady()) {
			return;
		}
		if (!(stackEquipped.getItem() instanceof VasculariumCharmItem)) {
			Minecraft.getInstance().setScreen(null);
		}
		if (!RadialClientEvents.isKeyDown(KeyBindInit.openVascCharmMenu)) {
			this.processClick(false);
		}
	}

	@Override // mouseReleased
	public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
		processClick(true);
		return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
	}

	protected void processClick(boolean triggeredByMouse) {
		menu.clickItem();
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if (this.needsRecheckStacks) {
			this.cachedMenuItems.clear();
			IBloodVolume volCap = mc.player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			IKnownManipulations manips = mc.player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(NullPointerException::new);

			for (int i = 0; i < manips.getKnownManips().size(); i++) {
				BloodManipulation c = (BloodManipulation) manips.getKnownManips().keySet().toArray()[i];
				BlitRadialMenuItem item = new BlitRadialMenuItem(this.menu, i,
						new ResourceLocation(Hemomancy.MOD_ID, "textures/item/memory_" + c.getName() + ".png"), 0, 0,
						16, 16, 16, 16, new TranslatableComponent(c.getProperName())) {
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
			this.menu.setCentralText((Component) new TranslatableComponent("No Known Manipulations")
					.append(new TranslatableComponent("\ntest")));
		} else {
			this.menu.setCentralText(null);
		}
		this.menu.draw(matrixStack, partialTicks, mouseX, mouseY);
	}

	@Override // isPauseScreen
	public boolean isPauseScreen() {
		return false;
	}
}
