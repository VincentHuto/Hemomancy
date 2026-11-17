package com.vincenthuto.hemomancy.common.world;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One owner's record on the Gardens level: their node, its synapses in the order raised, what it shows, and when; and
 * the respawn point they had before the node took it ({@code priorRespawn}, null when they had none).
 */
public record HeartNodeEntry(UUID owner, BlockPos heart, List<BlockPos> synapses, HeartNodeSettings settings,
		long plantedAt, @Nullable PriorRespawn priorRespawn) {
	public HeartNodeEntry {
		heart = heart.immutable();
		synapses = List.copyOf(synapses);
	}

	public HeartNodeEntry(UUID owner, BlockPos heart, List<BlockPos> synapses, HeartNodeSettings settings, long plantedAt) {
		this(owner, heart, synapses, settings, plantedAt, null);
	}

	public static HeartNodeEntry planted(UUID owner, BlockPos heart, long plantedAt) {
		return planted(owner, heart, plantedAt, null);
	}

	public static HeartNodeEntry planted(UUID owner, BlockPos heart, long plantedAt, @Nullable PriorRespawn priorRespawn) {
		return new HeartNodeEntry(owner, heart, List.of(), HeartNodeSettings.DEFAULT, plantedAt, priorRespawn);
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
		return new HeartNodeEntry(owner, heart, next, settings, plantedAt, priorRespawn);
	}

	public HeartNodeEntry withoutSynapse(BlockPos pos) {
		List<BlockPos> next = new ArrayList<>(synapses);
		next.remove(pos);
		return new HeartNodeEntry(owner, heart, next, settings, plantedAt, priorRespawn);
	}

	public HeartNodeEntry withSettings(HeartNodeSettings next) {
		return new HeartNodeEntry(owner, heart, synapses, next, plantedAt, priorRespawn);
	}
}
