package com.vincenthuto.hemomancy.gametest;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.antecedent.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ClairaudiographBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.*;

/** Opt-in fixture for a disposable review world, excluded from release sources. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class AntecedentClientReview {
    private static final VigilModules.Layout LAYOUT=VigilModules.ALL.getFirst();
    private static final BlockPos ORIGIN=new BlockPos(50,84,0),MACHINE=ORIGIN.offset(LAYOUT.plan().vessel()).offset(0,0,-2);
    private static boolean captureSilence;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.antecedentReview"))return;
        var mc=Minecraft.getInstance();if(mc.player==null || mc.getSingleplayerServer()==null)return;
        var root=mc.gameDirectory.toPath().toAbsolutePath().normalize();if(!root.endsWith("antecedent-client"))return;
        if(captureSilence) {
            var view=com.vincenthuto.hemomancy.client.sound.ClairaudiographSounds.view(MACHINE,0);
            if(view.program()==AncientRecordings.SEVERED && view.seconds()>=65 && view.seconds()<68 && view.analysis()!=null) {
                captureSilence=false;float energy=0;
                for(int band=0;band<64;band++)energy=Math.max(energy,view.analysis().energy(view.analysis().frameAt(view.seconds(),view.pitch()),band));
                Hemomancy.LOGGER.info("ANTECEDENT_REVIEW silence seconds={} spectrumEnergy={} muted={}",view.seconds(),energy,mc.options.getSoundSourceVolume(net.minecraft.sounds.SoundSource.MASTER)==0);
                net.minecraft.client.Screenshot.grab(mc.gameDirectory,"clairaudiograph-silent-verified.png",mc.getMainRenderTarget(),message->{});
            }
        }
        var request=root.resolve("antecedent-review.json");if(!Files.exists(request))return;
        try {
            var data=JsonParser.parseString(Files.readString(request)).getAsJsonObject();Files.delete(request);String op=data.get("op").getAsString();
            if(op.equals("play"))captureSilence=true;
            if(op.equals("spectrum")) {
                var view=com.vincenthuto.hemomancy.client.sound.ClairaudiographSounds.view(MACHINE,0);float peak=0;
                if(view.analysis()!=null)for(int b=0;b<64;b++)peak=Math.max(peak,view.analysis().energy(view.analysis().frameAt(view.seconds(),view.pitch()),b));
                int strongest=0;float maximum=0;
                if(view.analysis()!=null)for(int frame=0;frame<view.analysis().frames();frame++)for(int band=0;band<64;band++)if(view.analysis().energy(frame,band)>maximum){maximum=view.analysis().energy(frame,band);strongest=band;}
                Files.writeString(root.resolve("spectrum.txt"),"status="+view.status()+" seconds="+view.seconds()+" peak="+peak+" pitch="+view.pitch()+" strongestHz="+com.vincenthuto.hemomancy.client.sound.SpectrogramAnalysis.frequency(strongest)+" program="+view.program());return;
            }
            if(op.equals("mute")){mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0.0);return;}
            if(op.equals("scale")){mc.options.guiScale().set(data.get("value").getAsInt());mc.resizeDisplay();return;}
            if(op.equals("reload")){mc.reloadResourcePacks();return;}
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(p==null)return;
                var level=p.serverLevel();
                if(op.equals("setup")) {
                    level.getStructureManager().getOrCreate(net.minecraft.resources.ResourceLocation.withDefaultNamespace("ancient_city/city_center/city_center_1")).placeInWorld(level,new BlockPos(0,90,0),new BlockPos(0,90,0),new StructurePlaceSettings(),level.random,2);
                    level.getStructureManager().getOrCreate(Hemomancy.rloc("antecedent/"+LAYOUT.composite())).placeInWorld(level,ORIGIN,ORIGIN,new StructurePlaceSettings(),level.random,2);
                    level.setBlock(MACHINE,BlockInit.clairaudiograph.get().defaultBlockState(),3);
                    var machine=(ClairaudiographBlockEntity)level.getBlockEntity(MACHINE);
                    var sample=AhaematicSample.create("block","minecraft:sculk_catalyst");BloodSampleData.identify(sample);
                    machine.inventory.setStackInSlot(0,sample);machine.inventory.setStackInSlot(1,AncientRecordings.cylinder(AncientRecordings.SEVERED));
                    p.setGameMode(GameType.CREATIVE);HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(4);teleport(p,LAYOUT.plan().vessel().offset(0,0,-1),180,15);
                }
                if(op.equals("play")) {var machine=(ClairaudiographBlockEntity)level.getBlockEntity(MACHINE);machine.stopPlayback();machine.startPlayback(false);p.openMenu(machine,MACHINE);}
                if(op.equals("gallery"))teleport(p,new BlockPos(LAYOUT.plan().gallery().minX()+2,8,LAYOUT.plan().gallery().minZ()+9),-90,30);
                if(op.equals("entrance"))teleport(p,LAYOUT.entry().above(),-90,0);
                if(op.equals("archive"))teleport(p,LAYOUT.plan().vessel().offset(0,0,-1),180,15);
                if(op.equals("microscope")) {
                    var sample=AhaematicSample.create("entity","minecraft:warden");
                    p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(ItemInit.hematic_microscope.get()));p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,sample);
                }
                if(op.equals("ordinary")) {
                    var machine=(ClairaudiographBlockEntity)level.getBlockEntity(MACHINE);machine.stopPlayback();
                    var cylinder=new ItemStack(ItemInit.ambergris_cylinder.get());
                    cylinder.set(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get(),new com.vincenthuto.hemomancy.common.item.component.ClairaudiographRecording("minecraft:pig","minecraft:entity.pig.ambient","ambient",1));
                    machine.inventory.setStackInSlot(1,cylinder);machine.loop=true;machine.startPlayback(false);p.openMenu(machine,MACHINE);
                }
                Hemomancy.LOGGER.info("ANTECEDENT_REVIEW {}",op);
            });
        }catch(Exception failure){Hemomancy.LOGGER.error("ANTECEDENT_REVIEW",failure);}
    }
    private static void teleport(ServerPlayer player,BlockPos local,float yaw,float pitch) {
        var at=ORIGIN.offset(local);player.teleportTo(player.serverLevel(),at.getX()+.5,at.getY(),at.getZ()+.5,yaw,pitch);
    }

}
