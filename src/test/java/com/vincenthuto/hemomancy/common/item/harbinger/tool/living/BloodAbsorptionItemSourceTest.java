package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class BloodAbsorptionItemSourceTest {

	private BloodAbsorptionItemSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodAbsorptionItem.java");
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientInputEvents.java");
		String channel = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/verb/VerbChannel.java");
		String onUseTick = source.substring(source.indexOf("public void onUseTick"),
				source.indexOf("public static Optional<LivingEntity> findBareAbsorptionTarget"));
		String use = source.substring(source.indexOf("public InteractionResultHolder<ItemStack> use"));
		String absorbFromTarget = source.substring(source.indexOf("public static double absorbFromTarget"),
				source.indexOf("public static boolean isValidAbsorptionTarget"));

		assertBefore("bare absorption skips client drain ticks",
				onUseTick, "if (worldIn.isClientSide)", "absorbFromTarget");
		assertContains("bare absorption uses the one verb reach",
				onUseTick, "VerbReach.of(player, Verb.ABSORB)");
		assertBefore("a claiming Sculpting acts before the full-body stop",
				onUseTick, "SculptingDispatcher.trySculpt(sculptor, Verb.ABSORB)", "BloodAbsorptionChannelRules.canDrainLivingTarget");
		assertContains("bare block absorption uses the shared bare per-tick helper",
				onUseTick, "LivingStaffFocusRules.bareBlockAbsorptionPerTick()");
		assertContains("bare absorption applies the channel's movement rule while held",
				onUseTick, "VerbChannel.applyMovement(channelingPlayer)");
		assertContains("bare living-target absorption uses pulse interval helper",
				onUseTick, "BloodAbsorptionChannelRules.livingTargetPulseInterval");
		assertContains("bare living-target absorption records strain only after mob drain",
				onUseTick, "updateChannelStrain");
		assertContains("full blood can still begin non-drain absorption rites",
				use, "BloodAbsorptionChannelRules.canStartChannel(volume.isActive())");
		assertBefore("full blood stops ordinary mob drain after block and Will rites get priority",
				onUseTick, "BloodAbsorptionChannelRules.canDrainLivingTarget", "findBareAbsorptionTarget");
		assertContains("NQ-04-10: Sated Siphon sends a full body's lost blood to a worn gourd",
				absorbFromTarget, "SkillPointInit.skill_sated_siphon");
		assertContains("NQ-04-10: the lost blood goes to the worn gourd",
				absorbFromTarget, "BloodGourdItem.fillWornGourd(absorbingPlayer, overflow)");
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
		assertContains("absorption derives what comes back from actual damage, scaled by the rider (CL-26)",
				absorbFromTarget, "double returned = absorbed * rider.blood()");
		assertContains("absorption derives personal credit from what came back",
				absorbFromTarget, "personalBlood(sharing, returned)");
		assertContains("shared siphon derives its redirect from what came back",
				absorbFromTarget, "sharedBlood(sharing, returned)");
		assertContains("the draw's damage names the one drawing (CL-24)",
				absorbFromTarget, "HemoDamageTypes.bloodAbsorption(level, user)");
		if (absorbFromTarget.contains("damageSources().generic()")) {
			throw new AssertionError("the draw no longer deals ownerless generic damage (CL-24)");
		}
		assertContains("the rider table is read from the creature's school states",
				absorbFromTarget, "AbsorptionRiderEffects.riderFor(target)");
		assertContains("Crimson Mastery scales the draw's damage",
				absorbFromTarget, "SkillPointHelper.getCrimsonMasteryMultiplier(absorbingPlayer)");
		assertContains("channel movement penalty reads Dragging Siphon",
				source, "SkillPointHelper.getDraggingSiphonLevel");
		assertContains("channel movement penalty reads Mobile Conduit",
				source, "SkillPointHelper.getMobileConduitLevel");
		assertContains("channel movement penalty reads Unbound Siphon",
				source, "SkillPointHelper.hasUnboundSiphon");
		assertContains("channel movement exposes multiplier for client input",
				source, "getChannelMovementMultiplier");
		assertContains("channel movement compensates client input for vanilla use-item slowdown",
				channel, "BloodAbsorptionChannelRules.clientInputMultiplier(movementMultiplier(player))");
		assertContains("channel movement detects bare blood absorption use",
				channel, "stack.getItem() instanceof BloodAbsorptionItem");
		assertContains("channel movement detects staff use of the active verb",
				channel, "stack.getItem() instanceof LivingStaffItem");
		assertContains("a Sculpting that claims decides rooting",
				channel, "claim.selected().sculpting().roots()");
		assertContains("client input event handles channel movement at the input layer",
				clientEvents, "MovementInputUpdateEvent");
		assertContains("client input event checks the verb channel",
				clientEvents, "VerbChannel.channelling(player)");
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
		assertContains("absorption excludes armor stands from server and particle targeting",
				source, "target instanceof ArmorStand");
		assertContains("absorption excludes Hemomancy NPCs from server and particle targeting",
				source, "isHemomancyNpc(target)");
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex > secondIndex) {
			throw new AssertionError(label + " (expected '" + first + "' before '" + second + "')");
		}
	}
}
