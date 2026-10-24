package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetStringsRules;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * When each puppet's arrival or departure on its strings began, so the body renderer, its control thread and its
 * Crossbar follow the same timeline as the drawn strings. Keyed by entity id: an arrival cue can reach the client
 * before the body's spawn packet, and the body picks the timeline up when it appears.
 */
public final class PuppetStringStates {
	/** Blocks of lift, uniform scale about the head and twist in radians. */
	public record BodyPose(double lift, double scale, double twist) {
	}

	private record Start(Form form, long tick) {
	}

	private static final Map<Integer, Start> STARTS = new HashMap<>();
	private static final int FORGET_TICKS = 60;

	private PuppetStringStates() {
	}

	static void accept(ManipulationVisualPacket packet, long now) {
		if (!PuppetStringsGeometry.handles(packet.form()) || packet.ticks() <= 0 || packet.entityId() < 0) return;
		STARTS.values().removeIf(start -> now - start.tick() > FORGET_TICKS);
		STARTS.put(packet.entityId(), new Start(packet.form(), now));
	}

	public static void clear() {
		STARTS.clear();
	}

	/** The body's pose on its strings at {@code time} (game time plus partial tick), or null when not on them. */
	public static BodyPose body(int entityId, double time) {
		Start start = STARTS.get(entityId);
		if (start == null) return null;
		double age = time - start.tick();
		if (start.form() == Form.PUPPET_DESCEND) {
			if (age >= PuppetStringsRules.DESCEND_TICKS) return null;
			return new BodyPose(PuppetStringsRules.descendOffset(age), PuppetStringsRules.descendScale(age), 0.0D);
		}
		return new BodyPose(PuppetStringsRules.unspoolLift(age), PuppetStringsRules.unspoolScale(age),
				PuppetStringsRules.unspoolTwist(age));
	}

	/** Lowers an arriving body or hauls a departing one on its strings, scaling about the head they hold. */
	public static void applyBodyPose(Entity entity, float partialTicks, PoseStack poseStack) {
		BodyPose pose = body(entity.getId(), AnimationTime.gameTime(entity.level(), partialTicks));
		if (pose == null) return;
		float scale = (float) pose.scale();
		poseStack.translate(0.0D, pose.lift() + entity.getBbHeight() * (1.0F - scale), 0.0D);
		poseStack.mulPose(Axis.YP.rotation((float) pose.twist()));
		poseStack.scale(scale, scale, scale);
	}

	/** True from the moment a body starts being hauled away; it no longer counts as an active puppet. */
	public static boolean unspooling(int entityId) {
		Start start = STARTS.get(entityId);
		return start != null && start.form() == Form.PUPPET_UNSPOOL;
	}

	public static double controlThreadFade(int entityId, double time) {
		Start start = STARTS.get(entityId);
		return start == null || start.form() != Form.PUPPET_UNSPOOL ? 1.0D
				: PuppetStringsRules.controlThreadFade(time - start.tick());
	}
}
