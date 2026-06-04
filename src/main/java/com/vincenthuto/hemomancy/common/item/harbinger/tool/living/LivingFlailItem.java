package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.hematic.LivingFlailItemRenderer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class LivingFlailItem extends LivingToolItem implements HemoClientItemExtensionsProvider {
	public LivingFlailItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.9f, EnumBloodTendency.CONGEATIO, tier, builderIn);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (LivingStaffWeaponFormHelper.wasRestoredOutOfHand(stack, attacker)) {
			return true;
		}
		target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
		return true;
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return RenderPropLivingFlail.INSTANCE;
	}
}

class RenderPropLivingFlail implements IClientItemExtensions {
	public static final RenderPropLivingFlail INSTANCE = new RenderPropLivingFlail();

	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new LivingFlailItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}
