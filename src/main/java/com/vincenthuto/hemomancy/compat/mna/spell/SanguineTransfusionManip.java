package com.vincenthuto.hemomancy.compat.mna.spell;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A blood manipulation that converts the blood cost into MnA mana for the
 * caster. The blood has already been consumed by {@link BloodManipulation#performAction}
 * before {@link #getAction} runs, so this method only needs to restore the
 * corresponding mana.
 *
 * Conversion ratio: 1 blood → 0.2 mana  (the 200 blood cost yields 40 mana).
 */
public class SanguineTransfusionManip extends BloodManipulation {

	/** Mana gained per 1 blood spent. */
	private static final float MANA_PER_BLOOD = 0.2F;

	public SanguineTransfusionManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		System.out.println("Sanguine Transfusion activated: restoring mana based on blood cost.");

		IPlayerMagic magic = player.getCapability(PlayerMagicProvider.MAGIC).orElse(null);
		if (magic == null) {
			player.displayClientMessage(
					Component.literal("Your body lacks the arcane pathways!")
							.withStyle(ChatFormatting.RED),
					true);
			return;
		}
		System.out.println("Sanguine Transfusion activated: restoring mana based on blood cost.");
		float manaToRestore = (float) (getCost() * MANA_PER_BLOOD);

		// Cap so we don't exceed the player's max mana
		float currentMana = magic.getCastingResource().getAmount();
		float maxMana = magic.getCastingResource().getMaxAmount();
		float actualRestore = Math.min(manaToRestore, maxMana - currentMana);

		if (actualRestore <= 0) {
			player.displayClientMessage(
					Component.literal("Your mana is already full!")
							.withStyle(ChatFormatting.AQUA),
					true);
			return;
		}

		magic.getCastingResource().restore(actualRestore);

		// Visual feedback — blue/purple glow particles rising from the player
		if (world instanceof ServerLevel sLevel) {
			BlockPos pos = player.blockPosition();
			RandomSource random = world.random;
			for (int i = 0; i < 25; i++) {
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(
								80 + random.nextFloat() * 50,
								20,
								200 + random.nextFloat() * 55)),
						pos.getX() + 0.5 + (random.nextDouble() - 0.5),
						pos.getY() + random.nextDouble() + 0.5,
						pos.getZ() + 0.5 + (random.nextDouble() - 0.5),
						3, 0f, 0.25f, 0f, random.nextInt(3) * 0.015f);
			}
		}
	}
}
