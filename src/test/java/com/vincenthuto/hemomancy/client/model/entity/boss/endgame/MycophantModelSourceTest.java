package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantModelSourceTest {
	private static final Path SOURCE = Path.of("src/main/java/com/vincenthuto/hemomancy");
	private static final Path TEXTURES = Path.of("src/main/resources/assets/hemomancy/textures/entity/boss/endgame");

	@Test
	void theInGameAtlasUsesTheStandaloneArtAtItsOriginalResolution() throws Exception {
		assertArrayEquals(Files.readAllBytes(Path.of("docs/art/mycophant_spawn_mother/mycophant_spawn_mother.png")),
				Files.readAllBytes(TEXTURES.resolve("mycophant.png")));
	}

	@Test
	void theSharedModelResetsItsJoiningAnatomyForAnEarlierPhase() {
		var model = new MycophantModel();
		var thorax = model.root().getChild("body").getChild("thorax");
		var head = thorax.getChild("neck").getChild("head");
		var pulp = head.getChild("pulp");
		var closedMouth = head.getChild("skull").getChild("maw");
		var wings = thorax.getChild("wings");
		assertFalse(pulp.visible);
		assertFalse(wings.getChild("wing_l").visible);
		assertFalse(wings.getChild("wing_r").visible);

		model.applySplit(999.0F, 0.0F, 0.0F);
		assertTrue(pulp.visible);
		assertTrue(wings.getChild("wing_l").visible);
		assertTrue(wings.getChild("wing_r").visible);
		assertFalse(closedMouth.visible);

		model.applySplit(0.0F, 0.0F, 0.0F);
		assertFalse(pulp.visible);
		assertFalse(wings.getChild("wing_l").visible);
		assertFalse(wings.getChild("wing_r").visible);
		assertTrue(closedMouth.visible);
	}

	@Test
	void theMirroredInGameMeshKeepsOutwardWindingForTheCulledGlowPass() {
		var model = new MycophantModel();
		model.applySplit(999.0F, 0.0F, 0.0F);
		var matrices = new PoseStack();
		matrices.scale(-1.0F, 1.0F, 1.0F);
		var capture = new FaceCapture();
		model.root().render(matrices, capture, 0, 0, -1);
		assertTrue(capture.vertices.size() > 400);
		for (int i = 0; i < capture.vertices.size(); i += 4) {
			var first = capture.vertices.get(i);
			var edge = new Vector3f(capture.vertices.get(i + 1)).sub(first);
			var diagonal = new Vector3f(capture.vertices.get(i + 2)).sub(first);
			assertTrue(edge.cross(diagonal).dot(capture.normals.get(i)) > 0.0F, "face " + i / 4);
		}
	}

	@Test
	void theRendererDrawsTheGlowAndTheCradledBrood() throws Exception {
		String renderer = Files.readString(SOURCE.resolve("client/render/entity/boss/endgame/MycophantRenderer.java"));
		assertFalse(renderer.contains("mycophant_awoken"));
		assertTrue(renderer.contains("new MycophantGlowLayer(this)"));
		assertTrue(renderer.contains("new MycophantBroodLayer(this, context)"));
		assertTrue(renderer.contains("getFlipDegrees"));
		assertTrue(Files.exists(TEXTURES.resolve("mycophant.png")));
		assertTrue(Files.exists(TEXTURES.resolve("mycophant_glow.png")));
		assertFalse(Files.exists(TEXTURES.resolve("mycophant_awoken.png")));
	}

	private static final class FaceCapture implements VertexConsumer {
		final List<Vector3f> vertices = new ArrayList<>();
		final List<Vector3f> normals = new ArrayList<>();

		@Override public VertexConsumer addVertex(float x, float y, float z) {
			vertices.add(new Vector3f(x, y, z));
			return this;
		}

		@Override public VertexConsumer setNormal(float x, float y, float z) {
			normals.add(new Vector3f(x, y, z));
			return this;
		}

		@Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
		@Override public VertexConsumer setUv(float u, float v) { return this; }
		@Override public VertexConsumer setUv1(int u, int v) { return this; }
		@Override public VertexConsumer setUv2(int u, int v) { return this; }
	}
}
