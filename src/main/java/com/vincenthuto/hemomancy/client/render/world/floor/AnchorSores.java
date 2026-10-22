package com.vincenthuto.hemomancy.client.render.world.floor;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RingTear;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteRingRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The anchors of a rite held on a floor, drawn as sores in the ground: raw pits that the membrane shader lets the
 * camera look down into, with an inflamed rim pulsing on the heartbeat, and blood rising inside as each is filled.
 * A broken anchor's sore bruises and tears. One quad per anchor rides in the membrane's batch; the pit, rim and blood
 * are all drawn by the {@code sore} mode of {@code cardinal_floor_membrane.fsh}.
 * <p>
 * Vertex encoding: green {@link #LAYER} marks a sore, red and blue the position across its quad, and alpha its fill
 * (below one half for a sound anchor, above for a broken one).
 */
public final class AnchorSores {
	/** Half the width of a sore's quad, matching {@code SORE_HALF} in the shader. */
	public static final float HALF = 0.78F;
	/** Green marking a sore in the membrane shader (0.09). */
	public static final int LAYER = 23;
	/** Sores sit a hair above the membrane surface, which they cover. */
	private static final float LIFT = 0.003F;
	/** How fast the drawn blood level eases toward the anchor's real fill each frame. */
	private static final float EASE = 0.12F;
	private static final int MEMORY_LIMIT = 512;
	private static final Map<Long, Float> SHOWN_FILL = new HashMap<>();
	/** The game tick each Focus last opened its sores, so the boundary knows not to stack an orb on them. */
	private static final Map<Long, Long> OPENED = new HashMap<>();

	private AnchorSores() {
	}

	/** Emits one sore per anchor of the live rite whose Focus is at {@code origin}; nothing when there is none. */
	public static void emit(VertexConsumer consumer, Matrix4f matrix, Level level, BlockPos origin) {
		ActiveRiteData.RiteEntry rite = riteAt(origin);
		if (rite == null) return;
		int[] states = rite.getAnchorStates();
		if (states.length == 0) return;
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		if (recipe == null || recipe.getCeremony() == null) return;
		List<CardinalRiteCeremonyDefinition.Anchor> anchors = recipe.getCeremony().anchors();
		int[] ranks = CardinalRiteRingRules.ranksOf(anchors);
		if (SHOWN_FILL.size() > MEMORY_LIMIT) SHOWN_FILL.clear();
		if (OPENED.size() > MEMORY_LIMIT) OPENED.clear();
		OPENED.put(origin.asLong(), level.getGameTime());
		for (int i = 0; i < anchors.size() && i < states.length; i++) {
			CardinalRiteCeremonyDefinition.Anchor anchor = anchors.get(i);
			Integer ground = groundOffset(level, origin, anchor.x(), anchor.z());
			if (ground == null) continue;
			int state = states[i];
			// A sleeping anchor is a closed scar; a woken one tears open and fills toward its own requirement.
			float target = rite.anchorDormant(i) ? 0.0F : ActiveRiteData.RiteEntry.anchorBlood(state)
					/ (float) Math.max(1, rite.anchorRequirement(i));
			float size = RingTear.size(rite.anchorDormant(i), origin, ranks[i], 0.0F);
			long key = origin.asLong() * 31L + i;
			float shown = SHOWN_FILL.getOrDefault(key, target);
			shown += (target - shown) * EASE;
			if (Math.abs(target - shown) < 0.002F) shown = target;
			SHOWN_FILL.put(key, shown);
			emitSore(consumer, matrix, anchor.x(), ground + FloorMesh.SURFACE_Y + LIFT, anchor.z(), size,
					encode(shown, ActiveRiteData.RiteEntry.anchorDamaged(state)));
		}
	}

	/**
	 * Whether the Focus at {@code origin} drew its anchors as sores within the last couple of ticks. The sore's own
	 * pool shows an anchor's fill and breakage, so a boundary anchor orb there would only sit on top of it.
	 */
	public static boolean opened(BlockPos origin, long gameTime) {
		Long tick = OPENED.get(origin.asLong());
		return tick != null && gameTime - tick <= 2L;
	}

	/** The packed alpha of a sore: fill in [0, 127] for a sound anchor, [128, 255] for a broken one. */
	static int encode(float fill, boolean broken) {
		int level = Math.round(Math.max(0.0F, Math.min(1.0F, fill)) * 127.0F);
		return broken ? 128 + level : level;
	}

	private static ActiveRiteData.RiteEntry riteAt(BlockPos origin) {
		for (ActiveRiteData.RiteEntry rite : ActiveRiteClientData.getActiveRites()) {
			if (!rite.isUnstained() && rite.getCenter().equals(origin)) return rite;
		}
		return null;
	}

	/**
	 * The height of the ground under an anchor relative to the floor plane, or {@code null} with no ground to open a
	 * sore in: the nearest sturdy top within a block of the floor level with open space above it.
	 */
	private static Integer groundOffset(Level level, BlockPos origin, int dx, int dz) {
		for (int dy : new int[] {0, -1, 1}) {
			BlockPos below = origin.offset(dx, dy, dz);
			BlockState state = level.getBlockState(below);
			if (!state.isFaceSturdy(level, below, Direction.UP)) continue;
			BlockPos above = below.above();
			if (!level.getBlockState(above).getCollisionShape(level, above).isEmpty()) continue;
			return dy;
		}
		return null;
	}

	private static void emitSore(VertexConsumer consumer, Matrix4f matrix, int dx, float y, int dz, float size,
			int alpha) {
		float cx = dx + 0.5F, cz = dz + 0.5F;
		float half = HALF * size;
		vertex(consumer, matrix, cx - half, y, cz - half, 0, 0, alpha);
		vertex(consumer, matrix, cx - half, y, cz + half, 0, 255, alpha);
		vertex(consumer, matrix, cx + half, y, cz + half, 255, 255, alpha);
		vertex(consumer, matrix, cx + half, y, cz - half, 255, 0, alpha);
	}

	private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int across,
			int along, int alpha) {
		// UV is the offset from the Focus centre, like every membrane surface, so the tissue around the sore matches.
		consumer.addVertex(matrix, x, y, z).setUv(x - 0.5F, z - 0.5F).setColor(across, LAYER, along, alpha);
	}
}
