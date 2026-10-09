package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PerformerHandAnchorsTest {
	@Test
	void gripSitsAtTheBottomCentreOfTheArmsOwnCubes() {
		// Fire Eater arm: a gauntlet prop on a child part must not move the grip.
		ModelPart fireEater = arm(CubeListBuilder.create().addBox(-3.2F, -2.0F, -1.55F, 3.2F, 12.0F, 3.1F),
				CubeListBuilder.create().addBox(-3.7F, 6.5F, -2.0F, 0.55F, 6.1F, 4.0F));
		assertGrip(new Vector3f(-1.6F, 10.0F, 0.0F), PerformerHandAnchors.gripOf(fireEater));
		// Strongman arm: the fist is the forearm's lower face, centred inside the wider bicep.
		ModelPart strongman = arm(CubeListBuilder.create()
				.addBox(-3.5F, -2.5F, -2.5F, 5.0F, 6.0F, 5.0F)
				.addBox(-3.0F, 3.5F, -2.0F, 4.0F, 6.0F, 4.0F), null);
		assertGrip(new Vector3f(-1.0F, 9.5F, 0.0F), PerformerHandAnchors.gripOf(strongman));
	}

	/** Bakes a right arm the way the performer models build theirs; model classes need a bootstrapped mod. */
	private static ModelPart arm(CubeListBuilder cubes, CubeListBuilder prop) {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition arm = mesh.getRoot().addOrReplaceChild("right_arm", cubes, PartPose.offset(-5.0F, 2.0F, 0.0F));
		if (prop != null) arm.addOrReplaceChild("gauntlet", prop, PartPose.ZERO);
		return LayerDefinition.create(mesh, 128, 128).bakeRoot().getChild("right_arm");
	}

	@Test
	void worldOffsetUndoesTheFrameRotationAroundTheEntityRoot() {
		Vector3f expected = new Vector3f(0.5F, 1.2F, -0.4F);
		Matrix4f translated = new Matrix4f().translation(5.0F, 6.0F, 7.0F);
		assertOffset(expected, PerformerHandAnchors.worldOffset(translated,
				translated.transformPosition(new Vector3f(expected))));
		// A base frame that already carries a camera rotation must still yield world-axis offsets.
		Matrix4f rotated = new Matrix4f().rotationY((float) Math.toRadians(90.0D)).translate(5.0F, 6.0F, 7.0F);
		assertOffset(expected, PerformerHandAnchors.worldOffset(rotated,
				rotated.transformPosition(new Vector3f(expected))));
	}

	private static void assertGrip(Vector3f expected, Vector3f actual) {
		assertEquals(expected.x, actual.x, 0.0001F);
		assertEquals(expected.y, actual.y, 0.0001F);
		assertEquals(expected.z, actual.z, 0.0001F);
	}

	private static void assertOffset(Vector3f expected, Vec3 actual) {
		assertEquals(expected.x, actual.x, 0.0001D);
		assertEquals(expected.y, actual.y, 0.0001D);
		assertEquals(expected.z, actual.z, 0.0001D);
	}
}
