package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarType;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ScarInit {
	public static final ResourceKey<Registry<ScarDefinition>> SCAR_KEY =
			ResourceKey.createRegistryKey(Hemomancy.rloc("scars"));
	public static final DeferredRegister<ScarDefinition> SCARS =
			DeferredRegister.create(SCAR_KEY, Hemomancy.MOD_ID);
	public static final Registry<ScarDefinition> SCARS_TYPE_REGISTRY =
			SCARS.makeRegistry(builder -> builder.maxId(Integer.MAX_VALUE - 1)
					.defaultKey(Hemomancy.rloc("scar_heart")));

	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_heart = reg("scar_heart",
			() -> cerebral(EnumBloodTendency.ANIMUS, 1, 1)
					.withModifier(Attributes.MAX_HEALTH, "scar_heart_hp", 2.0, AttributeModifier.Operation.ADD_VALUE)
					.withMaxBloodModifier(-100.0));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_marrow = reg("scar_marrow",
			() -> cerebral(EnumBloodTendency.ANIMUS, 2, 2)
					.withModifier(Attributes.MAX_HEALTH, "scar_marrow_hp", 4.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_marrow_ms", -0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withMaxBloodModifier(-200.0));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_phoenix = reg("scar_phoenix",
			() -> cerebral(EnumBloodTendency.ANIMUS, 3, 3)
					.withModifier(Attributes.MAX_HEALTH, "scar_phoenix_hp", 6.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_phoenix_ms", -0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withMaxBloodModifier(-400.0));

	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_pyre = reg("scar_pyre",
			() -> cerebral(EnumBloodTendency.FLAMMEUS, 1, 1)
					.withModifier(Attributes.ATTACK_DAMAGE, "scar_pyre_ad", 1.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.ARMOR, "scar_pyre_armor", -1.0, AttributeModifier.Operation.ADD_VALUE));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_sol = reg("scar_sol",
			() -> cerebral(EnumBloodTendency.FLAMMEUS, 2, 2)
					.withModifier(Attributes.ATTACK_DAMAGE, "scar_sol_ad", 2.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.ARMOR, "scar_sol_armor", -2.0, AttributeModifier.Operation.ADD_VALUE));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_corona = reg("scar_corona",
			() -> cerebral(EnumBloodTendency.FLAMMEUS, 3, 3)
					.withModifier(Attributes.ATTACK_DAMAGE, "scar_corona_ad", 3.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.KNOCKBACK_RESISTANCE, "scar_corona_kb", 0.3, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.ARMOR, "scar_corona_armor", -3.0, AttributeModifier.Operation.ADD_VALUE)
					.withBloodUpkeep(0.5));

	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_feral = reg("scar_feral",
			() -> cerebral(EnumBloodTendency.DUCTILIS, 1, 1)
					.withModifier(Attributes.ATTACK_SPEED, "scar_feral_as", 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withModifier(Attributes.ARMOR, "scar_feral_armor", -1.0, AttributeModifier.Operation.ADD_VALUE));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_flux = reg("scar_flux",
			() -> cerebral(EnumBloodTendency.DUCTILIS, 2, 2)
					.withModifier(Attributes.ATTACK_SPEED, "scar_flux_as", 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withModifier(Attributes.ARMOR, "scar_flux_armor", -2.0, AttributeModifier.Operation.ADD_VALUE));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_chimera = reg("scar_chimera",
			() -> cerebral(EnumBloodTendency.DUCTILIS, 3, 3)
					.withModifier(Attributes.ATTACK_SPEED, "scar_chimera_as", 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withModifier(Attributes.ARMOR, "scar_chimera_armor", -3.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.MAX_HEALTH, "scar_chimera_hp", -4.0, AttributeModifier.Operation.ADD_VALUE));

	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_halo = reg("scar_halo",
			() -> cerebral(EnumBloodTendency.LUX, 1, 1)
					.withModifier(Attributes.ARMOR_TOUGHNESS, "scar_halo_at", 1.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_halo_ms", -0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_veil = reg("scar_veil",
			() -> cerebral(EnumBloodTendency.LUX, 2, 2)
					.withModifier(Attributes.ARMOR_TOUGHNESS, "scar_veil_at", 2.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_veil_ms", -0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_transcendence = reg("scar_transcendence",
			() -> cerebral(EnumBloodTendency.LUX, 3, 3)
					.withModifier(Attributes.ARMOR_TOUGHNESS, "scar_transcendence_at", 2.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_transcendence_ms", -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withMaxBloodModifier(-100.0));

	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_blight = reg("scar_blight",
			() -> cerebral(EnumBloodTendency.MORTEM, 1, 1)
					.withModifier(Attributes.ATTACK_DAMAGE, "scar_blight_ad", 1.0, AttributeModifier.Operation.ADD_VALUE));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_wither = reg("scar_wither",
			() -> cerebral(EnumBloodTendency.MORTEM, 2, 2)
					.withModifier(Attributes.ATTACK_DAMAGE, "scar_wither_ad", 2.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.MAX_HEALTH, "scar_wither_hp", -2.0, AttributeModifier.Operation.ADD_VALUE));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_oblivion = reg("scar_oblivion",
			() -> cerebral(EnumBloodTendency.MORTEM, 3, 3)
					.withModifier(Attributes.ATTACK_DAMAGE, "scar_oblivion_ad", 3.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.MAX_HEALTH, "scar_oblivion_hp", -4.0, AttributeModifier.Operation.ADD_VALUE)
					.withBloodUpkeep(0.3));

	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_rime = reg("scar_rime",
			() -> cerebral(EnumBloodTendency.CONGEATIO, 1, 1)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_rime_ms", 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withModifier(Attributes.ATTACK_SPEED, "scar_rime_as", -0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_glacier = reg("scar_glacier",
			() -> cerebral(EnumBloodTendency.CONGEATIO, 2, 2)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_glacier_ms", 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withModifier(Attributes.ATTACK_SPEED, "scar_glacier_as", -0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_descendence = reg("scar_descendence",
			() -> cerebral(EnumBloodTendency.CONGEATIO, 3, 3)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_descendence_ms", 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withModifier(Attributes.ATTACK_SPEED, "scar_descendence_as", -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withModifier(Attributes.ATTACK_DAMAGE, "scar_descendence_ad", -2.0, AttributeModifier.Operation.ADD_VALUE));

	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_thorn = reg("scar_thorn",
			() -> cerebral(EnumBloodTendency.FERRIC, 1, 1)
					.withIronHeartCapacityBonus(2)
					.withModifier(Attributes.ARMOR, "scar_thorn_armor", 1.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_thorn_ms", -0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_anvil = reg("scar_anvil",
			() -> cerebral(EnumBloodTendency.FERRIC, 2, 2)
					.withIronHeartCapacityBonus(4)
					.withModifier(Attributes.ARMOR, "scar_anvil_armor", 2.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.ARMOR_TOUGHNESS, "scar_anvil_at", 1.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_anvil_ms", -0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_crucible = reg("scar_crucible",
			() -> cerebral(EnumBloodTendency.FERRIC, 3, 3)
					.withIronHeartCapacityBonus(6)
					.withModifier(Attributes.ARMOR, "scar_crucible_armor", 3.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.ARMOR_TOUGHNESS, "scar_crucible_at", 2.0, AttributeModifier.Operation.ADD_VALUE)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_crucible_ms", -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withModifier(Attributes.ATTACK_SPEED, "scar_crucible_as", -0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_blood_honed =
			reg("scar_blood_honed", BloodHonedDefinition::new);

	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_shade = reg("scar_shade",
			() -> cerebral(EnumBloodTendency.TENEBRIS, 1, 1)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_shade_ms", 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withModifier(Attributes.ATTACK_DAMAGE, "scar_shade_ad", -1.0, AttributeModifier.Operation.ADD_VALUE));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_moon = reg("scar_moon",
			() -> cerebral(EnumBloodTendency.TENEBRIS, 2, 2)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_moon_ms", 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withModifier(Attributes.ATTACK_DAMAGE, "scar_moon_ad", -2.0, AttributeModifier.Operation.ADD_VALUE));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> scar_eye = reg("scar_eye",
			() -> cerebral(EnumBloodTendency.TENEBRIS, 3, 3)
					.withModifier(Attributes.MOVEMENT_SPEED, "scar_eye_ms", 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.withModifier(Attributes.ATTACK_DAMAGE, "scar_eye_ad", -3.0, AttributeModifier.Operation.ADD_VALUE)
					.withEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 0)
					.withBloodUpkeep(0.3));

	public static final DeferredHolder<ScarDefinition, ScarDefinition> rhizovitta_communis =
			reg("rhizovitta_communis", () -> fungal(EnumBloodTendency.ANIMUS));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> talaromyces_minus =
			reg("talaromyces_minus", () -> fungal(EnumBloodTendency.FERRIC));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> noctifly_agaric =
			reg("noctifly_agaric", () -> fungal(EnumBloodTendency.ANIMUS));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> antiphonomyces_resonans =
			reg("antiphonomyces_resonans", () -> fungal(EnumBloodTendency.DUCTILIS));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> putrivora_resolvens =
			reg("putrivora_resolvens", () -> fungal(EnumBloodTendency.MORTEM));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> oculiflora_reticularis =
			reg("oculiflora_reticularis", () -> fungal(EnumBloodTendency.TENEBRIS));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> cryostroma_perdurans =
			reg("cryostroma_perdurans", () -> fungal(EnumBloodTendency.CONGEATIO));
	public static final DeferredHolder<ScarDefinition, ScarDefinition> saprovitta_vestigium =
			reg("saprovitta_vestigium", () -> fungal(EnumBloodTendency.FLAMMEUS));

	public static List<ScarDefinition> getAllEntries() {
		return new ArrayList<>(SCARS_TYPE_REGISTRY.stream().toList());
	}

	public static ScarDefinition getByName(String name) {
		ResourceLocation id = name.contains(":") ? ResourceLocation.tryParse(name) : Hemomancy.rloc(name);
		if (id == null) {
			return null;
		}
		return SCARS_TYPE_REGISTRY.get(id);
	}

	private static DeferredHolder<ScarDefinition, ScarDefinition> reg(String name, Supplier<ScarDefinition> supplier) {
		return SCARS.register(name, supplier);
	}

	private static ScarDefinition cerebral(EnumBloodTendency tendency, float deepenAmount, int tier) {
		return new ScarDefinition(ScarType.CEREBRAL, tendency, deepenAmount, tier);
	}

	private static ScarDefinition fungal(EnumBloodTendency tendency) {
		return new ScarDefinition(ScarType.FUNGAL, tendency, 1, 1);
	}

	private static class BloodHonedDefinition extends ScarDefinition {
		private static final float BLOOD_DRAIN_PER_LEVEL = 40f;

		private BloodHonedDefinition() {
			super(ScarType.CEREBRAL, EnumBloodTendency.FERRIC, 2.0f, 2);
		}

		@Override
		public void onPlayerAttack(Player player, LivingEntity target) {
			super.onPlayerAttack(player, target);

			ItemStack weapon = player.getMainHandItem();
			if (weapon.isEmpty()) return;

			var registryAccess = player.level().registryAccess();
			int sharpness = getLevel(registryAccess, Enchantments.SHARPNESS, weapon);
			int smite = getLevel(registryAccess, Enchantments.SMITE, weapon);
			int bane = getLevel(registryAccess, Enchantments.BANE_OF_ARTHROPODS, weapon);
			int level = Math.max(sharpness, Math.max(smite, bane));
			if (level <= 0) return;

			HemoCapabilityAccess.getBloodVolume(player).ifPresent(blood -> {
				if (!blood.isActive()) return;
				float drain = level * BLOOD_DRAIN_PER_LEVEL;
				if (blood.getBloodVolume() < drain) return;

				blood.drain(drain);
				target.hurt(player.damageSources().playerAttack(player), level);

				if (player instanceof ServerPlayer sp) {
					PacketHandler.sendToPlayer(sp, new BloodVolumeServerPacket(blood));
				}
			});
		}

		private static int getLevel(net.minecraft.core.HolderLookup.Provider registryAccess,
		                            ResourceKey<Enchantment> key, ItemStack stack) {
			return registryAccess.lookupOrThrow(Registries.ENCHANTMENT)
					.get(key)
					.map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
					.orElse(0);
		}
	}
}
