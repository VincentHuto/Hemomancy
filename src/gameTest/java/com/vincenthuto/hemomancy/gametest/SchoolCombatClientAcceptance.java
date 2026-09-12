package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.damage.*;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Explicitly opt-in, disposable server only. Never enabled in an ordinary client/world. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class SchoolCombatClientAcceptance {
    private static int tick, waiting, lateTicks;
    private static Mob target;
    private static boolean initialized;
    private static final int SCENE_TICKS = 180;

    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.schoolAcceptance")) return;
        var server = event.getServer();
        if (++waiting > 18000) { server.halt(false); return; }
        var caster = server.getPlayerList().getPlayerByName("SchoolCaster");
        var observer = server.getPlayerList().getPlayerByName("SchoolObserver");
        if (caster == null || observer == null) return;
        if (!initialized) {
            initialized = true;
            tick = Integer.getInteger("hemomancy.schoolAcceptanceStartScene", 0) * SCENE_TICKS;
            ManipulationInit.MANIPS.getEntries().stream().map(java.util.function.Supplier::get)
                    .sorted(java.util.Comparator.comparing(BloodManipulation::getName)).forEach(manipulation ->
                        Hemomancy.LOGGER.info("SCHOOL_SOURCE {} {} {} {} entity={} drudge={}",
                            manipulation.getName(), manipulation.getTend(), manipulation.getSecondaryTend(),
                            SchoolDamage.context(manipulation, caster).role(),
                            EntityManipulationEffects.isSupported(manipulation), manipulation.getDrudgeAction().filter(action -> action != DrudgeAction.DRUDGE_UNSUPPORTED).isPresent()));
            var level = caster.serverLevel();
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
            level.setDayTime(6000);
            level.setWeatherParameters(100000, 0, false, false);
            for (int x = -14; x <= 14; x++) for (int z = -8; z <= 18; z++) {
                BlockPos floor = new BlockPos(x, 99, z);
                level.setBlockAndUpdate(floor, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                for (int y = 1; y <= 8; y++) level.setBlockAndUpdate(floor.above(y), Blocks.AIR.defaultBlockState());
            }
            target = EntityType.HUSK.create(level);
            target.setPos(.5, 100, 6.5);
            target.setNoAi(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(80);
            target.setHealth(80);
            target.setPersistenceRequired();
            target.setCustomName(Component.literal("School acceptance target"));
            level.addFreshEntity(target);
            for (var player : server.getPlayerList().getPlayers()) position(player);
            Hemomancy.LOGGER.info("SCHOOL_ACCEPTANCE two real clients connected");
        }
        int scene = tick / SCENE_TICKS, phase = tick++ % SCENE_TICKS;
        var late = server.getPlayerList().getPlayerByName("SchoolLate");
        if (late != null && late.getY() != 100) position(late);
        if (scene >= 11) {
            if (Integer.getInteger("hemomancy.schoolAcceptanceStartScene", 0) == 10) {
                ManipulationChannelManager.stop(caster, false);
                Hemomancy.LOGGER.info("SCHOOL_ACCEPTANCE tether-only complete");
                for (var player : server.getPlayerList().getPlayers()) message(player, "SCHOOL_DONE");
                server.halt(false);
                return;
            }
            // Keep an active state for a genuinely late observer's initial tracking snapshot.
            if (scene == 11 && phase == 0) {
                ManipulationChannelManager.stop(caster, false);
                target.setNoAi(true);
                target.setPos(.5, 100, 6.5);
                SchoolStates.apply(caster, target, SchoolState.PRESSURE, 2400);
                SchoolStates.apply(caster, target, SchoolState.NECROSIS, 2400);
                SchoolStates.data(target).get(SchoolState.NECROSIS).stored = 3;
                SchoolStates.data(target).changed();
                Hemomancy.LOGGER.info("SCHOOL_ACCEPTANCE waiting for late observer");
            }
            if (late == null) return;
            int trackedTime = ++lateTicks;
            if (trackedTime == 80) {
                for (var player : server.getPlayerList().getPlayers()) message(player, "SCHOOL_SHOT late " + target.getId());
            }
            if (trackedTime == 120) for (SchoolState state : SchoolState.values()) SchoolStates.clear(target, state);
            if (trackedTime == 150) for (var player : server.getPlayerList().getPlayers()) message(player, "SCHOOL_SHOT cleanup " + target.getId());
            if (trackedTime == 180) {
                Hemomancy.LOGGER.info("SCHOOL_ACCEPTANCE complete; inspect all three client logs and frames");
                for (var player : server.getPlayerList().getPlayers()) message(player, "SCHOOL_DONE");
                server.halt(false);
            }
            return;
        }
        if (phase == 0) {
            ManipulationChannelManager.stop(caster, false);
            target.setNoAi(true);
            target.setPos(.5, 100, 6.5);
            target.setHealth(80);
            for (var player : server.getPlayerList().getPlayers()) { position(player); player.removeAllEffects(); }
            for (SchoolState state : SchoolState.values()) SchoolStates.clear(target, state);
            if (scene < 9) {
                SchoolState state = SchoolState.values()[scene];
                SchoolStates.apply(caster, target, state, 170);
                if (state == SchoolState.PRESSURE) {
                    SchoolStates.apply(caster, target, state, 170);
                    SchoolStates.apply(caster, target, state, 170);
                }
                if (state == SchoolState.RIME) SchoolStates.setLevels(target, state, 3);
                if (state == SchoolState.NECROSIS) { SchoolStates.data(target).get(state).stored = 6; SchoolStates.data(target).changed(); }
                if (state == SchoolState.OBSCURED) SchoolStates.apply(null, observer, state, 170);
            } else if (scene == 9) {
                SchoolStates.apply(caster, target, SchoolState.VEILED, 170);
                SchoolStates.apply(caster, target, SchoolState.ILLUMINATED, 170);
            } else {
                target.setNoAi(false);
                var known = HemoCapabilityAccess.requireKnownManipulations(caster);
                known.getKnownManips().put(ManipulationInit.blood_binding.get(), new ManipLevel(3, 0));
                known.getKnownManips().put(ManipulationInit.sanguine_marionette.get(), new ManipLevel(3, 0));
                known.setEquippedManipNames(java.util.List.of("sanguine_marionette"));
                known.setSelectedManip(ManipulationInit.sanguine_marionette.get());
                var volume = HemoCapabilityAccess.requireBloodVolume(caster);
                volume.setActive(true); volume.setBloodVolume(3000);
                ManipulationChannelManager.start(caster);
                target.setOnGround(true);
            }
            Hemomancy.LOGGER.info("SCHOOL_ACCEPTANCE scene {} target {}", scene, target.getId());
        }
        if (scene == 10 && phase == 5) Hemomancy.LOGGER.info("SCHOOL_MOVE accepted={}",
                HematicCommandManager.orderMove(caster, new BlockPos(6, 100, 7)));
        if (scene == 10 && (phase == 30 || phase == 90)) Hemomancy.LOGGER.info("SCHOOL_MOVE phase={} position={}", phase, target.position());
        if (scene == 5 && phase % 8 == 0) SchoolStates.apply(caster, target, SchoolState.DISRUPTED, 10);
        if (phase == 60 || phase == 90 || scene == 10 && phase >= 30 && phase < 55)
            for (var player : server.getPlayerList().getPlayers()) message(player, "SCHOOL_SHOT " + scene + "-" + phase + " " + target.getId());
        if (phase == 110 && scene < 5) {
            String ability = switch (scene) { case 0 -> "blood_aneurysm"; case 1 -> "funeral_bell"; case 3 -> "vitric_combustion"; default -> "ferric_strike"; };
            var tendency = scene == 0 ? SchoolStates.school(SchoolState.PRESSURE) : scene == 1 ? SchoolStates.school(SchoolState.NECROSIS)
                    : scene == 3 ? SchoolStates.school(SchoolState.SEARING) : SchoolStates.school(SchoolState.LODESTONE);
            target.invulnerableTime = 0;
            target.hurt(SchoolDamage.attributed(target.damageSources().magic(), SchoolHitContext.direct(Hemomancy.rloc(ability), tendency, null, caster), caster), 2);
        }
        if (phase == 114) for (var player : server.getPlayerList().getPlayers()) message(player, "SCHOOL_SHOT payoff-" + scene + " " + target.getId());
    }

    private static void position(ServerPlayer player) {
        boolean caster = player.getGameProfile().getName().equals("SchoolCaster");
        player.teleportTo(player.serverLevel(), caster ? .5 : 4.5, 100, caster ? .5 : 4.5, caster ? 0 : 63.4f, 0);
        player.setGameMode(GameType.SURVIVAL);
        player.setHealth(20);
    }
    private static void message(ServerPlayer player, String message) { player.sendSystemMessage(Component.literal(message)); }

    @EventBusSubscriber(modid = Hemomancy.MOD_ID, value = net.neoforged.api.distmarker.Dist.CLIENT)
    public static final class Client {
        @SubscribeEvent public static void message(net.neoforged.neoforge.client.event.ClientChatReceivedEvent event) {
            if (!Boolean.getBoolean("hemomancy.schoolAcceptanceClient")) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            String text = event.getMessage().getString();
            if (text.equals("SCHOOL_DONE")) { mc.stop(); event.setCanceled(true); return; }
            if (!text.startsWith("SCHOOL_SHOT ") || mc.level == null) return;
            String[] parts = text.split(" ");
            var entity = mc.level.getEntity(Integer.parseInt(parts[2]));
            if (entity instanceof LivingEntity living) {
                String states = java.util.Arrays.stream(SchoolState.values()).filter(state -> SchoolStates.has(living, state))
                        .map(state -> {
                            var entry = SchoolStates.data(living).get(state);
                            return state + ":" + SchoolStates.levels(living, state)
                                    + ":bank=" + (entry == null ? 0 : entry.stored)
                                    + ":owner=" + (entry == null ? null : entry.origin.owner());
                        }).collect(java.util.stream.Collectors.joining(","));
                Hemomancy.LOGGER.info("SCHOOL_CLIENT {} {} states={} invisible={}", mc.getUser().getName(), parts[1], states, living.isInvisible());
            } else Hemomancy.LOGGER.error("SCHOOL_CLIENT missing tracked target {}", parts[2]);
            net.minecraft.client.Screenshot.grab(mc.gameDirectory, "school-" + parts[1] + ".png", mc.getMainRenderTarget(), ignored -> {});
            event.setCanceled(true);
        }
        @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
            if (!Boolean.getBoolean("hemomancy.schoolAcceptanceClient")) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            mc.options.pauseOnLostFocus = false;
            mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
            mc.options.particles().set(net.minecraft.client.ParticleStatus.MINIMAL);
            mc.options.darknessEffectScale().set(mc.getUser().getName().equals("SchoolObserver") ? 1.0 : 0.0);
            if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) mc.setScreen(null);
        }
    }
}
