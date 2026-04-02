package com.vincenthuto.hemomancy.common.capability.player.kinship;

import java.awt.Color;
import java.awt.Point;
import java.util.Map;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodTendencyServerPacket;
import com.vincenthuto.hemomancy.common.tile.VisceralRecallerBlockEntity;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.math.MathUtils;

//GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodTendencyEvents {
	private static Font fontRenderer;

	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(Hemomancy.rloc("bloodtendency"), new BloodTendencyProvider());
		}
	}

	@SubscribeEvent
	public static void attachCapabilitiesTile(final AttachCapabilitiesEvent<BlockEntity> event) {
		if (event.getObject() instanceof VisceralRecallerBlockEntity) {
			event.addCapability(Hemomancy.rloc("bloodtendency"), new BloodTendencyProvider());
		}
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumBloodTendency, Float> BloodTendency = BloodTendencyProvider.getPlayerTendency(player);
		PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> player),
				new BloodTendencyServerPacket(BloodTendency));
	}


	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {

		Player peorig = event.getOriginal();
		Player playernew = event.getEntity();
		if (event.isWasDeath()) {
			peorig.reviveCaps();
			IBloodTendency bloodTendencyNew = playernew.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
					.orElseThrow(IllegalStateException::new);
			bloodTendencyNew.setTendency(peorig.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
					.orElseThrow(IllegalArgumentException::new).getTendency());
			peorig.invalidateCaps();
		}

	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		Map<EnumBloodTendency, Float> BloodTendency = BloodTendencyProvider.getPlayerTendency(player);
		PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> player),
				new BloodTendencyServerPacket(BloodTendency));
//		player.displayClientMessage(
//				Component.literal("Welcome! Current Blood Tendency: " + ChatFormatting.GOLD + BloodTendency), false);
	}

	@SubscribeEvent
	public static void respawn(PlayerRespawnEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = event.getEntity();
			if (!player.getCommandSenderWorld().isClientSide) {
				IBloodTendency tendency = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				PacketHandler.CHANNELBLOODTENDENCY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
						new BloodTendencyServerPacket(tendency.getTendency()));
			}
		}
	}

}