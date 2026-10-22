package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 7 (roadmap Phase 5): a player pays blood only through {@code BloodAccounting}. Every other {@code drain(},
 * {@code subtractBloodVolume(} and {@code addBloodSpend(} is listed here, per file and count, with the volume it
 * touches; a new or stale entry fails. The player volume is synced only by {@code BloodVolumeEvents.syncVolume}.
 */
class BloodAccountingGuardrailTest {
	private static final String COMMON = "src/main/java/com/vincenthuto/hemomancy/common/";
	/** Not compiled: build.gradle excludes these source sets. */
	private static final String[] UNCOMPILED = {
			"src/main/java/com/vincenthuto/hemomancy/compat/mna/", "src/main/java/com/vincenthuto/hemomancy/compat/curios/" };

	/** A call on a lower-case receiver or a call result; {@code SomeRules.drain(...)} is not a volume. */
	private static final Pattern DRAIN = Pattern.compile("(?:\\b[a-z]\\w*|\\))\\.drain\\(");
	private static final Pattern SUBTRACT = Pattern.compile("\\.subtractBloodVolume\\(");
	private static final Pattern SPEND = Pattern.compile("\\.addBloodSpend\\(");
	private static final Pattern SET_MINUS = Pattern.compile("setBloodVolume\\(\\s*\\w+\\.getBloodVolume\\(\\)\\s*-");
	private static final Pattern VOLUME_PACKET = Pattern.compile("new BloodVolumeServerPacket\\(");

	private record Allowed(int count, String reason) {
	}

	private static final Map<String, Allowed> DRAINS = Map.ofEntries(
			entry(COMMON + "capability/player/harbinger/bloodvolume/BloodPaymentRules.java",
					new Allowed(1, "The payment itself")),
			entry(COMMON + "capability/player/harbinger/bloodvolume/BloodFlowLedger.java",
					new Allowed(1, "transferFromVessel drains the gourd, not the player")),
			entry(COMMON + "capability/player/harbinger/bloodvolume/routing/BloodRoutingHelper.java",
					new Allowed(1, "drawFromVolume draws from the owner's equipped gourd; the owner pays through BloodAccounting")),
			entry(COMMON + "capability/player/harbinger/bloodvolume/routing/BloodRoutingRules.java",
					new Allowed(1, "Routing source abstraction; LinkedPlayerSource has no callers, so no player pays here")),
			entry(COMMON + "capability/player/unstained/white_humor/WhiteHumorVolume.java",
					new Allowed(1, "White humor, not blood")),
			entry(COMMON + "entity/summon/BloodThrallEntity.java", new Allowed(1, "Drains the source reservoir it hauls from")),
			entry(COMMON + "menu/tile/crafting/EnzymaticScriptoriumMenu.java", new Allowed(2, "The Scriptorium's tank")),
			entry(COMMON + "tile/BloodContainerTransfer.java", new Allowed(1, "A reservoir's tank")),
			entry(COMMON + "tile/IBloodReservoir.java", new Allowed(1, "drainBlood: the reservoir's tank")),
			entry(COMMON + "tile/harbinger/crafting/GhastlyAlembicBlockEntity.java", new Allowed(6, "The Alembic's tank")),
			entry(COMMON + "tile/harbinger/crafting/HematicArmatureBlockEntity.java", new Allowed(1, "The Armature's tank")),
			entry(COMMON + "tile/harbinger/crafting/ResonantForgeBlockEntity.java", new Allowed(1, "The Forge's tank")),
			entry(COMMON + "tile/harbinger/functional/SanguineCondenserBlockEntity.java",
					new Allowed(1, "The Condenser's pool")),
			entry(COMMON + "tile/harbinger/functional/SemiSentientConstructBlockEntity.java",
					new Allowed(1, "The construct's tank")),
			entry(COMMON + "tile/unstained/crafting/PallidRetortBlockEntity.java", new Allowed(3, "White humor tanks")));

	private static final Map<String, Allowed> SUBTRACTS = Map.ofEntries(
			entry(COMMON + "block/harbinger/BlockBloodInteractions.java", new Allowed(1, "Absorption drains the reservoir")),
			entry(COMMON + "tile/BloodContainerTransfer.java", new Allowed(1, "A gourd")),
			entry(COMMON + "tile/harbinger/crafting/HematicArmatureBlockEntity.java", new Allowed(1, "A gourd")),
			entry(COMMON + "tile/harbinger/crafting/MorphlingIncubatorBlockEntity.java", new Allowed(1, "The Incubator's tank")),
			entry(COMMON + "tile/harbinger/crafting/MycelialCrucibleBlockEntity.java", new Allowed(2, "The Crucible's tank")),
			entry(COMMON + "tile/harbinger/crafting/MycelialLanternBlockEntity.java", new Allowed(1, "The Lantern's tank")),
			entry(COMMON + "tile/harbinger/functional/SanguineVigilBlockEntity.java", new Allowed(1, "The Vigil's store")));

	private static final Map<String, Allowed> SPENDS = Map.of(
			COMMON + "capability/player/harbinger/bloodvolume/BloodPaymentRules.java",
			new Allowed(1, "countsAsSpend"));

	private static final Map<String, Allowed> VOLUME_PACKETS = Map.of(
			COMMON + "capability/player/harbinger/bloodvolume/BloodVolumeEvents.java",
			new Allowed(1, "syncVolume, the one owner sync"),
			COMMON + "network/capa/harbinger/BloodVolumeServerPacket.java",
			new Allowed(1, "The payload's own decoder"));

	@Test
	void playerBloodIsDrainedOnlyByBloodAccounting() throws IOException {
		assertEquals(counts(DRAINS), found(DRAIN),
				"pay with BloodAccounting.pay(player, amount, Source), or list a non-player volume here with a reason");
	}

	@Test
	void subtractBloodVolumeNeverPaysForAPlayer() throws IOException {
		assertEquals(counts(SUBTRACTS), found(SUBTRACT),
				"subtractBloodVolume skips the payment rules; a player pays through BloodAccounting");
		assertEquals(Map.of(), found(SET_MINUS), "setBloodVolume(x - cost) is a payment; use BloodAccounting.pay");
	}

	@Test
	void bloodSpendIsCountedOnlyByBloodAccounting() throws IOException {
		assertEquals(counts(SPENDS), found(SPEND), "pass countsAsSpend in the payment's Source instead");
	}

	@Test
	void theOwnerVolumeIsSyncedOnlyBySyncVolume() throws IOException {
		assertEquals(counts(VOLUME_PACKETS), found(VOLUME_PACKET),
				"call BloodVolumeEvents.syncVolume, which also refreshes the blood diagnostics");
		String events = SourceAssertions.readMainJava(
				"common/capability/player/harbinger/bloodvolume/BloodVolumeEvents.java");
		SourceAssertions.assertContains("the packet is built inside syncVolume",
				SourceAssertions.methodBody(events, "public static void syncVolume("), "new BloodVolumeServerPacket(volume)");
	}

	@Test
	void everyPaymentEndsInSyncVolumeWhenBloodMoves() {
		String accounting = SourceAssertions.readMainJava(
				"common/capability/player/harbinger/bloodvolume/BloodAccounting.java");
		String settle = SourceAssertions.methodBody(accounting, "private static Payment settle(");
		SourceAssertions.assertContains("payments record to the ledger", settle, "BloodFlowLedger.recordApplied(");
		SourceAssertions.assertContains("payments sync the owner", settle, "BloodVolumeEvents.syncVolume(serverPlayer, volume)");
	}

	private static Map<String, Integer> counts(Map<String, Allowed> allowed) {
		Map<String, Integer> counts = new TreeMap<>();
		allowed.forEach((file, entry) -> counts.put(file, entry.count()));
		return counts;
	}

	private static Map<String, Integer> found(Pattern pattern) throws IOException {
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String shown = RepoPaths.display(file);
				if (isUncompiled(shown)) continue;
				Matcher matcher = pattern.matcher(SourceAssertions.read(file));
				int count = 0;
				while (matcher.find()) count++;
				if (count > 0) found.put(shown, count);
			}
		}
		return found;
	}

	private static boolean isUncompiled(String shown) {
		for (String prefix : UNCOMPILED) {
			if (shown.startsWith(prefix)) return true;
		}
		return false;
	}
}
