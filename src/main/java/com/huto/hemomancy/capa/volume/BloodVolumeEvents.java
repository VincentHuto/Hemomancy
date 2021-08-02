package com.huto.hemomancy.capa.volume;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.tool.ItemBloodGourd;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.huto.hemomancy.tile.BlockEntityVisceralRecaller;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class BloodVolumeEvents {
	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "bloodvolume"), new BloodVolumeProvider());
		}
	}

	@SubscribeEvent
	public static void attachCapabilitiesItemStack(final AttachCapabilitiesEvent<ItemStack> event) {
		if (event.getObject().getItem() instanceof ItemBloodGourd) {
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "bloodvolume"), new BloodVolumeProvider());

		}
	}

	@SubscribeEvent
	public static void attachCapabilitiesTile(final AttachCapabilitiesEvent<BlockEntity> event) {
		if (event.getObject() instanceof BlockEntityVisceralRecaller) {
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "bloodvolume"), new BloodVolumeProvider());

		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> player),
				new PacketBloodVolumeServer(volume));

		player.displayClientMessage(
				new TextComponent("Welcome! Blood Active? " + ChatFormatting.LIGHT_PURPLE + volume.isActive()), false);

		player.displayClientMessage(
				new TextComponent(
						"Welcome! Current Blood Volume: " + ChatFormatting.GOLD + volume.getBloodVolume() + "ml"),
				false);
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> player),
				new PacketBloodVolumeServer(volume));
		player.displayClientMessage(
				new TextComponent(
						"Welcome! Current Blood Volume: " + ChatFormatting.GOLD + volume.getBloodVolume() + "ml"),
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
			((Player) event.getEntity()).displayClientMessage(
					new TextComponent(ChatFormatting.ITALIC + "Upon death, your blood volume has decreased to: "
							+ ChatFormatting.RED + ChatFormatting.ITALIC + bloodVolumeNew.getBloodVolume() + "ml"),
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

	private static Font fontRenderer;

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(receiveCanceled = true)
	public static void onRenderGameOverlay(RenderGameOverlayEvent.Chat event) {

		if (fontRenderer == null) {
			fontRenderer = Minecraft.getInstance().font;
		}
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			if (player.isAlive()) {
				// Redraws Icons so they dont get overwrote
				Minecraft.getInstance().textureManager
						.bindForSetup(new ResourceLocation("minecraft", "textures/gui/icons.png"));
				// Coven color Overlay
				IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
				if (volume.isActive()) {
					if (volume.getBloodVolume() < 100) {
						Minecraft.getInstance().textureManager.bindForSetupForSetup(
								new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/blood_shot_overlay.png"));
						event.getMatrixStack().pushPose();
						/*
						 * //GlStateManager._pushMatrix(); //GlStateManager._color4f(100, 100, 100, 0.415f +
						 * (float) (Math.sin(player.level.getGameTime() * 0.055f) * 0.15f));
						 * //GlStateManager._popMatrix();
						 */
						float ratio = (float) event.getWindow().getGuiScale();
						event.getMatrixStack().scale(1 / ratio, 1 / ratio, 1);
						GuiComponent.blit(event.getMatrixStack(), 0, 0, 0, 0, event.getWindow().getScreenWidth(),
								event.getWindow().getScreenHeight(), event.getWindow().getScreenWidth(),
								event.getWindow().getScreenHeight());
						/*
						 * fontRenderer.drawString(event.getMatrixStack(), "Blood Shot",
						 * event.getWindow().getWidth() / 2, 15, new Color(0, 0, 0, 0).getRGB());
						 */
						event.getMatrixStack().popPose();
						Minecraft.getInstance().textureManager
								.bindForSetup(new ResourceLocation("minecraft", "textures/gui/icons.png"));
					}
				}
			}
		}
	}
}
