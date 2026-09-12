package com.vincenthuto.hemomancy.common.damage;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class SchoolAwareness {
    private record Investigation(Vec3 position, long until) {}
    private static final Map<Mob, Investigation> LOST = new WeakHashMap<>();
    private SchoolAwareness() {}

    public static boolean canPerceive(LivingEntity observer, LivingEntity target) {
        return SchoolStateRules.canPerceive(SchoolStates.has(observer, SchoolState.OBSCURED),
                SchoolStates.has(target, SchoolState.VEILED), SchoolStates.has(target, SchoolState.ILLUMINATED),
                ManipulationReactiveEvents.isBoss(observer), observer.distanceToSqr(target));
    }

    @SubscribeEvent public static void target(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target != null && !canPerceive(event.getEntity(), target))
            event.setNewAboutToBeSetTarget(null);
    }

    @SubscribeEvent public static void tick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide || ManipulationReactiveEvents.isBoss(mob)) return;
        LivingEntity target = mob.getTarget();
        if (target == null && mob.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET,
                net.minecraft.world.entity.ai.memory.MemoryStatus.REGISTERED))
            target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target != null && !canPerceive(mob, target)) {
            // Store a position, never the entity: investigation cannot follow an unseen moving target.
            Investigation previous = LOST.get(mob);
            Vec3 lastKnown = previous == null ? target.position() : previous.position;
            if (previous == null) LOST.put(mob, new Investigation(lastKnown, mob.level().getGameTime() + 60));
            mob.stopUsingItem();
            mob.setTarget(null);
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
            mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            mob.getNavigation().stop();
            mob.getNavigation().moveTo(lastKnown.x, lastKnown.y, lastKnown.z, 1);
            mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(lastKnown, 1, 1));
        }
        Investigation investigation = LOST.get(mob);
        if (investigation != null && (mob.level().getGameTime() >= investigation.until || mob.getTarget() != null)) {
            LOST.remove(mob);
        }
    }
}
