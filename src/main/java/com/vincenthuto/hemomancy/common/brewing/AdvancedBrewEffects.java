package com.vincenthuto.hemomancy.common.brewing;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class AdvancedBrewEffects {
    private static final String POISE_END = "hemomancyFerricPoiseEnd";
    private static final String SURGE_DEADLINES = "hemomancyNerveSurgeDeadlines";
    private static final String REPRISAL_END = "hemomancyFerventReprisalEnd";
    private static final String REPRISAL_NEXT = "hemomancyFerventReprisalNext";
    private static final ResourceLocation POISE_MODIFIER = Hemomancy.rloc("ferric_poise_knockback");

    private AdvancedBrewEffects() {}

    @SubscribeEvent public static void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (AdvancedPotionUse.isEmptyVessel(event.getItem())) event.setCanceled(true);
    }

    @SubscribeEvent public static void onDrink(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AdvancedBrewData data = event.getItem().get(DataComponentInit.ADVANCED_BREW.get());
        if (data == null) return;
        long now = player.serverLevel().getGameTime();
        CompoundTag persistent = player.getPersistentData();
        switch (data.attachment()) {
            case "ferric_poise" -> {
                persistent.putLong(POISE_END, now + 1800);
                updatePoise(player, true);
            }
            case "nerve_surge" -> {
                long[] old = persistent.getLongArray(SURGE_DEADLINES);
                long[] deadlines = new long[Math.min(old.length, 15) + 1];
                System.arraycopy(old, 0, deadlines, 0, deadlines.length - 1);
                deadlines[deadlines.length - 1] = now + 500;
                persistent.putLongArray(SURGE_DEADLINES, deadlines);
            }
            case "fervent_reprisal" -> persistent.putLong(REPRISAL_END, now + 1800);
            default -> { }
        }
    }

    @SubscribeEvent public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag persistent = player.getPersistentData();
        long now = player.serverLevel().getGameTime();
        boolean poise = persistent.getLong(POISE_END) > now && player.hasEffect(MobEffects.DAMAGE_BOOST);
        updatePoise(player, poise);
        if (!poise) persistent.remove(POISE_END);

        long[] deadlines = persistent.getLongArray(SURGE_DEADLINES);
        if (deadlines.length > 0) {
            List<Long> remaining = new ArrayList<>();
            for (long due : deadlines) {
                if (due <= now || !player.hasEffect(MobEffects.MOVEMENT_SPEED))
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 0));
                else remaining.add(due);
            }
            persistent.putLongArray(SURGE_DEADLINES, remaining.stream().mapToLong(Long::longValue).toArray());
        }
        if (persistent.getLong(REPRISAL_END) <= now) persistent.remove(REPRISAL_END);
    }

    @SubscribeEvent public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag persistent = player.getPersistentData();
        persistent.remove(SURGE_DEADLINES);
        persistent.remove(POISE_END);
        persistent.remove(REPRISAL_END);
        updatePoise(player, false);
    }

    @SubscribeEvent public static void onDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer wearer) || event.getNewDamage() <= 0) return;
        long now = wearer.serverLevel().getGameTime();
        CompoundTag persistent = wearer.getPersistentData();
        if (persistent.getLong(REPRISAL_END) <= now || persistent.getLong(REPRISAL_NEXT) > now
                || event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                || event.getSource().is(DamageTypes.THORNS)) return;
        if (!(event.getSource().getDirectEntity() instanceof LivingEntity attacker)
                || event.getSource().getEntity() != attacker || attacker == wearer || !attacker.isAlive()) return;
        attacker.igniteForSeconds(2);
        com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.burst(
                wearer.serverLevel(), com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.IGNITION,
                attacker.position(), attacker.position(), 0.65D, 10);
        persistent.putLong(REPRISAL_NEXT, now + 100);
    }

    private static void updatePoise(ServerPlayer player, boolean active) {
        var resistance = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (resistance == null) return;
        if (!active) resistance.removeModifier(POISE_MODIFIER);
        else if (resistance.getModifier(POISE_MODIFIER) == null)
            resistance.addTransientModifier(new AttributeModifier(POISE_MODIFIER, 0.20,
                    AttributeModifier.Operation.ADD_VALUE));
    }
}
