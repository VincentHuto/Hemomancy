package com.vincenthuto.hemomancy.common.succession;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

/** Malformed professional attacks share the original profession silhouette and animation rig. */
public final class MisbegottenBehavior {
    private MisbegottenBehavior() {}
    public static void tick(ServerLevel level, ProfessionalHarbingerEntity npc) {
        var tag = npc.getPersistentData();
        if (!tag.hasUUID("FraudulentOfficiant")) return;
        var player = level.getServer().getPlayerList().getPlayer(tag.getUUID("FraudulentOfficiant"));
        if (player == null || player.level() != level || !player.isAlive()) {
            int absent = tag.getInt("OfficiantAbsent") + 1; tag.putInt("OfficiantAbsent", absent);
            if (absent > 1200) npc.discard();
            npc.setTarget(null); return;
        }
        tag.putInt("OfficiantAbsent", 0);
        if (!player.isCreative() && !player.isSpectator()) npc.setTarget(player);
        else npc.setTarget(null);
        if (npc.tickCount % 100 == 0) {
            npc.setItemSlot(EquipmentSlot.MAINHAND, player.getMainHandItem().copy()); npc.setDropChance(EquipmentSlot.MAINHAND, 0);
            npc.setShiftKeyDown(player.isShiftKeyDown());
            player.displayClientMessage(Component.translatable("hemomancy.succession.fracture." + SuccessionProfessions.profession(npc)), true);
        }
        if (npc.tickCount % 60 == 0 && npc.getTarget() == player && npc.distanceToSqr(player) < 100 && npc.hasLineOfSight(player)) attack(level, npc, player);
    }
    public static void attack(ServerLevel level, ProfessionalHarbingerEntity npc, LivingEntity target) {
        String role = SuccessionProfessions.profession(npc);
        npc.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        switch (role) {
            case "alchemist" -> { target.hurt(npc.damageSources().mobAttack(npc), 3); target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0)); }
            case "artificer" -> { if (npc.distanceToSqr(target) <= 16) { target.hurt(npc.damageSources().mobAttack(npc), 7); target.knockback(.6, npc.getX()-target.getX(), npc.getZ()-target.getZ()); } }
            case "mnemonist" -> { target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0)); target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0)); }
            case "cicatrix_anchorite" -> target.addEffect(new MobEffectInstance(com.vincenthuto.hemomancy.common.init.EffectInit.counterfeit_scar, 100, 0));
            case "vicar" -> { target.hurt(npc.damageSources().mobAttack(npc), 4); target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 80, 0)); }
            default -> { }
        }
        SuccessionEffects.ritual(level, target.blockPosition(), 4, 20);
    }
}
