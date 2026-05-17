package com.vincenthuto.hemomancy.common.item.unstained.tool;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.unstained.UnstainedWeaponItemRenderer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.unstained.PaleHumorFlaskItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Absolution Dagger — a light, fast Unstained weapon.
 * <p>
 * The blade is coated in a hyper coagulant that seems to halt any blood from
 * being spilled, making it the perfect tool for those who walk the path of
 * purification yet still need to defend themselves.
 * <p>
 * On hit: applies Weakness I for 2 seconds, briefly suppressing blood-magic output.
 * At CLEANSING+ purity: every 10th hit strips one random beneficial effect from
 * the target (a small cleanse).
 */
public class AbsolutionDaggerItem extends SwordItem implements HemoClientItemExtensionsProvider {

	private static final String TAG_HIT_COUNT = "absolution_hit_count";

	/** Duration of Weakness in ticks (2 seconds). */
	private static final int WEAKNESS_DURATION = 40;

	/** How many hits before the CLEANSING+ cleanse triggers. */
	private static final int CLEANSE_HIT_THRESHOLD = 10;
	private static final float MAX_EXECUTION_BONUS_DAMAGE = 5.0F;

	public AbsolutionDaggerItem(Tier tier, int attackDamageIn, float attackSpeedIn, Properties properties) {
		super(tier, properties.attributes(SwordItem.createAttributes(tier, attackDamageIn, attackSpeedIn)));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal(
				"Coated in a hyper coagulant that seems to halt any blood from being spilled.")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("On hit: Weakness I (2s)")
				.withStyle(ChatFormatting.WHITE));
		tooltip.add(Component.literal("Deals increased damage to low-health enemies.")
				.withStyle(ChatFormatting.WHITE));
		tooltip.add(Component.literal("CLEANSING+: Every 10th hit strips a random beneficial effect.")
				.withStyle(ChatFormatting.AQUA));
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		boolean hit = super.hurtEnemy(stack, target, attacker);
		if (!hit) {
			return false;
		}

		if (!attacker.level().isClientSide && attacker instanceof Player player) {
			// Always apply Weakness I for 2 seconds
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION, 0, false, true, true));

			// Apply hemolysis if coated with white humor
			CompoundTag coatTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (coatTag.getBoolean(PaleHumorFlaskItem.TAG_WHITE_HUMOR_COATED)) {
				target.addEffect(new MobEffectInstance(EffectInit.hemolysis, 120, 0, false, true, true));
			}
			float maxHealth = target.getMaxHealth();
			if (maxHealth > 0.0F) {
				float missingHealthRatio = 1.0F - (target.getHealth() / maxHealth);
				float executeBonusDamage = Math.max(0.0F, missingHealthRatio) * MAX_EXECUTION_BONUS_DAMAGE;
				if (executeBonusDamage > 0.0F) {
					target.hurt(attacker.damageSources().playerAttack(player), executeBonusDamage);
				}
			}

			// At CLEANSING+ purity: every 10th hit strips a random positive effect
			HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
				if (EnumPurityStage.byPurity(progress.getPurity()).getLevel()
						>= EnumPurityStage.CLEANSING.getLevel()) {
					CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
					int hits = tag.getInt(TAG_HIT_COUNT) + 1;
					if (hits >= CLEANSE_HIT_THRESHOLD) {
						hits = 0;
						List<MobEffectInstance> beneficial = target.getActiveEffects().stream()
								.filter(e -> e.getEffect().value().isBeneficial())
								.collect(Collectors.toList());
						if (!beneficial.isEmpty()) {
							MobEffectInstance toRemove = beneficial.get(
									player.getRandom().nextInt(beneficial.size()));
							target.removeEffect(toRemove.getEffect());
							player.displayClientMessage(
									Component.literal("The coagulant purges a corruption from your foe.")
											.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
									true);
						}
					}
					tag.putInt(TAG_HIT_COUNT, hits);
					stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
				}
			});
		}
		return true;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack)).withStyle(ChatFormatting.WHITE);
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new UnstainedWeaponItemRenderer(
					UnstainedWeaponItemRenderer.Kind.DAGGER, null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}
}
