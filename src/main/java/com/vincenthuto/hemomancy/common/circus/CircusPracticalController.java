package com.vincenthuto.hemomancy.common.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.summon.*;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.summon.PuppeteerCommandMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CircusPracticalController {
    public static final String EXAM = "HemomancyCircusExam";
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final class Session {
        final UUID teacher, crossbar; final String summon; final BlockPos ring; final long expires; final boolean guest;
        final Set<UUID> targets = new HashSet<>(), hitTargets = new HashSet<>();
        UUID body, volleyTarget; int hits, guardTicks; boolean airborne, guardedHit; long volleyStart, breathCycle;
        Session(ServerPlayer player, CircusPerformerEntity teacher, String summon, UUID crossbar, boolean guest) {
            this.teacher = teacher.getUUID(); this.crossbar = crossbar; this.summon = summon;
            this.ring = teacher.getHome(); this.expires = player.level().getGameTime() + 6000; this.guest = guest;
        }
    }
    private CircusPracticalController() {}
    public static boolean begin(ServerPlayer player, CircusPerformerEntity teacher, String summon, boolean guest) {
        if (!teacher.canTeachNow() || !player.isAlive() || player.isSpectator() || player.distanceToSqr(teacher) > 64
                || !CircusCurriculum.forTeacher(teacher.facultyRoleId()).map(lesson -> lesson.summon().equals(summon)
                    && com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getPlayerDegreeNumber(player) >= lesson.degree()).orElse(false)) return false;
        if (!CircusApprenticeshipProgress.knows(player, summon) || (guest && (!CircusApprenticeshipProgress.practicalComplete(player, summon)
                || !CircusSchoolQuests.guestEligible(player)))) { message(player, "unlock_first"); return false; }
        var stack = player.getMainHandItem().getItem() instanceof MarionetteCrossbarItem ? player.getMainHandItem() : player.getOffhandItem();
        if (!(stack.getItem() instanceof MarionetteCrossbarItem) || !MarionetteCrossbarItem.validateControl(stack, player, false)) return false;
        cancel(player);
        var session = new Session(player, teacher, summon, MarionetteCrossbarItem.ensureCrossbarId(stack), guest);
        List<Mob> targets = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Mob target = EntityType.HUSK.create(player.serverLevel());
            if (target == null) { targets.forEach(Mob::discard); return false; }
            target.setPos(teacher.getX() + 4, teacher.getY(), teacher.getZ() + (i == 0 ? -1 : 1));
            if (!player.level().noCollision(target) || player.level().containsAnyLiquid(target.getBoundingBox())) {
                targets.forEach(Mob::discard); message(player, "ring_blocked"); return false;
            }
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(80); target.setHealth(80);
            target.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1);
            target.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.18);
            target.setPersistenceRequired(); target.getPersistentData().putUUID(EXAM, player.getUUID());
            target.setCustomName(Component.translatable("hemomancy.circus.school.target")); target.setCustomNameVisible(true);
            targets.add(target); session.targets.add(target.getUUID());
        }
        SESSIONS.put(player.getUUID(), session);
        for (Mob target : targets) { player.serverLevel().addFreshEntity(target); target.setTarget(player); }
        message(player, "exercise." + summon);
        return true;
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Session session = SESSIONS.get(player.getUUID());
        if (session == null) return;
        if (!player.isAlive() || player.isSpectator() || player.level().getGameTime() > session.expires
                || !session.ring.closerToCenterThan(player.position(), 20)
                || !(player.serverLevel().getEntity(session.teacher) instanceof CircusPerformerEntity teacher) || !teacher.canTeachNow()) {
            cancel(player); message(player, "retry"); return;
        }
        for (Mob body : MarionetteCrossbarItem.activeSummonsForOwner(player)) {
            if (!(body instanceof BoundPuppeteerSummon bound) || !session.summon.equals(bound.hemomancy$getSummonName())
                    || !session.crossbar.equals(bound.hemomancy$getCrossbarUUID())) continue;
            session.body = body.getUUID();
            if (body instanceof VeinwingVultureEntity && !body.onGround() && body.getY() > session.ring.getY() + .5) session.airborne = true;
            if (session.summon.equals("gorebound_hulk")) {
                boolean guard = MarionetteCrossbarItem.findEquippedCrossbar(player, session.crossbar)
                        .map(stack -> MarionetteCrossbarItem.getCommandMode(stack) == PuppeteerCommandMode.GUARD).orElse(false);
                session.guardTicks = guard && session.ring.closerToCenterThan(body.position(), 10) ? session.guardTicks + 1 : 0;
                if (session.guardTicks >= 100 && session.guardedHit) { finish(player); return; }
            }
            if (body instanceof ScarletMummerEntity mummer && mummer.isPerforming()) {
                long diverted = session.targets.stream().map(player.serverLevel()::getEntity)
                        .filter(entity -> entity instanceof Mob mob && mob.getTarget() == body).count();
                if (diverted >= 2) { finish(player); return; }
            }
        }
    }
    @SubscribeEvent public static void cleanup(EntityTickEvent.Pre event) {
        Entity target = event.getEntity();
        if (target.level().isClientSide || !target.getPersistentData().hasUUID(EXAM)) return;
        UUID owner = target.getPersistentData().getUUID(EXAM);
        Session session = SESSIONS.get(owner);
        if (session == null || !session.targets.contains(target.getUUID())
                || !(target.level() instanceof net.minecraft.server.level.ServerLevel level)
                || level.getPlayerByUUID(owner) == null) { target.discard(); return; }
        // Training bodies never award drops or XP, including player kills.
        if (target instanceof LivingEntity living && living.getHealth() <= 0) target.discard();
    }
    @SubscribeEvent public static void damage(LivingDamageEvent.Post event) {
        if (event.getNewDamage() <= 0 || !event.getEntity().getPersistentData().hasUUID(EXAM)) return;
        Entity direct = event.getSource().getDirectEntity();
        Entity attacker = direct instanceof Projectile projectile ? projectile.getOwner() : direct;
        if (!(attacker instanceof Mob body)) return;
        UUID owner = body instanceof SanguineHoundEntity hound && hound.isBloodCur() ? hound.curOwner()
                : body instanceof BoundPuppeteerSummon bound && !bound.hemomancy$isTrialSummon() ? bound.hemomancy$getOwnerUUID() : null;
        if (owner == null || !(body.level() instanceof net.minecraft.server.level.ServerLevel level)
                || !(level.getPlayerByUUID(owner) instanceof ServerPlayer player)) return;
        Session session = SESSIONS.get(owner);
        if (session == null || !session.targets.contains(event.getEntity().getUUID())) return;
        boolean cur = body instanceof SanguineHoundEntity hound && hound.isBloodCur() && session.summon.equals("sanguine_hound") && body instanceof BoundPuppeteerSummon curBound && session.crossbar.equals(curBound.hemomancy$getCrossbarUUID());
        if (!cur && (!(body instanceof BoundPuppeteerSummon bound) || !session.crossbar.equals(bound.hemomancy$getCrossbarUUID())
                || !session.summon.equals(bound.hemomancy$getSummonName()))) return;
        session.body = body.getUUID(); session.hits++;
        if (body instanceof GoreboundHulkEntity && MarionetteCrossbarItem.findEquippedCrossbar(player, session.crossbar).map(stack -> MarionetteCrossbarItem.getCommandMode(stack) == PuppeteerCommandMode.GUARD).orElse(false)) session.guardedHit = true;
        if (cur) { finish(player); return; }
        if (body instanceof MarrowSpitterEntity) {
            long now = level.getGameTime();
            if (session.volleyTarget == null || !session.volleyTarget.equals(event.getEntity().getUUID()) || now - session.volleyStart > 15) {
                session.volleyTarget = event.getEntity().getUUID(); session.volleyStart = now; session.hits = 1;
            }
            if (session.hits >= 3) finish(player);
        }
        if (body instanceof CinderBellowsEntity) {
            long cycle = level.getGameTime() - ((CinderBellowsEntity) body).getBreathCycle();
            if (session.breathCycle != cycle) { session.hitTargets.clear(); session.breathCycle = cycle; }
            session.hitTargets.add(event.getEntity().getUUID());
            if (session.hitTargets.size() >= 2) finish(player);
        }
    }
    public static void replay(MnemonistPuppetEntity body, LivingEntity target, UUID rememberedAttacker) {
        if (!(body.level() instanceof net.minecraft.server.level.ServerLevel level) || body.hemomancy$getOwnerUUID() == null
                || !body.hemomancy$getOwnerUUID().equals(rememberedAttacker) || body.hemomancy$isTrialSummon()) return;
        Session session = SESSIONS.get(body.hemomancy$getOwnerUUID());
        if (session != null && session.summon.equals("mnemonist_puppet") && session.crossbar.equals(body.hemomancy$getCrossbarUUID())
                && session.targets.contains(target.getUUID()) && level.getPlayerByUUID(body.hemomancy$getOwnerUUID()) instanceof ServerPlayer player) finish(player);
    }
    public static void recall(ServerPlayer player, UUID crossbar, String summon) {
        Session session = SESSIONS.get(player.getUUID());
        if (session != null && summon.equals("veinwing_vulture") && session.summon.equals(summon)
                && session.crossbar.equals(crossbar) && session.hits > 0 && session.airborne) finish(player);
    }
    private static void finish(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || !player.isAlive() || player.isSpectator() || !session.ring.closerToCenterThan(player.position(), 20)) return;
        if (session.guest) CircusSchoolQuests.guestReward(player);
        else CircusApprenticeshipProgress.completePractical(player, session.summon);
        cancel(player); CircusPlayerProgress.sync(player, true); message(player, "passed");
    }
    @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cancel(player);
    }
    @SubscribeEvent public static void changeDimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cancel(player);
    }
    @SubscribeEvent public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) { SESSIONS.clear(); }
    @SubscribeEvent public static void drops(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {
        if (event.getEntity().getPersistentData().hasUUID(EXAM) || event.getEntity() instanceof Mob mob && CircusDemonstrations.isStage(mob)) event.setCanceled(true);
    }
    @SubscribeEvent public static void experience(net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent event) {
        if (event.getEntity().getPersistentData().hasUUID(EXAM) || event.getEntity() instanceof Mob mob && CircusDemonstrations.isStage(mob)) event.setDroppedExperience(0);
    }

    public static void cancel(ServerPlayer player) {
        Session session = SESSIONS.remove(player.getUUID());
        if (session != null) for (var level : player.server.getAllLevels())
            for (UUID id : session.targets) { Entity target = level.getEntity(id); if (target != null) target.discard(); }
    }
    private static void message(ServerPlayer player, String key) { player.displayClientMessage(Component.translatable("hemomancy.circus.school." + key), false); }
}
