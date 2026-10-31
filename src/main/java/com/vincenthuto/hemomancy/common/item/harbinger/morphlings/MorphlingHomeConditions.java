package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicBiomes;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Each strain's home condition (spec, "Home condition"), one predicate per strain, plus the cached
 * {@link #isHome(Player, MorphlingStrain)} that powers and Track 3's tint and HUD read. The block scans run at most
 * once a second per host.
 */
public final class MorphlingHomeConditions {
	/** Blocks above the eye that still count as "within a few blocks of a daylit surface". */
	public static final int SUNLIT_WATER_DEPTH = 5;
	public static final double COMPANION_RANGE = 8.0D;
	public static final int STILL_HOME_TICKS = 20;
	public static final int HEAT_SCAN_RADIUS = 4;
	public static final float COLD_BIOME_TEMPERATURE = 0.15F;

	private static final Map<UUID, Cached> CACHE = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> LAST_KILL = new ConcurrentHashMap<>();

	private record Cached(long second, MorphlingStrain strain, boolean home) {
	}

	private MorphlingHomeConditions() {
	}

	/** Whether {@code player} is at home for {@code strain}, refreshed once a second. */
	public static boolean isHome(Player player, MorphlingStrain strain) {
		if (player == null || strain == null) return false;
		long second = player.level().getGameTime() / 20L;
		Cached cached = CACHE.get(player.getUUID());
		if (cached != null && cached.second() == second && cached.strain() == strain) return cached.home();
		boolean home = strain.homeCondition().isHome(player);
		CACHE.put(player.getUUID(), new Cached(second, strain, home));
		return home;
	}

	/** Whether the stack's own strain is at home on its host. */
	public static boolean isHome(Player player, ItemStack stack) {
		return MorphlingStrains.of(stack).map(strain -> isHome(player, strain)).orElse(false);
	}

	public static void forget(Player player) {
		CACHE.remove(player.getUUID());
	}

	public static void recordKill(Player player) {
		LAST_KILL.put(player.getUUID(), player.level().getGameTime());
		forget(player);
	}

	public static void clearSessionState() {
		CACHE.clear();
		LAST_KILL.clear();
	}

	public static void logout(UUID player) {
		CACHE.remove(player);
	}

	public static boolean sunburst(Player player) {
		Level level = player.level();
		if (!level.isDay()) return false;
		BlockPos eye = BlockPos.containing(player.getEyePosition());
		if (!player.isInWater()) return level.canSeeSky(eye);
		BlockPos.MutableBlockPos cursor = eye.mutable();
		for (int i = 0; i <= SUNLIT_WATER_DEPTH; i++) {
			if (!level.getFluidState(cursor).is(FluidTags.WATER)) {
				return level.getBlockState(cursor).isAir() && level.canSeeSky(cursor);
			}
			cursor.move(Direction.UP);
		}
		return false;
	}

	public static boolean holdfast(Player player) {
		if (player.isUnderWater()) return true;
		Level level = player.level();
		BlockPos feet = player.blockPosition();
		if (isStoneType(level.getBlockState(feet.below()))) return true;
		for (Direction side : Direction.Plane.HORIZONTAL) {
			if (isStoneType(level.getBlockState(feet.relative(side)))
					|| isStoneType(level.getBlockState(feet.above().relative(side)))) return true;
		}
		return false;
	}

	public static boolean mermaidsPurse(Player player) {
		if (player.isSwimming() && player.isSprinting()) return true;
		if (player.isInWater() && isOpenOcean(player)) return true;
		return !MorphlingAllies.allies(player, COMPANION_RANGE).isEmpty();
	}

	public static boolean deadMansRope(Player player) {
		return player.isUnderWater() || MorphlingLinks.count(player) > 0;
	}

	public static boolean inkCap(Player player) {
		if (player.isUnderWater()) return true;
		return player.level().getMaxLocalRawBrightness(BlockPos.containing(player.getEyePosition())) <= 7;
	}

	public static boolean hushbell(Player player) {
		Level level = player.level();
		if (level.getBiome(player.blockPosition()).value().getBaseTemperature() < COLD_BIOME_TEMPERATURE) return true;
		if (player.isUnderWater() && player.getY() < level.getSeaLevel()) return true;
		return MorphlingAnchorState.isStill(player, STILL_HOME_TICKS);
	}

	public static boolean marrowplume(Player player) {
		Long kill = LAST_KILL.get(player.getUUID());
		if (kill != null && player.level().getGameTime() - kill <= MarrowplumeRules.HOME_KILL_WINDOW) return true;
		int r = (int) MarrowplumeRules.HOME_BONE_RANGE;
		BlockPos origin = player.blockPosition();
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-r, -4, -r), origin.offset(r, 4, r))) {
			if (player.level().getBlockState(pos).is(Blocks.BONE_BLOCK)) return true;
		}
		return false;
	}

	public static boolean kinbud(Player player) {
		Level level = player.level();
		float temperature = level.getBiome(player.blockPosition()).value().getBaseTemperature();
		int heat = MorphlingHeatRules.environmentLevel(temperature, level.dimensionType().ultraWarm(),
				player.isOnFire(), player.isInLava());
		if (heat >= MorphlingHeatRules.HOT) return true;
		if (level.getBiome(player.blockPosition()).is(PelagicBiomes.key(PelagicLayer.HYDROTHERMAL))) return true;
		BlockPos origin = player.blockPosition();
		int r = HEAT_SCAN_RADIUS;
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-r, -2, -r), origin.offset(r, 2, r))) {
			if (isHeatSource(level.getBlockState(pos))) return true;
		}
		return false;
	}

	public static boolean isStoneType(BlockState state) {
		return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER)
				|| state.is(Tags.Blocks.STONES) || state.is(Tags.Blocks.COBBLESTONES)
				|| state.is(BlockTags.STONE_BRICKS) || state.is(Blocks.CALCITE) || state.is(Blocks.DRIPSTONE_BLOCK);
	}

	public static boolean isHeatSource(BlockState state) {
		return state.is(Blocks.MAGMA_BLOCK) || state.is(BlockTags.FIRE) || state.is(BlockTags.CAMPFIRES)
				|| state.getFluidState().is(FluidTags.LAVA);
	}

	private static boolean isOpenOcean(Player player) {
		var biome = player.level().getBiome(player.blockPosition());
		if (biome.is(BiomeTags.IS_DEEP_OCEAN)) return true;
		for (PelagicLayer layer : PelagicLayer.values()) {
			if (layer != PelagicLayer.SHORE && layer != PelagicLayer.REEF && biome.is(PelagicBiomes.key(layer))) return true;
		}
		return false;
	}

}
