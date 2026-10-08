package com.vincenthuto.hemomancy.common.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.summon.*;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CircusDemonstrations {
    public static final String TEACHER = "HemomancyStageTeacher";
    private CircusDemonstrations() {}
    public static boolean isStage(Mob mob) { return mob.getPersistentData().hasUUID(TEACHER); }
    @SubscribeEvent
    public static void beforeTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (event.getEntity() instanceof CircusPerformerEntity teacher && level.getGameTime() % 40 == 0) {
            var lesson = CircusCurriculum.forTeacher(teacher.facultyRoleId()).orElse(null);
            if (lesson == null || !teacher.canTeachNow() || level.getNearestPlayer(teacher, 24) == null) return;
            var stages = level.getEntitiesOfClass(Mob.class, teacher.getBoundingBox().inflate(16),
                    mob -> isStage(mob) && teacher.getUUID().equals(mob.getPersistentData().getUUID(TEACHER)));
            if (!stages.isEmpty()) { stages.stream().skip(1).forEach(Mob::discard); return; }
            Mob body = switch (lesson.summon()) {
                case "veinwing_vulture" -> EntityInit.veinwing_vulture.get().create(level);
                case "marrow_spitter" -> EntityInit.marrow_spitter.get().create(level);
                case "scarlet_mummer" -> EntityInit.scarlet_mummer.get().create(level);
                case "gorebound_hulk" -> EntityInit.gorebound_hulk.get().create(level);
                case "sanguine_hound" -> EntityInit.sanguine_hound.get().create(level);
                case "cinder_bellows" -> EntityInit.cinder_bellows.get().create(level);
                case "mnemonist_puppet" -> EntityInit.mnemonist_puppet.get().create(level);
                default -> null;
            };
            if (body == null) return;
            body.getPersistentData().putUUID(TEACHER, teacher.getUUID());
            body.setNoAi(true); body.setNoGravity(true); body.setInvulnerable(true); body.setPersistenceRequired();
            body.setPos(teacher.getX() + 2.5, teacher.getY(), teacher.getZ());
            PuppeteerSummonDefinitions.byName(lesson.summon()).ifPresent(def -> BoundSummonBehavior.applyTrialStats(body, def));
            level.addFreshEntity(body);
        }
        if (!(event.getEntity() instanceof Mob body) || !isStage(body)) return;
        event.setCanceled(true); // Bypasses combat, upkeep, owner reconciliation, and summon trials together.
        if (!(level.getEntity(body.getPersistentData().getUUID(TEACHER)) instanceof CircusPerformerEntity teacher)
                || !teacher.canTeachNow() || teacher.distanceToSqr(body) > 256) { body.discard(); return; }
        int cycle = (int) (level.getGameTime() % 100);
        double angle = level.getGameTime() * .035;
        boolean flying = body instanceof VeinwingVultureEntity || body instanceof MarrowSpitterEntity;
        Vec3 center = Vec3.atBottomCenterOf(teacher.getHome());
        body.setPos(center.x + 2.5 * (flying ? Math.cos(angle) : 1), center.y + (flying ? 1.5 + .3 * Math.sin(angle) : 0),
                center.z + (flying ? 2.5 * Math.sin(angle) : 1.5));
        body.setYRot((float) Math.toDegrees(-angle)); body.yBodyRot = body.getYRot();
        body.setDeltaMovement(Vec3.ZERO);
        if (body instanceof CinderBellowsEntity bellows) bellows.setDemonstrationCycle(cycle);
        if (body instanceof GoreboundHulkEntity hulk) hulk.setDemonstrationWindup(cycle >= 20 && cycle < 30 ? cycle - 19 : 0);
        if (body instanceof ScarletMummerEntity mummer) mummer.setDemonstrationPerformance(cycle >= 20 && cycle <= 60);
        if (cycle % 20 == 0) body.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        if (body instanceof CinderBellowsEntity && cycle > 20 && cycle <= 60 && cycle % 4 == 0)
            level.sendParticles(ParticleTypes.FLAME, body.getX() + 1, body.getY() + 1.8, body.getZ(), 4, .2, .15, .2, .01);
    }
}
