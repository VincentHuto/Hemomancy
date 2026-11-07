package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VelorumLimbTrailTest {
	@Test
	void itReadsBackWhereTheLimbWasBetweenFrames() {
		VelorumLimbTrail trail = new VelorumLimbTrail();
		assertNull(trail.at(0.0F), "nothing kept yet");
		trail.record(10.0F, new Vec3(0, 0, 0));
		trail.record(11.0F, new Vec3(2, 0, 0));
		assertEquals(1.0D, trail.at(10.5F).x, 1.0E-6D);
		assertEquals(2.0D, trail.at(11.3F).x, 1.0E-6D, "half a tick past the newest is the newest");
		assertNull(trail.at(12.0F), "never ahead of the animation");
		assertNull(trail.at(9.0F), "never before the kept frames");
	}

	@Test
	void aSecondRenderInTheSameFrameIsSkippedAndAReloadStartsAgain() {
		VelorumLimbTrail trail = new VelorumLimbTrail();
		trail.record(10.0F, new Vec3(0, 0, 0));
		trail.record(11.0F, new Vec3(1, 0, 0));
		trail.record(11.0F, new Vec3(9, 0, 0));
		assertEquals(2, trail.size());
		assertEquals(1.0D, trail.at(11.0F).x, 1.0E-6D);
		trail.record(3.0F, new Vec3(5, 0, 0));
		assertEquals(1, trail.size(), "a body that went back in time starts a new trail");
	}

	@Test
	void itKeepsAboutASecondOfFrames() {
		VelorumLimbTrail trail = new VelorumLimbTrail();
		for (int i = 0; i < 200; i++) trail.record(i / 3.0F, new Vec3(i, 0, 0));
		assertEquals(VelorumLimbTrail.FRAMES, trail.size());
		assertNull(trail.at(0.0F));
		assertEquals(199.0D, trail.at(199 / 3.0F).x, 1.0E-4D);
	}
}
