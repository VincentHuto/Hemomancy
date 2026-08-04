package com.vincenthuto.hemomancy.common.init;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RegistryCreativeOrderSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private RegistryCreativeOrderSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String blockInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");

		assertInOrder("base item registration should start with early progression before late relics",
				itemInit,
				"heart_pattern = BASEITEMS.register",
				"gourd_seeds = BASEITEMS.register",
				"sanguine_formation = BASEITEMS.register",
				"mnemonic_blueprint = BASEITEMS.register",
				"hematic_iron_scrap = BASEITEMS.register",
				"enzyme_primer = BASEITEMS.register",
				"hematic_memory = BASEITEMS.register",
				"scar_blank = BASEITEMS.register",
				"sanguine_conduit = BASEITEMS.register",
				"qliphoth_seed = BASEITEMS.register",
				"fungal_spine = BASEITEMS.register",
				"hemolytic_solution = BASEITEMS.register");

		assertInOrder("special item registration should put player-facing tools before debug tools",
				itemInit,
				"liber_sanguinum = SPECIALITEMS.register",
				"harbinger_assignment_ledger = SPECIALITEMS.register",
				"blood_absorption = SPECIALITEMS.register",
				"blood_projection = SPECIALITEMS.register",
				"living_syringe = SPECIALITEMS.register",
				"vial_rack = SPECIALITEMS.register",
				"morphling_jar = SPECIALITEMS.register",
				"chitinite_arm_banner = SPECIALITEMS.register",
				"structure_spawner = SPECIALITEMS.register",
				"debug_showcase = SPECIALITEMS.register",
				"structure_scanner = SPECIALITEMS.register");

		assertInOrder("item stream should expose special utilities before handheld weapons",
				itemInit,
				"BASEITEMS.getEntries()",
				"SPECIALITEMS.getEntries()",
				"HANDHELDITEMS.getEntries()",
				"SPAWNEGGS.getEntries()");

		assertInOrder("modeled block registration should follow degree progression",
				blockInit,
				"gourd = MODELEDBLOCKS.register",
				"vial_centrifuge = MODELEDBLOCKS.register",
				"ghastly_alembic = MODELEDBLOCKS.register",
				"somatic_loom = MODELEDBLOCKS",
				"mnemonic_reliquary = SPECIALBLOCKS.register",
				"scar_station = MODELEDBLOCKS.register",
				"mason_effigy = MODELEDBLOCKS.register",
				"morphling_incubator = MODELEDBLOCKS.register",
				"consecrated_bloodwell = MODELEDBLOCKS.register",
				"sanguine_monolith = MODELEDBLOCKS.register",
				"unstained_podium = MODELEDBLOCKS.register",
				"altar_of_cleansing = MODELEDBLOCKS.register",
				"pallid_retort = MODELEDBLOCKS.register");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertInOrder(String label, String text, String... expected) {
		int lastIndex = -1;
		for (String token : expected) {
			int index = text.indexOf(token, lastIndex + 1);
			if (index < 0) {
				throw new AssertionError(label + " (missing ordered token '" + token + "')");
			}
			lastIndex = index;
		}
	}
}
