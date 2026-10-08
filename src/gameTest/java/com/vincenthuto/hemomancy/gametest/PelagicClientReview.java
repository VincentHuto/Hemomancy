package com.vincenthuto.hemomancy.gametest;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.*;

/** Visits recorded natural sites in a disposable copied world and captures the actual game renderer. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class PelagicClientReview {
    private static JsonArray sites;
    private static int tick, index;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.pelagic.clientReview")) return;
        var client = Minecraft.getInstance();
        client.options.pauseOnLostFocus = false;
        if (client.player == null || client.getSingleplayerServer() == null) return;
        try {
            if (sites == null) {
                sites = JsonParser.parseString(Files.readString(client.gameDirectory.toPath().resolve("pelagic-report.json")))
                        .getAsJsonObject().getAsJsonArray("sites");
                client.options.renderDistance().set(10);
                client.options.gamma().set(1.0);
                client.options.hideGui = true;
            }
            if (index >= sites.size()) { client.stop(); return; }
            var site = sites.get(index).getAsJsonObject();
            if (tick++ == 0) {
                var server = client.getSingleplayerServer(); var uuid = client.player.getUUID();
                int x = site.get("x").getAsInt(), z = site.get("z").getAsInt();
                double y = site.get("predictedFloor").getAsDouble() + 8;
                String layer = site.get("layer").getAsString();
                y = switch (layer) {
                    case "rockpool_shore" -> 80;
                    case "pelagic_ocean" -> 50;
                    case "twilight_ocean" -> Math.min(32, y);
                    case "midnight_ocean" -> Math.min(11, y);
                    case "carrion_depths" -> Math.min(-10, y);
                    default -> y;
                };
                final double cameraY = y;
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayer(uuid);
                    var source = player.createCommandSourceStack().withPermission(4);
                    var terrain = com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicWorldgen.context(player.serverLevel().getChunkSource().randomState());
                    double cameraX = x + .5, cameraZ = z + .5, height = cameraY;
                    float yaw = 135, pitch = 30;
                    if (site.has("shoreEdge")) {
                        int lowest = Integer.MAX_VALUE;
                        for (int angle = 0; angle < 360; angle += 45) {
                            int dx = (int)Math.round(Math.cos(Math.toRadians(angle)) * 32);
                            int dz = (int)Math.round(Math.sin(Math.toRadians(angle)) * 32);
                            var chunkSource = player.serverLevel().getChunkSource();
                            int floor = chunkSource.getGenerator().getBaseHeight(x + dx, z + dz,
                                    net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,
                                    player.serverLevel(), chunkSource.randomState());
                            if (floor < lowest) {
                                lowest = floor; cameraX = x + dx + .5; cameraZ = z + dz + .5;
                                height = Math.min(62, floor + 10);
                                yaw = (float)Math.toDegrees(Math.atan2(dx, -dz));
                                pitch = (float)Math.toDegrees(Math.atan2(height - site.get("predictedFloor").getAsDouble() + 2, 32));
                            }
                        }
                    } else if (site.has("algaeReview")) {
                        net.minecraft.core.BlockPos patch = null;
                        for (var pos : net.minecraft.core.BlockPos.betweenClosed(
                                new net.minecraft.core.BlockPos(x - 24, 63, z - 24),
                                new net.minecraft.core.BlockPos(x + 24, 70, z + 24))) {
                            if (player.serverLevel().getBlockState(pos).is(
                                    com.vincenthuto.hemomancy.common.init.BlockInit.hematic_algal_crust.get())) {
                                patch = pos.immutable(); break;
                            }
                        }
                        if (patch == null) throw new IllegalStateException("No natural algal crust in review area");
                        cameraX = patch.getX() + .5; cameraZ = patch.getZ() - 2.5;
                        height = patch.getY() + 7; yaw = 0; pitch = 65;
                    } else if (site.has("camera")) {
                        var camera = site.getAsJsonObject("camera");
                        cameraX = camera.get("x").getAsDouble();
                        cameraZ = camera.get("z").getAsDouble();
                        height = camera.get("y").getAsDouble();
                        yaw = camera.get("yaw").getAsFloat();
                        pitch = camera.get("pitch").getAsFloat();
                    } else if (layer.equals("rockpool_shore") || site.has("landmark")) {
                        cameraX += 14; cameraZ += 14;
                        height = Math.max(height, terrain.sample(x + 14, z + 14).column().floor() + 8);
                        pitch = (float)Math.toDegrees(Math.atan2(height - site.get("predictedFloor").getAsDouble() - 3, 20));
                    } else {
                        // Face down the local slope so a nearby wall does not obscure the landform.
                        double lowest = Double.MAX_VALUE;
                        for (int angle = 0; angle < 360; angle += 45) {
                            int dx = (int)Math.round(Math.cos(Math.toRadians(angle)) * 24);
                            int dz = (int)Math.round(Math.sin(Math.toRadians(angle)) * 24);
                            double floor = terrain.sample(x + dx, z + dz).column().floor();
                            if (floor < lowest) { lowest = floor; yaw = (float)Math.toDegrees(Math.atan2(-dx, dz)); }
                        }
                    }
                    for (String command : new String[]{"gamemode spectator", "time set noon", "weather clear",
                            "gamerule doDaylightCycle false", "gamerule doMobSpawning false", "effect give @s minecraft:night_vision infinite 0 true",
                            "effect give @s minecraft:conduit_power infinite 0 true",
                            "tp @s " + cameraX + " " + height + " " + cameraZ + " " + yaw + " " + pitch})
                        server.getCommands().performPrefixedCommand(source, command);
                });
                LogUtils.getLogger().info("PELAGIC_REVIEW visit {} {} {} {}", index, layer, x, z);
            }
            if (tick == 180) {
                int captureIndex = site.has("captureIndex") ? site.get("captureIndex").getAsInt() : index;
                String name = "pelagic-reviewed-" + String.format("%02d", captureIndex) + "-" + site.get("layer").getAsString() + ".png";
                Screenshot.grab(client.gameDirectory, name, client.getMainRenderTarget(),
                        message -> LogUtils.getLogger().info("PELAGIC_SCREENSHOT {}", message.getString()));
            }
            if (tick > 200) { tick = 0; index++; }
        } catch (Exception e) {
            LogUtils.getLogger().error("Pelagic visual acceptance failed", e);
            client.stop();
        }
    }
}
