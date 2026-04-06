package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.SerpentParticleFactory;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
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
 * unwilling sacrifice processing, and player death during active rites.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CardinalRiteEvents {

	private static final float CASTER_BOUNDARY_DAMAGE_PER_TICK = 1.0f;
	private static final double CASTER_BOUNDARY_BLOOD_DRAIN_PER_TICK = 25.0;
	private static final float SACRIFICE_DAMAGE_PER_TICK = 0.5f;
	private static final int SACRIFICE_DAMAGE_INTERVAL = 10;
	private static final int PARTICLE_SPAWN_INTERVAL = 2;
	private static final int BOUNDARY_PARTICLE_INTERVAL = 3;
	private static final double BOUNDARY_WALL_HEIGHT = 3.0;
	private static final int BOUNDARY_WALL_STEPS = 6;

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel sLevel)) return;

		CardinalRiteSavedData savedData = CardinalRiteSavedData.get(sLevel);
		Map<UUID, ActiveCardinalRite> activeRites = savedData.getActiveRites();

		if (activeRites.isEmpty()) return;

		List<UUID> toRemove = new ArrayList<>();

		for (Map.Entry<UUID, ActiveCardinalRite> entry : activeRites.entrySet()) {
			UUID playerUUID = entry.getKey();
			ActiveCardinalRite rite = entry.getValue();

			ServerPlayer caster = sLevel.getServer().getPlayerList().getPlayer(playerUUID);

			if (caster == null || !caster.level().equals(sLevel)) {
				// Caster is offline or in a different dimension — stall the rite
				continue;
			}

			BlockPos center = rite.getCenterPos();
			int halfSize = rite.getRiteSize() / 2;
			AABB bounds = new AABB(center).inflate(halfSize);

			// === Caster boundary enforcement ===
			// Only the caster takes damage and blood drain for leaving the rite bounds
			if (!bounds.contains(caster.position())) {
				caster.hurt(caster.damageSources().generic(), CASTER_BOUNDARY_DAMAGE_PER_TICK);
				caster.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
					volume.drain(CASTER_BOUNDARY_BLOOD_DRAIN_PER_TICK);
					BloodVolumeEvents.syncVolume(caster, volume);
				});
				caster.displayClientMessage(
						Component.literal("The rite binds you! Return to the circle!")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
						true);
				// Don't tick the rite forward while the caster is outside
				continue;
			}

			// === Unwilling sacrifice processing ===
			// Non-caster living entities within bounds take damage and feed the ritual
			if (sLevel.getGameTime() % SACRIFICE_DAMAGE_INTERVAL == 0) {
				processSacrifices(sLevel, rite, caster, bounds);
			}

			// === Spawn helix particles ===
			if (sLevel.getGameTime() % PARTICLE_SPAWN_INTERVAL == 0) {
				spawnHelixParticles(sLevel, rite);
			}

			// === Spawn boundary wall particles ===
			if (sLevel.getGameTime() % BOUNDARY_PARTICLE_INTERVAL == 0) {
				spawnBoundaryParticles(sLevel, rite);
			}

			// Tick the rite
			rite.tick();
			savedData.setDirty();

			// Check if rite is complete
			if (rite.isComplete()) {
				completeRite(sLevel, caster, rite);
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

	/**
	 * Non-caster living entities inside the rite bounds are treated as unwilling
	 * sacrifices. They take damage and their life force feeds into the ritual,
	 * reducing remaining casting time. Bloodless entities (skeletons, golems, etc.)
	 * are not valid sacrifices.
	 */
	private static void processSacrifices(ServerLevel sLevel, ActiveCardinalRite rite, ServerPlayer caster,
			AABB bounds) {
		List<LivingEntity> entities = sLevel.getEntitiesOfClass(LivingEntity.class, bounds,
				entity -> entity != caster && entity.isAlive());

		boolean fedThisTick = false;
		for (LivingEntity entity : entities) {
			// Bloodless entities cannot feed the ritual
			if (HemoEntityPredicates.NOBLOOD.test(entity)) {
				continue;
			}

			entity.hurt(caster.damageSources().playerAttack(caster), SACRIFICE_DAMAGE_PER_TICK);

			// The first valid sacrifice each interval grants a bonus tick to speed the rite
			if (!fedThisTick && rite.getRemainingTicks() > 1) {
				rite.tick();
				fedThisTick = true;
			}

			// Draw blood particles from sacrifice toward the rite center
			BlockPos center = rite.getCenterPos();
			sLevel.sendParticles(
					BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
					entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ(),
					3, 0.1, 0.1, 0.1, 0.02);
			sLevel.sendParticles(
					SerpentParticleFactory.createData(new ParticleColor(200, 0, 0)),
					center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
					1, 0.1, 0.2, 0.1, 0);
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
	}

	/**
	 * Spawns a glowing red particle boundary around the rite area so players
	 * can clearly see the confinement zone. Draws vertical pillars at corners
	 * and connecting lines along each edge of the bounding box.
	 */
	private static void spawnBoundaryParticles(ServerLevel sLevel, ActiveCardinalRite rite) {
		BlockPos center = rite.getCenterPos();
		double halfSize = rite.getRiteSize() / 2.0;
		double cx = center.getX() + 0.5;
		double cy = center.getY();
		double cz = center.getZ() + 0.5;
		long gameTime = sLevel.getGameTime();

		// Pulsing brightness — oscillates between dim red and bright red
		double pulse = (Math.sin(gameTime * 0.1) + 1.0) * 0.5;
		int red = (int) (150 + 105 * pulse);

		// Four corners of the boundary
		double[][] corners = {
				{ cx - halfSize, cz - halfSize },
				{ cx + halfSize, cz - halfSize },
				{ cx + halfSize, cz + halfSize },
				{ cx - halfSize, cz + halfSize }
		};

		// Vertical pillars at each corner
		for (double[] corner : corners) {
			for (int step = 0; step <= BOUNDARY_WALL_STEPS; step++) {
				double y = cy + (BOUNDARY_WALL_HEIGHT * step / BOUNDARY_WALL_STEPS);
				sLevel.sendParticles(
						BloodCellParticleFactory.createData(new ParticleColor(red, 0, 0)),
						corner[0], y, corner[1], 1, 0.01, 0.01, 0.01, 0);
			}
		}

		// Horizontal connecting lines along the edges (at base, mid, and top)
		double[] edgeHeights = { cy + 0.1, cy + BOUNDARY_WALL_HEIGHT / 2.0, cy + BOUNDARY_WALL_HEIGHT };
		int edgeParticles = Math.max(4, rite.getRiteSize());
		for (int i = 0; i < 4; i++) {
			double[] from = corners[i];
			double[] to = corners[(i + 1) % 4];
			for (double edgeY : edgeHeights) {
				for (int p = 0; p <= edgeParticles; p++) {
					double t = (double) p / edgeParticles;
					double ex = from[0] + (to[0] - from[0]) * t;
					double ez = from[1] + (to[1] - from[1]) * t;
					sLevel.sendParticles(
							BloodCellParticleFactory.createData(new ParticleColor(red, 0, 0)),
							ex, edgeY, ez, 1, 0.01, 0.01, 0.01, 0);
				}
			}
		}
	}

	private static void completeRite(ServerLevel sLevel, ServerPlayer caster, ActiveCardinalRite rite) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(sLevel, rite.getRecipeId());
		if (recipe == null) return;

		// Drain blood cost
		caster.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
			volume.drain(recipe.getBloodCost());
			BloodVolumeEvents.syncVolume(caster, volume);
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

		// Notify the caster
		caster.displayClientMessage(
				Component.literal("The " + recipe.getRiteName() + " is complete!")
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
				false);
	}
}
