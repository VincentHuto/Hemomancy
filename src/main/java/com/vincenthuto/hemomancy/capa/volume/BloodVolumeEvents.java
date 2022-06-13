package com.vincenthuto.hemomancy.capa.volume;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.item.tool.ItemBloodGourd;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.vincenthuto.hemomancy.tile.IBloodTile;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class BloodVolumeEvents {
	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			BloodVolumeProvider provider = new BloodVolumeProvider();
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "bloodvolume"), provider);
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
		if (event.getObject() instanceof IBloodTile) {
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
				 Component.translatable("Welcome! Blood Active? " + ChatFormatting.LIGHT_PURPLE + volume.isActive()), false);
		player.displayClientMessage(
				 Component.translatable(
						"Welcome! Current Blood Volume: " + ChatFormatting.GOLD + volume.getBloodVolume() + "ml"),
				false);
		player.displayClientMessage(
				 Component.translatable("Welcome! Current Bloodline: " + ChatFormatting.GOLD + volume.getBloodLine().name),
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
				 Component.translatable(
						"Welcome! Current Blood Volume: " + ChatFormatting.GOLD + volume.getBloodVolume() + "ml"),
				false);
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerRespawnEvent event) {
		Player playernew = event.getPlayer();
		if (!playernew.level.isClientSide) {
			IBloodVolume bloodVolumeNew = playernew.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(IllegalStateException::new);
			PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playernew),
					new PacketBloodVolumeServer(bloodVolumeNew.isActive(), bloodVolumeNew.getMaxBloodVolume(),
							bloodVolumeNew.getBloodVolume(), bloodVolumeNew.getBloodLine()));
		}
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			Player peorig = event.getOriginal();
			peorig.revive();
			IBloodVolume bloodVolumeOld = peorig.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(IllegalStateException::new);

			Player playernew = event.getPlayer();
			peorig.reviveCaps();

			IBloodVolume bloodVolumeNew = playernew.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(IllegalStateException::new);
			bloodVolumeNew.setActive(bloodVolumeOld.isActive());
			bloodVolumeNew.setBloodVolume(bloodVolumeOld.getBloodVolume());
			bloodVolumeNew.setMaxBloodVolume(bloodVolumeOld.getMaxBloodVolume());
			bloodVolumeNew.setBloodLine(bloodVolumeOld.getBloodLine());
			peorig.invalidateCaps();
		}

	}

//	private static Font fontRenderer;
//
//	@OnlyIn(Dist.CLIENT)
//	@SubscribeEvent(receiveCanceled = true)
//	public static void onRenderGameOverlay(RenderGameOverlayEvent.Chat event) {
//
//		if (fontRenderer == null) {
//			fontRenderer = Minecraft.getInstance().font;
//		}
//		Player player = Minecraft.getInstance().player;
//		if (player != null) {
//			if (player.isAlive()) {
//				// Redraws Icons so they dont get overwrote
//				Minecraft.getInstance().textureManager
//						.bindForSetup(new ResourceLocation("minecraft", "textures/gui/icons.png"));
//				// Coven color Overlay
//				IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
//				if (volume.isActive()) {
//					if (volume.getBloodVolume() < 250) {
//						RenderSystem.setShader(GameRenderer::getPositionTexShader);
//						RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//						double time = (double) Math.sin(player.level.getGameTime() * 0.05f);
//						RenderSystem.setShaderColor(1, 1, 1, 0.5f + time);
//						RenderSystem._setShaderTexture(0,
//								new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/blood_shot_overlay.png"));
//						event.getPoseStack().pushPose();
//						double ratio = (double) event.getWindow().getGuiScale();
//						event.getPoseStack().scale(1 / ratio, 1 / ratio, 1);
//						GuiComponent.blit(event.getPoseStack(), 0, 0, 0, 0, event.getWindow().getScreenWidth(),
//								event.getWindow().getScreenHeight(), event.getWindow().getScreenWidth(),
//								event.getWindow().getScreenHeight());
//						event.getPoseStack().popPose();
//						Minecraft.getInstance().textureManager
//								.bindForSetup(new ResourceLocation("minecraft", "textures/gui/icons.png"));
//					}
//				}
//			}
//		}
//	}
}
