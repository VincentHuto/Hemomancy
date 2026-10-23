package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * O-3: the content helpers stay the only copies. Owned entities save their owner through {@code SyncedOwner};
 * menus extend {@code HemoContainerMenu} and add the player inventory through it; quest NPCs ask
 * {@code ProtectedNpc.admitsDamage}; shapes turn through {@code ModelShapes.rotate}; effects leave
 * {@code isBeneficial} to their registered category.
 */
class ContentBoilerplateGuardrailTest {
	private static final Pattern PLAYER_GRID_SLOT = Pattern.compile(
			"new Slot\\(\\w+,\\s*\\(?\\s*\\w+\\s*\\+\\s*\\w+\\s*\\*\\s*9\\s*\\)?\\s*\\+\\s*9");

	@Test
	void ownersAreSavedThroughSyncedOwner() throws Exception {
		assertEquals(List.of(), filesContaining(RepoPaths.mainJava("common/entity"), "putUUID(\"Owner\"",
				"SyncedOwner.java"));
	}

	@Test
	void menusExtendHemoContainerMenu() throws Exception {
		assertEquals(List.of(), filesContaining(RepoPaths.mainJava("common/menu"), "extends AbstractContainerMenu",
				"HemoContainerMenu.java"));
		List<String> loops = new ArrayList<>();
		forEach(RepoPaths.mainJava("common/menu"), (shown, text) -> {
			if (!shown.endsWith("HemoContainerMenu.java") && PLAYER_GRID_SLOT.matcher(text).find()) loops.add(shown);
		});
		assertEquals(List.of(), loops, "Add the player inventory with addPlayerInventory");
	}

	@Test
	void questNpcDamageGatesUseProtectedNpc() throws Exception {
		Map<String, String> explicit = new TreeMap<>(Map.of(
				"HarbingerHermitEntity.java", "farewell death and cleared invulnerability",
				"CircusPerformerEntity.java", "administrative, finale and downed states",
				"CircusRingmasterEntity.java", "administrative, descent and act states",
				"ProtectedNpc.java", "the shared check"));
		List<String> found = new ArrayList<>();
		forEach(RepoPaths.mainJava("common/entity/npc"), (shown, text) -> {
			if (text.contains("DamageTypes.GENERIC_KILL")) found.add(shown.substring(shown.lastIndexOf('/') + 1));
		});
		assertEquals(List.copyOf(explicit.keySet()), found.stream().sorted().toList());
	}

	@Test
	void shapesTurnThroughModelShapes() throws Exception {
		List<String> rotators = new ArrayList<>();
		forEach(RepoPaths.MAIN_JAVA, (shown, text) -> {
			if (text.contains("VoxelShape rotate(") && !shown.endsWith("ModelShapes.java")) rotators.add(shown);
		});
		assertEquals(List.of(), rotators);
	}

	@Test
	void effectsLeaveBeneficialToTheirCategory() throws Exception {
		assertEquals(List.of(), filesContaining(RepoPaths.mainJava("common/effect"), "boolean isBeneficial()", ""));
	}

	private interface Visitor {
		void visit(String shown, String text);
	}

	private static void forEach(Path root, Visitor visitor) throws Exception {
		try (Stream<Path> files = Files.walk(root)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
				visitor.visit(RepoPaths.display(file), SourceAssertions.read(file));
			}
		}
	}

	private static List<String> filesContaining(Path root, String needle, String exceptFile) throws Exception {
		List<String> found = new ArrayList<>();
		forEach(root, (shown, text) -> {
			if (text.contains(needle) && (exceptFile.isEmpty() || !shown.endsWith("/" + exceptFile))) found.add(shown);
		});
		return found;
	}
}
