package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodAbsorptionItemSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private BloodAbsorptionItemSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodAbsorptionItem.java");
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String onUseTick = source.substring(source.indexOf("public void onUseTick"),
				source.indexOf("public static Optional<LivingEntity> findBareAbsorptionTarget"));
		String absorbFromTarget = source.substring(source.indexOf("public static double absorbFromTarget"),
				source.indexOf("public static boolean isValidAbsorptionTarget"));

		assertBefore("bare absorption skips client drain ticks",
				onUseTick, "if (worldIn.isClientSide)", "absorbFromTarget");
		assertContains("bare block absorption uses the bare absorption reach",
				onUseTick, "LivingStaffFocusRules.bareAbsorptionRange()");
		assertContains("bare block absorption uses the shared bare per-tick helper",
				onUseTick, "LivingStaffFocusRules.bareBlockAbsorptionPerTick()");
		assertContains("bare absorption applies channel movement penalty while held",
				onUseTick, "applyChannelMovementPenalty");
		assertContains("bare living-target absorption uses pulse interval helper",
				onUseTick, "BloodAbsorptionChannelRules.livingTargetPulseInterval");
		assertContains("bare living-target absorption records strain only after mob drain",
				onUseTick, "updateChannelStrain");
		assertBefore("absorbing from a target only hurts entities on the server",
				absorbFromTarget, "if (level.isClientSide)", "target.hurt");
		assertContains("absorption tracks target health before damage",
				absorbFromTarget, "healthBefore = target.getHealth()");
		assertContains("absorption records whether damage was accepted",
				absorbFromTarget, "boolean hurt = target.hurt");
		assertBefore("absorption measures real health loss before filling blood",
				absorbFromTarget, "target.getHealth()", "volume.fill");
		assertContains("absorption ignores rejected or zero-damage hits",
				absorbFromTarget, "if (!hurt || absorbed <= 0.0D)");
		assertContains("absorption credits actual damage instead of requested amount",
				absorbFromTarget, "volume.fill(absorbed)");
		assertContains("channel movement penalty reads Dragging Siphon",
				source, "SkillPointHelper.getDraggingSiphonLevel");
		assertContains("channel movement penalty reads Mobile Conduit",
				source, "SkillPointHelper.getMobileConduitLevel");
		assertContains("channel movement penalty reads Unbound Siphon",
				source, "SkillPointHelper.hasUnboundSiphon");
		assertContains("channel movement exposes multiplier for client input",
				source, "getChannelMovementMultiplier");
		assertContains("channel movement compensates client input for vanilla use-item slowdown",
				source, "getClientChannelMovementInputMultiplier");
		assertContains("channel movement detects bare blood absorption use",
				source, "player.getUseItem().getItem() instanceof BloodAbsorptionItem");
		assertContains("channel movement detects staff blood absorption use",
				source, "LivingStaffItem.isLivingStaffAbsorptionUse(player, player.getUseItem())");
		assertContains("client input event handles channel movement at the input layer",
				clientEvents, "MovementInputUpdateEvent");
		assertContains("client input event checks blood absorption channeling",
				clientEvents, "BloodAbsorptionItem.isChannelingBloodAbsorption(player)");
		assertContains("client input event scales forward input by channel multiplier",
				clientEvents, "input.forwardImpulse *= inputMultiplier");
		assertContains("client input event scales strafe input by channel multiplier",
				clientEvents, "input.leftImpulse *= inputMultiplier");
		assertContains("client input event clears movement keys when fully locked",
				clientEvents, "input.up = false;");
		assertContains("blood poisoning applies wither",
				source, "MobEffects.WITHER");

		assertContains("absorption excludes bloodless entities",
				source, "HemoEntityPredicates.NOBLOOD");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex > secondIndex) {
			throw new AssertionError(label + " (expected '" + first + "' before '" + second + "')");
		}
	}

	private static void assertContains(String label, String text, String needle) {
		if (!text.contains(needle)) {
			throw new AssertionError(label + " (missing '" + needle + "')");
		}
	}
}
