package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.hematic.LivingTorchItemRenderer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.util.CrimsonFireHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class LivingTorchItem extends LivingToolItem implements HemoClientItemExtensionsProvider {
	public LivingTorchItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.1f, EnumBloodTendency.FLAMMEUS, tier, builderIn);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (LivingStaffWeaponFormHelper.wasRestoredOutOfHand(stack, attacker)) {
			return true;
		}
		CrimsonFireHelper.igniteCrimson(target, 4);
		if (!attacker.level().isClientSide) {
			attacker.level().playSound(null, target.blockPosition(), SoundEvents.FLINTANDSTEEL_USE,
					SoundSource.PLAYERS, 0.45f, 0.85f);
		}
		return true;
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return RenderPropLivingTorch.INSTANCE;
	}
}

class RenderPropLivingTorch implements IClientItemExtensions {
	public static final RenderPropLivingTorch INSTANCE = new RenderPropLivingTorch();

	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new LivingTorchItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}
