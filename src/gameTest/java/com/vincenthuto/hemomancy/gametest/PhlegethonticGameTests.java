package com.vincenthuto.hemomancy.gametest;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.*;
import com.vincenthuto.hemomancy.common.entity.projectile.RecallBarbEntity;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.worldgen.*;
import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTerrainPlan.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PhlegethonticGameTests {
    private static final String ROOM="phlegethontic_test_room";
    private static final List<Entity> TEST_ENTITIES=new ArrayList<>();
    private static <T extends Entity> T spawn(GameTestHelper helper,EntityType<T> type,BlockPos pos) {
        T entity=helper.spawn(type,pos);TEST_ENTITIES.add(entity);return entity;
    }

    @GameTest(batch="phlegethontic",template=ROOM,timeoutTicks=40)
    public static void authoritativeWorldgenResourcesDecode(GameTestHelper h) throws Exception {
        var ops=RegistryOps.create(JsonOps.INSTANCE,h.getLevel().registryAccess());
        for(String id:List.of("phlegethontic_basin_terrain","phlegethontic_vein","escharian_overgrowth")) {
            var configured=resource(h,"worldgen/configured_feature/"+id+".json");
            ConfiguredFeature.DIRECT_CODEC.parse(ops,configured).getOrThrow();
            PlacedFeature.DIRECT_CODEC.parse(ops,resource(h,"worldgen/placed_feature/"+id+".json")).getOrThrow();
        }
        net.minecraft.world.level.biome.Biome.DIRECT_CODEC.parse(ops,resource(h,"worldgen/biome/phlegethontic_basin.json")).getOrThrow();
        var foreign=h.getLevel().registryAccess().registryOrThrow(Registries.BIOME).get(ResourceLocation.parse("phlegethontic_validation:foreign_nether"));
        h.assertTrue(foreign.getGenerationSettings().features().get(7).stream().anyMatch(feature -> feature.is(PlacedFeatureInit.PHLEGETHONTIC_VEIN)),
                "The Nether tag modifier must also inject veins into a foreign namespace biome");
        h.succeed();
    }

    @GameTest(batch="phlegethontic",template=ROOM,timeoutTicks=80)
    public static void contactIsDeduplicatedAndFireResistanceKeepsBloodLoss(GameTestHelper h) {
        Cow cow=spawn(h,EntityType.COW,new BlockPos(8,3,8));cow.setNoAi(true);cow.setNoGravity(true);
        cow.setHealth(10);
        var fluid=BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get().defaultBlockState();
        fluid.entityInside(h.getLevel(),cow.blockPosition(),cow);
        float first=cow.getHealth();
        cow.invulnerableTime=0;
        fluid.entityInside(h.getLevel(),cow.blockPosition().east(),cow);
        h.assertTrue(first==7 && cow.getHealth()==first,"Several contact blocks must cause exactly one 3-point scald");
        h.assertTrue(cow.hasEffect(EffectInit.blood_loss),"Contact must apply Blood Loss");
        Cow resistant=spawn(h,EntityType.COW,new BlockPos(14,3,8));resistant.setNoAi(true);resistant.setNoGravity(true);
        resistant.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,200));
        float health=resistant.getHealth();
        fluid.entityInside(h.getLevel(),resistant.blockPosition(),resistant);
        h.assertTrue(resistant.getHealth()==health,"Fire Resistance must block scalding damage");
        h.assertTrue(resistant.hasEffect(EffectInit.blood_loss),"Fire Resistance must not block Blood Loss");
        var guardian=spawn(h,EntityInit.excoriated.get(),new BlockPos(20,3,8));guardian.setNoAi(true);
        var velocity=guardian.getDeltaMovement();
        fluid.entityInside(h.getLevel(),guardian.blockPosition(),guardian);
        h.assertTrue(guardian.getHealth()==52 && !guardian.hasEffect(EffectInit.blood_loss)
                && guardian.getDeltaMovement().equals(velocity),"Guardian must be immune to contact and current");
        cow.removeAllEffects();cow.clearFire();
        h.runAfterDelay(9,() -> {
            cow.invulnerableTime=0;fluid.entityInside(h.getLevel(),cow.blockPosition(),cow);
            h.assertTrue(cow.getHealth()==first,"Scald must wait ten ticks");
        });
        h.runAfterDelay(10,() -> {
            cow.invulnerableTime=0;fluid.entityInside(h.getLevel(),cow.blockPosition(),cow);
            h.assertTrue(cow.getHealth()==first-3,"Scald must resume at ten ticks");h.succeed();
        });
    }

    @GameTest(batch="phlegethontic",template=ROOM,timeoutTicks=160)
    public static void uncontainedIchorActuallyFlowsDuringScheduledTicks(GameTestHelper h) {
        for(int x=5;x<12;x++)for(int z=5;z<12;z++)h.setBlock(new BlockPos(x,3,z),Blocks.BLACKSTONE);
        h.setBlock(new BlockPos(8,4,8),BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get());
        h.runAfterDelay(60,() -> {
            h.assertTrue(h.getLevel().getFluidState(h.absolutePos(new BlockPos(9,4,8))).is(PhlegethonticTags.ICHOR),
                    "Positive control: exposed ichor must flow, so containment tests exercise active fluid");h.succeed();
        });
    }

    @GameTest(batch="phlegethontic",template=ROOM,timeoutTicks=160)
    public static void buriedCoreSurvivesFluidTicksBesideProtectedCaves(GameTestHelper h) {
        var origin=h.absolutePos(new BlockPos(8,8,8));
        // Build a real cave, ore, portal and block-entity obstruction inside the vein's envelope.
        for(int x=3;x<29;x++)for(int y=3;y<14;y++)for(int z=3;z<16;z++)h.setBlock(new BlockPos(x,y,z),Blocks.NETHERRACK);
        h.setBlock(new BlockPos(13,8,8),Blocks.NETHER_GOLD_ORE);
        h.setBlock(new BlockPos(18,8,8),Blocks.CHEST);
        h.setBlock(new BlockPos(16,8,8),com.vincenthuto.hutoslib.common.registry.HLBlockInit.display_glass.get());
        h.setBlock(new BlockPos(23,8,8),Blocks.AIR);
        var nodes=new ArrayList<PhlegethonticVeinPath.Node>();
        for(int x=0;x<18;x++)nodes.add(new PhlegethonticVeinPath.Node(origin.getX()+x,origin.getY(),origin.getZ(),2));
        var level=h.getLevel();Map<Voxel,Material> plan=new HashMap<>();
        for(int cx=origin.getX()>>4;cx<=(origin.getX()+17)>>4;cx++)for(int cz=(origin.getZ()-4)>>4;cz<=(origin.getZ()+4)>>4;cz++)
            plan.putAll(PhlegethonticTerrainPlan.vein(nodes,cx,cz,p -> level.getBlockState(pos(p)).is(PhlegethonticTags.VEIN_REPLACEABLE)));
        for(var e:plan.entrySet())if(e.getValue()==Material.SCAB)level.setBlock(pos(e.getKey()),BlockInit.blood_scorched_scab.get().defaultBlockState(),3);
        for(var e:plan.entrySet())if(e.getValue()==Material.ICHOR)level.setBlock(pos(e.getKey()),BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get().defaultBlockState(),3);
        h.assertTrue(plan.containsValue(Material.ICHOR),"Fixture must contain a surviving core");
        h.runAfterDelay(100,() -> {
            h.assertBlockPresent(Blocks.NETHER_GOLD_ORE,new BlockPos(13,8,8));
            h.assertBlockPresent(Blocks.CHEST,new BlockPos(18,8,8));
            h.assertBlockPresent(com.vincenthuto.hutoslib.common.registry.HLBlockInit.display_glass.get(),new BlockPos(16,8,8));
            h.assertBlockPresent(Blocks.AIR,new BlockPos(23,8,8));
            for(var e:plan.entrySet())if(e.getValue()==Material.ICHOR)for(Voxel n:e.getKey().neighbors())
                h.assertTrue(!level.getBlockState(pos(n)).isAir(),"Ticking buried core escaped at "+n);
            h.succeed();
        });
    }

    @GameTest(batch="phlegethontic",template=ROOM,timeoutTicks=100)
    public static void tetherExcludesDuplicatesAndClearsAfterOwnerUnloadAndReload(GameTestHelper h) {
        var owner=spawn(h,EntityInit.excoriated.get(),new BlockPos(7,3,7));owner.setNoAi(true);owner.setNoGravity(true);
        owner.setHome(owner.blockPosition(),true);
        Cow victim=spawn(h,EntityType.COW,new BlockPos(14,3,7));victim.setNoAi(true);victim.setNoGravity(true);
        var first=new TestBarb(h.getLevel(),owner);h.getLevel().addFreshEntity(first);first.hit(victim);
        h.assertTrue(first.victimId()==victim.getId(),"First barb must latch");
        victim.invulnerableTime=0;
        var second=new TestBarb(h.getLevel(),owner);h.getLevel().addFreshEntity(second);second.hit(victim);
        h.assertTrue(second.isRemoved(),"Only one tether may claim a victim");
        CompoundTag home=new CompoundTag();owner.addAdditionalSaveData(home);
        var restored=EntityInit.excoriated.get().create(h.getLevel());restored.readAdditionalSaveData(home);
        h.assertTrue(restored.home().equals(owner.home()) && restored.isSentinel()
                && restored.attackState()==ExcoriatedEntity.IDLE,"Home and sentinel identity survive; attack does not");
        CompoundTag saved=new CompoundTag();first.addAdditionalSaveData(saved);
        var reloaded=new TestBarb(h.getLevel(),owner);reloaded.readAdditionalSaveData(saved);reloaded.tick();
        h.assertTrue(reloaded.isRemoved(),"Reload must discard transient combat links");
        owner.discard();first.tick();h.assertTrue(first.isRemoved(),"Owner unload must release the victim");h.succeed();
    }

    private static BlockPos pos(Voxel p){return new BlockPos(p.x(),p.y(),p.z());}

    @GameTest(batch="phlegethontic",template="phlegethontic_patrol_room",timeoutTicks=340)
    public static void guardianNavigatesBackInsideItsHomeRange(GameTestHelper h) {
        for(int x=4;x<72;x++)for(int z=4;z<14;z++)h.setBlock(new BlockPos(x,3,z),BlockInit.blood_scorched_scab.get());
        var guardian=spawn(h,EntityInit.excoriated.get(),new BlockPos(60,4,8));
        guardian.setHome(h.absolutePos(new BlockPos(12,4,8)),true);
        h.runAfterDelay(300,() -> {
            h.assertTrue(guardian.distanceToSqr(Vec3.atBottomCenterOf(guardian.home()))<=32*32,
                    "A displaced guardian must navigate back into its home range");h.succeed();
        });
    }

    @GameTest(batch="phlegethontic",template=ROOM,timeoutTicks=40)
    public static void lowNetherOreReservationsSurviveSerialization(GameTestHelper h) {
        var level=h.getLevel().getServer().getLevel(net.minecraft.world.level.Level.NETHER);
        BlockPos ore=new BlockPos(-1,9,-1);var chunk=level.getChunkAt(ore);
        PhlegethonticOreReservations.begin(level);
        try {PhlegethonticOreReservations.reserve(ore,Blocks.NETHER_QUARTZ_ORE.defaultBlockState());}
        finally {PhlegethonticOreReservations.end();}
        var type=com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes.PHLEGETHONTIC_ORE_RESERVATIONS;
        var saved=chunk.getData(type).serializeNBT(level.registryAccess());
        var restored=new PhlegethonticOreReservations();
        restored.deserializeNBT(level.registryAccess(),saved);chunk.setData(type,restored);
        h.assertTrue(PhlegethonticOreReservations.reserved(level,ore),"Attempted ore cells must survive protochunk persistence");
        h.assertTrue(!PhlegethonticOreReservations.reserved(level,ore.above(16)),"Reservation height must stay inside the low Nether envelope");
        h.succeed();
    }

    @GameTest(batch="phlegethontic",template=ROOM,timeoutTicks=40)
    public static void escharianCenterAndRimHaveNoItemsAndSurvivePartialRemoval(GameTestHelper h) {
        var centerBlock=BlockInit.escharian_overgrowth.get();
        var rimBlock=BlockInit.escharian_overgrowth_rim.get();
        var center=h.absolutePos(new BlockPos(8,4,8));
        var east=center.east();
        h.getLevel().setBlock(center,centerBlock.defaultBlockState(),3);
        h.getLevel().setBlock(east,rimBlock.defaultBlockState(),3);
        h.assertTrue(centerBlock.asItem()==net.minecraft.world.item.Items.AIR,
                "Worldgen-only center must not have a BlockItem");
        h.assertTrue(rimBlock.asItem()==net.minecraft.world.item.Items.AIR,
                "Worldgen-only rim must not have a BlockItem");
        h.getLevel().setBlock(east,Blocks.AIR.defaultBlockState(),3);
        h.assertTrue(h.getLevel().getBlockState(center).is(centerBlock),
                "Breaking a rim block must not collapse the remaining center");
        h.succeed();
    }

    @GameTest(batch="phlegethontic",template=ROOM,timeoutTicks=40)
    public static void anOrdinaryBucketCannotDeleteOrExtractIchor(GameTestHelper h) {
        BlockPos relative=new BlockPos(8,3,8);h.setBlock(relative,BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get());
        BlockPos absolute=h.absolutePos(relative);
        var pickup=BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get().pickupBlock(null,h.getLevel(),absolute,h.getLevel().getBlockState(absolute));
        h.assertTrue(pickup.isEmpty(),"Ordinary extraction must remain unavailable");
        h.assertBlockPresent(BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get(),relative);h.succeed();
    }

    @GameTest(batch="phlegethontic",template=ROOM,timeoutTicks=300)
    public static void aSeparatedGuardianClotsAndRecoversBesideIchor(GameTestHelper h) {
        for(int x=3;x<18;x++)for(int z=3;z<18;z++)h.setBlock(new BlockPos(x,3,z),BlockInit.blood_scorched_scab.get());
        var guardian=spawn(h,EntityInit.excoriated.get(),new BlockPos(9,4,9));guardian.setNoGravity(true);
        guardian.setHome(guardian.blockPosition(),true);guardian.setPersistenceRequired();
        h.runAfterDelay(110,() -> h.assertTrue(guardian.clotted(),"A guardian separated for 100 ticks must clot"));
        h.runAfterDelay(215,() -> {
            h.assertTrue(guardian.getHealth()<52,"A guardian separated for 200 ticks must take damage");
            h.getLevel().setBlock(guardian.blockPosition().east(3),BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get().defaultBlockState(),3);
        });
        h.runAfterDelay(280,() -> {
            h.assertTrue(!guardian.clotted(),"Nearby ichor must reverse clotting");
            h.assertTrue(Math.abs(guardian.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)-.28)<.00001,
                    "Returning to ichor must restore normal movement speed");h.succeed();
        });
    }

    @GameTest(batch="phlegethontic",template=ROOM,timeoutTicks=60)
    public static void twoVictimsCanSeverIndependentlyWithoutActivatingBloodMagic(GameTestHelper h) {
        var owner=spawn(h,EntityInit.excoriated.get(),new BlockPos(5,3,5));owner.setNoAi(true);owner.setNoGravity(true);
        var active=player(h,"barb_active",new BlockPos(11,3,5));
        var inactive=player(h,"barb_inactive",new BlockPos(16,3,5));
        var activeBlood=com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getBloodVolume(active).orElseThrow();
        var inactiveBlood=com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getBloodVolume(inactive).orElseThrow();
        activeBlood.setMaxBloodVolume(5000);activeBlood.setBloodVolume(4000);activeBlood.setActive(true);
        inactiveBlood.setActive(false);
        var first=new TestBarb(h.getLevel(),owner);var second=new TestBarb(h.getLevel(),owner);
        h.getLevel().addFreshEntity(first);h.getLevel().addFreshEntity(second);first.hit(active);second.hit(inactive);
        double beforeEscape=activeBlood.getBloodVolume();
        float health=inactive.getHealth();
        active.setShiftKeyDown(true);inactive.setShiftKeyDown(true);
        for(int i=0;i<19;i++){first.tick();second.tick();}
        h.assertTrue(!first.isRemoved() && !second.isRemoved(),"Nineteen ticks must not sever a tether");
        active.invulnerableTime=0;inactive.invulnerableTime=0;first.tick();second.tick();
        h.assertTrue(first.isRemoved() && second.isRemoved(),"Both players must sever at twenty ticks");
        h.assertTrue(activeBlood.getBloodVolume()==beforeEscape-50,"Active player must pay one percent of maximum blood after the initial hit");
        h.assertTrue(inactive.getHealth()==health-1 && !inactiveBlood.isActive(),"Inactive player pays one damage and stays inactive");
        active.discard();inactive.discard();h.succeed();
    }

    @GameTest(batch="phlegethontic",template=ROOM,timeoutTicks=60)
    public static void soundingWaitsThenPullsAgainstCollisionAndExpires(GameTestHelper h) {
        var owner=spawn(h,EntityInit.excoriated.get(),new BlockPos(5,3,5));owner.setNoAi(true);owner.setNoGravity(true);
        Cow victim=spawn(h,EntityType.COW,new BlockPos(13,3,5));victim.setNoAi(true);victim.setNoGravity(true);
        var barb=new TestBarb(h.getLevel(),owner);h.getLevel().addFreshEntity(barb);barb.hit(victim);
        victim.setDeltaMovement(Vec3.ZERO);
        for(int i=0;i<20;i++)barb.tick();
        h.assertTrue(victim.getDeltaMovement().equals(Vec3.ZERO),"Sounding must not pull during its first twenty ticks");
        barb.tick();h.assertTrue(victim.getDeltaMovement().x<0,"Without a deeper river, sounding must pull toward its owner");
        for(int y=1;y<8;y++)for(int z=2;z<9;z++)h.setBlock(new BlockPos(11,y,z),Blocks.BLACKSTONE);
        double wall=h.absolutePos(new BlockPos(12,3,5)).getX();
        for(int i=0;i<55;i++){barb.tick();victim.move(MoverType.SELF,victim.getDeltaMovement());}
        h.assertTrue(victim.getBoundingBox().minX>=wall-.01,"Tether movement must respect the intervening wall");
        for(int i=0;i<10;i++)barb.tick();
        h.assertTrue(barb.isRemoved(),"A tether must expire after eighty ticks");h.succeed();
    }

    @GameTest(batch="phlegethontic",template=ROOM,timeoutTicks=60)
    public static void piercingVolleyHitsTwoVictimsAndStillStopsAtBlocks(GameTestHelper h) {
        var owner=spawn(h,EntityInit.excoriated.get(),new BlockPos(4,3,4));owner.setNoAi(true);
        Cow first=spawn(h,EntityType.COW,new BlockPos(10,3,4));Cow second=spawn(h,EntityType.COW,new BlockPos(15,3,4));
        var barb=new TestBarb(h.getLevel(),owner,false);h.getLevel().addFreshEntity(barb);
        barb.hit(first);h.assertTrue(!barb.isRemoved() && barb.victimId()<0,"Piercing arrow must continue without tethering");
        barb.hit(second);h.assertTrue(barb.isRemoved() && first.getHealth()==6 && second.getHealth()==6,"Piercing volley must damage two victims once");
        var blocked=new TestBarb(h.getLevel(),owner,false);h.getLevel().addFreshEntity(blocked);
        BlockPos wall=h.absolutePos(new BlockPos(8,3,4));h.getLevel().setBlock(wall,Blocks.BLACKSTONE.defaultBlockState(),3);
        blocked.block(wall);h.assertTrue(blocked.isRemoved(),"Piercing must not bypass blocks");h.succeed();
    }

    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h,String name,BlockPos relative) {
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(UUID.randomUUID(),name),false);
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.setNoGravity(true);
        player.setPos(Vec3.atBottomCenterOf(h.absolutePos(relative)));
        for(int i=0;i<61;i++)player.tick(); // Let vanilla's login protection expire before projectile assertions.
        h.getLevel().addNewPlayer(player);return player;
    }

    @AfterBatch(batch="phlegethontic")
    public static void removeTestPlayers(ServerLevel level) {
        TEST_ENTITIES.forEach(Entity::discard);TEST_ENTITIES.clear();
        for(var player:List.copyOf(level.players()))if(player.getGameProfile().getName().startsWith("barb_"))player.discard();
    }
    private static com.google.gson.JsonElement resource(GameTestHelper h,String path) throws Exception {
        try(var reader=h.getLevel().getServer().getResourceManager().openAsReader(ResourceLocation.fromNamespaceAndPath("hemomancy",path))) {
            return JsonParser.parseReader(reader);
        }
    }
    private static final class TestBarb extends RecallBarbEntity {
        TestBarb(ServerLevel level,ExcoriatedEntity owner){super(level,owner,true);}
        TestBarb(ServerLevel level,ExcoriatedEntity owner,boolean tether){super(level,owner,tether);}
        void hit(LivingEntity victim){onHitEntity(new EntityHitResult(victim));}
        void block(BlockPos pos){onHitBlock(new BlockHitResult(Vec3.atCenterOf(pos),Direction.WEST,pos,false));}
    }
}
