package com.vincenthuto.hemomancy.client.render.world.floor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/** A trench easing from its previous build must not trip over samples that build left empty. */
final class FloorMeshGashBlendTest {
	@Test
	void anUngroundedPreviousSampleBlendsToTheCurrentOne() {
		float[] current = {2.0F, 4.0F};
		assertArrayEquals(current, FloorMesh.mix(null, current, 0.3F),
				"the previous build had no ground at this sample, so there is nothing to blend from");
	}

	@Test
	void groundedSamplesStillBlend() {
		assertArrayEquals(new float[] {1.0F, 2.0F}, FloorMesh.mix(new float[] {0.0F, 0.0F}, new float[] {2.0F, 4.0F}, 0.5F));
	}
}
