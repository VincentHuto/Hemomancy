package com.vincenthuto.hemomancy.client.render.world.rite;

import static com.vincenthuto.hemomancy.client.render.world.rite.RiteSurface.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.world.AnimusMortemRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RingTear;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteRingRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMath;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * A small jet of blood standing over every filled anchor of a live Harbinger rite, so filled anchors read at a glance
 * from across the floor. It spurts with the floor's heartbeat, sheds a crown of droplets that curl back down into the
 * sore, and shoots up tall for a moment when the anchor first fills.
 */
public final class CardinalRiteAnchorJetRenderer {
	private static final RiteSurface.View VIEW = new RiteSurface.View();
	private static final RiteSurface.Path PATH = new RiteSurface.Path();
	private static final double MAX_DISTANCE_SQR = 72.0D * 72.0D;
	/** The jet rises from the pooled blood in the sore, just above the floor block's top. */
	private static final double BASE_Y = 1.03D;
	/** The sore's blood surface relative to the jet's base, where its welling pool lies. */
	private static final double POOL_Y = -0.02D;
	/** The column starts inside the floor block, so the block hides its end and it rises from under the pool. */
	private static final double ROOT_Y = -0.09D;
	/** Resting height of the jet, in blocks; it spurts a little higher on each beat. */
	private static final double JET_HEIGHT = 0.95D;
	/** The tall spurt when an anchor first fills. */
	private static final double BURST_HEIGHT = 2.6D;
	private static final float BURST_TICKS = 26.0F;
	private static final int STATIONS = 9;
	private static final int DROPLETS = 4;

	private CardinalRiteAnchorJetRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		List<ActiveRiteClientData.RiteEntry> rites = ActiveRiteClientData.getActiveRites();
		if (rites.isEmpty()) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		double time = mc.level.getGameTime() + partialTick;
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		VertexConsumer blood = null;
		for (ActiveRiteClientData.RiteEntry rite : rites) {
			int[] states = rite.getAnchorStates();
			if (rite.isUnstained() || states.length == 0) continue;
			BlockPos center = rite.getCenter();
			if (camera.distanceToSqr(Vec3.atCenterOf(center)) > MAX_DISTANCE_SQR) continue;
			CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(mc.level, rite.getRecipeId());
			if (recipe == null || recipe.getCeremony() == null) continue;
			List<CardinalRiteCeremonyDefinition.Anchor> anchors = recipe.getCeremony().anchors();
			int[] ranks = CardinalRiteRingRules.ranksOf(anchors);
			for (int i = 0; i < anchors.size() && i < states.length; i++) {
				int state = states[i];
				boolean full = !rite.anchorDormant(i) && !ActiveRiteClientData.RiteEntry.anchorDamaged(state)
						&& ActiveRiteClientData.RiteEntry.anchorBlood(state) >= rite.anchorRequirement(i);
				// A waking ring's sores flare once as they tear open, before any blood reaches them.
				CardinalRiteEventClientState.RiteEvent waking = full || rite.anchorDormant(i) ? null
						: RingTear.waking(center, ranks[i], partialTick);
				if (!full && waking == null) continue;
				if (blood == null) {
					AnimusMortemRenderTypes.begin(time);
					blood = buffers.getBuffer(AnimusMortemRenderTypes.ANIMUS);
				}
				CardinalRiteCeremonyDefinition.Anchor anchor = anchors.get(i);
				drawJet(poseStack, blood, mc, camera, center.getX() + anchor.x() + 0.5D, center.getY() + BASE_Y,
						center.getZ() + anchor.z() + 0.5D, time, i,
						full ? burstAge(center, i, partialTick) : waking.age(partialTick));
			}
		}
		if (blood != null) buffers.endBatch(AnimusMortemRenderTypes.ANIMUS);
	}

	/** Ticks since this anchor's newest fill beat, or -1 when none is remembered. */
	private static float burstAge(BlockPos center, int anchorIndex, float partialTick) {
		float newest = -1.0F;
		for (CardinalRiteEventClientState.RiteEvent event : CardinalRiteEventClientState.recent(center)) {
			if (event.kind() != CardinalRiteEventKind.ANCHOR_FILLED || event.index() != anchorIndex) continue;
			float age = event.age(partialTick);
			if (newest < 0.0F || age < newest) newest = age;
		}
		return newest;
	}

	/** Jet height: a resting spurt on the beat, overtaken by the tall first-fill burst while it lasts. */
	static double jetHeight(double beat, float burstAge) {
		double height = JET_HEIGHT * (0.7D + 0.45D * beat);
		if (burstAge >= 0.0F && burstAge < BURST_TICKS) {
			double t = burstAge / BURST_TICKS;
			height = Math.max(height, BURST_HEIGHT * Math.sin(Math.PI * Math.min(1.0D, t * 1.6D)) * (1.0D - t * 0.4D));
		}
		return height;
	}

	private static void drawJet(PoseStack poseStack, VertexConsumer v, Minecraft mc, Vec3 camera, double x, double y,
			double z, double time, int anchorIndex, float burstAge) {
		VIEW.set(camera.x, camera.y, camera.z, x, y, z, mc.gameRenderer.getMainCamera().rotation());
		poseStack.pushPose();
		poseStack.translate(x - camera.x, y - camera.y, z - camera.z);
		Matrix4f m = poseStack.last().pose();
		// Neighbouring jets beat a little out of step, so a full ring ripples rather than pumping in unison.
		double beat = CardinalRiteMath.heartbeat((float) (time + anchorIndex * 2.5D));
		double height = jetHeight(beat, burstAge);
		int seed = 40 + anchorIndex * 7;
		boolean bursting = burstAge >= 0.0F && burstAge < BURST_TICKS;

		// The column: thick where it leaves the pool, wavering and thinning as it rises.
		PATH.clear();
		for (int s = 0; s < STATIONS; s++) {
			double t = s / (STATIONS - 1.0D);
			double waver = Math.sin(time * 0.55D + t * 8.0D + anchorIndex) * 0.018D * t;
			PATH.add(waver, HemoEasing.lerp(ROOT_Y, height, t), waver * 0.6D, HemoEasing.lerp(0.075D, 0.028D, t) * (bursting ? 1.3D : 1.0D));
		}
		ribbon(m, v, VIEW, PATH, RIBBON, seed, TINT, 0.92F);
		// A welling pool on the sore's blood where the column leaves it, so the jet rises out of the pit instead of
		// ending in a flat cut just above it.
		ground(m, v, 0.0D, POOL_Y, 0.0D, 0.17D + 0.03D * beat, POOL, seed + 9, CLOT, 0.85F);
		ground(m, v, 0.0D, POOL_Y + 0.002D, 0.0D, 0.11D + 0.05D * beat, RIPPLE, seed + 10, CLOT, 0.45F);
		ribbon(m, v, VIEW, PATH, 0, STATIONS, 0.32D, RIBBON, seed + 1, CORE, 0.55F + 0.3F * (float) beat);

		// The crown: droplets thrown off the top that curl over and fall back toward the sore.
		for (int d = 0; d < DROPLETS; d++) {
			double angle = d * Math.PI * 0.5D + time * 0.04D + anchorIndex;
			double spread = 0.16D + 0.06D * beat;
			PATH.clear();
			for (int s = 0; s <= 5; s++) {
				double t = s / 5.0D;
				PATH.add(Math.cos(angle) * spread * t, height + 0.1D * t - 0.42D * t * t, Math.sin(angle) * spread * t,
						HemoEasing.lerp(0.026D, 0.012D, t));
			}
			ribbon(m, v, VIEW, PATH, RIBBON, seed + 2 + d, d % 2 == 0 ? TINT : CLOT, 0.75F);
		}
		// A bright bead where the jet breaks at its peak.
		card(m, v, VIEW, 0.0D, height, 0.0D, 0.05D, 0.05D, FOCUS, seed + 8, CORE, 0.6F + 0.3F * (float) beat);
		poseStack.popPose();
	}
}
