package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ScryingDiagnosticsTooltipLines {
	private ScryingDiagnosticsTooltipLines() {
	}

	public static List<String> maxBloodMods(MaxBloodSnapshot snapshot) {
		List<String> lines = new ArrayList<>();
		if (snapshot == null || snapshot.contributions().isEmpty()) {
			lines.add("No max-blood modifiers.");
			return lines;
		}
		for (MaxBloodContribution contribution : snapshot.contributions()) {
			String line = titleCase(contribution.category().name()) + " - " + contribution.label()
					+ ": " + signedWhole(contribution.amount()) + " mL";
			if (!contribution.condition().isEmpty()) {
				line += " [" + contribution.condition() + "]";
			}
			lines.add(line);
		}
		return lines;
	}

	public static List<String> bloodFlowMods(BloodFlowSnapshot snapshot) {
		List<String> lines = new ArrayList<>();
		if (snapshot == null || snapshot.contributions().isEmpty()) {
			lines.add("No active blood-flow modifiers.");
			return lines;
		}
		for (BloodFlowContribution contribution : snapshot.contributions()) {
			String line = titleCase(contribution.category().name()) + " - " + contribution.label()
					+ ": " + flowRate(contribution) + " mL/t";
			if (!contribution.condition().isEmpty()) {
				line += " [" + contribution.condition() + "]";
			}
			lines.add(line);
		}
		return lines;
	}

	public static List<String> manipulationCostMods(ManipulationCostSnapshot snapshot) {
		List<String> lines = new ArrayList<>();
		if (isEmptyCost(snapshot)) {
			lines.add("No selected manipulation cost.");
			return lines;
		}
		if (snapshot.contributions().isEmpty()) {
			lines.add("No manipulation cost modifiers.");
		} else {
			for (ManipulationCostContribution contribution : snapshot.contributions()) {
				String line = titleCase(contribution.category().name()) + " - " + contribution.label()
						+ ": x" + multiplier(contribution.multiplier());
				if (!contribution.condition().isEmpty()) {
					line += " [" + contribution.condition() + "]";
				}
				lines.add(line);
			}
		}
		lines.add("Effective Cost: " + unsigned(snapshot.effectiveCost()) + " mL");
		return lines;
	}

	private static boolean isEmptyCost(ManipulationCostSnapshot snapshot) {
		return snapshot == null || (snapshot.contributions().isEmpty()
				&& Math.abs(snapshot.baseCost()) < 0.000001D
				&& Math.abs(snapshot.effectiveCost()) < 0.000001D);
	}

	public static List<String> manipulationSlotMods(ManipulationSlotSnapshot snapshot) {
		List<String> lines = new ArrayList<>();
		if (snapshot == null || snapshot.contributions().isEmpty()) {
			lines.add("No manipulation slot modifiers.");
			return lines;
		}
		for (ManipulationSlotContribution contribution : snapshot.contributions()) {
			String line = titleCase(contribution.category().name()) + " - " + contribution.label()
					+ ": " + signedInt(contribution.amount());
			if (!contribution.condition().isEmpty()) {
				line += " [" + contribution.condition() + "]";
			}
			lines.add(line);
		}
		if (snapshot.cappedOffSlots() > 0) {
			lines.add("Cap: " + snapshot.maxSlots() + " slots, " + snapshot.cappedOffSlots() + " capped off");
		} else {
			lines.add("Cap: " + snapshot.maxSlots() + " slots");
		}
		return lines;
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

	private static String titleCase(String value) {
		String[] parts = value.toLowerCase(Locale.ROOT).split("_");
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			if (!builder.isEmpty()) {
				builder.append(' ');
			}
			builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return builder.toString();
	}
}
