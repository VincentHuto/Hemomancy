package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;

public final class CardinalRiteFogRenderer {
	private static final CardinalRiteFogState STATE = new CardinalRiteFogState();
	/** Most wave spawn points that part the fog of one rite at once. */
	private static final int MAX_PARTINGS = 24;
	private static final float PARTING_THICKEN_BAND = 1.6F;
	private static boolean loggedMissingShader;

	// Per-frame scratch, reused so the fog allocates nothing per puff.
	private static final float[] POSITION = new float[3];
	private static float[] puffX = new float[0];
	private static float[] puffY = new float[0];
	private static float[] puffZ = new float[0];
	private static float[] puffAlpha = new float[0];
	private static long[] drawOrder = new long[0];
	private static final float[] PARTING_X = new float[MAX_PARTINGS];
	private static final float[] PARTING_Z = new float[MAX_PARTINGS];
	private static final float[] PARTING_STRENGTH = new float[MAX_PARTINGS];
	private static final Vector3f CAMERA_RIGHT = new Vector3f();
	private static final Vector3f CAMERA_UP = new Vector3f();

	private CardinalRiteFogRenderer() {
	}

	public static void render(PoseStack poseStack, MultiBufferSource.BufferSource buffer,
			List<ActiveRiteData.RiteEntry> activeRites, float time, Camera camera) {
		// Lightning (ambient strikes and wave telegraphs) does not depend on the fog billboards.
		long gameTick = (long) Math.floor(time);
		CardinalRiteFogLightning.tick(activeRites, gameTick, time - gameTick);

		if (!enabled()) {
			STATE.clear();
			return;
		}
		List<CardinalRiteFogState.Sample> samples = STATE.update(activeRites, time,
				CardinalRiteFogRenderer::fadeOutTicks);
		if (samples.isEmpty()) return;
		if (ShaderInit.CARDINAL_RITE_FOG.getInstance().get() == null) {
			if (!loggedMissingShader) {
				loggedMissingShader = true;
				Hemomancy.LOGGER.debug("Cardinal rite fog shader is not loaded; fog billboards are skipped.");
			}
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		int renderDistance = minecraft.options.renderDistance().get();
		Vec3 cameraPosition = camera.getPosition();
		CAMERA_RIGHT.set(camera.getLeftVector()).negate();
		CAMERA_UP.set(camera.getUpVector());
		for (CardinalRiteFogState.Sample sample : samples) {
			ActiveRiteData.RiteEntry rite = sample.rite();
			float radius = CardinalRiteFogGeometry.perimeterRadius(
					rite.getFootprintRadius(), rite.getRiteSize(), rite.getCompletedRings(),
					rite.getTotalRings(), rite.isLegacy());
			float opacity = sample.opacity() * endingSurge(rite.getCenter(), sample.active(), time);
			if (opacity <= 0.001F || !CardinalRiteFogGeometry.isWithinRenderDistance(
					rite.getCenter(), cameraPosition, radius, renderDistance)) {
				continue;
			}
			drawRiteFog(poseStack, buffer, rite.getCenter(), radius, time, opacity, cameraPosition);
		}
	}

	public static void clear() {
		STATE.clear();
		CardinalRiteFogLightning.clear();
	}

	/** Endings keep their fog as long as their own choreography runs. */
	private static float fadeOutTicks(BlockPos center) {
		CardinalRiteEventClientState.Ghost ghost = CardinalRiteEventClientState.ghostAt(center);
		if (ghost == null) return CardinalRiteFogState.FADE_OUT_TICKS;
		return switch (ghost.kind()) {
			case COLLAPSED, BROKEN -> CardinalRiteBoundaryRenderer.VIOLENT_ENDING_TICKS;
			case COMPLETED -> CardinalRiteBoundaryRenderer.COMPLETED_ENDING_TICKS;
			default -> CardinalRiteFogState.FADE_OUT_TICKS;
		};
	}

	/** A violent ending makes the fog surge inward once before it thins. */
	private static float endingSurge(BlockPos center, boolean active, float time) {
		if (active) return 1.0F;
		CardinalRiteEventClientState.Ghost ghost = CardinalRiteEventClientState.ghostAt(center);
		if (ghost == null || (ghost.kind() != CardinalRiteEventKind.COLLAPSED
				&& ghost.kind() != CardinalRiteEventKind.BROKEN)) {
			return 1.0F;
		}
		float age = ghost.age(time - (float) Math.floor(time));
		return 1.0F + 0.55F * HemoEasing.pulse(age, 3.0F, 16.0F);
	}

	private static boolean enabled() {
		return HemoClientConfig.RENDER_CARDINAL_RITE_FOG == null
				|| HemoClientConfig.RENDER_CARDINAL_RITE_FOG.get();
	}

	private static void drawRiteFog(PoseStack poseStack, MultiBufferSource.BufferSource buffer,
			BlockPos center, float radius, float time, float opacity, Vec3 cameraPosition) {
		float seed = fogSeed(center);
		double centerX = center.getX() + 0.5D;
		double centerY = CardinalRiteBoundaryGeometry.boundaryPlaneY(center.getY());
		double centerZ = center.getZ() + 0.5D;
		float verticalOffset = fogVerticalOffset();
		int partings = gatherPartings(center, centerX, centerZ, time);

		List<CardinalRiteFogGeometry.FogPuff> puffs = CardinalRiteFogGeometry.puffs(seed, radius);
		int count = puffs.size();
		ensureCapacity(count);
		float cameraX = (float) (cameraPosition.x - centerX);
		float cameraY = (float) (cameraPosition.y - centerY);
		float cameraZ = (float) (cameraPosition.z - centerZ);
		for (int index = 0; index < count; index++) {
			CardinalRiteFogGeometry.FogPuff puff = puffs.get(index);
			CardinalRiteFogGeometry.positionInto(puff, radius, time, POSITION);
			float x = POSITION[0];
			float y = POSITION[1] + verticalOffset;
			float z = POSITION[2];
			float alpha = opacity * puff.opacity() * CardinalRiteFogGeometry.opacityMultiplier(puff, time);
			for (int parting = 0; parting < partings; parting++) {
				float dx = x - PARTING_X[parting];
				float dz = z - PARTING_Z[parting];
				float distance = (float) Math.sqrt(dx * dx + dz * dz);
				float strength = PARTING_STRENGTH[parting];
				float clearing = CardinalRiteFogGeometry.PARTING_RADIUS;
				if (distance < clearing) {
					// Shove the puff out of the clearing and thin it toward the spawn point.
					float push = (clearing - distance) * 0.85F * strength;
					float inverse = distance > 0.001F ? 1.0F / distance : 0.0F;
					x += dx * inverse * push;
					z += dz * inverse * push;
					alpha *= 1.0F - 0.8F * strength * (1.0F - distance / clearing);
				} else if (distance < clearing * PARTING_THICKEN_BAND) {
					// The displaced smoke banks up around the clearing's rim.
					float rim = 1.0F - Math.abs((distance - clearing * 1.25F) / (clearing * 0.35F));
					alpha *= 1.0F + 0.55F * strength * Math.max(0.0F, rim);
				}
			}
			puffX[index] = x;
			puffY[index] = y;
			puffZ[index] = z;
			puffAlpha[index] = alpha;
			float cdx = x - cameraX;
			float cdy = y - cameraY;
			float cdz = z - cameraZ;
			// Non-negative float bits sort like the floats themselves; the low word keeps the index.
			drawOrder[index] = ((long) Float.floatToIntBits(cdx * cdx + cdy * cdy + cdz * cdz) << 32) | index;
		}
		Arrays.sort(drawOrder, 0, count);

		RenderType renderType = HemoRenderTypes.cardinalRiteFog(time, seed);
		VertexConsumer consumer = buffer.getBuffer(renderType);
		poseStack.pushPose();
		poseStack.translate(centerX - cameraPosition.x, centerY - cameraPosition.y,
				centerZ - cameraPosition.z);
		Matrix4f matrix = poseStack.last().pose();
		// Back to front: farthest first.
		for (int order = count - 1; order >= 0; order--) {
			int index = (int) (drawOrder[order] & 0xFFFFFFFFL);
			emitPuff(consumer, matrix, puffs.get(index), puffX[index], puffY[index], puffZ[index],
					time, puffAlpha[index]);
		}
		poseStack.popPose();
		buffer.endBatch(renderType);
	}

	/** Spawn points of waves that began recently at this rite, as local offsets with their parting strength. */
	private static int gatherPartings(BlockPos center, double centerX, double centerZ, float time) {
		int count = 0;
		float partialTick = time - (float) Math.floor(time);
		for (CardinalRiteEventClientState.RiteEvent event : CardinalRiteEventClientState.recent(center)) {
			if (event.kind() != CardinalRiteEventKind.WAVE_START) continue;
			float strength = CardinalRiteFogGeometry.partingStrength(event.age(partialTick));
			if (strength <= 0.0F) continue;
			for (Vec3 point : event.points()) {
				if (count >= MAX_PARTINGS) return count;
				PARTING_X[count] = (float) (point.x - centerX);
				PARTING_Z[count] = (float) (point.z - centerZ);
				PARTING_STRENGTH[count] = strength;
				count++;
			}
		}
		return count;
	}

	private static void ensureCapacity(int count) {
		if (puffX.length >= count) return;
		puffX = new float[count];
		puffY = new float[count];
		puffZ = new float[count];
		puffAlpha = new float[count];
		drawOrder = new long[count];
	}

	private static void emitPuff(VertexConsumer consumer, Matrix4f matrix,
			CardinalRiteFogGeometry.FogPuff puff, float centerX, float centerY, float centerZ,
			float time, float alpha) {
		if (alpha <= 0.001F) return;
		float roll = puff.rollRadians()
				+ (float) Math.sin(time * puff.driftSpeed() * 10.37F + puff.phase()) * 0.10F;
		float cosine = (float) Math.cos(roll);
		float sine = (float) Math.sin(roll);
		float halfWidth = puff.halfWidth();
		float halfHeight = puff.halfHeight();
		// Rolled billboard axes from the camera's right/up vectors.
		float hx = (CAMERA_RIGHT.x * cosine + CAMERA_UP.x * sine) * halfWidth;
		float hy = (CAMERA_RIGHT.y * cosine + CAMERA_UP.y * sine) * halfWidth;
		float hz = (CAMERA_RIGHT.z * cosine + CAMERA_UP.z * sine) * halfWidth;
		float vx = (CAMERA_UP.x * cosine - CAMERA_RIGHT.x * sine) * halfHeight;
		float vy = (CAMERA_UP.y * cosine - CAMERA_RIGHT.y * sine) * halfHeight;
		float vz = (CAMERA_UP.z * cosine - CAMERA_RIGHT.z * sine) * halfHeight;

		// RGB carries stable per-puff shader metadata rather than a literal tint:
		// variant seed, crimson weighting, and brightness. Alpha is true opacity.
		emit(consumer, matrix, centerX - hx - vx, centerY - hy - vy, centerZ - hz - vz, 0.0F, 0.0F, puff, alpha);
		emit(consumer, matrix, centerX - hx + vx, centerY - hy + vy, centerZ - hz + vz, 0.0F, 1.0F, puff, alpha);
		emit(consumer, matrix, centerX + hx + vx, centerY + hy + vy, centerZ + hz + vz, 1.0F, 1.0F, puff, alpha);
		emit(consumer, matrix, centerX + hx - vx, centerY + hy - vy, centerZ + hz - vz, 1.0F, 0.0F, puff, alpha);
	}

	private static void emit(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
			float u, float v, CardinalRiteFogGeometry.FogPuff puff, float alpha) {
		consumer.addVertex(matrix, x, y, z)
				.setUv(u, v)
				.setColor(puff.variantSeed(), puff.crimsonWeight(), puff.brightness(), Math.min(1.0F, alpha));
	}

	private static float fogSeed(BlockPos center) {
		return center.getX() * 0.137F + center.getY() * 0.071F + center.getZ() * 0.193F;
	}

	static float fogVerticalOffset() {
		return HemoClientConfig.CARDINAL_RITE_FOG_VERTICAL_OFFSET == null
				? 0.0F
				: HemoClientConfig.CARDINAL_RITE_FOG_VERTICAL_OFFSET.get().floatValue();
	}
}
