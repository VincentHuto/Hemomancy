package com.huto.hemomancy.capa.volume;

import java.text.DecimalFormat;

import org.lwjgl.opengl.GL11;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeClient;
import com.hutoslib.client.screen.GuiUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BloodVolumeHud extends Screen {

	public float bloodVolume = 0;
	private Minecraft mc;
	LocalPlayer player;

	public BloodVolumeHud(LocalPlayer playerIn, Minecraft mcI) {
		super(new TextComponent(""));
		this.player = playerIn;
		this.mc = mcI;

	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);

	}

	/* This helper method will render the bar */
	@SuppressWarnings("unused")
	public void renderStatusBar(PoseStack matrix, int screenWidth, int screenHeight, Level world,
			LocalPlayer playerIn) {

		if (playerIn != null) {
			IBloodVolume bloodCap = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(IllegalArgumentException::new);
			if (bloodCap != null) {
				if (bloodCap.isActive()) {
					PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketBloodVolumeClient());
					bloodVolume = bloodCap.getBloodVolume();
					bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);
					String m = String.valueOf(bloodVolume);
					DecimalFormat d = new DecimalFormat("0.0");
					float newBarWidth = (int) ((bloodVolume) / 50) - 3;
					Font fr = mc.font;
					ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/blood_bar.png");
					ResourceLocation fill_texture = new ResourceLocation(Hemomancy.MOD_ID,
							"textures/gui/blood_fill_tiled.png");
					mc.textureManager.bindForSetup(fill_texture);
					// RenderSystem.enableAlphaTest();
					float textureUShift = (world.getGameTime() * 0.25f % 256);
					float textureVShift = (world.getGameTime() * 0.25f % 256);
					float heightShift = (float) Math.cos(world.getGameTime() * 0.1);
					// float heightShift = 0;
					// Cap
					int xOff = (int) ((-screenWidth / 2) + (screenWidth / 2.15));
					int yOff = ((-screenHeight / 2));
					// Bar
					GL11.glPushMatrix();
					GL11.glRotatef(180, 0, 0, 0);
					// bar
					GuiUtils.drawTexturedModalRect(xOff + 2, yOff + 3, 23 + textureUShift, textureVShift, 6,
							(int) newBarWidth + 8 + heightShift);
					mc.textureManager.bindForSetup(texture);
					// Cap
					GuiUtils.drawTexturedModalRect(xOff - 1, yOff + 106, 9, 244, 13, 12);
					// Frame
					GuiUtils.drawTexturedModalRect(xOff - 1, yOff, 1, 0, 12, 106);

					GL11.glPopMatrix();
					GL11.glPushMatrix();
					// RenderSystem.disableAlphaTest();
					GL11.glPushMatrix();
					// "Blood Volume"
					GL11.glPushMatrix();
					GL11.glRotatef(90, 0, 0, -1);

					fr.draw(matrix, m, (float) (screenWidth / 2 - (screenWidth / 1.4)),
							screenHeight / 2 - screenHeight / 2.12f, 0xFFFFFF);

					GL11.glPopMatrix();

					GL11.glPopMatrix();
					GL11.glPopMatrix();
					Minecraft.getInstance().textureManager
							.bindForSetup(new ResourceLocation("minecraft", "textures/gui/icons.png"));
				}
			}
		}
	}
}
