package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public final class SanguineFormationItem extends Item {
	public SanguineFormationItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (!context.getLevel().isClientSide
				&& context.getPlayer() instanceof ServerPlayer player) {
			CardinalRiteActivationRules.ActivationAttempt attempt =
					BloodCraftingKeyPressPacket.tryStartCardinalRite(
							player, context.getClickedPos(),
							CardinalRiteActivationRules.Trigger.SANGUINE_FORMATION_BLOCK_USE);
			if (attempt.shouldConsumeActivator(player.getAbilities().instabuild)) {
				context.getItemInHand().shrink(1);
			}
			if (attempt.handled()) return InteractionResult.SUCCESS;
		}
		return super.useOn(context);
	}
}
