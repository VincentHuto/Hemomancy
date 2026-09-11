package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public class ManipulationBalanceGameTests {
    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_balance")
    public static void ironheartedStoresFourHealthAndCapsAtTen(GameTestHelper h) {
        var p=player(h);
        try {
            var m=ManipulationInit.ironhearted.get();
            m.getAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition(),40);
            near(h,4,HemoCapabilityAccess.getPowerGuardrails(p).getIronHeartHealth(),"Stored shield per cast");
            m.getAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition(),40);
            m.getAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition(),40);
            near(h,10,HemoCapabilityAccess.getPowerGuardrails(p).getIronHeartHealth(),"Stored shield cap");
            h.succeed();
        } finally {p.discard();}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_balance")
    public static void carrionCannotHealFromOverkill(GameTestHelper h) {
        var p=player(h); var target=EntityType.COW.create(h.getLevel());
        target.setPos(p.position().add(0,0,2)); target.setHealth(.25f);
        target.addEffect(new MobEffectInstance(MobEffects.POISON,200)); h.getLevel().addFreshEntity(target); p.setHealth(10);
        try {
            ManipulationInit.carrion_communion.get().getAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition(),0);
            h.assertTrue(p.getHealth()>10 && p.getHealth()<=10.126f,"Drain healing counted overkill: player=" + p.getHealth()
                    + ", target=" + target.getHealth() + ", visible targets="
                    + h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, p.getBoundingBox().inflate(8)).size());
            h.succeed();
        } finally {target.discard();p.discard();}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_balance")
    public static void choirInterceptsThreeShotsPerPaidPulseAndProtectsFriendlyShots(GameTestHelper h) {
        var p=player(h); var enemy=EntityType.ZOMBIE.create(h.getLevel());
        enemy.setPos(p.position().add(0,0,4)); h.getLevel().addFreshEntity(enemy);
        var shots=new ArrayList<Arrow>();
        for(int i=0;i<4;i++){var a=new Arrow(h.getLevel(),enemy,new ItemStack(Items.ARROW),null);a.setPos(p.position().add(i*.2,1,2));h.getLevel().addFreshEntity(a);shots.add(a);}
        var own=new Arrow(h.getLevel(),p,new ItemStack(Items.ARROW),null);own.setPos(p.position().add(0,1,1));h.getLevel().addFreshEntity(own);
        try {
            var m=ManipulationInit.iron_choir.get();m.getAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition(),0);
            m.tickContinuousAction(p,h.getLevel());
            h.assertTrue(shots.stream().filter(a->!a.isRemoved()).count()==1,"Choir volley budget was not three: remaining="
                    + shots.stream().filter(a->!a.isRemoved()).count() + ", visible projectiles="
                    + h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class, p.getBoundingBox().inflate(5)).size());
            h.assertTrue(!own.isRemoved(),"Choir consumed own projectile");h.succeed();
        } finally {ManipulationInit.iron_choir.get().finishContinuousAction(p,false);shots.forEach(Arrow::discard);own.discard();enemy.discard();p.discard();}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_balance")
    public static void stillnessRespondsBetweenUpkeepPulses(GameTestHelper h) {
        var p=player(h);var shot=new Arrow(h.getLevel(),p.getX(),p.getY()+1,p.getZ()+3,new ItemStack(Items.ARROW),null);
        shot.setDeltaMovement(0,0,-3);h.getLevel().addFreshEntity(shot);
        var own=new Arrow(h.getLevel(),p,new ItemStack(Items.ARROW),null);own.setPos(shot.position());own.setDeltaMovement(0,0,3);h.getLevel().addFreshEntity(own);
        try {
            ManipulationInit.absolute_stillness.get().tickContinuousAction(p,h.getLevel());
            near(h,2.25,shot.getDeltaMovement().length(),"Between-pulse projectile friction");
            near(h,3,own.getDeltaMovement().length(),"Own projectile was slowed");h.succeed();
        } finally {shot.discard();own.discard();p.discard();}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_balance")
    public static void forgePreservesRecipeMultiplicityAndStackLimits(GameTestHelper h) {
        var p=player(h);var input=new ItemStack(Items.RAW_IRON,8);p.setItemInHand(InteractionHand.MAIN_HAND,input);
        var result=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,new SingleRecipeInput(input),h.getLevel()).orElseThrow().value().getResultItem(p.registryAccess());
        int original=result.getCount();result.setCount(16);
        try {
            ManipulationInit.pyretic_forge.get().getAction(p,h.getLevel(),input,p.blockPosition());
            h.assertTrue(p.getInventory().countItem(Items.IRON_INGOT)==128,"Forge lost multi-output recipe items");
            for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);h.assertTrue(s.getCount()<=s.getMaxStackSize(),"Forge made oversized stack");}
            h.succeed();
        } finally {result.setCount(original);p.discard();}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_balance")
    public static void furnaceProtectsOwnedAnimalsWhileMaintainingHeatSupport(GameTestHelper h) {
        var p=player(h);h.getLevel().addNewPlayer(p);var pet=EntityType.WOLF.create(h.getLevel());pet.tame(p);pet.setPos(p.position().add(0,0,2));pet.setTicksFrozen(100);h.getLevel().addFreshEntity(pet);
        float health=pet.getHealth();
        try {
            ManipulationInit.furnace_veins.get().getAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition(),0);
            near(h,health,pet.getHealth(),"Furnace damaged pet");h.assertTrue(!pet.isOnFire() && pet.getTicksFrozen()==0,"Furnace did not protect pet");
            h.assertTrue(p.hasEffect(MobEffects.FIRE_RESISTANCE),"Furnace lacks sustained heat protection");h.succeed();
        } finally {pet.discard();p.discard();}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_balance")
    public static void rejectedNeedleHitsDoNotApplyBleed(GameTestHelper h) {
        var p=player(h);var target=EntityType.COW.create(h.getLevel());target.setInvulnerable(true);
        var needle=new ProbeNeedle(h.getLevel(),p);needle.setDeltaMovement(0,0,3);
        try {needle.hit(target);h.assertTrue(!target.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.blood_loss),"Rejected needle applied bleed");h.succeed();}
        finally {needle.discard();target.discard();p.discard();}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_balance")
    public static void dowsingRecognizesNetherOres(GameTestHelper h) throws Exception {
        var method=com.vincenthuto.hemomancy.common.manipulation.ferric.VascularDowsingManip.class.getDeclaredMethod("isOre",net.minecraft.world.level.block.state.BlockState.class);
        method.setAccessible(true);
        for(var block:List.of(net.minecraft.world.level.block.Blocks.NETHER_QUARTZ_ORE,net.minecraft.world.level.block.Blocks.ANCIENT_DEBRIS))
            h.assertTrue((boolean)method.invoke(null,block.defaultBlockState()),"Dowsing excluded "+block);
        h.assertTrue(!(boolean)method.invoke(null,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState()),"Dowsing counted ordinary stone");h.succeed();
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_balance")
    public static void driftClearsOrdinaryAggroButNotBossAggro(GameTestHelper h) {
        var p=player(h);var enemy=EntityType.ZOMBIE.create(h.getLevel());var boss=EntityType.WITHER.create(h.getLevel());
        enemy.setPos(p.position().add(0,0,3));boss.setPos(p.position().add(0,0,5));enemy.setTarget(p);boss.setTarget(p);
        h.getLevel().addFreshEntity(enemy);h.getLevel().addFreshEntity(boss);
        var m=ManipulationInit.penumbral_drift.get();ready(p,m);
        try {
            ManipulationChannelManager.start(p);h.assertTrue(ManipulationChannelManager.isChanneling(p.getUUID()),"Drift failed to start");
            p.tickCount=10;ManipulationReactiveEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            h.assertTrue(enemy.getTarget()==null && boss.getTarget()==p,"Drift aggro policy failed");h.succeed();
        } finally {ManipulationChannelManager.stop(p,false);enemy.discard();boss.discard();p.discard();}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_balance")
    public static void titheProvidesLiquidityAndSettlesOnlyOnce(GameTestHelper h) {
        var p=player(h);var m=ManipulationInit.crimson_tithe.get();ready(p,m);
        var blood=HemoCapabilityAccess.requireBloodVolume(p);blood.setBloodVolume(500);
        try {
            double cost=com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger.collect(p,m,1).effectiveCost();
            h.assertTrue(m.tryPerformAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition(),0),"Tithe rejected");
            near(h,500-cost,blood.getBloodVolume(),"Tithe setup payment");
            var reserve=com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve.get(p);
            near(h,500,reserve,"Tithe issued amount");
            h.assertTrue(reserve-cost>=400,"Tithe net liquidity too low");
            com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve.drainToCover(p,100);
            p.getPersistentData().putLong("hemomancy:crimson_tithe_expiry",h.getLevel().getGameTime());
            com.vincenthuto.hemomancy.common.manipulation.saint.CrimsonTitheManip.tickDebt(p);
            near(h,300-cost,blood.getBloodVolume(),"Tithe repayment");
            double after=blood.getBloodVolume();com.vincenthuto.hemomancy.common.manipulation.saint.CrimsonTitheManip.tickDebt(p);
            near(h,after,blood.getBloodVolume(),"Tithe settled twice");h.succeed();
        } finally {p.discard();}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_balance")
    public static void lignumCompletesWithoutFurtherUpkeep(GameTestHelper h) {
        var p=player(h);var m=ManipulationInit.lignum_mortis.get();ready(p,m);
        p.setPos(p.position().add(.5,0,.5));
        var log=p.blockPosition().offset(0,1,3);
        h.getLevel().setBlockAndUpdate(log,net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState());
        p.setYRot(0);p.setXRot(0);
        try {
            ManipulationChannelManager.start(p);
            h.assertTrue(ManipulationChannelManager.isChanneling(p.getUUID()),"Lignum did not start");
            double paid=HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume();
            for(int i=0;i<25;i++) ManipulationChannelManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            h.assertTrue(!ManipulationChannelManager.isChanneling(p.getUUID()),"Completed selection kept charging");
            h.assertTrue(h.getLevel().getBlockState(log).isAir(),"Completed selection did not harvest");
            near(h,paid,HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume(),"Completed selection paid more upkeep");h.succeed();
        } finally {ManipulationChannelManager.stop(p,false);p.discard();}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_balance")
    public static void coronationSpendsOnePiercingCounterattackPerHostileHit(GameTestHelper h) {
        var p=player(h);var enemy=EntityType.ZOMBIE.create(h.getLevel());enemy.setPos(p.position().add(0,0,4));
        var pet=EntityType.WOLF.create(h.getLevel());pet.tame(p);
        try {
            ManipulationReactiveEvents.armCoronation(p,1,1);
            ManipulationReactiveEvents.onIncomingDamage(new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(p,
                new net.neoforged.neoforge.common.damagesource.DamageContainer(h.getLevel().damageSources().mobAttack(pet),2)));
            var event=new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(p,
                new net.neoforged.neoforge.common.damagesource.DamageContainer(h.getLevel().damageSources().mobAttack(enemy),2));
            ManipulationReactiveEvents.onIncomingDamage(event);ManipulationReactiveEvents.onIncomingDamage(event);
            var needles=h.getLevel().getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity.class,p.getBoundingBox().inflate(4),n->n.getOwner()==p);
            h.assertTrue(needles.size()==1,"Crown consumed friendly hit or duplicated spent counterattack");
            near(h,3,needles.getFirst().getBaseDamage(),"Full crown payload");
            h.assertTrue(needles.getFirst().getPierceLevel()==1,"Full crown lacks piercing");
            needles.forEach(net.minecraft.world.entity.Entity::discard);h.succeed();
        } finally {enemy.discard();pet.discard();p.discard();}
    }

    private static class ProbeNeedle extends com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity {
        ProbeNeedle(net.minecraft.world.level.Level level,ServerPlayer player){super(level,player);}
        void hit(net.minecraft.world.entity.Entity target){super.onHitEntity(new net.minecraft.world.phys.EntityHitResult(target));}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_visuals")
    public static void swordVisualStateSurvivesProjectileReload(GameTestHelper h) {
        var p=player(h);var needle=new ProbeNeedle(h.getLevel(),p);var loaded=new ProbeNeedle(h.getLevel(),p);
        try {
            needle.setCoronationSword(true);needle.configurePiercing((byte)1);
            var tag=new net.minecraft.nbt.CompoundTag();needle.addAdditionalSaveData(tag);loaded.readAdditionalSaveData(tag);
            h.assertTrue(loaded.isCoronationSword() && loaded.getPierceLevel()==1,"Sword appearance or piercing lost on reload");h.succeed();
        } finally {needle.discard();loaded.discard();p.discard();}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_visuals")
    public static void shotVisualFormsSurviveReload(GameTestHelper h) {
        var p=player(h);
        try {
            for(int mode=0;mode<4;mode++) {
                var shot=new com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity(h.getLevel(),p);
                if(mode==1)shot.setHomingTarget(null,60);if(mode==2)shot.setMortar(true);if(mode==3)shot.configureOrbit(p,0);
                var tag=new net.minecraft.nbt.CompoundTag();shot.addAdditionalSaveData(tag);
                var loaded=new com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity(h.getLevel(),p);loaded.readAdditionalSaveData(tag);
                h.assertTrue(loaded.visualForm()==mode,"Shot visual family lost on reload: "+mode);shot.discard();loaded.discard();
            }
            h.succeed();
        } finally {p.discard();}
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_visuals")
    public static void visualPayloadRoundTripsEveryShapeAndClear(GameTestHelper h) {
        for(var form:ManipulationVisuals.Form.values())for(int duration:new int[]{0,25,400}) {
            var packet=new com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket(form,17,
                    new net.minecraft.world.phys.Vec3(2,3,4),new net.minecraft.world.phys.Vec3(12,3,4),1.5f,duration,8);
            var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                var codec=com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket.STREAM_CODEC;
                codec.encode(buffer,packet);h.assertTrue(packet.equals(codec.decode(buffer)),"Visual payload changed "+form);
            } finally {buffer.release();}
        }
        h.succeed();
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_visuals")
    public static void crownSlotsStayDistinctAndBounded(GameTestHelper h) {
        for(int tick=0;tick<400;tick+=17) {
            var slots=new HashSet<net.minecraft.world.phys.Vec3>();
            for(int i=0;i<8;i++) {
                var offset=ManipulationVisuals.swordOffset(tick,i);slots.add(offset);
                near(h,1.15,Math.sqrt(offset.x*offset.x+offset.z*offset.z),"Crown radius");
                h.assertTrue(offset.y>2.9 && offset.y<3.2,"Crown entered aiming lane");
            }
            h.assertTrue(slots.size()==8,"Crown slots overlap");
        }
        h.succeed();
    }

    @GameTest(templateNamespace="minecraft", template="bastion/mobs/empty", batch="manipulation_visuals")
    public static void everyActiveChargedPowerHasBuildup(GameTestHelper h) {
        int count=0;
        for(var holder:ManipulationInit.MANIPS.getEntries()) {
            var power=holder.get();
            if(power.getType()!=EnumManipulationType.CHARGED)continue;
            h.assertTrue(ManipulationVisuals.chargeForm(power.getName())!=null,"Missing charge silhouette: "+power.getName());
            count++;
        }
        h.assertTrue(count>=16,"Charged roster was not exercised");h.succeed();
    }

    private static void ready(ServerPlayer p,BloodManipulation m){
        var known=HemoCapabilityAccess.requireKnownManipulations(p);known.getKnownManips().put(m,new ManipLevel(0,0));
        known.setSelectedManip(m);known.setEquippedManipNames(List.of(m.getName()));
        HemoCapabilityAccess.getBloodTendency(p).orElseThrow().setTendencyAlignment(m.getTend(),(float)m.getAlignLevel());
    }

    private static void near(GameTestHelper h,double expected,double actual,String message){h.assertTrue(Math.abs(expected-actual)<.002,message+": "+actual+" expected "+expected);}
    private static ServerPlayer player(GameTestHelper h){
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"bal_"+UUID.randomUUID().toString().substring(0,8)),false);
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND);new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,p,cookie){@Override public void send(net.minecraft.network.protocol.Packet<?> packet){}};
        p.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2,8,2)));
        HemoCapabilityAccess.requireBloodVolume(p).setActive(true);HemoCapabilityAccess.requireBloodVolume(p).setBloodVolume(5000);return p;
    }
}
