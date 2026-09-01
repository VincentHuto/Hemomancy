package com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.HematicMemoryExpression;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarNoeticRoutingRules;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class VascularDiagnosisRules {
	private VascularDiagnosisRules() {
	}

	public static Diagnosis diagnose(Map<EnumVeinSections, Float> vascularSystem, List<MemoryRoute> routes) {
		EnumVeinSections worst = EnumVeinSections.HEAD;
		float worstHealth = 100F;
		int belowStable = 0;
		int damaged = 0;
		int varicose = 0;
		boolean critical = false;
		for (EnumVeinSections section : EnumVeinSections.values()) {
			float health = vascularSystem.getOrDefault(section, 100F);
			if (health < worstHealth) {
				worst = section;
				worstHealth = health;
			}
			EnumBloodFlow flow = flow(health);
			if (health < 100F) damaged++;
			if (health < 50F) belowStable++;
			if (flow == EnumBloodFlow.VARICOSE) varicose++;
			if (flow == EnumBloodFlow.ClOTTED || flow == EnumBloodFlow.DEAD) critical = true;
		}

		Treatment treatment = critical || varicose >= 3 ? Treatment.VASCULAR_MENDING
				: belowStable >= 2 ? Treatment.VASCULAR_POULTICE
				: belowStable == 1 ? Treatment.SANGUINE_SALVE
				: Treatment.REST_AND_FOOD;
		EnumMap<EnumVeinSections, List<MemoryRoute>> bySection = new EnumMap<>(EnumVeinSections.class);
		for (EnumVeinSections section : EnumVeinSections.values()) bySection.put(section, new ArrayList<>());
		for (MemoryRoute route : routes) bySection.get(route.section()).add(route);
		bySection.replaceAll((section, values) -> List.copyOf(values));
		return new Diagnosis(worst, worstHealth, flow(worstHealth), damaged, Map.copyOf(bySection), treatment);
	}

	public static Diagnosis diagnosePlayer(Player player) {
		List<ScarDefinition> active = new ArrayList<>();
		HemoCapabilityAccess.getScarState(player).ifPresent(scars -> scars.forEachActiveCerebralScar(active::add));
		List<MemoryRoute> routes = new ArrayList<>();
		HemoCapabilityAccess.getKnownManipulations(player).ifPresent(known -> known.getEquippedMemoryRefs().forEach(ref -> {
			if (ref.expression() == HematicMemoryExpression.THELEMIC) {
				ref.muscleMemory().ifPresent(memory -> routes.add(new MemoryRoute(memory.section(),
						HematicMemoryExpression.THELEMIC, memory.id(), 0)));
			} else {
				known.getKnownManips().keySet().stream().filter(manip -> manip.getName().equals(ref.id())).findFirst()
						.ifPresent(manip -> routes.add(new MemoryRoute(manip.getSection(), HematicMemoryExpression.NOETIC,
								manip.getName(), ScarNoeticRoutingRules.bestMatchingTier(manip.getTend(), active))));
			}
		}));
		Map<EnumVeinSections, Float> system = HemoCapabilityAccess.getVascularSystem(player)
				.map(IVascularSystem::getVascularSystem).orElseGet(Map::of);
		return diagnose(system, routes);
	}

	private static EnumBloodFlow flow(float health) {
		if (health >= 90F) return EnumBloodFlow.RAGING;
		if (health >= 75F) return EnumBloodFlow.FLOWING;
		if (health >= 50F) return EnumBloodFlow.STABLE;
		if (health >= 15F) return EnumBloodFlow.VARICOSE;
		if (health > 0F) return EnumBloodFlow.ClOTTED;
		return EnumBloodFlow.DEAD;
	}

	public enum Treatment {
		REST_AND_FOOD,
		SANGUINE_SALVE,
		VASCULAR_POULTICE,
		VASCULAR_MENDING
	}

	public record MemoryRoute(EnumVeinSections section, HematicMemoryExpression expression, String name,
			int matchingScarTier) {
	}

	public record Diagnosis(EnumVeinSections worstSection, float health, EnumBloodFlow flow, int damagedSections,
			Map<EnumVeinSections, List<MemoryRoute>> routes, Treatment treatment) {
	}
}
