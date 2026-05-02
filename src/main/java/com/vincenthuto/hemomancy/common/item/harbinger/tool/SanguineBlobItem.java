package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A cosmetic blood orb unlocked at Initiatory Degree 3 (Initiate of the Scarlet Sanctum).
 * While held, a liquid blood sphere floats in the air at the player's look-target point.
 * The player can press the "Drop Blood Ball" keybind to release it, watching it fall and fade away.
 * All visual effects are rendered client-side via {@link com.vincenthuto.hemomancy.client.render.world.BloodBallRenderer}.
 */
public class SanguineBlobItem extends Item {

	public SanguineBlobItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		// Suppress default right-click behaviour; ball control is via dedicated keybind
		return InteractionResultHolder.fail(player.getItemInHand(hand));
	}
}
