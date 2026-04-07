package com.vincenthuto.hemomancy.common.init;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPoint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class SkillPointInit {

	public static List<List<SkillPoint>> SKILL_TREE = new ArrayList<>();
	public static List<SkillPoint> BASE = new ArrayList<>();
	/** Available skill-point currency earned from using blood manipulations. */
	public static int skillPoints = 0;
	public static SkillPoint base_skill, skill_capacity, skill_efficiency, skill_last_wind, skill_dynamic_use,
			skill_feeding_frenzy, skill_hemostasis, skill_sanguine_surge, skill_crimson_mastery,
			skill_vital_link, skill_iron_will, skill_blood_flow, skill_coagulation, skill_sanguine_reach;

	public static void init() {
		initBaseBranch();

	}

	public static void initBaseBranch() {
		// SkillPoint(id, name, bloodCostPerLevel, maxLevels, state, parent)
		base_skill = registerSkill(BASE, new SkillPoint(0, "base", 0, 1, EnumSkillStates.UNLOCKED, null));

		skill_capacity = registerSkill(BASE,
				new SkillPoint(1, "skill_capacity", 100, 5, EnumSkillStates.LOCKED, base_skill));
		skill_efficiency = registerSkill(BASE,
				new SkillPoint(2, "skill_efficiency", 100, 5, EnumSkillStates.LOCKED, base_skill));
		skill_last_wind = registerSkill(BASE,
				new SkillPoint(3, "skill_last_wind", 300, 3, EnumSkillStates.LOCKED, skill_capacity)
						.setSkillPointCost(2).setRequiredDegree(2));
		skill_dynamic_use = registerSkill(BASE,
				new SkillPoint(4, "skill_dynamic_use", 300, 3, EnumSkillStates.LOCKED, skill_efficiency)
						.setSkillPointCost(2).setRequiredDegree(2));
		skill_feeding_frenzy = registerSkill(BASE,
				new SkillPoint(5, "skill_feeding_frenzy", 500, 3, EnumSkillStates.LOCKED, skill_last_wind)
						.setSkillPointCost(3).setRequiredDegree(3));

		// ── New skills ──
		skill_hemostasis = registerSkill(BASE,
				new SkillPoint(6, "skill_hemostasis", 200, 3, EnumSkillStates.LOCKED, skill_efficiency)
						.setSkillPointCost(2).setRequiredDegree(2));
		skill_sanguine_surge = registerSkill(BASE,
				new SkillPoint(7, "skill_sanguine_surge", 200, 3, EnumSkillStates.LOCKED, skill_capacity)
						.setSkillPointCost(2).setRequiredDegree(2));
		skill_crimson_mastery = registerSkill(BASE,
				new SkillPoint(8, "skill_crimson_mastery", 400, 3, EnumSkillStates.LOCKED, skill_dynamic_use)
						.setSkillPointCost(3).setRequiredDegree(4));
		skill_vital_link = registerSkill(BASE,
				new SkillPoint(9, "skill_vital_link", 600, 3, EnumSkillStates.LOCKED, skill_feeding_frenzy)
						.setSkillPointCost(4).setRequiredDegree(5));
		skill_iron_will = registerSkill(BASE,
				new SkillPoint(10, "skill_iron_will", 400, 3, EnumSkillStates.LOCKED, skill_last_wind)
						.setSkillPointCost(3).setRequiredDegree(3));
		skill_blood_flow = registerSkill(BASE,
				new SkillPoint(11, "skill_blood_flow", 300, 5, EnumSkillStates.LOCKED, skill_hemostasis)
						.setSkillPointCost(2).setRequiredDegree(3));
		skill_coagulation = registerSkill(BASE,
				new SkillPoint(12, "skill_coagulation", 500, 3, EnumSkillStates.LOCKED, skill_hemostasis)
						.setSkillPointCost(3).setRequiredDegree(4));
		skill_sanguine_reach = registerSkill(BASE,
				new SkillPoint(13, "skill_sanguine_reach", 400, 3, EnumSkillStates.LOCKED, skill_crimson_mastery)
						.setSkillPointCost(3).setRequiredDegree(5));

		registerSkillBranch(BASE);
	}

	// Adds the skill to the selected Branch
	public static SkillPoint registerSkill(List<SkillPoint> branch, SkillPoint manip) {
		branch.add(manip);
		return manip;
	}

	// Adds branch to the greater skill tree
	public static List<SkillPoint> registerSkillBranch(List<SkillPoint> branch) {
		SKILL_TREE.add(branch);
		return branch;
	}

	/** Get a flat list of every registered skill */
	public static List<SkillPoint> getAllSkills() {
		List<SkillPoint> all = new ArrayList<>();
		for (List<SkillPoint> branch : SKILL_TREE) {
			all.addAll(branch);
		}
		return all;
	}

	/** Lookup a skill by its integer ID, or null if not found */
	public static SkillPoint getById(int id) {
		for (List<SkillPoint> branch : SKILL_TREE) {
			for (SkillPoint sp : branch) {
				if (sp.getId() == id) return sp;
			}
		}
		return null;
	}

	/** Serialize every skill's state and level into a ListTag */
	public static ListTag serializeAll() {
		ListTag list = new ListTag();
		for (SkillPoint sp : getAllSkills()) {
			list.add(sp.serialize());
		}
		// Append a meta entry for the skill-point currency
		CompoundTag meta = new CompoundTag();
		meta.putString("name", "__meta__");
		meta.putInt("skillPoints", skillPoints);
		list.add(meta);
		return list;
	}

	/** Restore skill states/levels from a previously serialized ListTag */
	public static void deserializeAll(ListTag list) {
		for (Tag tag : list) {
			if (tag instanceof CompoundTag entry) {
				String name = entry.getString("name");
				if ("__meta__".equals(name)) {
					if (entry.contains("skillPoints")) {
						skillPoints = entry.getInt("skillPoints");
					}
					continue;
				}
				for (SkillPoint sp : getAllSkills()) {
					if (sp.getName().equals(name)) {
						sp.deserialize(entry);
						break;
					}
				}
			}
		}
	}

}
