package com.vincenthuto.hemomancy.common.world;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.vincenthuto.hemomancy.common.world.HeartNodeRules.HEART_RADIUS;
import static com.vincenthuto.hemomancy.common.world.HeartNodeRules.SYNAPSE_RADIUS;
import static com.vincenthuto.hemomancy.common.world.HeartNodeRules.VERTICAL_BAND;

/**
 * One Apotheos's sanctum in the Gardens: the heart lobe around their node and a lobe for each Mycelial Synapse. Every
 * lobe holds only {@link HeartNodeRules#VERTICAL_BAND} blocks up or down, so stacked caverns stay separate.
 */
public record HeartNodeFootprint(UUID owner, @Nullable BlockPos heart, List<BlockPos> synapses) {
	public HeartNodeFootprint {
		synapses = List.copyOf(synapses);
	}

	public boolean contains(BlockPos pos) {
		return LobeEnvelope.contains(heart, synapses, pos, HEART_RADIUS, SYNAPSE_RADIUS, VERTICAL_BAND);
	}

	/** 1 at the heart, tapering to {@code 1 - EDGE_FALLOFF} at the outermost synapse's far edge; 0 outside. */
	public double strength(BlockPos pos) {
		if (heart == null || !contains(pos)) return 0.0D;
		return LobeEnvelope.strength(heart, synapses, pos, HEART_RADIUS, SYNAPSE_RADIUS, HeartNodeRules.EDGE_FALLOFF);
	}

	public boolean canAddSynapse(BlockPos pos) {
		return LobeEnvelope.canAddLobe(heart, synapses, pos, HEART_RADIUS, SYNAPSE_RADIUS,
				HeartNodeRules.SYNAPSE_BUDGET, VERTICAL_BAND);
	}

	/**
	 * The synapses that still chain back to the heart, lobe to lobe, by the rule they were raised under. A synapse cut
	 * off by a removal keeps working, as a Hematic Stake does; this is what draws the chain, not what prunes it.
	 */
	public List<BlockPos> connectedSynapses() {
		if (heart == null) return List.of();
		List<BlockPos> reached = new ArrayList<>();
		List<BlockPos> frontier = new ArrayList<>();
		for (BlockPos synapse : synapses) {
			if (LobeEnvelope.inLobe(synapse, heart, HEART_RADIUS + SYNAPSE_RADIUS, VERTICAL_BAND)) frontier.add(synapse);
		}
		while (!frontier.isEmpty()) {
			BlockPos next = frontier.remove(frontier.size() - 1);
			if (reached.contains(next)) continue;
			reached.add(next);
			for (BlockPos synapse : synapses) {
				if (!reached.contains(synapse)
						&& LobeEnvelope.inLobe(synapse, next, SYNAPSE_RADIUS * 2.0D, VERTICAL_BAND)) frontier.add(synapse);
			}
		}
		// Raised order, so a chain reads heart outward.
		return synapses.stream().filter(reached::contains).toList();
	}

	/**
	 * The lobe centre a connected synapse's thread runs from: the heart when it touches the heart lobe, otherwise the
	 * earliest-raised connected synapse it touches. Null for a synapse that is cut off or not this sanctum's.
	 */
	@Nullable
	public BlockPos chainedFrom(BlockPos synapse) {
		List<BlockPos> connected = connectedSynapses();
		if (!connected.contains(synapse)) return null;
		if (LobeEnvelope.inLobe(synapse, heart, HEART_RADIUS + SYNAPSE_RADIUS, VERTICAL_BAND)) return heart;
		for (BlockPos other : connected) {
			if (!other.equals(synapse) && LobeEnvelope.inLobe(synapse, other, SYNAPSE_RADIUS * 2.0D, VERTICAL_BAND)) {
				return other;
			}
		}
		return null;
	}

	public double maxRadius() {
		return LobeEnvelope.maxRadius(heart, synapses, HEART_RADIUS, SYNAPSE_RADIUS);
	}

	/** Whether this sanctum and another owner's would share a cell. An owner never crowds themself. */
	public boolean overlapsAnother(HeartNodeFootprint other) {
		if (owner.equals(other.owner)) return false;
		for (Lobe lobe : lobes()) {
			if (other.wouldOverlap(lobe.centre(), lobe.radius())) return true;
		}
		return false;
	}

	/** Whether a lobe of {@code radius} at {@code pos} would share a cell with this sanctum. */
	public boolean wouldOverlap(BlockPos pos, double radius) {
		for (Lobe lobe : lobes()) {
			if (LobeEnvelope.lobesTouch(lobe.centre(), lobe.radius(), pos, radius, VERTICAL_BAND)) return true;
		}
		return false;
	}

	public HeartNodeFootprint withSynapse(BlockPos pos) {
		List<BlockPos> next = new ArrayList<>(synapses);
		if (!next.contains(pos)) next.add(pos.immutable());
		return new HeartNodeFootprint(owner, heart, next);
	}

	public HeartNodeFootprint withoutSynapse(BlockPos pos) {
		List<BlockPos> next = new ArrayList<>(synapses);
		next.remove(pos);
		return new HeartNodeFootprint(owner, heart, next);
	}

	/** The node gone: its synapses go with it. */
	public HeartNodeFootprint withoutHeart() {
		return new HeartNodeFootprint(owner, null, List.of());
	}

	private List<Lobe> lobes() {
		List<Lobe> lobes = new ArrayList<>(synapses.size() + 1);
		if (heart != null) lobes.add(new Lobe(heart, HEART_RADIUS));
		for (BlockPos synapse : synapses) lobes.add(new Lobe(synapse, SYNAPSE_RADIUS));
		return lobes;
	}

	private record Lobe(BlockPos centre, double radius) {}
}
