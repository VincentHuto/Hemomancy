package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.damage.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Uses the specimen's authored tendencies, never the staff or the currently held weapon. */
public final class MorphlingCombat {
    private MorphlingCombat() {}

    public static SchoolDamage.Scope scope(IMorphling morphling, Player player, ItemStack stack,
            DamageSource trigger) {
        SchoolHitContext active = SchoolDamage.current();
        if (active != null && active.ability().equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                && player.getUUID().equals(active.owner())) return SchoolDamage.scope(active, player);
        SchoolHitContext hit = SchoolHitContext.direct(BuiltInRegistries.ITEM.getKey(stack.getItem()),
                morphling.getPreferredTendency(), morphling.getSecondaryTendency(), player);
        if (trigger != null) {
            var parent = trigger instanceof SchoolDamageSource school ? school.context() : SchoolDamage.current();
            if (parent != null) hit = new SchoolHitContext(hit.ability(), hit.primary(), hit.secondary(), hit.owner(),
                    hit.direct(), parent.rootAttack(), hit.kind(), hit.role(), 1, 0, 1);
            hit = hit.child(SchoolHitContext.Kind.REACTION);
        }
        return SchoolDamage.scope(hit, player);
    }

    public static boolean isFollowUp(DamageSource source) {
        return source instanceof SchoolDamageSource school && school.context().kind() != SchoolHitContext.Kind.DIRECT;
    }

    public static void tick(IMorphling morphling, Player player, ItemStack stack) {
        try (var scope = scope(morphling, player, stack, null)) {
            morphling.onEquippedTick(player, stack);
        }
    }

    public static boolean afflict(IMorphling morphling, Player caster, LivingEntity target, int ticks) {
        SchoolHitContext hit = SchoolDamage.current();
        if (hit == null) hit = SchoolHitContext.direct(BuiltInRegistries.ITEM.getKey(
                (net.minecraft.world.item.Item)morphling), morphling.getPreferredTendency(), morphling.getSecondaryTendency(), caster);
        SchoolState state = SchoolStates.primaryState(morphling.getPreferredTendency());
        if (SchoolStates.data(target).hasApplied(hit.rootAttack(), state)) {
            SchoolStates.refresh(target, state, ticks);
            return false;
        }
        boolean applied = SchoolStates.apply(hit, caster, target, state, ticks, 1);
        if (applied) SchoolStates.data(target).recordApplication(hit.rootAttack(), state, target.level().getGameTime());
        return applied;
    }
}
