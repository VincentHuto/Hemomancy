package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.summon.*;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes.Kind;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FerricGameTests {
    @GameTest(templateNamespace="hemomancy",template="ductilis_arena",batch="ferric")
    public static void rememberedFormsUnlockFromSharedMagnetismMastery(GameTestHelper h) {
        var owner=player(h);var known=HemoCapabilityAccess.requireKnownManipulations(owner);
        var base=ManipulationInit.sanguine_magnetism.get();var rampart=ManipulationInit.ferric_rampart.get();var spikes=ManipulationInit.ferric_spikes.get();
        var mastery=new ManipLevel(0,0);known.getKnownManips().put(base,mastery);
        known.getKnownManips().put(rampart,new ManipLevel(0,0));known.getKnownManips().put(spikes,new ManipLevel(0,0));
        h.assertTrue(!known.isManipulationAvailable(rampart)&&!known.isManipulationAvailable(spikes),"Forms ignored their level requirements");
        mastery.setCurrentLevel(1);
        h.assertTrue(known.isManipulationAvailable(rampart)&&!known.isManipulationAvailable(spikes),"Rampart did not unlock alone at level one");
        mastery.setCurrentLevel(2);
        com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry.normalizeKnown(known.getKnownManips());
        h.assertTrue(known.isManipulationAvailable(spikes)&&known.getManipLevel(spikes)==known.getManipLevel(base),"Spikes did not share level-two mastery");
        owner.discard();h.succeed();
    }
    @GameTest(templateNamespace="hemomancy",template="ductilis_arena",batch="ferric")
    public static void rampartBlocksBodiesAndFastProjectilesOnlyAcrossItsThinFace(GameTestHelper h) {
        var owner=player(h);var wall=EntityInit.iron_wall.get().create(h.getLevel());
        wall.setPos(h.absoluteVec(new Vec3(10,2,8)));wall.configure(owner,Kind.WALL,Direction.NORTH,120);h.getLevel().addFreshEntity(wall);
        var mob=EntityType.HUSK.create(h.getLevel());mob.setNoAi(true);mob.setPos(wall.position().add(0,0,-2));h.getLevel().addFreshEntity(mob);
        mob.move(MoverType.SELF,new Vec3(0,0,4));
        h.assertTrue(mob.getZ()<wall.getZ(),"Body passed through the rampart");
        mob.setPos(wall.position().add(3,0,-2));mob.move(MoverType.SELF,new Vec3(0,0,4));
        h.assertTrue(mob.getZ()>wall.getZ(),"Wall collision extends beyond the five-block silhouette");
        var arrow=EntityType.ARROW.create(h.getLevel());arrow.setPos(wall.position().add(0,1,-3));arrow.setDeltaMovement(0,0,6);h.getLevel().addFreshEntity(arrow);
        wall.tick();h.assertTrue(arrow.isRemoved(),"Fast projectile crossed a plate between ticks");
        var anchor=wall.position();wall.move(MoverType.PISTON,new Vec3(1,0,0));
        wall.setDeltaMovement(2,.5,1);wall.tick();
        h.assertTrue(wall.position().equals(anchor),"Settled iron drifted from its collision anchor");
        wall.discard();mob.discard();owner.discard();h.succeed();
    }

    @GameTest(templateNamespace="hemomancy",template="ductilis_arena",batch="ferric",timeoutTicks=50)
    public static void adjacentSpikesShareContactIntervalAndIgnoreTheirOwner(GameTestHelper h) {
        var owner=player(h);var origin=h.absoluteVec(new Vec3(10,3,8));
        List<FerricConstructEntity> spikes=new ArrayList<>();
        for(int i=0;i<2;i++) {
            var spike=EntityInit.iron_spike.get().create(h.getLevel());spike.setPos(origin.add(i*.5,0,0));
            spike.configure(owner,Kind.SPIKE,Direction.NORTH,120);h.getLevel().addFreshEntity(spike);spikes.add(spike);
        }
        var target=EntityType.HUSK.create(h.getLevel());target.setNoAi(true);target.setNoGravity(true);
        target.setPos(origin.add(.25,0,0));h.getLevel().addFreshEntity(target);float health=target.getHealth();
        h.runAfterDelay(8,()-> {
            h.assertTrue(target.getHealth()==health-3.5f,"Overlapping blades multiplied contact damage or missed their tell");
            target.invulnerableTime=0;target.setPos(origin.add(.25,0,0));
            spikes.forEach(FerricConstructEntity::tick);
            h.assertTrue(target.getHealth()==health-3.5f,"Blade ignored ten-tick hit interval");
        });
        h.runAfterDelay(18,()-> {
            target.invulnerableTime=0;target.setPos(origin.add(.25,0,0));spikes.forEach(FerricConstructEntity::tick);
            h.assertTrue(target.getHealth()==health-7,"Blade did not rearm after ten ticks");
            owner.setPos(origin);float ownerHealth=owner.getHealth();spikes.forEach(FerricConstructEntity::tick);
            h.assertTrue(owner.getHealth()==ownerHealth,"Spike harmed its caster");
            spikes.forEach(Entity::discard);target.discard();owner.discard();h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy",template="ductilis_arena",batch="ferric")
    public static void unsupportedPlacementRejectsBeforeBloodAndCooldown(GameTestHelper h) {
        var player=player(h);var spell=ManipulationInit.ferric_rampart.get();
        var blood=HemoCapabilityAccess.requireBloodVolume(player);blood.setActive(true);blood.setBloodVolume(4000);
        for(var tendency:com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.values())
            HemoCapabilityAccess.getBloodTendency(player).orElseThrow().setTendencyAlignment(tendency,100);
        var known=HemoCapabilityAccess.requireKnownManipulations(player);
        known.getKnownManips().put(ManipulationInit.sanguine_magnetism.get(),new ManipLevel(4,185));
        known.getKnownManips().put(spell,new ManipLevel(0,0));
        known.setSelectedManip(spell);known.setEquippedManipNames(List.of(spell.getName()));
        player.setPos(h.absoluteVec(new Vec3(12,8,2)));player.setYRot(0);player.setXRot(0);
        double before=blood.getBloodVolume();
        h.assertTrue(!spell.tryPerformAction(player,h.getLevel(),ItemStack.EMPTY,player.blockPosition(),0),"Unsupported cast accepted");
        h.assertTrue(blood.getBloodVolume()==before,"Rejected placement consumed blood");
        for(int x=7;x<=17;x++)for(int z=1;z<=12;z++)h.setBlock(new BlockPos(x,1,z),Blocks.STONE);
        player.setPos(h.absoluteVec(new Vec3(12.5,2,2.5)));player.setXRot(25);
        var placement=com.vincenthuto.hemomancy.common.manipulation.ferric.FerricPlacement.aimed(player,Kind.WALL,18);
        h.assertTrue(placement!=null,"Supported fixture has no clear placement");
        var boat=EntityType.BOAT.create(h.getLevel());boat.setPos(placement.origins().getFirst());h.getLevel().addFreshEntity(boat);
        h.assertTrue(!spell.tryPerformAction(player,h.getLevel(),ItemStack.EMPTY,player.blockPosition(),0)&&blood.getBloodVolume()==before,
                "Vehicle overlap was paid for or accepted");boat.discard();
        h.assertTrue(spell.tryPerformAction(player,h.getLevel(),ItemStack.EMPTY,player.blockPosition(),0),"Rejected placement charged cooldown or family form was unavailable");
        h.assertTrue(blood.getBloodVolume()<before,"Valid wall was free");
        player.discard();h.succeed();
    }

    @GameTest(templateNamespace="hemomancy",template="ductilis_arena",batch="ferric",timeoutTicks=40)
    public static void savedConstructRetainsOwnerOrientationAndAbsoluteExpiry(GameTestHelper h) {
        var owner=player(h);var wall=EntityInit.iron_wall.get().create(h.getLevel());
        wall.setPos(h.absoluteVec(new Vec3(10,2,8)));wall.configure(owner,Kind.WALL,Direction.EAST,12);wall.energize(h.getLevel().getGameTime()+50);
        var tag=new CompoundTag();wall.addAdditionalSaveData(tag);long until=wall.expiresAt();
        wall.discard();
        h.runAfterDelay(15,()-> {
            var restored=EntityInit.iron_wall.get().create(h.getLevel());restored.setPos(h.absoluteVec(new Vec3(10,2,8)));restored.readAdditionalSaveData(tag);
            h.assertTrue(restored.isPlayerConstruct()&&restored.constructFacing()==Direction.EAST&&restored.getCreator()==owner,"Saved construct lost its mode, facing or owner");
            h.assertTrue(restored.expiresAt()==until&&restored.energizedUntil()<=until,"Reload extended a construct or its electricity");
            restored.tick();h.assertTrue(restored.isRemoved(),"Reload resurrected expired wall");owner.discard();h.succeed();
        });
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"ferric"),false);
        var player=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND);new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        player.setPos(h.absoluteVec(new Vec3(2,2,2)));h.getLevel().addNewPlayer(player);return player;
    }
}
