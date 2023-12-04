package com.vincenthuto.hemomancy.common.item;

import java.util.List;

import com.vincenthuto.hutoslib.client.HLClientUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class DSDItem extends Item {

	public DSDItem(Properties prop) {
		super(prop);
		prop.stacksTo(1);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component.literal("Also known as a D.S.D. used to"));
		tooltip.add(Component.literal("commandeer Drudges to your will."));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide) {
			Minecraft mc = Minecraft.getInstance();
			mc.cameraEntity.setPos(mc.cameraEntity.getPosition(HLClientUtils.getPartialTicks()).add(0, 2, 0));
		}
		return super.use(level, player, hand);
	}

}