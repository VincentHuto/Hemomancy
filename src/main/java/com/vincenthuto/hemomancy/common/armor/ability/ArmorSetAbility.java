package com.vincenthuto.hemomancy.common.armor.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ArmorSetAbility {
	ResourceLocation id();

	Component displayName();

	List<Component> tooltip();

	ItemStack getDisplayIcon(Player player);

	boolean isAvailable(Player player);

	boolean canActivate(Player player);

	void activate(ServerPlayer player);

	default int cooldownTicks() {
		return 0;
	}

	default double bloodCost() {
		return 0.0D;
	}
}
