package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEffects;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VelorumRimeGeometryTest {
	private static final Vec3 CAMERA = new Vec3(4, 4, -8), RIGHT = new Vec3(1, 0, 0), UP = new Vec3(0, 1, 0);

	@Test
	void everyBeatStaysFiniteBoundedAndRetires() {
		for (int kind = VelorumEffects.TOLL; kind <= VelorumEffects.BLACK_ICE; kind++) {
			int life = VelorumEffects.life(kind);
			for (float age : new float[]{0, 1, 3, life / 2.0F, life - 1, life}) {
				var mesh = new Capture();
				var packet = new ManipulationVisualPacket(Form.VELORUM_RIME, -1, Vec3.ZERO, new Vec3(8, 0, 2), 6, life, kind);
				VelorumRimeGeometry.draw(packet, new PoseStack(), mesh, mesh, CAMERA, RIGHT, UP, 100 + age, age, 1);
				mesh.check(2400);
				if (age >= life) assertEquals(0, mesh.vertices, "beat " + kind + " retires");
			}
		}
	}

	@Test
	void theBeatsThePassDrawsFromSyncedStateDrawNothingHere() {
		// visual pass 2: the moves, the black ice, the struck and the preserved are VelorumTelegraphPass's
		for (int kind : new int[]{VelorumEffects.TOLL, VelorumEffects.FROST_SLASH, VelorumEffects.STILL_FLOOR, VelorumEffects.LONG_NIGHT,
				VelorumEffects.SHATTER, VelorumEffects.PRESERVED, VelorumEffects.FREED, VelorumEffects.GUTTER, VelorumEffects.BLACK_ICE}) {
			var mesh = new Capture();
			var packet = new ManipulationVisualPacket(Form.VELORUM_RIME, -1, Vec3.ZERO, new Vec3(8, 0, 2), 6, VelorumEffects.life(kind), kind);
			VelorumRimeGeometry.draw(packet, new PoseStack(), mesh, mesh, CAMERA, RIGHT, UP, 101, 1, 1);
			assertEquals(0, mesh.vertices, "beat " + kind + " is drawn by the pass");
		}
		var empty = new Capture();
		VelorumRimeGeometry.spire(new PoseStack(), empty, Vec3.ZERO, .1, 0, Vec3.ZERO, 1, 1);
		assertEquals(0, empty.vertices);
		var spire = new Capture();
		// .5 is the brightest any caller lays a spire down at; overlays stay partly clear
		VelorumRimeGeometry.spire(new PoseStack(), spire, Vec3.ZERO, .1, .3, Vec3.ZERO, 1, .5F);
		assertEquals(16, spire.vertices, "a spire has four faces");
	}

	@Test
	void theFadeEasesOutAndEnds() {
		assertEquals(1.0F, VelorumRimeGeometry.fade(0, 20));
		assertEquals(0.0F, VelorumRimeGeometry.fade(20, 20));
		assertTrue(VelorumRimeGeometry.fade(15, 20) < VelorumRimeGeometry.fade(5, 20));
	}

	private static final class Capture implements VertexConsumer {
		int vertices;
		public VertexConsumer addVertex(float x, float y, float z) {
			assertTrue(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z)); vertices++; return this;
		}
		public VertexConsumer setColor(int r, int g, int b, int a) {
			for (int color : new int[]{r, g, b, a}) assertTrue(color >= 0 && color <= 255);
			assertTrue(a <= 0.62F * 255 + 1, "overlays stay partly clear");
			return this;
		}
		public VertexConsumer setUv(float u, float v) { assertTrue(Float.isFinite(u) && Float.isFinite(v)); return this; }
		public VertexConsumer setUv1(int u, int v) { return this; }
		public VertexConsumer setUv2(int u, int v) { return this; }
		public VertexConsumer setNormal(float x, float y, float z) { return this; }
		void check(int max) { assertEquals(0, vertices % 4); assertTrue(vertices <= max, "Vertices: " + vertices); }
	}
}
