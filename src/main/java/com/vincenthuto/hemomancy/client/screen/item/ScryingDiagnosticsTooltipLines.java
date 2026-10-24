package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotSnapshot;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ScryingDiagnosticsTooltipLines {
	private ScryingDiagnosticsTooltipLines() {
	}

	public static List<Component> maxBloodMods(MaxBloodSnapshot snapshot) {
		List<Component> lines = new ArrayList<>();
		if (snapshot == null || snapshot.contributions().isEmpty()) {
			lines.add(Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.no_max_blood_modifiers"));
			return lines;
		}
		for (MaxBloodContribution contribution : snapshot.contributions()) {
			lines.add(row(contribution.category(), contribution.label(),
					Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.ml",
							signedWhole(contribution.amount())),
					contribution.condition()));
		}
		return lines;
	}

	public static List<Component> bloodFlowMods(BloodFlowSnapshot snapshot) {
		List<Component> lines = new ArrayList<>();
		if (snapshot == null || snapshot.contributions().isEmpty()) {
			lines.add(Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.no_active_blood_flow_modifiers"));
			return lines;
		}
		for (BloodFlowContribution contribution : snapshot.contributions()) {
			lines.add(row(contribution.category(), contribution.label(),
					Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.ml_per_tick",
							flowRate(contribution)),
					contribution.condition()));
		}
		return lines;
	}

	public static List<Component> manipulationCostMods(ManipulationCostSnapshot snapshot) {
		List<Component> lines = new ArrayList<>();
		if (isEmptyCost(snapshot)) {
			lines.add(Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.no_selected_manipulation_cost"));
			return lines;
		}
		if (snapshot.contributions().isEmpty()) {
			lines.add(Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.no_manipulation_cost_modifiers"));
		} else {
			for (ManipulationCostContribution contribution : snapshot.contributions()) {
				lines.add(row(contribution.category(), contribution.label(),
						Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.multiplier",
								multiplier(contribution.multiplier())),
						contribution.condition()));
			}
		}
		lines.add(Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.effective_cost_ml",
				unsigned(snapshot.effectiveCost())));
		return lines;
	}

	private static boolean isEmptyCost(ManipulationCostSnapshot snapshot) {
		return snapshot == null || (snapshot.contributions().isEmpty()
				&& Math.abs(snapshot.baseCost()) < 0.000001D
				&& Math.abs(snapshot.effectiveCost()) < 0.000001D);
	}

	public static List<Component> manipulationSlotMods(ManipulationSlotSnapshot snapshot) {
		List<Component> lines = new ArrayList<>();
		if (snapshot == null || snapshot.contributions().isEmpty()) {
			lines.add(Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.no_manipulation_slot_modifiers"));
			return lines;
		}
		for (ManipulationSlotContribution contribution : snapshot.contributions()) {
			lines.add(row(contribution.category(), contribution.label(),
					signedInt(contribution.amount()), contribution.condition()));
		}
		if (snapshot.cappedOffSlots() > 0) {
			lines.add(Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.cap_slots_capped_off",
					snapshot.maxSlots(), snapshot.cappedOffSlots()));
		} else {
			lines.add(Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.cap_slots",
					snapshot.maxSlots()));
		}
		return lines;
	}

	/** "Category - Label", with " [condition]" when the source reports one. */
	public static Component sourceLabel(Enum<?> category, String label, String condition) {
		return condition.isEmpty()
				? Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.source",
						categoryName(category), text(label))
				: Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.source_with_condition",
						categoryName(category), text(label), text(condition));
	}

	public static Component categoryName(Enum<?> category) {
		return Component.translatable("screen.hemomancy.scrying_diagnostics.category."
				+ category.name().toLowerCase(Locale.ROOT));
	}

	/**
	 * A ledger label or condition: a translation key, or older plain text from a source that has not moved to one
	 * (a missing key shows as itself).
	 */
	private static Component text(String keyOrText) {
		return Component.translatable(keyOrText);
	}

	private static Component row(Enum<?> category, String label, Object value, String condition) {
		return condition.isEmpty()
				? Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.row",
						categoryName(category), text(label), value)
				: Component.translatable("screen.hemomancy.scrying_diagnostics_tooltip_lines.row_with_condition",
						categoryName(category), text(label), value, text(condition));
	}

	private static String flowRate(BloodFlowContribution contribution) {
		if (Math.abs(contribution.requestedPerTick() - contribution.appliedPerTick()) > 0.000001D) {
			return signed(contribution.appliedPerTick()) + "/" + signed(contribution.requestedPerTick());
		}
		return signed(contribution.appliedPerTick());
	}

	private static String signed(double value) {
		return String.format(Locale.ROOT, "%+.2f", value);
	}

	private static String unsigned(double value) {
		return String.format(Locale.ROOT, "%.2f", value);
	}

	private static String multiplier(double value) {
		return String.format(Locale.ROOT, "%.2f", value);
	}

	private static String signedWhole(double value) {
		return String.format(Locale.ROOT, "%+.0f", value);
	}

	private static String signedInt(int value) {
		return String.format(Locale.ROOT, "%+d", value);
	}
}
