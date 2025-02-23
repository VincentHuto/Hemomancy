package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeClientPacket;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.ScreenUtils;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class BloodVolumeOverlay {

	static ResourceLocation frame = Hemomancy.rloc("textures/gui/blood_bar.png");
	static ResourceLocation fill_texture = Hemomancy.rloc("textures/gui/blood_fill_tiled.png");
	static ResourceLocation memory_border = Hemomancy.rloc("textures/gui/memory_border.png");
	public static BloodVolumeOverlay instance;
	private Minecraft mc = Minecraft.getInstance();
	protected int screenWidth;
	protected int screenHeight;

	public void renderHUD(GuiGraphics pGuiGraphics, int width, int height, float partialTicks) {

		LocalPlayer player = this.mc.player;
		Font fr = this.mc.font;
		ClientLevel world = this.mc.level;

		if (player != null) {
			player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(bloodCap -> {
				if (bloodCap != null) {
					if (bloodCap.isActive()) {
						player.getCapability(RunesCapabilities.RUNES).ifPresent(inv -> {
							if (inv.getStackInSlot(5).getItem() instanceof VasculariumCharmItem charm) {
								RenderSystem.setShader(GameRenderer::getPositionTexShader);
								RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
								RenderSystem.setShaderTexture(0, fill_texture);
								var w = width;
								var h = height;
								double bloodVolume = 0;
								PacketHandler.CHANNELBLOODVOLUME.sendToServer(new BloodVolumeClientPacket());
								bloodVolume = bloodCap.getBloodVolume();
								bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);

								var positionLoc = HemoClientConfig.HUD_LOCATION.get();
								var posX = 0;
								var posY = 0;

								switch (positionLoc) {
								case 0:
									posX = 0;
									posY = 0;
									break;
								case 1:
									if (player.getActiveEffects().isEmpty()) {
										posX = w - 16;
										posY = 0;
									} else {
										posX = w - 16;
										posY = 52;
									}
									break;
								case 2:
									posX = 0;
									posY = h - 120;
									break;
								case 3:
									posX = w - 16;
									posY = h - 120;
									break;
								default:
									posX = 0;
									posY = 0;
								}
								this.renderBloodBar(pGuiGraphics, posX, posY, bloodCap, player, world, partialTicks);

							}
						});
					}
				}
			});
		}
	}

	private void renderBloodBar(GuiGraphics pGuiGraphics, int posX, int posY, IBloodVolume bloodVolume, Player player,
			ClientLevel world, float partialTicks) {
		Font fr = this.mc.font;

		// Fill
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
		RenderSystem.setShaderTexture(0, fill_texture);
		int newBarWidth =(int) (bloodVolume.getMaxBloodVolume()-bloodVolume.getBloodVolume())/50;
		float textureShift = (world.getGameTime() * 0.25f % 256);
		float surfaceWobble = (float) Math.cos(world.getGameTime() * 0.2) * 2;
		surfaceWobble=0;
		pGuiGraphics.blit(fill_texture, 
				posX +4,  posX + 10,  				//pX Coords
				(int) posY +10 +newBarWidth ,  posY +108, 				//pY Cords + (int) surfaceWobble
				 0,									//Blit offset for z ordering
				 6,  (int) 256-newBarWidth ,//pUwidth and pVheight + (int) surfaceWobble
				 22 + textureShift, textureShift,	//pUOffset and pVOffset
				 256,256);							//Texture width and height
		
		
		
		pGuiGraphics.drawString(fr, Component.literal(String.valueOf(bloodVolume.getBloodVolume())), posX -20, posY+8,
				0xffffff, true);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, frame); // Cap
		pGuiGraphics.blit(frame, posX + 1, posY + 106, 9, 244, 13, 12);
		pGuiGraphics.blit(frame, posX + 1, posY, 1, 0, 12, 106);

	}

}
