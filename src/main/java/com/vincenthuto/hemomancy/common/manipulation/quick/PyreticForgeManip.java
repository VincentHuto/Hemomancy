package com.vincenthuto.hemomancy.common.manipulation.quick;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.SimpleContainer;

import java.util.Optional;

/**
 * Pyretic Forge — a T2 (MEDIOCRITAS) quick manipulation that smelts
 * the player's held item stack in-hand using blood-borne heat.
 * <p>
 * Looks up the smelting recipe for the held item and replaces the
 * entire stack with the smelted result (same quantity). Requires
 * the held item to have a valid furnace smelting recipe.
 * <p>
 * Perfect mid-game utility for instant field smelting of ores and food.
 */
public class PyreticForgeManip extends BloodManipulation {

	public PyreticForgeManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (heldItemMainhand.isEmpty()) {
			return;
		}

		// Look up smelting recipe for the held item
		Optional<SmeltingRecipe> recipe = world.getRecipeManager()
				.getRecipeFor(RecipeType.SMELTING, new SimpleContainer(heldItemMainhand), world);

		if (recipe.isEmpty()) {
			return;
		}

		ItemStack result = recipe.get().getResultItem(world.registryAccess());
		if (result.isEmpty()) {
			return;
		}

		int count = heldItemMainhand.getCount();
		ItemStack smeltedStack = result.copy();
		smeltedStack.setCount(count);

		// Replace held item with smelted version (always main hand since heldItemMainhand is main hand)
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, smeltedStack);

		world.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 1.0f);
		world.playSound(null, player.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.6f, 1.2f);

		if (world instanceof ServerLevel sLevel) {
			BlockPos pos = player.blockPosition();
			RandomSource random = world.random;

			// Hot orange-red particle burst
			for (int i = 0; i < 20; i++) {
				float r = 200 + random.nextFloat() * 55;
				float g = 80 + random.nextFloat() * 100;
				float b = random.nextFloat() * 20;
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(r, g, b)),
						pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
						pos.getY() + 1.0 + random.nextDouble() * 0.5,
						pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
						1, 0f, 0.15f, 0f, 0.02f);
			}
		}
	}
}
