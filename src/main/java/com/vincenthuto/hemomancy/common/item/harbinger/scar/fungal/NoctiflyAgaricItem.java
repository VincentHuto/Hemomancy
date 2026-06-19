package com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class NoctiflyAgaricItem extends ItemFungalScar {

	public NoctiflyAgaricItem(Properties properties, DeferredHolder<ScarDefinition, ScarDefinition> scarDefinition) {
		super(properties, scarDefinition);
	}

	@Override
	public void onWornTick(LivingEntity player) {
		super.onWornTick(player);
	}

	@Override
	public void onEquipped(LivingEntity player) {
		super.onEquipped(player);
		player.addEffect(new MobEffectInstance(EffectInit.fungal_elytra, -1, 0, true, true));

	}

	@Override
	public void onUnequipped(LivingEntity player) {
		super.onUnequipped(player);
		player.removeEffect(EffectInit.fungal_elytra);
	}


	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal("Wings of pale mycelium. Less like flying — more like being carried.").withStyle(ChatFormatting.ITALIC));
	}

}
