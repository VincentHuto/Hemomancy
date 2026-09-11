package com.vincenthuto.hemomancy.client.vein;

import com.vincenthuto.hemomancy.common.vein.EarthenVeinTravelPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class EarthenVeinTravelVisuals {
	private final Map<Key, Visual> entries = new HashMap<>();

	public void update(ResourceLocation dimension, BlockPos pos, int travelerEntityId,
			EarthenVeinTravelPhase phase, float progress) {
		Key key = new Key(dimension, pos, travelerEntityId);
		if (phase == EarthenVeinTravelPhase.IDLE) entries.remove(key);
		else entries.put(key, new Visual(travelerEntityId, phase, progress));
	}

	@Nullable
	public Visual visual(ResourceLocation dimension, BlockPos pos) {
		return entries.entrySet().stream()
				.filter(entry -> entry.getKey().dimension.equals(dimension) && entry.getKey().pos.equals(pos))
				.map(Map.Entry::getValue)
				.max(Comparator.comparingInt((Visual visual) -> visual.phase().ordinal())
						.thenComparingDouble(visual -> visual.phase() == EarthenVeinTravelPhase.EJECTING
								? -visual.progress() : visual.progress()))
				.orElse(null);
	}

	public void retainDimension(ResourceLocation dimension) {
		entries.keySet().removeIf(key -> !key.dimension.equals(dimension));
	}

	public void clear() {
		entries.clear();
	}

	public record Visual(int travelerEntityId, EarthenVeinTravelPhase phase, float progress) { }
	private record Key(ResourceLocation dimension, BlockPos pos, int travelerEntityId) {
		private Key { pos = pos.immutable(); }
	}
}
