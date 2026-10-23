package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.support.RepoPaths;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The saved form of an active rite, kind by kind, pinned key for key. The rite's per-kind state lives in its own
 * objects since step 11; these snapshots, taken before that split, prove each kind still writes exactly the NBT it
 * wrote before, so worlds saved mid-rite load unchanged. Each fixture also reloads to the same NBT.
 *
 * <p>A missing snapshot is written under {@code build/rite-nbt-layout/} and the test fails; review it and copy it
 * into {@code src/test/resources/rite/nbt-layout/}.
 */
final class ActiveCardinalRiteNbtLayoutTest {
	private static final UUID CASTER = UUID.fromString("00000000-0000-0000-0000-00000000c0de");
	private static final UUID ALLY = UUID.fromString("00000000-0000-0000-0000-0000000a11e5");
	private static final UUID NPC = UUID.fromString("00000000-0000-0000-0000-000000000e9c");
	private static final UUID THREAT = UUID.fromString("00000000-0000-0000-0000-0000000bad00");
	private static final UUID PUPPET = UUID.fromString("00000000-0000-0000-0000-00000000d011");
	private static final UUID CROSSBAR = UUID.fromString("00000000-0000-0000-0000-0000000c4055");
	private static final UUID IDENTITY = UUID.fromString("00000000-0000-0000-0000-00000001d000");
	private static final BlockPos CENTER = new BlockPos(12, 64, -7);

	private static ResourceLocation rite(String path) {
		return ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/" + path);
	}

	@Test
	void countdownRite() {
		ActiveCardinalRite rite = new ActiveCardinalRite(CASTER, CENTER, rite("still_waters"), 400, 3);
		for (int i = 0; i < 5; i++) rite.tick();
		assertLayout("countdown", rite);
	}

	@Test
	void ceremonyInConsecration() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(CASTER, CENTER, rite("initiate_rite"), 1200, 3, 2,
				false, 0, 4);
		rite.setInstabilityDamagePriority(new int[] {3, 2, 1, 0});
		rite.configureRings(new int[] {0, 0, 1, 1}, false);
		rite.fillAnchor(0, 100);
		rite.fillAnchor(1, 40);
		rite.setSigilProgress("hemomancy:reservoir", 2);
		rite.awakenSigil("hemomancy:reservoir");
		rite.assignAlly(ALLY, CardinalRiteAllyRole.ANCHOR);
		rite.setSharedPoolOptIn(ALLY, true);
		rite.getNpcReservesMl().put(NPC, 7);
		rite.getBloodspentUntil().put(NPC, 99L);
		rite.addRiteThreat(THREAT);
		rite.addInstability(10);
		rite.carryIchor(20);
		rite.addSacrificeMl(15);
		rite.setMatchedFloor(ResourceLocation.fromNamespaceAndPath("hemomancy", "threshold_lesser"), Direction.EAST,
				Direction.UP);
		rite.beginStaffPlanting();
		rite.tickStaffPlanting();
		rite.getWaveDeck().add("swarm");
		for (int i = 0; i < 3; i++) rite.tick();
		assertLayout("ceremony_consecration", rite);
	}

	@Test
	void ceremonyBetweenWaves() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(CASTER, CENTER, rite("hematic_fortification"), 2400, 5, 4,
				false, 2, 8);
		for (int i = 0; i < 8; i++) rite.fillAnchor(i, rite.bloodNeededForAnchor(i));
		rite.enterInscription();
		rite.sealAltar(true);
		rite.getWaveDeck().add("swarm");
		rite.getWaveDeck().add("siphon");
		rite.completeWave();
		rite.storeReservoirBlood(60, 200);
		for (int i = 0; i < 4; i++) rite.tick();
		assertLayout("ceremony_still_interval", rite);
	}

	@Test
	void puppeteerTrial() {
		ActiveCardinalRite rite = ActiveCardinalRite.puppeteerTrial(CASTER, CENTER, rite("puppeteer_trial_sanguine_hound"), 5);
		rite.setMatchedFloor(ResourceLocation.fromNamespaceAndPath("hemomancy", "working_lesser"), Direction.SOUTH,
				Direction.UP);
		rite.beginPuppeteerTrial("sanguine_hound", PUPPET, CROSSBAR);
		rite.puppeteerTrial().updateHealth(7.0F, 28.0F);
		rite.puppeteerTrial().incrementMissingTicks();
		rite.addRiteThreat(PUPPET);
		assertLayout("puppeteer_trial", rite);
	}

	@Test
	void workingCraft() {
		ActiveCardinalRite rite = ActiveCardinalRite.workingCraft(CASTER, CENTER, rite("working/somatic_loom"), 5, 300);
		rite.feedSubject(120);
		rite.craft().tag().putString("EscrowState", "ESCROWED");
		rite.craft().tag().putUUID("RiteIdentity", IDENTITY);
		rite.craft().tag().putString("Side", "north");
		assertLayout("working_craft", rite);
	}

	@Test
	void stationUpgrade() {
		ActiveCardinalRite rite = stationRite();
		assertLayout("station_upgrade", rite);
	}

	@Test
	void aStationUpgradeSavedUnderTheAlembicKeyLoadsAsAStationUpgrade() {
		CompoundTag modern = stationRite().serialize();
		CompoundTag legacy = modern.copy();
		legacy.put("AlembicUpgrade", legacy.get("StationUpgrade"));
		legacy.remove("StationUpgrade");
		assertEquals(modern, ActiveCardinalRite.deserialize(legacy).serialize());
	}

	@Test
	void succession() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(CASTER, CENTER, rite("hematic_succession"), 1200, 7, 5,
				false, 0, 4);
		CompoundTag context = new CompoundTag();
		context.putUUID("Transaction", IDENTITY);
		context.putLong("Body", CENTER.offset(2, 2, 0).asLong());
		context.putInt("Ticks", 250);
		context.putBoolean("BloodConsumed", true);
		context.putString("Profession", "alchemist");
		rite.beginSuccession(context);
		assertLayout("succession", rite);
	}

	@Test
	void aRiteSavedBeforeStateVersionsLoadsAsACountdown() {
		CompoundTag old = new CompoundTag();
		old.putUUID("PlayerUUID", CASTER);
		old.putLong("CenterPos", CENTER.asLong());
		old.putString("RecipeId", "hemomancy:cardinal_rite/monolithic_script");
		old.putInt("TotalTicks", 600);
		old.putInt("RemainingTicks", 240);
		old.putInt("RiteSize", 5);
		assertLayout("pre_state_version", ActiveCardinalRite.deserialize(old));
	}

	private static ActiveCardinalRite stationRite() {
		var tier = com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog.get(
				com.vincenthuto.hemomancy.common.station.UpgradeStation.RESONANT_FORGE, 1);
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(CASTER, CENTER, tier.rite(), 2400, tier.requiredDegree(),
				tier.requiredDegree(), false, 0, tier.anchors());
		for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
		rite.enterInscription();
		rite.sealAltar(false);
		rite.fillUpgradeCircuit(0, tier.bloodPerCircuit() / 2);
		rite.upgrade().tag().putString("EscrowState", "ESCROWED");
		rite.upgrade().tag().putUUID("RiteIdentity", IDENTITY);
		return rite;
	}

	/** One line per top-level key, sorted, so a changed key reads as a one-line diff. */
	private static String layout(CompoundTag tag) {
		List<String> keys = new ArrayList<>(tag.getAllKeys());
		keys.sort(null);
		StringBuilder out = new StringBuilder();
		for (String key : keys) out.append(key).append(": ").append(tag.get(key)).append('\n');
		return out.toString();
	}

	private static void assertLayout(String name, ActiveCardinalRite rite) {
		CompoundTag written = rite.serialize();
		assertEquals(written, ActiveCardinalRite.deserialize(written.copy()).serialize(), name + " must reload to the same NBT");
		String actual = layout(written);
		Path golden = RepoPaths.resolve("src/test/resources/rite/nbt-layout/" + name + ".txt");
		try {
			if (!Files.exists(golden)) {
				Path recorded = RepoPaths.resolve("build/rite-nbt-layout/" + name + ".txt");
				Files.createDirectories(recorded.getParent());
				Files.writeString(recorded, actual, StandardCharsets.UTF_8);
				throw new AssertionError("No snapshot for " + name + "; recorded " + recorded);
			}
			assertEquals(Files.readString(golden, StandardCharsets.UTF_8).replace("\r\n", "\n"), actual,
					name + " no longer writes the NBT a saved world expects");
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
	}
}
