package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.item.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeClientPacket;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ScreenUtils;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class BloodVolumeOverlay {

	static ResourceLocation frame = Hemomancy.rloc("textures/gui/blood_bar.png");
	static ResourceLocation fill_texture = Hemomancy.rloc("textures/gui/blood_fill_tiled.png");
	static ResourceLocation memory_border = Hemomancy.rloc("textures/gui/memory_border.png");

	public static final IGuiOverlay HUD_BLOODVOLUME = ((gui, matrix, partialTick, width, height) -> {
		ClientLevel world = gui.getMinecraft().level;

		LocalPlayer playerIn = (LocalPlayer) HLClientUtils.getClientPlayer();
		if (playerIn != null) {
			playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(bloodCap -> {
				if (bloodCap != null) {
					if (bloodCap.isActive()) {
						playerIn.getCapability(RunesCapabilities.RUNES).ifPresent(inv -> {
							if (inv.getStackInSlot(4).getItem() instanceof VasculariumCharmItem charm) {
								double bloodVolume = 0;
								matrix.pose().pushPose();
								PacketHandler.CHANNELBLOODVOLUME.sendToServer(new BloodVolumeClientPacket());
								bloodVolume = bloodCap.getBloodVolume();
								bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);
								float newBarWidth = (int) ((bloodVolume) / 50) - 3;
								matrix.pose().popPose();

								float textureUShift = (world.getGameTime() * 0.25f % 256);
								float heightShift = (float) Math.cos(world.getGameTime() * 0.2) * 2;

//
//								matrix.pushPose();
//								RenderSystem.setShader(GameRenderer::getPositionTexShader);
//								RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1F);
//								RenderSystem.setShaderTexture(0, memory_border);
//								ScreenUtils.drawTexturedModalRect(matrix, 103, 45, 0, 0,256,144, 10);
//								matrix.popPose();

								// Fill
								matrix.pose().pushPose();
								RenderSystem.setShader(GameRenderer::getPositionTexShader);
								RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
								RenderSystem.setShaderTexture(0, fill_texture);
								matrix.pose().mulPose(new Quaternion(Vector3.ZN, 180, true).toMoj());
								ScreenUtils.drawTexturedModalRect(matrix, -10, -106, 22 + (int) textureUShift, 0, 6,
										(int) newBarWidth + (int) heightShift, heightShift);
								matrix.pose().popPose();

								// Frame
								matrix.pose().pushPose();
								RenderSystem.setShader(GameRenderer::getPositionTexShader);
								RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
								RenderSystem.setShaderTexture(0, frame); // Cap
								ScreenUtils.drawTexturedModalRect(matrix, 1, 0 + 106, 9, 244, 13, 12, heightShift);
								ScreenUtils.drawTexturedModalRect(matrix, 1, 0, 1, 0, 12, 106, heightShift);
								matrix.pose().popPose();
							}
						});
					}
				}
			});
		}
	});
}
