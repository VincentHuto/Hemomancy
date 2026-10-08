package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.VampireSquidEntity;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("pelagic_ecology_validation")
@PrefixGameTestTemplate(false)
public final class VampireSquidGameTests {
    @GameTest(template = "pool", timeoutTicks = 40)
    public static void registeredSquidUsesNativeDeepSpawnsAndAquaticBlood(GameTestHelper h) {
        var type = EntityInit.vampire_squid.get();
        h.assertTrue(type.getCategory() == MobCategory.WATER_AMBIENT, "Squid must use a native swimming cap");
        h.assertTrue(type.is(EntityInit.SPECIMEN_JAR_CAPTURABLE) && type.is(EntityTypeTags.AQUATIC)
                && type.is(EntityTypeTags.CAN_BREATHE_UNDER_WATER), "Missing aquatic/capture tags");
        h.assertTrue(ItemInit.spawn_egg_vampire_squid.get().getType(new net.minecraft.world.item.ItemStack(ItemInit.spawn_egg_vampire_squid.get())) == type,
                "Spawn egg points to the wrong creature");
        var profile = BloodProfileData.profile(type, false);
        h.assertTrue(profile.tendencies().size() == 1 && profile.tendencies().getFirst().name().equals("TENEBRIS")
                && profile.properties().contains(Hemomancy.rloc("blood_properties/aquatic")) && !profile.requiresLivingSyringe(), "Wrong squid blood profile");
        var origin = h.absolutePos(new BlockPos(2304, 0, 2304));
        for (var layer : PelagicLayer.values()) {
            var biome = h.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(PelagicBiomes.key(layer)).value();
            var packs = biome.getMobSettings().getMobs(MobCategory.WATER_AMBIENT).unwrap().stream().filter(p -> p.type == type).toList();
            boolean deep = layer == PelagicLayer.CARRION || layer == PelagicLayer.HYDROTHERMAL;
            h.assertTrue(packs.size() == (deep ? 1 : 0), "Incorrect spawn table in " + layer);
            var pos = new BlockPos(origin.getX() + layer.ordinal() * 64, deep && layer == PelagicLayer.HYDROTHERMAL ? -44 : -20, origin.getZ());
            habitat(h.getLevel(), pos, layer, 3, 3, false);
            h.assertTrue(SpawnPlacements.checkSpawnRules(type, h.getLevel(), MobSpawnType.NATURAL, pos, h.getLevel().random) == deep,
                    "Native placement predicate disagrees with deep habitat: " + layer);
            if (deep) {
                h.getLevel().setBlock(pos.above(), Blocks.STONE.defaultBlockState(), 2);
                h.assertTrue(!SpawnPlacements.checkSpawnRules(type, h.getLevel(), MobSpawnType.NATURAL, pos, h.getLevel().random), "Obstructed arms should reject spawning");
                h.getLevel().setBlock(pos.above(), Blocks.WATER.defaultBlockState(), 2);
                h.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                h.assertTrue(!SpawnPlacements.checkSpawnRules(type, h.getLevel(), MobSpawnType.NATURAL, pos, h.getLevel().random), "Squid spawned in air");
            }
        }
        h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 130)
    public static void cloakExpiresAndRespectsItsCooldownWithoutMovingDisabledAi(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(6, 4, 6));
        fillWater(h.getLevel(), pos, 3, 3);
        var squid = squid(h, pos, true);
        var start = squid.position();
        h.assertTrue(squid.hurt(squid.damageSources().generic(), 1) && squid.isCloaked(), "Nonlethal damage should fold the arm web");
        h.runAfterDelay(12, () -> {
            h.assertTrue(squid.cloakAmount(1) > .95F, "Cloak did not ease fully closed");
            h.assertTrue(squid.getTarget() == null && squid.position().distanceToSqr(start) < .01, "Defence moved disabled AI or targeted an attacker");
        });
        h.runAfterDelay(66, () -> {
            h.assertTrue(!squid.isCloaked(), "Cloak did not expire");
            squid.hurt(squid.damageSources().generic(), .5F);
            h.assertTrue(!squid.isCloaked(), "Cooldown should prevent immediate re-cloaking");
        });
        h.runAfterDelay(105, () -> {
            h.assertTrue(squid.cloakAmount(1) == 0, "Resting pose retained the cloak");
            squid.hurt(squid.damageSources().generic(), .5F);
            h.assertTrue(squid.isCloaked(), "Squid did not regain its defence");
            squid.discard(); h.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 200)
    public static void bothDeepHabitatsSupportSwimmingAboveTheFloor(GameTestHelper h) {
        var origin = new BlockPos(4096, 0, 4096);
        var actors = new VampireSquidEntity[2]; var starts = new Vec3[2]; var centers = new BlockPos[2];
        var maximumDisplacement = new double[2];
        for (int i = 0; i < 2; i++) {
            centers[i] = new BlockPos(origin.getX() + i * 64, i == 0 ? -20 : -44, origin.getZ());
            habitat(h.getLevel(), centers[i], i == 0 ? PelagicLayer.CARRION : PelagicLayer.HYDROTHERMAL, 14, 6, true);
            actors[i] = squid(h, centers[i], false); actors[i].getRandom().setSeed(1337 + i);
            starts[i] = actors[i].position();
        }
        // Exercise navigation deterministically; random wander may legitimately idle during a short fixture.
        h.runAfterDelay(20, () -> {
            for (int i = 0; i < 2; i++) h.assertTrue(actors[i].getNavigation().moveTo(centers[i].getX() + 5.5,
                    centers[i].getY() + .5, centers[i].getZ() + .5, 1), "No open-water route in depth " + i);
        });
        for (int delay : new int[]{40, 80, 120}) h.runAfterDelay(delay, () -> {
            for (int i = 0; i < 2; i++) maximumDisplacement[i] = Math.max(maximumDisplacement[i], actors[i].position().distanceToSqr(starts[i]));
        });
        h.runAfterDelay(160, () -> {
            for (int i = 0; i < 2; i++) {
                h.assertTrue(actors[i].isAlive() && actors[i].isInWater() && actors[i].getHealth() == actors[i].getMaxHealth(), "Swimming/survival failed in depth " + i);
                maximumDisplacement[i] = Math.max(maximumDisplacement[i], actors[i].position().distanceToSqr(starts[i]));
                h.assertTrue(maximumDisplacement[i] > .1, "Deep squid never swam: depth=" + i + " ticks=" + actors[i].tickCount + " displacement=" + actors[i].position().subtract(starts[i]) + " maximum=" + maximumDisplacement[i]
                        + " nav=" + actors[i].getNavigation().getPath() + " wanted=" + actors[i].getMoveControl().getWantedX() + "," + actors[i].getMoveControl().getWantedY() + "," + actors[i].getMoveControl().getWantedZ());
                h.assertTrue(actors[i].getY() > centers[i].getY() - 4.5, "Squid behaved as bottom fauna");
                actors[i].discard();
                for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
                    h.getLevel().setChunkForced((centers[i].getX() >> 4) + dx, (centers[i].getZ() >> 4) + dz, false);
            }
            h.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 50)
    public static void threatenedSquidRetreatsWithoutAttacking(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(10, 4, 10)); fillWater(h.getLevel(), pos, 5, 3);
        var squid = squid(h, pos, false); var start = squid.position();
        var player = h.makeMockPlayer(GameType.SURVIVAL); player.moveTo(start.add(3, 0, 0));
        float health = player.getHealth();
        squid.hurt(squid.damageSources().playerAttack(player), 1);
        h.runAfterDelay(25, () -> {
            h.assertTrue(squid.isAlive() && squid.isCloaked() && squid.getX() < start.x - .3, "Cloaked squid did not withdraw from its attacker");
            h.assertTrue(squid.getTarget() == null && player.getHealth() == health, "Defence became an attack");
            squid.discard(); h.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 40)
    public static void jarRoundTripKeepsSquidHealthNameAndDefence(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(6, 4, 6)); fillWater(h.getLevel(), pos, 3, 3);
        var squid = squid(h, pos, true);
        squid.setCustomName(Component.literal("Little cloak")); squid.hurt(squid.damageSources().generic(), 1);
        var saved = SpecimenJarData.captureEntity(squid);
        var jar = SpecimenJarData.createStackWithSpecimen(saved);
        var restored = (VampireSquidEntity)SpecimenJarData.releaseSpecimen(h.getLevel(), pos, SpecimenJarData.getSpecimen(jar)).orElseThrow();
        h.assertTrue(restored.getType() == squid.getType() && restored.getHealth() == squid.getHealth()
                && squid.getCustomName().equals(restored.getCustomName()) && restored.isPersistenceRequired(), "Capture changed the specimen");
        h.assertTrue(restored.isCloaked() && restored.actionTicks() == squid.actionTicks(), "Defence was lost across capture/reload");
        h.assertTrue(restored.cloakAmount(1) > .95F, "A jar's unticked render copy must retain the stored cloak pose");
        h.assertTrue(SpecimenBestiaryDefinitions.isResearchSpecimen(Hemomancy.rloc("vampire_squid"))
                && !SpecimenBestiaryDefinitions.createSurrenderReward(Hemomancy.rloc("vampire_squid")).isEmpty(), "Living Bestiary does not accept the new specimen");
        squid.discard(); restored.discard(); h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 40)
    public static void playerKillsDropExistingSharedSplinters(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(12, 4, 12)); fillWater(h.getLevel(), pos, 4, 3);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        for (int i = 0; i < 8; i++) {
            var squid = squid(h, pos, true);
            h.assertTrue(squid.hurt(squid.damageSources().playerAttack(player), 100), "Native player kill failed");
        }
        h.runAfterDelay(3, () -> {
            var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(5));
            int count = drops.stream().filter(e -> e.getItem().is(ItemInit.calcified_blood_spine.get())).mapToInt(e -> e.getItem().getCount()).sum();
            h.assertTrue(count >= 8 && count <= 16, "Eight kills should drop 8–16 shared splinters, got " + count);
            h.assertTrue(drops.stream().allMatch(e -> e.getItem().is(ItemInit.calcified_blood_spine.get())), "Squid introduced an extra unique drop");
            drops.forEach(Entity::discard); h.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 100)
    public static void squidAndCombJellyTiltWithActualSwimmingAndRestUpright(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(8, 4, 8)); fillWater(h.getLevel(), pos, 6, 3);
        var squid = squid(h, pos, true);
        var jelly = EntityInit.bloody_belly_comb_jelly.get().create(h.getLevel());
        jelly.moveTo(pos.east(2).getCenter()); jelly.setPersistenceRequired(); jelly.setNoAi(true);
        h.getLevel().addFreshEntity(jelly);
        var swimmers = java.util.List.of(squid, jelly);
        for (var mob : swimmers) {
            mob.setNoAi(false);
            mob.goalSelector.removeAllGoals(goal -> true);
        }
        for (int tick=1;tick<=30;tick++) h.runAfterDelay(tick,
                () -> swimmers.forEach(mob -> mob.setDeltaMovement(new Vec3(.04,-.02,.03))));
        h.runAfterDelay(30, () -> {
            for (var mob : swimmers) {
                var pose = mob.swimmingPose();
                h.assertTrue(pose.leanRadians(1) > Math.PI/2 && pose.leanRadians(1) < Math.PI*.75,
                        "Diving swimmer did not angle its body: " + mob.getType() + " lean=" + pose.leanRadians(1)
                                + " motion=" + mob.getDeltaMovement() + " water=" + mob.isInWater());
                h.assertTrue(Math.abs(pose.headingDegrees(1)+53.13) < 5, "Body ignored actual travel heading: " + mob.getType());
            }
        });
        for (int tick=31;tick<=70;tick++) h.runAfterDelay(tick,
                () -> swimmers.forEach(mob -> mob.setDeltaMovement(Vec3.ZERO)));
        h.runAfterDelay(72, () -> {
            for (var mob : swimmers) {
                h.assertTrue(mob.swimmingPose().leanRadians(1) < .01, "Stopped swimmer did not return upright: "
                        + mob.getType() + " lean=" + mob.swimmingPose().leanRadians(1) + " motion=" + mob.getDeltaMovement());
                var copy = (com.vincenthuto.hemomancy.common.entity.mob.aquatic.PelagicAnimal)
                        net.minecraft.world.entity.EntityType.create(SpecimenJarData.captureEntity(mob), h.getLevel()).orElseThrow();
                h.assertTrue(copy.swimmingPose().leanRadians(1) == 0, "Captured display inherited movement tilt");
                mob.discard();
            }
            h.succeed();
        });
    }

    private static VampireSquidEntity squid(GameTestHelper h, BlockPos pos, boolean noAi) {
        var squid = EntityInit.vampire_squid.get().create(h.getLevel());
        squid.moveTo(pos.getCenter()); squid.setPersistenceRequired(); squid.setNoAi(noAi);
        h.getLevel().addFreshEntity(squid); return squid;
    }
    private static void fillWater(ServerLevel level, BlockPos pos, int radius, int height) {
        for (var p : BlockPos.betweenClosed(pos.offset(-radius, -height, -radius), pos.offset(radius, height, radius)))
            level.setBlock(p, p.getY() == pos.getY() - height ? Blocks.STONE.defaultBlockState() : Blocks.WATER.defaultBlockState(), 2);
    }
    private static void habitat(ServerLevel level, BlockPos pos, PelagicLayer layer, int radius, int height, boolean force) {
        var biome = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(PelagicBiomes.key(layer));
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            if (force) level.setChunkForced((pos.getX() >> 4) + dx, (pos.getZ() >> 4) + dz, true);
            level.getChunk((pos.getX() >> 4) + dx, (pos.getZ() >> 4) + dz).fillBiomesFromNoise((x, y, z, sampler) -> biome, level.getChunkSource().randomState().sampler());
        }
        fillWater(level, pos, radius, height);
    }
}
