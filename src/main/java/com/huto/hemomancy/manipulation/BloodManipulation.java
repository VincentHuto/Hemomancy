package com.huto.hemomancy.manipulation;

import com.huto.hemomancy.capa.tendency.BloodTendencyProvider;
import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.tendency.IBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.hutoslib.client.TextUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.core.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class BloodManipulation extends ForgeRegistryEntry<BloodManipulation> {
	String name;
	double cost;
	double alignLevel;
	EnumBloodTendency tend;
	EnumManipulationRank rank;
	EnumVeinSections section;
	EnumManipulationType type;

	public BloodManipulation(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		this.name = name;
		this.cost = cost;
		this.alignLevel = alignLevel;
		this.type = type;
		this.tend = tendency;
		this.rank = rank;
		this.section = section;
	}

	public void performAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		IBloodTendency tendency = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
				.orElseThrow(NullPointerException::new);

		if (!player.world.isRemote) {
			if (volume.isActive()) {
				if (volume.getBloodVolume() > cost) {
					if (tendency.getAlignmentByTendency(tend) >= alignLevel) {
						volume.subtractBloodVolume((float) cost);
						getAction(player, world, heldItemMainhand, position);
					} else {
						player.sendStatusMessage(new StringTextComponent("Not Enough Alignment for Manipulation!")
								.mergeStyle(TextFormatting.RED), true);
					}
				} else {
					player.sendStatusMessage(
							new StringTextComponent("Not Enough Blood to be Shed!").mergeStyle(TextFormatting.RED),
							true);
				}
			} else {
				player.sendStatusMessage(new StringTextComponent("You strain your body but nothing happens!")
						.mergeStyle(TextFormatting.RED), true);
			}
		}
	}

	public void getAction(PlayerEntity player, World world, ItemStack heldItemMainhand, BlockPos position) {

	}

	/*
	 * Reads a NBT tag and converts it to a manipulation
	 */
	public static BloodManipulation deserialize(CompoundNBT nbt) {
		if (nbt != null && !nbt.isEmpty()) {
			if (nbt.contains("name") && nbt.contains("cost") && nbt.contains("level") && nbt.contains("type")
					&& nbt.contains("tendency") && nbt.contains("rank") && nbt.contains("section")) {
				BloodManipulation manip = new BloodManipulation(nbt.getString("name"), nbt.getDouble("cost"),
						nbt.getFloat("level"), EnumManipulationType.valueOf(nbt.getString("type")),
						EnumManipulationRank.valueOf(nbt.getString("rank")),
						EnumBloodTendency.valueOf(nbt.getString("tendency")),
						EnumVeinSections.valueOf(nbt.getString("section")));

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
		nbt.putDouble("level", alignLevel);
		nbt.putString("type", type.name());
		nbt.putString("rank", rank.name());
		nbt.putString("tendency", tend.name());
		nbt.putString("section", section.name());
		return nbt;
	}

	public String getName() {
		return name;
	}

	public String getProperName() {
		return TextUtils.convertInitToLang(name);
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

	public double getAlignLevel() {
		return alignLevel;
	}

	public void setAlignLevel(float alignLevel) {
		this.alignLevel = alignLevel;
	}

	public EnumManipulationType getType() {
		return type;
	}

	public void setType(EnumManipulationType type) {
		this.type = type;
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
