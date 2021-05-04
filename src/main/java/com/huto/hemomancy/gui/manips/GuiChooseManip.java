package com.huto.hemomancy.gui.manips;

import java.awt.Point;
import java.util.List;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.manip.PacketUpdateCurrentManip;
import com.hutoslib.client.gui.GuiButtonTextured;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiChooseManip extends Screen {
	int left, top;
	static StringTextComponent titleComponent = new StringTextComponent("");
	Minecraft mc = Minecraft.getInstance();
	int centerX = (width / 2);
	int centerY = (height / 2);
	PlayerEntity player;
	final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/tendencybook_hidden.png");

	@OnlyIn(Dist.CLIENT)
	public GuiChooseManip(PlayerEntity clientPlayer) {
		super(titleComponent);
		this.player = clientPlayer;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		// this.renderBackground(matrixStack);
		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
			IKnownManipulations manips = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(NullPointerException::new);
			List<BloodManipulation> known = manips.getKnownManips();
			for (int j = 0; j < known.size(); j++) {
				if (i == j) {
					int xOff = font.getStringWidth(known.get(j).getProperName());
					if (buttons.get(i).isHovered()) {
						font.drawStringWithShadow(matrixStack, known.get(j).getProperName(),
								buttons.get(i).x - xOff / 2, (float) (buttons.get(i).y - 10
										+ Math.sin(getMinecraft().world.getGameTime() * 0.15) + partialTicks),
								0xffffff);
					} else {
						font.drawStringWithShadow(matrixStack, known.get(j).getProperName(),
								buttons.get(i).x - xOff / 2, (float) (buttons.get(i).y - 10), 0xffffff);
					}
				}
			}
		}

	}

	private static Point rotatePointAbout(Point in, Point about, double degrees) {
		double rad = degrees * Math.PI / 180.0;
		double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
		double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
		return new Point((int) newX, (int) newY);
	}

	@Override
	protected void init() {
		buttons.clear();
		IKnownManipulations manips = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(NullPointerException::new);
		BloodManipulation selected = manips.getSelectedManip();
		List<BloodManipulation> known = manips.getKnownManips();
		double angleBetweenEach = 360.0 / known.size();
		Point point = new Point(mc.getMainWindow().getScaledWidth() / 2 - 48, mc.getMainWindow().getScaledHeight() / 2),
				center = new Point(mc.getMainWindow().getScaledWidth() / 2, mc.getMainWindow().getScaledHeight() / 2);
		if (!known.isEmpty()) {
			for (int i = 0; i < known.size(); i++) {
				GlStateManager.pushMatrix();
				GlStateManager.enableAlphaTest();
				GlStateManager.enableBlend();
				BloodManipulation current = known.get(i);
				if (current.getProperName().equals(selected.getProperName())) {
					this.addButton(
							new GuiButtonTextured(texture, i, point.x, point.y, 16, 16, 225, 0, null, new IPressable() {
								@Override
								public void onPress(Button press) {
									if (press instanceof GuiButtonTextured) {
										player.playSound(SoundEvents.BLOCK_GLASS_PLACE, 0.40f, 1F);
										player.sendStatusMessage(
												new StringTextComponent("Manipulation Already Selected")
														.mergeStyle(TextFormatting.RED),
												true);
									}
									closeScreen();
								}
							}));
				} else {
					this.addButton(
							new GuiButtonTextured(texture, i, point.x, point.y, 16, 16, 209, 0, null, new IPressable() {

								@Override
								public void onPress(Button press) {
									if (press instanceof GuiButtonTextured) {
										player.playSound(SoundEvents.BLOCK_GLASS_PLACE, 0.40f, 1F);
										int id = ((GuiButtonTextured) press).getId();
										manips.setSelectedManip(known.get(id));
										PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new PacketUpdateCurrentManip(id));
									}
									closeScreen();
								}

							}));
				}

				GlStateManager.disableBlend();
				GlStateManager.disableAlphaTest();
				GlStateManager.popMatrix();
				point = rotatePointAbout(point, center, angleBetweenEach);
			}
		} else {
			closeScreen();
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
