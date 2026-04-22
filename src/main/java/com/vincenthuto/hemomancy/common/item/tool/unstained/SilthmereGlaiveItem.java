package com.vincenthuto.hemomancy.common.item.tool.unstained;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Silthmere Glaive — a reach weapon for the Unstained path.
 * <p>
 * A pale silver glaive blessed in the memory of Silthmere. Its extended
 * reach lets the wielder keep blood-touched foes at a safe distance.
 * <p>
 * Passive (mainhand): removes Glowing from the wielder every 40 ticks,
 * reducing mob target-acquisition against them.
 * <p>
 * At ABSOLVED+ purity: killing a mob grants +0.5 purity.
 * This is handled by {@link SilthmereGlaiveEvents}.
 */
public class SilthmereGlaiveItem extends SwordItem {

	/** ResourceLocation ID for the reach attribute modifier — must be unique across the mod. */
	private static final net.minecraft.resources.ResourceLocation REACH_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hemomancy", "silthmere_glaive_reach");

	/** Extra entity reach granted by this weapon (in blocks). */
	public static final double REACH_BONUS = 1.5;
	private static final int MAX_HIT_TARGETS = 3;
	private static final int ADDITIONAL_CLEAVE_TARGETS = MAX_HIT_TARGETS - 1;
	private static final double CLEAVE_RANGE = 3.25D;
	private static final double CLEAVE_ARC_DOT_THRESHOLD = 0.5D; // 120-degree cone

	public SilthmereGlaiveItem(int attackDamage, float attackSpeed, Tier tier, Properties properties) {
		super(tier, attackDamage, attackSpeed, properties);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
		if (slot == EquipmentSlot.MAINHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(super.getDefaultAttributeModifiers(slot));
			builder.put(
					NeoForgeMod.ENTITY_REACH.get(),
					new AttributeModifier(REACH_ID,
							REACH_BONUS, AttributeModifier.Operation.ADD_VALUE));
			return builder.build();
		}
		return super.getDefaultAttributeModifiers(slot);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component.literal(
				"A pale silver glaive forged in Silthmere's memory. Its reach keeps corruption at arm's length.")
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

		return hit;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack)).withStyle(ChatFormatting.WHITE);
	}
}
