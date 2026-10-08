package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("pelagic_ecology_validation")
@PrefixGameTestTemplate(false)
public final class PelagicEcologyGameTests {
    private static final String[] CREATURES = {"chiton", "pyrosome", "pelagic_herring", "siphonophore", "bloody_belly_comb_jelly", "hagfish"};

    @GameTest(template = "pool", timeoutTicks = 80)
    public static void pyrosomesSpawnWithFullLargeSwimmerCapAndStopAtTheirOwnCap(GameTestHelper h) {
        var level = h.getLevel();
        var pos = h.absolutePos(new BlockPos(12, 6, 12));
        var category = EntityInit.pyrosome.get().getCategory();
        h.assertTrue(category.getName().equals("hemomancy:pyrosomes")
                && category.getMaxInstancesPerChunk() == 8 && category.isFriendly() && !category.isPersistent(),
                "Pyrosomes need their own bounded, ordinary-cadence population");
        var observer = new net.neoforged.neoforge.common.util.FakePlayer(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "PyrosomeObserver"));
        observer.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        observer.moveTo(pos.getCenter());
        level.addNewPlayer(observer);
        h.runAfterDelay(25, () -> {
            try {
                var entities = new java.util.ArrayList<net.minecraft.world.entity.Entity>();
                for (int i = 0; i < 5; i++) {
                    var squid = net.minecraft.world.entity.EntityType.SQUID.create(level);
                    squid.moveTo(pos.getCenter()); entities.add(squid);
                }
                var canSpawn = net.minecraft.world.level.NaturalSpawner.SpawnState.class.getDeclaredMethod(
                        "canSpawnForCategory", net.minecraft.world.entity.MobCategory.class, net.minecraft.world.level.ChunkPos.class);
                canSpawn.setAccessible(true);
                var chunk = level.getChunkAt(pos);
                for (int count = 0; count <= 8; count++) {
                    var state = net.minecraft.world.level.NaturalSpawner.createState(289, entities,
                            (packed, consumer) -> consumer.accept(chunk),
                            new net.minecraft.world.level.LocalMobCapCalculator(level.getChunkSource().chunkMap));
                    h.assertTrue(!(boolean)canSpawn.invoke(state, net.minecraft.world.entity.MobCategory.WATER_CREATURE, chunk.getPos()),
                            "Control: five squid must fill the large-swimmer cap");
                    h.assertTrue((boolean)canSpawn.invoke(state, category, chunk.getPos()) == (count < 8),
                            "Pyrosome capacity must be independent and stop at eight, count=" + count);
                    if (count < 8) {
                        var pyrosome = EntityInit.pyrosome.get().create(level);
                        pyrosome.moveTo(pos.getCenter()); entities.add(pyrosome);
                    }
                }
                h.succeed();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            } finally { observer.discard(); }
        });
    }

    @GameTest(template = "pool", timeoutTicks = 40)
    public static void benthosUseBoundedNativeCapsAndMatchingBiomeCategories(GameTestHelper h) {
        var category = net.minecraft.world.entity.MobCategory.valueOf("HEMOMANCY_PELAGIC_BENTHOS");
        h.assertTrue(category.getMaxInstancesPerChunk() == 12 && category.isFriendly() && !category.isPersistent(),
                "Bottom dwellers need a bounded friendly cap with ordinary spawn cadence");
        for (String id : new String[]{"chiton", "hagfish", "chalybeate_snail"}) {
            var type = BuiltInRegistries.ENTITY_TYPE.get(Hemomancy.rloc(id));
            h.assertTrue(type.getCategory() == category, "Incorrect native category: " + id);
            h.assertTrue(!((Mob)type.create(h.getLevel())).isPersistenceRequired(), "Natural benthos must despawn normally: " + id);
        }
        var biomes = h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        for (var layer : com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer.values()) {
            var biome = biomes.getHolderOrThrow(com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicBiomes.key(layer)).value();
            for (var mobCategory : net.minecraft.world.entity.MobCategory.values())
                for (var entry : biome.getMobSettings().getMobs(mobCategory).unwrap())
                    h.assertTrue(entry.type.getCategory() == mobCategory, "Spawn table/category mismatch in " + layer + ": " + entry.type);
        }
        h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 100)
    public static void nativeSpawnerProducesPyrosomesWhileSquidCapacityIsFull(GameTestHelper h) {
        var level = h.getLevel();
        var origin = h.absolutePos(new BlockPos(4096, 0, 4096));
        var pos = new BlockPos((origin.getX() & ~15) + 8, 45, (origin.getZ() & ~15) + 8);
        var biome = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getHolderOrThrow(com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicBiomes.key(
                        com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer.OPEN));
        var chunks = new java.util.ArrayList<net.minecraft.world.level.chunk.LevelChunk>();
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            var chunk = level.getChunk((pos.getX() >> 4) + dx, (pos.getZ() >> 4) + dz);
            chunk.fillBiomesFromNoise((a,b,c,s) -> biome, level.getChunkSource().randomState().sampler());
            chunks.add(chunk);
        }
        fillWater(h, pos, 23, 15);
        var observer = new net.neoforged.neoforge.common.util.FakePlayer(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "PyrosomeSpawn"));
        observer.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        observer.setNoGravity(true); observer.moveTo(pos.getCenter().add(48, 0, 0));
        level.addNewPlayer(observer);
        var competitors = new java.util.ArrayList<Mob>();
        for (int i = 0; i < 5; i++) {
            var squid = net.minecraft.world.entity.EntityType.SQUID.create(level);
            squid.moveTo(pos.getCenter().add(i, 0, 0)); squid.setNoAi(true);
            level.addFreshEntity(squid); competitors.add(squid);
        }
        h.runAfterDelay(25, () -> {
            var bounds = new net.minecraft.world.phys.AABB(pos).inflate(30);
            try {
                var entities = new java.util.ArrayList<net.minecraft.world.entity.Entity>(competitors);
                var state = net.minecraft.world.level.NaturalSpawner.createState(289, entities,
                        (packed, consumer) -> {
                            var chunk = level.getChunkSource().getChunkNow(net.minecraft.world.level.ChunkPos.getX(packed),
                                    net.minecraft.world.level.ChunkPos.getZ(packed));
                            if (chunk != null) consumer.accept(chunk);
                        }, new net.minecraft.world.level.LocalMobCapCalculator(level.getChunkSource().chunkMap));
                h.assertTrue(state.getMobCategoryCounts().getInt(net.minecraft.world.entity.MobCategory.WATER_CREATURE) == 5,
                        "Control must fill the ordinary water-creature cap");
                level.random.setSeed(1337);
                for (int pass = 0; pass < 200; pass++) for (var chunk : chunks)
                    net.minecraft.world.level.NaturalSpawner.spawnForChunk(level, chunk, state, true, false, false);
                var spawned = level.getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.mob.aquatic.PyrosomeEntity.class, bounds);
                h.assertTrue(!spawned.isEmpty(), "Native spawning must produce Pyrosomes despite five existing squid");
                for (var pyrosome : spawned) {
                    h.assertTrue(!pyrosome.isPersistenceRequired(), "Natural Pyrosomes must retain ordinary despawning");
                    h.assertTrue(pyrosome.getY() >= 28 && pyrosome.getY() <= 59, "Spawn escaped the depth window");
                }
                h.succeed();
            } finally {
                level.getEntitiesOfClass(Mob.class, bounds).forEach(Mob::discard);
                competitors.forEach(Mob::discard); observer.discard();
            }
        });
    }

    @GameTest(template = "pool", timeoutTicks = 200)
    public static void bottomAnimalsStayAttachedAndCuttlesDescendTowardTwilight(GameTestHelper h) {
        var level = h.getLevel(); var origin = h.absolutePos(new BlockPos(1024,0,1024));
        var mobs = new java.util.ArrayList<Mob>();
        var layers = new com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer[]{
                com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer.REEF,
                com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer.CARRION,
                com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer.HYDROTHERMAL,
                com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer.TWILIGHT};
        var ids = new String[]{"barbed_urchin", "hagfish", "chalybeate_snail", "prism_cuttle"};
        var ys = new int[]{45, -20, -45, 42};
        for (int i=0; i<ids.length; i++) {
            var center = new BlockPos(origin.getX()+i*64,ys[i],origin.getZ());
            var biome = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                    .getHolderOrThrow(com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicBiomes.key(layers[i]));
            for (int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) {
                level.setChunkForced((center.getX()>>4)+dx,(center.getZ()>>4)+dz,true);
                level.getChunk((center.getX()>>4)+dx,(center.getZ()>>4)+dz).fillBiomesFromNoise((a,b,c,s) -> biome, level.getChunkSource().randomState().sampler());
            }
            fillWater(h, center, 14, i==3 ? 20 : 3);
            if (i<3) for (var p:BlockPos.betweenClosed(center.offset(-14,-1,-14),center.offset(14,-1,14)))
                level.setBlock(p, (i==1 ? Blocks.BONE_BLOCK : i==2 ? Blocks.MAGMA_BLOCK : Blocks.STONE).defaultBlockState(),2);
            Mob mob=(Mob)BuiltInRegistries.ENTITY_TYPE.get(Hemomancy.rloc(ids[i])).create(level);
            mob.moveTo(center.getCenter()); mob.setPersistenceRequired(); mob.getRandom().setSeed(42+i); level.addFreshEntity(mob); mobs.add(mob);
        }
        h.runAfterDelay(160, () -> {
            for(int i=0;i<3;i++) h.assertTrue(mobs.get(i).isAlive() && Math.abs(mobs.get(i).getY()-ys[i])<1.2,
                    "Bottom association lost " + ids[i] + " Y" + mobs.get(i).getY() + " health=" + mobs.get(i).getHealth() + " removed=" + mobs.get(i).getRemovalReason());
            h.assertTrue(mobs.get(3).isAlive() && mobs.get(3).getY()<34, "Twilight cuttle still steers into shallow water: " + mobs.get(3).getY());
            mobs.forEach(Mob::discard);
            for(int i=0;i<4;i++) for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++)
                level.setChunkForced(((origin.getX()+i*64)>>4)+dx,(origin.getZ()>>4)+dz,false);
            h.succeed();
        });
    }

    @GameTest(template="pool", timeoutTicks=40)
    public static void softHabitatTargetsNeverPutFeedingFilamentsInsideTheSeabed(GameTestHelper h) {
        var level=h.getLevel(); var origin=h.absolutePos(new BlockPos(1536,0,1536)); var pos=new BlockPos(origin.getX(),17,origin.getZ());
        fillWater(h,pos,10,5);
        for(var p:BlockPos.betweenClosed(pos.offset(-10,-1,-10),pos.offset(10,-1,10))) level.setBlock(p,Blocks.STONE.defaultBlockState(),2);
        var biome=level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(
                com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicBiomes.key(com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer.TWILIGHT));
        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) level.getChunk((pos.getX()>>4)+dx,(pos.getZ()>>4)+dz)
                .fillBiomesFromNoise((a,b,c,s)->biome,level.getChunkSource().randomState().sampler());
        var animal=EntityInit.siphonophore.get().create(level); animal.moveTo(pos.getCenter()); animal.getRandom().setSeed(1337);
        h.assertTrue(com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat.waterClearance(level,pos.above(3),0,3,1),"Missing valid deeper-clearance alternative");
        int valid=0;
        for(int i=0;i<100;i++) {
            var target=com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat.target(animal,
                    com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.Species.SIPHONOPHORE,false);
            if(target!=null) {
                valid++;
                h.assertTrue(com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat.waterClearance(level,BlockPos.containing(target),0,3,1),"Soft habitat fallback aimed filaments into stone at " + target);
            }
        }
        h.assertTrue(valid>0,"All safe movement targets were rejected"); h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 40)
    public static void anemoneContactStingsMildlyAfterApproachRetraction(GameTestHelper h) {
        var level=h.getLevel(); var pos=h.absolutePos(new BlockPos(10,4,10)); fillWater(h,pos,3,2);
        var block=com.vincenthuto.hemomancy.common.init.BlockInit.tidepool_anemone.get();
        level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3); level.setBlock(pos,block.defaultBlockState(),3);
        ((com.vincenthuto.hemomancy.common.tile.harbinger.plant.PelagicColonyBlockEntity)level.getBlockEntity(pos)).retract();
        var fish=net.minecraft.world.entity.EntityType.COD.create(level); fish.moveTo(pos.getCenter()); fish.tickCount=20;
        float before=fish.getHealth(); level.getBlockState(pos).entityInside(level,pos,fish);
        h.assertTrue(before-fish.getHealth()==.5F,"Touching a withdrawn anemone should still give one mild contact sting");
        h.assertTrue(level.getBlockState(pos).getFluidState().isSource(),"Sting removed pool water"); h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 100)
    public static void loadedHabitatBoundariesSubstratesAndUnloadedEdges(GameTestHelper h) {
        var level = h.getLevel();
        var origin = h.absolutePos(new BlockPos(512, 0, 512));
        int column = 0;
        for (var species : com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.Species.values()) {
            var x = origin.getX() + column++ * 32;
            var biome = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                    .getHolderOrThrow(com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicBiomes.key(species.home));
            var chunk = level.getChunk(x >> 4, origin.getZ() >> 4);
            chunk.fillBiomesFromNoise((a,b,c,sampler) -> biome, level.getChunkSource().randomState().sampler());
            for (int y : new int[]{species.minY - 1, species.minY, species.maxY, species.maxY + 1}) {
                var pos = new BlockPos((x & ~15) + 8, y, (origin.getZ() & ~15) + 8);
                fillWater(h, pos, 3, 4);
                var floor = switch (species) {
                    case HAGFISH -> Blocks.BONE_BLOCK;
                    case SNAIL -> Blocks.MAGMA_BLOCK;
                    default -> Blocks.STONE;
                };
                boolean bottom = switch (species) { case CHITON, HEMOLYMPHOPODA, URCHIN, HAGFISH, SNAIL -> true; default -> false; };
                if (bottom) level.setBlock(pos.below(), floor.defaultBlockState(), 2);
                h.assertTrue(com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat.suitable(level, pos, species)
                        == (y >= species.minY && y <= species.maxY), "Loaded habitat boundary " + species + " Y" + y);
                if (y != species.minY) continue;
                if (species.name().equals("HAGFISH") || species.name().equals("SNAIL")) {
                    level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 2);
                    h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat.suitable(level, pos, species), "Missing required substrate accepted " + species);
                }
                if (species.name().equals("WHALE") || species.name().equals("SIPHONOPHORE")) {
                    level.setBlock(pos.below(2), Blocks.STONE.defaultBlockState(), 2);
                    h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat.suitable(level, pos, species), "Insufficient body/filament clearance accepted " + species);
                }
                if (species.name().equals("HEMOLYMPHOPODA")) {
                    level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
                    h.assertTrue(com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat.suitable(level, pos, species), "Shallow pool rejected");
                }
            }
        }
        var edge = new BlockPos(29000000, -20, 29000000);
        h.assertTrue(!level.hasChunk(edge.getX() >> 4, edge.getZ() >> 4), "Edge control already loaded");
        h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat.waterClearance(level, edge, 2, 2, 2), "Unloaded clearance accepted");
        h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat.nearBlock(level, edge, 6, 4, s -> s.is(Blocks.BONE_BLOCK)), "Unloaded substrate accepted");
        h.assertTrue(!level.hasChunk(edge.getX() >> 4, edge.getZ() >> 4), "Habitat reads loaded a neighboring chunk");
        h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 100)
    public static void everyNewCreatureSurvivesActualJarPlacementPickupReloadAndBreaking(GameTestHelper h) {
        var level = h.getLevel(); var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var jarBlock = com.vincenthuto.hemomancy.common.init.BlockInit.specimen_jar.get();
        var pos = h.absolutePos(new BlockPos(10, 4, 10)); fillWater(h, pos, 5, 3);
        for (String id : CREATURES) {
            var type = BuiltInRegistries.ENTITY_TYPE.get(Hemomancy.rloc(id)); var original = (Mob)type.create(level);
            original.moveTo(pos.getCenter()); original.setNoAi(true); original.setHealth(original.getMaxHealth() - 1);
            original.setCustomName(Component.literal("Jar lifecycle " + id)); level.addFreshEntity(original);
            var empty = new net.minecraft.world.item.ItemStack(jarBlock); player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, empty);
            empty.getItem().interactLivingEntity(empty, player, original, net.minecraft.world.InteractionHand.MAIN_HAND);
            var filled = player.getMainHandItem().copy();
            h.assertTrue(original.isRemoved() && SpecimenJarData.hasSpecimen(filled), "Actual capture failed " + id);
            var state = jarBlock.defaultBlockState(); level.setBlock(pos, state, 3); jarBlock.setPlacedBy(level, pos, state, player, filled);
            var jar = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.SpecimenJarBlockEntity)level.getBlockEntity(pos);
            var saved = jar.saveWithFullMetadata(level.registryAccess());
            var restored = new com.vincenthuto.hemomancy.common.tile.harbinger.functional.SpecimenJarBlockEntity(pos, state);
            restored.loadWithComponents(saved, level.registryAccess()); level.setBlockEntity(restored);
            player.getInventory().clearContent(); player.setShiftKeyDown(true);
            state.useWithoutItem(level, player, new net.minecraft.world.phys.BlockHitResult(pos.getCenter(), net.minecraft.core.Direction.UP, pos, false));
            var picked = java.util.stream.StreamSupport.stream(player.getInventory().items.spliterator(), false)
                    .filter(SpecimenJarData::hasSpecimen).findFirst().orElseThrow();
            h.assertTrue(SpecimenJarData.getSpecimen(picked).equals(SpecimenJarData.getSpecimen(filled)), "Pickup/reload changed specimen " + id);
            level.setBlock(pos, state, 3); jarBlock.setPlacedBy(level, pos, state, player, picked); level.destroyBlock(pos, false);
            var released = level.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(pos).inflate(3)).stream()
                    .filter(m -> m.getType() == type && !m.isRemoved()).toList();
            h.assertTrue(released.size() == 1 && released.getFirst().getHealth() == original.getHealth()
                    && released.getFirst().isPersistenceRequired() && original.getCustomName().equals(released.getFirst().getCustomName()), "Breaking jar lost or duplicated specimen " + id);
            var key = Hemomancy.rloc(id);
            h.assertTrue(com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions.isResearchSpecimen(key)
                    && !com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions.createSurrenderReward(key).isEmpty(), "Bestiary collection missing " + id);
            released.forEach(Mob::discard);
        }
        h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 140)
    public static void nativeHerringSchoolFormsAndMovesTogether(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(12, 6, 12));
        fillWater(h, pos, 10, 4);
        var fish = new java.util.ArrayList<com.vincenthuto.hemomancy.common.entity.mob.aquatic.PelagicHerringEntity>();
        net.minecraft.world.entity.SpawnGroupData group = null;
        for (int i = 0; i < 10; i++) {
            var member = EntityInit.pelagic_herring.get().create(h.getLevel());
            member.moveTo(pos.getX() + (i % 5) * .6, pos.getY(), pos.getZ() + (i / 5) * .6);
            member.getRandom().setSeed(42 + i); member.setPersistenceRequired();
            group = member.finalizeSpawn(h.getLevel(), h.getLevel().getCurrentDifficultyAt(pos), net.minecraft.world.entity.MobSpawnType.NATURAL, group);
            h.getLevel().addFreshEntity(member); fish.add(member);
        }
        h.assertTrue(fish.getFirst().getMaxSpawnClusterSize() == 10 && fish.getFirst().hasFollowers(), "Native school leader/cap missing");
        h.assertTrue(fish.stream().filter(com.vincenthuto.hemomancy.common.entity.mob.aquatic.PelagicHerringEntity::isFollower).count() == 9, "Pack did not form one coordinated school");
        var start = fish.getFirst().position();
        h.runAfterDelay(100, () -> {
            h.assertTrue(fish.getFirst().position().distanceTo(start) > .5, "School failed to swim");
            h.assertTrue(fish.stream().allMatch(f -> f.isAlive() && f.distanceToSqr(fish.getFirst()) < 144), "School dispersed or died in clear water");
            fish.forEach(Mob::discard); h.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 100)
    public static void chitonClampAndHagfishOfferSlimeAndSavedStateWork(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(10, 5, 10)); fillWater(h, pos, 5, 3);
        // Mock interaction players do not count as nearby players for natural despawning.
        var chiton = EntityInit.chiton.get().create(h.getLevel()); chiton.moveTo(pos.getCenter()); chiton.setPersistenceRequired(); h.getLevel().addFreshEntity(chiton);
        chiton.hurt(h.getLevel().damageSources().generic(), 1);
        h.assertTrue(chiton.isClamped(), "Attacked chiton did not clamp");
        float before = chiton.getHealth(); chiton.invulnerableTime = 0; chiton.hurt(h.getLevel().damageSources().generic(), 2);
        h.assertTrue(before - chiton.getHealth() < 1, "Clamped plates did not reduce damage");
        var hagfish = EntityInit.hagfish.get().create(h.getLevel()); hagfish.moveTo(pos.offset(2,0,0).getCenter()); hagfish.setPersistenceRequired(); h.getLevel().addFreshEntity(hagfish);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL); player.moveTo(hagfish.position().add(1,0,0));
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ROTTEN_FLESH, 3));
        hagfish.interact(player, net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(player.getMainHandItem().getCount() == 2 && hagfish.isFeeding(), "Hagfish did not accept one hand-offered flesh");
        hagfish.hurt(h.getLevel().damageSources().playerAttack(player), 1);
        h.assertTrue(hagfish.isSliming() && player.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN), "Defensive slime missing");
        var saved = SpecimenJarData.captureEntity(hagfish);
        var restored = (com.vincenthuto.hemomancy.common.entity.mob.aquatic.HagfishEntity)net.minecraft.world.entity.EntityType.create(saved, h.getLevel()).orElseThrow();
        h.assertTrue(restored.isFeeding() && restored.isSliming() && restored.feedingTicks() == hagfish.feedingTicks(), "Capture/reload lost behavioral state");
        var start = hagfish.position();
        h.runAfterDelay(20, () -> {
            h.assertTrue(hagfish.isAlive(), "Hagfish disappeared before retreat: " + hagfish.getRemovalReason());
            h.assertTrue(hagfish.getX() < start.x - .1, "Slime defense did not retreat away from the attacker");
            chiton.discard(); hagfish.discard(); h.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 80)
    public static void siphonophoreFeedsWithinVisibleFilamentsAndColonyRetracts(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(12, 6, 12)); fillWater(h, pos, 5, 4);
        var siphon = EntityInit.siphonophore.get().create(h.getLevel()); siphon.moveTo(pos.getCenter()); siphon.setPersistenceRequired(); h.getLevel().addFreshEntity(siphon);
        var prey = net.minecraft.world.entity.EntityType.COD.create(h.getLevel()); prey.setNoAi(true); prey.setNoGravity(true); prey.setPersistenceRequired();
        prey.moveTo(pos.getCenter().add(0, -1.5, 0)); h.getLevel().addFreshEntity(prey);
        var colonyPos = pos.offset(3, -2, 0); var block = com.vincenthuto.hemomancy.common.init.BlockInit.bone_worm_colony.get();
        h.getLevel().setBlock(colonyPos.below(), Blocks.BONE_BLOCK.defaultBlockState(), 3);
        h.getLevel().setBlock(colonyPos, block.defaultBlockState(), 3);
        var colony = (com.vincenthuto.hemomancy.common.tile.harbinger.plant.PelagicColonyBlockEntity)h.getLevel().getBlockEntity(colonyPos);
        colony.retract();
        h.assertTrue(h.getLevel().getBlockState(colonyPos).getValue(com.vincenthuto.hemomancy.common.block.harbinger.plant.PelagicColonyBlock.RETRACTED), "Retraction not synchronized as block state");
        h.runAfterDelay(65, () -> {
            h.assertTrue(!prey.isAlive(), "Small fish in feeding filaments was not caught");
            h.assertTrue(!h.getLevel().getBlockState(colonyPos).getValue(com.vincenthuto.hemomancy.common.block.harbinger.plant.PelagicColonyBlock.RETRACTED), "Colony did not extend after threat passed");
            h.assertTrue(h.getLevel().getBlockState(colonyPos.below()).is(Blocks.BONE_BLOCK), "Feeding/retraction erased fossil support");
            siphon.discard(); h.succeed();
        });
    }

    private static void fillWater(GameTestHelper h, BlockPos center, int radius, int halfHeight) {
        for (var p : BlockPos.betweenClosed(center.offset(-radius, -halfHeight, -radius), center.offset(radius, halfHeight, radius)))
            h.getLevel().setBlock(p, p.getY() == center.getY() - halfHeight ? Blocks.STONE.defaultBlockState() : Blocks.WATER.defaultBlockState(), 2);
    }

    @GameTest(template = "pool", timeoutTicks = 80)
    public static void disabledCuttleAiDoesNotDriftAwayFromFeedingFixtures(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(10, 5, 10));
        for (var p : BlockPos.betweenClosed(pos.offset(-4, -3, -4), pos.offset(4, 3, 4))) h.getLevel().setBlock(p, Blocks.WATER.defaultBlockState(), 2);
        var cuttle = EntityInit.prism_cuttle.get().create(h.getLevel());
        cuttle.moveTo(pos.getCenter()); cuttle.setNoAi(true); cuttle.setNoGravity(true); cuttle.setPersistenceRequired();
        h.getLevel().addFreshEntity(cuttle);
        var start = cuttle.position();
        var jelly = EntityInit.blood_lantern_jelly.get().create(h.getLevel());
        jelly.moveTo(pos.offset(2,0,0).getCenter()); jelly.setNoAi(true); jelly.setNoGravity(true); jelly.setPersistenceRequired(); h.getLevel().addFreshEntity(jelly);
        var jellyStart = jelly.position();
        h.runAfterDelay(40, () -> {
            h.assertTrue(cuttle.position().distanceTo(start) < .05, "NoAI cuttle swam on its own; supplied prey must remain stationary: " + cuttle.position().subtract(start));
            h.assertTrue(jelly.position().distanceTo(jellyStart)<.05,"NoAI lantern jelly swam during reload fixture: " + jelly.position().subtract(jellyStart));
            cuttle.discard(); jelly.discard(); h.succeed();
        });
    }

    @GameTest(template = "pool", timeoutTicks = 40)
    public static void boneWormColoniesStackWithoutDuplicatingSpecimens(GameTestHelper h) {
        var level = h.getLevel();
        var pos = h.absolutePos(new BlockPos(8, 5, 8));
        var block = com.vincenthuto.hemomancy.common.init.BlockInit.bone_worm_colony.get();
        var player = new net.neoforged.neoforge.common.util.FakePlayer(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "worm-harvest"));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.moveTo(pos.getCenter().add(2, 0, 0));
        var stack = new net.minecraft.world.item.ItemStack(block, 8);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
        level.setBlock(pos.below(), Blocks.BONE_BLOCK.defaultBlockState(), 3);
        level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        var hit = new net.minecraft.world.phys.BlockHitResult(pos.getCenter(), net.minecraft.core.Direction.UP, pos, false);
        var item = (net.minecraft.world.item.BlockItem)block.asItem();
        var shears = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHEARS);
        for (int count = 2; count <= 5; count++) {
            var context = new net.minecraft.world.item.context.BlockPlaceContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, stack, hit);
            h.assertTrue(item.place(context).consumesAction(), "Could not add bone worm " + count);
            var state = level.getBlockState(pos);
            var property = state.getBlock().getStateDefinition().getProperty("worms");
            h.assertTrue(property instanceof net.minecraft.world.level.block.state.properties.IntegerProperty,
                    "Bone worm colony needs a worms blockstate instead of five fixed worms");
            var worms = (net.minecraft.world.level.block.state.properties.IntegerProperty)property;
            h.assertTrue(state.getValue(worms) == count, "Placement did not increase the colony to " + count);
            h.assertTrue(stack.getCount() == 9 - count, "Adding a worm did not consume exactly one colony item");
            h.assertTrue(state.getFluidState().isSource(), "Stacking lost source water");
            var drops = net.minecraft.world.level.block.Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, shears);
            h.assertTrue(drops.stream().mapToInt(net.minecraft.world.item.ItemStack::getCount).sum() == count - 1,
                    "Shearing must return exactly the colony items used, without duplication");
            h.assertTrue(net.minecraft.world.level.block.Block.getDrops(state, level, pos, level.getBlockEntity(pos), player,
                    net.minecraft.world.item.ItemStack.EMPTY).isEmpty(), "Bone worms require shears for collection");
            player.setShiftKeyDown(true);
            var separate = new net.minecraft.world.item.context.BlockPlaceContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, stack, hit);
            h.assertTrue(!separate.replacingClickedOnBlock(), "Sneaking should bypass colony stacking");
            player.setShiftKeyDown(false);
        }
        int remaining = stack.getCount();
        var full = new net.minecraft.world.item.context.BlockPlaceContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, stack, hit);
        h.assertTrue(!full.replacingClickedOnBlock() && !item.place(full).consumesAction() && stack.getCount() == remaining,
                "A full colony accepted a sixth worm or consumed an item");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, shears);
        h.assertTrue(player.gameMode.destroyBlock(pos), "Native colony harvest failed");
        var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(2));
        h.assertTrue(drops.stream().filter(e -> e.getItem().is(block.asItem())).mapToInt(e -> e.getItem().getCount()).sum() == 4,
                "Five-worm colony must return the four invested colony items");
        drops.forEach(net.minecraft.world.entity.Entity::discard);
        h.assertTrue(level.getBlockState(pos).is(Blocks.WATER) && level.getFluidState(pos).isSource()
                && level.getBlockState(pos.below()).is(Blocks.BONE_BLOCK), "Harvest changed water or fossil support");
        h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 40)
    public static void tubeWormCountsStaySyncedWhenStackingEitherHalf(GameTestHelper h) {
        var level = h.getLevel();
        var pos = h.absolutePos(new BlockPos(8, 5, 8));
        var block = com.vincenthuto.hemomancy.common.init.BlockInit.giant_tube_worm_colony.get();
        var property = block.getStateDefinition().getProperty("worms");
        h.assertTrue(property instanceof net.minecraft.world.level.block.state.properties.IntegerProperty,
                "Giant tube worm colonies need a worms blockstate instead of five fixed worms");
        var worms = (net.minecraft.world.level.block.state.properties.IntegerProperty)property;
        var player = new net.neoforged.neoforge.common.util.FakePlayer(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "tube-stack"));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.moveTo(pos.getCenter().add(2, 0, 0));
        var stack = new net.minecraft.world.item.ItemStack(block, 8);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
        level.setBlock(pos.below(), Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        for (int y = 0; y <= 2; y++) level.setBlock(pos.above(y), Blocks.WATER.defaultBlockState(), 3);
        var item = (net.minecraft.world.item.BlockItem)block.asItem();
        net.minecraft.world.level.block.entity.BlockEntity root = null;
        for (int count = 2; count <= 5; count++) {
            var selected = count % 2 == 0 ? pos : pos.above();
            var hit = new net.minecraft.world.phys.BlockHitResult(selected.getCenter(), net.minecraft.core.Direction.UP, selected, false);
            var context = new net.minecraft.world.item.context.BlockPlaceContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, stack, hit);
            h.assertTrue(item.place(context).consumesAction(), "Could not add tube worm " + count + " through selected half");
            if (count == 2) {
                root = level.getBlockEntity(pos);
                ((com.vincenthuto.hemomancy.common.tile.harbinger.plant.PelagicColonyBlockEntity)root).retract();
            }
            h.assertTrue(stack.getCount() == 9 - count, "Stacking did not consume exactly one colony item");
            h.assertTrue(level.getBlockEntity(pos) == root, "Stacking replaced the colony's root block entity");
            for (var half : new BlockPos[]{pos, pos.above()}) {
                var state = level.getBlockState(half);
                h.assertTrue(state.is(block) && state.getValue(worms) == count, "Tube colony halves disagree about worm count");
                h.assertTrue(state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.plant.PelagicColonyBlock.RETRACTED)
                        && state.getFluidState().isSource(), "Stacking lost retraction or source water");
                var saved = net.minecraft.nbt.NbtUtils.writeBlockState(state);
                var restored = net.minecraft.nbt.NbtUtils.readBlockState(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK), saved);
                h.assertTrue(restored.equals(state), "Reload lost the tube colony count or half state");
                player.setShiftKeyDown(true);
                var separateHit = new net.minecraft.world.phys.BlockHitResult(half.getCenter(), net.minecraft.core.Direction.UP, half, false);
                h.assertTrue(!new net.minecraft.world.item.context.BlockPlaceContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, stack, separateHit)
                        .replacingClickedOnBlock(), "Sneaking should bypass stacking on either half");
                player.setShiftKeyDown(false);
            }
            h.assertTrue(level.getBlockState(pos.above(2)).is(Blocks.WATER), "Stacking on the upper half grew a third block");
        }
        for (var half : new BlockPos[]{pos, pos.above()}) {
            var hit = new net.minecraft.world.phys.BlockHitResult(half.getCenter(), net.minecraft.core.Direction.UP, half, false);
            var context = new net.minecraft.world.item.context.BlockPlaceContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, stack, hit);
            h.assertTrue(!context.replacingClickedOnBlock() && !item.place(context).consumesAction() && stack.getCount() == 4,
                    "Full tube colony accepted another worm or consumed an item");
        }
        h.assertTrue(level.getBlockState(pos.below()).is(Blocks.MAGMA_BLOCK), "Stacking changed vent support");
        h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 40)
    public static void growthHarvestPreservesBothWaterCellsAndReturnsInvestedSpecimens(GameTestHelper h) {
        var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(8, 5, 8));
        var tube = com.vincenthuto.hemomancy.common.init.BlockInit.giant_tube_worm_colony.get();
        var property = tube.getStateDefinition().getProperty("worms");
        h.assertTrue(property instanceof net.minecraft.world.level.block.state.properties.IntegerProperty, "Tube harvest requires variable colony sizes");
        var worms = (net.minecraft.world.level.block.state.properties.IntegerProperty)property;
        var player = new net.neoforged.neoforge.common.util.FakePlayer(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "colony-harvest"));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL); player.moveTo(pos.getCenter().add(2,0,0));
        var shears = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHEARS);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,shears);
        for (int count = 2; count <= 5; count++) for (boolean upper : new boolean[]{false, true}) {
            level.setBlock(pos.below(), Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3); level.setBlock(pos.above(), Blocks.WATER.defaultBlockState(), 3);
            var state = tube.defaultBlockState().setValue(worms, count); level.setBlock(pos, state, 2);
            tube.setPlacedBy(level, pos, state, player, new net.minecraft.world.item.ItemStack(tube));
            var selected = upper ? pos.above() : pos;
            var selectedState = level.getBlockState(selected);
            var drops = net.minecraft.world.level.block.Block.getDrops(selectedState, level, selected, level.getBlockEntity(selected), player, shears);
            h.assertTrue(drops.stream().mapToInt(net.minecraft.world.item.ItemStack::getCount).sum() == count - 1, "Either half must return the invested colony items");
            var noTool = net.minecraft.world.level.block.Block.getDrops(selectedState, level, selected, null, player, net.minecraft.world.item.ItemStack.EMPTY);
            h.assertTrue(noTool.isEmpty(), "Ordinary breaking should not collect living colonies");
            h.assertTrue(player.gameMode.destroyBlock(selected), "Native survival harvest failed");
            var actualDrops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(2));
            h.assertTrue(actualDrops.stream().filter(e->e.getItem().is(tube.asItem())).mapToInt(e->e.getItem().getCount()).sum()==count-1,"Actual two-half harvest duplicated or lost invested specimens");
            actualDrops.forEach(net.minecraft.world.entity.Entity::discard);
            h.assertTrue(level.getBlockState(pos).is(Blocks.WATER) && level.getBlockState(pos.above()).is(Blocks.WATER), "Harvest lost source water");
            h.assertTrue(level.getBlockState(pos.below()).is(Blocks.MAGMA_BLOCK), "Harvest changed the vent substrate");
        }
        h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 40)
    public static void everyBiologicalBloodProfileLoadsWithoutEnvironmentalReassignment(GameTestHelper h) {
        var tendencies = new String[]{"FERRIC", "ANIMUS", "DUCTILIS", "DUCTILIS", "TENEBRIS", "MORTEM"};
        for (int i = 0; i < CREATURES.length; i++) {
            var type = BuiltInRegistries.ENTITY_TYPE.get(Hemomancy.rloc(CREATURES[i]));
            var profile = com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData.profile(type, false);
            h.assertTrue(profile.tendencies().size() == 1 && profile.tendencies().getFirst().name().equals(tendencies[i]), "Wrong blood tendency " + CREATURES[i]);
            h.assertTrue(profile.requiresLivingSyringe() == (i == 0), "Wrong syringe gate " + CREATURES[i]);
        }
        h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 60)
    public static void everyCreatureSurvivesSpecimenCaptureAndReload(GameTestHelper h) {
        for (String id : CREATURES) {
            var key = Hemomancy.rloc(id);
            h.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(key), "Missing ecological creature " + id);
            var type = BuiltInRegistries.ENTITY_TYPE.get(key);
            h.assertTrue(type.is(EntityInit.SPECIMEN_JAR_CAPTURABLE), "Jar tag missing " + id);
            Mob original = (Mob) type.create(h.getLevel());
            original.setCustomName(Component.literal("Preserved " + id));
            original.setHealth(Math.max(1, original.getMaxHealth() - 1));
            original.setPersistenceRequired();
            var captured = SpecimenJarData.captureEntity(original);
            var jar = SpecimenJarData.createStackWithSpecimen(captured);
            Mob released = (Mob) SpecimenJarData.releaseSpecimen(h.getLevel(), h.absolutePos(new BlockPos(4, 3, 4)),
                    SpecimenJarData.getSpecimen(jar)).orElseThrow();
            h.assertTrue(released.getType() == type && released.getHealth() == original.getHealth(), "Type or health lost " + id);
            h.assertTrue(released.isPersistenceRequired() && released.getCustomName().equals(original.getCustomName()), "Name or persistence lost " + id);
            var saved = SpecimenJarData.captureEntity(released);
            h.assertTrue(saved.getString("id").equals(key.toString()), "Second capture lost type " + id);
            released.discard();
        }
        h.succeed();
    }

    @GameTest(template = "pool", timeoutTicks = 40)
    public static void allGrowthsHavePlaceableItems(GameTestHelper h) {
        for (String id : new String[]{"hematic_algal_crust", "tidepool_anemone", "bone_worm_colony", "giant_tube_worm_colony"}) {
            var key = Hemomancy.rloc(id);
            h.assertTrue(BuiltInRegistries.BLOCK.containsKey(key), "Missing habitat growth " + id);
            var block = BuiltInRegistries.BLOCK.get(key);
            h.assertTrue(block != Blocks.AIR && BuiltInRegistries.ITEM.containsKey(key), "No placeable specimen " + id);
        }
        h.succeed();
    }
}

