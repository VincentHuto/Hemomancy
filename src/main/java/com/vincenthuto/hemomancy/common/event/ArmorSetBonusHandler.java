package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.CirculationIncomeHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.armor.ArmorSetHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.BloodLustArmorItem;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.BloodLustLineageRules;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.MarrowCrownArmorItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.EnumModArmorTiers;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.ForkFamily;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

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
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class ArmorSetBonusHandler {

	private static final net.minecraft.resources.ResourceLocation CHITINITE_TOUGHNESS_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hemomancy", "chitinite_toughness");
	private static final net.minecraft.resources.ResourceLocation MARROW_CROWN_DAMAGE_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hemomancy", "marrow_crown_damage");
	private static final String SILENT_ARCHON_COOLDOWN_TAG = "hemomancy:silent_archon_refusal_until";
	private static final String PRISMATIC_FLASH_COOLDOWN_TAG = "hemomancy:prismatic_flash_until";

	private static final double HEMATIC_IRON_BLOOD_REGEN = 2.0;
	private static final int HEMATIC_IRON_REGEN_INTERVAL = 20; // Every 20 ticks = 1 second
	private static final float BLOOD_LUST_LIFESTEAL_FRACTION = 0.10f;
	private static final float BARBED_THORNS_DAMAGE = 2.0f;
	private static final int BARBED_BLOOD_LOSS_DURATION = 60; // 3 seconds
	private static final int BARBED_BLOOD_LOSS_AMPLIFIER = 0;
	private static final int BARBED_POISON_DURATION = 80;
	private static final double CHITINITE_TOUGHNESS_BONUS = 2.0;
	private static final float CHITINITE_PROJECTILE_REDUCTION = 0.25f;
	private static final int PRISMATIC_FLASH_COOLDOWN_TICKS = 160;
	private static final int PRISMATIC_FLASH_BLINDNESS_TICKS = 40;
	private static final int PRISMATIC_FLASH_CONFUSION_TICKS = 80;
	private static final int PRISMATIC_FLASH_SPEED_TICKS = 60;
	private static final double PRISMATIC_FLASH_RANGE = 4.0D;
	private static final double MARROW_CROWN_DAMAGE_BONUS = 0.10;
	private static final double MARROW_CROWN_BLOOD_THRESHOLD = 0.50;
	private static final int UNSTAINED_CHECK_INTERVAL = 10; // Check every 10 ticks
	private static final float VENOUS_STRIDER_SABATONS_DAMAGE_REDUCTION = 0.15F;
	private static final double COVENANT_MANTLE_BLOOD_COST = 10.0D;

	/**
	 * Count how many armor pieces of a given material the player is wearing.
	 */
	private static int countArmorPieces(Player player, EnumModArmorTiers tier) {
		int count = 0;
		for (ItemStack stack : player.getArmorSlots()) {
			if (stack.getItem() instanceof ArmorItem armor && armor.getMaterial().value() == tier.holder().value()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Check if the player is wearing a full set (4 pieces) of a given material.
	 */
	private static boolean hasFullSet(Player player, EnumModArmorTiers tier) {
		return countArmorPieces(player, tier) >= 4;
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

		syncSilentArchonLastRite(player);
	}

	// â”€â”€â”€â”€â”€ Tick-Based Bonuses (rate-limited) â”€â”€â”€â”€â”€

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide()) return;

		// Hematic Iron set bonus: passive blood regen (every HEMATIC_IRON_REGEN_INTERVAL ticks)
		if (player.tickCount % HEMATIC_IRON_REGEN_INTERVAL == 0 && hasFullSet(player, EnumModArmorTiers.HEMATIC_IRON)) {
			HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
				if (player instanceof ServerPlayer serverPlayer) {
					BloodFlowLedger.applyCirculationIncome(serverPlayer, volume, "hematic_iron_set",
							"Hematic Iron Set", Category.ARMOR, HEMATIC_IRON_BLOOD_REGEN,
							HEMATIC_IRON_REGEN_INTERVAL, CirculationIncomeHelper.IncomeChannel.ARMOR);
				}
			});
		}

		if (player.tickCount % HEMATIC_IRON_REGEN_INTERVAL == 0 && !LastRiteHelper.hasArmedSource(player)) {
			armLegacyLastRiteSource(player);
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

		if (player.tickCount % 20 == 0
				&& player.getItemBySlot(EquipmentSlot.FEET).is(ItemInit.venous_strider_sabatons.get())
				&& player.fallDistance > 3.0F) {
			player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, false, true, true));
		}

		if (player.tickCount % 40 == 0
				&& player.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.covenant_mantle.get())) {
			pulseCovenantMantle(player);
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
				float healthBefore = player.getHealth();
				player.heal(healAmount);
				// Lifesteal past full health is not wasted: the overkill slice
				// banks into the borrowed-blood reserve.
				float overkill = BorrowedBloodRules.overkillHealing(healthBefore, player.getMaxHealth(), healAmount);
				if (overkill > 0.0F) {
					BorrowedBloodReserve.deposit(player, overkill * BorrowedBloodRules.BLOOD_PER_OVERKILL_HEALTH);
				}
			}
			applyBloodLustMaskBonus(player, event.getEntity());
			if (event.getNewDamage() > 0 && player instanceof ServerPlayer serverPlayer) {
				ArtificerAssignments.onBloodLustDemonstrated(serverPlayer);
			}
		}
	}

	// â”€â”€â”€â”€â”€ Barbed: Thorns + Blood Loss â”€â”€â”€â”€â”€

	@SubscribeEvent
	public static void onPlayerHurt(LivingDamageEvent.Pre event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide()) return;

		if (trySilentArchonDeathRefusal(player, event)) {
			return;
		}

		if (player.getItemBySlot(EquipmentSlot.FEET).is(ItemInit.venous_strider_sabatons.get())
				&& (event.getSource().is(DamageTypeTags.IS_PROJECTILE)
				|| event.getSource().is(DamageTypes.FALL))) {
			event.setNewDamage(event.getNewDamage() * (1.0F - VENOUS_STRIDER_SABATONS_DAMAGE_REDUCTION));
		}

		String bloodLustLineage = ArmorSetHelper.bloodLustLineage(player);
		BloodLustLineageRules.InheritedTrait inherited = BloodLustLineageRules.inheritedTrait(bloodLustLineage);

		float projectileReduction = hasFullSet(player, EnumModArmorTiers.CHITINITE)
				? CHITINITE_PROJECTILE_REDUCTION : inherited.projectileReduction();
		if (projectileReduction > 0) {
			if (!event.getSource().isDirect() || event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
				event.setNewDamage(event.getNewDamage() * (1.0f - projectileReduction));
				if (hasFullSet(player, EnumModArmorTiers.CHITINITE) && player instanceof ServerPlayer serverPlayer) {
					ArtificerAssignments.onForkDemonstrated(serverPlayer, ForkFamily.CHITINITE);
				}
			}
		}

		LivingEntity attacker = null;
		if (event.getSource().getEntity() instanceof LivingEntity living) {
			attacker = living;
		}
		if (attacker == null) return;

		boolean fullBarbed = hasFullSet(player, EnumModArmorTiers.BARBED);
		float thornsDamage = fullBarbed ? BARBED_THORNS_DAMAGE : inherited.thornsDamage();
		if (thornsDamage > 0) {
			// Thorns damage
			boolean retaliated = attacker.hurt(player.damageSources().thorns(player), thornsDamage);

			// Apply Blood Loss and venom to attacker.
			attacker.addEffect(new MobEffectInstance(
					EffectInit.blood_loss,
					fullBarbed ? BARBED_BLOOD_LOSS_DURATION : inherited.bloodLossTicks(),
					BARBED_BLOOD_LOSS_AMPLIFIER));
			attacker.addEffect(new MobEffectInstance(MobEffects.POISON,
					fullBarbed ? BARBED_POISON_DURATION : inherited.poisonTicks(), 0, false, true, true));
			if (fullBarbed && retaliated && player instanceof ServerPlayer serverPlayer) {
				ArtificerAssignments.onForkDemonstrated(serverPlayer, ForkFamily.BARBED);
			}
		}

		boolean fullPrismatic = hasFullSet(player, EnumModArmorTiers.PRISMATIC);
		if (fullPrismatic || inherited.speedTicks() > 0) {
			triggerPrismaticFlash(player, attacker,
					fullPrismatic ? PRISMATIC_FLASH_SPEED_TICKS : inherited.speedTicks(),
					fullPrismatic ? PRISMATIC_FLASH_BLINDNESS_TICKS : inherited.blindnessTicks(),
					fullPrismatic ? PRISMATIC_FLASH_CONFUSION_TICKS : inherited.confusionTicks(),
					fullPrismatic);
		}
	}

	private static void triggerPrismaticFlash(Player player, LivingEntity attacker,
			int speedTicks, int blindnessTicks, int confusionTicks, boolean recordForkAssignment) {
		long now = player.level().getGameTime();
		if (player.getPersistentData().getLong(PRISMATIC_FLASH_COOLDOWN_TAG) > now) {
			return;
		}

		player.getPersistentData().putLong(PRISMATIC_FLASH_COOLDOWN_TAG, now + PRISMATIC_FLASH_COOLDOWN_TICKS);
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, speedTicks, 0, false, true, true));

		player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(PRISMATIC_FLASH_RANGE),
				target -> target.isAlive() && target != player && (target == attacker || target instanceof Monster))
				.forEach(target -> applyPrismaticFlashEffects(target, blindnessTicks, confusionTicks));
		if (recordForkAssignment && player instanceof ServerPlayer serverPlayer) {
			ArtificerAssignments.onForkDemonstrated(serverPlayer, ForkFamily.PRISMATIC);
		}
	}

	private static void syncSilentArchonLastRite(Player player) {
		if (hasFullSet(player, EnumModArmorTiers.SILENT_ARCHON)) {
			LastRiteHelper.arm(player, LastRiteHelper.SILENT_REFUSAL_ID);
		} else {
			LastRiteHelper.clearIfArmed(player, LastRiteHelper.SILENT_REFUSAL_ID);
		}
	}

	private static void armLegacyLastRiteSource(Player player) {
		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(morphCap -> {
			if (morphCap.hasMorphling()) {
				LastRiteHelper.armForMorphling(player, morphCap.getEquippedMorphling());
			}
		});
		if (!LastRiteHelper.hasArmedSource(player) && hasFullSet(player, EnumModArmorTiers.SILENT_ARCHON)) {
			LastRiteHelper.arm(player, LastRiteHelper.SILENT_REFUSAL_ID);
		}
	}

	private static void applyPrismaticFlashEffects(LivingEntity target, int blindnessTicks, int confusionTicks) {
		target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blindnessTicks, 0, false, true, true));
		target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, confusionTicks, 0, false, true, true));
		target.addEffect(new MobEffectInstance(MobEffects.GLOWING, confusionTicks, 0, false, true, true));
	}

	private static boolean trySilentArchonDeathRefusal(Player player, LivingDamageEvent.Pre event) {
		if (event.getNewDamage() < player.getHealth()) {
			return false;
		}

		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive()) {
			return false;
		}

		long now = player.level().getGameTime();
		long cooldownUntil = player.getPersistentData().getLong(SILENT_ARCHON_COOLDOWN_TAG);
		String archonChoice = player.getPersistentData().getString(FungalGardenTravelHelper.ARCHON_CHOICE_KEY);
		boolean canRefuse = SilentArchonArmorRules.canRefuseDeath(
				hasFullSet(player, EnumModArmorTiers.SILENT_ARCHON),
				HemoCapabilityAccess.getPlayerDegreeNumber(player),
				archonChoice,
				volume.getBloodVolume(),
				SilentArchonArmorRules.DEATH_REFUSAL_BLOOD_COST,
				now,
				cooldownUntil);
		if (!canRefuse) {
			return false;
		}
		if (!LastRiteHelper.canFire(player, LastRiteHelper.SILENT_REFUSAL_ID)) {
			return false;
		}

		volume.drain(SilentArchonArmorRules.DEATH_REFUSAL_BLOOD_COST);
		if (player instanceof ServerPlayer serverPlayer) {
			syncVolume(serverPlayer, volume);
		}
		player.getPersistentData().putLong(SILENT_ARCHON_COOLDOWN_TAG,
				SilentArchonArmorRules.nextCooldownUntil(now));
		LastRiteHelper.consume(player, LastRiteHelper.SILENT_REFUSAL_ID);
		event.setNewDamage(SilentArchonArmorRules.damageLeavingBarelyAlive(player.getHealth()));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4, false, true, true));
		player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 1, false, true, true));
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 1, false, true, true));
		player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 240, 1, false, true, true));
		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0, false, true, true));
		return true;
	}

	private static void applyBloodLustMaskBonus(Player player, LivingEntity target) {
		BloodLustArmorItem.MaskType maskType = getBloodLustMaskType(player);
		if (maskType == null) {
			return;
		}
		switch (maskType) {
			case TENGU -> target.addEffect(new MobEffectInstance(EffectInit.blood_loss, 40, 0, false, true, true));
			case GRINNING -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, true, true));
			case LODESTONE -> player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 80, 0, false, true, true));
			case VELORUM -> HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
				if (volume.isActive() && !volume.isFull()) {
					CirculationIncomeHelper.grant(player, volume, 4.0D,
							CirculationIncomeHelper.IncomeChannel.ARMOR);
					if (player instanceof ServerPlayer serverPlayer) {
						syncVolume(serverPlayer, volume);
					}
				}
			});
			default -> {
			}
		}
	}

	private static BloodLustArmorItem.MaskType getBloodLustMaskType(Player player) {
		ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
		if (helmet.getItem() instanceof BloodLustArmorItem bloodLustArmor) {
			return bloodLustArmor.getMaskType();
		}
		return null;
	}

	private static void pulseCovenantMantle(Player player) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive()
				|| volume.getBloodVolume() < COVENANT_MANTLE_BLOOD_COST) {
			return;
		}

		boolean granted = false;
		for (Player nearby : player.level().getEntitiesOfClass(Player.class,
				player.getBoundingBox().inflate(8.0D),
				nearby -> nearby != player && HemoCapabilityAccess.getBloodVolume(nearby)
						.map(IBloodVolume::isActive).orElse(false))) {
			nearby.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, false, true, true));
			granted = true;
		}
		if (granted && player instanceof ServerPlayer serverPlayer) {
			BloodFlowLedger.applyDrain(serverPlayer, volume, "covenant_mantle",
					"Covenant Mantle", Category.ARMOR, COVENANT_MANTLE_BLOOD_COST, 40, true);
		}
	}

	// â”€â”€â”€â”€â”€ Chitinite: Armor Toughness Modifier â”€â”€â”€â”€â”€

	private static void updateChitiniteToughness(Player player) {
		AttributeInstance toughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
		if (toughness == null) return;

		double bonus = hasFullSet(player, EnumModArmorTiers.CHITINITE)
				? CHITINITE_TOUGHNESS_BONUS
				: BloodLustLineageRules.inheritedTrait(ArmorSetHelper.bloodLustLineage(player)).toughness();
		AttributeModifier existing = toughness.getModifier(CHITINITE_TOUGHNESS_ID);

		if (existing != null && (bonus <= 0 || existing.amount() != bonus)) {
			toughness.removeModifier(CHITINITE_TOUGHNESS_ID);
			existing = null;
		}
		if (bonus > 0 && existing == null) {
			// Pre-clamped against the triad toughness budget; morphling and
			// scar layers join this accounting on the dev-machine pass.
			toughness.addTransientModifier(new AttributeModifier(
					CHITINITE_TOUGHNESS_ID,
					TriadAttributeCaps.clampToughness(0.0D, bonus),
					AttributeModifier.Operation.ADD_VALUE));
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
