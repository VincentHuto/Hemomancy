package com.vincenthuto.hemomancy.common.item.morphlings;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MorphlingItem extends Item implements IMorphling {

	public int bloodCost;

	public MorphlingItem(Properties prop) {
		super(prop);
		prop.stacksTo(1);
		bloodCost= 0;
	}
	public MorphlingItem(Properties prop, int bloodCostIn) {
		super(prop);
		prop.stacksTo(1);
		bloodCost= bloodCostIn;
	}

	@Override
	public int getBloodCost() {
		return 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			float power = stack.getTag().getFloat("EnzymePower");
			int feedings = stack.getTag().getInt("EnzymeFeedings");
			if (feedings > 0) {
				tooltip.add(Component.literal("Enzyme Power: " + String.format("%.1f", power))
						.withStyle(ChatFormatting.DARK_GREEN));
				tooltip.add(Component.literal("Feedings: " + feedings)
						.withStyle(ChatFormatting.GOLD));
			}
		}
	}

}
