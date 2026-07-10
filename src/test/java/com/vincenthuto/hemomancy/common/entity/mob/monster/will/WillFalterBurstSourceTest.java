package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WillFalterBurstSourceTest {
	private static final String WILL = "src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java";

	private WillFalterBurstSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(Path.of(WILL));
		burstMeterUsesPlayerDamageOnly(source);
		burstMeterIsSingleAttackerAndTimed(source);
		oldLowHealthFalterTriggerIsRemoved(source);
		burstStateClearsWhenResolved(source);
	}

	private static void burstMeterUsesPlayerDamageOnly(String source) {
		assertContains("burst damage field exists", source, "private float falterBurstDamage;");
		assertContains("burst tracking is called after damage", source, "trackFalterBurst(source, dealt);");
		assertContains("non-player sources are ignored", source, "source.getEntity() instanceof Player player");
		assertContains("only broken wills falter from burst", source, "getOrigin() != WillOrigin.BROKEN");
		assertContains("only materialized wills track burst", source, "getPhase() != WillPhase.MATERIALIZED");
		assertContains("burst damage accumulates dealt damage", source, "falterBurstDamage += amount;");
		assertContains("burst threshold scales from max health", source, "getMaxHealth() * safeConfig(");
		assertContains("burst threshold uses configured fraction", source, "HemoServerConfig.WILL_FALTER_BURST_FRACTION");
		assertContains("burst threshold uses fallback fraction", source, "WillCombatRules.falterBurstFraction()");
		assertContains("burst threshold enters faltering", source, "enterFaltering();");
	}

	private static void burstMeterIsSingleAttackerAndTimed(String source) {
		assertContains("burst window field exists", source, "private int falterBurstWindowTicks;");
		assertContains("burst attacker field exists", source, "private UUID falterBurstAttackerId;");
		assertContains("expired burst window resets", source,
				"if (falterBurstWindowTicks > 0 && --falterBurstWindowTicks <= 0) {");
		assertContains("attacker change resets burst", source, "!player.getUUID().equals(falterBurstAttackerId)");
		assertContains("burst reset clears meter", source, "clearFalterBurst();");
		assertContains("burst window uses server config", source, "HemoServerConfig.WILL_FALTER_BURST_WINDOW_TICKS");
		assertContains("burst window uses fallback default", source, "WillCombatRules.falterBurstWindowTicks()");
	}

	private static void oldLowHealthFalterTriggerIsRemoved(String source) {
		assertNotContains("old hp threshold comparison removed", source, "getHealth() <= getMaxHealth() *");
		assertNotContains("old falterFraction fallback removed", source, "falterFraction()");
	}

	private static void burstStateClearsWhenResolved(String source) {
		assertContains("faltering clears burst tracking", source, "private void enterFaltering()");
		assertContains("dissolving clears burst tracking", source, "clearFalterBurst();");
		assertContains("burst damage is zeroed", source, "falterBurstDamage = 0.0F;");
		assertContains("burst window is zeroed", source, "falterBurstWindowTicks = 0;");
		assertContains("burst attacker is zeroed", source, "falterBurstAttackerId = null;");
	}

	private static void assertContains(String label, String source, String expected) {
		if (!source.contains(expected)) {
			throw new AssertionError(label + ": missing `" + expected + "`");
		}
	}

	private static void assertNotContains(String label, String source, String unexpected) {
		if (source.contains(unexpected)) {
			throw new AssertionError(label + ": found `" + unexpected + "`");
		}
	}
}
