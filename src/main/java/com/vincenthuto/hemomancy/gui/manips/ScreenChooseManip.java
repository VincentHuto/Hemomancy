package com.vincenthuto.hemomancy.gui.manips;

import java.awt.Point;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.PacketUpdateCurrentManip;
import com.vincenthuto.hutoslib.client.screen.GuiButtonTextured;
import com.vincenthuto.hutoslib.math.MathUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScreenChooseManip extends Screen {
	int left, top;
	static TextComponent titleComponent = new TextComponent("");
	Minecraft mc = Minecraft.getInstance();
	int centerX = (width / 2);
	int centerY = (height / 2);
	Player player;
	final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/tendencybook_hidden.png");

	public ScreenChooseManip(Player clientPlayer) {
		super(titleComponent);
		this.player = clientPlayer;
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		for (int i = 0; i < renderables.size(); i++) {
			renderables.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
			IKnownManipulations manips = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(NullPointerException::new);
			List<BloodManipulation> known = manips.getManipList();
			List<ManipLevel> level = manips.getLevelList();

			for (int j = 0; j < known.size(); j++) {
				if (i == j) {
					// Hovered
					int xOff = font.width(known.get(j).getProperName());

					// eventually a config option

					boolean alwaysDisplayHover = false;
					if (alwaysDisplayHover) {
						font.drawShadow(matrixStack, known.get(j).getProperName(),
								((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
								(float) ((GuiButtonTextured) renderables.get(i)).y - 10, 0xffffff);

						font.drawShadow(matrixStack, level.get(j).getCurrentLevel() + "",
								((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
								(float) ((GuiButtonTextured) renderables.get(i)).y + 10, 0xffffff);
					} else {

						if (((GuiButtonTextured) renderables.get(i)).isHoveredOrFocused()) {
							font.drawShadow(matrixStack, known.get(j).getProperName(),
									((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
									(float) (((GuiButtonTextured) renderables.get(i)).y - 10
											+ Math.sin(getMinecraft().level.getGameTime() * 0.15) + partialTicks),
									0xffffff);
							font.drawShadow(matrixStack, level.get(j).getCurrentLevel() + "",
									((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
									(float) (((GuiButtonTextured) renderables.get(i)).y + 10
											+ Math.sin(getMinecraft().level.getGameTime() * 0.15) + partialTicks),
									0xffffff);
						} else {
//							font.drawShadow(matrixStack, known.get(j).getProperName(),
//									((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
//									(float) ((GuiButtonTextured) renderables.get(i)).y - 10, 0xffffff);
//
//							font.drawShadow(matrixStack, level.get(j).getCurrentLevel() + "",
//									((GuiButtonTextured) renderables.get(i)).x - xOff / 2,
//									(float) ((GuiButtonTextured) renderables.get(i)).y + 10, 0xffffff);
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
		BloodManipulation selected = manips.getSelectedManip();
		List<BloodManipulation> known = manips.getManipList();
		double angleBetweenEach = 360.0 / known.size();
		Point point = new Point(mc.getWindow().getGuiScaledWidth() / 2 - 48, mc.getWindow().getGuiScaledHeight() / 2),
				center = new Point(mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 2);
		if (!known.isEmpty()) {
			for (int i = 0; i < known.size(); i++) {
				BloodManipulation current = known.get(i);
				if (current.getProperName().equals(selected.getProperName())) {
					this.addRenderableWidget(
							new GuiButtonTextured(texture, i, point.x, point.y, 16, 16, 225, 0, null, (press) -> {
								if (press instanceof GuiButtonTextured) {
									player.playSound(SoundEvents.GLASS_PLACE, 0.40f, 1F);
									player.displayClientMessage(new TextComponent("Manipulation Already Selected")
											.withStyle(ChatFormatting.RED), true);
								}
								onClose();
							}));
				} else {
					this.addRenderableWidget(
							new GuiButtonTextured(texture, i, point.x, point.y, 16, 16, 209, 0, null, (press) -> {
								if (press instanceof GuiButtonTextured) {
									player.playSound(SoundEvents.GLASS_PLACE, 0.40f, 1F);
									int id = ((GuiButtonTextured) press).getId();
									manips.setSelectedManip(known.get(id));
									PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new PacketUpdateCurrentManip(id));
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