package com.vincenthuto.hemomancy.common.item.tool.unstained;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

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
public class SilthmereGlaiveItem extends DiggerItem {

	/** UUID for the reach attribute modifier — must be unique across the mod. */
	public static final UUID REACH_UUID = UUID.fromString("9a8cc52e-47d0-4b6c-8c3e-4f4e4a7d9c01");

	/** Extra attack range granted by this weapon (in blocks). */
	public static final double REACH_BONUS = 1.5;

	public SilthmereGlaiveItem(float attackDamage, float attackSpeed, Tier tier, Properties properties) {
		super(attackDamage, attackSpeed, tier, BlockTags.MINEABLE_WITH_SHOVEL, properties);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
		if (slot == EquipmentSlot.MAINHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(super.getDefaultAttributeModifiers(slot));
			builder.put(
					ForgeMod.ATTACK_RANGE.get(),
					new AttributeModifier(REACH_UUID, "Glaive reach bonus",
							REACH_BONUS, AttributeModifier.Operation.ADDITION));
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
		tooltip.add(Component.literal("Passive: removes Glowing from the wielder.").withStyle(ChatFormatting.WHITE));
		tooltip.add(Component.literal("ABSOLVED+: killing a mob grants +0.5 purity.").withStyle(ChatFormatting.AQUA));
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack)).withStyle(ChatFormatting.WHITE);
	}
}
