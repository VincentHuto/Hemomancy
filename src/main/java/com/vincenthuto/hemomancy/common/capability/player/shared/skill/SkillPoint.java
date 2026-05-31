package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.EnumNodeShape;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SkillPoint {
	int id, maxLevels;
	String name;
	double cost;           // Blood cost per level-up
	int skillPointCost;    // Skill-point currency cost per level-up
	int requiredDegree;    // Minimum initiatory degree required (0 = none)
	int treeX, treeY;      // Optional content-space coordinates for the skill tree screen
	boolean hasTreePosition;
	String branch = "core";
	EnumSkillStates state;
	SkillPoint parent;
	@Nullable Supplier<ItemStack> iconItem;
	@Nullable ResourceLocation iconTexture;
	EnumNodeShape nodeShape = EnumNodeShape.SQUARE;

	public SkillPoint(int id, String name, double cost, int maxLevel, EnumSkillStates state,
			@Nullable SkillPoint parent) {
		this.id = id;
		this.name = name;
		this.maxLevels = maxLevel;
		this.cost = cost;
		this.skillPointCost = 1;
		this.requiredDegree = 0;
		this.hasTreePosition = false;
		this.state = state;
		this.parent = parent;
	}

	// ── Getters ──

	public double getCost() {
		return cost;
	}

	public double getLevelUpCost(int level) {
		return cost * (1.0 + 0.5 * level);
	}

	public int getId() {
		return id;
	}

	public int getMaxLevels() {
		return maxLevels;
	}

	public int getSkillPointCost() {
		return skillPointCost;
	}

	/** Builder-style setter for the skill-point cost per level. */
	public SkillPoint setSkillPointCost(int cost) {
		this.skillPointCost = cost;
		return this;
	}

	public int getRequiredDegree() {
		return requiredDegree;
	}

	/** Builder-style setter for the minimum initiatory degree required to unlock this skill. */
	public SkillPoint setRequiredDegree(int degree) {
		this.requiredDegree = degree;
		return this;
	}

	/** Builder-style setter for the content-space position used by the skill tree screen. */
	public SkillPoint setTreePosition(int x, int y) {
		this.treeX = x;
		this.treeY = y;
		this.hasTreePosition = true;
		return this;
	}

	/** Builder-style setter for the skill tree branch this node belongs to. */
	public SkillPoint setBranch(String branch) {
		this.branch = branch;
		return this;
	}

	/** Builder-style setter for the icon item rendered inside this node on the skill tree. */
	public SkillPoint setIconItem(Supplier<ItemStack> icon) {
		this.iconItem = icon;
		return this;
	}

	/** Builder-style setter for a texture icon rendered inside this node on the skill tree. */
	public SkillPoint setIconTexture(ResourceLocation texture) {
		this.iconTexture = texture;
		return this;
	}

	/** Builder-style setter for the node shape on the skill tree. */
	public SkillPoint setNodeShape(EnumNodeShape shape) {
		this.nodeShape = shape;
		return this;
	}

	/**
	 * Returns the icon item to render inside this skill's node, or null if none is set.
	 */
	@Nullable
	public ItemStack getIconItem() {
		return iconItem != null ? iconItem.get() : null;
	}

	/**
	 * Returns the texture icon to render inside this skill's node, or null if none is set.
	 */
	@Nullable
	public ResourceLocation getIconTexture() {
		return iconTexture;
	}

	/** Returns the node shape for this skill. Defaults to SQUARE. */
	public EnumNodeShape getNodeShape() {
		return nodeShape;
	}

	public boolean hasTreePosition() {
		return hasTreePosition;
	}

	public int getTreeX() {
		return treeX;
	}

	public int getTreeY() {
		return treeY;
	}

	public String getBranch() {
		return branch;
	}

	/** Returns true if the player's degree is too low to interact with this skill. */
	public boolean isDegreeLocked(int playerDegree) {
		return requiredDegree > 0 && playerDegree < requiredDegree;
	}

	public boolean isMaxed(int level) {
		return level >= maxLevels;
	}

	public String getName() {
		return name;
	}

	public SkillPoint getParent() {
		return parent;
	}

	public EnumSkillStates getState() {
		return state;
	}

	// ── Level-up ──

	// ── Serialisation ──

	// ── Setters ──

	public void setCost(double cost) {
		this.cost = cost;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(SkillPoint parent) {
		this.parent = parent;
	}

}
