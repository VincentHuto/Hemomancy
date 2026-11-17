package com.vincenthuto.hemomancy.common.world.farm;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodGougeBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodStainBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.PlantingBasinBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.tile.harbinger.plant.PlantingBasinBlockEntity;
import com.vincenthuto.hemomancy.common.world.ground.GroundFunction;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;
import java.util.Collections;

/**
 * Fed ground (core-loop Track 7): reads how well the soil under a crop is fed and stamps soil that a drip or a worked
 * gutter fed. Nothing here ticks: the level is worked out when the crop asks ({@code BloodFedGrowthEvents}), from the
 * block entities and marks around it and the chunk's {@link FedGroundStamps}.
 */
public final class BloodFedGround {
	/** Plants a planting basin holds. */
	public static final TagKey<Block> PLANTING_BASIN_PLANTS = TagKey.create(Registries.BLOCK, Hemomancy.rloc("planting_basin_plants"));
	/** Crops that only advance on fed ground (the gourd stem). */
	public static final TagKey<Block> NEEDS_BLOOD = TagKey.create(Registries.BLOCK, Hemomancy.rloc("needs_blood_to_grow"));

	private BloodFedGround() {
	}

	/** How well the crop at {@code crop} is fed, with the basin that feeds it at level 2 (null for a feeding run). */
	public record Feeding(int level, @Nullable PlantingBasinBlockEntity basin) {
		public static final Feeding UNFED = new Feeding(BloodFedGrowthRules.UNFED, null);
	}

	/** The soil a crop at {@code crop} stands in. */
	public static BlockPos soilOf(BlockPos crop) {
		return crop.below();
	}

	/** How well the crop standing at {@code crop} is fed, 0 to 2 in the world (level 3 is the cloche's own). */
	public static Feeding feeding(ServerLevel level, BlockPos crop) {
		BlockPos soil = soilOf(crop);
		PlantingBasinBlockEntity basin = freshBasin(level, soil);
		if (basin == null) {
			for (Direction side : Direction.Plane.HORIZONTAL) {
				basin = freshBasin(level, soil.relative(side));
				if (basin != null) break;
			}
		}
		if (basin != null) return new Feeding(BloodFedGrowthRules.BASIN, basin);
		boolean feedRun = feedingRunBeside(level, crop);
		int fed = BloodFedGrowthRules.level(false, feedRun, stamped(level, soil), wetStainBeside(level, crop, soil));
		return fed == BloodFedGrowthRules.UNFED ? Feeding.UNFED : new Feeding(fed, null);
	}

	/**
	 * Whether {@code state} is a plant at its last age: a growth check then grows nothing more (a stem only fruits, and
	 * only where there is soil beside it). Read from the {@code age} property, so other mods' crops answer too.
	 */
	public static boolean ripe(BlockState state) {
		for (Property<?> property : state.getProperties()) {
			if (property instanceof IntegerProperty age && "age".equals(age.getName())) {
				return state.getValue(age) >= Collections.max(age.getPossibleValues());
			}
		}
		return false;
	}

	public static int level(ServerLevel level, BlockPos crop) {
		return feeding(level, crop).level();
	}

	/**
	 * Whether the crop at {@code crop} is fed by something both sides can see in block states: a planting basin showing
	 * {@link PlantingBasinBlock#FED} under or beside its soil, a lit feeding run beside it, or a wet stain beside it.
	 * Safe on the client, which uses it to predict bone meal. Soil fed only by a stamp (a drip, a gutter, Vital
	 * Effusion's soak) reads unfed here, since the chunk's stamps are server data: the client then under-predicts and the
	 * server's answer corrects it.
	 */
	public static boolean visiblyFed(Level level, BlockPos crop) {
		BlockPos soil = soilOf(crop);
		if (showsFedBasin(level, soil)) return true;
		for (Direction side : Direction.Plane.HORIZONTAL) {
			if (showsFedBasin(level, soil.relative(side))) return true;
		}
		return feedingRunBeside(level, crop) || wetStainBeside(level, crop, soil);
	}

	private static boolean showsFedBasin(Level level, BlockPos pos) {
		if (!level.isLoaded(pos)) return false;
		BlockState state = level.getBlockState(pos);
		return state.getBlock() instanceof PlantingBasinBlock && state.getValue(PlantingBasinBlock.FED);
	}

	@Nullable
	private static PlantingBasinBlockEntity freshBasin(ServerLevel level, BlockPos pos) {
		return level.isLoaded(pos) && level.getBlockEntity(pos) instanceof PlantingBasinBlockEntity basin && basin.fed() ? basin : null;
	}

	/** A lit gouge or vein whose run feeds (Track 6's bone-meal function) touching the crop's cell. */
	private static boolean feedingRunBeside(Level level, BlockPos crop) {
		for (Direction side : Direction.values()) {
			BlockState beside = level.getBlockState(crop.relative(side));
			if (beside.getBlock() instanceof BloodGougeBlock && beside.getValue(BloodGougeBlock.LIT)
					&& beside.getValue(BloodGougeBlock.FUNCTION) == GroundFunction.FEED) return true;
		}
		return false;
	}

	/**
	 * A wet stain lying beside the crop: on the soil next to it (a stain in a cell beside the crop, lying on its floor)
	 * or on the side of the crop's own soil.
	 */
	private static boolean wetStainBeside(Level level, BlockPos crop, BlockPos soil) {
		for (Direction side : Direction.Plane.HORIZONTAL) {
			BlockState besideCrop = level.getBlockState(crop.relative(side));
			if (wet(besideCrop) && MultifaceBlock.hasFace(besideCrop, Direction.DOWN)) return true;
			BlockState besideSoil = level.getBlockState(soil.relative(side));
			if (wet(besideSoil) && MultifaceBlock.hasFace(besideSoil, side.getOpposite())) return true;
		}
		return false;
	}

	private static boolean wet(BlockState state) {
		return state.getBlock() instanceof BloodStainBlock && state.getValue(BloodStainBlock.AGE) == GroundRunRules.WET;
	}

	/** Whether a drip or a gutter stamped {@code soil} and the stamp still feeds. */
	public static boolean stamped(ServerLevel level, BlockPos soil) {
		if (!level.isLoaded(soil)) return false;
		FedGroundStamps stamps = HemoCapabilityAccess.findFedGround(level.getChunkAt(soil));
		return stamps != null && BloodFedGrowthRules.stampFeeds(stamps.until(soil), level.getGameTime());
	}

	/** Stamps {@code soil} fed for {@code ticks}, never shortening a later stamp. */
	public static void stamp(ServerLevel level, BlockPos soil, int ticks) {
		if (!level.isLoaded(soil)) return;
		LevelChunk chunk = level.getChunkAt(soil);
		HemoCapabilityAccess.getFedGround(chunk).stamp(soil.immutable(), level.getGameTime(), ticks);
		chunk.setUnsaved(true);
	}

	/** Stamps the soil at {@code centre} and the ring round it (a drip's splash). */
	public static void splash(ServerLevel level, BlockPos centre, int radius, int ticks) {
		for (BlockPos soil : BlockPos.betweenClosed(centre.offset(-radius, 0, -radius), centre.offset(radius, 0, radius))) {
			stamp(level, soil, ticks);
		}
	}
}
