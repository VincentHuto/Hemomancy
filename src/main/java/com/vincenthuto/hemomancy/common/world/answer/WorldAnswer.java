package com.vincenthuto.hemomancy.common.world.answer;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodStainBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VampireBatEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
import com.vincenthuto.hemomancy.config.HemoConfigValues;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The world's answer (core-loop Track 10): what reads the blood counted in {@link BloodSpilledLedger}. The Blood Moon
 * roll asks {@link #moonChanceTonight}; the Wills' director {@link #willMultiplier}; wet stains near a player draw a
 * vampire bat at night or underground ({@link #tickScavengers}); a strong chum pool draws a drowned
 * ({@link #drawHunter}); a dry stain fading over grass in a heavy region leaves stained sod ({@link #stainFaded}).
 * The numbers are {@link WorldAnswerRules}.
 */
public final class WorldAnswer {
	private WorldAnswer() {
	}

	// ── Counting ──

	/** A stain of {@code ml} laid at {@code cell}, by a player or a body. */
	public static void stainLaid(ServerLevel level, BlockPos cell, double ml) {
		BloodSpilledLedger.spill(level, cell, ml * WorldAnswerRules.STAIN_WEIGHT);
	}

	/** A stain face taken back up at {@code cell} by Absorption, or drunk by a scavenger: its blood leaves the count. */
	public static void stainCleaned(ServerLevel level, BlockPos cell) {
		BloodSpilledLedger.clean(level, cell, GroundRunRules.STAIN_ML * WorldAnswerRules.STAIN_WEIGHT);
	}

	/** Chum poured at {@code pos}. */
	public static void chumPoured(ServerLevel level, BlockPos pos, double ml) {
		BloodSpilledLedger.spill(level, pos, ml * WorldAnswerRules.CHUM_WEIGHT);
	}

	/** A creature a Harbinger killed, bleeding where it fell; kills alone never lift a region past uneasy (CB-26). */
	public static void killed(ServerLevel level, LivingEntity victim) {
		BloodSpilledLedger.spillKill(level, victim.blockPosition(), WorldAnswerRules.killSpill(victim.getMaxHealth()));
	}

	// ── The Blood Moon ──

	/** The highest pressure under any living player in {@code level}: the Blood Moon reads where players are. */
	public static double pressureUnderPlayers(ServerLevel level) {
		double highest = 0.0D;
		for (ServerPlayer player : level.getPlayers(ServerPlayer::isAlive)) {
			highest = Math.max(highest, BloodSpilledLedger.pressure(level, player.blockPosition()));
		}
		return highest;
	}

	/** Tonight's chance of a Blood Moon in {@code level}: the moon's phase, raised by the count where players stand. */
	public static double moonChanceTonight(ServerLevel level) {
		return WorldAnswerRules.moonChance(level.getMoonPhase(), pressureUnderPlayers(level));
	}

	/**
	 * Tonight's chance read at {@code pos}: what an instrument standing there shows. The Blood Moon rises only over the
	 * Overworld, so an instrument in another dimension (whose moon is fixed) shows the Overworld's roll.
	 */
	public static double moonChanceAt(ServerLevel level, BlockPos pos) {
		ServerLevel overworld = level.getServer().overworld();
		if (level != overworld) return moonChanceTonight(overworld);
		return WorldAnswerRules.moonChance(level.getMoonPhase(), BloodSpilledLedger.pressure(level, pos));
	}

	/**
	 * A Blood Moon rose that the phase alone would not have raised ({@link WorldAnswerRules#broughtOn}): every living
	 * player in {@code level} with their blood awake who stands where blood has been counted brought it on.
	 */
	public static void creditMoonBroughtOn(ServerLevel level) {
		for (ServerPlayer player : level.getPlayers(ServerPlayer::isAlive)) creditMoonBroughtOn(level, player);
	}

	/** One player's share of {@link #creditMoonBroughtOn(ServerLevel)}: returns whether they were credited. */
	public static boolean creditMoonBroughtOn(ServerLevel level, ServerPlayer player) {
		if (!player.isAlive() || !HemoCapabilityAccess.getBloodVolume(player).isActive()) return false;
		if (BloodSpilledLedger.pressure(level, player.blockPosition()) <= 0.0D) return false;
		HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_MOON_BROUGHT_ON);
		return true;
	}

	/** Nights to the Overworld's next full moon, the one the Blood Moon follows. */
	public static int nightsToFullMoon(ServerLevel level) {
		return WorldAnswerRules.nightsToFullMoon(level.getServer().overworld().getMoonPhase());
	}

	// ── Wills ──

	public static double willMultiplier(ServerLevel level, BlockPos pos) {
		return WorldAnswerRules.willMultiplier(BloodSpilledLedger.pressure(level, pos));
	}

	// ── Scavengers ──

	/** The wet stains within {@code radius} (and half that up and down) of {@code centre}. */
	public static List<BlockPos> wetStains(Level level, BlockPos centre, int radius) {
		List<BlockPos> found = new ArrayList<>();
		int vertical = Math.max(2, radius / 2);
		for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-radius, -vertical, -radius), centre.offset(radius, vertical, radius))) {
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof BloodStainBlock && state.getValue(BloodStainBlock.AGE) == GroundRunRules.WET) {
				found.add(pos.immutable());
			}
		}
		return found;
	}

	/** Whether the world sends scavengers and hunters at all: the server config, and never on Peaceful. */
	public static boolean scavengersAwake(ServerLevel level) {
		return HemoConfigValues.get(HemoServerConfig.WORLD_ANSWER_SCAVENGERS, true) && level.getDifficulty() != Difficulty.PEACEFUL;
	}

	/** A level's scavenger check: each player, staggered, every {@link WorldAnswerRules#SCAVENGER_CHECK_TICKS}. */
	public static void tickScavengers(ServerLevel level) {
		if (!scavengersAwake(level)) return;
		for (ServerPlayer player : level.getPlayers(p -> p.isAlive() && !p.isSpectator())) {
			if ((level.getGameTime() + player.getId() * 37L) % WorldAnswerRules.SCAVENGER_CHECK_TICKS != 0L) continue;
			drawScavenger(level, player.blockPosition(), level.random);
		}
	}

	/**
	 * Wet stains near {@code centre} draw a vampire bat, at night or out of the sky's sight, more likely the heavier the
	 * region; at most {@link WorldAnswerRules#SCAVENGER_CAP} near. Returns the bat, or null.
	 */
	@Nullable
	public static VampireBatEntity drawScavenger(ServerLevel level, BlockPos centre, RandomSource random) {
		List<BlockPos> stains = wetStains(level, centre, WorldAnswerRules.SCAVENGER_RADIUS);
		if (stains.size() < WorldAnswerRules.SCAVENGER_STAINS) return null;
		BlockPos stain = stains.get(random.nextInt(stains.size()));
		if (!level.isNight() && openToSky(level, stain)) return null;
		AABB near = new AABB(centre).inflate(WorldAnswerRules.SCAVENGER_RADIUS * 2.0D);
		if (level.getEntitiesOfClass(VampireBatEntity.class, near, bat -> bat.isAlive() && !bat.isManifestedAggregate()).size()
				>= WorldAnswerRules.SCAVENGER_CAP) return null;
		if (random.nextDouble() >= WorldAnswerRules.scavengerChance(stains.size(), BloodSpilledLedger.band(level, centre))) return null;
		BlockPos at = openAirNear(level, stain, random);
		if (at == null) return null;
		return EntityInit.vampire_bat.get().spawn(level, at, MobSpawnType.EVENT);
	}

	/**
	 * Whether nothing solid stands over {@code pos}: the column's motion-blocking height is at or below it. Read from
	 * the heightmap, which a block change updates at once, not from sky light, which the light engine settles later.
	 * A ceiling of any kind (a roof, a cave, the Nether's) hides a stain from the sky.
	 */
	public static boolean openToSky(Level level, BlockPos pos) {
		return level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) <= pos.getY();
	}

	@Nullable
	private static BlockPos openAirNear(ServerLevel level, BlockPos stain, RandomSource random) {
		for (int attempt = 0; attempt < 12; attempt++) {
			BlockPos pos = stain.offset(random.nextInt(9) - 4, 2 + random.nextInt(4), random.nextInt(9) - 4);
			if (level.isLoaded(pos) && level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isAir()
					&& level.getFluidState(pos).isEmpty()) return pos;
		}
		return null;
	}

	/** Monsters a chum pool draws; one of them at most near a pool. */
	public static final TagKey<EntityType<?>> CHUM_HUNTERS = TagKey.create(Registries.ENTITY_TYPE, Hemomancy.rloc("chum_hunters"));
	/** The Brined Court's home waters, as the chum catch reads them. */
	public static final TagKey<Biome> HOME_WATERS = TagKey.create(Registries.BIOME, Hemomancy.rloc("chum_home_waters"));

	/**
	 * A strong chum pool draws one of the water's hunters at night or in an uneasy region, at most
	 * {@link WorldAnswerRules#HUNTER_CAP} of them near the pool. Which hunter depends on the water at the pool
	 * ({@link #hunterWatersAt}). Returns it, or null.
	 */
	@Nullable
	public static Entity drawHunter(ServerLevel level, Vec3 centre, double radius, double strength, int depth, RandomSource random) {
		return drawHunter(level, centre, radius, strength, depth, random, hunterWatersAt(level, BlockPos.containing(centre)));
	}

	/** The waters at {@code pos}: the Court's home waters, a swamp, or open water. */
	public static WorldAnswerRules.HunterWaters hunterWatersAt(ServerLevel level, BlockPos pos) {
		Holder<Biome> biome = level.getBiome(pos);
		return WorldAnswerRules.hunterWaters(biome.is(HOME_WATERS), biome.is(Tags.Biomes.IS_SWAMP));
	}

	/**
	 * The draw for given waters: a Brined Votary rises from home waters, a Bog Revenant hauls itself out of a swamp onto
	 * the bank, a Drowned rises anywhere else. A bold upwelling of the toolkit's blood marks where it comes up.
	 */
	@Nullable
	public static Entity drawHunter(ServerLevel level, Vec3 centre, double radius, double strength, int depth, RandomSource random,
			WorldAnswerRules.HunterWaters waters) {
		BlockPos at = BlockPos.containing(centre.x, centre.y - 1.5D, centre.z);
		if (depth < WorldAnswerRules.hunterDepth(waters)
				|| !WorldAnswerRules.poolDrawsHunter(strength, level.isNight(), BloodSpilledLedger.band(level, at))) return null;
		AABB around = new AABB(centre, centre).inflate(radius + 8.0D, 8.0D, radius + 8.0D);
		if (level.getEntitiesOfClass(Mob.class, around, mob -> mob.isAlive() && mob.getType().is(CHUM_HUNTERS)).size()
				>= WorldAnswerRules.HUNTER_CAP) return null;
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double below = waters == WorldAnswerRules.HunterWaters.SWAMP ? 0.5D : 1.5D;
		BlockPos edge = BlockPos.containing(centre.x + Math.cos(angle) * radius, centre.y - below, centre.z + Math.sin(angle) * radius);
		BlockPos spawn = level.getFluidState(edge).is(FluidTags.WATER) ? edge : BlockPos.containing(centre.x, centre.y - below, centre.z);
		if (!level.getFluidState(spawn).is(FluidTags.WATER)) return null;
		EntityType<? extends Mob> type = switch (waters) {
			case HOME -> EntityInit.brined_votary.get();
			case SWAMP -> EntityInit.bog_revenant.get();
			case OPEN -> EntityType.DROWNED;
		};
		BlockPos rise = spawn;
		if (waters == WorldAnswerRules.HunterWaters.SWAMP) spawn = bankNear(level, spawn);
		Entity hunter = type.spawn(level, spawn, MobSpawnType.EVENT);
		if (hunter != null) showHunterRising(level, Vec3.atBottomCenterOf(rise), centre.y);
		return hunter;
	}

	/** A Bog Revenant walks: the nearest dry standing cell on the pool's bank, or the water cell when there is none. */
	private static BlockPos bankNear(ServerLevel level, BlockPos water) {
		BlockPos best = null;
		double bestDistance = Double.MAX_VALUE;
		for (BlockPos pos : BlockPos.betweenClosed(water.offset(-3, -1, -3), water.offset(3, 2, 3))) {
			if (!level.getFluidState(pos).isEmpty() || !level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) continue;
			BlockPos floor = pos.below();
			if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) continue;
			double distance = pos.distToCenterSqr(water.getX() + 0.5D, water.getY() + 0.5D, water.getZ() + 0.5D);
			if (distance < bestDistance) {
				bestDistance = distance;
				best = pos.immutable();
			}
		}
		return best != null ? best : water;
	}

	/**
	 * The water opens where a hunter comes up: a broad, dense bloom of blood on the surface, the toolkit's pool spreading
	 * from the spot, and a column of blood cells boiling up through it. Bold on purpose: it is the world answering the
	 * pool, seen from the shore.
	 */
	private static void showHunterRising(ServerLevel level, Vec3 rise, double surfaceY) {
		Vec3 surface = new Vec3(rise.x, surfaceY, rise.z);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.CHUM_BLOOM, surface, surface, 3.5D, 80, 100);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.CHUM_BLOOM, surface, surface, 1.6D, 60, 100);
		// The water breaks over it: a splash of blood thrown up where it rises, and the toolkit's impact on the surface.
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.BLOOD_SPLASH, surface, surface.add(0.0D, 1.2D, 0.0D), 2.5D, 22);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.ANIMUS_IMPACT, surface, surface.add(0.0D, 0.6D, 0.0D), 1.6D, 16);
		ParticleColor deep = new ParticleColor(96, 6, 16);
		ParticleColor bright = new ParticleColor(214, 22, 34);
		double rising = Math.max(1.0D, surfaceY - rise.y);
		level.sendParticles(HemoParticleData.bloodCell(bright), rise.x, rise.y + rising * 0.5D, rise.z, 40, 0.35D, rising * 0.5D, 0.35D, 0.05D);
		level.sendParticles(HemoParticleData.bloodCell(deep), surface.x, surface.y + 0.05D, surface.z, 48, 1.3D, 0.05D, 1.3D, 0.03D);
		level.sendParticles(HemoParticleData.glow(bright), surface.x, surface.y + 0.2D, surface.z, 14, 0.5D, 0.25D, 0.5D, 0.02D);
		level.playSound(null, BlockPos.containing(surface), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 0.35F, 0.6F);
	}

	// ── The ground remembers ──

	/**
	 * A stain at {@code cell} has worn away. If it lay on the floor over grass in a heavy region, the grass under it
	 * may turn to stained sod; it turns back when the region quiets ({@code StainedSodBlock}). Returns whether it did.
	 */
	public static boolean stainFaded(ServerLevel level, BlockPos cell, BlockState stain, float roll) {
		if (!HemoConfigValues.get(HemoServerConfig.WORLD_ANSWER_GROUND_REMEMBERS, true)) return false;
		if (!MultifaceBlock.hasFace(stain, Direction.DOWN)) return false;
		BlockPos ground = cell.below();
		if (!level.getBlockState(ground).is(Blocks.GRASS_BLOCK)) return false;
		if (BloodSpilledLedger.band(level, ground) < WorldAnswerRules.SOD_BAND || roll >= WorldAnswerRules.SOD_CHANCE) return false;
		level.setBlockAndUpdate(ground, BlockInit.stained_sod.get().defaultBlockState());
		return true;
	}
}
