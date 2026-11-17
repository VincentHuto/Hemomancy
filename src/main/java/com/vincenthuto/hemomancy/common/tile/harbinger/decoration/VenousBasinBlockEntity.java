package com.vincenthuto.hemomancy.common.tile.harbinger.decoration;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.VenousBasinBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.world.farm.BloodFedGround;
import com.vincenthuto.hemomancy.common.world.farm.BloodFedGrowthRules;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * A player's venous basin (core-loop Track 6): an inert tank of {@link GroundRunRules#BASIN_CAPACITY} that clots like
 * any other (Track 3). While it holds fresh blood and its spout overhangs a gutter, the blood runs out down the
 * connected gutters and feeds what they end at: a machine's working buffer, farmland. When the fresh window closes the
 * stream stops and what is left sets in the bowl, to be scraped out as Formations by Absorption. Pour it and walk
 * away for a few minutes works; storing it does not.
 *
 * <p>Only a basin a player set down ({@link VenousBasinBlock#WORKED}) keeps a tank. A Saint complex's basins are set
 * pieces whose fill the arena drives; they have no block entity, so no player basin can feed a Saint fight and no
 * hopper can drain one.
 */
public class VenousBasinBlockEntity extends HemoBlockEntity implements IBloodReservoir {
	/** Its gutters are walked again this often even when nothing changed, to find a machine set at a run's end. */
	private static final int RESCAN_TICKS = 200;
	private boolean configured;
	private boolean stale = true;
	private long nextScan;
	private VenousBasinBlock.Run run = VenousBasinBlock.Run.EMPTY;

	public VenousBasinBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.venous_basin.get(), pos, state);
	}

	@Override
	public boolean holdsBlood() {
		return getBlockState().getValue(VenousBasinBlock.WORKED);
	}

	@Override
	public boolean isBloodStore() {
		return true;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		configure();
	}

	/** Sizes a worked basin's tank (once); a structure basin keeps none. Safe to call before the first tick. */
	public BlockBloodVolume configure() {
		BlockBloodVolume volume = ReservoirBlood.settled(this);
		if (!configured && holdsBlood()) {
			volume.setActive(true);
			if (volume.capacity() != GroundRunRules.BASIN_CAPACITY) volume.setMaxBloodVolume(GroundRunRules.BASIN_CAPACITY);
			configured = true;
			setChanged();
		}
		return volume;
	}

	/** A gutter appeared or went under the spout: walk the run again on the next tick. */
	public void markStale() {
		stale = true;
	}

	/** The gutters this basin wets and what they end at, as last walked. */
	public VenousBasinBlock.Run run() {
		return run;
	}

	/** The stream this basin alone gave the gutter at {@code gutter} when it last walked its run; 0 when it does not reach it. */
	public int streamAt(BlockPos gutter) {
		return run.streams().getOrDefault(gutter, 0);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, VenousBasinBlockEntity basin) {
		if (!(level instanceof ServerLevel server) || !state.getValue(VenousBasinBlock.WORKED)) return;
		long now = server.getGameTime();
		boolean second = Math.floorMod(now + pos.asLong(), 20L) == 0L;
		if (!second && !basin.stale) return;
		BlockBloodVolume volume = basin.configure();
		double fresh = volume.getBloodVolume();
		int fill = GroundRunRules.basinFill(fresh + volume.clotted(), volume.capacity());
		BlockState updated = state.setValue(VenousBasinBlock.FILL, fill)
				.setValue(VenousBasinBlock.POURING, fresh > 0.0D && VenousBasinBlock.overGutter(server, pos, state));
		boolean changed = updated != state;
		if (changed) server.setBlock(pos, updated, Block.UPDATE_CLIENTS);
		if (changed || basin.stale || now >= basin.nextScan) {
			// The stream runs as far as the basin's fresh blood reaches: dry when it has clotted through.
			VenousBasinBlock.Run before = basin.run;
			basin.run = VenousBasinBlock.flowWorked(server, pos, updated, updated.getValue(VenousBasinBlock.POURING) ? fill : 0);
			basin.stale = false;
			basin.nextScan = now + RESCAN_TICKS;
			// The other basins on the network show the highest stream on each gutter, so they walk again when this
			// one's own stream changed; only on a real change, so two basins never wake each other forever.
			if (!before.streams().equals(basin.run.streams())) {
				for (BlockPos other : basin.run.others()) {
					if (server.getBlockEntity(other) instanceof VenousBasinBlockEntity neighbour) neighbour.markStale();
				}
			}
		}
		// A second's flow once a second: a re-walk between beats (a gutter changed) delivers nothing extra.
		if (second && updated.getValue(VenousBasinBlock.POURING)) basin.deliver(server, volume);
	}

	/** One second of the stream: an even share of the flow to each machine and stretch of farmland the run reaches. */
	private void deliver(ServerLevel level, BlockBloodVolume volume) {
		List<BlockPos> machines = run.machines();
		List<BlockPos> farmland = run.farmland();
		double share = GroundRunRules.deliveryShare(volume.getBloodVolume(), machines.size() + farmland.size());
		if (share <= 0.0D) return;
		double delivered = 0.0D;
		for (BlockPos target : machines) {
			BlockEntity blockEntity = level.getBlockEntity(target);
			if (!(blockEntity instanceof IBloodReservoir reservoir) || blockEntity instanceof VenousBasinBlockEntity
					|| !reservoir.holdsBlood()) continue;
			delivered += reservoir.receiveBlood(share);
		}
		for (BlockPos field : farmland) {
			BlockState soil = level.getBlockState(field);
			if (!(soil.getBlock() instanceof FarmBlock)) continue;
			if (soil.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) {
				level.setBlock(field, soil.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), Block.UPDATE_CLIENTS);
			}
			// Blood-wet farmland is fed ground at level 1 (core-loop Track 7) while the stream keeps reaching it.
			BloodFedGround.stamp(level, field, BloodFedGrowthRules.GUTTER_FEED_TICKS);
			delivered += Math.min(share, GroundRunRules.FARMLAND_ML);
		}
		if (delivered > 0.0D) {
			volume.drain(Math.min(delivered, volume.getBloodVolume()));
			setChanged();
		}
	}
}
