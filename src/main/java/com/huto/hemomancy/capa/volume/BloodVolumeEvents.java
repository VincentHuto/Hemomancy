package com.huto.hemomancy.capa.volume;

import java.awt.Color;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.tool.ItemBloodGourd;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class BloodVolumeEvents {
	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof PlayerEntity) {
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "bloodvolume"), new BloodVolumeProvider());
		}
	}

	@SubscribeEvent
	public static void attachCapabilitiesItemStack(final AttachCapabilitiesEvent<ItemStack> event) {
		if (((ItemStack) event.getObject()).getItem() instanceof ItemBloodGourd) {
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "bloodvolume"), new BloodVolumeProvider());
		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> player),
				new PacketBloodVolumeServer(volume.getMaxBloodVolume(), volume.getBloodVolume()));
		player.sendStatusMessage(
				new StringTextComponent(
						"Welcome! Current Blood Volume: " + TextFormatting.GOLD + volume.getBloodVolume() + "ml"),
				false);
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> player),
				new PacketBloodVolumeServer(volume.getMaxBloodVolume(), volume.getBloodVolume()));
		player.sendStatusMessage(
				new StringTextComponent(
						"Welcome! Current Blood Volume: " + TextFormatting.GOLD + volume.getBloodVolume() + "ml"),
				false);
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		IBloodVolume bloodVolumeOld = event.getOriginal().getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(IllegalStateException::new);
		IBloodVolume bloodVolumeNew = event.getEntity().getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(IllegalStateException::new);
		if (bloodVolumeOld.getBloodVolume() > 20) {
			bloodVolumeNew.setBloodVolume(bloodVolumeOld.getBloodVolume() - 20f);
			((PlayerEntity) event.getEntity()).sendStatusMessage(
					new StringTextComponent(TextFormatting.ITALIC + "Upon death, your blood volume has decreased to: "
							+ TextFormatting.RED + TextFormatting.ITALIC + bloodVolumeNew.getBloodVolume() + "ml"),
					false);
		} else {
			bloodVolumeNew.setBloodVolume(bloodVolumeOld.getBloodVolume());

		}
	}

	@SubscribeEvent
	public static void regainBloodVolume(PlayerTickEvent e) {

		/*
		 * IBloodVolume bloodVolume =
		 * e.player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
		 * .orElseThrow(NullPointerException::new); if (bloodVolume.getBloodVolume() <
		 * 5000) { bloodVolume.addBloodVolume(0.5f); }
		 */
	}

	private static FontRenderer fontRenderer;

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(receiveCanceled = true)
	public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {

		if (fontRenderer == null) {
			fontRenderer = Minecraft.getInstance().fontRenderer;
		}
		PlayerEntity player = Minecraft.getInstance().player;
		if (player != null) {
			if (player.isAlive()) {
				// Redraws Icons so they dont get overwrote
				Minecraft.getInstance().textureManager
						.bindTexture(new ResourceLocation("minecraft", "textures/gui/icons.png"));
				// Coven color Overlay
				IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
				if (volume.getBloodVolume() < 0) {
					Minecraft.getInstance().textureManager
							.bindTexture(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/blood_shot_overlay.png"));
					event.getMatrixStack().push();
					float diffX = 1920 / event.getWindow().getWidth();
					float diffY = 1080 / event.getWindow().getHeight();
					event.getMatrixStack().scale((float) Math.pow(diffX, 1 / 3), (float) Math.pow(diffY, 1 / 3), 0.5f);
					/*
					 * AbstractGui.fill(event.getMatrixStack(), 0, 0, event.getWindow().getWidth(),
					 * event.getWindow().getHeight(), new Color(25, 0, 0, 26).getRGB());
					 */
					AbstractGui.blit(event.getMatrixStack(), 0, 0, 0, 0, event.getWindow().getWidth(),
							event.getWindow().getHeight(), (1920), (1080));
					fontRenderer.drawString(event.getMatrixStack(), "Blood Shot", event.getWindow().getWidth() / 2, 15,
							new Color(0, 0, 0, 0).getRGB());
					event.getMatrixStack().pop();
					Minecraft.getInstance().textureManager
							.bindTexture(new ResourceLocation("minecraft", "textures/gui/icons.png"));
				}
			}
		}
	}
}
