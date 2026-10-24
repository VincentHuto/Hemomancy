package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * S-4: attachments are read through {@code HemoCapabilityAccess}. Player accessors return the attachment itself, so
 * nothing bypasses them to reach a concrete type, and nothing wraps a player attachment in {@code Optional}.
 */
class AttachmentAccessTest {
	private static final Pattern DIRECT_ATTACHMENT = Pattern.compile(
			"\\b(?:get|getExisting|has|set|remove)Data\\(\\s*(?:[\\w.]+\\.)?HemoAttachmentTypes\\.");
	private static final Pattern DIRECT_CAPABILITY = Pattern.compile(
			"\\bgetCapability\\(\\s*(?:[\\w.]+\\.)?HemoCapabilityKeys\\.");

	/** Files that may touch attachments directly, each with the reason the access class does not cover them. */
	private static final Map<String, String> ALLOWED = Map.ofEntries(
			Map.entry("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityAccess.java",
					"the access class"),
			Map.entry("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityRegistrar.java",
					"bridges capabilities to attachments"),
			Map.entry("src/main/java/com/vincenthuto/hemomancy/common/tile/IBloodReservoir.java",
					"owns the block blood attachment for every reservoir"),
			Map.entry("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/station/StationUpgradeMigration.java",
					"one-shot migration out of the retired brewing and forge attachments"),
			Map.entry("src/main/java/com/vincenthuto/hemomancy/common/worldgen/terrain/phlegethontic/PhlegethonticOreReservations.java",
					"chunk attachment"),
			Map.entry("src/main/java/com/vincenthuto/hemomancy/common/worldgen/feature/PhlegethonticWorldgenFeature.java",
					"chunk attachment"),
			Map.entry("src/main/java/com/vincenthuto/hemomancy/mixin/core/MixinPhlegethonticVeinCompletion.java",
					"chunk attachment"),
			Map.entry("src/gameTest/java/com/vincenthuto/hemomancy/gametest/StationUpgradeGameTests.java",
					"seeds the retired brewing attachment to prove the migration"));

	@Test
	void attachmentsAreReadThroughTheAccessClass() throws Exception {
		List<String> offending = new ArrayList<>();
		TreeSet<String> used = new TreeSet<>();
		for (Path root : List.of(RepoPaths.MAIN_JAVA, RepoPaths.GAMETEST_JAVA)) {
			try (Stream<Path> files = Files.walk(root)) {
				for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
					String text = SourceAssertions.read(file);
					if (!DIRECT_ATTACHMENT.matcher(text).find() && !DIRECT_CAPABILITY.matcher(text).find()) continue;
					String shown = RepoPaths.display(file);
					if (ALLOWED.containsKey(shown)) {
						used.add(shown);
					} else {
						offending.add(shown);
					}
				}
			}
		}
		assertEquals(List.of(), offending, "Use HemoCapabilityAccess instead of reading attachments directly");
		assertEquals(new TreeSet<>(ALLOWED.keySet()), used, "Remove allow-list entries that no longer read attachments");
	}

	@Test
	void playerAccessorsReturnTheAttachmentItself() {
		List<String> optional = new ArrayList<>();
		for (Method method : HemoCapabilityAccess.class.getDeclaredMethods()) {
			if (!Modifier.isPublic(method.getModifiers()) || method.getParameterCount() != 1) continue;
			if (method.getParameterTypes()[0] == Player.class && method.getReturnType() == Optional.class) {
				optional.add(method.getName());
			}
		}
		assertEquals(List.of(), optional);
	}
}
