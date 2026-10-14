package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteFloor;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldMath;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class RiteSoundBedTest {
	private static RiteWorldState.World world(BlockPos center, int stage) {
		float level = RiteWorldMath.level(stage, 4);
		return new RiteWorldState.World(center, null, level, level, 0.0F, stage, 4, CardinalRiteInstability.STABLE,
				8.0F, "storm", true, CardinalRitePhase.ORDEAL, RiteFloor.NONE, RiteWorldMath.Ending.NONE, 0.0F);
	}

	@Test
	void soundBedFollowsTheNearestRite() {
		BlockPos near = new BlockPos(5, 64, 0);
		BlockPos far = new BlockPos(30, 64, 0);

		RiteWorldState.World chosen = RiteSoundBed.nearest(List.of(world(far, 3), world(near, 1)), new Vec3(0, 65, 0));

		assertEquals(near, chosen.center());
	}

	@Test
	void riteBeforeItsFirstStepOrOutOfEarshotIsSilent() {
		assertNull(RiteSoundBed.nearest(List.of(world(BlockPos.ZERO, 0)), new Vec3(0, 65, 0)));
		assertNull(RiteSoundBed.nearest(List.of(world(new BlockPos(200, 64, 0), 3)), new Vec3(0, 65, 0)));
	}

	@Test
	void theHeartbeatKeepsTheFloorsTempo() {
		assertEquals(18, RiteSoundBed.beatPeriod(CardinalRitePhase.ORDEAL));
		assertEquals(18, RiteSoundBed.beatPeriod(CardinalRitePhase.CULMINATION));
		assertEquals(24, RiteSoundBed.beatPeriod(CardinalRitePhase.STILL_INTERVAL));
		assertEquals(36, RiteSoundBed.beatPeriod(null), "succession keeps the resting beat");
	}

	@Test
	void theBedIsNotCutOffBySoundDistance() {
		SoundEvent event = SoundEvent.createVariableRangeEvent(ResourceLocation.parse("hemomancy:rite.ambient.drone"));

		assertEquals(SoundInstance.Attenuation.NONE, new RiteLoopSound(event, 0.0D, 0.0D, 0.0D).getAttenuation(),
				"the viewer weight carries the falloff, not the sound engine's sixteen blocks");
		assertEquals(SoundInstance.Attenuation.NONE,
				RiteSoundBed.heartbeat(event, 0.0D, 0.0D, 0.0D, 0.5F, 1.0F).getAttenuation());
	}

	@Test
	void aLoopTheEngineDroppedIsStartedAgain() {
		RiteLoopSound sound = new RiteLoopSound(
				SoundEvent.createVariableRangeEvent(ResourceLocation.parse("hemomancy:rite.ambient.drone")),
				0.0D, 0.0D, 0.0D);

		assertTrue(RiteSoundBed.needsStart(null, false));
		assertTrue(RiteSoundBed.needsStart(sound, false), "dropped by the sound engine");
		assertFalse(RiteSoundBed.needsStart(sound, true));
	}
}
