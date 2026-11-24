package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * With hemomancy.bestiaryReview=true, stands every Hemomancy entity that is a Mob in turn, AI off, and takes one
 * three-quarter still of each into the review directory's screenshots as &lt;id&gt;.png. Run with bestiaryReviewClient;
 * drop {"op":"create","name":"BestiaryReview","seed":"1"} after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class BestiaryModelReview {
    private static final int PER_MOB = 40, CAMERA_AT = 12, SHOT_AT = 34;
    private static int ticks;
    private static BlockPos arena;
    private static List<EntityType<?>> roster;
    private static volatile Mob actor;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.bestiaryReview")) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = true;
        var server = mc.getSingleplayerServer();
        UUID uuid = mc.player.getUUID();
        if (roster == null) roster = BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals(Hemomancy.MOD_ID)).toList();
        int index = ticks / PER_MOB, local = ticks++ % PER_MOB;
        if (index >= roster.size()) {
            Hemomancy.LOGGER.info("BESTIARY_REVIEW captured {}", roster.size());
            mc.stop();
            return;
        }
        EntityType<?> type = roster.get(index);
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
        if (local == 0) server.execute(() -> setup(server, uuid, type));
        if (actor == null) return;
        if (local >= CAMERA_AT && local % 4 == 0) server.execute(() -> camera(server, uuid));
        if (local == SHOT_AT) Screenshot.grab(mc.gameDirectory, id + ".png", mc.getMainRenderTarget(), message -> {});
    }

    private static void setup(MinecraftServer server, UUID uuid, EntityType<?> type) {
        var player = server.getPlayerList().getPlayer(uuid);
        var level = player.serverLevel();
        var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        if (arena == null) {
            arena = player.blockPosition().above(40);
            for (String cmd : new String[]{"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false",
                    "time set 6000"})
                server.getCommands().performPrefixedCommand(source, cmd);
        }
        player.setGameMode(GameType.SPECTATOR);
        if (actor != null) actor.discard();
        actor = null;
        Mob mob;
        try {
            if (!(type.create(level) instanceof Mob created)) return;
            mob = created;
        } catch (RuntimeException e) {
            Hemomancy.LOGGER.warn("BESTIARY_REVIEW could not create {}", type, e);
            return;
        }
        boolean water = mob instanceof WaterAnimal;
        int depth = water ? Math.max(4, (int) Math.ceil(mob.getBbHeight()) + 3) : 0;
        for (BlockPos pos : BlockPos.betweenClosed(arena.offset(-14, -1, -14), arena.offset(14, 14, 14))) {
            int height = pos.getY() - arena.getY();
            level.setBlock(pos, height < 0 ? (water ? Blocks.SAND : Blocks.GRASS_BLOCK).defaultBlockState()
                    : height < depth ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
        }
        mob.setPersistenceRequired();
        mob.setInvulnerable(true);
        mob.setNoAi(true);
        mob.setNoGravity(water);
        mob.setPos(Vec3.atBottomCenterOf(arena.above(water ? 1 : 0)));
        mob.setYRot(0);
        mob.setYHeadRot(0);
        mob.setYBodyRot(0);
        level.addFreshEntity(mob);
        actor = mob;
    }

    private static void camera(MinecraftServer server, UUID uuid) {
        var player = server.getPlayerList().getPlayer(uuid);
        Mob mob = actor;
        if (mob == null) return;
        double size = Math.max(mob.getBbWidth(), mob.getBbHeight());
        Vec3 focus = mob.position().add(0, mob.getBbHeight() * 0.5, 0);
        double distance = size * 1.7 + 1.4;
        // Front three-quarter: the mob faces +z at yaw 0.
        Vec3 eye = focus.add(distance * 0.6, distance * 0.35, distance * 0.75);
        float yaw = (float) Math.toDegrees(Math.atan2(-(focus.x - eye.x), focus.z - eye.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(focus.y - eye.y, Math.hypot(focus.x - eye.x, focus.z - eye.z)));
        player.teleportTo(player.serverLevel(), eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
    }
}
