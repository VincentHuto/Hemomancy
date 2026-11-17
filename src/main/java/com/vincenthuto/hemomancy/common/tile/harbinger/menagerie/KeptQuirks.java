package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * A studied kind's quirks (core-loop round two, P6): the keeper's notes the second level of research reveals. They are
 * not new mechanics. Each reads a rule that already holds for the kind (the Living Syringe, rattling, forcing the glass,
 * its upkeep, which vessel it fits, whether it breeds) or, on one vessel's screen, what that individual carries. Shown
 * only to a player who has studied the kind; nothing about them is tracked.
 */
public final class KeptQuirks {
	public static final String PREFIX = "message.hemomancy.menagerie.quirk.";

	private KeptQuirks() {
	}

	/**
	 * What is known of a kind and, when {@code inherited} is given, of one kept individual.
	 *
	 * @param jarKind whether the kind goes in a Specimen Jar (otherwise only a Vat holds it)
	 * @param breeds  whether two of the kind may be paired to breed
	 */
	public record Facts(boolean hostile, float maxHealth, boolean needsSyringe, boolean jarKind, boolean breeds,
			@Nullable EnumBloodTemper inherited) {
	}

	/** One note: its id (its lang key is {@link #PREFIX} + id) and the value it names, if any. */
	public record Quirk(String id, double value, @Nullable EnumBloodTemper school) {
		public String key() {
			return PREFIX + id;
		}
	}

	/** The notes for {@code facts}, in the order a screen lists them. */
	public static List<Quirk> of(Facts facts) {
		List<Quirk> quirks = new ArrayList<>();
		if (facts.needsSyringe()) quirks.add(new Quirk("syringe", 0.0D, null));
		quirks.add(new Quirk("upkeep", VatVigorRules.mlPerVigor(facts.maxHealth()), null));
		quirks.add(new Quirk(facts.jarKind() ? "jar" : "vat", 0.0D, null));
		if (facts.hostile()) quirks.add(new Quirk("rattles", VatVigorRules.RATTLE_BELOW, null));
		if (VatVigorRules.mayBreakOut(0.0D, facts.hostile(), facts.maxHealth())) {
			quirks.add(new Quirk("breaks_out", VatVigorRules.BREAKOUT_BELOW, null));
		}
		if (facts.breeds()) quirks.add(new Quirk("breeds", BroodRules.broodCost(facts.maxHealth()), null));
		else quirks.add(new Quirk("no_brood", 0.0D, null));
		if (facts.inherited() != null) quirks.add(new Quirk("inherited", 0.0D, facts.inherited()));
		return quirks;
	}

	public static List<String> ids(Facts facts) {
		return of(facts).stream().map(Quirk::id).toList();
	}

	/** The notes as lines, naming a school with {@code temperName}. */
	public static List<Component> lines(Facts facts, Function<EnumBloodTemper, Component> temperName) {
		List<Component> lines = new ArrayList<>();
		for (Quirk quirk : of(facts)) {
			if (quirk.school() != null) lines.add(Component.translatable(quirk.key(), temperName.apply(quirk.school())));
			else if (quirk.value() > 0.0D) lines.add(Component.translatable(quirk.key(), number(quirk.value())));
			else lines.add(Component.translatable(quirk.key()));
		}
		return lines;
	}

	private static String number(double value) {
		return value == Math.rint(value) ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.1f", value);
	}
}
