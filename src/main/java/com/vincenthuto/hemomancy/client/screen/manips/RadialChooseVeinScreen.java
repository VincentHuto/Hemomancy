package com.vincenthuto.hemomancy.client.screen.manips;

import java.util.List;

import com.google.common.collect.Lists;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.ClientEvents;
import com.vincenthuto.hemomancy.client.screen.radial.BlitRadialMenuItem;
import com.vincenthuto.hemomancy.client.screen.radial.GenericRadialMenu;
import com.vincenthuto.hemomancy.client.screen.radial.IRadialMenuHost;
import com.vincenthuto.hemomancy.client.screen.radial.RadialMenuItem;
import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.TeleportToVeinPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RadialChooseVeinScreen extends Screen {

	final ResourceLocation selectTexture = Hemomancy.rloc("textures/gui/vein_selection.png");
	final ResourceLocation teleportTexture = Hemomancy.rloc("textures/gui/vein_teleport.png");

	@SubscribeEvent
	public static void overlayEvent(RenderGuiOverlayEvent.Pre event) {
		if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type())
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof RadialChooseVeinScreen) {
			event.setCanceled(true);
		}
	}

	private IRunesItemHandler inv;
	private Minecraft mc;
	private boolean needsRecheckStacks = true;
	private final List<BlitRadialMenuItem> cachedMenuItems = Lists.newArrayList();
	IKnownManipulations manips;
	private final GenericRadialMenu menu;

	public RadialChooseVeinScreen(IKnownManipulations manip) {
		super(Component.literal("RADIAL MENU"));
		this.mc = Minecraft.getInstance();
		manips = manip;

		menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost() {
			@Override
			public void renderTooltip(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY) {
				graphics.renderTooltip(font, stack, mouseX, mouseY);
			}

			@Override
			public Screen getScreen() {
				return RadialChooseVeinScreen.this;
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

			VeinLocation selected = manips.getSelectedVein();
			List<VeinLocation> known = manips.getVeinList();
			if (!known.isEmpty()) {
				for (int i = 0; i < known.size(); i++) {
					VeinLocation current = known.get(i);
					
					
					
					 
					BlitRadialMenuItem item = new BlitRadialMenuItem(this.menu, i, selectTexture, 0, 0, 16, 16, 16, 16,
							Component.literal(current.getName()+" [" + HLTextUtils.convertInitToLang(current.getDimension().getPath())+"]")) {
						@Override
						public boolean onClick() {
							mc.player.playSound(SoundEvents.PORTAL_TRAVEL, 0.20f, 0.1F);
							PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new TeleportToVeinPacket(current));
							RadialChooseVeinScreen.this.menu.close();
							return true;
						}
					};
					item.setVisible(true);
					this.cachedMenuItems.add(item);
				}
			}

			this.menu.clear();
			this.menu.addAll(this.cachedMenuItems);
			this.needsRecheckStacks = false;
		}
		if (this.cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible)) {
			this.menu.setCentralText(Component.literal("No Known Manipulations"));
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
		if (!(manips.getSelectedManip() != ManipulationInit.venous_travel.get())) {
			Minecraft.getInstance().setScreen(null);
		}
		if (!ClientEvents.isKeyDown(ClientEvents.useContManip)) {
			this.processClick(false);
		}
	}
}
