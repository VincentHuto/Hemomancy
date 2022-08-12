package com.vincenthuto.hemomancy.capa.volume;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.BloodVolumeClientPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.GuiUtils;

@OnlyIn(Dist.CLIENT)
public class BloodVolumeHud extends Screen {

	public double bloodVolume = 0;
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

	/**
	 * Draws a textured rectangle at the current z-value. Ported From past Versions
	 */
	public static void drawFlippedTexturedModalRect(float x, float y, float textureX, float textureY, float width,
			float height) {
		/*
		 * float f = 0.00390625F; float f1 = 0.00390625F;
		 */
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		x = x + 10;
		// soon to be top right
		bufferbuilder.vertex(x + 13f, y + height - 13.25, 1)
				.uv((textureX + 0) * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
		// soon to be top left
		bufferbuilder.vertex(x + width, y + height - 13.25, 1)
				.uv((textureX + width) * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
		
		
		// soon to be bottom left
		bufferbuilder.vertex(x + width, y-6 , 1).uv((textureX + width) * 0.00390625F, (textureY + 0) * 0.00390625F)
				.endVertex();
		// now bottom right
		bufferbuilder.vertex(x + 13f, y-6 , 1).uv((textureX + 0) * 0.00390625F, (textureY + 0) * 0.00390625F)
				.endVertex();

		tessellator.end();
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

					matrix.pushPose();
					PacketHandler.CHANNELBLOODVOLUME.sendToServer(new BloodVolumeClientPacket());
					bloodVolume = bloodCap.getBloodVolume();
					bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);
					String m = String.valueOf(bloodVolume);
					float newBarWidth = (int) ((bloodVolume) / 50) - 3;
					Font fr = mc.font;
					ResourceLocation frame = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/blood_bar.png");
					ResourceLocation fill_texture = new ResourceLocation(Hemomancy.MOD_ID,
							"textures/gui/blood_fill_tiled.png");
					matrix.popPose();

					float textureUShift = (world.getGameTime() * 0.25f % 256);
					float heightShift = (float) Math.cos(world.getGameTime() * 0.1);
					
					
					//Fill
					matrix.pushPose();
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
					RenderSystem.setShaderTexture(0, fill_texture);
					matrix.mulPose(new Quaternion(Vector3f.ZP, 45, true));
					drawFlippedTexturedModalRect(-13, 113, 22 + textureUShift, textureUShift, 7f,
							2 + heightShift - newBarWidth);
					matrix.popPose();
	
					//Frame
					matrix.pushPose();
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
					RenderSystem.setShaderTexture(0, frame); // Cap
					GuiUtils.drawTexturedModalRect(matrix, 1, 0 + 106, 9, 244, 13, 12, heightShift);
					GuiUtils.drawTexturedModalRect(matrix, 1, 0, 1, 0, 12, 106, heightShift);
					matrix.popPose();
		

				}
			}
		}
	}
}
