package com.vincenthuto.hemomancy.common.creative;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class HemomancyCreativeTabsSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final List<String> WIP_ITEMS = List.of(
			"hematic_suture_needle",
			"unsigned_ancestral_ledger",
			"sanguine_blob",
			"fervent_husk",
			"curor_lens",
			"vascular_poultice",
			"memory_thread",
			"hearty_compass",
			"void_eye_organ",
			"zombie_husk_effigy",
			"desert_husk_effigy",
			"spider_husk_effigy",
			"vitality_chalice",
			"vein_spider",
			"constrictor_cord",
			"blood_thrall_effigy",
			"blood_bolt",
			"vascular_status_gauge",
			"hallowed_residuum_hemorath",
			"hallowed_residuum_seraphae",
			"hallowed_residuum_putriciel",
			"hallowed_residuum_velorum",
			"saint_relic_hemorath",
			"saint_relic_seraphae",
			"saint_relic_putriciel",
			"saint_relic_velorum",
			"echo_of_spleen",
			"echo_of_liver",
			"echo_of_heart",
			"echo_of_lungs",
			"echo_of_kidneys",
			"draught_of_still_mercy",
			"lethean_brew");
	private static final List<String> WIP_BLOCKS = List.of(
			"humane_idol",
			"serpentine_idol",
			"morphling_cradle",
			"witness_organ",
			"saint_sarcophagus",
			"gourdvine_tap",
			"sanguine_vigil",
			"visceral_mirror",
			"blood_basin",
			"blood_pylon",
			"blood_trial_altar",
			"offering_gate",
			"crucible_of_nether_ichor",
			"voidtouched_vessel");

	private HemomancyCreativeTabsSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String hemomancy = read("src/main/java/com/vincenthuto/hemomancy/Hemomancy.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");

		assertContains("main creative tab should use Charm of Vascularium as its icon",
				hemomancy, ".icon(() -> new ItemStack(ItemInit.charm_of_vascularium.get()))");
		assertContains("WIP creative tab should be registered",
				hemomancy, "hemomancywiptab = CREATIVETABS.register(");
		assertContains("WIP creative tab should use the requested registry name",
				hemomancy, "\"hemomancywip\"");
		assertContains("WIP creative tab should appear after the main Hemomancy tab",
				hemomancy, ".withTabsBefore(hemomancytab.getKey())");
		assertContains("WIP creative tab should use Sanguine Blob as its icon",
				hemomancy, ".icon(() -> new ItemStack(ItemInit.sanguine_blob.get()))");
		assertContains("WIP creative tab should have localized title",
				lang, "\"item_group.hemomancy.hemomancywip\": \"Hemomancy WIP\"");
		assertContains("Vascular Poultice should be localized",
				lang, "\"item.hemomancy.vascular_poultice\": \"Vascular Poultice\"");

		for (String item : WIP_ITEMS) {
			assertContains("WIP item list should contain " + item,
					hemomancy, "ItemInit." + item + ".get()");
		}
		for (String block : WIP_BLOCKS) {
			assertContains("WIP block list should contain " + block,
					hemomancy, "BlockInit." + block + ".get()");
		}
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
