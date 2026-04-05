package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server-side event handler for managing active cardinal rite casting.
 * Handles tick processing, particle spawning, boundary enforcement,
 * and player death during active rites.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CardinalRiteEvents {

	private static final float BOUNDARY_DAMAGE_PER_TICK = 1.0f;
	private static final double BOUNDARY_BLOOD_DRAIN_PER_TICK = 25.0;
	private static final int PARTICLE_SPAWN_INTERVAL = 2;

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel sLevel)) return;

		CardinalRiteSavedData savedData = CardinalRiteSavedData.get(sLevel);
		Map<UUID, ActiveCardinalRite> activeRites = savedData.getActiveRites();

		if (activeRites.isEmpty()) return;

		List<UUID> toRemove = new ArrayList<>();

		for (Iterator<Map.Entry<UUID, ActiveCardinalRite>> it = activeRites.entrySet().iterator(); it.hasNext();) {
			Map.Entry<UUID, ActiveCardinalRite> entry = it.next();
			UUID playerUUID = entry.getKey();
			ActiveCardinalRite rite = entry.getValue();

			ServerPlayer player = sLevel.getServer().getPlayerList().getPlayer(playerUUID);

			if (player == null || !player.level().equals(sLevel)) {
				// Player is offline or in a different dimension — stall the rite
				continue;
			}

			// Check if player is within rite bounds
			BlockPos center = rite.getCenterPos();
			int halfSize = rite.getRiteSize() / 2;
			AABB bounds = new AABB(center).inflate(halfSize);

			if (!bounds.contains(player.position())) {
				// Player left the rite bounds — damage and drain blood
				player.hurt(player.damageSources().generic(), BOUNDARY_DAMAGE_PER_TICK);
				player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
					volume.drain(BOUNDARY_BLOOD_DRAIN_PER_TICK);
					BloodVolumeEvents.syncVolume(player, volume);
				});
				player.displayClientMessage(
						Component.literal("The rite binds you! Return to the circle!")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
						true);
				// Don't tick the rite forward while the player is outside
				continue;
			}

			// Spawn helix particles
			if (sLevel.getGameTime() % PARTICLE_SPAWN_INTERVAL == 0) {
				spawnHelixParticles(sLevel, rite);
			}

			// Tick the rite
			rite.tick();
			savedData.setDirty();

			// Check if rite is complete
			if (rite.isComplete()) {
				completeRite(sLevel, player, rite);
				toRemove.add(playerUUID);
			}
		}

		for (UUID uuid : toRemove) {
			savedData.removeRite(uuid);
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (player.level().isClientSide) return;

		ServerLevel sLevel = (ServerLevel) player.level();
		CardinalRiteSavedData savedData = CardinalRiteSavedData.get(sLevel);

		if (savedData.hasActiveRite(player.getUUID())) {
			savedData.removeRite(player.getUUID());
			player.displayClientMessage(
					Component.literal("The rite has been broken by your death...")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
		}
	}

	private static void spawnHelixParticles(ServerLevel sLevel, ActiveCardinalRite rite) {
		BlockPos center = rite.getCenterPos();
		int elapsed = rite.getTotalTicks() - rite.getRemainingTicks();
		double progress = rite.getProgress();

		// Helix rises higher as the ritual progresses
		double maxHeight = 3.0 + rite.getRiteSize() * 0.5;
		double currentMaxHeight = maxHeight * progress;
		double time = elapsed * 0.15;

		// Spawn particles along two helical strands
		int particleCount = 8;
		for (int i = 0; i < particleCount; i++) {
			double heightFraction = (double) i / particleCount;
			double h = currentMaxHeight * heightFraction;

			for (int strand = 0; strand < 2; strand++) {
				double angle = time + h * 2.0 + strand * Math.PI;
				double radius = 0.5;
				double x = center.getX() + 0.5 + Math.cos(angle) * radius;
				double z = center.getZ() + 0.5 + Math.sin(angle) * radius;
				double y = center.getY() + 1.0 + h;

				sLevel.sendParticles(
						BloodCellParticleFactory.createData(new ParticleColor(200, 0, 0)),
						x, y, z, 1, 0.02, 0.02, 0.02, 0);
			}
		}

		// Also spawn a subtle ring at the base
		double baseRadius = rite.getRiteSize() / 2.0;
		for (float angle = 0; angle < 360; angle += 30) {
			double rad = Math.toRadians(angle + elapsed * 2.0);
			double bx = center.getX() + 0.5 + Math.cos(rad) * baseRadius;
			double bz = center.getZ() + 0.5 + Math.sin(rad) * baseRadius;
			double by = center.getY() + 0.1;

			sLevel.sendParticles(
					BloodCellParticleFactory.createData(new ParticleColor(150, 0, 0)),
					bx, by, bz, 1, 0.01, 0.01, 0.01, 0);
		}
	}

	private static void completeRite(ServerLevel sLevel, ServerPlayer player, ActiveCardinalRite rite) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(sLevel, rite.getRecipeId());
		if (recipe == null) return;

		// Drain blood cost
		player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
			volume.drain(recipe.getBloodCost());
			BloodVolumeEvents.syncVolume(player, volume);
		});

		// Destroy the multiblock pattern
		BlockPos center = rite.getCenterPos();
		BlockPattern blockPattern = recipe.getPattern().getBlockPattern();
		BlockPattern.BlockPatternMatch match = blockPattern.find(sLevel, center);
		if (match != null) {
			for (int i = 0; i < blockPattern.getWidth(); i++) {
				for (int j = 0; j < blockPattern.getHeight(); j++) {
					for (int k = 0; k < blockPattern.getDepth(); k++) {
						BlockInWorld cachedBlockInfo = match.getBlock(i, j, k);
						sLevel.setBlock(cachedBlockInfo.getPos(), Blocks.AIR.defaultBlockState(), 2);
						sLevel.levelEvent(2001, cachedBlockInfo.getPos(),
								Block.getId(cachedBlockInfo.getState()));
					}
				}
			}
		}

		// Spawn result item
		if (!recipe.getResult().isEmpty()) {
			sLevel.addFreshEntity(new ItemEntity(sLevel,
					center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5,
					recipe.getResult().copy()));
		}

		// Play completion sound
		sLevel.playSound(null, center, SoundEvents.ENDERMAN_SCREAM, SoundSource.BLOCKS, 1.0f, 0.5f);
		sLevel.playSound(null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);

		// Notify the player
		player.displayClientMessage(
				Component.literal("The " + recipe.getRiteName() + " is complete!")
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
				false);
	}
}
