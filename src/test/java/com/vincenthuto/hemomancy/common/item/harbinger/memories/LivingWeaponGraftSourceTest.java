package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingWeaponGraftSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingWeaponGraftSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String dataComponentInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/DataComponentInit.java");
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String form = read("src/main/java/com/vincenthuto/hemomancy/common/item/component/LivingWeaponForm.java");
		String data = read("src/main/java/com/vincenthuto/hemomancy/common/item/component/LivingWeaponGraftData.java");
		String item = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftItem.java");
		String hemomancy = read("src/main/java/com/vincenthuto/hemomancy/Hemomancy.java");
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String graftModel = read("src/main/resources/assets/hemomancy/models/item/living_weapon_graft.json");

		assertContains("component registry declares graft data component", dataComponentInit, "LIVING_WEAPON_GRAFT_DATA");
		assertContains("component registry uses persistent codec", dataComponentInit, "LivingWeaponGraftData.CODEC");
		assertContains("component registry syncs over network", dataComponentInit, "LivingWeaponGraftData.STREAM_CODEC");
		assertContains("item registry declares one dynamic graft item", itemInit, "living_weapon_graft");
		assertContains("item registry uses graft item class", itemInit, "new LivingWeaponGraftItem");

		for (String name : new String[] { "BLADE", "AXE", "SPEAR", "CLAWS", "CROSSBOW", "TORCH", "FLAIL" }) {
			assertContains("form enum has " + name, form, name);
		}
		assertNotContains("form enum keeps vesper out of graft forms", form, "VESPER");
		assertContains("form enum exposes serialized id", form, "serializedName()");
		assertContains("form enum maps blade to manipulation", form, "conjure_blade");
		assertContains("form enum maps flail to manipulation", form, "conjure_flail");

		assertContains("graft data is a record", data, "record LivingWeaponGraftData(LivingWeaponForm form)");
		assertContains("graft data has codec", data, "CODEC");
		assertContains("graft data has stream codec", data, "STREAM_CODEC");
		assertContains("graft data has stack reader", data, "fromStack");
		assertContains("graft data has stack factory", data, "createStack");

		assertContains("graft item dynamic name", item, "getName(ItemStack stack)");
		assertContains("graft item tooltip", item, "appendHoverText");
		assertContains("graft item exposes creative variants", item, "creativeStacks()");
		assertContains("blade display is player-facing", item, "Blade Graft");
		assertContains("flail display is player-facing", item, "Flail Graft");
		assertNotContains("graft item does not expose Vesper Graft", item, "Vesper Graft");

		assertContains("creative tab expands graft variants", hemomancy, "LivingWeaponGraftItem.creativeStacks()");

		assertContains("client registers graft form predicate", clientEvents,
				"ItemProperties.register(ItemInit.living_weapon_graft.get(), Hemomancy.rloc(\"form\")");
		assertContains("graft base model uses form predicate", graftModel, "\"hemomancy:form\"");
		for (String name : new String[] { "blade", "axe", "spear", "claws", "crossbow", "torch", "flail" }) {
			String model = read("src/main/resources/assets/hemomancy/models/item/living_weapon_graft_" + name + ".json");
			assertContains("graft " + name + " model keeps graft base texture", model,
					"\"layer0\": \"hemomancy:item/hematic_memory\"");
			assertContains("graft " + name + " model uses old memory overlay", model,
					"\"layer1\": \"hemomancy:item/memories/memory_living_" + name + "_overlay\"");
		}
		assertNoPath("src/main/resources/assets/hemomancy/models/item/living_weapon_graft_vesper.json");
	}

	private static String read(String path) throws IOException {
		Path absolute = ROOT.resolve(path);
		if (!Files.exists(absolute)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(absolute).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (found '" + unexpected + "')");
		}
	}

	private static void assertNoPath(String path) {
		if (Files.exists(ROOT.resolve(path))) {
			throw new AssertionError("unexpected path exists: " + path);
		}
	}
}
