package com.vincenthuto.hemomancy.gametest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Frozen identifiers: every Hemomancy id in every loaded registry (static, NeoForge custom and
 * datapack), and the exact order of both creative tabs. Both are compared with the committed
 * lists under {@code src/gameTest/resources/frozen/}. Payload ids are frozen separately by
 * {@code PayloadProtocolVersionTest}.
 *
 * <p>Set {@code HEMOMANCY_FROZEN_SNAPSHOT_OUT} to a folder to write the current lists there
 * instead of comparing; only an approved migration may replace the committed files.
 */
@GameTestHolder("registration_validation")
@PrefixGameTestTemplate(false)
public final class FrozenIdentifierGameTests {

	private static final String OUT = System.getenv("HEMOMANCY_FROZEN_SNAPSHOT_OUT");

	private FrozenIdentifierGameTests() {
	}

	@GameTest(template = "empty")
	public static void everyHemomancyRegistryIdMatchesTheFrozenSnapshot(GameTestHelper helper) {
		List<String> ids = new ArrayList<>();
		helper.getLevel().registryAccess().registries().forEach(entry -> entry.value().keySet().stream()
				.filter(id -> id.getNamespace().equals(Hemomancy.MOD_ID))
				.forEach(id -> ids.add(entry.key().location() + " " + id)));
		ids.sort(null);
		check(helper, "registry-ids.txt", ids);
	}

	@GameTest(template = "empty")
	public static void creativeTabOrderMatchesTheFrozenSnapshot(GameTestHelper helper) {
		var parameters = new CreativeModeTab.ItemDisplayParameters(
				helper.getLevel().enabledFeatures(), true, helper.getLevel().registryAccess());
		List<String> lines = new ArrayList<>();
		for (String tab : List.of("hemomancytab", "hemomancywip")) {
			CreativeModeTab creativeTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(Hemomancy.rloc(tab));
			helper.assertTrue(creativeTab != null, "Creative tab is missing: " + tab);
			creativeTab.buildContents(parameters);
			lines.add("# " + tab);
			// Stacks with components (graft forms, the random saint syringe) are marked, not spelled out.
			for (ItemStack stack : creativeTab.getDisplayItems()) {
				lines.add(BuiltInRegistries.ITEM.getKey(stack.getItem())
						+ (stack.getComponentsPatch().isEmpty() ? "" : " *"));
			}
		}
		check(helper, "creative-tab-order.txt", lines);
	}

	private static void check(GameTestHelper helper, String file, List<String> actual) {
		if (OUT != null && !OUT.isBlank()) {
			try {
				Path out = Path.of(OUT).resolve(file);
				Files.createDirectories(out.getParent());
				Files.write(out, actual, StandardCharsets.UTF_8);
			} catch (IOException e) {
				helper.fail("Could not write " + file + ": " + e);
			}
			helper.succeed();
			return;
		}
		List<String> expected = read(helper, file);
		for (int i = 0; i < Math.max(expected.size(), actual.size()); i++) {
			String want = i < expected.size() ? expected.get(i) : "<end>";
			String got = i < actual.size() ? actual.get(i) : "<end>";
			helper.assertTrue(want.equals(got), file + " line " + (i + 1) + ": expected '" + want
					+ "', found '" + got + "'");
		}
		helper.succeed();
	}

	private static List<String> read(GameTestHelper helper, String file) {
		try (InputStream in = FrozenIdentifierGameTests.class.getResourceAsStream("/frozen/" + file)) {
			helper.assertTrue(in != null, "Missing committed snapshot frozen/" + file);
			return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines()
					.filter(line -> !line.isBlank()).toList();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
