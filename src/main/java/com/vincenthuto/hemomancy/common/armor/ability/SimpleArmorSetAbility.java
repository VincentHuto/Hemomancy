package com.vincenthuto.hemomancy.common.armor.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SimpleArmorSetAbility implements ArmorSetAbility {
	private final ResourceLocation id;
	private final Component displayName;
	private final Map<EquipmentSlot, Supplier<? extends Item>> requiredPieces;
	private final Supplier<? extends Item> displayIcon;
	private final Predicate<Player> validation;
	private final Consumer<ServerPlayer> activation;
	private final int cooldownTicks;
	private final double bloodCost;

	public SimpleArmorSetAbility(ResourceLocation id, Component displayName,
			Map<EquipmentSlot, Supplier<? extends Item>> requiredPieces,
			Supplier<? extends Item> displayIcon,
			Predicate<Player> validation,
			Consumer<ServerPlayer> activation,
			int cooldownTicks,
			double bloodCost) {
		this.id = id;
		this.displayName = displayName;
		this.requiredPieces = Map.copyOf(requiredPieces);
		this.displayIcon = displayIcon;
		this.validation = validation;
		this.activation = activation;
		this.cooldownTicks = cooldownTicks;
		this.bloodCost = bloodCost;
	}

	@Override
	public ResourceLocation id() {
		return id;
	}

	@Override
	public Component displayName() {
		return displayName;
	}

	@Override
	public ItemStack getDisplayIcon(Player player) {
		ItemStack equippedHelmet = player.getItemBySlot(EquipmentSlot.HEAD);
		if (!equippedHelmet.isEmpty() && equippedHelmet.is(displayIcon.get())) {
			return equippedHelmet.copy();
		}
		return new ItemStack(displayIcon.get());
	}

	@Override
	public boolean isAvailable(Player player) {
		for (Map.Entry<EquipmentSlot, Supplier<? extends Item>> entry : requiredPieces.entrySet()) {
			if (!player.getItemBySlot(entry.getKey()).is(entry.getValue().get())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean canActivate(Player player) {
		return validation.test(player);
	}

	@Override
	public void activate(ServerPlayer player) {
		activation.accept(player);
	}

	@Override
	public int cooldownTicks() {
		return cooldownTicks;
	}

	@Override
	public double bloodCost() {
		return bloodCost;
	}
}
