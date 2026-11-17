package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;

/**
 * The Living Staff's ninth form, a pickaxe of hematic iron on a bone haft (core-loop Track 8). Learned with the bond,
 * so a new Harbinger mines iron ore with the Staff on the first day. It harvests like a stone tool at the bond (iron
 * ore included), like iron from the Staff's second growth ring (diamonds) and like diamond from the fourth (obsidian),
 * never netherite ({@link LivingStaffGrowthRules#digTier}); it takes no enchantment and has no durability, and each block it breaks
 * costs blood by hardness ({@link LivingDigRules}). One object, summoned, in one form at a time: it does not replace a
 * Fortune pickaxe.
 */
public class LivingPickaxeItem extends LivingToolItem {
	public LivingPickaxeItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.8F, EnumBloodTemper.FERRIC, BlockTags.MINEABLE_WITH_PICKAXE, tier, builderIn);
	}

	@Override
	public boolean digs() {
		return true;
	}
}
