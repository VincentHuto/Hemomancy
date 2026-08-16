package com.vincenthuto.hemomancy.common.item.unstained.tool;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.unstained.UnstainedWarhammerItemRenderer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.unstained.PaleHumorFlaskItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

/**
 * Unstained Warhammer — a blunt, heavy weapon for followers of the Path of the
 * Unstained. Deals high base damage with strong knockback but never draws blood,
 * so it does not aid opponents who use hemomancy. Damage scales with the
 * wielder's purity level.
 */
public class UnstainedWarhammerItem extends DiggerItem implements HemoClientItemExtensionsProvider {
	private static final int CRIPPLE_DURATION = 100;
	private static final int CRIPPLE_AMPLIFIER = 0;

	public UnstainedWarhammerItem(float attackDamage, float attackSpeed, Tier tier, Properties properties) {
		super(tier, BlockTags.MINEABLE_WITH_PICKAXE,
				properties.attributes(DiggerItem.createAttributes(tier, attackDamage, attackSpeed)));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal(
				"A bell-headed pale silver hammer for breaking blood-borne corruption without drawing blood.")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("On hit: cripples enemies with Slowness and Weakness.")
				.withStyle(ChatFormatting.WHITE));
		tooltip.add(Component.literal("Heavy impact: knocks enemies back, scaling with purity.")
				.withStyle(ChatFormatting.WHITE));
		tooltip.add(Component.literal("When coated with White Humor: applies Hemolysis on hit.")
				.withStyle(ChatFormatting.AQUA));
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker instanceof Player player && !player.level().isClientSide) {
			// Apply knockback — warhammers are blunt impact weapons
			HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
				// Extra knockback at higher purity
				double bonus = progress.getPurity() / 100.0 * 0.4;
				double total = 0.8 + bonus;
				double dx = player.getX() - target.getX();
				double dz = player.getZ() - target.getZ();
				double dist = Math.sqrt(dx * dx + dz * dz);
				if (dist > 0.01) {
					target.push((-dx / dist) * total, 0.25, (-dz / dist) * total);
				}
			});
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, CRIPPLE_DURATION, CRIPPLE_AMPLIFIER,
					false, true, true));
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, CRIPPLE_DURATION, CRIPPLE_AMPLIFIER,
					false, true, true));

			if (PaleHumorFlaskItem.consumeCoatingHit(stack)) {
				target.addEffect(new MobEffectInstance(EffectInit.hemolysis, 120, 0, false, true, true));
			}

			player.level().playSound(null, target.blockPosition(),
					SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.4f, 1.2f);
		}

		return super.hurtEnemy(stack, target, attacker);
	}

	/**
	 * Purity-scaling damage bonus: up to +4 additional damage at full purity.
	 */
	public static float getPurityDamageBonus(Player player) {
		return HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> progress.getPurity() / 100.0f * 4.0f)
				.orElse(0.0f);
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack))
				.withStyle(ChatFormatting.WHITE);
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new UnstainedWarhammerItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}
}
