package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.*;
import com.vincenthuto.hemomancy.common.network.axonal.AxonalStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("naeglerophaeon_validation")
@PrefixGameTestTemplate(false)
public final class AxonalTransductionGameTests {
    private static final String ARENA = "ductilis_arena";
    private static final class Fixture implements AutoCloseable {
        final ServerPlayer player;
        final BlockPos source;
        final BlockPos exit;
        long token;
        int teleportId = -1;
        Fixture(GameTestHelper h) {
            source = h.absolutePos(new BlockPos(2,3,2));
            exit = source.offset(4,0,0);
            for (BlockPos p : BlockPos.betweenClosed(source.offset(-2,-1,-2), exit.offset(2,4,2))) h.getLevel().setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            h.getLevel().setBlock(source, BlockInit.synaptic_node.get().defaultBlockState(),3);
            h.getLevel().setBlock(exit, BlockInit.synaptic_node.get().defaultBlockState(),3);
            for (int i=1;i<4;i++) h.getLevel().setBlock(source.offset(i,0,0),
                    BlockInit.nerve_fiber.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,net.minecraft.core.Direction.Axis.X),3);
            player = DuctilisGameTests.player(h, packet -> {
                if (packet instanceof ClientboundCustomPayloadPacket custom && custom.payload() instanceof AxonalStatePacket state && state.active()) token = state.token();
                if (packet instanceof net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket pose) teleportId = pose.getId();
            });
            HemoCapabilityAccess.getInitiatoryDegree(player).orElseThrow().setDegreeNumber(6);
            HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
            HemoCapabilityAccess.requireBloodVolume(player).setBloodVolume(10000);
            HemoCapabilityAccess.getBloodTendency(player).orElseThrow().setTendencyAlignment(EnumBloodTendency.DUCTILIS,100);
            var known = HemoCapabilityAccess.requireKnownManipulations(player);
            known.getKnownManips().put(ManipulationInit.axonal_transduction.get(),new ManipLevel(0,0));
            known.setEquippedManipNames(java.util.List.of("axonal_transduction"));
            if (!known.isPassiveActive("axonal_transduction")) known.togglePassive("axonal_transduction");
            player.setPos(Vec3.atBottomCenterOf(source.above())); player.setYRot(-90); player.setXRot(0);
        }
        void enter() { AxonalTransductionManager.tick(player); }
        void forward() { AxonalTransductionManager.input(player,token,1,false); AxonalTransductionManager.tick(player); }
        public void close() { AxonalTransductionManager.end(player,true); player.discard(); }
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void passiveEntryChargesOnceAndArrivesAtTheNextNode(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            double before = HemoCapabilityAccess.requireBloodVolume(f.player).getBloodVolume(); f.enter();
            h.assertTrue(AxonalTransductionManager.isTraveling(f.player),"Node contact did not trigger the equipped passive");
            h.assertTrue(f.token > 0,"Owner did not receive a travel synchronization token");
            double after = HemoCapabilityAccess.requireBloodVolume(f.player).getBloodVolume();
            h.assertTrue(Math.abs(before-after-150)<.001,"Entry did not pay exactly one base cost: "+(before-after));
            for (int i=0;i<3;i++) f.forward();
            h.assertTrue(!AxonalTransductionManager.isTraveling(f.player),"Destination node did not reform the traveler");
            h.assertTrue(f.player.position().distanceToSqr(Vec3.atBottomCenterOf(f.exit.above()))<.01,"Arrival skipped the node");
            AxonalTransductionManager.tick(f.player);
            h.assertTrue(!AxonalTransductionManager.isTraveling(f.player),"Arrival immediately sucked the player in again");
            h.assertTrue(HemoCapabilityAccess.requireBloodVolume(f.player).getBloodVolume()==after,"Travel charged upkeep or duplicate entry");
            h.assertTrue(!f.player.noPhysics&&!f.player.isNoGravity(),"Arrival leaked movement flags");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void eligibilitySneakingAndMissingFibersRefuseEntryWithoutPayment(GameTestHelper h) {
        try (Fixture f=new Fixture(h)) {
            double blood=HemoCapabilityAccess.requireBloodVolume(f.player).getBloodVolume();
            f.player.setShiftKeyDown(true); f.enter(); h.assertTrue(!AxonalTransductionManager.isTraveling(f.player),"Sneaking did not bypass entry");
            f.player.setShiftKeyDown(false); HemoCapabilityAccess.getInitiatoryDegree(f.player).orElseThrow().setDegreeNumber(5); f.enter();
            h.assertTrue(!AxonalTransductionManager.isTraveling(f.player),"D5 bypassed PERFECTUS eligibility");
            HemoCapabilityAccess.getInitiatoryDegree(f.player).orElseThrow().setDegreeNumber(6);
            HemoCapabilityAccess.getBloodTendency(f.player).orElseThrow().setTendencyAlignment(EnumBloodTendency.DUCTILIS,64); f.enter();
            h.assertTrue(!AxonalTransductionManager.isTraveling(f.player),"Insufficient alignment entered the network");
            HemoCapabilityAccess.getBloodTendency(f.player).orElseThrow().setTendencyAlignment(EnumBloodTendency.DUCTILIS,100);
            h.getLevel().setBlock(f.source.offset(1,0,0),Blocks.AIR.defaultBlockState(),3); f.enter();
            h.assertTrue(!AxonalTransductionManager.isTraveling(f.player),"Isolated node entered a nonexistent route");
            h.assertTrue(HemoCapabilityAccess.requireBloodVolume(f.player).getBloodVolume()==blood,"Refused entry spent blood");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void blockedExitAllowsBacktrackingAndStaleInputsCannotMoveThePlayer(GameTestHelper h) {
        try (Fixture f=new Fixture(h)) {
            for(BlockPos p:BlockPos.betweenClosed(f.exit.offset(-2,-1,-2),f.exit.offset(2,4,2)))
                if(!p.equals(f.exit)&&!(p.getY()==f.exit.getY()&&p.getZ()==f.exit.getZ()&&p.getX()<f.exit.getX()))
                    h.getLevel().setBlock(p,Blocks.STONE.defaultBlockState(),3);
            f.enter(); Vec3 start=f.player.position();
            AxonalTransductionManager.input(f.player,f.token+1,1,false); AxonalTransductionManager.tick(f.player);
            h.assertTrue(f.player.position().distanceToSqr(start)<.001,"Input for another session moved this traveler");
            for(int i=0;i<3;i++) f.forward();
            h.assertTrue(AxonalTransductionManager.isTraveling(f.player),"Blocked exit reformed inside stone");
            h.assertTrue(f.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(f.exit))<.01,"Blocked exit skipped or lost its anchor");
            AxonalTransductionManager.input(f.player,f.token,-1,false); AxonalTransductionManager.tick(f.player);
            h.assertTrue(f.player.getX()<f.exit.getX(),"Blocked endpoint could not be reversed");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void logoutRecoveryAndPassiveRemovalRestorePhysicalState(GameTestHelper h) {
        for (int reason=0;reason<3;reason++) try(Fixture f=new Fixture(h)) {
            f.enter(); f.forward();
            h.assertTrue(Paralysis.blocksActions(f.player),"Signal could still perform ordinary actions");
            if(reason==0) { HemoCapabilityAccess.requireKnownManipulations(f.player).togglePassive("axonal_transduction"); AxonalTransductionManager.tick(f.player); }
            if(reason==1) AxonalTransductionManager.onLogout(new PlayerEvent.PlayerLoggedOutEvent(f.player));
            if(reason==2) AxonalTransductionManager.onLogin(new PlayerEvent.PlayerLoggedInEvent(f.player));
            h.assertTrue(!AxonalTransductionManager.isTraveling(f.player)&&!f.player.noPhysics&&!f.player.isNoGravity(),"Recovery leaked travel state: "+reason);
            h.assertTrue(f.player.position().distanceToSqr(Vec3.atBottomCenterOf(f.source.above()))<.01,"Recovery did not return to a safe entry landing");
            h.assertTrue(!f.player.getPersistentData().contains("hemomancy_axonal_return"),"Recovery marker survived cleanup");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void recoveryCannotChargeAnotherEntryWhileStillTouchingTheSource(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            f.enter(); f.forward();
            double paid=HemoCapabilityAccess.requireBloodVolume(f.player).getBloodVolume();
            AxonalTransductionManager.end(f.player,true);
            AxonalTransductionManager.tick(f.player);
            h.assertTrue(!AxonalTransductionManager.isTraveling(f.player),"Recovery immediately recaptured its traveler");
            h.assertTrue(HemoCapabilityAccess.requireBloodVolume(f.player).getBloodVolume()==paid,"Recovery charged a second entry");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void entryAcknowledgementDoesNotRestoreTheOldPhysicalPosition(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            f.enter();
            h.assertTrue(f.teleportId>=0,"Entry did not establish a vanilla teleport acknowledgement at its signal position");
            f.player.connection.handleAcceptTeleportPacket(new net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket(f.teleportId));
            AxonalTransductionManager.tick(f.player);
            h.assertTrue(AxonalTransductionManager.isTraveling(f.player),"Entry acknowledgement restored the old position and ended travel");
            h.assertTrue(f.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(f.source))<.001,"Entry acknowledgement moved the signal out of its source");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void enteringTheSignalClearsAnExistingPhysicalTarget(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            var zombie=net.minecraft.world.entity.EntityType.ZOMBIE.create(h.getLevel());
            zombie.setPos(f.player.position().add(3,0,0));h.getLevel().addFreshEntity(zombie);
            try {
                zombie.setTarget(f.player);h.assertTrue(zombie.getTarget()==f.player,"Target fixture never acquired its player");
                f.enter();h.assertTrue(zombie.getTarget()==null,"Existing physical targeting survived signal entry");
                zombie.setTarget(f.player);h.assertTrue(zombie.getTarget()==null,"Signal became a new physical target");
            } finally {zombie.discard();}
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void missingRecoveryDimensionUsesASafePhysicalFallback(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            f.enter();f.forward();
            f.player.getPersistentData().getCompound("hemomancy_axonal_return").putString("dimension","hemomancy:removed_dimension");
            AxonalTransductionManager.end(f.player,true);
            h.assertTrue(!AxonalTransductionManager.isTraveling(f.player),"Fallback did not clear signal state");
            h.assertTrue(f.player.serverLevel().noCollision(f.player,f.player.getBoundingBox()),"Missing recovery dimension reformed inside a fiber");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void signalHasNoPhysicalTargetOrProjectileBody(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            h.assertTrue(f.player.isPickable()&&f.player.canBeHitByProjectile(),"Physical target fixture was not hittable");
            f.enter();
            h.assertTrue(!f.player.isPickable()&&!f.player.canBeHitByProjectile()&&!f.player.isPushable()&&!f.player.canBeSeenAsEnemy(),
                    "Signal retained a physical targeting or projectile body");
            float health=f.player.getHealth();
            f.player.hurt(h.getLevel().damageSources().generic(),10);
            h.assertTrue(f.player.getHealth()==health,"Ordinary damage harmed the signal");
            AxonalTransductionManager.end(f.player,true);
            h.assertTrue(f.player.isPickable()&&f.player.canBeHitByProjectile(),"Physical targeting did not return after reformation");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void signalRefusesHeldAndMenuDropsBeforeInventoryExtraction(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            f.player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.world.item.Items.DIAMOND,3));
            f.enter();
            for(var action:java.util.List.of(net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.DROP_ITEM,
                    net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS)) {
                f.player.connection.handlePlayerAction(new net.minecraft.network.protocol.game.ServerboundPlayerActionPacket(
                        action,BlockPos.ZERO,net.minecraft.core.Direction.DOWN));
                h.assertTrue(f.player.getMainHandItem().getCount()==3,"Signal drop extracted carried items: "+action);
            }
            f.player.connection.handleContainerClick(new net.minecraft.network.protocol.game.ServerboundContainerClickPacket(
                    f.player.inventoryMenu.containerId,f.player.inventoryMenu.getStateId(),36,1,
                    net.minecraft.world.inventory.ClickType.THROW,ItemStack.EMPTY,new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>()));
            h.assertTrue(f.player.getMainHandItem().getCount()==3,"Menu throw extracted signal inventory");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void signalCannotCollectPhysicalItemsThroughTheFiber(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            f.enter();
            var item=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),f.player.getX(),f.player.getY(),f.player.getZ(),
                    new ItemStack(net.minecraft.world.item.Items.DIAMOND,9));
            item.setNoPickUpDelay();h.getLevel().addFreshEntity(item);
            try {
                f.player.aiStep();
                h.assertTrue(!item.isRemoved()&&item.getItem().getCount()==9,"Signal picked up a physical item through the fiber");
                AxonalTransductionManager.end(f.player,true);
                item.setPos(f.player.position());f.player.aiStep();
                h.assertTrue(item.isRemoved(),"Reformed player could not collect ordinary items");
            } finally {item.discard();}
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void pistonMovementCannotDisplaceTheSignal(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            f.enter();f.forward();Vec3 position=f.player.position();
            f.player.move(net.minecraft.world.entity.MoverType.PISTON,new Vec3(.51,0,0));
            h.assertTrue(f.player.position().distanceToSqr(position)<.0001,"Piston movement displaced the signal");
            AxonalTransductionManager.input(f.player,f.token,0,false);AxonalTransductionManager.tick(f.player);
            h.assertTrue(AxonalTransductionManager.isTraveling(f.player),"Piston movement restored a body inside the fiber");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void recoveredLogoutAndColdLoginCannotRecaptureAtTheSource(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            f.enter();f.forward();double paid=HemoCapabilityAccess.requireBloodVolume(f.player).getBloodVolume();
            AxonalTransductionManager.onLogout(new PlayerEvent.PlayerLoggedOutEvent(f.player));
            AxonalTransductionManager.onLogin(new PlayerEvent.PlayerLoggedInEvent(f.player));
            AxonalTransductionManager.tick(f.player);
            h.assertTrue(!AxonalTransductionManager.isTraveling(f.player),"Reconnected player was recaptured before leaving contact");
            h.assertTrue(HemoCapabilityAccess.requireBloodVolume(f.player).getBloodVolume()==paid,"Reconnect charged a second entry");
            var saved=f.player.getPersistentData().copy();
            AxonalTransductionManager.onLogout(new PlayerEvent.PlayerLoggedOutEvent(f.player));
            f.player.getPersistentData().merge(saved);
            AxonalTransductionManager.onLogin(new PlayerEvent.PlayerLoggedInEvent(f.player));
            AxonalTransductionManager.tick(f.player);
            h.assertTrue(!AxonalTransductionManager.isTraveling(f.player),"Saved exit contact did not survive a cold login");
            f.player.setPos(Vec3.atBottomCenterOf(f.source.above()).add(0,0,3));AxonalTransductionManager.tick(f.player);
            f.player.setPos(Vec3.atBottomCenterOf(f.source.above()));f.enter();
            h.assertTrue(AxonalTransductionManager.isTraveling(f.player),"Leaving contact never permitted a deliberate second entry");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void vanillaConnectionTickPreservesEntryAndSignalMovement(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            f.player.connection.tick();
            h.assertTrue(AxonalTransductionManager.isTraveling(f.player),"Connection tick did not enter the signal");
            h.assertTrue(f.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(f.source))<.01,"Vanilla connection restored the pre-entry position");
            f.player.connection.handleAcceptTeleportPacket(new net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket(f.teleportId));
            AxonalTransductionManager.input(f.player,f.token,1,false);
            f.player.connection.tick();
            h.assertTrue(f.player.getX()>f.source.getX()+1.5,"Vanilla connection reset the server-owned signal movement");
            h.assertTrue(AxonalTransductionManager.isTraveling(f.player),"Full player tick ended signal travel");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void cameraRotationPacketsReverseHeldForwardTravel(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            f.enter();
            f.player.connection.handleAcceptTeleportPacket(new net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket(f.teleportId));
            f.forward();
            f.player.connection.handleMovePlayer(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot(90,0,true));
            f.forward();
            h.assertTrue(!AxonalTransductionManager.isTraveling(f.player),"Held forward did not return to the source after looking back");
            h.assertTrue(f.player.position().distanceToSqr(Vec3.atBottomCenterOf(f.source.above()))<.01,"Camera reversal did not reform at the source");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void backwardInputAfterCameraRotationTravelsAwayFromTheView(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            f.enter();
            f.player.connection.handleAcceptTeleportPacket(new net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket(f.teleportId));
            f.forward();
            f.player.connection.handleMovePlayer(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot(90,0,true));
            AxonalTransductionManager.input(f.player,f.token,-1,false);AxonalTransductionManager.tick(f.player);
            h.assertTrue(AxonalTransductionManager.isTraveling(f.player),"Backward input moved toward the view and reformed at the source");
            h.assertTrue(Math.abs(f.player.getX()-(f.source.getX()+3.7))<.001,"Backward input did not continue away from the west-facing camera");
        } h.succeed();
    }
    @GameTest(templateNamespace="naeglerophaeon_validation",template=ARENA,batch="axonal")
    public static void retiredSynapticStepCannotTeleport(GameTestHelper h) {
        try(Fixture f=new Fixture(h)) {
            Vec3 before=f.player.position(); ItemStack tool=new ItemStack(ItemInit.synaptic_step.get());
            f.player.setItemInHand(InteractionHand.MAIN_HAND,tool); tool.getItem().use(h.getLevel(),f.player,InteractionHand.MAIN_HAND);
            h.assertTrue(f.player.position().equals(before),"The retired tool still teleported its user");
        } h.succeed();
    }
}
