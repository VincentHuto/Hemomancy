package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerMnemonistModel;
import com.vincenthuto.hemomancy.client.player.CastingClips;
import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingPoseModifiers;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo.EchoCast;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo.EchoCasts;
import com.vincenthuto.hemomancy.client.render.world.ThreadKnotGeometry;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomFrame;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPresentation;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomGeometry.*;

/**
 * The memory's echo: a past Harbinger, translucent and colour-shifted, standing on the warp and casting the memory
 * on loop with the real casting clip for its school and rank. Each loose thread is a limb torn from it, flickering
 * where it was; each woven thread makes it more solid. At the climax it grows whole, turns to the weaver, casts
 * once more in the memory's full colour and sinks into the fell.
 * <p>
 * The loom's stage decides how much of the memory it can hold (SomaticLoomEffectRules.echoLook): a base loom shows
 * a small grey figure that stutters; a Ganglionic loom holds it steady and coloured; a Cortical loom sees the past
 * Harbinger at full stature, which brightens at the climax instead of growing.
 */
final class SomaticLoomEcho {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/npc/harbinger/harbinger_mnemonist/harbinger_mnemonist.png");
	/** The bone-grey of a memory not yet bound, before the climax floods it with the school's colour. */
	private static final float[] ASH = {0.78f, 0.7f, 0.7f};
	private final ModelPart whole;
	private final ModelPart head, body, rightArm, leftArm, rightLeg, leftLeg;
	/** Limbs a loose thread tears away, in the order threads fray. */
	private final ModelPart[] frayOrder;

	SomaticLoomEcho(BlockEntityRendererProvider.Context context) {
		ModelPart root = context.bakeLayer(HarbingerMnemonistModel.LAYER_LOCATION);
		whole = root.getChild("whole");
		head = whole.getChild("head");
		body = whole.getChild("body");
		rightArm = whole.getChild("rightArm");
		leftArm = whole.getChild("leftArm");
		rightLeg = whole.getChild("rightLeg");
		leftLeg = whole.getChild("leftLeg");
		frayOrder = new ModelPart[]{rightArm, leftArm, rightLeg, leftLeg};
	}

	/** Draws in the loom's model frame (already turned to its facing); `stack` is in block units. */
	void render(SomaticLoomBlockEntity te, float partialTicks, PoseStack stack, MultiBufferSource buffer) {
		BloodManipulation manip = te.getWeaveManipulation();
		if (manip == null || !te.isWeaving() || te.getLevel() == null) return;
		boolean climax = te.isInClimax();
		float loop = climax ? 0 : (te.getWeaveTicks() + partialTicks) % SomaticWeaveRules.LOOP_TICKS;
		float c = climax ? Mth.clamp((te.getClimaxTicks() + partialTicks) / SomaticWeaveRules.CLIMAX_TICKS, 0, 1) : 0;
		float time = te.getLevel().getGameTime() + partialTicks;
		long total = te.getWeaveThreads().size();
		long woven = te.getWeaveThreads().stream().filter(t -> t.state() == SomaticLoomBlockEntity.WeaveThread.State.WOVEN).count();
		int loose = (int) te.getWeaveThreads().stream().filter(t -> t.state() == SomaticLoomBlockEntity.WeaveThread.State.LOOSE).count();

		pose(manip, climax ? climaxPhase(c) : loopPhase(loop), climax ? climaxTime(c) : loopTime(loop));
		SomaticLoomEffectRules.EchoLook look = SomaticLoomEffectRules.echoLook(te.upgradeTier());
		float grow = climax ? smooth(Mth.clamp(c / 0.3f, 0, 1)) : 0;
		float sink = climax ? smooth(Mth.clamp((c - 0.7f) / 0.3f, 0, 1)) : 0;
		// A whole figure has nowhere to grow: it brightens instead.
		float brighten = look.climaxScale() > look.weavingScale() ? 0 : grow;
		float scale = Mth.lerp(grow, look.weavingScale(), look.climaxScale()) * (1 - 0.85f * sink);
		float solidity = total == 0 ? 0 : woven / (float) total;
		float alpha = climax ? (0.95f + 0.05f * brighten) * (1 - sink) : look.alphaMin() + look.alphaSpan() * solidity;
		if (te.isPaused()) alpha *= 0.45f;
		// Breathe faintly, as the loom's heartbeat does; a base loom also loses its grip on the memory now and then.
		alpha *= 0.9f + 0.1f * Mth.sin(time * 0.15f);
		if (!climax) alpha *= SomaticLoomEffectRules.echoStutter(time, look.stutter());
		float[] school = ThreadKnotGeometry.vivid(manip.getTend().getColor());
		// A wash only: the aura carries the school's colour, so the robe and hood keep their detail.
		float wash = climax ? look.washMax() + (0.3f + 0.15f * brighten) * grow
				: Mth.lerp(solidity, look.washMin(), look.washMax());
		float[] tint = ThreadKnotGeometry.mix(ASH, school, Math.min(1f, wash));

		stack.pushPose();
		// Sinking, it is drawn forward and down into the fell, where the memory crystallizes.
		stack.translate(ECHO_X / 16f, Mth.lerp(sink, ECHO_Y, FELL_Y) / 16f, Mth.lerp(sink, ECHO_Z, FELL_Z) / 16f);
		if (climax) stack.mulPose(Axis.YP.rotationDegrees(grow * turnTowardWeaver(te)));
		stack.scale(scale, scale, scale);
		// The entity-model convention: flipped, feet on the origin.
		stack.scale(-1, -1, 1);
		stack.translate(0, -1.501f, 0);

		// An aura first, slightly larger and in the school's colour, so the figure reads as a glow, not a statue.
		stack.pushPose();
		stack.translate(0, 0.75f, 0);
		stack.scale(1.07f, 1.04f, 1.07f);
		stack.translate(0, -0.75f, 0);
		for (ModelPart limb : frayOrder) limb.visible = true;
		VertexConsumer aura = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
		whole.render(stack, aura, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
				argb(alpha * (climax ? 0.45f + 0.3f * brighten : 0.25f),
						ThreadKnotGeometry.mix(aura(look, school, climax ? grow : 0), ThreadKnotGeometry.HOT, 0.2f + 0.3f * brighten)));
		stack.popPose();
		if (brighten > 0) {
			// The whole figure's climax: a second, wider corona flares around it.
			stack.pushPose();
			stack.translate(0, 0.75f, 0);
			stack.scale(1.16f, 1.08f, 1.16f);
			stack.translate(0, -0.75f, 0);
			whole.render(stack, aura, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
					argb(alpha * 0.22f * brighten, ThreadKnotGeometry.mix(school, ThreadKnotGeometry.HOT, 0.5f)));
			stack.popPose();
		}

		VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
		int torn = Math.min(loose, frayOrder.length);
		for (int i = 0; i < frayOrder.length; i++) frayOrder[i].visible = i >= torn;
		whole.render(stack, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, argb(alpha, tint));
		for (int i = 0; i < torn; i++) {
			// A limb torn away as a thread flickers faintly where it was.
			ModelPart limb = frayOrder[i];
			limb.visible = true;
			float flicker = 0.08f + 0.22f * Math.max(0, Mth.sin(time * 0.9f + i * 1.7f));
			stack.pushPose();
			whole.translateAndRotate(stack);
			limb.render(stack, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, argb(alpha * flicker, school));
			stack.popPose();
		}
		for (ModelPart limb : frayOrder) limb.visible = true;
		stack.popPose();

		fireRelease(te, manip, climax, loop, c, scale);
	}

	private void pose(BloodManipulation manip, CastPhase phase, float t) {
		for (ModelPart part : new ModelPart[]{whole, head, body, rightArm, leftArm, rightLeg, leftLeg}) part.resetPose();
		CastPresentation presentation = manip.getCastPresentation();
		CastingPose pose = CastingPoseModifiers.apply(CastingClips.get(manip.getTend(), manip.getRank()).sample(phase, t),
				presentation.purpose(), presentation.style(), phase, t, false);
		turn(head, pose.head());
		turn(body, pose.body());
		turn(rightArm, pose.rightArm());
		turn(leftArm, pose.leftArm());
		turn(rightLeg, pose.rightLeg());
		turn(leftLeg, pose.leftLeg());
	}

	/** The clip's rotation on top of the part's authored rest pose, so robes and hood keep their drape. */
	private static void turn(ModelPart part, CastingPose.Rotation r) {
		part.xRot += r.x();
		part.yRot += r.y();
		part.zRot += r.z();
	}

	/** One loop: opening 0-6, a held gather to the release at 40, release 40-46, recovery, then a breath. */
	private static CastPhase loopPhase(float loop) {
		if (loop < 6) return CastPhase.OPENING;
		if (loop < SomaticWeaveRules.RELEASE_TICK) return CastPhase.HOLD;
		if (loop < SomaticWeaveRules.RELEASE_TICK + 6) return CastPhase.RELEASE;
		return CastPhase.RECOVERY;
	}

	private static float loopTime(float loop) {
		if (loop < 6) return loop;
		if (loop < SomaticWeaveRules.RELEASE_TICK) return loop - 6;
		if (loop < SomaticWeaveRules.RELEASE_TICK + 6) return loop - SomaticWeaveRules.RELEASE_TICK;
		return Math.min(14, loop - SomaticWeaveRules.RELEASE_TICK - 6);
	}

	private static CastPhase climaxPhase(float c) {
		return c < 0.35f ? CastPhase.OPENING : c < 0.45f ? CastPhase.RELEASE : CastPhase.RECOVERY;
	}

	private static float climaxTime(float c) {
		return c < 0.35f ? Math.min(6, c / 0.35f * 6) : c < 0.45f ? (c - 0.35f) / 0.1f * 6 : Math.min(14, (c - 0.45f) * 30);
	}

	private static float smooth(float t) {
		return t * t * (3 - 2 * t);
	}

	/** Degrees, in the loom's frame, to face the weaver; the echo faces the loom's front (-z) at rest. */
	private static float turnTowardWeaver(SomaticLoomBlockEntity te) {
		var level = Minecraft.getInstance().level;
		Player weaver = level == null || te.getWeaver() == null ? null : level.getPlayerByUUID(te.getWeaver());
		if (weaver == null) return 0;
		Vec3 echo = te.echoAnchor();
		Vec3 front = forward(te);
		Vec3 to = weaver.position().subtract(echo).multiply(1, 0, 1);
		if (to.lengthSqr() < 1.0E-4D) return 0;
		to = to.normalize();
		// Signed angle from the loom's front to the weaver, about +y.
		double angle = Math.atan2(front.x * to.z - front.z * to.x, front.x * to.x + front.z * to.z);
		return (float) -Math.toDegrees(angle);
	}

	/** The loom's front in world space, toward where the weaver stands. */
	private static Vec3 forward(SomaticLoomBlockEntity te) {
		Direction facing = te.getBlockState().hasProperty(SomaticLoomBlock.FACING)
				? te.getBlockState().getValue(SomaticLoomBlock.FACING) : Direction.NORTH;
		Vec3 front = SomaticLoomFrame.toWorld(te.getBlockPos(), facing, 8, 0, 0);
		Vec3 back = SomaticLoomFrame.toWorld(te.getBlockPos(), facing, 8, 0, 16);
		return front.subtract(back).normalize();
	}

	private static void fireRelease(SomaticLoomBlockEntity te, BloodManipulation manip, boolean climax, float loop, float c,
			float scale) {
		var level = Minecraft.getInstance().level;
		if (level == null) return;
		long beat = te.getWeaveTicks() / SomaticWeaveRules.LOOP_TICKS;
		boolean due = climax ? c >= 0.35f && !te.echoClimaxFired()
				: loop >= SomaticWeaveRules.RELEASE_TICK && te.echoLastLoopFired() != beat && !te.isPaused();
		if (!due) return;
		if (climax) te.markEchoClimaxFired();
		else te.markEchoLoopFired(beat);
		Vec3 front = forward(te);
		Vec3 feet = te.echoAnchor();
		Vec3 hands = feet.add(front.scale(0.25 * scale)).add(0, 1.25 * scale, 0);
		EchoCasts.releaseSafely(manip.getName(), new EchoCast.EchoContext(level, hands, feet, front, scale, manip));
	}

	/** The aura's colour: a base loom's memory glows mostly grey until the climax floods it; upgraded looms glow in the school. */
	private static float[] aura(SomaticLoomEffectRules.EchoLook look, float[] school, float climax) {
		float schoolShare = Math.min(1f, 0.35f + look.washMax() + climax);
		return ThreadKnotGeometry.mix(ASH, school, schoolShare);
	}

	private static int argb(float alpha, float[] rgb) {
		int a = Mth.clamp((int) (alpha * 255), 0, 255);
		int r = Mth.clamp((int) (rgb[0] * 255), 0, 255), g = Mth.clamp((int) (rgb[1] * 255), 0, 255),
				b = Mth.clamp((int) (rgb[2] * 255), 0, 255);
		return a << 24 | r << 16 | g << 8 | b;
	}
}
