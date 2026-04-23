package com.vincenthuto.hemomancy.common.event;

import net.neoforged.fml.common.EventBusSubscriber;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.armor.EnumModArmorTiers;
import com.vincenthuto.hemomancy.common.item.armor.MarrowCrownArmorItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Handles armor set bonuses for all Hemomancy armor sets.
 * Full-set bonuses require all 4 armor pieces of the same material.
 *
 * <ul>
 *   <li><b>Hematic Iron:</b> Passive blood regeneration (+2 blood/second)</li>
 *   <li><b>Blood Lust:</b> Lifesteal â€” 10% of melee damage dealt heals the player</li>
 *   <li><b>Barbed:</b> Thorns â€” attackers take 2 damage and receive Blood Loss</li>
 *   <li><b>Chitinite:</b> +2.0 Armor Toughness and 25% projectile damage reduction</li>
 *   <li><b>Unstained:</b> Immunity to Blood Loss and Hemolysis effects</li>
 *   <li><b>Marrow Crown:</b> Artifact helmet â€” +10% melee damage when blood &gt; 50%</li>
 * </ul>
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ArmorSetBonusHandler {

	private static final net.minecraft.resources.ResourceLocation CHITINITE_TOUGHNESS_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hemomancy", "chitinite_toughness");
	private static final net.minecraft.resources.ResourceLocation MARROW_CROWN_DAMAGE_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hemomancy", "marrow_crown_damage");

	private static final double HEMATIC_IRON_BLOOD_REGEN = 2.0;
	private static final int HEMATIC_IRON_REGEN_INTERVAL = 20; // Every 20 ticks = 1 second
	private static final float BLOOD_LUST_LIFESTEAL_FRACTION = 0.10f;
	private static final float BARBED_THORNS_DAMAGE = 2.0f;
	private static final int BARBED_BLOOD_LOSS_DURATION = 60; // 3 seconds
	private static final int BARBED_BLOOD_LOSS_AMPLIFIER = 0;
	private static final double CHITINITE_TOUGHNESS_BONUS = 2.0;
	private static final float CHITINITE_PROJECTILE_REDUCTION = 0.25f;
	private static final double MARROW_CROWN_DAMAGE_BONUS = 0.10;
	private static final double MARROW_CROWN_BLOOD_THRESHOLD = 0.50;
	private static final int UNSTAINED_CHECK_INTERVAL = 10; // Check every 10 ticks

	/**
	 * Count how many armor pieces of a given material the player is wearing.
	 */
	private static int countArmorPieces(Player player, ArmorMaterial material) {
		int count = 0;
		for (ItemStack stack : player.getArmorSlots()) {
			if (stack.getItem() instanceof ArmorItem armor && armor.getMaterial().value() == material) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Check if the player is wearing a full set (4 pieces) of a given material.
	 */
	private static boolean hasFullSet(Player player, ArmorMaterial material) {
		return countArmorPieces(player, material) >= 4;
	}

	// â”€â”€â”€â”€â”€ Equipment Change: Attribute Modifiers â”€â”€â”€â”€â”€

	/**
	 * Update attribute modifiers when armor equipment changes, avoiding per-tick overhead.
	 */
	@SubscribeEvent
	public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide()) return;

		EquipmentSlot slot = event.getSlot();
		if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) return;

		// Chitinite set bonus: attribute modifier for toughness
		updateChitiniteToughness(player);

		// Marrow Crown artifact: damage bonus attribute modifier
		updateMarrowCrownDamage(player);
	}

	// â”€â”€â”€â”€â”€ Tick-Based Bonuses (rate-limited) â”€â”€â”€â”€â”€

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide()) return;

		// Hematic Iron set bonus: passive blood regen (every HEMATIC_IRON_REGEN_INTERVAL ticks)
		if (player.tickCount % HEMATIC_IRON_REGEN_INTERVAL == 0 && hasFullSet(player, EnumModArmorTiers.HEMATIC_IRON)) {
			HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
				if (volume.isActive() && !volume.isFull()) {
					volume.fill(HEMATIC_IRON_BLOOD_REGEN);
					syncVolume((ServerPlayer) player, volume);
				}
			});
		}

		// Marrow Crown: re-check blood threshold periodically (blood level can change without equipment change)
		if (player.tickCount % HEMATIC_IRON_REGEN_INTERVAL == 0) {
			updateMarrowCrownDamage(player);
		}

		// Unstained set bonus: remove blood-related debuffs (check every UNSTAINED_CHECK_INTERVAL ticks)
		if (player.tickCount % UNSTAINED_CHECK_INTERVAL == 0 && hasFullSet(player, EnumModArmorTiers.UNSTAINED)) {
			if (player.hasEffect(EffectInit.blood_loss)) {
				player.removeEffect(EffectInit.blood_loss);
			}
			if (player.hasEffect(EffectInit.hemolysis)) {
				player.removeEffect(EffectInit.hemolysis);
			}
		}
	}

	// â”€â”€â”€â”€â”€ Blood Lust: Lifesteal â”€â”€â”€â”€â”€

	@SubscribeEvent
	public static void onLivingDamage(LivingDamageEvent.Post event) {
		if (!(event.getSource().getEntity() instanceof Player player)) return;
		if (player.level().isClientSide()) return;
		if (!event.getSource().isDirect()) return;

		if (hasFullSet(player, EnumModArmorTiers.BLOODLUST)) {
			float healAmount = event.getNewDamage() * BLOOD_LUST_LIFESTEAL_FRACTION;
			if (healAmount > 0) {
				player.heal(healAmount);
			}
		}
	}

	// â”€â”€â”€â”€â”€ Barbed: Thorns + Blood Loss â”€â”€â”€â”€â”€

	@SubscribeEvent
	public static void onPlayerHurt(LivingDamageEvent.Pre event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide()) return;

		LivingEntity attacker = null;
		if (event.getSource().getEntity() instanceof LivingEntity living) {
			attacker = living;
		}
		if (attacker == null) return;

		if (hasFullSet(player, EnumModArmorTiers.BARBED)) {
			// Thorns damage
			attacker.hurt(player.damageSources().thorns(player), BARBED_THORNS_DAMAGE);

			// Apply Blood Loss effect to attacker
			attacker.addEffect(new MobEffectInstance(
					EffectInit.blood_loss,
					BARBED_BLOOD_LOSS_DURATION,
					BARBED_BLOOD_LOSS_AMPLIFIER));
		}

		// Chitinite set bonus: projectile damage reduction
		if (hasFullSet(player, EnumModArmorTiers.CHITINITE)) {
			if (!event.getSource().isDirect() || event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
				event.setNewDamage(event.getNewDamage() * (1.0f - CHITINITE_PROJECTILE_REDUCTION));
			}
		}
	}

	// â”€â”€â”€â”€â”€ Chitinite: Armor Toughness Modifier â”€â”€â”€â”€â”€

	private static void updateChitiniteToughness(Player player) {
		AttributeInstance toughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
		if (toughness == null) return;

		boolean hasSet = hasFullSet(player, EnumModArmorTiers.CHITINITE);
		AttributeModifier existing = toughness.getModifier(CHITINITE_TOUGHNESS_ID);

		if (hasSet && existing == null) {
			toughness.addTransientModifier(new AttributeModifier(
					CHITINITE_TOUGHNESS_ID,
					CHITINITE_TOUGHNESS_BONUS,
					AttributeModifier.Operation.ADD_VALUE));
		} else if (!hasSet && existing != null) {
			toughness.removeModifier(CHITINITE_TOUGHNESS_ID);
		}
	}

	// â”€â”€â”€â”€â”€ Marrow Crown: Damage Bonus â”€â”€â”€â”€â”€

	private static void updateMarrowCrownDamage(Player player) {
		AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
		if (damage == null) return;

		ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
		boolean hasCrown = helmet.getItem() instanceof MarrowCrownArmorItem;

		boolean hasBlood = false;
		if (hasCrown) {
			hasBlood = HemoCapabilityAccess.getBloodVolume(player)
					.map(vol -> vol.isActive() && vol.getBloodVolume() > vol.getMaxBloodVolume() * MARROW_CROWN_BLOOD_THRESHOLD)
					.orElse(false);
		}

		boolean shouldHaveBonus = hasCrown && hasBlood;
		AttributeModifier existing = damage.getModifier(MARROW_CROWN_DAMAGE_ID);

		if (shouldHaveBonus && existing == null) {
			damage.addTransientModifier(new AttributeModifier(
					MARROW_CROWN_DAMAGE_ID,
					MARROW_CROWN_DAMAGE_BONUS,
					AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		} else if (!shouldHaveBonus && existing != null) {
			damage.removeModifier(MARROW_CROWN_DAMAGE_ID);
		}
	}

	// â”€â”€â”€â”€â”€ Utility â”€â”€â”€â”€â”€

	private static void syncVolume(ServerPlayer player, IBloodVolume volume) {
		BloodVolumeEvents.syncVolume(player, volume);
	}
}
