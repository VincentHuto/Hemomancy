package com.vincenthuto.hemomancy.common.entity.npc.court;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtWorldProbe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The Shared Wake's schools in one level: which 1024 x 1024 regions have already had one start, and the two ways one
 * starts. A Nursery tender launches its nursery's school the first time a player comes near; out in Pelagic Ocean a
 * school sometimes starts on its own, out of sight of a player swimming there ({@link CourtSchoolRules} for where). Each
 * region gets one, ever, so the cap holds however the school then roams.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CourtSchools extends SavedData {
	private static final String NAME = "hemomancy_court_schools";
	/** How often the natural start is tried for each player, in ticks. */
	public static final int CHECK_INTERVAL = 400;

	private final Set<Long> claimed = new HashSet<>();

	public static CourtSchools get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(CourtSchools::new, CourtSchools::load, null), NAME);
	}

	public boolean claimed(long region) {
		return claimed.contains(region);
	}

	/** Takes the region for a school; false when it has had one already. */
	public boolean claim(long region) {
		if (!claimed.add(region)) return false;
		setDirty();
		return true;
	}

	/**
	 * A school out of the nursery ground the tender keeps: the region's one school, swimming its loop round the water
	 * above the nursery. Empty when the region has had its school.
	 */
	public List<CourtMemberEntity> launchFromNursery(ServerLevel level, CourtMemberEntity tender) {
		BlockPos home = tender.home();
		if (home == null || !claim(CourtSchoolRules.region(home.getX(), home.getZ()))) return List.of();
		BlockPos centre = home.above(6);
		if (!level.getFluidState(centre).is(FluidTags.WATER)) centre = home.above(2);
		return launch(level, centre, CourtSchoolRules.LOOP_RADIUS, level.getRandom(), MobSpawnType.STRUCTURE);
	}

	/**
	 * Sets a school down at {@code centre}: four to six Edge-swimmers, the first the leader, the rest on their stations,
	 * all bound to one school id and one loop. Nothing here checks the cap; callers claim the region first.
	 */
	public static List<CourtMemberEntity> launch(ServerLevel level, BlockPos centre, int radius, RandomSource random,
			MobSpawnType reason) {
		var type = EntityInit.court_members.get(CourtRole.EDGE_SWIMMER);
		if (type == null) return List.of();
		UUID school = UUID.randomUUID();
		int size = CourtSchoolRules.size(random.nextDouble());
		float yaw = random.nextFloat() * 360.0F;
		List<CourtMemberEntity> members = new ArrayList<>();
		for (int slot = 0; slot < size; slot++) {
			CourtMemberEntity member = type.get().create(level);
			if (member == null) continue;
			double[] station = slot == 0 ? new double[] {centre.getX() + 0.5D, 0, centre.getZ() + 0.5D}
					: CourtSchoolRules.stationAt(centre.getX() + 0.5D, centre.getZ() + 0.5D, yaw, slot);
			BlockPos at = BlockPos.containing(station[0], centre.getY() + station[1], station[2]);
			if (!level.getFluidState(at).is(FluidTags.WATER)) at = centre;
			member.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, yaw, 0.0F);
			member.joinSchool(school, slot, centre, radius);
			member.finalizeSpawn(level, level.getCurrentDifficultyAt(at), reason, null);
			level.addFreshEntity(member);
			members.add(member);
		}
		return members;
	}

	/** Tries a natural start near one player; the school, or empty. */
	public List<CourtMemberEntity> tryNatural(ServerLevel level, ServerPlayer player, @Nullable CourtWorldProbe probe) {
		if (probe == null || !player.isAlive() || player.isSpectator()) return List.of();
		RandomSource random = level.getRandom();
		double angle = random.nextDouble() * Math.PI * 2.0D;
		int distance = CourtSchoolRules.SPAWN_MIN + random.nextInt(CourtSchoolRules.SPAWN_MAX - CourtSchoolRules.SPAWN_MIN + 1);
		int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
		int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
		long region = CourtSchoolRules.region(x, z);
		if (claimed(region) || !CourtSchoolRules.regionHasSchool(level.getSeed(), region)) return List.of();
		BlockPos column = new BlockPos(x, level.getSeaLevel(), z);
		if (!level.isLoaded(column)) return List.of();
		int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
		int surface = level.getSeaLevel() - 1;
		if (surface - floor < CourtSchoolRules.MIN_DEPTH) return List.of();
		BlockPos at = new BlockPos(x, floor + (surface - floor) / 2, z);
		for (int dy = -1; dy <= 1; dy++) if (!level.getFluidState(at.above(dy)).is(FluidTags.WATER)) return List.of();
		var layer = CourtWorldProbe.layerOf(level.getBiome(at));
		boolean nearSeat = layer != null && probe.nearSeat(x, z, CourtSchoolRules.SEAT_CLEARANCE);
		if (!CourtSchoolRules.mayStartNaturally(false, true, layer, nearSeat) || !claim(region)) return List.of();
		return launch(level, at, CourtSchoolRules.LOOP_RADIUS, random, MobSpawnType.NATURAL);
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % CHECK_INTERVAL != 0) return;
		if (level.players().isEmpty() || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
				|| !EntityInit.court_members.containsKey(CourtRole.EDGE_SWIMMER)) return;
		var source = level.getChunkSource();
		CourtWorldProbe probe = CourtWorldProbe.of(level.registryAccess(), source.getGenerator().getBiomeSource(),
				source.randomState(), level.getSeed());
		if (probe == null) return;
		CourtSchools schools = get(level);
		for (ServerPlayer player : level.players()) schools.tryNatural(level, player, probe);
	}

	private static CourtSchools load(CompoundTag tag, HolderLookup.Provider registries) {
		CourtSchools data = new CourtSchools();
		for (long region : tag.getLongArray("Claimed")) data.claimed.add(region);
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		tag.put("Claimed", new LongArrayTag(claimed.stream().mapToLong(Long::longValue).toArray()));
		return tag;
	}
}
