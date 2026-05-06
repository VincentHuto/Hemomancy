package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Crimson Tithe — Canon Memory of Saint Hemorath.
 * Doctrine: Judgment / Silence
 *
 * Damage dealt is stored as blood volume. However, a periodic repayment
 * is demanded — miss it and suffer internal hemorrhage.
 *
 * Imprinted, not learned. The player uses it uncomfortably.
 */
public class CrimsonTitheManip extends BloodManipulation {

	private static final String TITHE_EXPIRY_KEY = "hemomancy:crimson_tithe_expiry";
	private static final String TITHE_STORED_KEY = "hemomancy:crimson_tithe_stored";
	private static final double BLOOD_STORE_AMOUNT = 500.0;
	private static final int REPAYMENT_WINDOW_TICKS = 600; // 30 seconds

	public CrimsonTitheManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		// Check if a repayment is already pending
		long currentTime = world.getGameTime();
		long titheExpiry = player.getPersistentData().getLong(TITHE_EXPIRY_KEY);

		if (titheExpiry > 0 && currentTime > titheExpiry) {
			// Missed repayment — internal hemorrhage
			applyHemorrhage(player, world);
			return;
		}

		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive()) {
			return;
		}

		// Store blood from damage dealt
		double toStore = Math.min(BLOOD_STORE_AMOUNT, volume.getMaxBloodVolume() - volume.getBloodVolume());
		volume.addBloodVolume(toStore);

		// Set repayment timer
		player.getPersistentData().putLong(TITHE_EXPIRY_KEY, currentTime + REPAYMENT_WINDOW_TICKS);
		player.getPersistentData().putDouble(TITHE_STORED_KEY, toStore);

		// Sync blood volume
		if (player instanceof ServerPlayer serverPlayer) {
			PacketHandler.sendToPlayer(serverPlayer, new BloodVolumeServerPacket(volume));
		}

		player.displayClientMessage(
				Component.literal("Blood stored. The tithe will be collected in 30 seconds.")
						.withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.ITALIC),
				true);

		world.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.8f, 1.2f);

		if (world instanceof ServerLevel sLevel) {
			sLevel.sendParticles(
					GlowParticleFactory.createData(new ParticleColor(180, 0, 0)),
					player.getX(), player.getY() + 1.0, player.getZ(),
					20, 0.3, 0.5, 0.3, 0.02);
		}
	}

	private void applyHemorrhage(Player player, Level world) {
		double storedAmount = player.getPersistentData().getDouble(TITHE_STORED_KEY);

		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume != null) {
			// Drain double what was stored as punishment
			volume.drain(storedAmount * 2.0);
			if (player instanceof ServerPlayer serverPlayer) {
				PacketHandler.sendToPlayer(serverPlayer, new BloodVolumeServerPacket(volume));
			}
		}

		player.hurt(player.damageSources().magic(), 6.0f);
		player.displayClientMessage(
				Component.literal("The tithe was not repaid. Hemorath collects with interest.")
						.withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD),
				false);

		// Clear tithe data
		player.getPersistentData().remove(TITHE_EXPIRY_KEY);
		player.getPersistentData().remove(TITHE_STORED_KEY);

		world.playSound(null, player.blockPosition(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.5f, 0.5f);
	}
}
