package com.vincenthuto.hemomancy.common.item.unstained.tool;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.unstained.SilthmereGlaiveItemRenderer;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.unstained.PaleHumorFlaskItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Silthmere Glaive — a reach weapon for the Unstained path.
 * <p>
 * A pale silver glaive blessed under Silthmere, the liturgical title of Our Lady. Its extended
 * reach lets the wielder keep blood-touched foes at a safe distance.
 * <p>
 * Passive (mainhand): removes Glowing from the wielder every 40 ticks,
 * reducing mob target-acquisition against them.
 * <p>
 * At ABSOLVED+ purity: killing a mob grants +0.5 purity.
 * This is handled by {@link SilthmereGlaiveEvents}.
 */
public class SilthmereGlaiveItem extends SwordItem implements HemoClientItemExtensionsProvider {

	/** Extra entity reach granted by this weapon (in blocks). */
	public static final double REACH_BONUS = 1.5;
	private static final int MAX_HIT_TARGETS = 3;
	private static final int ADDITIONAL_CLEAVE_TARGETS = MAX_HIT_TARGETS - 1;
	private static final double CLEAVE_RANGE = 3.25D;
	private static final double CLEAVE_ARC_DOT_THRESHOLD = 0.5D; // 120-degree cone

	public SilthmereGlaiveItem(int attackDamage, float attackSpeed, Tier tier, Properties properties) {
		super(tier, properties.attributes(SwordItem.createAttributes(tier, attackDamage, attackSpeed)));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal(
				"A pale silver glaive consecrated under Silthmere, Our Lady's title. Its reach keeps corruption at arm's length.")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("+1.5 Attack Range").withStyle(ChatFormatting.WHITE));
		tooltip.add(Component.literal("On hit: cleaves up to 2 additional enemies in front (3 total).")
				.withStyle(ChatFormatting.WHITE));
		tooltip.add(Component.literal("Passive: removes Glowing from the wielder.").withStyle(ChatFormatting.WHITE));
		tooltip.add(Component.literal("ABSOLVED+: killing a mob grants +0.5 purity.").withStyle(ChatFormatting.AQUA));
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		boolean hit = super.hurtEnemy(stack, target, attacker);
		if (!hit || attacker.level().isClientSide || ADDITIONAL_CLEAVE_TARGETS <= 0) {
			return hit;
		}

		DamageSource source = attacker instanceof Player player
				? attacker.damageSources().playerAttack(player)
				: attacker.damageSources().mobAttack(attacker);
		float cleaveDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
		if (cleaveDamage <= 0.0F) {
			return hit;
		}

		Vec3 look = attacker.getViewVector(1.0F).normalize();
		List<LivingEntity> cleaveTargets = attacker.level()
				.getEntitiesOfClass(LivingEntity.class, attacker.getBoundingBox().inflate(CLEAVE_RANGE),
						candidate -> candidate != attacker
								&& candidate != target
								&& candidate.isAlive()
								&& !attacker.isAlliedTo(candidate)
								&& attacker.canAttack(candidate))
				.stream()
				.filter(candidate -> {
					Vec3 toCandidate = candidate.position().subtract(attacker.position());
					if (toCandidate.lengthSqr() < 1.0E-6D) return false;
					return toCandidate.normalize().dot(look) >= CLEAVE_ARC_DOT_THRESHOLD;
				})
				.sorted((a, b) -> Double.compare(a.distanceToSqr(attacker), b.distanceToSqr(attacker)))
				.limit(ADDITIONAL_CLEAVE_TARGETS)
				.collect(Collectors.toList());

		for (LivingEntity cleaveTarget : cleaveTargets) {
			cleaveTarget.hurt(source, cleaveDamage);
		}

		// Apply hemolysis to primary target if coated
		if (PaleHumorFlaskItem.consumeCoatingHit(stack)) {
			target.addEffect(new MobEffectInstance(EffectInit.hemolysis, 120, 0, false, true, true));
		}

		return hit;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack)).withStyle(ChatFormatting.WHITE);
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new SilthmereGlaiveItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}
}
