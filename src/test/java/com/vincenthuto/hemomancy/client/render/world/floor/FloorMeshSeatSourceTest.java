package com.vincenthuto.hemomancy.client.render.world.floor;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Standing braziers anywhere on the floor sink into flesh mounds; only those on the gash bend the trench. */
final class FloorMeshSeatSourceTest {
	@Test
	void seatsAreStandingBraziersOnTheFloorAndOnlyGashSeatsBendTheTrench() throws IOException {
		String mesh = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/render/world/floor/FloorMesh.java"));
		assertTrue(mesh.contains("CardinalRiteFloorGeometry.standingCells("), "seats come from every floor cell");
		assertTrue(mesh.contains("AttachFace.FLOOR"), "a sconce or hanging brazier does not sink into the floor");
		assertTrue(mesh.contains("GashLoop.circle(half, moatCentres, weights)"), "only braziers on the gash bend the trench");
	}
}
