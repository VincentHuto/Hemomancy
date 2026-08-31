package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveBloodStructureFeedClientData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class BloodStructureFeedWarpRendererTest {
	@Test
	void shellFacesKeepClearanceOutsideTheirBlockPlanes() throws ReflectiveOperationException {
		Method emitUnitFace = connectedFaceEmitter();

		for (Direction direction : Direction.values()) {
			RecordingVertexConsumer vertices = new RecordingVertexConsumer();
			emitUnitFace.invoke(null, vertices, new PoseStack().last(), direction, Set.of(BlockPos.ZERO),
					BlockPos.ZERO, Vec3.ZERO, 0xFFFFFFFF);
			float blockPlane = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0F : 0.0F;
			for (Vector3f vertex : vertices.positions) {
				float coordinate = (float) direction.getAxis().choose(vertex.x, vertex.y, vertex.z);
				assertTrue(direction.getAxisDirection() == Direction.AxisDirection.POSITIVE
						? coordinate >= blockPlane + 0.0015F : coordinate <= blockPlane - 0.0015F,
						() -> direction + " overlay lacks depth clearance at " + coordinate);
			}
		}
	}

	@Test
	void isolatedBlockCornersWarpOutward() throws ReflectiveOperationException {
		Method emitUnitFace = connectedFaceEmitter();

		for (Direction direction : Direction.values()) {
			RecordingVertexConsumer vertices = new RecordingVertexConsumer();
			emitUnitFace.invoke(null, vertices, new PoseStack().last(), direction, Set.of(BlockPos.ZERO),
					BlockPos.ZERO, Vec3.ZERO, 0xFFFFFFFF);
			for (int index = 0; index < vertices.positions.size(); index++) {
				Vector3f position = vertices.positions.get(index);
				Vector3f normal = vertices.normals.get(index);
				assertEquals(position.x < 0.5F ? -1.0F : 1.0F, Math.signum(normal.x),
						() -> direction + " splits the shared corner at " + position);
				assertEquals(position.y < 0.5F ? -1.0F : 1.0F, Math.signum(normal.y),
						() -> direction + " splits the shared corner at " + position);
				assertEquals(position.z < 0.5F ? -1.0F : 1.0F, Math.signum(normal.z),
						() -> direction + " splits the shared corner at " + position);
			}
		}
	}

	@Test
	void adjoiningBlocksWarpTheirSharedEdgeTogether() throws ReflectiveOperationException {
		Method emitUnitFace = connectedFaceEmitter();
		BlockPos east = BlockPos.ZERO.east();
		Set<BlockPos> positions = Set.of(BlockPos.ZERO, east);
		RecordingVertexConsumer vertices = new RecordingVertexConsumer();
		emitUnitFace.invoke(null, vertices, new PoseStack().last(), Direction.SOUTH, positions,
				BlockPos.ZERO, Vec3.ZERO, 0xFFFFFFFF);
		emitUnitFace.invoke(null, vertices, new PoseStack().last(), Direction.SOUTH, positions,
				east, Vec3.ZERO, 0xFFFFFFFF);

		int sharedVertices = 0;
		for (int index = 0; index < vertices.positions.size(); index++) {
			Vector3f position = vertices.positions.get(index);
			if (Math.abs(position.x - 1.0F) > 0.0001F) continue;
			Vector3f normal = vertices.normals.get(index);
			assertEquals(0.0F, normal.x, 0.001F, "coplanar blocks split their shared edge");
			assertTrue(normal.z > 0.0F, "the shell must still warp out from the covered blocks");
			sharedVertices++;
		}
		assertEquals(4, sharedVertices);
	}

	@Test
	void engulfmentFadesFromBloodRedToDarkRotGreen() throws ReflectiveOperationException {
		Method rotColor;
		try {
			rotColor = BloodStructureFeedWarpRenderer.class.getDeclaredMethod("rotColor", float.class);
		} catch (NoSuchMethodException missing) {
			fail("Lignum Mortis has no rot color progression");
			return;
		}
		rotColor.setAccessible(true);

		int fresh = (int) rotColor.invoke(null, 0.0F);
		int engulfed = (int) rotColor.invoke(null, 1.0F);
		assertTrue(red(fresh) > green(fresh) * 3 && red(fresh) > blue(fresh) * 3,
				"fresh coverage should be blood-red");
		assertTrue(green(engulfed) > red(engulfed) * 2 && green(engulfed) > blue(engulfed) * 2,
				"fully engulfed wood should be green");
		assertTrue(Math.max(red(engulfed), Math.max(green(engulfed), blue(engulfed))) < 96,
				"the final green should look dark and rotten");
	}

	@Test
	void lignumEngulfmentStartsFreshAndKeepsAgingAcrossCrawlUpdates() throws ReflectiveOperationException {
		ActiveBloodStructureFeedClientData.clear(List.of());
		try {
			ActiveBloodStructureFeedClientData.upsert(4L, List.of(BlockPos.ZERO), 0.92F, 40);
			ActiveBloodStructureFeedClientData.FeedEntry feed =
					ActiveBloodStructureFeedClientData.getActiveFeeds().getFirst();
			Method engulfmentProgress;
			try {
				engulfmentProgress = feed.getClass().getDeclaredMethod("getEngulfmentProgress", float.class);
			} catch (NoSuchMethodException missing) {
				fail("Lignum Mortis tint still uses its fixed distance-band progress");
				return;
			}

			assertEquals(0.0F, (float) engulfmentProgress.invoke(feed, 0.0F), 0.001F);
			for (int tick = 0; tick < 10; tick++) ActiveBloodStructureFeedClientData.tick();
			float halfway = (float) engulfmentProgress.invoke(feed, 0.0F);
			assertTrue(halfway > 0.0F && halfway < 1.0F);

			ActiveBloodStructureFeedClientData.upsert(4L,
					List.of(BlockPos.ZERO, BlockPos.ZERO.above()), 0.92F, 40);
			feed = ActiveBloodStructureFeedClientData.getActiveFeeds().getFirst();
			assertEquals(halfway, (float) engulfmentProgress.invoke(feed, 0.0F), 0.001F);
			for (int tick = 0; tick < 10; tick++) ActiveBloodStructureFeedClientData.tick();
			assertEquals(1.0F, (float) engulfmentProgress.invoke(feed, 0.0F), 0.001F);
		} finally {
			ActiveBloodStructureFeedClientData.clear(List.of());
		}
	}

	private static int red(int color) {
		return color >> 16 & 0xFF;
	}

	private static int green(int color) {
		return color >> 8 & 0xFF;
	}

	private static int blue(int color) {
		return color & 0xFF;
	}

	private static Method connectedFaceEmitter() {
		try {
			Method method = BloodStructureFeedWarpRenderer.class.getDeclaredMethod("emitUnitFace",
					VertexConsumer.class, PoseStack.Pose.class, Direction.class, Set.class,
					BlockPos.class, Vec3.class, int.class);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException missing) {
			fail("the warp renderer does not share connected-shell topology between adjoining faces");
			throw new AssertionError(missing);
		}
	}

	private static final class RecordingVertexConsumer implements VertexConsumer {
		private final List<Vector3f> positions = new ArrayList<>();
		private final List<Vector3f> normals = new ArrayList<>();

		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			positions.add(new Vector3f(x, y, z));
			return this;
		}

		@Override public VertexConsumer setColor(int red, int green, int blue, int alpha) { return this; }
		@Override public VertexConsumer setUv(float u, float v) { return this; }
		@Override public VertexConsumer setUv1(int u, int v) { return this; }
		@Override public VertexConsumer setUv2(int u, int v) { return this; }
		@Override
		public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
			normals.add(new Vector3f(normalX, normalY, normalZ));
			return this;
		}
	}
}
