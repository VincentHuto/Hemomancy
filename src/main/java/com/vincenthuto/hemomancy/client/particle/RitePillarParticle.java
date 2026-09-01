package com.vincenthuto.hemomancy.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRitePillarTiming;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class RitePillarParticle extends TextureSheetParticle {
	private static final int RIBBON_SEGMENTS = 12;
	private static final float AURA_HALF_WIDTH = 0.21F;
	private static final float CORE_HALF_WIDTH = 0.095F;
	private static final float RIBBON_SWAY = 0.055F;
	private final float halfHeight;

	public RitePillarParticle(ClientLevel level, double x, double y, double z,
			float height, SpriteSet sprites) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D);
		halfHeight = Math.max(0.125F, height * 0.5F);
		lifetime = CardinalRitePillarTiming.LIFETIME_TICKS;
		hasPhysics = false;
		gravity = 0.0F;
		alpha = CardinalRitePillarTiming.opacityAtAge(0);
		setColor(1.0F, 0.22F, 0.28F);
		pickSprite(sprites);
	}

	@Override
	public void tick() {
		xo = x;
		yo = y;
		zo = z;
		age++;
		if (!CardinalRitePillarTiming.isVisibleAtAge(age)) {
			remove();
			return;
		}
		alpha = CardinalRitePillarTiming.opacityAtAge(age);
	}

	@Override
	public void render(VertexConsumer buffer, Camera camera, float partialTick) {
		Vec3 cameraPosition = camera.getPosition();
		Vector3f look = camera.getLookVector();
		float centerX = (float) (Mth.lerp(partialTick, xo, x) - cameraPosition.x);
		float centerY = (float) (Mth.lerp(partialTick, yo, y) - cameraPosition.y);
		float centerZ = (float) (Mth.lerp(partialTick, zo, z) - cameraPosition.z);
		float phase = (level.getGameTime() + partialTick) * 0.12F;
		int light = getLightColor(partialTick);
		renderRibbon(buffer, centerX, centerY, centerZ, look, phase,
				AURA_HALF_WIDTH, RIBBON_SWAY, 0.56F, 0.015F, 0.025F, alpha * 0.32F, light);
		renderRibbon(buffer, centerX, centerY, centerZ, look, phase,
				CORE_HALF_WIDTH, RIBBON_SWAY, 1.0F, 0.16F, 0.22F, alpha, light);
	}

	private void renderRibbon(VertexConsumer buffer, float centerX, float centerY, float centerZ,
			Vector3f look, float phase, float halfWidth, float sway,
			float red, float green, float blue, float layerAlpha, int light) {
		var vertices = RitePillarParticleGeometry.ribbon(
				halfWidth, halfHeight, look.x(), look.z(), phase, RIBBON_SEGMENTS, sway);
		for (int index = 0; index < vertices.size(); index++) {
			RitePillarParticleGeometry.Vertex vertex = vertices.get(index);
			float heightFraction = (index / 4 + 0.5F) / RIBBON_SEGMENTS;
			float travelingPulse = 0.7F + 0.3F * (0.5F
					+ 0.5F * Mth.sin(phase * 2.4F - heightFraction * Mth.TWO_PI * 1.5F));
			buffer.addVertex(centerX + vertex.x(), centerY + vertex.y(), centerZ + vertex.z())
					.setUv(Mth.lerp(vertex.u(), getU0(), getU1()),
							Mth.lerp(vertex.v(), getV0(), getV1()))
					.setColor(
							Mth.clamp(red * travelingPulse, 0.0F, 1.0F),
							Mth.clamp(green * travelingPulse, 0.0F, 1.0F),
							Mth.clamp(blue * travelingPulse, 0.0F, 1.0F),
							layerAlpha)
					.setLight(light);
		}
	}

	@Override
	public int getLightColor(float partialTick) {
		return 0xF000F0;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return HLRenderTypeInit.DARK_GLOW_RENDER;
	}

	@Override
	public AABB getRenderBoundingBox(float partialTick) {
		return new AABB(x - 0.35D, y - halfHeight, z - 0.35D,
				x + 0.35D, y + halfHeight, z + 0.35D);
	}
}
