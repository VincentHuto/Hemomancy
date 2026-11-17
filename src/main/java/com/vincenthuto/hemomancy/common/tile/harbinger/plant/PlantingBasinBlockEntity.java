package com.vincenthuto.hemomancy.common.tile.harbinger.plant;

import com.vincenthuto.hemomancy.common.block.harbinger.plant.PlantingBasinBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.world.farm.BloodFedGrowthRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A planting basin's soil and its small tank (core-loop Track 7). Blood comes in by hand (Projection, a poured flask)
 * or down a worked gutter, which already feeds any tank beside it. While some of it is fresh the bed is fed at level 2:
 * each extra growth it buys a crop costs a few mL. It is an inert container, so what is not used clots (Track 3), and
 * the clot a tended bed leaves is scraped out as Formations by Absorption.
 */
public class PlantingBasinBlockEntity extends HemoBlockEntity implements IBloodReservoir {
	private boolean configured;

	public PlantingBasinBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.planting_basin.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		configure();
	}

	/** Sizes the tank (once). Safe to call before the first tick. */
	public BlockBloodVolume configure() {
		BlockBloodVolume volume = ReservoirBlood.settled(this);
		if (!configured) {
			volume.setActive(true);
			if (volume.capacity() != BloodFedGrowthRules.PLANTING_BASIN_CAPACITY) {
				volume.setMaxBloodVolume(BloodFedGrowthRules.PLANTING_BASIN_CAPACITY);
			}
			configured = true;
			setChanged();
		}
		return volume;
	}

	/** Whether the bed holds enough fresh blood to buy a crop one extra growth. */
	public boolean fed() {
		return configure().getBloodVolume() >= BloodFedGrowthRules.BASIN_GROWTH_ML;
	}

	/** Takes one extra growth's blood; false when there was not enough fresh blood. */
	public boolean spendGrowth() {
		BlockBloodVolume volume = configure();
		if (volume.getBloodVolume() < BloodFedGrowthRules.BASIN_GROWTH_ML) return false;
		volume.drain(BloodFedGrowthRules.BASIN_GROWTH_ML);
		setChanged();
		return true;
	}

	/** Once a second the bed shows whether it is fed: a dark, soaked surface while fresh blood stands in it. */
	public static void serverTick(Level level, BlockPos pos, BlockState state, PlantingBasinBlockEntity basin) {
		if (!(level instanceof ServerLevel server) || Math.floorMod(server.getGameTime() + pos.asLong(), 20L) != 0L) return;
		boolean fed = basin.fed();
		if (state.getValue(PlantingBasinBlock.FED) != fed) {
			server.setBlock(pos, state.setValue(PlantingBasinBlock.FED, fed), Block.UPDATE_CLIENTS);
		}
	}
}
