package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumOpeningRules;
import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VelorumModelTest {
	private static VelorumModel<?> model(ModelPart root) {
		return new VelorumModel<>(root);
	}

	@Test
	void everyTrackPosesAndTheRestPoseComesBack() {
		var root = VelorumModel.createBodyLayer().bakeRoot();
		var model = model(root);
		model.pose(VelorumAnimState.Frame.REST, 45, 10, 5);
		var rest = root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.xScale, p.y)).toList();
		var chest = root.getChild("root").getChild("hips").getChild("chest");
		for (VelorumMove move : VelorumMove.values()) {
			int total = move.authored().total();
			boolean moved = false;
			for (int t = 0; t <= total; t += 2) {
				model.pose(VelorumAnimState.Frame.move(move, t), 45, 10, 5);
				for (ModelPart part : root.getAllParts().toList()) {
					assertTrue(Float.isFinite(part.xRot) && Float.isFinite(part.yRot) && Float.isFinite(part.zRot), move + " " + t);
				}
				moved |= !root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.xScale, p.y)).toList().equals(rest);
			}
			assertTrue(moved, move + " has a track");
		}
		model.pose(VelorumAnimState.Frame.REST, 45, 10, 5);
		assertEquals(rest, root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.xScale, p.y)).toList());
		assertNotNull(chest);
	}

	@Test
	void theBowDipsTheHoodAndTheGutterShrinksTheLight() {
		var root = VelorumModel.createBodyLayer().bakeRoot();
		var model = model(root);
		var chest = root.getChild("root").getChild("hips").getChild("chest");
		var core = chest.getChild("left_arm").getChild("left_forearm").getChild("left_hand").getChild("staff")
				.getChild("staff_lantern").getChild("staff_lantern_crystal").getChild("staff_lantern_core");
		model.pose(VelorumAnimState.Frame.REST, 0, 0, 0);
		float upright = chest.xRot;
		model.pose(VelorumAnimState.Frame.opening(VelorumOpeningRules.BOW_ID, 30, 70), 0, 0, 0);
		assertTrue(chest.xRot > upright + 0.2F, "the Bow dips him");
		model.pose(VelorumAnimState.Frame.opening(VelorumOpeningRules.GUTTER_ID, 20, 60), 0, 0, 0);
		assertTrue(core.xScale < 0.5F, "the Gutter shrinks the light, not the cage");
		model.pose(VelorumAnimState.Frame.ending(60), 0, 0, 0);
		assertTrue(chest.xRot > upright + 0.2F, "the hood bows as his body fails");
	}

	@Test
	void sleevesAndClawIceHangAndTheStaffIsVerticalAtRest() {
		var root = VelorumModel.createBodyLayer().bakeRoot();
		var model = model(root);
		var hips = root.getChild("root").getChild("hips");
		var chest = hips.getChild("chest");
		for (VelorumMove move : VelorumMove.values()) {
			model.pose(VelorumAnimState.Frame.move(move, move.authored().windup()), 30, 10, 5);
			for (String side : new String[]{"left", "right"}) {
				var arm = chest.getChild(side + "_arm");
				var fore = arm.getChild(side + "_forearm");
				PoseStack pose = new PoseStack();
				for (var part : new ModelPart[]{hips, chest, arm, fore}) part.translateAndRotate(pose);
				fore.getChild(side + "_sleeve").translateAndRotate(pose);
				assertVertical(pose, 0.02F);
			}
		}
		model.pose(VelorumAnimState.Frame.REST, 0, 0, 0);
		var arm = chest.getChild("left_arm");
		var fore = arm.getChild("left_forearm");
		var hand = fore.getChild("left_hand");
		PoseStack pose = new PoseStack();
		for (var part : new ModelPart[]{hips, chest, arm, fore, hand, hand.getChild("staff")}) part.translateAndRotate(pose);
		assertVertical(pose, 0.0001F);
	}

	private static void assertVertical(PoseStack pose, float slack) {
		var down = pose.last().pose().transformDirection(new Vector3f(0, 1, 0));
		assertEquals(1, down.y, slack);
		assertEquals(0, down.x, slack);
		assertEquals(0, down.z, slack);
	}

	@Test
	void phaseThreeTracksKeepTheirCommitBeat() {
		var cut = VelorumMove.HOLLOW_TOLL.timeline(new SaintMoveInputs(3, 0, 0, 1));
		assertEquals(VelorumMove.HOLLOW_TOLL.authored().windup(),
				VelorumAnimState.trackTime(VelorumMove.HOLLOW_TOLL, cut.windup(), new SaintMoveInputs(3, 0, 0, 1)), 1.0E-4F);
		assertEquals(10.0F, VelorumAnimState.trackTime(VelorumMove.STAFF_TOLL, 10.0F, SaintMoveInputs.NONE), 1.0E-4F);
	}

	@Test
	void theNewPartsResolve() {
		var chest = VelorumModel.createBodyLayer().bakeRoot().getChild("root").getChild("hips").getChild("chest");
		assertNotNull(chest.getChild("breast_seam"));
		var halo = chest.getChild("head").getChild("halo");
		for (int i = 0; i < 4; i++) assertNotNull(halo.getChild("halo_chain_" + i).getChild("halo_icicle_" + i));
	}

	@Test
	void authoredStaffFootReachesTheFloor() {
		var hips = VelorumModel.createBodyLayer().bakeRoot().getChild("root").getChild("hips");
		var chest = hips.getChild("chest");
		var arm = chest.getChild("left_arm");
		var fore = arm.getChild("left_forearm");
		var hand = fore.getChild("left_hand");
		var staff = hand.getChild("staff");
		PoseStack pose = new PoseStack();
		for (var part : new ModelPart[]{hips, chest, arm, fore, hand, staff}) part.translateAndRotate(pose);
		float bottom = staff.cubes.stream().map(c -> c.maxY).max(Float::compare).orElseThrow();
		var foot = pose.last().pose().transformPosition(new Vector3f(0, bottom / 16, 0)).mul(16);
		assertEquals(24, foot.y, .2F);
	}

	@Test
	void exportedFaceUvsMatchTheBakedJavaModel() {
		var project = ResourceReader.asset("models/entity/bbmodel/bosses/VelorumModel.bbmodel");
		int width = project.getAsJsonObject("resolution").get("width").getAsInt();
		int height = project.getAsJsonObject("resolution").get("height").getAsInt();
		var expected = new ArrayList<String>();
		for (var element : project.getAsJsonArray("elements"))
			for (var face : element.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
				var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
				expected.add(rectangle(uv.get(0).getAsFloat(), uv.get(1).getAsFloat(), uv.get(2).getAsFloat(), uv.get(3).getAsFloat()));
			}
		var collector = new UvCollector(width, height);
		VelorumModel.createBodyLayer().bakeRoot().render(new PoseStack(), collector, 0, 0, -1);
		Collections.sort(expected);
		Collections.sort(collector.faces);
		assertEquals(expected, collector.faces);
	}

	private static String rectangle(float u0, float v0, float u1, float v1) {
		return Math.round(Math.min(u0, u1) * 4) + ":" + Math.round(Math.min(v0, v1) * 4) + ":"
				+ Math.round(Math.max(u0, u1) * 4) + ":" + Math.round(Math.max(v0, v1) * 4);
	}

	private static final class UvCollector implements VertexConsumer {
		final ArrayList<String> faces = new ArrayList<>();
		final int width, height;
		int vertices;
		float minU = Float.POSITIVE_INFINITY, minV = Float.POSITIVE_INFINITY;
		float maxU = Float.NEGATIVE_INFINITY, maxV = Float.NEGATIVE_INFINITY;

		UvCollector(int width, int height) { this.width = width; this.height = height; }
		@Override public VertexConsumer addVertex(float x, float y, float z) { return this; }
		@Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
		@Override public VertexConsumer setUv(float u, float v) {
			minU = Math.min(minU, u * width); maxU = Math.max(maxU, u * width);
			minV = Math.min(minV, v * height); maxV = Math.max(maxV, v * height);
			if (++vertices % 4 == 0) {
				faces.add(rectangle(minU, minV, maxU, maxV));
				minU = minV = Float.POSITIVE_INFINITY;
				maxU = maxV = Float.NEGATIVE_INFINITY;
			}
			return this;
		}
		@Override public VertexConsumer setUv1(int u, int v) { return this; }
		@Override public VertexConsumer setUv2(int u, int v) { return this; }
		@Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
	}

	@Test
	void savedGeometryUsesTheQuarterUnitGridAndRealThickness() {
		VelorumModel.createBodyLayer().bakeRoot().getAllParts().forEach(part -> {
			for (float c : new float[]{part.x, part.y, part.z}) assertEquals(Math.round(c * 4), c * 4, .0001F);
			for (var cube : part.cubes) {
				for (float c : new float[]{cube.minX, cube.minY, cube.minZ, cube.maxX, cube.maxY, cube.maxZ})
					assertEquals(Math.round(c * 4), c * 4, .0001F);
				assertTrue(cube.maxX - cube.minX >= .25F);
				assertTrue(cube.maxY - cube.minY >= .25F);
				assertTrue(cube.maxZ - cube.minZ >= .25F);
			}
		});
	}

	@Test
	void oneTexelPerModelUnitAndTheGlowStaysOnVisibleSurfaces() throws Exception {
		var project = ResourceReader.asset("models/entity/bbmodel/bosses/VelorumModel.bbmodel");
		var path = ResourceReader.path("assets/hemomancy/textures/entity/boss/velorum/velorum.png");
		var embedded = project.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
		assertArrayEquals(Files.readAllBytes(path), Base64.getDecoder().decode(embedded.split(",", 2)[1]));
		var base = ImageIO.read(path.toFile());
		assertEquals(project.getAsJsonObject("resolution").get("width").getAsInt(), base.getWidth(), "16 px per block");
		var glow = ImageIO.read(ResourceReader.path("assets/hemomancy/textures/entity/boss/velorum/velorum_glow.png").toFile());
		assertEquals(base.getWidth(), glow.getWidth());
		assertEquals(base.getHeight(), glow.getHeight());
		int lit = 0, dark = 0;
		for (int y = 0; y < base.getHeight(); y++) for (int x = 0; x < base.getWidth(); x++) {
			if ((glow.getRGB(x, y) >>> 24) != 0) {
				assertNotEquals(0, base.getRGB(x, y) >>> 24);
				lit++;
			} else if ((base.getRGB(x, y) >>> 24) != 0) dark++;
		}
		assertTrue(lit > 0 && dark > lit * 3, "Shadow robes must frame the smaller luminous frost details");
	}
}
