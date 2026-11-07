package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.effect.MorphicTonicRules;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.world.FungalGardensPlaces;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;

/**
 * A Bloody Jug steeped in morphic nectar: purges every harmful effect, then mends flesh and blood together. Only an
 * Apotheos drinks it, and only in the Gardens ({@link MorphicTonicRules}).
 */
public class MorphicTonicItem extends Item {

	public MorphicTonicItem(Item.Properties properties) {
		super(properties.stacksTo(16));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("tooltip.hemomancy.morphic_tonic"));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (entity instanceof ServerPlayer serverPlayer) CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
		if (!level.isClientSide) {
			entity.getActiveEffects().stream()
					.filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
					.map(MobEffectInstance::getEffect).toList()
					.forEach(entity::removeEffect);
			entity.addEffect(new MobEffectInstance(EffectInit.morphic_mending, MorphicTonicRules.MENDING_TICKS, 0));
		}
		if (entity instanceof Player player) {
			player.awardStat(Stats.ITEM_USED.get(this));
			if (!player.getAbilities().instabuild) stack.shrink(1);
		}
		level.gameEvent(entity, GameEvent.DRINK, entity.getEyePosition());
		return stack;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 16;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		MorphicTonicRules.Refusal refusal = MorphicTonicRules.refusal(ApotheosBodyEvents.isApotheos(player),
				FungalGardensPlaces.isInGardens(level, player.blockPosition()));
		if (refusal != MorphicTonicRules.Refusal.NONE) {
			if (!level.isClientSide) {
				player.displayClientMessage(Component.translatable(refusal == MorphicTonicRules.Refusal.NOT_APOTHEOS
						? "message.hemomancy.morphic_tonic.not_apotheos"
						: "message.hemomancy.morphic_tonic.outside_gardens")
						.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			}
			return InteractionResultHolder.fail(player.getItemInHand(hand));
		}
		return ItemUtils.startUsingInstantly(level, player, hand);
	}
}
