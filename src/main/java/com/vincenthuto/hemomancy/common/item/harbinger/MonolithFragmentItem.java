package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.MonolithFragmentItemRenderer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class MonolithFragmentItem extends Item implements HemoClientItemExtensionsProvider {

	public MonolithFragmentItem(Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (level.isClientSide || !(entity instanceof Player player)) {
			return;
		}
		if (level.getGameTime() % MonolithFragmentBurdenRules.BURDEN_CHECK_INTERVAL_TICKS != 0) {
			return;
		}
		HemoCapabilityAccess.getInitiatoryDegree(player)
				.filter(degree -> MonolithFragmentBurdenRules.shouldBurdenCarrier(degree.getDegreeNumber()))
				.ifPresent(degree -> applyApotheosBurden(player));
	}

	private static void applyApotheosBurden(Player player) {
		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
				MonolithFragmentBurdenRules.BURDEN_EFFECT_TICKS, 0, false, true, true));
		player.addEffect(new MobEffectInstance(MobEffects.CONFUSION,
				MonolithFragmentBurdenRules.BURDEN_EFFECT_TICKS, 0, false, true, true));
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new MonolithFragmentItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}
}
