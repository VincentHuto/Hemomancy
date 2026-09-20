package com.vincenthuto.hemomancy.gametest;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.succession.*;
import com.vincenthuto.hemomancy.common.rite.*;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.*;
import java.util.*;

/** Disposable live review fixture; never loaded by normal clients. All assists are explicit. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class SuccessionClientReview {
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.successionReview"))return;
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.getSingleplayerServer()==null)return;
        var root=mc.gameDirectory.toPath().toAbsolutePath().normalize();
        if(!root.endsWith("succession-client"))return;
        var request=root.resolve("succession-review.json");if(!Files.exists(request))return;
        try {
            String op=JsonParser.parseString(Files.readString(request)).getAsJsonObject().get("op").getAsString();Files.delete(request);
            if(op.equals("publish")) {
                mc.getSingleplayerServer().setUsesAuthentication(false);
                if(!mc.getSingleplayerServer().publishServer(GameType.CREATIVE,false,25568))throw new IllegalStateException("Could not publish review world");
            } else if(op.equals("labels")) {
                try {
                    var field=com.vincenthuto.hemomancy.client.render.entity.npc.SuccessionBrazierLabels.class.getDeclaredField("nearby");field.setAccessible(true);
                    Hemomancy.LOGGER.info("SUCCESSION_REVIEW label cache={}",field.get(null));
                } catch(ReflectiveOperationException failure) {throw new IllegalStateException(failure);}
                var block=mc.level.getBlockEntity(new BlockPos(5,101,0));
                Hemomancy.LOGGER.info("SUCCESSION_REVIEW label target={} renderer={} focus={} body={}",block,
                        block==null?null:mc.getBlockEntityRenderDispatcher().getRenderer(block),
                        mc.level.getBlockState(new BlockPos(0,100,0)),mc.level.getBlockState(new BlockPos(2,102,0)));
                for(var direction:Direction.Plane.HORIZONTAL) {
                    var focus=new BlockPos(5,101,0).relative(direction,-5).below();
                    Hemomancy.LOGGER.info("SUCCESSION_REVIEW label direction={} focus={} block={} body={}",direction,focus,mc.level.getBlockState(focus),mc.level.getBlockState(focus.relative(direction,2).above(2)));
                }
            } else if(op.equals("residents")) {
                com.vincenthuto.hemomancy.client.screen.item.SuccessionResidentsScreen.open(null);
            } else mc.getSingleplayerServer().execute(()-> {
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
                if(op.equals("setup"))setup(player);
                if(op.equals("start")) {
                    player.setGameMode(GameType.SURVIVAL);
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(ItemInit.living_staff.get()));
                    var result=com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket.tryStartCardinalRite(player,new BlockPos(0,100,0),CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
                    Hemomancy.LOGGER.info("SUCCESSION_REVIEW activation {}",result);
                }
                if(op.equals("hostiles")) for(int i=0;i<SuccessionProfessions.ROLES.size();i++) {
                    var npc=(ProfessionalHarbingerEntity)BuiltInRegistries.ENTITY_TYPE.get(SuccessionProfessions.entityType(SuccessionProfessions.ROLES.get(i))).create(player.serverLevel());
                    npc.initializeMisbegotten(player.getUUID(),i,"Broken Teacher");npc.setNoAi(true);npc.setPos(-8+i*4,101,10);player.serverLevel().addFreshEntity(npc);
                }
                if(op.equals("status")) {
                    var data=SuccessionSavedData.get(player.serverLevel());var rite=CardinalRiteSavedData.get(player.serverLevel()).getRite(player.getUUID());
                    Hemomancy.LOGGER.info("SUCCESSION_REVIEW residents={} locks={} rite={}",data.residents.size(),data.ledger.reservations().size(),rite==null?"none":rite.succession());
                }
            });
        }catch(Exception failure){Hemomancy.LOGGER.error("SUCCESSION_REVIEW",failure);}
    }
    private static void setup(ServerPlayer p) {
        var level=p.serverLevel();var center=new BlockPos(0,100,0);
        for(var pos:BlockPos.betweenClosed(-18,99,-18,18,100,18))level.setBlock(pos,Blocks.STONE.defaultBlockState(),3);
        for(var pos:BlockPos.betweenClosed(-18,101,-18,18,108,18))level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        p.teleportTo(level,.5,101,-4.5,0,10);p.setGameMode(GameType.CREATIVE);
        level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);
        var line=new Bloodline("Succession Review",p.getUUID(),UUID.randomUUID(),new ArrayList<>(List.of(p.getUUID())));
        BloodlineSavedData.get(p.server.overworld()).registerBloodline(line);
        HemoCapabilityAccess.requireBloodVolume(p).setBloodLine(line);HemoCapabilityAccess.requireBloodVolume(p).setActive(true);HemoCapabilityAccess.requireBloodVolume(p).setBloodVolume(5000);
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        var heart=center.offset(0,1,12);level.setBlock(heart,BlockInit.consecrated_bloodwell.get().defaultBlockState(),3);FoundingFaneSavedData.get(level).consecrateHeart(p.getUUID(),heart);
        var floor=CardinalRiteFloorRegistry.get(Hemomancy.rloc("dominion_greater")).orElseThrow();var pattern=floor.pattern().getPatternArray();
        for(int z=0;z<7;z++)for(int x=0;x<7;x++)level.setBlock(center.offset(x-3,0,3-z),floor.pattern().getSymbolList().get(String.valueOf(pattern[z][0].charAt(x))).defaultBlockState(),3);
        level.setBlock(center.offset(2,1,0),BlockInit.venous_stone.get().defaultBlockState(),3);level.setBlock(center.offset(2,2,0),BlockInit.vacant_effigy.get().defaultBlockState(),3);
        ((CardinalFocusBlockEntity)level.getBlockEntity(center)).insertMedium(null,new ItemStack(ItemInit.sanguine_quintessence.get()));
        var donor=EntityInit.harbinger_alchemist.get().create(level);var data=SuccessionSavedData.get(level);data.bequeath(donor.getUUID(),line.getBloodlineUUID(),"alchemist","Review Teacher");
        var blood=new ItemStack(ItemInit.bloody_vial.get());SuccessionSamples.fill(blood,donor,line.getBloodlineUUID(),"alchemist");
        var own=new ItemStack(ItemInit.bloody_vial.get());SuccessionSamples.fill(own,p,null,"");var offerings=List.of(blood,own,new ItemStack(ItemInit.mnemonic_ambergris.get()));
        for(int i=0;i<3;i++) {var s=floor.brazierSockets().get(i);var pos=center.offset(s.getX(),s.getY(),-s.getZ());level.setBlock(pos,BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.RITUAL_PHASE,1),3);((IronBrazierBlockEntity)level.getBlockEntity(pos)).insertOffering(null,offerings.get(i));}
        var workplaces=List.of(BlockInit.ghastly_alembic.get(),BlockInit.somatic_loom.get(),BlockInit.hematic_armature.get(),BlockInit.mason_effigy.get(),BlockInit.cardinal_focus.get());
        for(int i=0;i<5;i++) {
            var pos=center.offset(-8+i*4,1,7);level.setBlock(pos,workplaces.get(i).defaultBlockState(),3);
            var npc=(ProfessionalHarbingerEntity)BuiltInRegistries.ENTITY_TYPE.get(SuccessionProfessions.entityType(SuccessionProfessions.ROLES.get(i))).create(level);
            SuccessionTestFixtures.resident(p,npc,pos);npc.setPos(pos.getX()+.5,101,pos.getZ()-1);npc.setNoAi(true);level.addFreshEntity(npc);
        }
        level.setBlock(center.offset(8,1,0),BlockInit.vial_centrifuge.get().defaultBlockState(),3);
        Hemomancy.LOGGER.info("SUCCESSION_REVIEW fixture prepared, five assisted residents and one unperformed rite");
    }
}
