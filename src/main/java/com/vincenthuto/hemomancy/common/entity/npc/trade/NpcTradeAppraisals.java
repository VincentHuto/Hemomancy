package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The named graders an {@code items} want can use. Each turns one stack into a {@link Grade}; a {@code POOR} stack is
 * refused, and a {@code FINE} trade pays {@code give_fine} (or one more rapport). A grader is added here with the
 * catalogue rows that need it, after reading which components those items carry (docs/archive/tracks/npc_trade/02).
 */
public final class NpcTradeAppraisals {
	public static final String NONE = "none";
	public static final String DURABILITY = "durability";
	public static final String RARITY = "rarity";
	public static final String TENDENCY = "tendency";
	public static final String SPECIMEN = "specimen";
	/** The {@code appraisal_tendency} field: which tendency a {@link #TENDENCY} want reads for. */
	public static final String TENDENCY_PARAM = "tendency";

	/** Below this share of durability left, worn gear is refused. */
	static final float DURABILITY_POOR_BELOW = 0.25F;
	/** At or above this share left, worn gear grades fine. */
	static final float DURABILITY_FINE_FROM = 0.75F;

	public enum Grade {
		POOR, FAIR, FINE;

		public String key() {
			return "hemomancy.trade.grade." + name().toLowerCase(Locale.ROOT);
		}

		/** The lower of two grades: a trade grades as its worst stack. */
		public Grade min(Grade other) {
			return ordinal() <= other.ordinal() ? this : other;
		}
	}

	/** Grades one stack. {@code clientSide} picks which copy of reloadable data (blood profiles) to read. */
	@FunctionalInterface
	public interface Grader {
		Grade grade(ItemStack stack, NpcTradeAppraisal appraisal, boolean clientSide);
	}

	/**
	 * A grader, the {@code appraisal_*} fields it reads, whether it can ever grade {@code FINE}, and a check of those
	 * fields' values that throws {@link IllegalArgumentException} for a bad one (the loader fails the file).
	 */
	public record Spec(Grader grader, Set<String> params, boolean canGradeFine, Consumer<Map<String, String>> check) {
		public Spec {
			params = Set.copyOf(params);
		}

		Spec(Grader grader, Set<String> params, boolean canGradeFine) {
			this(grader, params, canGradeFine, values -> {});
		}
	}

	private static final Map<String, Spec> GRADERS = Map.of(
			NONE, new Spec((stack, appraisal, clientSide) -> Grade.FAIR, Set.of(), false),
			DURABILITY, new Spec((stack, appraisal, clientSide) -> durability(stack), Set.of(), true),
			RARITY, new Spec((stack, appraisal, clientSide) -> rarityGrade(stack.getRarity()), Set.of(), true),
			TENDENCY, new Spec(NpcTradeAppraisals::tendency, Set.of(TENDENCY_PARAM), true,
					NpcTradeAppraisals::checkTendency),
			SPECIMEN, new Spec((stack, appraisal, clientSide) -> specimen(stack), Set.of(), true));

	private NpcTradeAppraisals() {}

	public static Optional<Spec> spec(String grader) {
		return Optional.ofNullable(GRADERS.get(grader));
	}

	public static Set<String> graders() {
		return GRADERS.keySet();
	}

	/** The server's grade, which is the one a trade pays by. */
	public static Grade grade(ItemStack stack, NpcTradeAppraisal appraisal) {
		return grade(stack, appraisal, false);
	}

	/** The grade read from the client's or the server's copy of reloadable data; the Trade box previews with the first. */
	public static Grade grade(ItemStack stack, NpcTradeAppraisal appraisal, boolean clientSide) {
		return spec(appraisal.grader()).map(spec -> spec.grader().grade(stack, appraisal, clientSide)).orElse(Grade.FAIR);
	}

	/** Gear graded by the share of durability left; an undamageable stack is fair. */
	static Grade durability(ItemStack stack) {
		if (!stack.isDamageableItem()) return Grade.FAIR;
		return durabilityGrade(stack.getDamageValue(), stack.getMaxDamage());
	}

	static Grade durabilityGrade(int damage, int maxDamage) {
		if (maxDamage <= 0) return Grade.FAIR;
		float left = 1.0F - (float) damage / maxDamage;
		if (left < DURABILITY_POOR_BELOW) return Grade.POOR;
		return left >= DURABILITY_FINE_FROM ? Grade.FINE : Grade.FAIR;
	}

	/** A curiosity graded by its rarity: common is fair, anything rarer is fine, nothing is poor. */
	static Grade rarityGrade(Rarity rarity) {
		return rarity == Rarity.COMMON ? Grade.FAIR : Grade.FINE;
	}

	/**
	 * A blood draw graded by its tendencies: a Bloody Vial (or other sample vessel) whose source's blood profile carries
	 * {@code appraisal_tendency}. An empty vessel or the wrong blood is poor; a sample already identified under the
	 * microscope is fine, since the bench can use it without running the assay again.
	 */
	static Grade tendency(ItemStack stack, NpcTradeAppraisal appraisal, boolean clientSide) {
		Optional<EnumBloodTendency> wanted = appraisal.param(TENDENCY_PARAM).flatMap(NpcTradeAppraisals::tendencyNamed);
		if (wanted.isEmpty() || !BloodSampleData.isSpecimenVessel(stack) || !BloodSampleData.isFilled(stack)) return Grade.POOR;
		BloodSampleData.Profile profile = BloodSampleData.profile(stack, clientSide);
		return tendencyGrade(profile.tendencies(), profile.identified(), wanted.get());
	}

	static Grade tendencyGrade(Collection<EnumBloodTendency> tendencies, boolean identified, EnumBloodTendency wanted) {
		if (!tendencies.contains(wanted)) return Grade.POOR;
		return identified ? Grade.FINE : Grade.FAIR;
	}

	static Optional<EnumBloodTendency> tendencyNamed(String name) {
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			if (tendency.name().equalsIgnoreCase(name)) return Optional.of(tendency);
		}
		return Optional.empty();
	}

	private static void checkTendency(Map<String, String> params) {
		String name = params.get(TENDENCY_PARAM);
		if (name == null) throw new IllegalArgumentException("A tendency appraisal needs 'appraisal_tendency'");
		if (tendencyNamed(name).isEmpty()) throw new IllegalArgumentException("Unknown tendency '" + name + "'");
	}

	/**
	 * A Specimen Jar graded by what it holds: an empty jar is poor, an ordinary creature fair, and one of the
	 * infection's own creatures ({@code hemomancy:} entities) fine. The jar stores no capture time, so there is no
	 * freshness to read.
	 */
	static Grade specimen(ItemStack stack) {
		CompoundTag specimen = SpecimenJarData.getSpecimen(stack);
		return specimenGrade(specimen.isEmpty() ? "" : specimen.getString("id"));
	}

	static Grade specimenGrade(String entityId) {
		if (entityId == null || entityId.isEmpty()) return Grade.POOR;
		return entityId.startsWith(Hemomancy.MOD_ID + ":") ? Grade.FINE : Grade.FAIR;
	}
}
