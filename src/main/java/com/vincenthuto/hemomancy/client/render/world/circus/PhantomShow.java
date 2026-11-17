package com.vincenthuto.hemomancy.client.render.world.circus;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.circus.PhantomShowRules.Look;
import com.vincenthuto.hemomancy.client.screen.overlay.CircusPerceptionOverlay;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * The phantom show in the big top, drawn once a frame from the Circus perception state: spotlights, the string
 * lights, the trapeze artists' phantom ropes, then the audience and performers. Per viewer, presentation only.
 */
public final class PhantomShow {
	private static final int ROPE = 0xE8DAC0;
	private static final int HOUSE = 0x07020A;
	/** The darkest the house goes: the veil's alpha at a full house and full darkness setting. */
	private static final float HOUSE_MAX = 0.55F;

	private PhantomShow() {
	}

	public static void render(PoseStack poseStack, float partialTick, ShowMarks marks, List<StringLights.Run> runs,
			CircusPerceptionOverlay.Frame frame, List<AABB> creatures) {
		Look look = frame.blended();
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null || !marks.hasRing()) return;
		Camera camera = minecraft.gameRenderer.getMainCamera();
		Vec3 eye = camera.getPosition();
		float time = AnimationTime.gameTime(minecraft.level, partialTick);
		Vec3 viewer = minecraft.player.getEyePosition();
		List<PhantomFigures.Figure> leaving = PhantomFigures.plan(marks, frame.from(), time, viewer,
				minecraft.level::getBlockState, 1.0F - frame.t());
		List<PhantomFigures.Figure> cast = PhantomFigures.plan(marks, frame.to(), time, viewer,
				minecraft.level::getBlockState, frame.t());
		MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
		houseDark(buffers, poseStack.last().pose(), camera, look.house()
				* minecraft.options.darknessEffectScale().get().floatValue());
		VertexConsumer glow = buffers.getBuffer(HemoRenderTypes.QLIPHOTH_GLOW);
		Matrix4f matrix = poseStack.last().pose();
		Spotlights.render(glow, matrix, eye, marks, Spotlights.targets(marks, frame.from(), leaving, time),
				frame.from().house() * (1.0F - frame.t()));
		Spotlights.render(glow, matrix, eye, marks, Spotlights.targets(marks, frame.to(), cast, time),
				frame.to().house() * frame.t());
		StringLights.render(glow, matrix, eye, camera.getLeftVector(), camera.getUpVector(), runs, look, time);
		List<PhantomFigures.Figure> everyone = new java.util.ArrayList<>(leaving);
		everyone.addAll(cast);
		for (PhantomFigures.Figure figure : everyone)
			if (figure.kind() == PhantomFigures.Kind.SWINGING) ropes(glow, matrix, eye, figure);
		buffers.endBatch(HemoRenderTypes.QLIPHOTH_GLOW);
		PhantomFigures.render(poseStack, buffers, eye, camera.getLookVector(), everyone, creatures, time);
		buffers.endBatch();
	}

	/**
	 * The house going dark: a veil just in front of the eye over everything already drawn, so the lights, beams and
	 * phantoms drawn after it stand out. Never past {@link #HOUSE_MAX}, so the real tent and anyone in it stay readable.
	 */
	private static void houseDark(MultiBufferSource.BufferSource buffers, Matrix4f matrix, Camera camera, float house) {
		int alpha = (int) (HemoEasing.clamp01(house) * HOUSE_MAX * 255.0F);
		if (alpha <= 2) return;
		VertexConsumer veil = buffers.getBuffer(HemoRenderTypes.CIRCUS_HOUSE_DARK);
		Vector3f look = camera.getLookVector(), left = camera.getLeftVector(), up = camera.getUpVector();
		float ahead = 0.2F, reach = 4.0F;
		float cx = look.x() * ahead, cy = look.y() * ahead, cz = look.z() * ahead;
		float lx = left.x() * reach, ly = left.y() * reach, lz = left.z() * reach;
		float ux = up.x() * reach, uy = up.y() * reach, uz = up.z() * reach;
		StringLights.quad(veil, matrix, cx - lx - ux, cy - ly - uy, cz - lz - uz, cx + lx - ux, cy + ly - uy,
				cz + lz - uz, cx + lx + ux, cy + ly + uy, cz + lz + uz, cx - lx + ux, cy - ly + uy, cz - lz + uz,
				HOUSE, alpha);
		buffers.endBatch(HemoRenderTypes.CIRCUS_HOUSE_DARK);
	}

	/** Two faint ropes from the pivot to the phantom bar the artist hangs from. */
	private static void ropes(VertexConsumer glow, Matrix4f matrix, Vec3 eye, PhantomFigures.Figure figure) {
		double yaw = Math.toRadians(figure.yaw());
		double sideX = Math.cos(yaw), sideZ = Math.sin(yaw);
		double out = Math.sin(figure.swing()) * 2.0D, down = Math.cos(figure.swing()) * 2.0D;
		int alpha = (int) (figure.alpha() * 150.0F);
		for (int side = -1; side <= 1; side += 2) {
			Vec3 top = figure.at().add(sideX * 0.4D * side, 0.0D, sideZ * 0.4D * side);
			Vec3 bar = top.add(-Math.sin(yaw) * out, -down, Math.cos(yaw) * out);
			float w = 0.018F;
			StringLights.quad(glow, matrix, (float) (top.x - eye.x) - w, (float) (top.y - eye.y), (float) (top.z - eye.z),
					(float) (top.x - eye.x) + w, (float) (top.y - eye.y), (float) (top.z - eye.z),
					(float) (bar.x - eye.x) + w, (float) (bar.y - eye.y), (float) (bar.z - eye.z),
					(float) (bar.x - eye.x) - w, (float) (bar.y - eye.y), (float) (bar.z - eye.z), ROPE, alpha);
		}
	}
}
