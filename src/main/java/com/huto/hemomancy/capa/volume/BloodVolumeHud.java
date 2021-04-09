package com.huto.hemomancy.capa.volume;

import java.text.DecimalFormat;

import org.lwjgl.opengl.GL11;

import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeClient;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BloodVolumeHud extends Screen {

	public static float bloodVolume = 0;
	private Minecraft mc;
	ClientPlayerEntity player;

	public BloodVolumeHud(ClientPlayerEntity playerIn, Minecraft mcI) {
		super(new StringTextComponent(""));
		this.player = playerIn;
		this.mc = mcI;

	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);

	}

	/* This helper method will render the bar */
	public void renderStatusBar(MatrixStack matrix, int screenWidth, int screenHeight, World world,
			ClientPlayerEntity playerIn) {
		if (playerIn != null) {
			IBloodVolume bloodCap = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(IllegalArgumentException::new);
			if (bloodCap != null) {
				PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketBloodVolumeClient());
				bloodVolume = bloodCap.getBloodVolume();
				bloodVolume = 0.01f*(float) Math.floor(bloodVolume * 100.0);
				String m = String.valueOf(bloodVolume);
				DecimalFormat d = new DecimalFormat("0.0");

				FontRenderer fr = mc.fontRenderer;
				final int vanillaExpLeftX = screenWidth / 2 - 91; // leftmost edge of the experience bar
				final int vanillaExpTopY = screenHeight - 9; // top of the experience bar

				GL11.glPushMatrix();
				GL11.glTranslatef(vanillaExpLeftX + 320, vanillaExpTopY - 8, 0);

				GL11.glPushMatrix();
				// "Blood Volume"
				GL11.glTranslatef(-50, 30, 0);
				fr.drawString(matrix, "Blood Volume: " + m, -fr.getStringWidth(d.format(bloodVolume)) - 46, -30,
						0x000000);
				fr.drawString(matrix, "Blood Volume: " + m, -fr.getStringWidth(d.format(bloodVolume)) - 47, -30,
						0xFFFFFF);
				GL11.glPopMatrix();

				GL11.glPopMatrix();

			}
		}
	}
}
