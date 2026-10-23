package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 10 (roadmap Phase 9, A-3): third-person player poses apply through {@code PlayerAnimationRegistry} in one
 * order, and the first-person hand renderers keep their event priorities, which order them against each other and
 * against other mods.
 */
class PlayerAnimationGuardrailTest {
	private static final Pattern POSE_CALL = Pattern.compile(
			"(CardinalRiteStaffPlantingClientState\\.applyThirdPersonPose|WarpChairPlayerPose\\.applyModelPose"
					+ "|CastingPlayerPose\\.apply|PlayerAnimationRegistry\\.applyThirdPersonPose)\\(");
	private static final Pattern HAND_HANDLER = Pattern.compile(
			"@SubscribeEvent(\\([^)]*\\))?\\s+public static void \\w+\\(RenderHandEvent event\\)");
	private static final Pattern PRIORITY = Pattern.compile("priority\\s*=\\s*EventPriority\\.(\\w+)");

	/** Where each third-person pose entry point may be called from (the pose classes also serve first person). */
	private static final Map<String, Set<String>> CALLERS = Map.of(
			"CardinalRiteStaffPlantingClientState.applyThirdPersonPose", Set.of("client/player/PlayerAnimationRegistry.java"),
			"WarpChairPlayerPose.applyModelPose", Set.of("client/player/PlayerAnimationRegistry.java"),
			"CastingPlayerPose.apply", Set.of("client/player/PlayerAnimationRegistry.java"),
			"PlayerAnimationRegistry.applyThirdPersonPose", Set.of("mixin/core/MixinHumanoidModel.java"));

	/** First-person hand renderers and their priorities. */
	private static final Map<String, String> HAND_PRIORITIES = Map.of(
			"client/event/CardinalRiteStaffPlantingClientEvents.java", "HIGHEST",
			"client/event/LivingStaffMorphClientEvents.java", "HIGHEST",
			"client/event/BloodVialInjectionClientEvents.java", "HIGH",
			"client/event/HematicMicroscopeClientEvents.java", "HIGH",
			"client/event/LivingTorchClientEvents.java", "NORMAL",
			"client/event/FungalProjectionClientEvents.java", "NORMAL",
			"client/event/AxonalTransductionClient.java", "NORMAL",
			"client/render/layer/player/EquippedMorphlingHandRenderer.java", "LOW",
			"client/event/CastingAnimationClientEvents.java", "LOWEST",
			"client/render/item/harbinger/HeldWeaponPose.java", "LOWEST");

	@Test
	void posesApplyThroughTheRegistry() throws IOException {
		Map<String, Set<String>> found = new TreeMap<>();
		for (Path file : sources()) {
			Matcher matcher = POSE_CALL.matcher(SourceAssertions.read(file));
			while (matcher.find()) found.computeIfAbsent(matcher.group(1), key -> new TreeSet<>()).add(relative(file));
		}
		assertEquals(new TreeMap<>(CALLERS), found, "add a pose to PlayerAnimationRegistry.THIRD_PERSON in its place");
	}

	@Test
	void handRenderersKeepTheirPriorities() throws IOException {
		Map<String, String> found = new TreeMap<>();
		for (Path file : sources()) {
			Matcher matcher = HAND_HANDLER.matcher(SourceAssertions.read(file));
			while (matcher.find()) {
				Matcher priority = PRIORITY.matcher(matcher.group(1) == null ? "" : matcher.group(1));
				found.put(relative(file), priority.find() ? priority.group(1) : "NORMAL");
			}
		}
		assertEquals(new TreeMap<>(HAND_PRIORITIES), found,
				"a first-person hand renderer's priority orders it against the others; list a new one here");
	}

	private static String relative(Path file) {
		return RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/');
	}

	private static List<Path> sources() throws IOException {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			return files.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> !RepoPaths.display(path).contains("/compat/mna/")).toList();
		}
	}
}
