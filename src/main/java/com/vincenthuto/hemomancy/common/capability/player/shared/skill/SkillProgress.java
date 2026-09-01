package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SkillProgress implements INBTSerializable<CompoundTag> {
	private final Map<Integer, SkillState> skills = new HashMap<>();
	private int skillPoints = 0;
	private Set<HemoMilestone> completedMilestones = EnumSet.noneOf(HemoMilestone.class);
	private int totalManipulationUses = 0;
	private int totalKillsWithBlood = 0;
	private int totalRitesCompleted = 0;
	private int totalHemoAdvancements = 0;

	public int getSkillPoints() {
		return skillPoints;
	}

	public void setSkillPoints(int skillPoints) {
		this.skillPoints = Math.max(0, skillPoints);
	}

	public void addSkillPoints(int amount) {
		setSkillPoints(skillPoints + amount);
	}

	public boolean spendSkillPoints(int amount) {
		if (amount <= 0) return true;
		if (skillPoints < amount) return false;
		skillPoints -= amount;
		return true;
	}

	public void refundSkillPoints(int amount) {
		if (amount > 0) addSkillPoints(amount);
	}

	public int getLevel(SkillPoint skill) {
		return skill == null ? 0 : stateFor(skill).level();
	}

	public EnumSkillStates getState(SkillPoint skill) {
		if (skill == null) return EnumSkillStates.LOCKED;
		return stateFor(skill).state();
	}

	public boolean isUnlocked(SkillPoint skill) {
		return getState(skill) == EnumSkillStates.UNLOCKED;
	}

	public boolean isEnabled(SkillPoint skill) {
		return skill != null && isUnlocked(skill) && (!skill.isToggleable() || stateFor(skill).enabled());
	}

	public boolean toggleEnabled(SkillPoint skill) {
		if (skill == null || !skill.isToggleable() || !isUnlocked(skill)) return false;
		SkillState current = stateFor(skill);
		skills.put(skill.getId(), new SkillState(current.state(), current.level(), !current.enabled()));
		return true;
	}

	public boolean isMaxed(SkillPoint skill) {
		return skill != null && getLevel(skill) >= skill.getMaxLevels();
	}

	public double getLevelUpCost(SkillPoint skill) {
		return skill == null ? 0 : skill.getCost() * (1.0 + 0.5 * getLevel(skill));
	}

	public void setSkill(SkillPoint skill, EnumSkillStates state, int level) {
		if (skill == null) return;
		SkillState old = skills.get(skill.getId());
		boolean enabled = old != null && old.state() == EnumSkillStates.UNLOCKED
				? old.enabled() : state == EnumSkillStates.UNLOCKED;
		skills.put(skill.getId(), new SkillState(state, clampLevel(skill, level), enabled));
	}

	public boolean setSkillLevel(SkillPoint skill, int level) {
		if (skill == null) return false;
		setSkill(skill, getState(skill), level);
		return true;
	}

	public boolean unlockOrLevel(SkillPoint skill) {
		if (skill == null || isMaxed(skill)) return false;
		if (getState(skill) == EnumSkillStates.LOCKED) {
			setSkill(skill, EnumSkillStates.UNLOCKED, 1);
			return true;
		}
		if (getState(skill) == EnumSkillStates.UNLOCKED) {
			setSkill(skill, EnumSkillStates.UNLOCKED, getLevel(skill) + 1);
			return true;
		}
		return false;
	}

	public boolean tryAwardMilestone(HemoMilestone milestone) {
		if (milestone == null || completedMilestones.contains(milestone)) return false;
		completedMilestones.add(milestone);
		addSkillPoints(milestone.getSkillPointReward());
		return true;
	}

	public boolean isMilestoneCompleted(HemoMilestone milestone) {
		return completedMilestones.contains(milestone);
	}

	public Set<HemoMilestone> getCompletedMilestones() {
		return EnumSet.copyOf(completedMilestones);
	}

	public int getCompletedMilestoneCount() {
		return completedMilestones.size();
	}

	public int incrementManipulationUses() {
		return ++totalManipulationUses;
	}

	public int incrementKillsWithBlood() {
		return ++totalKillsWithBlood;
	}

	public int incrementRitesCompleted() {
		return ++totalRitesCompleted;
	}

	public int incrementHemoAdvancements() {
		return ++totalHemoAdvancements;
	}

	public int getTotalManipulationUses() {
		return totalManipulationUses;
	}

	public int getTotalKillsWithBlood() {
		return totalKillsWithBlood;
	}

	public int getTotalRitesCompleted() {
		return totalRitesCompleted;
	}

	public int getTotalHemoAdvancements() {
		return totalHemoAdvancements;
	}

	public void reset() {
		skills.clear();
		skillPoints = 0;
		completedMilestones = EnumSet.noneOf(HemoMilestone.class);
		totalManipulationUses = 0;
		totalKillsWithBlood = 0;
		totalRitesCompleted = 0;
		totalHemoAdvancements = 0;
	}

	public ListTag toSyncTag() {
		ListTag list = new ListTag();
		for (SkillPoint skill : SkillPointInit.getAllSkills()) {
			CompoundTag entry = new CompoundTag();
			entry.putInt("id", skill.getId());
			entry.putString("name", skill.getName());
			entry.putString("state", getState(skill).name());
			entry.putInt("level", getLevel(skill));
			entry.putBoolean("enabled", isEnabled(skill));
			list.add(entry);
		}

		CompoundTag meta = new CompoundTag();
		meta.putString("name", "__meta__");
		meta.putInt("skillPoints", skillPoints);
		meta.putInt("totalManipulationUses", totalManipulationUses);
		meta.putInt("totalKillsWithBlood", totalKillsWithBlood);
		meta.putInt("totalRitesCompleted", totalRitesCompleted);
		meta.putInt("totalHemoAdvancements", totalHemoAdvancements);

		ListTag milestoneList = new ListTag();
		for (HemoMilestone milestone : completedMilestones) {
			milestoneList.add(StringTag.valueOf(milestone.getId()));
		}
		meta.put("completedMilestones", milestoneList);
		list.add(meta);
		return list;
	}

	public void readSyncTag(ListTag list) {
		reset();
		for (Tag tag : list) {
			if (!(tag instanceof CompoundTag entry)) continue;
			String name = entry.getString("name");
			if ("__meta__".equals(name)) {
				skillPoints = Math.max(0, entry.getInt("skillPoints"));
				totalManipulationUses = entry.getInt("totalManipulationUses");
				totalKillsWithBlood = entry.getInt("totalKillsWithBlood");
				totalRitesCompleted = entry.getInt("totalRitesCompleted");
				totalHemoAdvancements = entry.getInt("totalHemoAdvancements");
				ListTag milestoneList = entry.getList("completedMilestones", Tag.TAG_STRING);
				for (Tag milestoneTag : milestoneList) {
					HemoMilestone milestone = HemoMilestone.byId(milestoneTag.getAsString());
					if (milestone != null) completedMilestones.add(milestone);
				}
				continue;
			}
			SkillPoint skill = entry.contains("id")
					? SkillPointInit.getById(entry.getInt("id"))
					: SkillPointInit.getByName(name);
			if (skill == null) continue;
			EnumSkillStates state = parseState(entry.getString("state"), defaultState(skill));
			setSkill(skill, state, entry.getInt("level"));
			if (skill.isToggleable() && entry.contains("enabled") && !entry.getBoolean("enabled")) {
				toggleEnabled(skill);
			}
		}
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.put("skills", toSyncTag());
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		readSyncTag(tag.getList("skills", Tag.TAG_COMPOUND));
	}

	private SkillState stateFor(SkillPoint skill) {
		SkillState current = skills.computeIfAbsent(skill.getId(), id -> new SkillState(defaultState(skill), 0,
				defaultState(skill) == EnumSkillStates.UNLOCKED));
		if (current.state() == EnumSkillStates.UNLOCKED && current.level() == 0
				&& skill.getState() == EnumSkillStates.LOCKED) {
			current = new SkillState(EnumSkillStates.LOCKED, 0, false);
			skills.put(skill.getId(), current);
		}
		return current;
	}

	private static EnumSkillStates defaultState(SkillPoint skill) {
		return skill.getState();
	}

	private static EnumSkillStates parseState(String state, EnumSkillStates fallback) {
		try {
			return EnumSkillStates.valueOf(state);
		} catch (IllegalArgumentException ignored) {
			return fallback;
		}
	}

	private static int clampLevel(SkillPoint skill, int level) {
		return Math.max(0, Math.min(skill.getMaxLevels(), level));
	}

	private record SkillState(EnumSkillStates state, int level, boolean enabled) {}
}
