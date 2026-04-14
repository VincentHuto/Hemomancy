package com.vincenthuto.hemomancy.common.item.scar.functional;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.scar.ItemFungalScar;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class NoctiflyAgaricItem extends ItemFungalScar {

	public NoctiflyAgaricItem(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties, tendencyIn, deepenAmountIn);
	}

	@Override
	public void onWornTick(LivingEntity player) {
		super.onWornTick(player);
	}

	@Override
	public void onEquipped(LivingEntity player) {
		super.onEquipped(player);
		player.addEffect(new MobEffectInstance(EffectInit.fungal_elytra.get(), -1, 0, true, true));

	}

	@Override
	public void onUnequipped(LivingEntity player) {
		super.onUnequipped(player);
		player.removeEffect(EffectInit.fungal_elytra.get());
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
		return super.onBlockStartBreak(itemstack, pos, player);

	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component.literal("Wings of pale mycelium. Less like flying — more like being carried.").withStyle(ChatFormatting.ITALIC));
	}

}
