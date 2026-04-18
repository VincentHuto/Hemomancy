package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager;
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
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.AncestralCommunionDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.bloodline.UnsignedLedgerItem;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncActiveRites;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

		// Periodically sync Qliphoth Bloom data to clients for tree rendering
		// (must be outside the active-rite block since blooms persist after rites end)
		// Only sync from the overworld to prevent other dimension ticks from overwriting
		// the client bloom list with an empty set.
		if (sLevel == sLevel.getServer().overworld() && sLevel.getGameTime() % 200 == 0) {
			syncQliphothBlooms(sLevel.getServer());
		}

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
			// Compute the outermost boundary radius to match the rendered ring.
			// Renderer: baseRadius = riteSize / 2.0 + 1.0, ringCount rings spaced 2 blocks apart.
			int riteSize = rite.getRiteSize();
			int ringCount = Math.max(1, (riteSize - 1) / 2);
			double outermostRadius = riteSize / 2.0 + 1.0 + (ringCount - 1) * 2.0;
			AABB bounds = new AABB(center).inflate(outermostRadius);

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
						rite.getCenterPos(), rite.getRiteSize(), rite.getProgress(), rite.getRecipeId()));
			}
			PacketHandler.CHANNELBLOODVOLUME.send(
					PacketDistributor.ALL.noArg(),
					new PacketSyncActiveRites(entries));
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
						rite.getCenterPos(), rite.getRiteSize(), rite.getProgress(), rite.getRecipeId()));
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
	 * Vanilla {@link BlockPattern#find} scans a cube of only
	 * {@code maxDim × maxDim × maxDim} starting positions, which is too
	 * small when the stored center sits in the middle of the structure.
	 * Depending on which rotation matches, the pattern's
	 * {@code frontTopLeft} anchor can be up to {@code (maxDim - 1)} blocks
	 * from center in <b>any</b> direction. We therefore scan a larger cube
	 * of radius {@code (maxDim - 1)} around center to guarantee coverage.
	 */
	private static BlockPattern.BlockPatternMatch findPatternNearCenter(
			BlockPattern blockPattern, ServerLevel sLevel, BlockPos center) {
		int maxDim = Math.max(Math.max(
				blockPattern.getWidth(), blockPattern.getHeight()), blockPattern.getDepth());
		int radius = maxDim - 1;
		for (BlockPos candidate : BlockPos.betweenClosed(
				center.offset(-radius, -radius, -radius),
				center.offset(radius, radius, radius))) {
			for (Direction finger : Direction.values()) {
				for (Direction thumb : Direction.values()) {
					if (thumb == finger || thumb == finger.getOpposite()) continue;
					BlockPattern.BlockPatternMatch match = blockPattern.matches(sLevel, candidate, finger, thumb);
					if (match != null) return match;
				}
			}
		}
		return null;
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
				// Null or air expected means this was a space (wildcard) — skip it
				if (expected == null || expected == Blocks.AIR) continue;
				BlockPos relPos = pair.getPos();
				BlockPos worldPos = center.offset(
						relPos.getX() - halfW,
						relPos.getY() - halfH,
						relPos.getZ() - halfD
				);
				Block actualBlock = sLevel.getBlockState(worldPos).getBlock();
				boolean mismatch = actualBlock != expected;
				if (mismatch) mismatches++;
				Hemomancy.LOGGER.warn("  {} Expected [{}] at rel {} -> world {} | Found [{}]{}",
						mismatch ? "XX" : "OK",
						expected,
						relPos, worldPos,
						actualBlock,
						mismatch ? " << MISMATCH" : "");
			}
			Hemomancy.LOGGER.warn("  Total mismatches: {} / {} non-wildcard positions", mismatches, blockPairs.size());
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
	private static final String SANGUINE_INITIATION_RITE = "cardinal_rite/sanguine_initiation";

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
	private static final String PALLID_SHADOW_RITE = "cardinal_rite/pallid_shadow";
	private static final String BLOOM_OF_QLIPHOTH_RITE = "cardinal_rite/bloom_of_qliphoth";
	private static final String PRUNING_OF_QLIPHOTH_RITE = "cardinal_rite/pruning_of_qliphoth";
	private static final String SANGUINE_FERVOR_RITE = "cardinal_rite/sanguine_fervor";
	private static final String ILLUMINATUS_RITE = "cardinal_rite/illuminatus_rite";

	// ── Gourd upgrade rite paths ──
	private static final String PALLID_VESSEL_RITE = "cardinal_rite/pallid_vessel_rite";
	private static final String CRIMSON_VESSEL_RITE = "cardinal_rite/crimson_vessel_rite";
	private static final String ASHEN_VESSEL_RITE = "cardinal_rite/ashen_vessel_rite";
	private static final String HORN_OF_CULMINATION_RITE = "cardinal_rite/horn_of_culmination_rite";

	// ── Unstained rite paths ──
	private static final String LETHEAN_BAPTISM_RITE = "cardinal_rite/lethean_baptism";
	private static final String SILVER_VEIL_RITE = "cardinal_rite/silver_veil";
	private static final String CLARITY_ASCENSION_RITE = "cardinal_rite/clarity_ascension";
	private static final String LETHEAN_JUDGMENT_RITE = "cardinal_rite/lethean_judgment";
	private static final String SILVER_DAWN_RITE = "cardinal_rite/silver_dawn";
	private static final String STILL_WATERS_RITE = "cardinal_rite/still_waters";
	private static final String PALE_CONSECRATION_RITE = "cardinal_rite/pale_consecration";
	private static final String SILTHMERES_REMEMBRANCE_RITE = "cardinal_rite/silthmeres_remembrance";
	private static final String LETHE_COVENANT_RITE = "cardinal_rite/lethe_covenant";

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
	/** Chunk radius for the Sanguine Fervor spawn-boost zone. */
	private static final int SANGUINE_FERVOR_CHUNK_RADIUS = 3;
	/** Chunk radius for the Crimson Lodge buff zone. */
	private static final int CRIMSON_LODGE_CHUNK_RADIUS = 5;
	/** Duration in ticks for the Sanguine Fervor spawn-boost effect (5 minutes). */
	private static final long SANGUINE_FERVOR_DURATION_TICKS = 6000L;
	/** Blood cost per member for Scarlet Summons (from bloodline pool). */
	private static final float SUMMONS_COST_PER_MEMBER = 200f;
	/** Radius (in blocks) for Lethean Judgment anti-blood disruption. */
	private static final int LETHEAN_JUDGMENT_RADIUS = 16;
	/** Duration in ticks for Silver Veil effect (30 minutes = 36000 ticks). */
	private static final int SILVER_VEIL_DURATION_TICKS = 36000;

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

		// Destroy the multiblock pattern (only structure blocks, not wildcard positions)
		BlockPos center = rite.getCenterPos();
		BlockPattern blockPattern = recipe.getPattern().getBlockPattern();
		BlockPattern.BlockPatternMatch match = findPatternNearCenter(blockPattern, sLevel, center);
		if (match != null && recipe.shouldBreakBlocksOnCreation()) {
			// Build a lookup of which (charIndex, invertedRow, aisle) positions are
			// actual structure blocks vs wildcard spaces
			String[][] patternArray = recipe.getPattern().getPatternArray();
			java.util.Map<String, Block> symbolList = recipe.getPattern().getSymbolList();
			int width = blockPattern.getWidth();
			int height = blockPattern.getHeight();
			int depth = blockPattern.getDepth();

			// patternArray maps as: aisle -> rows (top-to-bottom) -> chars (left-to-right)
			// BlockPattern maps as: i=charIndex (width), j=invertedRow (height), k=aisle (depth)
			for (int k = 0; k < depth; k++) {
				String[] aisle = patternArray[k];
				for (int j = 0; j < height; j++) {
					String row = aisle[j];
					for (int i = 0; i < width; i++) {
						if (i >= row.length()) continue;
						char c = row.charAt(i);
						// Space character is a wildcard — don't destroy whatever happens to be there
						if (c == ' ') continue;
						Block expected = symbolList.get(String.valueOf(c));
						if (expected == null || expected == Blocks.AIR) continue;

						// match.getBlock handles rotation so we get the correct world pos
						BlockPos worldPos = match.getBlock(i, j, k).getPos();
						BlockState state = sLevel.getBlockState(worldPos);
						sLevel.setBlock(worldPos, Blocks.AIR.defaultBlockState(), 2);
						sLevel.levelEvent(2001, worldPos, Block.getId(state));
					}
				}
			}
		}

		// Spawn result item
		String ritePath = rite.getRecipeId().getPath();
		if (!recipe.getResult().isEmpty()) {
			ItemStack resultStack = recipe.getResult().copy();

			// === Gourd upgrade rites: consume the prerequisite gourd from player ===
			if (PALLID_VESSEL_RITE.equals(ritePath)) {
				if (!consumeGourdPrerequisite(caster, ItemInit.dried_gourd.get())) {
					caster.displayClientMessage(
							Component.literal("You carry no dried gourd to consecrate. The rite yields nothing.")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							false);
					resultStack = ItemStack.EMPTY;
				} else {
					caster.displayClientMessage(
							Component.literal("The dried gourd awakens, its pallid shell now a vessel for living blood.")
									.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
							false);
				}
			}

			if (CRIMSON_VESSEL_RITE.equals(ritePath)) {
				if (!consumeGourdPrerequisite(caster, ItemInit.blood_gourd_white.get())) {
					caster.displayClientMessage(
							Component.literal("You carry no pallid vessel to steep. The rite yields nothing.")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							false);
					resultStack = ItemStack.EMPTY;
				} else {
					caster.displayClientMessage(
							Component.literal("The pallid vessel flushes crimson — reborn in the deepest scarlet.")
									.withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
							false);
				}
			}

			if (ASHEN_VESSEL_RITE.equals(ritePath)) {
				if (!consumeGourdPrerequisite(caster, ItemInit.blood_gourd_red.get())) {
					caster.displayClientMessage(
							Component.literal("You carry no crimson vessel to temper. The rite yields nothing.")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							false);
					resultStack = ItemStack.EMPTY;
				} else {
					caster.displayClientMessage(
							Component.literal("Through fire and ash the vessel is reborn — blackened, hardened, and hungry.")
									.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
							false);
				}
			}

			if (HORN_OF_CULMINATION_RITE.equals(ritePath)) {
				if (!consumeGourdPrerequisite(caster, ItemInit.blood_gourd_black.get())) {
					caster.displayClientMessage(
							Component.literal("You carry no ashen vessel to transcend. The rite yields nothing.")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
							false);
					resultStack = ItemStack.EMPTY;
				} else {
					caster.displayClientMessage(
							Component.literal("The final vessel transcends flesh and gourd alike — the Curved Horn is born.")
									.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
							false);
				}
			}

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

			if (!resultStack.isEmpty()) {
				sLevel.addFreshEntity(new ItemEntity(sLevel,
						center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5,
						resultStack));
			}
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

		// Rite of the Pallid Shadow: strip Unstained progress from a nearby player
		if (PALLID_SHADOW_RITE.equals(ritePath)) {
			completePallidShadow(sLevel, caster, center);
		}

		// Bloom of the Qliphoth: summon a persistent bloom tree that buffs nearby players
		if (BLOOM_OF_QLIPHOTH_RITE.equals(ritePath)) {
			completeBloomOfQliphoth(sLevel, caster, center);
		}

		// Pruning of the Qliphoth: remove a bloom tree rooted in the same chunk
		if (PRUNING_OF_QLIPHOTH_RITE.equals(ritePath)) {
			completePruningOfQliphoth(sLevel, caster, center);
		}

		// Rite of Sanguine Fervor: boost mob spawn rates in a 3-chunk radius for 5 minutes
		if (SANGUINE_FERVOR_RITE.equals(ritePath)) {
			completeSanguineFervor(sLevel, caster, center);
		}

		// Rite of the Crimson Lodge: establish a lodge zone with strength and blood virility
		if (ILLUMINATUS_RITE.equals(ritePath)) {
			completeCrimsonLodge(sLevel, caster, center);
		}

		// ── Unstained rites ──

		// Rite of Lethean Baptism: begin the Unstained path
		if (LETHEAN_BAPTISM_RITE.equals(ritePath)) {
			completeLetheanBaptism(sLevel, caster);
		}

		// Rite of the Silver Veil: grant Silver Ward effect and purity
		if (SILVER_VEIL_RITE.equals(ritePath)) {
			completeSilverVeil(sLevel, caster);
		}

		// Rite of Clarity Ascension: unlock clarity phase
		if (CLARITY_ASCENSION_RITE.equals(ritePath)) {
			completeClarityAscension(sLevel, caster);
		}

		// Rite of Lethean Judgment: disrupt nearby hemomancers
		if (LETHEAN_JUDGMENT_RITE.equals(ritePath)) {
			completeLetheanJudgment(sLevel, caster, center);
		}

		// Rite of the Silver Dawn: create a persistent cleansed zone
		if (SILVER_DAWN_RITE.equals(ritePath)) {
			completeSilverDawn(sLevel, caster, center);
		}

		// Rite of Still Waters: create a 5-minute zone of reduced magic damage
		if (STILL_WATERS_RITE.equals(ritePath)) {
			completeStillWaters(sLevel, caster, center);
		}

		// Rite of Pale Consecration: consecrate the ground, damaging hostile mobs
		if (PALE_CONSECRATION_RITE.equals(ritePath)) {
			completePaleConsecration(sLevel, caster, center);
		}

		// Rite of Silthmere's Remembrance: burst purity + Silver Ward for nearby Unstained players
		if (SILTHMERES_REMEMBRANCE_RITE.equals(ritePath)) {
			completeSilthmereRemembrance(sLevel, caster, center);
		}

		// Rite of the Lethe Covenant: establish a grand Unstained domain
		if (LETHE_COVENANT_RITE.equals(ritePath)) {
			completeLetheCovenantRite(sLevel, caster, center);
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

		// Sanguine Initiation: give the caster a Sanguine Conduit so they can monitor their progress
		if (SANGUINE_INITIATION_RITE.equals(ritePath)) {
			ItemStack conduit = new ItemStack(ItemInit.sanguine_conduit.get());
			if (!caster.getInventory().add(conduit)) {
				sLevel.addFreshEntity(new ItemEntity(sLevel,
						center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5, conduit));
			}
			caster.displayClientMessage(
					Component.translatable("hemomancy.rite.sanguine_initiation.conduit_granted")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
		}

		if ("cardinal_rite/initiate_rite".equals(ritePath)) {
			ItemStack blob = new ItemStack(ItemInit.sanguine_blob.get());
			if (!caster.getInventory().add(blob)) {
				sLevel.addFreshEntity(new ItemEntity(sLevel,
						center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5, blob));
			}
			caster.displayClientMessage(
					Component.translatable("hemomancy.rite.initiate_rite.blob_granted")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
		}
	}

	// ══════════════════════════════════════════════════════════════════════
	// Gourd Upgrade Helpers
	// ══════════════════════════════════════════════════════════════════════

	/**
	 * Searches the player's main hand, off hand, and inventory for an item matching
	 * the given prerequisite. If found, one stack entry is consumed and true is returned.
	 * Prefers the main hand, then off hand, then the first matching inventory slot.
	 */
	private static boolean consumeGourdPrerequisite(ServerPlayer caster, net.minecraft.world.item.Item prerequisite) {
		// Check main hand first
		ItemStack mainHand = caster.getMainHandItem();
		if (mainHand.getItem() == prerequisite) {
			mainHand.shrink(1);
			return true;
		}

		// Check off hand
		ItemStack offHand = caster.getOffhandItem();
		if (offHand.getItem() == prerequisite) {
			offHand.shrink(1);
			return true;
		}

		// Search entire inventory
		for (int i = 0; i < caster.getInventory().getContainerSize(); i++) {
			ItemStack stack = caster.getInventory().getItem(i);
			if (stack.getItem() == prerequisite) {
				stack.shrink(1);
				return true;
			}
		}

		return false;
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
	 * Rite of the Crimson Lodge (Degree 5, Greater):
	 * Establishes a persistent Crimson Lodge centered on the rite location.
	 * Within the lodge: players gain Strength I and enhanced blood
	 * regeneration. The structure blocks are preserved (breakBlocksOnCreation
	 * is false in the recipe). Recruited NPC Harbingers may be summoned
	 * anywhere within the lodge radius via the ancestral ledger.
	 */
	private static void completeCrimsonLodge(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		CrimsonLodgeSavedData data = CrimsonLodgeSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();

		// Check for overlap with existing lodges
		CrimsonLodgeSavedData.LodgeEntry overlapping = data.getOverlappingLodge(
				center, dimension, CRIMSON_LODGE_CHUNK_RADIUS);
		if (overlapping != null) {
			caster.displayClientMessage(
					Component.literal("A Crimson Lodge already exists within " + CRIMSON_LODGE_CHUNK_RADIUS
							+ " chunks of here. Only one lodge may exist per region.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		CrimsonLodgeSavedData.LodgeEntry entry = new CrimsonLodgeSavedData.LodgeEntry(
				caster.getUUID(), center, dimension, CRIMSON_LODGE_CHUNK_RADIUS, sLevel.getGameTime(), center);
		data.addLodge(entry);

		int blockRadius = CRIMSON_LODGE_CHUNK_RADIUS * 16;
		caster.displayClientMessage(
				Component.literal("The Crimson Lodge has been consecrated! Strength and blood virility flow within "
						+ blockRadius + " blocks.")
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
	 * Rite of the Pallid Shadow (Cross-tier, Grand):
	 * Targets the nearest non-caster player within the rite circle and strips
	 * their Unstained purification progress. A direct hematic assault against
	 * followers of Our Lady of Still Waters.
	 */
	private static void completePallidShadow(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
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
						Component.literal("The Pallid Shadow consumes " + victim.getName().getString()
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
	 * <p>
	 * Places a 1×1×8 multi-block (QliphothBloomBlock + 7 fillers) at the
	 * ritual center and registers the bloom in world SavedData.
	 */
	private static void completeBloomOfQliphoth(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();

		// Check if a bloom already exists within the radius — only one bloom per 3-chunk area
		QliphothBloomSavedData.BloomEntry overlapping = data.getOverlappingBloom(
				center.above(2), dimension, QLIPHOTH_BLOOM_CHUNK_RADIUS);
		if (overlapping != null) {
			caster.displayClientMessage(
					Component.literal("A Qliphoth Bloom already exists within " + QLIPHOTH_BLOOM_CHUNK_RADIUS
							+ " chunks of here. Only one bloom may exist per " + QLIPHOTH_BLOOM_CHUNK_RADIUS
							+ "-chunk radius.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Verify there is room for the 1×1×8 column
		Block bloomBlock = BlockInit.qliphoth_bloom.get();
		com.vincenthuto.hemomancy.common.block.IMultiBlock multiBlock =
				(com.vincenthuto.hemomancy.common.block.IMultiBlock) bloomBlock;
		if (!multiBlock.canPlaceMultiBlock(sLevel, center.above(2))) {
			caster.displayClientMessage(
					Component.literal("There is not enough room for the Qliphoth to bloom here.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Place the multi-block
		sLevel.setBlockAndUpdate(center.above(2), bloomBlock.defaultBlockState());
		net.minecraft.world.level.block.entity.BlockEntity be = sLevel.getBlockEntity(center.above(2));
		if (be instanceof com.vincenthuto.hemomancy.common.tile.functional.QliphothBloomBlockEntity bloomBE) {
			bloomBE.setOwnerUUID(caster.getUUID());
			bloomBE.setChunkRadius(QLIPHOTH_BLOOM_CHUNK_RADIUS);
		}
		// Place filler blocks above
		multiBlock.placeFillers(sLevel, center.above(2), bloomBlock.defaultBlockState());

		// Register in SavedData
		QliphothBloomSavedData.BloomEntry entry = new QliphothBloomSavedData.BloomEntry(
				caster.getUUID(), center.above(2),dimension, QLIPHOTH_BLOOM_CHUNK_RADIUS, sLevel.getGameTime());
		data.addBloom(entry);

		// Sync to all nearby clients so the tree renders immediately
		syncQliphothBlooms(sLevel.getServer());

		int blockRadius = QLIPHOTH_BLOOM_CHUNK_RADIUS * 16;
		caster.displayClientMessage(
				Component.literal("The Qliphoth blooms! A dark tree takes root, empowering blood within "
						+ blockRadius + " blocks.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	/**
	 * Pruning of the Qliphoth (Degree 0, Minor):
	 * Removes a Qliphoth Bloom tree whose center is in the same chunk as the rite.
	 * Destroys the physical multi-block and removes the SavedData entry.
	 */
	private static void completePruningOfQliphoth(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();

		QliphothBloomSavedData.BloomEntry removed = data.removeBloomInChunk(center, dimension);

		if (removed != null) {
			// Destroy the physical bloom block and its fillers
			BlockPos bloomPos = removed.center();
			BlockState bloomState = sLevel.getBlockState(bloomPos);
			if (bloomState.getBlock() instanceof com.vincenthuto.hemomancy.common.block.IMultiBlock multiBlock) {
				multiBlock.removeFillers(sLevel, bloomPos);
			}
			sLevel.removeBlock(bloomPos, false);

			// Sync updated bloom list to all clients
			syncQliphothBlooms(sLevel.getServer());

			caster.displayClientMessage(
					Component.literal("The Qliphoth withers... the dark tree has been pruned.")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					false);
			sLevel.playSound(null, center, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.BLOCKS, 0.6f, 1.2f);
		} else {
			caster.displayClientMessage(
					Component.literal("There is no Qliphoth bloom rooted in this chunk to prune.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
		}
	}

	/**
	 * Rite of Sanguine Fervor (Lesser):
	 * Registers a time-limited spawn-boost zone centred on the rite. For
	 * {@link #SANGUINE_FERVOR_DURATION_TICKS} ticks (5 minutes) natural mob
	 * spawns within a {@link #SANGUINE_FERVOR_CHUNK_RADIUS}-chunk radius are
	 * force-allowed regardless of the global mob cap, greatly increasing local
	 * mob density and making the area ideal for farming.
	 */
	private static void completeSanguineFervor(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		SanguineFervorSavedData data = SanguineFervorSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		long expiryTick = sLevel.getGameTime() + SANGUINE_FERVOR_DURATION_TICKS;

		SanguineFervorSavedData.FervorEntry entry = new SanguineFervorSavedData.FervorEntry(
				caster.getUUID(), center, dimension, SANGUINE_FERVOR_CHUNK_RADIUS, expiryTick);
		data.addEntry(entry);

		int blockRadius = SANGUINE_FERVOR_CHUNK_RADIUS * 16;
		long durationMinutes = SANGUINE_FERVOR_DURATION_TICKS / 1200;
		caster.displayClientMessage(
				Component.literal("The blood heats the earth! Mobs will swarm within "
						+ blockRadius + " blocks for " + durationMinutes + " minutes.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	/**
	 * Syncs all Qliphoth Bloom locations to each connected client for rendering.
	 * Each player receives only the blooms in their current dimension.
	 */
	private static void syncQliphothBlooms(net.minecraft.server.MinecraftServer server) {
		QliphothBloomSavedData data = QliphothBloomSavedData.get(server.overworld());
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			String dimension = player.level().dimension().location().toString();
			java.util.List<com.vincenthuto.hemomancy.client.data.QliphothBloomClientData.BloomEntry> clientEntries = new ArrayList<>();
			for (QliphothBloomSavedData.BloomEntry bloom : data.getBlooms()) {
				if (bloom.dimension().equals(dimension)) {
					clientEntries.add(new com.vincenthuto.hemomancy.client.data.QliphothBloomClientData.BloomEntry(
							bloom.center(), bloom.chunkRadius()));
				}
			}
			com.vincenthuto.hemomancy.common.network.capa.PacketSyncQliphothBlooms packet =
					new com.vincenthuto.hemomancy.common.network.capa.PacketSyncQliphothBlooms(clientEntries);
			PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> player), packet);
		}
	}

	/**
	 * Public helper called by {@link com.vincenthuto.hemomancy.common.block.functional.QliphothBloomBlock#onRemove}
	 * when the bloom block is broken (directly or via filler destruction).
	 * Removes the SavedData entry and syncs clients.
	 */
	public static void removeBloomAt(net.minecraft.world.level.Level level, BlockPos pos) {
		if (!(level instanceof ServerLevel sLevel)) return;
		ServerLevel overworld = sLevel.getServer().overworld();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		data.removeBloomInChunk(pos, dimension);
		syncQliphothBlooms(sLevel.getServer());
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

	// ────────────────────────────────────────────────────────────────────────
	// Unstained Rite Completion Handlers
	// ────────────────────────────────────────────────────────────────────────

	/**
	 * Rite of Lethean Baptism (Minor, 0 blood):
	 * Entry rite that formally begins the Unstained path. Grants starting
	 * purity and sets the purification flag.
	 */
	private static void completeLetheanBaptism(ServerLevel sLevel, ServerPlayer caster) {
		caster.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(unstained -> {
			if (!unstained.hasBegunPurification()) {
				unstained.setBegunPurification(true);
			}
			unstained.addPurity(5.0f);
			UnstainedProgressEvents.syncProgress(caster, unstained);
		});

		caster.displayClientMessage(
				Component.literal("The still waters wash over you. The Unstained path has begun.")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
				false);
		sLevel.sendParticles(ParticleTypes.END_ROD,
				caster.getX(), caster.getY() + 1.0, caster.getZ(),
				30, 0.5, 1.0, 0.5, 0.05);
	}

	/**
	 * Rite of the Silver Veil (Lesser, 0 blood):
	 * Grants the Silver Ward mob effect for 30 minutes and adds 10 purity.
	 * Requires purity >= 25 (Tainted stage).
	 */
	private static void completeSilverVeil(ServerLevel sLevel, ServerPlayer caster) {
		caster.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(unstained -> {
			if (unstained.getPurity() < 25.0f) {
				caster.displayClientMessage(
						Component.literal("Your soul is not yet pure enough to bear the Silver Veil.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						false);
				return;
			}
			unstained.addPurity(10.0f);
			UnstainedProgressEvents.syncProgress(caster, unstained);

			// Apply Silver Ward effect (amplifier 1, 30 minutes)
			caster.addEffect(new MobEffectInstance(
					EffectInit.silver_ward.get(), SILVER_VEIL_DURATION_TICKS, 1, false, true, true));

			caster.displayClientMessage(
					Component.literal("A veil of pale silver light surrounds you. Blood magic cannot touch you.")
							.withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC),
					false);
			sLevel.sendParticles(ParticleTypes.END_ROD,
					caster.getX(), caster.getY() + 1.0, caster.getZ(),
					50, 1.0, 1.5, 1.0, 0.02);
		});
	}

	/**
	 * Rite of Clarity Ascension (Greater, 0 blood):
	 * Unlocks the clarity phase for a fully purified Unstained player.
	 * Requires purity = 100 (Purified stage).
	 */
	private static void completeClarityAscension(ServerLevel sLevel, ServerPlayer caster) {
		caster.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(unstained -> {
			if (!unstained.isPurified()) {
				caster.displayClientMessage(
						Component.literal("You must achieve full purity before ascending to clarity.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						false);
				return;
			}
			if (unstained.hasClarityUnlocked()) {
				caster.displayClientMessage(
						Component.literal("Clarity has already been unlocked within you.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						false);
				return;
			}

			unstained.setClarityUnlocked(true);
			UnstainedProgressEvents.syncProgress(caster, unstained);

			caster.displayClientMessage(
					Component.literal("The veil parts. True sight is yours — clarity has been unlocked.")
							.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
					false);
			caster.displayClientMessage(
					Component.literal("Blood magic is no longer your domain. Walk the path of enlightenment.")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					false);
			sLevel.sendParticles(ParticleTypes.END_ROD,
					caster.getX(), caster.getY() + 1.0, caster.getZ(),
					80, 1.5, 2.0, 1.5, 0.03);

			com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter.grantIfNotDone(
					caster, com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter.ADV_CLARITY_AWAKENED);
		});
	}

	/**
	 * Rite of Lethean Judgment (Grand, 0 blood):
	 * Offensive rite that disrupts all blood-active Hemomancers within 16
	 * blocks, applying Hemolysis and stripping active blood effects.
	 */
	private static void completeLetheanJudgment(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		AABB area = new AABB(center).inflate(LETHEAN_JUDGMENT_RADIUS);
		List<ServerPlayer> nearbyPlayers = sLevel.getEntitiesOfClass(
				ServerPlayer.class, area, p -> p != caster);

		int[] affected = {0};
		for (ServerPlayer target : nearbyPlayers) {
			target.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
				if (volume.isActive()) {
					// Apply Hemolysis effect (amplifier 2, 30 seconds)
					target.addEffect(new MobEffectInstance(
							EffectInit.hemolysis.get(), 600, 2, false, true, true));

					// Disrupt vascular system
					target.getCapability(VascularSystemProvider.VASCULAR_CAPA).ifPresent(vascular -> {
						Map<EnumVeinSections, Float> sys = vascular.getVascularSystem();
						for (EnumVeinSections section : EnumVeinSections.values()) {
							float current = sys.getOrDefault(section, 100f);
							sys.put(section, Math.max(0f, current - 30f));
						}
						vascular.setVascularSystem(sys);
						VascularSystemEvents.syncVascular(target, vascular);
					});

					target.displayClientMessage(
							Component.literal("A wave of silver light burns through your veins!")
									.withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD),
							false);
					affected[0]++;
				}
			});
		}

		String msg = affected[0] > 0
				? "Lethean judgment descends. " + affected[0] + " hemomancer(s) have been purged."
				: "Lethean judgment descends, but no blood-wielders were found nearby.";
		caster.displayClientMessage(
				Component.literal(msg).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
				false);
		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				100, LETHEAN_JUDGMENT_RADIUS * 0.5, 2.0, LETHEAN_JUDGMENT_RADIUS * 0.5, 0.01);
	}

	// ════════════════════════════════════════════════════════════
	//  SILVER DAWN — Persistent Cleansed Zone
	// ════════════════════════════════════════════════════════════

	/** Radius of the Silver Dawn cleansing zone in blocks. */
	private static final int SILVER_DAWN_RADIUS = 8;
	/** Duration of the Verdigris Aura granted by Silver Dawn (10 minutes). */
	private static final int SILVER_DAWN_AURA_DURATION = 12000;
	/** Amplifier of the Verdigris Aura granted by Silver Dawn. */
	private static final int SILVER_DAWN_AURA_AMPLIFIER = 2;

	/**
	 * Lazy block-conversion map for Silver Dawn / Consecration.
	 * Maps blood-faction blocks to their cleansed equivalents.
	 * Unstained rites have zero blood cost by design — they draw
	 * from purity and clarity, not from the hemomancer's reservoir.
	 */
	private static Map<Block, Block> SILVER_DAWN_CONVERSIONS;

	private static Map<Block, Block> getSilverDawnConversions() {
		if (SILVER_DAWN_CONVERSIONS == null) {
			SILVER_DAWN_CONVERSIONS = Map.of(
					BlockInit.venous_stone.get(), BlockInit.cleansed_stone.get(),
					BlockInit.sanguine_glass.get(), BlockInit.cleansed_sanguine_glass.get(),
					BlockInit.infested_venous_stone.get(), BlockInit.cleansed_stone.get(),
					BlockInit.hematic_iron_block.get(), BlockInit.pale_silver_block.get()
			);
		}
		return SILVER_DAWN_CONVERSIONS;
	}

	/**
	 * Rite of the Silver Dawn: converts blood-faction blocks in a radius
	 * around the altar into their cleansed equivalents and grants a
	 * long-duration Verdigris Aura to the caster.
	 */
	private static void completeSilverDawn(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		// Require clarity to perform this rite
		boolean hasClarityUnlocked = caster.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
				.map(p -> p.hasClarityUnlocked()).orElse(false);
		if (!hasClarityUnlocked) {
			caster.displayClientMessage(
					Component.literal("You must have unlocked Clarity to perform the Rite of the Silver Dawn.")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Convert blood-faction blocks in radius
		int converted = 0;
		Map<Block, Block> conversions = getSilverDawnConversions();

		for (int x = -SILVER_DAWN_RADIUS; x <= SILVER_DAWN_RADIUS; x++) {
			for (int y = -SILVER_DAWN_RADIUS / 2; y <= SILVER_DAWN_RADIUS / 2; y++) {
				for (int z = -SILVER_DAWN_RADIUS; z <= SILVER_DAWN_RADIUS; z++) {
					BlockPos pos = center.offset(x, y, z);
					Block block = sLevel.getBlockState(pos).getBlock();
					Block replacement = conversions.get(block);
					if (replacement != null) {
						sLevel.setBlock(pos, replacement.defaultBlockState(), 3);
						converted++;
					}
				}
			}
		}

		// Grant extended Verdigris Aura
		caster.addEffect(new MobEffectInstance(
				EffectInit.verdigris_aura.get(), SILVER_DAWN_AURA_DURATION, SILVER_DAWN_AURA_AMPLIFIER, false, true, true));

		// Grant purity/clarity boost
		caster.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(progress -> {
			progress.addClarity(5.0f);
			UnstainedProgressEvents.syncProgress(caster, progress);
		});

		String msg = converted > 0
				? "Silver dawn breaks. " + converted + " block(s) have been cleansed."
				: "Silver dawn breaks, but no blood-stained blocks were found nearby.";
		caster.displayClientMessage(
				Component.literal(msg).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
				false);

		// Visual burst
		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				150, SILVER_DAWN_RADIUS * 0.5, 3.0, SILVER_DAWN_RADIUS * 0.5, 0.02);
		sLevel.sendParticles(ParticleTypes.SCRAPE,
				center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
				80, SILVER_DAWN_RADIUS * 0.4, 1.5, SILVER_DAWN_RADIUS * 0.4, 0.01);
	}

	// ════════════════════════════════════════════════════════════
	//  STILL WATERS — 5-minute zone of reduced magic damage
	// ════════════════════════════════════════════════════════════

	/** Radius of the Still Waters zone in blocks. */
	private static final int STILL_WATERS_RADIUS = 16;
	/** Duration of the Still Waters zone in ticks (5 minutes). */
	private static final long STILL_WATERS_DURATION_TICKS = 6000L;

	/**
	 * Rite of Still Waters (Minor, 0 blood):
	 * Creates a 5-minute zone around the altar within which all magic damage
	 * is reduced by 30%, countering Sanguine Dominion bleeds and other threats.
	 */
	private static void completeStillWaters(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		StillWatersSavedData data = StillWatersSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		long expiryTick = sLevel.getGameTime() + STILL_WATERS_DURATION_TICKS;

		StillWatersSavedData.StillWatersEntry entry = new StillWatersSavedData.StillWatersEntry(
				caster.getUUID(), center, dimension, STILL_WATERS_RADIUS, expiryTick);
		data.addEntry(entry);

		long durationMinutes = STILL_WATERS_DURATION_TICKS / 1200;
		caster.displayClientMessage(
				Component.literal("The waters grow still. Magic damage is reduced by 30% within "
						+ STILL_WATERS_RADIUS + " blocks for " + durationMinutes + " minutes.")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
				false);

		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				60, STILL_WATERS_RADIUS * 0.3, 1.5, STILL_WATERS_RADIUS * 0.3, 0.005);
	}

	// ════════════════════════════════════════════════════════════
	//  PALE CONSECRATION — 10-minute zone of hostile mob denial
	// ════════════════════════════════════════════════════════════

	/** Radius of the Pale Consecration zone in blocks. */
	private static final int PALE_CONSECRATION_RADIUS = 8;
	/** Duration of the Pale Consecration zone in ticks (10 minutes). */
	private static final long PALE_CONSECRATION_DURATION_TICKS = 12000L;

	/**
	 * Rite of Pale Consecration (Lesser, 0 blood):
	 * Consecrates the ground within a radius. Hostile mobs inside take periodic
	 * damage and Slowness I. Lasts 10 minutes.
	 */
	private static void completePaleConsecration(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		PaleConsecrationSavedData data = PaleConsecrationSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		long expiryTick = sLevel.getGameTime() + PALE_CONSECRATION_DURATION_TICKS;

		PaleConsecrationSavedData.ConsecrationEntry entry = new PaleConsecrationSavedData.ConsecrationEntry(
				caster.getUUID(), center, dimension, PALE_CONSECRATION_RADIUS, expiryTick);
		data.addEntry(entry);

		long durationMinutes = PALE_CONSECRATION_DURATION_TICKS / 1200;
		caster.displayClientMessage(
				Component.literal("The ground is consecrated. Hostile creatures will be seared within "
						+ PALE_CONSECRATION_RADIUS + " blocks for " + durationMinutes + " minutes.")
						.withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD),
				false);

		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				80, PALE_CONSECRATION_RADIUS * 0.4, 1.5, PALE_CONSECRATION_RADIUS * 0.4, 0.01);
		sLevel.sendParticles(ParticleTypes.SCRAPE,
				center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
				40, PALE_CONSECRATION_RADIUS * 0.3, 0.5, PALE_CONSECRATION_RADIUS * 0.3, 0.005);
	}

	// ════════════════════════════════════════════════════════════
	//  SILTHMERE'S REMEMBRANCE — one-time burst purity + Silver Ward
	// ════════════════════════════════════════════════════════════

	/** Radius within which Unstained players receive the Remembrance burst. */
	private static final int REMEMBRANCE_RADIUS = 32;
	/** Purity granted per Unstained player by the burst. */
	private static final float REMEMBRANCE_PURITY = 5.0f;
	/** Silver Ward refresh duration in ticks (15 minutes). */
	private static final int REMEMBRANCE_SILVER_WARD_DURATION = 18000;

	/**
	 * Rite of Silthmere's Remembrance (Greater, 0 blood):
	 * A one-time burst. All Unstained players within 32 blocks immediately gain
	 * +5 purity and have their Silver Ward refreshed or applied (amplifier 1).
	 */
	private static void completeSilthmereRemembrance(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		AABB area = new AABB(center).inflate(REMEMBRANCE_RADIUS);
		List<ServerPlayer> nearby = sLevel.getEntitiesOfClass(
				ServerPlayer.class, area, p -> true);

		int[] affected = {0};
		for (ServerPlayer target : nearby) {
			target.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(progress -> {
				if (!progress.hasBegunPurification()) return;

				progress.addPurity(REMEMBRANCE_PURITY);
				UnstainedProgressEvents.syncProgress(target, progress);

				target.addEffect(new MobEffectInstance(
						EffectInit.silver_ward.get(), REMEMBRANCE_SILVER_WARD_DURATION, 1,
						false, true, true));

				target.displayClientMessage(
						Component.literal("Silthmere's memory washes over you. Purity blooms within.")
								.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
						false);
				affected[0]++;
			});
		}

		String msg = affected[0] > 0
				? "Silthmere remembers. " + affected[0] + " Unstained soul(s) have been blessed."
				: "Silthmere's remembrance echoes, but no Unstained walk near enough to hear it.";
		caster.displayClientMessage(
				Component.literal(msg).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC),
				false);

		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				120, REMEMBRANCE_RADIUS * 0.3, 3.0, REMEMBRANCE_RADIUS * 0.3, 0.02);
	}

	// ════════════════════════════════════════════════════════════
	//  LETHE COVENANT — grand 30-minute Unstained domain
	// ════════════════════════════════════════════════════════════

	/** Chunk radius of the Lethe Covenant domain. */
	private static final int LETHE_COVENANT_CHUNK_RADIUS = 5;
	/** Duration of the Lethe Covenant domain in ticks (30 minutes). */
	private static final long LETHE_COVENANT_DURATION_TICKS = 36000L;

	/**
	 * Rite of the Lethe Covenant (Grand, 0 blood):
	 * Establishes a grand Unstained domain for 30 minutes within a 5-chunk radius.
	 * The domain suppresses mob spawns, shields Silver Ward players from bleed,
	 * and slowly grows the purity of Unstained players inside it.
	 */
	private static void completeLetheCovenantRite(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		LetheCovenantSavedData data = LetheCovenantSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		long expiryTick = sLevel.getGameTime() + LETHE_COVENANT_DURATION_TICKS;

		LetheCovenantSavedData.CovenantEntry entry = new LetheCovenantSavedData.CovenantEntry(
				caster.getUUID(), center, dimension, LETHE_COVENANT_CHUNK_RADIUS, expiryTick);
		data.addEntry(entry);

		int blockRadius = LETHE_COVENANT_CHUNK_RADIUS * 16;
		long durationMinutes = LETHE_COVENANT_DURATION_TICKS / 1200;
		caster.displayClientMessage(
				Component.literal("The Lethe Covenant is sealed! A domain of stillness spreads "
						+ blockRadius + " blocks in every direction for " + durationMinutes + " minutes.")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
				false);
		caster.displayClientMessage(
				Component.literal("Spawns are halved. Bleed cannot touch those warded in silver. Purity grows.")
						.withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC),
				false);

		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				200, blockRadius * 0.2, 4.0, blockRadius * 0.2, 0.02);
	}

}
