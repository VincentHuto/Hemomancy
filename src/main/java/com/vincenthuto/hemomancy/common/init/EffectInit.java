package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.effect.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class EffectInit {
    public static final DeferredRegister<Potion> POTION_TYPES = DeferredRegister.create(Registries.POTION,
            Hemomancy.MOD_ID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT,
            Hemomancy.MOD_ID);
    private static final Set<UUID> MNEMONIC_WHISPERS_REDRINKS = ConcurrentHashMap.newKeySet();

    public static final DeferredHolder<MobEffect, MobEffect> fungal_elytra = EFFECTS.register("fungal_elytra",
            () -> new ElytraEffect()
                    .addAttributeModifier(AttributeInit.getFlightAttribute(), ResourceLocation.fromNamespaceAndPath("hemomancy", "elytra_fall_flying"),
                            1.0D, AttributeModifier.Operation.ADD_VALUE));

    public static final DeferredHolder<MobEffect, MobEffect> blood_binding = EFFECTS.register("blood_binding",
            () -> new BloodBindingEffect(MobEffectCategory.HARMFUL, 3735555));
    public static final DeferredHolder<Potion, Potion> potion_of_blood_binding = POTION_TYPES.register(
            "potion_of_blood_binding",
            () -> new Potion("potion_of_blood_binding", new MobEffectInstance(blood_binding, 1000, 3)));

    public static final DeferredHolder<MobEffect, MobEffect> blood_loss = EFFECTS.register("blood_loss",
            () -> new BloodLossEffect(MobEffectCategory.HARMFUL, 11075587).addAttributeModifier(
                    Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("hemomancy", "blood_loss_movement_speed"), -0.15F,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<Potion, Potion> potion_of_blood_loss = POTION_TYPES.register("potion_of_blood_loss",
            () -> new Potion("potion_of_blood_loss", new MobEffectInstance(blood_loss, 1000, 3)));

    public static final DeferredHolder<MobEffect, MobEffect> blood_drunkenness = EFFECTS.register("blood_drunkenness",
            () -> new BloodDrunkennessEffect(MobEffectCategory.HARMFUL, 0x6E0E1C));

    public static final DeferredHolder<MobEffect, MobEffect> blood_rush = EFFECTS.register("blood_rush",
            () -> new BloodRushEffect(MobEffectCategory.BENEFICIAL, 16711680)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("hemomancy", "blood_rush_movement_speed"), 0.2F,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_SPEED, ResourceLocation.fromNamespaceAndPath("hemomancy", "blood_rush_attack_speed"), 0.1F,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.fromNamespaceAndPath("hemomancy", "blood_rush_attack_damage"), 0.0D,
                            AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<Potion, Potion> potion_of_blood_rush = POTION_TYPES.register("potion_of_blood_rush",
            () -> new Potion("potion_of_blood_rush", new MobEffectInstance(blood_rush, 1000, 3)));

    public static final DeferredHolder<MobEffect, MobEffect> hemolysis = EFFECTS.register("hemolysis",
            () -> new HemolysisEffect(MobEffectCategory.NEUTRAL, 15186121));

    public static final DeferredHolder<Potion, Potion> potion_of_hemolysis = POTION_TYPES.register("potion_of_hemolysis",
            () -> new Potion("potion_of_hemolysis", new MobEffectInstance(hemolysis, 1000, 3)));

    public static final DeferredHolder<MobEffect, MobEffect> sanguine_fertility = EFFECTS.register("sanguine_fertility",
            () -> new SanguineFertilityEffect(MobEffectCategory.BENEFICIAL, 0xCC3344));
    public static final DeferredHolder<Potion, Potion> potion_of_sanguine_fertility = POTION_TYPES.register(
            "potion_of_sanguine_fertility",
            () -> new Potion("potion_of_sanguine_fertility",
                    new MobEffectInstance(sanguine_fertility, 1200, 0)));

    public static final DeferredHolder<MobEffect, MobEffect> arachnid_anastomosis = EFFECTS.register("arachnid_anastomosis",
            () -> new ArachnidAnastomosisEffect(MobEffectCategory.BENEFICIAL, 0x8B0000));
    public static final DeferredHolder<Potion, Potion> potion_of_arachnid_anastomosis = POTION_TYPES.register(
            "potion_of_arachnid_anastomosis",
            () -> new Potion("potion_of_arachnid_anastomosis",
                    new MobEffectInstance(arachnid_anastomosis, 1200, 0)));

    public static final DeferredHolder<MobEffect, MobEffect> mycorrhizal_mending = EFFECTS.register("mycorrhizal_mending",
            () -> new MycorrhizalMendingEffect(MobEffectCategory.BENEFICIAL, 0x7B4F2A));
    public static final DeferredHolder<Potion, Potion> potion_of_mycorrhizal_mending = POTION_TYPES.register(
            "potion_of_mycorrhizal_mending",
            () -> new Potion("potion_of_mycorrhizal_mending",
                    new MobEffectInstance(mycorrhizal_mending, 1200, 0)));

    public static final DeferredHolder<MobEffect, MobEffect> sanguine_siphon = EFFECTS.register("sanguine_siphon",
            () -> new SanguineSiphonEffect(MobEffectCategory.BENEFICIAL, 0x8B0000));
    public static final DeferredHolder<Potion, Potion> potion_of_sanguine_siphon = POTION_TYPES.register(
            "potion_of_sanguine_siphon",
            () -> new Potion("potion_of_sanguine_siphon",
                    new MobEffectInstance(sanguine_siphon, 1200, 0)));

    public static final DeferredHolder<MobEffect, MobEffect> chitinous_bulwark = EFFECTS.register("chitinous_bulwark",
            () -> new ChitinousBulwarkEffect(MobEffectCategory.BENEFICIAL, 0x556B2F)
                    .addAttributeModifier(Attributes.ARMOR_TOUGHNESS, ResourceLocation.fromNamespaceAndPath("hemomancy", "chitinous_bulwark_armor_toughness"),
                            4.0D, AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<Potion, Potion> potion_of_chitinous_bulwark = POTION_TYPES.register(
            "potion_of_chitinous_bulwark",
            () -> new Potion("potion_of_chitinous_bulwark",
                    new MobEffectInstance(chitinous_bulwark, 1200, 0)));

    public static final DeferredHolder<MobEffect, MobEffect> serpentine_guile = EFFECTS.register("serpentine_guile",
            () -> new SerpentineGuileEffect(MobEffectCategory.BENEFICIAL, 0x2E8B57)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("hemomancy", "serpentine_guile_movement_speed"),
                            0.15F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_SPEED, ResourceLocation.fromNamespaceAndPath("hemomancy", "serpentine_guile_attack_speed"),
                            0.1F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<Potion, Potion> potion_of_serpentine_guile = POTION_TYPES.register(
            "potion_of_serpentine_guile",
            () -> new Potion("potion_of_serpentine_guile",
                    new MobEffectInstance(serpentine_guile, 1200, 0)));

    public static final DeferredHolder<MobEffect, MobEffect> verminous_aura = EFFECTS.register("verminous_aura",
            () -> new VerminousAuraEffect(MobEffectCategory.BENEFICIAL, 0x4A3728));
    public static final DeferredHolder<Potion, Potion> potion_of_verminous_aura = POTION_TYPES.register(
            "potion_of_verminous_aura",
            () -> new Potion("potion_of_verminous_aura",
                    new MobEffectInstance(verminous_aura, 1200, 0)));

    // New Morphling Effects
    public static final DeferredHolder<MobEffect, MobEffect> luminous_dissipation = EFFECTS.register("luminous_dissipation",
            () -> new LuminousDissipationEffect(MobEffectCategory.BENEFICIAL, 0xFFFFAA)
                    .addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, ResourceLocation.fromNamespaceAndPath("hemomancy", "luminous_dissipation_knockback_resistance"),
                            0.1D, AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<Potion, Potion> potion_of_luminous_dissipation = POTION_TYPES.register(
            "potion_of_luminous_dissipation",
            () -> new Potion("potion_of_luminous_dissipation",
                    new MobEffectInstance(luminous_dissipation, 1200, 0)));

    public static final DeferredHolder<MobEffect, MobEffect> hemorrhagic_venom = EFFECTS.register("hemorrhagic_venom",
            () -> new HemorrhagicVenomEffect(MobEffectCategory.BENEFICIAL, 0x660033));
    public static final DeferredHolder<Potion, Potion> potion_of_hemorrhagic_venom = POTION_TYPES.register(
            "potion_of_hemorrhagic_venom",
            () -> new Potion("potion_of_hemorrhagic_venom",
                    new MobEffectInstance(hemorrhagic_venom, 1200, 0)));

    public static final DeferredHolder<MobEffect, MobEffect> venomous_resilience = EFFECTS.register("venomous_resilience",
            () -> new VenomousResilienceEffect(MobEffectCategory.BENEFICIAL, 0x336B87)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("hemomancy", "venomous_resilience_movement_speed"),
                            0.05F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<Potion, Potion> potion_of_venomous_resilience = POTION_TYPES.register(
            "potion_of_venomous_resilience",
            () -> new Potion("potion_of_venomous_resilience",
                    new MobEffectInstance(venomous_resilience, 1200, 0)));

    public static final DeferredHolder<MobEffect, MobEffect> echoic_perception = EFFECTS.register("echoic_perception",
            () -> new EchoicPerceptionEffect(MobEffectCategory.BENEFICIAL, 0x2A0A3C));
    public static final DeferredHolder<Potion, Potion> potion_of_echoic_perception = POTION_TYPES.register(
            "potion_of_echoic_perception",
            () -> new Potion("potion_of_echoic_perception",
                    new MobEffectInstance(echoic_perception, 1200, 0)));

    public static final DeferredHolder<MobEffect, MobEffect> spined_barricade = EFFECTS.register("spined_barricade",
            () -> new SpinedBarricadeEffect(MobEffectCategory.BENEFICIAL, 0x1A8A9F)
                    .addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath("hemomancy", "spined_barricade_armor"),
                            2.0D, AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<Potion, Potion> potion_of_spined_barricade = POTION_TYPES.register(
            "potion_of_spined_barricade",
            () -> new Potion("potion_of_spined_barricade",
                    new MobEffectInstance(spined_barricade, 1200, 0)));

    public static final DeferredHolder<MobEffect, MobEffect> burrowers_instinct = EFFECTS.register("burrowers_instinct",
            () -> new BurrowersInstinctEffect(MobEffectCategory.BENEFICIAL, 0x8B6914)
                    .addAttributeModifier(Attributes.ATTACK_SPEED, ResourceLocation.fromNamespaceAndPath("hemomancy", "burrowers_instinct_attack_speed"),
                            0.15F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<Potion, Potion> potion_of_burrowers_instinct = POTION_TYPES.register(
            "potion_of_burrowers_instinct",
            () -> new Potion("potion_of_burrowers_instinct",
                    new MobEffectInstance(burrowers_instinct, 1200, 0)));

    // Unstained Path Effects
    public static final DeferredHolder<MobEffect, MobEffect> silver_ward = EFFECTS.register("silver_ward",
            () -> new SilverWardEffect(MobEffectCategory.BENEFICIAL, 0xC0C0C0)
                    .addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath("hemomancy", "silver_ward_armor"),
                            4.0D, AttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, ResourceLocation.fromNamespaceAndPath("hemomancy", "silver_ward_knockback_resistance"),
                            0.2D, AttributeModifier.Operation.ADD_VALUE));

    public static final DeferredHolder<MobEffect, MobEffect> verdigris_aura = EFFECTS.register("verdigris_aura",
            () -> new VerdigrisAuraEffect(MobEffectCategory.BENEFICIAL, 0x4A8B6F));

    // MnA Cross-Mod Combo Effects
    public static final DeferredHolder<MobEffect, MobEffect> arcane_resonance = EFFECTS.register("arcane_resonance",
            () -> new ArcaneResonanceEffect(MobEffectCategory.BENEFICIAL, 0x8B0020));

    public static final DeferredHolder<MobEffect, MobEffect> sanguine_clarity = EFFECTS.register("sanguine_clarity",
            () -> new SanguineClarityEffect(MobEffectCategory.BENEFICIAL, 0x4020A0));

    public static final DeferredHolder<MobEffect, MobEffect> sporitic_resonance = EFFECTS.register("sporitic_resonance",
            () -> new SporiticResonanceEffect(MobEffectCategory.BENEFICIAL, 0x8F3A54));

    // Neurotic Tendency Effect
    public static final DeferredHolder<MobEffect, MobEffect> neural_overload = EFFECTS.register("neural_overload",
            () -> new NeuralOverloadEffect(MobEffectCategory.HARMFUL, 0x7DF9FF)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("hemomancy", "neural_overload_movement_speed"),
                            -0.15F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    // Saintly Enzyme System â€” Marked by Canon (persistent debuff from failed extraction)
    public static final DeferredHolder<MobEffect, MobEffect> marked_by_canon = EFFECTS.register("marked_by_canon",
            () -> new MarkedByCanonEffect(MobEffectCategory.HARMFUL, 0x8B0000));

    // Hollow Vessel â€” Hemophagy (healing reduction from Empty Pulse)
    public static final DeferredHolder<MobEffect, MobEffect> hemophagy = EFFECTS.register("hemophagy",
            () -> new HemophagyEffect(MobEffectCategory.HARMFUL, 0x4B0000));

    // Inner Trial â€” Hematic Strain (40% max HP reduction while in trial chamber)
    public static final DeferredHolder<MobEffect, MobEffect> hematic_strain = EFFECTS.register("hematic_strain",
            () -> new HematicStrainEffect(MobEffectCategory.HARMFUL, 0x660000));

    public static final DeferredHolder<MobEffect, MobEffect> morphic_strain = EFFECTS.register("morphic_strain",
            () -> new MorphicStrainEffect(MobEffectCategory.HARMFUL, 0x5A2A65));

    public static final DeferredHolder<MobEffect, MobEffect> chummed_waters = EFFECTS.register("chummed_waters",
            () -> new ChummedWatersEffect(MobEffectCategory.BENEFICIAL, 0x7A1E2A));

    public static final DeferredHolder<MobEffect, MobEffect> constricted = EFFECTS.register("constricted",
            () -> new ConstrictedEffect(MobEffectCategory.HARMFUL, 0x6D4731));

    public static final DeferredHolder<MobEffect, MobEffect> mnemonic_candle_aura = EFFECTS.register("mnemonic_candle_aura",
            () -> new MnemonicCandleAuraEffect(MobEffectCategory.BENEFICIAL, 0xB45A3C));

    public static final DeferredHolder<MobEffect, MobEffect> mnemonic_whispers = EFFECTS.register("mnemonic_whispers",
            () -> new MnemonicWhispersEffect(MobEffectCategory.BENEFICIAL, 0x7A5C91));
    public static final DeferredHolder<Potion, Potion> potion_of_mnemonic_whispers = POTION_TYPES.register(
            "potion_of_mnemonic_whispers",
            () -> new Potion("potion_of_mnemonic_whispers",
                    new MobEffectInstance(mnemonic_whispers, MnemonicPotionRules.WHISPERS_DURATION_TICKS, 0)));

    public static final DeferredHolder<MobEffect, MobEffect> mnemonic_screams = EFFECTS.register("mnemonic_screams",
            () -> new MnemonicScreamsEffect(MobEffectCategory.HARMFUL, 0x3F102B));

    public static final DeferredHolder<MobEffect, MobEffect> monolithic_dislocation = EFFECTS.register("monolithic_dislocation",
            () -> new MonolithicDislocationEffect(MobEffectCategory.HARMFUL, 0x1D2226));

    @SubscribeEvent
    public static void setupPotionRecipes(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public static void registerEnzymeBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.AWKWARD, ItemInit.vivacious_enzyme.get(), Potions.REGENERATION);
        event.getBuilder().addMix(Potions.AWKWARD, ItemInit.fervent_enzyme.get(), Potions.FIRE_RESISTANCE);
        event.getBuilder().addMix(Potions.AWKWARD, ItemInit.neurotic_enzyme.get(), Potions.SWIFTNESS);
        event.getBuilder().addMix(Potions.AWKWARD, ItemInit.incandescent_enzyme.get(), Potions.NIGHT_VISION);
        event.getBuilder().addMix(Potions.AWKWARD, ItemInit.ruinous_enzyme.get(), Potions.POISON);
        event.getBuilder().addMix(Potions.AWKWARD, ItemInit.frigid_enzyme.get(), Potions.SLOWNESS);
        event.getBuilder().addMix(Potions.AWKWARD, ItemInit.ferric_enzyme.get(), Potions.STRENGTH);
        event.getBuilder().addMix(Potions.NIGHT_VISION, ItemInit.umbral_enzyme.get(), Potions.INVISIBILITY);
        event.getBuilder().addMix(Potions.AWKWARD, ItemInit.mnemonic_ambergris.get(), potion_of_mnemonic_whispers);
    }

    @SubscribeEvent
    public static void onMnemonicPotionDrinkStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean drinkingWhispers = isMnemonicWhispersPotion(event.getItem());
        if (MnemonicPotionRules.shouldApplyScreamsOnWhispersDrink(drinkingWhispers,
                player.hasEffect(mnemonic_whispers))) {
            MNEMONIC_WHISPERS_REDRINKS.add(player.getUUID());
        } else {
            MNEMONIC_WHISPERS_REDRINKS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onMnemonicPotionDrinkFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !isMnemonicWhispersPotion(event.getItem())) {
            return;
        }
        if (MNEMONIC_WHISPERS_REDRINKS.remove(player.getUUID())) {
            player.removeEffect(mnemonic_whispers);
            player.addEffect(new MobEffectInstance(mnemonic_screams,
                    MnemonicPotionRules.SCREAMS_DURATION_TICKS, 0, false, true, true));
        }
    }

    private static boolean isMnemonicWhispersPotion(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.is(potion_of_mnemonic_whispers);
    }

}
