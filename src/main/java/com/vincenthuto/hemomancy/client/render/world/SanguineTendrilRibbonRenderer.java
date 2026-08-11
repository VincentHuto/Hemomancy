package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/** Shared camera-facing crimson and near-black ribbon rendering for authored tendril curves. */
public final class SanguineTendrilRibbonRenderer {
	private static final float GLOW_WIDTH_MULTIPLIER = 2.65F;

	private SanguineTendrilRibbonRenderer() {
	}

	public static void render(PoseStack poseStack, VertexConsumer consumer,
			Iterable<? extends Strand> strands, Vec3 camera, boolean glowPass) {
		render(poseStack, consumer, strands, camera, glowPass, true);
	}

	public static void renderLocal(PoseStack poseStack, VertexConsumer consumer,
			Iterable<? extends Strand> strands, Vec3 localCamera, boolean glowPass) {
		render(poseStack, consumer, strands, localCamera, glowPass, false);
	}

	private static void render(PoseStack poseStack, VertexConsumer consumer,
			Iterable<? extends Strand> strands, Vec3 camera, boolean glowPass, boolean subtractCamera) {
		Matrix4f matrix = poseStack.last().pose();
		for (Strand strand : strands) {
			List<? extends Joint> joints = strand.joints();
			for (int index = 0; index < joints.size() - 1; index++) {
				Joint first = joints.get(index);
				Joint second = joints.get(index + 1);
				Vec3 firstSide = ribbonSide(joints, index, camera);
				Vec3 secondSide = ribbonSide(joints, index + 1, camera);
				float widthMultiplier = glowPass ? GLOW_WIDTH_MULTIPLIER : 1.0F;
				double firstWidth = first.halfWidth() * widthMultiplier;
				double secondWidth = second.halfWidth() * widthMultiplier;

				Vec3 origin = subtractCamera ? camera : Vec3.ZERO;
				Vec3 firstLeft = first.center().add(firstSide.scale(firstWidth)).subtract(origin);
				Vec3 firstRight = first.center().subtract(firstSide.scale(firstWidth)).subtract(origin);
				Vec3 secondLeft = second.center().add(secondSide.scale(secondWidth)).subtract(origin);
				Vec3 secondRight = second.center().subtract(secondSide.scale(secondWidth)).subtract(origin);
				Color color = color(strand.index(), glowPass);
				emitQuad(consumer, matrix, firstLeft, firstRight, secondRight, secondLeft,
						color.red(), color.green(), color.blue(),
						first.opacity() * color.alpha(), second.opacity() * color.alpha());
			}
		}
	}

	private static Vec3 ribbonSide(List<? extends Joint> joints, int index, Vec3 camera) {
		int previousIndex = Math.max(0, index - 1);
		int nextIndex = Math.min(joints.size() - 1, index + 1);
		Vec3 previous = joints.get(previousIndex).center();
		Vec3 next = joints.get(nextIndex).center();
		Vec3 center = joints.get(index).center();
		Vec3 tangent = next.subtract(previous);
		Vec3 side = tangent.cross(camera.subtract(center));
		if (side.lengthSqr() < 1.0E-7D) side = new Vec3(-tangent.z, 0.0D, tangent.x);
		return side.lengthSqr() < 1.0E-7D ? new Vec3(1.0D, 0.0D, 0.0D) : side.normalize();
	}

	private static Color color(int strandIndex, boolean glowPass) {
		boolean crimsonCore = (strandIndex & 1) == 0;
		if (glowPass) {
			return crimsonCore
					? new Color(0.008F, 0.0F, 0.006F, 0.26F)
					: new Color(0.42F, 0.006F, 0.018F, 0.24F);
		}
		return crimsonCore
				? new Color(0.50F, 0.008F, 0.022F, 0.72F)
				: new Color(0.012F, 0.0F, 0.008F, 0.80F);
	}

	private static void emitQuad(VertexConsumer consumer, Matrix4f matrix,
			Vec3 firstLeft, Vec3 firstRight, Vec3 secondRight, Vec3 secondLeft,
			float red, float green, float blue, float firstAlpha, float secondAlpha) {
		consumer.addVertex(matrix, (float) firstLeft.x, (float) firstLeft.y, (float) firstLeft.z)
				.setColor(red, green, blue, firstAlpha);
		consumer.addVertex(matrix, (float) firstRight.x, (float) firstRight.y, (float) firstRight.z)
				.setColor(red, green, blue, firstAlpha);
		consumer.addVertex(matrix, (float) secondRight.x, (float) secondRight.y, (float) secondRight.z)
				.setColor(red, green, blue, secondAlpha);
		consumer.addVertex(matrix, (float) secondLeft.x, (float) secondLeft.y, (float) secondLeft.z)
				.setColor(red, green, blue, secondAlpha);
	}

	public interface Strand {
		int index();
		List<? extends Joint> joints();
	}

	public interface Joint {
		Vec3 center();
		float halfWidth();
		float opacity();
	}

	private record Color(float red, float green, float blue, float alpha) {
	}
}
