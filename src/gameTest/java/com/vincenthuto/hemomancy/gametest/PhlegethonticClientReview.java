package com.vincenthuto.hemomancy.gametest;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.ExcoriatedEntity;
import com.vincenthuto.hemomancy.common.init.*;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.*;

/** Isolated client-review driver. This entire source set is excluded from release jars. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class PhlegethonticClientReview {
    private static int tick,lastSequence=-1,captureTether=-1;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.phlegethontic.clientReview") || ++tick%10!=0)return;
        var client=Minecraft.getInstance();
        client.options.pauseOnLostFocus=false;
        if(captureTether>=0 && client.level!=null)for(var entity:client.level.entitiesForRendering()) {
            if(entity instanceof com.vincenthuto.hemomancy.common.entity.projectile.RecallBarbEntity barb && barb.victimId()>=0) {
                Screenshot.grab(client.gameDirectory,"phlegethontic-tether-"+captureTether+".png",client.getMainRenderTarget(),
                        message -> LogUtils.getLogger().info("PHLEGETHONTIC_SCREENSHOT {}",message.getString()));
                captureTether=-1;break;
            }
        }
        Path instructions=client.gameDirectory.toPath().resolve("review.json");
        if(!Files.isRegularFile(instructions))return;
        try {
            var request=JsonParser.parseString(Files.readString(instructions)).getAsJsonObject();
            int sequence=request.get("sequence").getAsInt();if(sequence==lastSequence)return;
            lastSequence=sequence;String operation=request.get("operation").getAsString();
            if(operation.equals("screenshot")) {
                client.options.hideGui=true;
                Screenshot.grab(client.gameDirectory,"phlegethontic-"+sequence+".png",client.getMainRenderTarget(),
                        message -> LogUtils.getLogger().info("PHLEGETHONTIC_SCREENSHOT {}",message.getString()));
            } else if(operation.equals("stop"))client.stop();
            else if(operation.equals("captureTether"))captureTether=sequence;
            else if(operation.equals("sneak"))client.options.keyShift.setDown(request.get("held").getAsBoolean());
            else if(operation.equals("publish") && client.getSingleplayerServer()!=null) {
                if(!client.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("phlegethontic-client"))
                    throw new IllegalStateException("Offline observer review requires the disposable Phlegethontic client directory");
                client.getSingleplayerServer().setUsesAuthentication(false);
                if(!client.getSingleplayerServer().publishServer(GameType.CREATIVE,false,25566))
                    throw new IllegalStateException("Cannot publish disposable Phlegethontic review");
            }
            else if(operation.equals("status") && client.getSingleplayerServer()!=null) {
                var server=client.getSingleplayerServer();
                server.execute(() -> {
                    JsonObject status=new JsonObject();JsonArray players=new JsonArray();
                    for(var player:server.getPlayerList().getPlayers()) {
                        JsonObject entry=new JsonObject();entry.addProperty("name",player.getGameProfile().getName());
                        entry.addProperty("health",player.getHealth());entry.addProperty("position",player.position().toString());
                        entry.addProperty("sneaking",player.isShiftKeyDown());
                        var volume=com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getBloodVolume(player).orElse(null);
                        entry.addProperty("bloodActive",volume!=null && volume.isActive());
                        entry.addProperty("tethers",player.serverLevel().getEntitiesOfClass(
                                com.vincenthuto.hemomancy.common.entity.projectile.RecallBarbEntity.class,player.getBoundingBox().inflate(40),
                                barb -> barb.victimId()==player.getId()).size());players.add(entry);
                    }
                    status.add("players",players);status.addProperty("sequence",sequence);
                    try {Files.writeString(client.gameDirectory.toPath().resolve("review-status.json"),new GsonBuilder().setPrettyPrinting().create().toJson(status));}
                    catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
                });
            }
            else if(operation.equals("escharian") && client.getSingleplayerServer()!=null && client.player!=null) {
                var server=client.getSingleplayerServer();var uuid=client.player.getUUID();
                server.execute(() -> {
                    var player=server.getPlayerList().getPlayer(uuid);
                    if(player!=null)EscharianOvergrowthClientReview.place(player);
                });
            }
            else if(operation.equals("command") && client.getSingleplayerServer()!=null && client.player!=null) {
                java.util.List<String> commands=request.has("commands")
                        ?request.getAsJsonArray("commands").asList().stream().map(JsonElement::getAsString).toList()
                        :java.util.List.of(request.get("command").getAsString());
                var server=client.getSingleplayerServer();var uuid=client.player.getUUID();
                server.execute(() -> {
                    var player=server.getPlayerList().getPlayer(uuid);
                    if(player!=null)for(String command:commands)
                        server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4),command);
                });
            }
            LogUtils.getLogger().info("PHLEGETHONTIC_REVIEW {} {}",sequence,operation);
        } catch(Exception e){LogUtils.getLogger().error("Phlegethontic client review failed",e);}
    }
}
