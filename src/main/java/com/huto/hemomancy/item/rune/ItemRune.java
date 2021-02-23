package com.huto.hemomancy.item.rune;

import java.util.List;

import com.huto.hemomancy.capabilities.mindrune.IRune;
import com.huto.hemomancy.capabilities.mindrune.RuneType;
import com.huto.hemomancy.capabilities.tendency.BloodTendencyProvider;
import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;
import com.huto.hemomancy.capabilities.tendency.IBloodTendency;
import com.huto.hemomancy.font.ModTextFormatting;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.BloodTendencyPacketServer;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemRune extends Item implements IRune {

	EnumBloodTendency assignedTendency;
	float deepenAmount;

	public ItemRune(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties);
		this.assignedTendency = tendencyIn;
		this.deepenAmount = deepenAmountIn;
	}

	@Override
	public void onEquipped(LivingEntity player) {
		if (player instanceof PlayerEntity) {
			if (!player.getEntityWorld().isRemote) {
				IBloodTendency coven = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				if (coven != null) {
					coven.setTendencyTendency(getAssignedTendency(), getDeepenAmount());
					PlayerEntity playerEnt = (PlayerEntity) player;
					PacketHandler.CHANNELBLOODTENDENCY.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerEnt),
							new BloodTendencyPacketServer(coven.getTendency()));
				}
			}
		}
	}

	@Override
	public void onUnequipped(LivingEntity player) {
		if (player instanceof PlayerEntity) {
			if (!player.getEntityWorld().isRemote) {
				IBloodTendency coven = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				if (coven != null) {
					coven.setTendencyTendency(getAssignedTendency(), -getDeepenAmount());
					PlayerEntity playerEnt = (PlayerEntity) player;
					PacketHandler.CHANNELBLOODTENDENCY.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerEnt),
							new BloodTendencyPacketServer(coven.getTendency()));
				}
			}
		}

	}

	public float getDeepenAmount() {
		return deepenAmount;
	}

	public void setDeepenAmount(int deepenAmount) {
		this.deepenAmount = deepenAmount;
	}

	public EnumBloodTendency getAssignedTendency() {
		return assignedTendency;
	}

	public void setAssignedTendency(EnumBloodTendency assignedTendency) {
		this.assignedTendency = assignedTendency;
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent(
				TextFormatting.GOLD + "Tendency: " + ModTextFormatting.toProperCase(assignedTendency.name())));
		tooltip.add(new StringTextComponent(TextFormatting.GREEN + "Tendency Amount: " + deepenAmount));

	}

	@Override
	public RuneType getRuneType() {
		return RuneType.RUNE;
	}

}
