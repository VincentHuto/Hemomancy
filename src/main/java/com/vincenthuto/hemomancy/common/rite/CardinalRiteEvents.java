package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.crafting.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.SerpentParticleFactory;
import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.capability.player.vascular.VascularSystemProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.dialogue.AncestralCommunionDialogueTrees;
import com.vincenthuto.hemomancy.common.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.bloodline.UnsignedLedgerItem;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncActiveRites;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

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
	private static final int RITE_SYNC_INTERVAL = 10;

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel sLevel)) return;

		// Tick pending blood structure crafts (delayed block breaking)
		PendingBloodCraftManager.tick();

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
			AABB bounds = new AABB(center).inflate(halfSize + 1); // +1 on each side = riteSize + 2 total buffer

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


			// Tick the rite
			rite.tick();
			savedData.setDirty();

			// Check if rite is complete
			if (rite.isComplete()) {
				// === Final structure integrity check ===
				if (!verifyRiteStructure(sLevel, rite)) {
					failRite(sLevel, caster, rite);
					toRemove.add(playerUUID);
					continue;
				}
				completeRite(sLevel, caster, rite);
				toRemove.add(playerUUID);
			}
		}

		for (UUID uuid : toRemove) {
			savedData.removeRite(uuid);
		}

		// Sync active rites to clients for boundary circle rendering
		if (sLevel.getGameTime() % RITE_SYNC_INTERVAL == 0 || !toRemove.isEmpty()) {
			List<ActiveRiteClientData.RiteEntry> entries = new ArrayList<>();
			for (ActiveCardinalRite rite : activeRites.values()) {
				entries.add(new ActiveRiteClientData.RiteEntry(
						rite.getCenterPos(), rite.getRiteSize(), rite.getProgress()));
			}
			PacketHandler.CHANNELBLOODVOLUME.send(
					PacketDistributor.ALL.noArg(),
					new PacketSyncActiveRites(entries));
		}

		// Periodically sync Qliphoth Bloom data to clients for tree rendering
		if (sLevel.getGameTime() % 200 == 0) {
			syncQliphothBlooms(sLevel);
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

			// Sync updated rite list to clients so boundary circle is removed
			List<ActiveRiteClientData.RiteEntry> entries = new ArrayList<>();
			for (ActiveCardinalRite rite : savedData.getActiveRites().values()) {
				entries.add(new ActiveRiteClientData.RiteEntry(
						rite.getCenterPos(), rite.getRiteSize(), rite.getProgress()));
			}
			PacketHandler.CHANNELBLOODVOLUME.send(
					PacketDistributor.ALL.noArg(),
					new PacketSyncActiveRites(entries));
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
	 * Searches for a block pattern match near a center position.
	 * <p>
	 * Vanilla {@link BlockPattern#find} only scans from {@code pos} to
	 * {@code pos + (maxDim - 1)} in the <b>positive</b> direction. When
	 * the stored center is in the middle of the structure, the pattern
	 * origin can lie in the negative direction and be missed entirely.
	 * <p>
	 * This helper offsets the search start back by {@code (maxDim - 1)}
	 * so that the full structure volume is within the scan range.
	 */
	private static BlockPattern.BlockPatternMatch findPatternNearCenter(
			BlockPattern blockPattern, ServerLevel sLevel, BlockPos center) {
		int maxDim = Math.max(Math.max(
				blockPattern.getWidth(), blockPattern.getHeight()), blockPattern.getDepth());
		BlockPos searchStart = center.offset(-(maxDim - 1), -(maxDim - 1), -(maxDim - 1));
		return blockPattern.find(sLevel, searchStart);
	}

	/**
	 * Re-validates that the multiblock structure for a cardinal rite is still intact.
	 * Returns true if the pattern still matches at the rite's center position.
	 */
	private static boolean verifyRiteStructure(ServerLevel sLevel, ActiveCardinalRite rite) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(sLevel, rite.getRecipeId());
		if (recipe == null) {
			Hemomancy.LOGGER.warn("Rite verification failed: recipe {} not found", rite.getRecipeId());
			return false;
		}

		BlockPos center = rite.getCenterPos();
		BlockPattern blockPattern = recipe.getPattern().getBlockPattern();
		BlockPattern.BlockPatternMatch match = findPatternNearCenter(blockPattern, sLevel, center);
		if (match == null) {
			Hemomancy.LOGGER.warn("Rite verification failed for {} at center {}. Dumping expected vs actual:",
					rite.getRecipeId(), center);
			var blockPairs = recipe.getPattern().getBlockPosBlockList();
			int width = blockPattern.getWidth();
			int height = blockPattern.getHeight();
			int depth = blockPattern.getDepth();
			Hemomancy.LOGGER.warn("  Pattern size: {}w x {}h x {}d, blockPairs: {}",
					width, height, depth, blockPairs.size());

			// Compute the origin of the structure relative to the stored center.
			// getBlockPosBlockList uses (charIndex, invertedRow, aisleIndex) as (X,Y,Z).
			// The center was derived from pattern index (width/2, height/2, depth/2).
			int halfW = width / 2;
			int halfH = height / 2;
			int halfD = depth / 2;

			int mismatches = 0;
			for (var pair : blockPairs) {
				Block expected = pair.getBlock();
				if (expected == null) expected = Blocks.AIR;
				BlockPos relPos = pair.getPos();
				BlockPos worldPos = center.offset(
						relPos.getX() - halfW,
						relPos.getY() - halfH,
						relPos.getZ() - halfD
				);
				Block actualBlock = sLevel.getBlockState(worldPos).getBlock();
				boolean mismatch = actualBlock != expected;
				if (mismatch) mismatches++;
				// Always log mismatches; only log OK lines for non-air to reduce spam
				if (mismatch || expected != Blocks.AIR) {
					Hemomancy.LOGGER.warn("  {} Expected [{}] at rel {} -> world {} | Found [{}]{}",
							mismatch ? "XX" : "OK",
							expected,
							relPos, worldPos,
							actualBlock,
							mismatch ? " << MISMATCH" : "");
				}
			}
			Hemomancy.LOGGER.warn("  Total mismatches: {} / {} positions", mismatches, blockPairs.size());
		}
		return match != null;
	}

	/**
	 * Fails a cardinal rite because the structure was tampered with.
	 * Deals damage to the caster, blasts them back, plays a loud noise, and sends a message.
	 */
	private static void failRite(ServerLevel sLevel, ServerPlayer caster, ActiveCardinalRite rite) {
		BlockPos center = rite.getCenterPos();

		// Deal damage to the caster
		caster.hurt(caster.damageSources().magic(), 10.0f);

		// Blast the caster back from the rite center
		double dx = caster.getX() - (center.getX() + 0.5);
		double dz = caster.getZ() - (center.getZ() + 0.5);
		double dist = Math.sqrt(dx * dx + dz * dz);
		if (dist < 0.1) {
			// If standing right on center, pick a random direction
			dx = caster.getRandom().nextDouble() - 0.5;
			dz = caster.getRandom().nextDouble() - 0.5;
			dist = Math.sqrt(dx * dx + dz * dz);
		}
		double knockbackStrength = 2.5;
		caster.setDeltaMovement(
				(dx / dist) * knockbackStrength,
				0.5,
				(dz / dist) * knockbackStrength);
		caster.hurtMarked = true;

		// Play loud, ominous failure sounds
		sLevel.playSound(null, center, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.0f, 0.5f);
		sLevel.playSound(null, center, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 1.5f, 0.7f);

		// Notify the caster
		caster.displayClientMessage(
				Component.literal("The rite structure has been broken! The ritual backlashes!")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	private static final String BLOODLINE_FOUNDING_RITE = "cardinal_rite/bloodline_founding";
	private static final String BLOODLINE_RECALL_RITE = "cardinal_rite/bloodline_recall";

	// ── New utility rite paths ──
	private static final String SANGUINE_ATTUNEMENT_RITE = "cardinal_rite/sanguine_attunement";
	private static final String CRIMSON_BEACON_RITE = "cardinal_rite/crimson_beacon";
	private static final String VASCULAR_MENDING_RITE = "cardinal_rite/vascular_mending";
	private static final String HUNGERING_EARTH_RITE = "cardinal_rite/hungering_earth";
	private static final String SCARLET_SUMMONS_RITE = "cardinal_rite/scarlet_summons";
	private static final String SANGUINE_DOMINION_RITE = "cardinal_rite/sanguine_dominion";
	private static final String ETERNAL_COVENANT_RITE = "cardinal_rite/eternal_covenant";
	private static final String ANCESTRAL_COMMUNION_RITE = "cardinal_rite/ancestral_communion";
	private static final String EXSANGUINATION_RITE = "cardinal_rite/exsanguination";
	private static final String HEMATIC_UNBINDING_RITE = "cardinal_rite/hematic_unbinding";
	private static final String LETHES_SHADOW_RITE = "cardinal_rite/lethes_shadow";
	private static final String BLOOM_OF_QLIPHOTH_RITE = "cardinal_rite/bloom_of_qliphoth";

	/** Eternal Covenant max blood volume bonus, applied once per player. */
	private static final double ETERNAL_COVENANT_BONUS = 500.0;
	/** NBT key stored on player persistent data to track covenant usage. */
	private static final String ETERNAL_COVENANT_TAG = "hemomancy:eternal_covenant_used";
	/** Radius (in blocks) for Hungering Earth terrain corruption. */
	private static final int HUNGERING_EARTH_RADIUS = 16;
	/** Chunk radius for Sanguine Dominion blood domain. */
	private static final int DOMINION_CHUNK_RADIUS = 3;
	/** Chunk radius for Qliphoth Bloom effect zone. */
	private static final int QLIPHOTH_BLOOM_CHUNK_RADIUS = 3;
	/** Blood cost per member for Scarlet Summons (from bloodline pool). */
	private static final float SUMMONS_COST_PER_MEMBER = 200f;

	private static final java.util.Map<String, Integer> DEGREE_RITE_PATHS = new java.util.HashMap<>();

	static {
		DEGREE_RITE_PATHS.put("cardinal_rite/sanguine_initiation", 1); // Neophyte of the Crimson Veil
		DEGREE_RITE_PATHS.put("cardinal_rite/votary_rite", 2);          // Votary of the Hematic Covenant
		DEGREE_RITE_PATHS.put("cardinal_rite/initiate_rite", 3);        // Initiate of the Scarlet Sanctum
		DEGREE_RITE_PATHS.put("cardinal_rite/adept_rite", 4);           // Adept of the Sanguine Brotherhood
		DEGREE_RITE_PATHS.put("cardinal_rite/illuminatus_rite", 5);     // Illuminatus of the Crimson Lodge
		DEGREE_RITE_PATHS.put("cardinal_rite/sanctified_rite", 6);      // Sanctified of the Bloodline Covenant
		DEGREE_RITE_PATHS.put("cardinal_rite/archon_rite", 7);          // Archon of the Hematic Order
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
		BlockPattern.BlockPatternMatch match = findPatternNearCenter(blockPattern, sLevel, center);
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
		String ritePath = rite.getRecipeId().getPath();
		if (!recipe.getResult().isEmpty()) {
			ItemStack resultStack = recipe.getResult().copy();

			// Bloodline founding rite: pre-sign the ledger with the caster's new bloodline
			if (BLOODLINE_FOUNDING_RITE.equals(ritePath) && resultStack.getItem() instanceof UnsignedLedgerItem) {
				presignBloodlineLedger(sLevel, caster, resultStack);
			}

			// Bloodline recall rite: re-issue a ledger from the caster's existing bloodline
			if (BLOODLINE_RECALL_RITE.equals(ritePath) && resultStack.getItem() instanceof UnsignedLedgerItem) {
				if (!recallBloodlineLedger(sLevel, caster, resultStack)) {
					// Caster has no bloodline — the rite still completes but the ledger stays unsigned
					caster.displayClientMessage(
							Component.literal("The blood remembers nothing... You have no bloodline to recall.")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							false);
				}
			}

			// Exsanguination rite: verify a named sacrifice was killed during the rite
			if (EXSANGUINATION_RITE.equals(ritePath)) {
				// The sacrifice processing in the tick loop already damages entities.
				// The quintessence result item is always produced — the rite IS the sacrifice.
				caster.displayClientMessage(
						Component.literal("The lifeblood crystallizes... Sanguine Quintessence is born.")
								.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
						false);
			}

			sLevel.addFreshEntity(new ItemEntity(sLevel,
					center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5,
					resultStack));
		}

		// === Utility rite effects (no result item needed) ===

		// Rite of Sanguine Attunement: reset all blood tendency scores to zero
		if (SANGUINE_ATTUNEMENT_RITE.equals(ritePath)) {
			completeSanguineAttunement(caster);
		}

		// Rite of the Crimson Beacon: register a death waypoint at the rite center
		if (CRIMSON_BEACON_RITE.equals(ritePath)) {
			completeCrimsonBeacon(sLevel, caster, center);
		}

		// Rite of Vascular Mending: fully heal all 7 vein sections
		if (VASCULAR_MENDING_RITE.equals(ritePath)) {
			completeVascularMending(caster);
		}

		// Rite of the Hungering Earth: corrupt terrain in a radius
		if (HUNGERING_EARTH_RITE.equals(ritePath)) {
			completeHungeringEarth(sLevel, caster, center);
		}

		// Rite of the Scarlet Summons: teleport all bloodline members
		if (SCARLET_SUMMONS_RITE.equals(ritePath)) {
			completeScarletSummons(sLevel, caster, center);
		}

		// Rite of Sanguine Dominion: establish a blood domain
		if (SANGUINE_DOMINION_RITE.equals(ritePath)) {
			completeSanguineDominion(sLevel, caster, center);
		}

		// Rite of the Eternal Covenant: permanently increase max blood volume
		if (ETERNAL_COVENANT_RITE.equals(ritePath)) {
			completeEternalCovenant(caster);
		}

		// Rite of Ancestral Communion: open a lore dialogue
		if (ANCESTRAL_COMMUNION_RITE.equals(ritePath)) {
			completeAncestralCommunion(sLevel, caster);
		}

		// Rite of Hematic Unbinding: dissolve the caster's bloodline
		if (HEMATIC_UNBINDING_RITE.equals(ritePath)) {
			completeHematicUnbinding(sLevel, caster);
		}

		// Rite of the Lethe's Shadow: strip Unstained progress from a nearby player
		if (LETHES_SHADOW_RITE.equals(ritePath)) {
			completeLethesShadow(sLevel, caster, center);
		}

		// Bloom of the Qliphoth: summon a persistent bloom tree that buffs nearby players
		if (BLOOM_OF_QLIPHOTH_RITE.equals(ritePath)) {
			completeBloomOfQliphoth(sLevel, caster, center);
		}

		// Play completion sound
		sLevel.playSound(null, center, SoundEvents.ENDERMAN_SCREAM, SoundSource.BLOCKS, 1.0f, 0.5f);
		sLevel.playSound(null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);

		// Notify the caster
		caster.displayClientMessage(
				Component.literal("The " + recipe.getRiteName() + " is complete!")
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
				false);

		// Award rite completion milestone (first rite + tiered)
		SkillPointGainEvents.onRiteCompleted(caster);

		// Check if this rite grants an initiatory degree
		Integer targetDegree = DEGREE_RITE_PATHS.get(ritePath);
		if (targetDegree != null) {
			caster.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA).ifPresent(degree -> {
				int currentDegree = degree.getDegreeNumber();
				if (currentDegree < targetDegree) {
					degree.setDegreeNumber(targetDegree);
					InitiatoryDegreeEvents.syncDegree(caster, degree);

					// Award degree milestone skill points
					SkillPointGainEvents.onDegreeReached(caster, targetDegree);

					// Mutual exclusion: reset Unstained progress (Harbingers and Unstained are opposed)
					caster.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(unstained -> {
						if (unstained.hasBegunPurification()) {
							unstained.setBegunPurification(false);
							unstained.setPurity(0);
							unstained.setClarityUnlocked(false);
							unstained.setClarity(0);
							UnstainedProgressEvents.syncProgress(caster, unstained);
							caster.displayClientMessage(
									Component.literal("Your purification has been undone by the blood rite.")
											.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
									false);
						}
					});

					EnumInitiatoryDegree newDegree = degree.getDegree();
					if (newDegree != null) {
						caster.displayClientMessage(
								Component.literal("You have attained the ")
										.withStyle(ChatFormatting.DARK_RED)
										.append(Component.translatable(newDegree.getLangKey())
												.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
										.append(Component.literal("!")
												.withStyle(ChatFormatting.DARK_RED)),
								false);
					}
				}
			});
		}
	}

	// ══════════════════════════════════════════════════════════════════════
	// Utility Rite Completion Handlers
	// ══════════════════════════════════════════════════════════════════════

	/**
	 * Rite of Sanguine Attunement (Degree 2, Minor):
	 * Resets all blood tendency alignment axes to zero.
	 */
	private static void completeSanguineAttunement(ServerPlayer caster) {
		caster.getCapability(BloodTendencyProvider.TENDENCY_CAPA).ifPresent(tendency -> {
			for (EnumBloodTendency bt : EnumBloodTendency.values()) {
				tendency.setTendencyAlignment(bt, 0f);
			}
			BloodTendencyEvents.syncTendency(caster, tendency);
		});
		caster.displayClientMessage(
				Component.literal("Your blood tendencies have been purged. You are a blank slate once more.")
						.withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of the Crimson Beacon (Degree 3, Lesser):
	 * Registers a death waypoint at the rite center. On fatal damage, the
	 * player's body will be teleported here before death (one-time use).
	 */
	private static void completeCrimsonBeacon(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		CrimsonBeaconSavedData data = CrimsonBeaconSavedData.get(sLevel.getServer().overworld());
		String dimension = sLevel.dimension().location().toString();
		data.setBeacon(caster.getUUID(), center, dimension);

		caster.displayClientMessage(
				Component.literal("A Crimson Beacon is anchored here. Should you fall, your body will return.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of Vascular Mending (Degree 3, Lesser):
	 * Fully restores all vein sections to maximum health (100).
	 */
	private static void completeVascularMending(ServerPlayer caster) {
		caster.getCapability(VascularSystemProvider.VASCULAR_CAPA).ifPresent(vascular -> {
			for (EnumVeinSections section : EnumVeinSections.values()) {
				java.util.Map<EnumVeinSections, Float> sys = vascular.getVascularSystem();
				sys.put(section, 100f);
				vascular.setVascularSystem(sys);
			}
			VascularSystemEvents.syncVascular(caster, vascular);
		});
		caster.displayClientMessage(
				Component.literal("Purified blood surges through your veins. All vascular damage has been mended.")
						.withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of the Hungering Earth (Degree 3, Lesser):
	 * Corrupts natural terrain in a radius around the rite center, converting:
	 * <ul>
	 *   <li>Stone → Venous Stone</li>
	 *   <li>Cobblestone → Venous Stone</li>
	 *   <li>Deepslate → Infested Venous Stone</li>
	 *   <li>Dirt/Grass → Befouling Ash Trail (block below becomes venous stone)</li>
	 *   <li>Sand/Gravel → Polished Venous Stone</li>
	 * </ul>
	 */
	private static void completeHungeringEarth(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		int radius = HUNGERING_EARTH_RADIUS;
		int converted = 0;

		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				// Circular check
				if (x * x + z * z > radius * radius) continue;

				for (int y = -radius / 2; y <= radius / 2; y++) {
					BlockPos pos = center.offset(x, y, z);
					BlockState state = sLevel.getBlockState(pos);
					Block block = state.getBlock();

					BlockState replacement = getHungeringEarthReplacement(block);
					if (replacement != null) {
						sLevel.setBlock(pos, replacement, 2);
						converted++;

						// Spawn occasional particles for visual feedback
						if (converted % 10 == 0) {
							sLevel.levelEvent(2001, pos, Block.getId(state));
						}
					}
				}
			}
		}

		caster.displayClientMessage(
				Component.literal("The earth hungers... " + converted + " blocks have been corrupted.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Returns the blood-corrupted replacement for a natural block, or null if
	 * the block should not be converted.
	 */
	private static BlockState getHungeringEarthReplacement(Block block) {
		if (block == Blocks.STONE || block == Blocks.COBBLESTONE || block == Blocks.ANDESITE
				|| block == Blocks.DIORITE || block == Blocks.GRANITE) {
			return BlockInit.venous_stone.get().defaultBlockState();
		}
		if (block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE) {
			return BlockInit.infested_venous_stone.get().defaultBlockState();
		}
		if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL
				|| block == Blocks.MYCELIUM || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT) {
			return BlockInit.befouling_ash_trail.get().defaultBlockState();
		}
		if (block == Blocks.SAND || block == Blocks.RED_SAND || block == Blocks.GRAVEL) {
			return BlockInit.polished_venous_stone.get().defaultBlockState();
		}
		return null;
	}

	/**
	 * Rite of the Scarlet Summons (Degree 5, Greater):
	 * Teleports all online bloodline members to the rite center. Draws blood
	 * from the shared bloodline pool proportional to the number of members.
	 */
	private static void completeScarletSummons(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodlineSavedData bloodlineData = BloodlineSavedData.get(overworld);
		Bloodline bloodline = bloodlineData.getBloodlineForPlayer(caster.getUUID());

		if (bloodline == null || !bloodline.isValid()) {
			caster.displayClientMessage(
					Component.literal("You have no bloodline to summon.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Only the bloodline leader may perform the summons
		if (!bloodline.getLeaderUUID().equals(caster.getUUID())) {
			caster.displayClientMessage(
					Component.literal("Only the bloodline leader may perform the Scarlet Summons.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		List<ServerPlayer> onlineMembers = new ArrayList<>();
		for (UUID memberUUID : bloodline.getPlayerUUIDS()) {
			if (memberUUID.equals(caster.getUUID())) continue; // Skip the caster
			ServerPlayer member = sLevel.getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				onlineMembers.add(member);
			}
		}

		if (onlineMembers.isEmpty()) {
			caster.displayClientMessage(
					Component.literal("No bloodline members are online to summon.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Draw blood from the shared pool
		float totalCost = onlineMembers.size() * SUMMONS_COST_PER_MEMBER;
		float drawn = bloodlineData.drawBlood(bloodline.getBloodlineUUID(), totalCost);
		if (drawn < totalCost * 0.5f) {
			caster.displayClientMessage(
					Component.literal("The bloodline pool lacks sufficient blood for the summons.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Teleport all online members
		int teleported = 0;
		for (ServerPlayer member : onlineMembers) {
			// Handle cross-dimension teleport
			if (!member.level().equals(sLevel)) {
				member.teleportTo(sLevel, center.getX() + 0.5, center.getY() + 1.5,
						center.getZ() + 0.5, member.getYRot(), member.getXRot());
			} else {
				member.teleportTo(center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5);
			}
			member.displayClientMessage(
					Component.literal("The blood calls! You have been summoned by " + caster.getName().getString() + ".")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
					false);
			sLevel.playSound(null, member.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
					SoundSource.PLAYERS, 1.0f, 0.7f);
			teleported++;
		}

		caster.displayClientMessage(
				Component.literal("The Scarlet Summons draws " + teleported + " blood-kin to your side.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of Sanguine Dominion (Degree 6, Greater):
	 * Establishes a persistent Blood Domain centered on the rite location.
	 * Within the domain: enemies take slow bleed damage, and the caster's
	 * manipulations cost less blood.
	 */
	private static void completeSanguineDominion(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		SanguineDominionSavedData data = SanguineDominionSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();

		SanguineDominionSavedData.DominionEntry entry = new SanguineDominionSavedData.DominionEntry(
				caster.getUUID(), center, dimension, DOMINION_CHUNK_RADIUS, sLevel.getGameTime());
		data.addDominion(entry);

		int blockRadius = DOMINION_CHUNK_RADIUS * 16;
		caster.displayClientMessage(
				Component.literal("A Blood Domain has been established! " + blockRadius
						+ " blocks in every direction now bow to your crimson will.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	/**
	 * Rite of the Eternal Covenant (Degree 6, Greater):
	 * Permanently increases the caster's maximum blood volume. Can only be
	 * performed once per player.
	 */
	private static void completeEternalCovenant(ServerPlayer caster) {
		CompoundTag persistentData = caster.getPersistentData();
		if (persistentData.getBoolean(ETERNAL_COVENANT_TAG)) {
			caster.displayClientMessage(
					Component.literal("The covenant has already been sealed. Its boon cannot be granted twice.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		caster.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
			volume.addMaxBloodVolume(ETERNAL_COVENANT_BONUS);
			BloodVolumeEvents.syncVolume(caster, volume);
		});
		persistentData.putBoolean(ETERNAL_COVENANT_TAG, true);

		caster.displayClientMessage(
				Component.literal("The Eternal Covenant is sealed! Your maximum blood volume has been permanently increased by "
						+ (int) ETERNAL_COVENANT_BONUS + ".")
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
				false);
	}

	/**
	 * Rite of Ancestral Communion (Degree 7, Grand):
	 * Opens a dialogue with the fungal consciousness, granting unique lore.
	 * Each invocation uses a different dialogue variant from a pool.
	 */
	private static void completeAncestralCommunion(ServerLevel sLevel, ServerPlayer caster) {
		// Determine variant based on game time for variety
		int variant = (int) ((sLevel.getGameTime() / 100) % AncestralCommunionDialogueTrees.VARIANT_COUNT);
		DialogueTree tree = AncestralCommunionDialogueTrees.forVariant(variant);

		PacketHandler.CHANNELBLOODVOLUME.send(
				PacketDistributor.PLAYER.with(() -> caster),
				new OpenDialoguePacket(tree));

		caster.displayClientMessage(
				Component.literal("The ancient blood stirs... a voice rises from the depths.")
						.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of Hematic Unbinding (Cross-tier, Lesser):
	 * Destroys the caster's bloodline, freeing all members. Any shared blood
	 * in the pool is returned proportionally to the remaining members.
	 */
	private static void completeHematicUnbinding(ServerLevel sLevel, ServerPlayer caster) {
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodlineSavedData bloodlineData = BloodlineSavedData.get(overworld);
		Bloodline bloodline = bloodlineData.getBloodlineForPlayer(caster.getUUID());

		if (bloodline == null || !bloodline.isValid()) {
			caster.displayClientMessage(
					Component.literal("You have no bloodline to unbind.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Only the leader can dissolve the bloodline
		if (!bloodline.getLeaderUUID().equals(caster.getUUID())) {
			caster.displayClientMessage(
					Component.literal("Only the bloodline leader may perform the Hematic Unbinding.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		String bloodlineName = bloodline.getName();
		float poolBlood = bloodline.getBloodVolume();
		int memberCount = bloodline.getPlayerUUIDS().size();
		float bloodPerMember = memberCount > 0 ? poolBlood / memberCount : 0;

		// Return blood to all online members and clear their bloodline reference
		for (UUID memberUUID : new ArrayList<>(bloodline.getPlayerUUIDS())) {
			ServerPlayer member = sLevel.getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				// Return their share of the pool blood
				if (bloodPerMember > 0) {
					member.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
						volume.fill(bloodPerMember);
						volume.setBloodLine(Bloodline.NOBLOODLINE);
						BloodVolumeEvents.syncVolume(member, volume);
					});
				} else {
					member.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
						volume.setBloodLine(Bloodline.NOBLOODLINE);
						BloodVolumeEvents.syncVolume(member, volume);
					});
				}

				member.displayClientMessage(
						Component.literal("The bloodline " + bloodlineName + " has been dissolved. You are unbound.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						false);
			}
		}

		// Remove the bloodline from world data
		bloodlineData.getAllBloodlines().remove(bloodline.getBloodlineUUID());

		caster.displayClientMessage(
				Component.literal("The " + bloodlineName + " is dissolved. What was bound by blood is unbound.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	/**
	 * Rite of the Lethe's Shadow (Cross-tier, Grand):
	 * Targets the nearest non-caster player within the rite circle and strips
	 * their Unstained purification progress. A direct hematic assault against
	 * followers of Our Lady of Lethe.
	 */
	private static void completeLethesShadow(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		int halfSize = (9 - 1) / 2; // Grand rite 9x9 structure
		AABB bounds = new AABB(center).inflate(halfSize + 1);

		// Find the nearest non-caster player in the rite bounds
		List<Player> nearbyPlayers = sLevel.getEntitiesOfClass(Player.class, bounds,
				p -> p.isAlive() && !p.getUUID().equals(caster.getUUID()));

		if (nearbyPlayers.isEmpty()) {
			caster.displayClientMessage(
					Component.literal("No target stands within the circle. The shadow dissipates.")
							.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Target the closest player
		Player target = nearbyPlayers.get(0);
		double closestDist = target.distanceToSqr(center.getX(), center.getY(), center.getZ());
		for (Player p : nearbyPlayers) {
			double dist = p.distanceToSqr(center.getX(), center.getY(), center.getZ());
			if (dist < closestDist) {
				target = p;
				closestDist = dist;
			}
		}

		final Player victim = target;
		victim.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(unstained -> {
			boolean hadProgress = unstained.hasBegunPurification() || unstained.getPurity() > 0;

			unstained.setBegunPurification(false);
			unstained.setPurity(0);
			unstained.setClarityUnlocked(false);
			unstained.setClarity(0);

			if (victim instanceof ServerPlayer serverVictim) {
				UnstainedProgressEvents.syncProgress(serverVictim, unstained);
			}

			if (hadProgress) {
				if (victim instanceof ServerPlayer sp) {
					sp.displayClientMessage(
							Component.literal("A shadow of crimson corruption washes over you... Your purification has been destroyed!")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
							false);
				}
				caster.displayClientMessage(
						Component.literal("The Lethe's Shadow consumes " + victim.getName().getString()
								+ "'s purity. Their purification is undone.")
								.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
						false);
			} else {
				caster.displayClientMessage(
						Component.literal(victim.getName().getString()
								+ " had no purification to destroy. The shadow finds nothing.")
								.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
						false);
			}
		});

		// Visual/sound feedback
		sLevel.playSound(null, center, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.7f, 1.5f);
	}

	/**
	 * Bloom of the Qliphoth (Degree 4, Lesser):
	 * Summons a persistent Qliphoth Bloom at the rite center. Within a 3-chunk
	 * radius, all blood manipulations cost 25% less blood and players receive
	 * passive health regeneration and enhanced blood regeneration.
	 */
	private static void completeBloomOfQliphoth(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();

		QliphothBloomSavedData.BloomEntry entry = new QliphothBloomSavedData.BloomEntry(
				caster.getUUID(), center, dimension, QLIPHOTH_BLOOM_CHUNK_RADIUS, sLevel.getGameTime());
		data.addBloom(entry);

		// Sync to all nearby clients so the tree renders immediately
		syncQliphothBlooms(sLevel);

		int blockRadius = QLIPHOTH_BLOOM_CHUNK_RADIUS * 16;
		caster.displayClientMessage(
				Component.literal("The Qliphoth blooms! A dark tree takes root, empowering blood within "
						+ blockRadius + " blocks.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	/**
	 * Syncs all Qliphoth Bloom locations to nearby clients for rendering.
	 */
	private static void syncQliphothBlooms(ServerLevel sLevel) {
		QliphothBloomSavedData data = QliphothBloomSavedData.get(sLevel.getServer().overworld());
		String dimension = sLevel.dimension().location().toString();
		java.util.List<com.vincenthuto.hemomancy.client.data.QliphothBloomClientData.BloomEntry> clientEntries = new ArrayList<>();
		for (QliphothBloomSavedData.BloomEntry bloom : data.getBlooms()) {
			if (bloom.dimension().equals(dimension)) {
				clientEntries.add(new com.vincenthuto.hemomancy.client.data.QliphothBloomClientData.BloomEntry(
						bloom.center(), bloom.chunkRadius()));
			}
		}
		com.vincenthuto.hemomancy.common.network.capa.PacketSyncQliphothBlooms packet =
				new com.vincenthuto.hemomancy.common.network.capa.PacketSyncQliphothBlooms(clientEntries);
		PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.ALL.noArg(), packet);
	}

	/**
	 * Pre-signs a bloodline ledger with the caster's newly founded bloodline.
	 * Creates the bloodline, registers it in world data, sets the caster's capability,
	 * and writes the signed state onto the ledger item so it can be redistributed.
	 */
	private static void presignBloodlineLedger(ServerLevel sLevel, ServerPlayer caster, ItemStack ledgerStack) {
		String bloodLineName = caster.getName().getString() + "'s Blood Line";
		UUID bloodLineUUID = new UUID(caster.getUUID().getMostSignificantBits(), sLevel.getGameTime());
		ArrayList<UUID> uuids = new ArrayList<>();
		Bloodline playerLine = new Bloodline(bloodLineName, caster.getUUID(), bloodLineUUID, uuids);

		// Register bloodline in world-level saved data
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
		savedData.registerBloodline(playerLine);

		// Set the caster's bloodline capability
		caster.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
			volume.setBloodLine(playerLine);
			BloodVolumeEvents.syncVolume(caster, volume);
		});

		// Write signed state and bloodline data onto the ledger
		CompoundTag compound = ledgerStack.getOrCreateTag();
		compound.putBoolean(UnsignedLedgerItem.TAG_STATE, true);
		compound.put(UnsignedLedgerItem.TAG_BLOODLINE, playerLine.serialize());
		ledgerStack.setTag(compound);

		caster.displayClientMessage(
				Component.literal("You have founded: " + playerLine.getName())
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	/**
	 * Recalls a lost bloodline ledger by looking up the caster's existing bloodline
	 * from world data and writing it onto the result item. This is a penitent rite —
	 * the covenant does not forget, but it demands a price for carelessness.
	 * Returns false if the caster has no bloodline to recall.
	 */
	private static boolean recallBloodlineLedger(ServerLevel sLevel, ServerPlayer caster, ItemStack ledgerStack) {
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
		Bloodline existingLine = savedData.getBloodlineForPlayer(caster.getUUID());

		if (existingLine == null || !existingLine.isValid()) {
			return false;
		}

		// Write the existing bloodline data onto the new ledger as a signed copy
		CompoundTag compound = ledgerStack.getOrCreateTag();
		compound.putBoolean(UnsignedLedgerItem.TAG_STATE, true);
		compound.put(UnsignedLedgerItem.TAG_BLOODLINE, existingLine.serialize());
		ledgerStack.setTag(compound);

		caster.displayClientMessage(
				Component.literal("The covenant remembers. Your ledger for " + existingLine.getName()
						+ " has been restored.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
		return true;
	}
}
