package com.vincenthuto.hemomancy.common.manipulation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodBindingManipulationSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private BloodBindingManipulationSourceTest() {
	}

	public static void main(String[] args) throws Exception {
		assertFile("manipulation implementation",
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodBindingManip.java");
		assertFile("memory model", "src/main/resources/assets/hemomancy/models/item/memory_blood_binding.json");
		assertFile("memory overlay",
				"src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_binding_overlay.png");
		assertFile("memory inquiry",
				"src/main/resources/data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/memory_blood_binding.json");
		assertFile("tendril client state",
				"src/main/java/com/vincenthuto/hemomancy/client/data/BloodBindingTendrilClientState.java");
		assertFile("tendril renderer",
				"src/main/java/com/vincenthuto/hemomancy/client/render/world/BloodBindingTendrilRenderer.java");
		assertFile("tendril sync packet",
				"src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/SyncBloodBindingTendrilS2CPacket.java");

		String manipulations = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java");
		assertContains(manipulations, "MANIPS.register(\"blood_binding\"");
		assertContains(manipulations, "new BloodBindingManip(\"blood_binding\", 125, 0, 0, EnumManipulationType.QUICK");
		assertContains(manipulations, "EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.LEGS");
		assertContains(manipulations, ".setSecondaryTend(EnumBloodTendency.DUCTILIS)");
		assertContains(manipulations, ".setCooldownTicks(60)");
		assertContains(read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodBindingManip.java"),
				"PacketHandler.sendBloodBindingTendril");

		String packets = read("src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java");
		assertContains(packets, "SyncBloodBindingTendrilS2CPacket.TYPE");
		assertContains(packets, "sendBloodBindingTendril");
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		assertContains(clientEvents, "BloodBindingTendrilClientState.tick()");
		assertContains(clientEvents, "BloodBindingTendrilRenderer.render(event.getPoseStack(), partialTick)");

		String tree = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java");
		assertContains(tree, "register(\"blood_binding\",470,342);");

		String items = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		assertContains(items, "memory_blood_binding");
		assertContains(items, "ManipulationInit.blood_binding");

		JsonObject recipe = JsonParser.parseString(read(
				"src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_binding.json"))
				.getAsJsonObject();
		assertEquals("recipe result", "hemomancy:memory_blood_binding", recipe.get("result").getAsString());
		assertEquals("recipe blood", 75, recipe.get("blood").getAsInt());
		assertEquals("recipe catalyst", "hemomancy:foul_paste",
				recipe.getAsJsonArray("catalysts").get(0).getAsJsonObject().get("item").getAsString());
		assertEquals("Animus enzyme", 1, recipe.getAsJsonObject("enzymes").get("animus").getAsInt());
		assertEquals("Ductilis enzyme", 1, recipe.getAsJsonObject("enzymes").get("ductilis").getAsInt());

		String language = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		assertContains(language, "\"item.hemomancy.memory_blood_binding\": \"Blood Binding\"");
		assertContains(language, "\"manip.blood_binding.desc\"");
		assertContains(read("docs/HEMOMANCY_REFERENCE.md"),
				"| `blood_binding` | 125 | Quick | Humilis | Animus / Ductilis | Legs | 60t |");
	}

	private static void assertFile(String label, String path) {
		if (!Files.isRegularFile(ROOT.resolve(path))) {
			throw new AssertionError("Missing " + label + ": " + path);
		}
	}

	private static String read(String path) throws Exception {
		Path resolved = ROOT.resolve(path);
		assertFile(path, path);
		return Files.readString(resolved).replace("\r\n", "\n");
	}

	private static void assertContains(String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError("Missing expected content: " + expected);
		}
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
