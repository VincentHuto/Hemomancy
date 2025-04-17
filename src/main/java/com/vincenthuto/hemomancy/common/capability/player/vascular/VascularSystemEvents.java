package com.vincenthuto.hemomancy.common.capability.player.vascular;

import java.awt.Color;
import java.awt.Point;
import java.util.Map;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.VascularSystemServerPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VascularSystemEvents {
	private static Font fontRenderer;

	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(Hemomancy.rloc("vascularsystem"), new VascularSystemProvider());
		}
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumVeinSections, Float> BloodFlow = VascularSystemProvider.getPlayerVascularSystem(player);
		PacketHandler.CHANNELVASCULARSYSTEM.send(PacketDistributor.PLAYER.with(() -> player),
				new VascularSystemServerPacket(BloodFlow));
//		player.displayClientMessage(
//				Component.literal("Welcome! Current Vascular System: " + ChatFormatting.GOLD + BloodFlow), false);
	}

	@SubscribeEvent
	public static void onPlayerDamage(LivingDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			if (!player.getCommandSenderWorld().isClientSide) {

				IVascularSystem system = player.getCapability(VascularSystemProvider.VASCULAR_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				for (EnumVeinSections section : system.getVascularSystem().keySet()) {

//					if (section != EnumVeinSections.HEART) {
//						system.setVascularSectionHealth(section, -player.level().random.nextFloat() * 3f);
//						PacketHandler.CHANNELVASCULARSYSTEM.send(
//								PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
//								new VascularSystemServerPacket(system.getVascularSystem()));
//					}
				}
			}
		}
	}

	@SuppressWarnings({ "deprecation", "unused" })
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(receiveCanceled = true)
	public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {

		if (fontRenderer == null) {
			fontRenderer = Minecraft.getInstance().font;
		}
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			if (player.isAlive()) {
				IVascularSystem section = player.getCapability(VascularSystemProvider.VASCULAR_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				ItemStack stack = player.getMainHandItem();
				Item item = stack.getItem();

				// Allegiance Identifier overlay
				if (item == ItemInit.dried_leech.get()) {
					Item renderItem = ItemInit.dried_leech.get();
					int centerX = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) - 6;
					int centerY = (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2) - 15;
					double angleBetweenEach = 360.0 / EnumVeinSections.values().length;
					Point point = new Point(centerX - 45, centerY - 36), center = new Point(centerX, centerY);
					for (int i = 0; i < section.getVascularSystem().keySet().size(); i++) {
						EnumVeinSections selectedSection = (EnumVeinSections) section.getVascularSystem().keySet()
								.toArray()[i];
						// GlStateManager._pushMatrix();

						event.getGuiGraphics().drawCenteredString(fontRenderer,
								HLTextUtils.toProperCase(selectedSection.toString()), point.x, point.y + 20,
								new Color(255, 0, 0, 255).getRGB());
						event.getGuiGraphics().drawCenteredString(fontRenderer,
								String.valueOf(section.getBloodFlowBySection(selectedSection)), point.x, point.y + 30,
								new Color(255, 0, 0, 255).getRGB());
						event.getGuiGraphics().renderItem(new ItemStack(renderItem), point.x, point.y);

						event.getGuiGraphics().renderItem(new ItemStack(renderItem), point.x, point.y);

						point = rotatePointAbout(point, center, angleBetweenEach);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		Player peorig = event.getOriginal();
		Player playernew = event.getEntity();
		if (event.isWasDeath()) {
			peorig.reviveCaps();
			IVascularSystem bloodVolumeNew = playernew.getCapability(VascularSystemProvider.VASCULAR_CAPA)
					.orElseThrow(IllegalStateException::new);
			bloodVolumeNew.setVascularSystem(peorig.getCapability(VascularSystemProvider.VASCULAR_CAPA)
					.orElseThrow(IllegalArgumentException::new).getVascularSystem());
			peorig.invalidateCaps();
		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumVeinSections, Float> BloodFlow = VascularSystemProvider.getPlayerVascularSystem(player);
		PacketHandler.CHANNELVASCULARSYSTEM.send(PacketDistributor.PLAYER.with(() -> player),
				new VascularSystemServerPacket(BloodFlow));
		player.displayClientMessage(
				Component.literal("Welcome! Current Vascular System: " + ChatFormatting.GOLD + BloodFlow), false);
	}

	@SubscribeEvent
	public static void respawn(PlayerRespawnEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = event.getEntity();
			if (!player.getCommandSenderWorld().isClientSide) {
				IVascularSystem section = player.getCapability(VascularSystemProvider.VASCULAR_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				PacketHandler.CHANNELVASCULARSYSTEM.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
						new VascularSystemServerPacket(section.getVascularSystem()));
			}
		}
	}

	private static Point rotatePointAbout(Point in, Point about, double degrees) {
		double rad = degrees * Math.PI / 180.0;
		double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
		double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
		return new Point((int) newX, (int) newY);
	}

}