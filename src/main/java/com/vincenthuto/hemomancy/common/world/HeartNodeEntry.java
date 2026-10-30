package com.vincenthuto.hemomancy.common.world;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** One owner's record on the Gardens level: their node, its synapses in the order raised, what it shows, and when. */
public record HeartNodeEntry(UUID owner, BlockPos heart, List<BlockPos> synapses, HeartNodeSettings settings,
		long plantedAt) {
	public HeartNodeEntry {
		heart = heart.immutable();
		synapses = List.copyOf(synapses);
	}

	public static HeartNodeEntry planted(UUID owner, BlockPos heart, long plantedAt) {
		return new HeartNodeEntry(owner, heart, List.of(), HeartNodeSettings.DEFAULT, plantedAt);
	}

	public HeartNodeFootprint footprint() {
		return new HeartNodeFootprint(owner, heart, synapses);
	}

	public int stage(long gameTime) {
		return HeartNodeRules.stageAt(plantedAt, gameTime);
	}

	public HeartNodeEntry withSynapse(BlockPos pos) {
		List<BlockPos> next = new ArrayList<>(synapses);
		if (!next.contains(pos)) next.add(pos.immutable());
		return new HeartNodeEntry(owner, heart, next, settings, plantedAt);
	}

	public HeartNodeEntry withoutSynapse(BlockPos pos) {
		List<BlockPos> next = new ArrayList<>(synapses);
		next.remove(pos);
		return new HeartNodeEntry(owner, heart, next, settings, plantedAt);
	}

	public HeartNodeEntry withSettings(HeartNodeSettings next) {
		return new HeartNodeEntry(owner, heart, synapses, next, plantedAt);
	}
}
