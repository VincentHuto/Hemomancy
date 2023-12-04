package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class BloodManipulation  {
	public static BloodManipulation BLANK = new BloodManipulation("No Selected", 0, 0, 0, EnumManipulationType.QUICK,
			EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD);
	/*
	 * Reads a NBT tag and converts it to a manipulation
	 */
	public static BloodManipulation deserialize(CompoundTag nbt) {
		if (nbt != null && !nbt.isEmpty()) {
			if (nbt.contains("name") && nbt.contains("cost") && nbt.contains("level") && nbt.contains("type")
					&& nbt.contains("tendency") && nbt.contains("rank") && nbt.contains("section")) {
				BloodManipulation manip = new BloodManipulation(nbt.getString("name"), nbt.getDouble("cost"),
						nbt.getDouble("level"), nbt.getDouble("xpcost"),
						EnumManipulationType.valueOf(nbt.getString("type")),
						EnumManipulationRank.valueOf(nbt.getString("rank")),
						EnumBloodTendency.valueOf(nbt.getString("tendency")),
						EnumVeinSections.valueOf(nbt.getString("section")));

				return manip;
			}
		}
		return null;
	}
	String name;
	double cost, alignLevel, xpCost;
	EnumBloodTendency tend;
	EnumManipulationRank rank;
	EnumVeinSections section;

	EnumManipulationType type;

	public BloodManipulation(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		this.name = name;
		this.cost = cost;
		this.alignLevel = alignLevel;
		this.type = type;
		this.tend = tendency;
		this.rank = rank;
		this.section = section;
	}

	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {

	}

	public double getAlignLevel() {
		return alignLevel;
	}

	public double getCost() {
		return cost;
	}

	public String getName() {
		return name;
	}

	public String getProperName() {
		return HLTextUtils.convertInitToLang(name);
	}

	public EnumManipulationRank getRank() {
		return rank;
	}

	public EnumVeinSections getSection() {
		return section;
	}

	public EnumBloodTendency getTend() {
		return tend;
	}

	public EnumManipulationType getType() {
		return type;
	}

	public double getXpCost() {
		return xpCost;
	}

	public void performAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		IBloodTendency tendency = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
				.orElseThrow(NullPointerException::new);

		if (!player.level().isClientSide) {
			if (volume.isActive()) {
				if (volume.getBloodVolume() > cost) {
					if (tendency.getAlignmentByTendency(tend) >= alignLevel) {
						volume.drain(cost);
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
								new BloodVolumeServerPacket(volume));
						getAction(player, world, heldItemMainhand, position);
					} else {
						player.displayClientMessage(Component.translatable("Not Enough Alignment for Manipulation!")
								.withStyle(ChatFormatting.RED), true);
					}
				} else {
					player.displayClientMessage(
							Component.translatable("Not Enough Blood to be Shed!").withStyle(ChatFormatting.RED), true);
				}
			} else {
				player.displayClientMessage(Component.translatable("You strain your body but nothing happens!")
						.withStyle(ChatFormatting.RED), true);
			}
		}
	}

	/*
	 * Writes a NBT tag from this manipulation
	 */
	public CompoundTag serialize() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("name", name);
		nbt.putDouble("cost", cost);
		nbt.putDouble("level", alignLevel);
		nbt.putDouble("xpcost", xpCost);
		nbt.putString("type", type.name());
		nbt.putString("rank", rank.name());
		nbt.putString("tendency", tend.name());
		nbt.putString("section", section.name());
		return nbt;
	}

	public void setAlignLevel(float alignLevel) {
		this.alignLevel = alignLevel;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRank(EnumManipulationRank rank) {
		this.rank = rank;
	}

	public void setSection(EnumVeinSections section) {
		this.section = section;
	}

	public void setTend(EnumBloodTendency tend) {
		this.tend = tend;
	}

	public void setType(EnumManipulationType type) {
		this.type = type;
	}

	public void setXpCost(double xpCost) {
		this.xpCost = xpCost;
	}

	@Override
	public String toString() {
		return HLTextUtils.convertInitToLang(name);
	}

}