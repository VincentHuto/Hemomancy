package com.vincenthuto.hemomancy.gui.manips;

import java.awt.Point;
import java.awt.TextComponent;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.TeleportToVeinPacket;
import com.vincenthuto.hemomancy.network.capa.manips.UpdateCurrentVeinPacket;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.client.screen.GuiButtonTextured;
import com.vincenthuto.hutoslib.math.MathUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

public class ChooseVeinScreen extends Screen {
	int left, top;
	static MutableComponent titleComponent = Component.literal("");
	Minecraft mc = Minecraft.getInstance();
	int centerX = (width / 2);
	int centerY = (height / 2);
	final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/choose_vein.png");
	Player player = HLClientUtils.getClientPlayer();

	public ChooseVeinScreen() {
		super(titleComponent);
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		for (int i = 0; i < renderables.size(); i++) {
			renderables.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
			IKnownManipulations manips = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(NullPointerException::new);
			List<VeinLocation> known = manips.getVeinList();

			if (renderables.get(i)instanceof GuiButtonTextured telebutton) {
				if (telebutton.id == 69) {
					if (telebutton.isHoveredOrFocused()) {
						font.drawShadow(matrixStack, "Teleport To Selected", telebutton.x - 10 / 2, telebutton.y - 10,
								0xffffff);
					} else {

//						font.drawShadow(matrixStack,  level.get(j).getCurrentLevel() + "",
//								((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
//								(float) ((GuiButtonTextured) renderables.get(i)).y + 10, 0xffffff);
					}
				}
			}

			for (int j = 0; j < known.size(); j++) {
				if (i == j + 1) {
					// Hovered
					int xOff = font.width(known.get(j).getName());

					// eventually a config option
					boolean alwaysDisplayHover = false;
					if (alwaysDisplayHover) {
						font.drawShadow(matrixStack, known.get(j).getName(),
								((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
								(float) (((GuiButtonTextured) renderables.get(i)).y - 20
										+ Math.sin(getMinecraft().level.getGameTime() * 0.15) + partialTicks),
								0xffffff);

						font.drawShadow(matrixStack, HLTextUtils.convertInitToLang(known.get(j).getDimension().getPath()),
								((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
								(float) (((GuiButtonTextured) renderables.get(i)).y - 10
										+ Math.sin(getMinecraft().level.getGameTime() * 0.15) + partialTicks),
								0xffffff);

						font.drawShadow(matrixStack, known.get(j).getPosition().toShortString(),
								((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
								(float) (((GuiButtonTextured) renderables.get(i)).y
										+ Math.sin(getMinecraft().level.getGameTime() * 0.15) + partialTicks),
								0xffffff);
					} else {
						// is hovered
						if (((GuiButtonTextured) renderables.get(i)).isHoveredOrFocused()) {
							font.drawShadow(matrixStack, known.get(j).getName(),
									((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
									(float) (((GuiButtonTextured) renderables.get(i)).y - 20
											+ Math.sin(getMinecraft().level.getGameTime() * 0.15) + partialTicks),
									0xffffff);

							font.drawShadow(matrixStack,
									HLTextUtils.convertInitToLang(known.get(j).getDimension().getPath()),
									((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
									(float) (((GuiButtonTextured) renderables.get(i)).y - 10
											+ Math.sin(getMinecraft().level.getGameTime() * 0.15) + partialTicks),
									0xffffff);

							font.drawShadow(matrixStack, known.get(j).getPosition().toShortString(),
									((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
									(float) (((GuiButtonTextured) renderables.get(i)).y
											+ Math.sin(getMinecraft().level.getGameTime() * 0.15) + partialTicks),
									0xffffff);
						} else {
//							font.drawShadow(matrixStack, known.get(j).getName(),
//									((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
//									(float) ((GuiButtonTextured) renderables.get(i)).y - 20, 0xffffff);
//	
//							font.drawShadow(matrixStack, HLTextUtils.convertInitToLang(known.get(j).getDimension().getPath()),
//									((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
//									(float) ((GuiButtonTextured) renderables.get(i)).y - 10, 0xffffff);
//							
//							font.drawShadow(matrixStack,known.get(j).getPosition().toShortString(),
//									((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
//									(float) ((GuiButtonTextured) renderables.get(i)).y - 0, 0xffffff);
						}
					}
				}
			}
		}

	}

	@Override
	protected void init() {
		renderables.clear();
		IKnownManipulations manips = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(NullPointerException::new);
		VeinLocation selected = manips.getSelectedVein();
		List<VeinLocation> known = manips.getVeinList();
		double angleBetweenEach = 360.0 / known.size();
		Point point = new Point(mc.getWindow().getGuiScaledWidth() / 2 - 48, mc.getWindow().getGuiScaledHeight() / 2),
				center = new Point(mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 2);
		if (!known.isEmpty()) {

			this.addRenderableWidget(new GuiButtonTextured(texture, 69, mc.getWindow().getGuiScaledWidth() / 2,
					mc.getWindow().getGuiScaledHeight() / 2, 16, 16, 209, 32, null, (press) -> {
						if (press instanceof GuiButtonTextured) {
							player.playSound(SoundEvents.PORTAL_TRAVEL, 0.20f, 0.1F);
							PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new TeleportToVeinPacket());
						}
						onClose();

					}));

			for (int i = 0; i < known.size(); i++) {
				VeinLocation current = known.get(i);
				if (current.getName().equals(selected.getName())) {
					this.addRenderableWidget(
							new GuiButtonTextured(texture, i, point.x, point.y, 16, 16, 225, 0, null, (press) -> {
								if (press instanceof GuiButtonTextured) {
									player.playSound(SoundEvents.GLASS_PLACE, 0.40f, 1F);
									player.displayClientMessage(
											Component.literal("Vein Already Selected").withStyle(ChatFormatting.RED),
											true);
								}
								onClose();
							}));
				} else {
					this.addRenderableWidget(
							new GuiButtonTextured(texture, i, point.x, point.y, 16, 16, 209, 0, null, (press) -> {
								if (press instanceof GuiButtonTextured) {
									player.playSound(SoundEvents.GLASS_PLACE, 0.40f, 1F);
									int id = ((GuiButtonTextured) press).getId();
									manips.setSelectedVein(known.get(id));
									PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new UpdateCurrentVeinPacket(id));
								}
								onClose();

							}));
				}
				point = MathUtils.rotatePointAbout(point, center, angleBetweenEach);
			}
		} else {

			onClose();
		}

		super.init();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

}
