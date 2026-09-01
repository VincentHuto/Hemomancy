package com.vincenthuto.hemomancy.common.armor.ability;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.SyncArmorSetAbilityCooldownS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.function.Supplier;

public final class ArmorSetAbilityRegistry {
	public static final ResourceLocation EDACIOUS_BLOODBURST = Hemomancy.rloc("edacious_bloodburst");
	public static final ResourceLocation SHEOLIC_BASTION_STANCE = Hemomancy.rloc("sheolic_bastion_stance");
	public static final ResourceLocation MASQUERADE_OF_THE_FORGOTTEN = Hemomancy.rloc("masquerade_of_the_forgotten");
	public static final ResourceLocation SILENT_ARCHON_SEVERANCE = Hemomancy.rloc("silent_archon_severance");

	private static final String COOLDOWN_PREFIX = "hemomancy:armor_set_ability_cooldown:";
	private static final Map<ResourceLocation, ArmorSetAbility> ABILITIES = new LinkedHashMap<>();

	static {
		register(new SimpleArmorSetAbility(
				EDACIOUS_BLOODBURST,
				Component.literal("Bloodburst"),
				tooltip("ability.hemomancy.edacious_bloodburst.tooltip", 240, 250.0D),
				requiredSet(ItemInit.edacious_blood_lust_helm, ItemInit.edacious_blood_lust_chest,
						ItemInit.edacious_blood_lust_legs, ItemInit.edacious_blood_lust_boots),
				ItemInit.edacious_blood_lust_helm,
				player -> true,
				EdaciousBloodburstArmorAbilityHandler::activateBloodburst,
				240,
				250.0D));
		register(new SimpleArmorSetAbility(
				SHEOLIC_BASTION_STANCE,
				Component.literal("Bastion Stance"),
				tooltip("ability.hemomancy.sheolic_bastion_stance.tooltip", 360, 0.0D),
				requiredSet(ItemInit.sheolic_blood_lust_helm, ItemInit.sheolic_blood_lust_chest,
						ItemInit.sheolic_blood_lust_legs, ItemInit.sheolic_blood_lust_boots),
				ItemInit.sheolic_blood_lust_helm,
				player -> true,
				SheolicBastionBloodlustArmorAbilityHandler::activateBastionStance,
				360,
				0.0D));
		register(new SimpleArmorSetAbility(
				MASQUERADE_OF_THE_FORGOTTEN,
				Component.literal("Masquerade of the Forgotten"),
				tooltip("ability.hemomancy.masquerade_of_the_forgotten.tooltip", 1200, 250.0D),
				requiredSet(ItemInit.phantasmal_blood_lust_helm, ItemInit.phantasmal_blood_lust_chest,
						ItemInit.phantasmal_blood_lust_legs, ItemInit.phantasmal_blood_lust_boots),
				ItemInit.phantasmal_blood_lust_helm,
				player -> true,
				PhanstmalBloodlustArmorAbilityHandler::activateMasqueradeOfTheForgotten,
				1200,
				250.0D));
		register(new SimpleArmorSetAbility(
				SILENT_ARCHON_SEVERANCE,
				Component.literal("Silent Severance"),
				tooltip("ability.hemomancy.silent_archon_severance.tooltip", 240, 300.0D),
				requiredSet(ItemInit.silent_archon_helm, ItemInit.silent_archon_chestplate,
						ItemInit.silent_archon_leggings, ItemInit.silent_archon_boots),
				ItemInit.silent_archon_helm,
				player -> true,
				SilentArchonArmorAbilityHandler::activateSilentArchonSeverance,
				240,
				300.0D));
	}

	private ArmorSetAbilityRegistry() {
	}

	public static void register(ArmorSetAbility ability) {
		ABILITIES.put(ability.id(), ability);
	}

	public static Collection<ArmorSetAbility> all() {
		return ABILITIES.values();
	}

	public static Optional<ArmorSetAbility> get(ResourceLocation id) {
		return Optional.ofNullable(ABILITIES.get(id));
	}

	public static Optional<ArmorSetAbility> getActiveAbility(Player player) {
		if (player == null) {
			return Optional.empty();
		}
		return ABILITIES.values().stream()
				.filter(ability -> ability.isAvailable(player))
				.findFirst();
	}

	public static boolean tryActivate(ServerPlayer player, ResourceLocation abilityId) {
		ArmorSetAbility ability = ABILITIES.get(abilityId);
		if (ability == null) {
			message(player, Component.literal("No such armor ability.").withStyle(ChatFormatting.RED));
			return false;
		}
		if (!player.isAlive()) {
			message(player, Component.literal("The armor is silent.").withStyle(ChatFormatting.RED));
			return false;
		}
		if (!ability.isAvailable(player)) {
			message(player, Component.literal("The armor is incomplete.").withStyle(ChatFormatting.RED));
			return false;
		}
		if (!ability.canActivate(player)) {
			message(player, Component.literal("The armor refuses.").withStyle(ChatFormatting.RED));
			return false;
		}
		if (SHEOLIC_BASTION_STANCE.equals(ability.id()) && player.isShiftKeyDown()) {
			ability.activate(player);
			return true;
		}
		if (SHEOLIC_BASTION_STANCE.equals(ability.id())
				&& SheolicBastionBloodlustArmorAbilityHandler.isBastionActive(player)) {
			ability.activate(player);
			ArtificerAssignments.onD7AbilityActivated(player);
			return true;
		}
		long now = player.level().getGameTime();
		long cooldownUntil = getCooldownUntil(player, ability);
		if (cooldownUntil > now) {
			syncCooldown(player, ability, cooldownUntil);
			message(player, Component.literal("Armor ability recharging.").withStyle(ChatFormatting.RED));
			return false;
		}
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (ability.bloodCost() > 0.0D) {
			if (volume == null || !volume.isActive() || volume.getBloodVolume() < ability.bloodCost()) {
				message(player, Component.literal("Not enough blood.").withStyle(ChatFormatting.RED));
				return false;
			}
			if (!volume.drain(ability.bloodCost())) {
				message(player, Component.literal("Not enough blood.").withStyle(ChatFormatting.RED));
				return false;
			}
			BloodVolumeEvents.syncVolume(player, volume);
		}
		if (ability.cooldownTicks() > 0) {
			setCooldownUntil(player, ability, now + ability.cooldownTicks());
		}
		ability.activate(player);
		ArtificerAssignments.onD7AbilityActivated(player);
		return true;
	}

	public static long getCooldownUntil(Player player, ArmorSetAbility ability) {
		return player.getPersistentData().getLong(COOLDOWN_PREFIX + ability.id());
	}

	public static long getClientCooldownUntil(Player player, ArmorSetAbility ability) {
		return getCooldownUntil(player, ability);
	}

	public static void handleClientCooldownSync(Player player, ResourceLocation abilityId, int remainingTicks) {
		ArmorSetAbility ability = ABILITIES.get(abilityId);
		if (ability == null || player.level() == null) {
			return;
		}
		long cooldownUntil = remainingTicks > 0 ? player.level().getGameTime() + remainingTicks : 0L;
		player.getPersistentData().putLong(COOLDOWN_PREFIX + ability.id(), cooldownUntil);
	}

	public static boolean isAbilityAvailable(Player player, ResourceLocation abilityId) {
		ArmorSetAbility ability = ABILITIES.get(abilityId);
		return ability != null && ability.isAvailable(player);
	}

	private static void setCooldownUntil(ServerPlayer player, ArmorSetAbility ability, long cooldownUntil) {
		player.getPersistentData().putLong(COOLDOWN_PREFIX + ability.id(), cooldownUntil);
		syncCooldown(player, ability, cooldownUntil);
	}

	private static void syncCooldown(ServerPlayer player, ArmorSetAbility ability, long cooldownUntil) {
		long remainingTicks = Math.max(0L, cooldownUntil - player.level().getGameTime());
		PacketHandler.sendToPlayer(player, new SyncArmorSetAbilityCooldownS2CPacket(
				ability.id(), (int) Math.min(Integer.MAX_VALUE, remainingTicks)));
	}

	private static List<Component> tooltip(String descriptionKey, int cooldownTicks, double bloodCost) {
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
		if (bloodCost > 0.0D) {
			tooltip.add(Component.translatable("ability.hemomancy.armor_set.cost", (int) bloodCost)
					.withStyle(ChatFormatting.DARK_RED));
		}
		if (cooldownTicks > 0) {
			tooltip.add(Component.translatable("ability.hemomancy.armor_set.cooldown", cooldownTicks / 20)
					.withStyle(ChatFormatting.DARK_GRAY));
		}
		return List.copyOf(tooltip);
	}

	private static Map<EquipmentSlot, Supplier<? extends Item>> requiredSet(
			Supplier<? extends Item> helmet,
			Supplier<? extends Item> chest,
			Supplier<? extends Item> legs,
			Supplier<? extends Item> boots) {
		return Map.of(
				EquipmentSlot.HEAD, helmet,
				EquipmentSlot.CHEST, chest,
				EquipmentSlot.LEGS, legs,
				EquipmentSlot.FEET, boots);
	}

	private static void message(ServerPlayer player, Component message) {
		player.displayClientMessage(message, true);
	}
}
