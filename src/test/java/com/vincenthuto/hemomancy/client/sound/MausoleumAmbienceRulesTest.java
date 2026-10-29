package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MausoleumAmbienceRulesTest {
	/** A complex whose reliquary floor stands at y=60 and whose pit floor at y=20. */
	private static final int TOP = 60;
	private static final int PIT = 20;

	@Test
	void depthRunsFromTheReliquaryToThePit() {
		assertEquals(0.0F, MausoleumAmbienceRules.depth(60.0, TOP, PIT));
		assertEquals(0.5F, MausoleumAmbienceRules.depth(40.0, TOP, PIT), 1.0E-6F);
		assertEquals(1.0F, MausoleumAmbienceRules.depth(20.0, TOP, PIT));
	}

	@Test
	void depthIsClampedAboveTheEntranceAndBelowThePit() {
		assertEquals(0.0F, MausoleumAmbienceRules.depth(75.0, TOP, PIT));
		assertEquals(1.0F, MausoleumAmbienceRules.depth(5.0, TOP, PIT));
	}

	@Test
	void aComplexOnOneLevelIsAllEntrance() {
		assertEquals(0.0F, MausoleumAmbienceRules.depth(30.0, 40, 40));
	}

	@Test
	void theHeartIsBarelyThereAtTheDoorAndPlainInThePit() {
		assertEquals(0.05F, MausoleumAmbienceRules.heartVolume(0.0F), 1.0E-6F);
		assertEquals(0.4F, MausoleumAmbienceRules.heartVolume(1.0F), 1.0E-6F);
		float previous = 0.0F;
		for (int step = 0; step <= 10; step++) {
			float volume = MausoleumAmbienceRules.heartVolume(step / 10.0F);
			assertTrue(volume >= previous, "the beat never quietens going down");
			previous = volume;
		}
	}

	@Test
	void theBeatQuickensAndDeepensGoingDown() {
		assertEquals(70, MausoleumAmbienceRules.beatInterval(0.0F));
		assertEquals(45, MausoleumAmbienceRules.beatInterval(1.0F));
		assertTrue(MausoleumAmbienceRules.beatInterval(0.5F) < 70 && MausoleumAmbienceRules.beatInterval(0.5F) > 45);
		assertEquals(1.0F, MausoleumAmbienceRules.heartPitch(0.0F), 1.0E-6F);
		assertEquals(0.88F, MausoleumAmbienceRules.heartPitch(1.0F), 1.0E-6F);
	}

	@Test
	void dripsAreRareNearTheTopAndCommonInThePit() {
		assertEquals(1.0F / 220.0F, MausoleumAmbienceRules.dripChance(0.0F), 1.0E-7F);
		assertEquals(1.0F / 50.0F, MausoleumAmbienceRules.dripChance(1.0F), 1.0E-7F);
		assertTrue(MausoleumAmbienceRules.dripVolume(1.0F) > MausoleumAmbienceRules.dripVolume(0.0F));
	}

	@Test
	void theHeardDepthNeverJumps() {
		float heard = 0.0F;
		for (int tick = 0; tick < 400; tick++) {
			float next = MausoleumAmbienceRules.easeDepth(heard, 1.0F);
			assertTrue(next - heard <= MausoleumAmbienceRules.DEPTH_RATE + 1.0E-6F, "one tick moves depth a little");
			heard = next;
		}
		assertEquals(1.0F, heard, "the heard depth settles on its target");
	}

	@Test
	void presenceFadesInAndOutOverTwoSeconds() {
		float presence = 0.0F;
		for (int tick = 0; tick < 40; tick++) presence = MausoleumAmbienceRules.easePresence(presence, true);
		assertEquals(1.0F, presence, 1.0E-5F);
		presence = MausoleumAmbienceRules.easePresence(presence, true);
		assertEquals(1.0F, presence);
		for (int tick = 0; tick < 41; tick++) presence = MausoleumAmbienceRules.easePresence(presence, false);
		assertEquals(0.0F, presence);
	}

	@Test
	void hemorathSeraphaePutricielAndVelorumAreHeardAndANamelessWardIsNot() {
		assertTrue(MausoleumAmbienceRules.voiced(EnumSaintType.HEMORATH));
		assertTrue(MausoleumAmbienceRules.voiced(EnumSaintType.SERAPHAE));
		assertTrue(MausoleumAmbienceRules.voiced(EnumSaintType.PUTRICIEL));
		assertTrue(MausoleumAmbienceRules.voiced(EnumSaintType.VELORUM));
		assertFalse(MausoleumAmbienceRules.voiced(null));
	}

	@Test
	void theChoirIsFaintAndSwellsOnlyALittleGoingDown() {
		assertEquals(0.06F, MausoleumAmbienceRules.choirVolume(0.0F), 1.0E-6F);
		assertEquals(0.2F, MausoleumAmbienceRules.choirVolume(1.0F), 1.0E-6F);
		float previous = 0.0F;
		for (int step = 0; step <= 10; step++) {
			float volume = MausoleumAmbienceRules.choirVolume(step / 10.0F);
			assertTrue(volume >= previous, "the choir never quietens going down");
			assertTrue(volume < MausoleumAmbienceRules.heartVolume(1.0F), "the choir stays under Hemorath's heart");
			previous = volume;
		}
		assertEquals(1.0F, MausoleumAmbienceRules.choirPitch(0.0F), 1.0E-6F);
		assertEquals(0.92F, MausoleumAmbienceRules.choirPitch(1.0F), 1.0E-6F);
		// The sound file plays at 0.6 and the engine clamps below 0.5: the deepest choir must stay above it.
		assertTrue(0.6F * MausoleumAmbienceRules.choirPitch(1.0F) >= 0.5F);
	}

	@Test
	void anchorsAreLookedForRarelyAndNearby() {
		assertTrue(MausoleumAmbienceRules.ANCHOR_SCAN_INTERVAL >= 20, "never a block scan every tick");
		int across = MausoleumAmbienceRules.ANCHOR_SCAN_RADIUS * 2 + 1;
		int blocks = across * across * (MausoleumAmbienceRules.ANCHOR_SCAN_HEIGHT * 2 + 1);
		assertTrue(blocks <= 4096, "one look stays a small box: " + blocks + " blocks");
	}

	@Test
	void anchorsTickMoreOftenAndPlainerTowardTheInfirmary() {
		assertEquals(1.0F / 70.0F, MausoleumAmbienceRules.anchorTickChance(0.0F), 1.0E-7F);
		assertEquals(1.0F / 30.0F, MausoleumAmbienceRules.anchorTickChance(1.0F), 1.0E-7F);
		assertTrue(MausoleumAmbienceRules.anchorTickVolume(1.0F) > MausoleumAmbienceRules.anchorTickVolume(0.0F));
		assertTrue(MausoleumAmbienceRules.anchorTickVolume(1.0F) <= 0.5F, "a tick is soft");
	}

	@Test
	void anchorTicksSpreadEvenlyAboutTheirPitch() {
		float spread = MausoleumAmbienceRules.ANCHOR_PITCH_SPREAD;
		assertEquals(1.0F - spread, MausoleumAmbienceRules.anchorTickPitch(0.0F), 1.0E-6F);
		assertEquals(1.0F, MausoleumAmbienceRules.anchorTickPitch(0.5F), 1.0E-6F);
		assertEquals(1.0F + spread, MausoleumAmbienceRules.anchorTickPitch(1.0F), 1.0E-6F);
		assertEquals(1.0F + spread, MausoleumAmbienceRules.anchorTickPitch(3.0F), 1.0E-6F, "a roll out of range is held");
	}

	/* ---- Putriciel ---- */

	@Test
	void bandsAreLookedForRarelyAndNearby() {
		assertTrue(MausoleumAmbienceRules.BAND_SCAN_INTERVAL >= 20, "never a block scan every tick");
		int across = MausoleumAmbienceRules.BAND_SCAN_RADIUS * 2 + 1;
		int blocks = across * across * (MausoleumAmbienceRules.BAND_SCAN_HEIGHT * 2 + 1);
		assertTrue(blocks <= 4096, "one look stays a small box: " + blocks + " blocks");
	}

	@Test
	void aBurnedBandOnlyCracklesAndARottenOneOnlyCreaks() {
		int full = MausoleumAmbienceRules.BAND_FULL_COUNT;
		assertEquals(1.0F / MausoleumAmbienceRules.CRACKLE_TICKS, MausoleumAmbienceRules.crackleChance(full, 0), 1.0E-7F);
		assertEquals(0.0F, MausoleumAmbienceRules.creakChance(full, 0));
		assertEquals(1.0F / MausoleumAmbienceRules.CREAK_TICKS, MausoleumAmbienceRules.creakChance(0, full), 1.0E-7F);
		assertEquals(0.0F, MausoleumAmbienceRules.crackleChance(0, full));
		assertTrue(MausoleumAmbienceRules.CRACKLE_TICKS < MausoleumAmbienceRules.CREAK_TICKS,
				"fire chatters, wet wood creaks now and then");
	}

	@Test
	void theBandThatDominatesIsTheOneHeard() {
		assertTrue(MausoleumAmbienceRules.crackleChance(18, 6) > MausoleumAmbienceRules.crackleChance(6, 18));
		assertTrue(MausoleumAmbienceRules.creakChance(6, 18) > MausoleumAmbienceRules.creakChance(18, 6));
		// Roots in a burned band still creak, but far more rarely than in a rotten band of their own.
		int full = MausoleumAmbienceRules.BAND_FULL_COUNT;
		float rare = MausoleumAmbienceRules.creakChance(1000, full);
		assertTrue(rare > 0.0F);
		assertTrue(rare < MausoleumAmbienceRules.creakChance(0, full) / 4.0F);
	}

	@Test
	void withNoBandNearThereIsNothingToHear() {
		assertEquals(0.0F, MausoleumAmbienceRules.crackleChance(0, 0));
		assertEquals(0.0F, MausoleumAmbienceRules.creakChance(0, 0));
		assertEquals(0.5F, MausoleumAmbienceRules.burnedShare(0, 0), "neither band is favoured");
		assertTrue(MausoleumAmbienceRules.bandStrength(6) < MausoleumAmbienceRules.bandStrength(18), "a thin band thins out");
		assertEquals(1.0F, MausoleumAmbienceRules.bandStrength(MausoleumAmbienceRules.BAND_FULL_COUNT * 4), "and a thick one is held");
	}

	@Test
	void bandSoundsAreQuieterFarAway() {
		float radius = MausoleumAmbienceRules.BAND_SCAN_RADIUS;
		assertEquals(MausoleumAmbienceRules.CRACKLE_VOLUME, MausoleumAmbienceRules.crackleVolume(0.0D), 1.0E-6F);
		assertEquals(MausoleumAmbienceRules.CREAK_VOLUME * MausoleumAmbienceRules.FAR_VOLUME_SHARE,
				MausoleumAmbienceRules.creakVolume(radius), 1.0E-6F);
		float previous = Float.MAX_VALUE;
		for (int step = 0; step <= 12; step++) {
			float volume = MausoleumAmbienceRules.crackleVolume(step);
			assertTrue(volume <= previous, "a crackle never grows louder further off");
			assertTrue(volume > 0.0F, "a band block in the look is still heard");
			previous = volume;
		}
		assertTrue(MausoleumAmbienceRules.CRACKLE_VOLUME <= MausoleumAmbienceRules.CHAMBER_VOLUME, "no louder than the heart");
		assertTrue(MausoleumAmbienceRules.CREAK_VOLUME <= MausoleumAmbienceRules.CHAMBER_VOLUME);
	}

	@Test
	void aPyreFlaresAboutEveryHalfMinuteNeverOnTheBeat() {
		int interval = MausoleumAmbienceRules.FLARE_INTERVAL;
		int jitter = MausoleumAmbienceRules.FLARE_JITTER;
		assertEquals(interval - jitter, MausoleumAmbienceRules.flareInterval(0.0F));
		assertEquals(interval, MausoleumAmbienceRules.flareInterval(0.5F));
		assertEquals(interval + jitter, MausoleumAmbienceRules.flareInterval(1.0F));
		assertEquals(interval + jitter, MausoleumAmbienceRules.flareInterval(4.0F), "a roll out of range is held");
		assertTrue(MausoleumAmbienceRules.flareInterval(0.0F) >= 200, "never closer together than ten seconds");
		assertTrue(jitter > 0, "the flares do not keep time");
		assertTrue(MausoleumAmbienceRules.FLARE_FIRST_DELAY > 0 && MausoleumAmbienceRules.FLARE_FIRST_DELAY < interval);
	}

	@Test
	void theFlareIsAlwaysFarOffAndFaint() {
		int full = MausoleumAmbienceRules.BAND_FULL_COUNT;
		assertEquals(MausoleumAmbienceRules.FLARE_BURNED_VOLUME, MausoleumAmbienceRules.flareVolume(full, 0), 1.0E-6F);
		assertEquals(MausoleumAmbienceRules.FLARE_ROTTEN_VOLUME, MausoleumAmbienceRules.flareVolume(0, full), 1.0E-6F);
		assertTrue(MausoleumAmbienceRules.flareVolume(full, 0) > MausoleumAmbienceRules.flareVolume(0, full),
				"plainer from the burned bands");
		assertTrue(MausoleumAmbienceRules.FLARE_BURNED_VOLUME <= 0.3F, "a far flare is never loud");
		assertTrue(MausoleumAmbienceRules.flareDistance(0.0F) > MausoleumAmbienceRules.BAND_SCAN_RADIUS,
				"it flares beyond the band blocks being heard");
		assertEquals(MausoleumAmbienceRules.FLARE_FARTHEST, MausoleumAmbienceRules.flareDistance(1.0F), 1.0E-6F);
	}

	@Test
	void putricielsSoundsSpreadEvenlyAndStayAboveTheEnginesPitchFloor() {
		assertEquals(1.0F, MausoleumAmbienceRules.cracklePitch(0.5F), 1.0E-6F);
		assertEquals(1.0F + MausoleumAmbienceRules.CREAK_PITCH_SPREAD, MausoleumAmbienceRules.creakPitch(1.0F), 1.0E-6F);
		assertEquals(1.0F - MausoleumAmbienceRules.FLARE_PITCH_SPREAD, MausoleumAmbienceRules.flarePitch(0.0F), 1.0E-6F);
		// The engine clamps below 0.5; the lowest sound-file pitches in sounds.json are crackle 0.8 (fire ambient),
		// creak 0.6 (mangrove roots) and flare 0.55 (blaze shoot).
		assertTrue(0.8F * MausoleumAmbienceRules.cracklePitch(0.0F) >= 0.5F);
		assertTrue(0.6F * MausoleumAmbienceRules.creakPitch(0.0F) >= 0.5F);
		assertTrue(0.55F * MausoleumAmbienceRules.flarePitch(0.0F) >= 0.5F);
	}

	@Test
	void velorumsIceCreaksRarelyQuietlyAndAboveThePitchFloor() {
		assertEquals(MausoleumAmbienceRules.ICE_CREAK_INTERVAL, MausoleumAmbienceRules.iceCreakInterval(0.5F));
		assertEquals(600, MausoleumAmbienceRules.iceCreakInterval(0.0F), "never sooner than half a minute");
		assertEquals(1200, MausoleumAmbienceRules.iceCreakInterval(1.0F));
		assertTrue(MausoleumAmbienceRules.iceCreakVolume(1.0F) > MausoleumAmbienceRules.iceCreakVolume(0.0F));
		assertTrue(MausoleumAmbienceRules.iceCreakVolume(1.0F) <= 0.3F, "near silence: the creak is never loud");
		assertEquals(MausoleumAmbienceRules.ICE_CREAK_NEAREST, MausoleumAmbienceRules.iceCreakDistance(0.0F), 1.0E-6F);
		assertEquals(MausoleumAmbienceRules.ICE_CREAK_FARTHEST, MausoleumAmbienceRules.iceCreakDistance(1.0F), 1.0E-6F);
		// The lowest sound-file pitch in sounds.json for the creak is 0.55 (glass); the engine clamps below 0.5.
		assertTrue(0.55F * MausoleumAmbienceRules.iceCreakPitch(0.0F) >= 0.5F);
	}

	@Test
	void onlyTheListenersOwnStepsAreMuffled() {
		assertTrue(MausoleumAmbienceRules.ownFootstep("block.deepslate_bricks.step", true, 0.0));
		assertTrue(MausoleumAmbienceRules.ownFootstep("block.glass.step", true, 0.5));
		assertFalse(MausoleumAmbienceRules.ownFootstep("block.deepslate_bricks.step", true, 9.0), "someone else's step");
		assertFalse(MausoleumAmbienceRules.ownFootstep("entity.zombie.step", false, 0.0), "a mob's step");
		assertFalse(MausoleumAmbienceRules.ownFootstep("block.deepslate_bricks.fall", true, 0.0), "a landing");
		assertFalse(MausoleumAmbienceRules.ownFootstep("entity.player.attack.sweep", true, 0.0));
		assertTrue(MausoleumAmbienceRules.MUFFLED_STEP_VOLUME < 0.5F && MausoleumAmbienceRules.MUFFLED_STEP_PITCH < 1.0F,
				"muffled is quieter and lower");
	}

	@Test
	void stepsAreMuffledOnlyInsideVelorumsHallowedPieces() {
		List<BoundingBox> hallowed = List.of(new BoundingBox(0, 40, 0, 20, 50, 20), new BoundingBox(30, 30, 0, 40, 36, 8));
		assertTrue(MausoleumAmbienceRules.mufflesSteps(EnumSaintType.VELORUM, hallowed, new BlockPos(10, 41, 10)));
		assertTrue(MausoleumAmbienceRules.mufflesSteps(EnumSaintType.VELORUM, hallowed, new BlockPos(35, 31, 4)));
		assertFalse(MausoleumAmbienceRules.mufflesSteps(EnumSaintType.VELORUM, hallowed, new BlockPos(25, 41, 10)),
				"between the pieces");
		assertFalse(MausoleumAmbienceRules.mufflesSteps(EnumSaintType.VELORUM, hallowed, new BlockPos(10, 51, 10)),
				"above the roof");
		assertFalse(MausoleumAmbienceRules.mufflesSteps(EnumSaintType.PUTRICIEL, hallowed, new BlockPos(10, 41, 10)),
				"another Saint's halls keep their footsteps");
		assertFalse(MausoleumAmbienceRules.mufflesSteps(EnumSaintType.VELORUM, List.of(), new BlockPos(10, 41, 10)));
	}
}
