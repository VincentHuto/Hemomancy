package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.ConductionManager;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Discharge;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DuctilisGameTests {
    private static final String EMPTY = "ductilis_arena";

    @GameTest(templateNamespace="hemomancy",template=EMPTY,batch="ductilis")
    public static void paralysisBlocksAlternateUsePacketsButAllowsStop(GameTestHelper h) {
        var player=player(h);
        var known=com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireKnownManipulations(player);
        var passive=ManipulationInit.blackhearted.get();
        known.getKnownManips().put(passive,new com.vincenthuto.hemomancy.common.manipulation.ManipLevel(4,185));
        known.setEquippedManipNames(java.util.List.of(passive.getName())); known.setSelectedManip(passive);
        var context=new net.neoforged.neoforge.network.handling.ServerPayloadContext(player.connection,
                com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UseManipKeyPacket.TYPE.id());
        try {
            boolean active=known.isPassiveActive(passive.getName());
            h.assertTrue(Paralysis.apply(player,20),"Packet fixture paralysis rejected");
            com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UseManipKeyPacket.handle(
                    new com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UseManipKeyPacket(),context);
            h.assertTrue(known.isPassiveActive(passive.getName())==active,"Paralyzed use packet toggled a passive before the cast guard");
            Paralysis.onLogin(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(player));
            com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UseManipKeyPacket.handle(
                    new com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UseManipKeyPacket(),context);
            h.assertTrue(known.isPassiveActive(passive.getName())!=active,"Unparalyzed alternate packet fixture did not toggle");
            var ward=ManipulationInit.sanguine_ward.get();
            var blood=com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireBloodVolume(player);
            blood.setActive(true);blood.setBloodVolume(2000);
            for (var tendency:com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.values())
                com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getBloodTendency(player).orElseThrow().setTendencyAlignment(tendency,100);
            known.getKnownManips().put(ward,new com.vincenthuto.hemomancy.common.manipulation.ManipLevel(4,185));
            known.setEquippedManipNames(java.util.List.of(ward.getName()));known.setSelectedManip(ward);
            com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.start(player);
            h.assertTrue(com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.isChanneling(player.getUUID()),"STOP fixture channel did not start");
            // Simulate a paralysis flag arriving before the next interruption tick so STOP must still drain the channel.
            player.getActiveEffectsMap().put(EffectInit.paralysis,new net.minecraft.world.effect.MobEffectInstance(EffectInit.paralysis,20));
            h.assertTrue(Paralysis.isParalyzed(player),"STOP fixture lacks paralysis");
            com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UseManipKeyPacket.handle(
                    com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UseManipKeyPacket.stopContinuous(),context);
            h.assertTrue(!com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.isChanneling(player.getUUID()),"Paralysis blocked STOP_CONTINUOUS cleanup");
            h.succeed();
        } finally { com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.stop(player,false);player.discard(); }
    }

    @GameTest(templateNamespace="hemomancy",template=EMPTY,batch="ductilis")
    public static void paralysisLifecycleClearsEffectsRecoveryAndRespectsImmunity(GameTestHelper h) {
        for (int reason=0;reason<5;reason++) {
            var player=player(h);
            try {
                h.assertTrue(Paralysis.apply(player,20),"Lifecycle fixture paralysis rejected");
                player.removeEffect(EffectInit.paralysis);
                h.assertTrue(!Paralysis.apply(player,20),"Lifecycle fixture has no recovery");
                player.getActiveEffectsMap().put(EffectInit.paralysis,new net.minecraft.world.effect.MobEffectInstance(EffectInit.paralysis,20));
                if (reason==0) {
                    player.setHealth(0);
                    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(player,player.damageSources().generic()));
                    player.setHealth(20);
                }
                if (reason==1) net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
                if (reason==2) net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(player));
                if (reason==3) net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent(player,false));
                if (reason==4) net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent(player,net.minecraft.world.level.Level.OVERWORLD,net.minecraft.world.level.Level.NETHER));
                h.assertTrue(!player.hasEffect(EffectInit.paralysis),"Lifecycle retained paralysis: "+reason);
                h.assertTrue(Paralysis.apply(player,20),"Lifecycle retained recovery: "+reason);
            } finally { player.discard(); }
        }
        var immune=new net.minecraft.world.entity.monster.Husk(EntityType.HUSK,h.getLevel()) {
            @Override public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
                return !effect.is(EffectInit.paralysis) && super.canBeAffected(effect);
            }
        };
        h.assertTrue(!Paralysis.apply(immune,60) && !Paralysis.isParalyzed(immune),"Effect immunity was bypassed by hard paralysis");
        h.assertTrue(!immune.isNoAi(),"Immune mob received persistent NoAI");
        immune.discard();h.succeed();
    }

    @GameTest(templateNamespace="hemomancy",template=EMPTY,batch="ductilis",timeoutTicks=50)
    public static void energizedWallOnlyDischargesOnContact(GameTestHelper h) {
        var owner=player(h);
        var wall=com.vincenthuto.hemomancy.common.init.EntityInit.iron_wall.get().create(h.getLevel());
        wall.setPos(h.absoluteVec(new Vec3(8,2,8)));
        wall.configure(owner,com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes.Kind.WALL,
                net.minecraft.core.Direction.NORTH,100); h.getLevel().addFreshEntity(wall);
        var touching=mob(h,new Vec3(8,2,8.5)); touching.setNoGravity(true);
        var distant=mob(h,new Vec3(8,2,11)); distant.setNoGravity(true);
        ConductionManager.energizeRelay(owner,wall,new Discharge());
        h.runAfterDelay(11,()-> {
            h.assertTrue(touching.getHealth()<touching.getMaxHealth(),"Energized wall did not discharge on contact");
            h.assertTrue(distant.getHealth()==distant.getMaxHealth(),"Wall incorrectly emitted a pillar area discharge");
            touching.discard();distant.discard();wall.discard();owner.discard();h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy",template=EMPTY,batch="ductilis",timeoutTicks=50)
    public static void emptyStormCanEnergizeAnAimedConductor(GameTestHelper h) {
        var owner=player(h); owner.setPos(h.absoluteVec(new Vec3(12.5,4,12.5))); owner.setNoGravity(true);owner.setYRot(0);owner.setXRot(90);
        h.setBlock(new BlockPos(12,1,12),Blocks.IRON_BLOCK);
        h.assertTrue(h.getLevel().getEntitiesOfClass(LivingEntity.class,owner.getBoundingBox().inflate(18),
                e->ConductionManager.canHarm(owner,e) && owner.distanceToSqr(e)<=18*18).isEmpty(),"Empty Storm fixture contains a nearby victim");
        Vec3 aim=Vec3.atCenterOf(h.absolutePos(new BlockPos(12,1,12))).subtract(owner.getEyePosition());
        owner.setXRot((float)-Math.toDegrees(Math.atan2(aim.y,Math.sqrt(aim.x*aim.x+aim.z*aim.z))));
        owner.setYRot((float)Math.toDegrees(Math.atan2(-aim.x,aim.z)));
        var ray=h.getLevel().clip(new net.minecraft.world.level.ClipContext(owner.getEyePosition(),
                owner.getEyePosition().add(owner.getLookAngle().scale(18)),net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.ANY,owner));
        h.assertTrue(ray.getBlockPos().equals(h.absolutePos(new BlockPos(12,1,12))),"Storm fixture ray missed iron: "+ray.getBlockPos()+", expected="+h.absolutePos(new BlockPos(12,1,12))+", eye="+owner.getEyePosition()+", look="+owner.getLookAngle());
        ManipulationInit.synaptic_storm.get().getAction(owner,h.getLevel(),ItemStack.EMPTY,owner.blockPosition(),60);
        var victim=mob(h,new Vec3(12.5,2,12.5)); victim.setNoGravity(true);
        h.runAfterDelay(11,()-> {
            h.assertTrue(victim.getHealth()<victim.getMaxHealth(),"Empty Storm did not energize aimed iron");
            victim.discard();owner.discard();h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy",template=EMPTY,batch="ductilis",timeoutTicks=50)
    public static void activationAirStrikesAndSeedsRespectBlockingGeometry(GameTestHelper h) {
        var owner=player(h); owner.setPos(h.absoluteVec(new Vec3(5.5,2,5.5)));
        var hidden=mob(h,new Vec3(8.5,2,5.5)); hidden.setNoGravity(true);
        var marked=mob(h,new Vec3(6,2,5.5)); marked.setNoGravity(true);
        com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper.markConductive(marked,80);
        for (int y=1;y<=5;y++) for (int z=3;z<=7;z++) h.setBlock(new BlockPos(7,y,z),Blocks.STONE);
        h.setBlock(new BlockPos(8,1,5),Blocks.IRON_BLOCK);
        h.setBlock(new BlockPos(4,1,8),Blocks.IRON_BLOCK);
        ManipulationInit.activation_potential.get().getAction(owner,h.getLevel(),ItemStack.EMPTY,owner.blockPosition(),30);
        h.assertTrue(hidden.getHealth()==hidden.getMaxHealth(),"Activation struck a body through a stone wall");
        var npc=mob(h,new Vec3(5.5,2,5.5)); npc.setNoGravity(true);
        var hiddenPig=EntityType.PIG.create(h.getLevel()); hiddenPig.setPos(h.absoluteVec(new Vec3(8.5,2,5.5)));
        hiddenPig.setNoAi(true); hiddenPig.setNoGravity(true); h.getLevel().addFreshEntity(hiddenPig); npc.setTarget(hiddenPig);
        var context=com.vincenthuto.hemomancy.common.manipulation.ManipulationCastContext.forWill(npc,hiddenPig,ItemStack.EMPTY);
        com.vincenthuto.hemomancy.common.manipulation.EntityManipulationEffects.cast(ManipulationInit.activation_potential.get(),context);
        h.assertTrue(hiddenPig.getHealth()==hiddenPig.getMaxHealth(),"Entity Activation struck through stone");
        h.assertTrue(!com.vincenthuto.hemomancy.common.manipulation.EntityManipulationEffects.cast(ManipulationInit.synaptic_jolt.get(),context)
                && hiddenPig.getHealth()==hiddenPig.getMaxHealth(),"Entity Jolt struck through stone");
        var exposed=mob(h,new Vec3(4.5,2,8.5)); exposed.setNoGravity(true);
        h.runAfterDelay(11,()-> {
            h.assertTrue(hidden.getHealth()==hidden.getMaxHealth(),"Activation energized an isolated hidden conductor through stone");
            h.assertTrue(exposed.getHealth()<exposed.getMaxHealth(),"Activation missed nearby exposed iron");
            h.assertTrue(hiddenPig.getHealth()==hiddenPig.getMaxHealth(),"Entity Activation energized hidden iron");
            hidden.discard();marked.discard();npc.discard();hiddenPig.discard();exposed.discard();owner.discard();h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy",template=EMPTY,batch="ductilis")
    public static void contactUsesShallowWaterAndPartialBlockShape(GameTestHelper h) {
        var body=mob(h,new Vec3(6.5,1.7,6.5));
        BlockPos pos=h.absolutePos(new BlockPos(6,1,6));
        try {
            h.getLevel().setBlockAndUpdate(pos,Blocks.WATER.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL,7));
            h.assertTrue(!ConductionManager.conductive(body),"Body above shallow water counted as touching it");
            body.setPos(h.absoluteVec(new Vec3(6.5,1.02,6.5)));
            h.assertTrue(ConductionManager.conductive(body),"Body inside shallow water did not conduct");
            h.getLevel().setBlockAndUpdate(pos,Blocks.CUT_COPPER_SLAB.defaultBlockState());
            body.setPos(h.absoluteVec(new Vec3(6.5,1.75,6.5)));
            h.assertTrue(!ConductionManager.conductive(body),"Empty upper slab voxel counted as metal contact");
            body.setPos(h.absoluteVec(new Vec3(6.5,1.5,6.5)));
            h.assertTrue(ConductionManager.conductive(body),"Standing on copper slab did not conduct");
            h.getLevel().setBlockAndUpdate(pos,Blocks.IRON_BARS.defaultBlockState());
            body.setBoundingBox(new net.minecraft.world.phys.AABB(pos.getX()+.1,pos.getY()+.2,pos.getZ()+.1,
                    pos.getX()+.2,pos.getY()+.4,pos.getZ()+.2));
            h.assertTrue(!ConductionManager.conductive(body),"Empty corner of iron bars counted as metal contact");
            h.succeed();
        } finally { body.discard(); }
    }

    @GameTest(templateNamespace="hemomancy",template=EMPTY,batch="ductilis")
    public static void markedStormCannotExceedItsChargedVictimCap(GameTestHelper h) {
        var caster=player(h); var first=mob(h,new Vec3(4,2,2)); var branch=mob(h,new Vec3(5,2,2));
        try {
            com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper.markConductive(first,80);
            ManipulationInit.synaptic_storm.get().getAction(caster,h.getLevel(),ItemStack.EMPTY,caster.blockPosition(),1);
            h.assertTrue(first.getHealth()<first.getMaxHealth(),"Primary marked victim missed");
            h.assertTrue(branch.getHealth()==branch.getMaxHealth(),"Mark exceeded a one-victim Storm charge");
            h.succeed();
        } finally { first.discard();branch.discard();caster.discard(); }
    }

    @GameTest(templateNamespace="hemomancy",template=EMPTY,batch="ductilis")
    public static void markedStormCannotBranchAroundAnObstructedHop(GameTestHelper h) {
        var caster=player(h); caster.setPos(h.absoluteVec(new Vec3(3,2,5)));
        var first=mob(h,new Vec3(6,2,5)); var hidden=mob(h,new Vec3(8.5,2,5));
        for (int y=1;y<=5;y++) for (int z=3;z<=7;z++) h.setBlock(new BlockPos(7,y,z),Blocks.STONE);
        try {
            com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper.markConductive(first,80);
            ManipulationInit.synaptic_storm.get().getAction(caster,h.getLevel(),ItemStack.EMPTY,caster.blockPosition(),60);
            h.assertTrue(first.getHealth()<first.getMaxHealth(),"Primary marked victim missed");
            h.assertTrue(hidden.getHealth()==hidden.getMaxHealth(),"Mark branched around Storm air line of sight");
            h.succeed();
        } finally { first.discard();hidden.discard();caster.discard(); }
    }

    @GameTest(templateNamespace="hemomancy",template=EMPTY,batch="ductilis",timeoutTicks=50)
    public static void olderNetworkCannotShortenARefreshedRelay(GameTestHelper h) {
        var owner=player(h);
        var relay=com.vincenthuto.hemomancy.common.init.EntityInit.iron_pillar.get().create(h.getLevel());
        relay.setPos(h.absoluteVec(new Vec3(6.5,2,6.5)));
        relay.configure(owner,com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes.Kind.PILLAR,
                net.minecraft.core.Direction.NORTH,100); h.getLevel().addFreshEntity(relay);
        BlockPos seed=h.absolutePos(new BlockPos(6,1,6)); h.getLevel().setBlockAndUpdate(seed,Blocks.IRON_BLOCK.defaultBlockState());
        ConductionManager.energizeAt(owner,seed,new Discharge());
        long[] refreshed={0};
        h.runAfterDelay(15,()-> {
            ConductionManager.energizeRelay(owner,relay,new Discharge()); refreshed[0]=relay.energizedUntil();
        });
        h.runAfterDelay(21,()-> {
            h.assertTrue(relay.energizedUntil()==refreshed[0],"Old network shortened newer relay energization");
            relay.discard();owner.discard();h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy",template=EMPTY,batch="ductilis",timeoutTicks=50)
    public static void entityJoltEnergizesTerrainAndKeepsItsCasterSafe(GameTestHelper h) {
        var caster=mob(h,new Vec3(4.5,2,5.5));
        var target=EntityType.PIG.create(h.getLevel()); target.setPos(h.absoluteVec(new Vec3(6.5,2,5.5)));
        target.setNoAi(true); target.setNoGravity(true); h.getLevel().addFreshEntity(target); caster.setTarget(target);
        var bystander=EntityType.PIG.create(h.getLevel()); bystander.setPos(h.absoluteVec(new Vec3(7.5,2,5.5)));
        bystander.setNoAi(true); bystander.setNoGravity(true); h.getLevel().addFreshEntity(bystander);
        for (int x=4;x<=7;x++) h.setBlock(new BlockPos(x,1,5),Blocks.IRON_BLOCK);
        var context=com.vincenthuto.hemomancy.common.manipulation.ManipulationCastContext.forWill(caster,target,ItemStack.EMPTY);
        h.assertTrue(com.vincenthuto.hemomancy.common.manipulation.EntityManipulationEffects.cast(ManipulationInit.synaptic_jolt.get(),context),"Entity Jolt failed");
        h.assertTrue(target.getHealth()<target.getMaxHealth(),"Entity Jolt lost direct damage");
        h.runAfterDelay(11,()-> {
            h.assertTrue(bystander.getHealth()<bystander.getMaxHealth(),"Entity cast did not energize touching iron");
            h.assertTrue(caster.getHealth()==caster.getMaxHealth(),"Conductor damaged its entity caster");
            caster.discard();target.discard();bystander.discard();h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy", template=EMPTY, batch="ductilis")
    public static void joltInterruptsItemUseAndAnActiveChannel(GameTestHelper h) {
        ServerPlayer player=player(h);
        var ward=ManipulationInit.sanguine_ward.get();
        var blood=com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireBloodVolume(player);
        blood.setActive(true); blood.setBloodVolume(2000);
        for (var tendency:com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.values())
            com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getBloodTendency(player).orElseThrow().setTendencyAlignment(tendency,100);
        var known=com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireKnownManipulations(player);
        known.getKnownManips().put(ward,new com.vincenthuto.hemomancy.common.manipulation.ManipLevel(4,185));
        known.setEquippedManipNames(java.util.List.of(ward.getName())); known.setSelectedManip(ward);
        try {
            com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.start(player);
            h.assertTrue(com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.isChanneling(player.getUUID()),"Fixture channel did not start");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BOW));
            player.startUsingItem(InteractionHand.MAIN_HAND);
            com.vincenthuto.hemomancy.common.manipulation.ductilis.SynapticJoltManip.staggerTarget(player);
            h.assertTrue(!player.isUsingItem(),"Jolt left item use active");
            h.assertTrue(!com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.isChanneling(player.getUUID()),"Jolt left the channel active");
            h.succeed();
        } finally { com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.stop(player,false); player.discard(); }
    }

    @GameTest(templateNamespace="hemomancy", template=EMPTY, batch="ductilis")
    public static void stormUsesAnEnergizedRelayAndConductiveVictimPriority(GameTestHelper h) {
        ServerPlayer caster=player(h);
        caster.setPos(h.absoluteVec(new Vec3(2,2,5)));
        var ordinary=mob(h,new Vec3(5,2,5));
        var marked=mob(h,new Vec3(18,2,5));
        var relay=com.vincenthuto.hemomancy.common.init.EntityInit.iron_pillar.get().create(h.getLevel());
        relay.setPos(h.absoluteVec(new Vec3(11,2,5)));
        relay.configure(caster,com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes.Kind.PILLAR,
                net.minecraft.core.Direction.NORTH,100);
        h.getLevel().addFreshEntity(relay);
        ConductionManager.energizeRelay(caster,relay,new Discharge());
        // Both relay and marked victim are conductive; relay is nearer and should lead the path.
        com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper.markConductive(marked,100);
        try {
            ManipulationInit.synaptic_storm.get().getAction(caster,h.getLevel(),ItemStack.EMPTY,caster.blockPosition(),1);
            h.assertTrue(marked.getHealth()<marked.getMaxHealth(),"Relay consumed the sole victim slot or did not extend range");
            h.assertTrue(ordinary.getHealth()==ordinary.getMaxHealth(),"Nearest ordinary entity overrode conductive priority");
            h.succeed();
        } finally { ordinary.discard(); marked.discard(); relay.discard(); caster.discard(); }
    }

    @GameTest(templateNamespace="hemomancy", template=EMPTY, batch="ductilis")
    public static void activationAndConductorHitsRespectOwnersTeamsAndPvp(GameTestHelper h) {
        ServerPlayer caster=player(h),other=player(h);
        var wolf=EntityType.WOLF.create(h.getLevel()); wolf.tame(caster);
        wolf.setPos(caster.position().add(1,0,0)); h.getLevel().addFreshEntity(wolf);
        var enemy=mob(h,new Vec3(4,2,2));
        boolean pvp=h.getLevel().getServer().isPvpAllowed(); h.getLevel().getServer().setPvpAllowed(false);
        try {
            ManipulationInit.activation_potential.get().getAction(caster,h.getLevel(),ItemStack.EMPTY,caster.blockPosition(),30);
            h.assertTrue(enemy.getHealth()<enemy.getMaxHealth(),"Eligible activation victim missed");
            h.assertTrue(wolf.getHealth()==wolf.getMaxHealth() && !wolf.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Owned wolf disrupted");
            h.assertTrue(other.getHealth()==other.getMaxHealth(),"PvP-disabled player hit");
            h.assertTrue(!ConductionManager.canHarm(caster,wolf)&&!ConductionManager.canHarm(caster,other),"Conductor protections diverge from direct cast");
            h.succeed();
        } finally { h.getLevel().getServer().setPvpAllowed(pvp); wolf.discard(); enemy.discard(); other.discard(); caster.discard(); }
    }

    @GameTest(templateNamespace="hemomancy", template=EMPTY, batch="ductilis")
    public static void paralysisLocksPlayerActionsCapsPlayersAndReducesBosses(GameTestHelper h) {
        ServerPlayer player=player(h); var target=mob(h,new Vec3(3,2,2));
        var boss=EntityType.WITHER.create(h.getLevel());
        try {
            h.assertTrue(Paralysis.apply(player,60),"Player paralysis rejected");
            h.assertTrue(player.getEffect(EffectInit.paralysis).getDuration()==20,"Player lock exceeded20 ticks");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BOW));
            player.startUsingItem(InteractionHand.MAIN_HAND);
            h.assertTrue(!player.isUsingItem(),"Player started item use during paralysis");
            player.attack(target);
            h.assertTrue(target.getHealth()==target.getMaxHealth(),"Player attacked during paralysis");
            h.assertTrue(!ManipulationInit.synaptic_jolt.get().tryPerformAction(player,h.getLevel(),ItemStack.EMPTY,player.blockPosition(),0),"Player cast during paralysis");
            h.assertTrue(Paralysis.apply(boss,60) && !boss.hasEffect(EffectInit.paralysis)
                    && boss.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Boss received hard paralysis");
            player.removeEffectsCuredBy(net.neoforged.neoforge.common.EffectCures.MILK);
            h.assertTrue(!Paralysis.isParalyzed(player)&&!Paralysis.apply(player,60),"Milk left lock or bypassed recovery");
            h.succeed();
        } finally { player.discard(); target.discard(); boss.discard(); }
    }

    @GameTest(templateNamespace="hemomancy", template=EMPTY, batch="ductilis")
    public static void directAndMarkedAndSeparateRelayDischargesShareHitAccounting(GameTestHelper h) {
        ServerPlayer caster=player(h); var first=mob(h,new Vec3(4,2,2)); var second=mob(h,new Vec3(6,2,2));
        try {
            var discharge=new Discharge();
            h.assertTrue(ConductionManager.claimHit(caster,first,discharge),"Direct victim was not claimable");
            h.assertTrue(!ConductionManager.claimHit(caster,first,new Discharge()),"Separate relay multiplied a direct hit");
            com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper.markConductive(first,80);
            h.assertTrue(com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper.tryTriggerConductiveArc(caster,first,
                    com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.DUCTILIS,null,3,discharge),"Mark did not arc");
            h.assertTrue(second.getHealth()<second.getMaxHealth(),"Marked arc had no damage");
            h.assertTrue(!ConductionManager.claimHit(caster,second,new Discharge()),"Environment could hit the marked-arc victim twice");
            h.succeed();
        } finally { first.discard(); second.discard(); caster.discard(); }
    }

    @GameTest(templateNamespace="hemomancy", template=EMPTY, batch="ductilis")
    public static void stormCannotJumpAnAirGapBetweenVictims(GameTestHelper h) {
        ServerPlayer caster = player(h);
        var first = mob(h, new Vec3(2,2,7));
        var second = mob(h, new Vec3(13,2,7));
        caster.setPos(h.absoluteVec(new Vec3(7,2,7)));
        try {
            ManipulationInit.synaptic_storm.get().getAction(caster,h.getLevel(),ItemStack.EMPTY,caster.blockPosition(),60);
            h.assertTrue(first.getHealth() < first.getMaxHealth(), "First visible victim was skipped");
            h.assertTrue(second.getHealth() == second.getMaxHealth(), "Storm crossed an 11-block ordinary air gap");
            h.succeed();
        } finally { first.discard(); second.discard(); caster.discard(); }
    }

    @GameTest(templateNamespace="hemomancy", template=EMPTY, batch="ductilis", timeoutTicks=100)
    public static void paralysisExpiresAndRecoveryStartsAfterRemoval(GameTestHelper h) {
        for (int x=3;x<=5;x++) for (int z=3;z<=5;z++) h.setBlock(new BlockPos(x,1,z),Blocks.STONE);
        var target = mob(h,new Vec3(4,5,4));
        target.setPersistenceRequired(); // Other test players can be beyond the hostile-mob despawn radius.
        target.setNoAi(false);
        h.assertTrue(Paralysis.apply(target, 10), "Initial paralysis rejected");
        target.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BOW));
        target.startUsingItem(InteractionHand.MAIN_HAND);
        h.assertTrue(!target.isUsingItem(), "Paralyzed mob began using an item");
        h.assertTrue(!target.isNoAi(), "Paralysis persisted NoAI");
        double initialY = target.getY();
        h.runAfterDelay(12, () -> {
            h.assertTrue(!target.hasEffect(EffectInit.paralysis), "Paralysis effect did not expire");
            h.assertTrue(target.getY() < initialY, "Paralysis stopped gravity");
            h.assertTrue(!target.isNoAi(),"Paralysis left NoAI after expiry");
            target.setNoAi(true);
            h.assertTrue(!Paralysis.apply(target,60), "Paralysis ignored recovery after expiry");
        });
        h.runAfterDelay(33, () -> {
            h.assertTrue(target.isAlive(),"Recovery fixture died: y="+target.getY()+", removed="+target.isRemoved());
            h.assertTrue(Paralysis.apply(target,60), "Paralysis recovery never ended");
            target.removeEffect(EffectInit.paralysis);
            h.assertTrue(!Paralysis.isParalyzed(target), "Dispel left a hard lock");
            h.assertTrue(!Paralysis.apply(target,60), "Dispel did not start recovery");
            target.discard(); h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy", template=EMPTY, batch="ductilis", timeoutTicks=100)
    public static void conductorPulseRechecksBrokenBridgeAndNeverChangesBlocks(GameTestHelper h) {
        ServerPlayer caster = player(h);
        BlockPos seed = h.absolutePos(new BlockPos(3,1,3));
        for (int x=0;x<4;x++) h.getLevel().setBlockAndUpdate(seed.east(x),Blocks.IRON_BLOCK.defaultBlockState());
        var touching = mob(h,new Vec3(6.5,2,3.5));
        touching.setNoGravity(true);
        ConductionManager.energizeAt(caster,seed,new Discharge());
        h.getLevel().setBlockAndUpdate(seed.east(),Blocks.AIR.defaultBlockState());
        h.runAfterDelay(12, () -> {
            h.assertTrue(touching.getHealth() == touching.getMaxHealth(), "Pulse crossed a removed bridge");
            h.assertTrue(h.getLevel().getBlockState(seed).is(Blocks.IRON_BLOCK), "Conduction mutated seed");
            h.assertTrue(h.getLevel().getBlockState(seed.east()).isAir(), "Conduction restored a broken block");
            touching.discard(); caster.discard(); h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy", template=EMPTY, batch="ductilis", timeoutTicks=100)
    public static void waterNetworkPulsesAtMostThreeAndExpires(GameTestHelper h) {
        ServerPlayer caster=player(h);
        BlockPos seed=h.absolutePos(new BlockPos(5,1,5));
        var victims=new java.util.ArrayList<net.minecraft.world.entity.monster.Husk>();
        for (int x=0;x<4;x++) {
            h.getLevel().setBlockAndUpdate(seed.east(x),Blocks.WATER.defaultBlockState());
            var victim=mob(h,new Vec3(5.5+x,1,5.5)); victim.setNoGravity(true); victims.add(victim);
        }
        com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper.markConductive(victims.getFirst(),80);
        h.assertTrue(ConductionManager.energizeAt(caster,seed,new Discharge()),"Water did not conduct");
        h.runAfterDelay(11,()-> {
            long hits=victims.stream().filter(v->v.getHealth()<v.getMaxHealth()).count();
            h.assertTrue(hits==3,"Water pulse hit "+hits+" victims instead of3");
        });
        h.runAfterDelay(41,()-> {
            float[] health=new float[victims.size()];
            for (int i=0;i<health.length;i++) health[i]=victims.get(i).getHealth();
            h.runAfterDelay(12,()-> {
                for (int i=0;i<health.length;i++) h.assertTrue(victims.get(i).getHealth()==health[i],"Expired network still damaged victim");
                victims.forEach(LivingEntity::discard);caster.discard();h.succeed();
            });
        });
    }

    private static net.minecraft.world.entity.monster.Husk mob(GameTestHelper h, Vec3 position) {
        var mob = EntityType.HUSK.create(h.getLevel());
        mob.setPos(h.absoluteVec(position)); mob.setNoAi(true); h.getLevel().addFreshEntity(mob); return mob;
    }

    static ServerPlayer player(GameTestHelper h) {
        return player(h,packet->{});
    }

    static ServerPlayer player(GameTestHelper h,java.util.function.Consumer<net.minecraft.network.protocol.Packet<?>> sink) {
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"duct_"+UUID.randomUUID().toString().substring(0,8)),false);
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND); new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,p,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { sink.accept(packet); }
        };
        p.setPos(h.absoluteVec(new Vec3(2,2,2))); h.getLevel().addNewPlayer(p); return p;
    }
}
