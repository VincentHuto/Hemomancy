package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.circus.CircusProgressRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class CircusPerceptionOverlay {
	private static int acclimation;
	private static boolean active;

	private CircusPerceptionOverlay() {
	}

	public static void setState(int score, boolean inside) {
		acclimation = CircusProgressRules.clamp(score);
		active = inside;
	}

	public static void clear() {
		acclimation = 0;
		active = false;
	}

	public static CircusProgressRules.Stage stage() {
		return CircusProgressRules.stage(acclimation);
	}

	public static boolean isActive() {
		return active;
	}

	public static void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		CircusProgressRules.Stage stage = stage();
		if (!active || !stage.hasPresentation() || minecraft.level == null || minecraft.player == null) return;
		int interval = stage.particleIntervalTicks();
		if (minecraft.player.tickCount % interval != 0) return;
		var random = minecraft.level.random;
		double radius = 2.0D + random.nextDouble() * 3.0D;
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double x = minecraft.player.getX() + Math.cos(angle) * radius;
		double y = minecraft.player.getY() + 0.2D + random.nextDouble() * 2.4D;
		double z = minecraft.player.getZ() + Math.sin(angle) * radius;
		var particle = switch (stage) {
			case DISTURBED -> ParticleTypes.CRIMSON_SPORE;
			case ACCLIMATING -> ParticleTypes.REVERSE_PORTAL;
			case ATTUNED -> ParticleTypes.END_ROD;
			default -> throw new IllegalStateException("Unaware Circus presentation cannot emit particles");
		};
		double dx = stage == CircusProgressRules.Stage.DISTURBED ? (random.nextDouble() - 0.5D) * 0.03D
				: stage == CircusProgressRules.Stage.ACCLIMATING ? (minecraft.player.getX() - x) * 0.01D : 0.0D;
		double dz = stage == CircusProgressRules.Stage.DISTURBED ? (random.nextDouble() - 0.5D) * 0.03D
				: stage == CircusProgressRules.Stage.ACCLIMATING ? (minecraft.player.getZ() - z) * 0.01D : 0.0D;
		minecraft.level.addParticle(particle, x, y, z, dx, stage == CircusProgressRules.Stage.ATTUNED ? 0.025D : 0.01D, dz);
		if (minecraft.player.tickCount % (interval * 12) == 0) {
			var sound = switch (stage) {
				case DISTURBED -> SoundEvents.AMBIENT_CAVE.value();
				case ACCLIMATING -> SoundEvents.ENCHANTMENT_TABLE_USE;
				case ATTUNED -> SoundEvents.AMETHYST_BLOCK_CHIME;
				default -> throw new IllegalStateException();
			};
			minecraft.level.playLocalSound(x, y, z, sound, SoundSource.AMBIENT, 0.28F,
					0.85F + random.nextFloat() * 0.3F, false);
		}
	}

	public static void render(GuiGraphics graphics) {
		if (!active || !stage().hasPresentation()) return;
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		int alpha = switch (stage()) {
			case DISTURBED -> 34;
			case ACCLIMATING -> 22;
			case ATTUNED -> 14;
			default -> 0;
		};
		int color = alpha << 24 | 0x780018;
		int clear = 0x00780018;
		int edge = Math.max(8, Math.min(width, height) / 18);
		graphics.fillGradient(0, 0, width, edge, color, clear);
		graphics.fillGradient(0, height - edge, width, height, clear, color);
		for (int x = 0; x < edge; x++) {
			float fade = 1.0F - x / (float) edge;
			int side = (int) (alpha * fade * fade) << 24 | 0x780018;
			graphics.fill(x, edge, x + 1, height - edge, side);
			graphics.fill(width - x - 1, edge, width - x, height - edge, side);
		}
	}

	public static void renderWorld(PoseStack poseStack, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		CircusProgressRules.Stage stage = stage();
		if (!active || !stage.hasPresentation() || minecraft.level == null || minecraft.player == null) return;
		CircusCarouselEntity carousel = minecraft.level.getEntitiesOfClass(CircusCarouselEntity.class,
				minecraft.player.getBoundingBox().inflate(32.0D)).stream()
				.min(java.util.Comparator.comparingDouble(minecraft.player::distanceToSqr)).orElse(null);
		if (carousel == null) return;

		Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
		Vec3 center = carousel.position();
		float time = minecraft.level.getGameTime() + partialTick;
		MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
		VertexConsumer consumer = buffers.getBuffer(HemoRenderTypes.QLIPHOTH_GLOW);
		Matrix4f matrix = poseStack.last().pose();
		float instability = stage.motionJitter() * 8.0F;

		for (int i = 0; i < stage.silhouetteCount(); i++) {
			double angle = Math.PI * 2.0D * (i + 0.25D) / stage.silhouetteCount();
			float drift = instability * (float) Math.sin(time * 0.19F + i * 2.1F);
			float x = (float) (center.x + Math.cos(angle) * (5.0D + drift) - camera.x);
			float y = (float) (center.y + 0.1D - camera.y);
			float z = (float) (center.z + Math.sin(angle) * (5.0D + drift) - camera.z);
			drawSilhouette(consumer, matrix, x, y, z, (float) angle,
					stage == CircusProgressRules.Stage.ATTUNED ? 104 : stage == CircusProgressRules.Stage.ACCLIMATING ? 74 : 52);
		}
		for (int i = 0; i < stage.clothCount(); i++) {
			double angle = Math.PI * 2.0D * i / stage.clothCount();
			float sway = instability * (float) Math.sin(time * 0.12F + i * 1.7F);
			float x = (float) (center.x + Math.cos(angle) * 6.4D - camera.x);
			float y = (float) (center.y + 0.8D - camera.y);
			float z = (float) (center.z + Math.sin(angle) * 6.4D - camera.z);
			drawCloth(consumer, matrix, x, y, z, (float) angle, sway,
					stage == CircusProgressRules.Stage.ATTUNED ? 72 : stage == CircusProgressRules.Stage.ACCLIMATING ? 48 : 30);
		}
		for (int i = 0; i < stage.lightCount(); i++) {
			double angle = Math.PI * 2.0D * (i + 0.5D) / stage.lightCount();
			float pulse = 0.85F + 0.15F * (float) Math.sin(time * 0.16F + i);
			float x = (float) (center.x + Math.cos(angle) * 4.2D - camera.x);
			float y = (float) (center.y + 4.2D - camera.y);
			float z = (float) (center.z + Math.sin(angle) * 4.2D - camera.z);
			drawLight(consumer, matrix, x, y, z, (float) angle,
					(int) ((stage == CircusProgressRules.Stage.ATTUNED ? 126 : 82) * pulse));
		}
		buffers.endBatch(HemoRenderTypes.QLIPHOTH_GLOW);
	}

	private static void drawSilhouette(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
			float angle, int alpha) {
		float sideX = (float) Math.cos(angle);
		float sideZ = (float) Math.sin(angle);
		drawFigurePlane(consumer, matrix, x, y, z, sideX, sideZ, alpha);
		drawFigurePlane(consumer, matrix, x, y, z, -sideZ, sideX, alpha);
	}

	private static void drawFigurePlane(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
			float sideX, float sideZ, int alpha) {
		quad(consumer, matrix,
				x - sideX * 0.22F, y, z - sideZ * 0.22F,
				x + sideX * 0.22F, y, z + sideZ * 0.22F,
				x + sideX * 0.42F, y + 1.45F, z + sideZ * 0.42F,
				x - sideX * 0.42F, y + 1.45F, z - sideZ * 0.42F,
				46, 3, 20, alpha);
		quad(consumer, matrix,
				x, y + 1.42F, z,
				x + sideX * 0.29F, y + 1.72F, z + sideZ * 0.29F,
				x, y + 2.02F, z,
				x - sideX * 0.29F, y + 1.72F, z - sideZ * 0.29F,
				88, 8, 35, alpha);
	}

	private static void drawCloth(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
			float angle, float sway, int alpha) {
		float sideX = (float) -Math.sin(angle);
		float sideZ = (float) Math.cos(angle);
		quad(consumer, matrix,
				x - sideX * 0.7F + sway, y, z - sideZ * 0.7F,
				x + sideX * 0.7F + sway, y + 0.18F, z + sideZ * 0.7F,
				x + sideX, y + 4.0F, z + sideZ,
				x - sideX, y + 4.0F, z - sideZ,
				108, 5, 38, alpha);
	}

	private static void drawLight(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
			float angle, int alpha) {
		float sideX = (float) Math.cos(angle) * 0.28F;
		float sideZ = (float) Math.sin(angle) * 0.28F;
		drawDiamond(consumer, matrix, x, y, z, sideX, sideZ, alpha);
		drawDiamond(consumer, matrix, x, y, z, -sideZ, sideX, alpha);
	}

	private static void drawDiamond(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
			float sideX, float sideZ, int alpha) {
		quad(consumer, matrix,
				x, y - 0.48F, z,
				x + sideX, y, z + sideZ,
				x, y + 0.48F, z,
				x - sideX, y, z - sideZ,
				255, 92, 126, alpha);
	}

	private static void quad(VertexConsumer consumer, Matrix4f matrix,
			float x1, float y1, float z1, float x2, float y2, float z2,
			float x3, float y3, float z3, float x4, float y4, float z4,
			int red, int green, int blue, int alpha) {
		consumer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
		consumer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
		consumer.addVertex(matrix, x3, y3, z3).setColor(red, green, blue, alpha);
		consumer.addVertex(matrix, x4, y4, z4).setColor(red, green, blue, alpha);
	}
}
