package com.huto.hemomancy.manipulation;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.capabilities.tendency.BloodTendencyProvider;
import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;
import com.huto.hemomancy.capabilities.tendency.IBloodTendency;
import com.huto.hemomancy.capabilities.vascularsystem.EnumVeinSections;
import com.huto.hemomancy.font.ModTextFormatting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class BloodManipulation {
	String name;
	double cost;
	float alignLevel;
	EnumBloodTendency tend;
	EnumManipulationRank rank;
	EnumVeinSections section;

	public BloodManipulation(String name, double cost, float alignLevel, EnumManipulationRank rank,
			EnumBloodTendency tendency, EnumVeinSections section) {
		this.name = name;
		this.cost = cost;
		this.alignLevel = alignLevel;
		this.tend = tendency;
		this.rank = rank;
		this.section = section;
	}

	public void use(PlayerEntity player) {
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		IBloodTendency tendency = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
				.orElseThrow(NullPointerException::new);
		if (!player.world.isRemote) {
			if (volume.getBloodVolume() > cost && tendency.getAlignmentByTendency(tend) > alignLevel) {
				performAction(player, (ServerWorld) player.world, player.getHeldItemMainhand(), player.getPosition());
			}
		}
	}

	public void performAction(PlayerEntity player, ServerWorld world, ItemStack heldItemMainhand, BlockPos position) {
		System.out.println("Performing Base Manipulation, Someone forgot to override...cough vince cough");
	}

	/*
	 * Reads a NBT tag and converts it to a manipulation
	 */
	public static BloodManipulation deserialize(CompoundNBT nbt) {
		if (nbt != null && !nbt.isEmpty()) {
			if (nbt.contains("name") && nbt.contains("cost") && nbt.contains("level") && nbt.contains("tendency")
					&& nbt.contains("rank") && nbt.contains("section")) {

				BloodManipulation manip = new BloodManipulation(nbt.getString("name"), nbt.getDouble("cost"),
						nbt.getFloat("level"), EnumManipulationRank.valueOf(nbt.getString("rank")),
						EnumBloodTendency.valueOf(nbt.getString("tendency")),
						EnumVeinSections.valueOf(nbt.getString("section")));

				System.out.println(manip.getRank());

				return manip;
			}
		}
		return null;
	}

	/*
	 * Writes a NBT tag from this manipulation
	 */
	public CompoundNBT serialize() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("name", name);
		nbt.putDouble("cost", cost);
		nbt.putFloat("level", alignLevel);
		nbt.putString("rank", rank.name());
		nbt.putString("tendency", tend.name());
		nbt.putString("section", section.name());

		return nbt;
	}

	public String getName() {
		return name;
	}

	public String getProperName() {
		return ModTextFormatting.convertInitToLang(name);
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public float getAlignLevel() {
		return alignLevel;
	}

	public void setAlignLevel(float alignLevel) {
		this.alignLevel = alignLevel;
	}

	public EnumBloodTendency getTend() {
		return tend;
	}

	public void setTend(EnumBloodTendency tend) {
		this.tend = tend;
	}

	public EnumManipulationRank getRank() {
		return rank;
	}

	public void setRank(EnumManipulationRank rank) {
		this.rank = rank;
	}

	public EnumVeinSections getSection() {
		return section;
	}

	public void setSection(EnumVeinSections section) {
		this.section = section;
	}

}
