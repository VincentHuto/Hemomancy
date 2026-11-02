package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VampireBatEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.UUID;

/**
 * With hemomancy.vampireBatReview, frames one Vampire Bat cruising, hunting a wounded pig (its real pursuit) and roosting
 * under a ceiling, each from several sides, into build/vampire-bat-review/screenshots. Run with vampireBatReviewClient;
 * drop {"op":"create","name":"BatReview","seed":12345} after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class VampireBatClientReview {
    private static final String[] SCENES = {"cruise", "hunt", "roost"};
    /** Camera offset from the bat: x, y, z. */
    private static final double[][] VIEWS = {{-1.4, 0.5, -2.2}, {2.6, 0.3, 0.4}, {0.3, 2.6, 0.6}, {1.2, -0.9, 2.2}};
    private static final int SETTLE = 40, PER_VIEW = 25;
    private static int ticks;
    private static BlockPos arena;
    private static volatile VampireBatEntity actor;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.vampireBatReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("vampire-bat-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = true;
        var server = mc.getSingleplayerServer();
        UUID uuid = mc.player.getUUID();
        int span = SETTLE + VIEWS.length * PER_VIEW;
        int scene = ticks / span, local = ticks++ % span;
        if (scene >= SCENES.length) {
            mc.stop();
            return;
        }
        if (local == 0) server.execute(() -> setup(server, uuid, SCENES[scene]));
        if (local < SETTLE) return;
        int view = (local - SETTLE) / PER_VIEW, shot = (local - SETTLE) % PER_VIEW;
        if (actor != null && shot % 3 == 0) server.execute(() -> camera(server, uuid, VIEWS[view]));
        if (shot == PER_VIEW - 2) Screenshot.grab(mc.gameDirectory, "bat-" + SCENES[scene] + "-" + view + ".png",
                mc.getMainRenderTarget(), message -> {});
    }

    private static void setup(MinecraftServer server, UUID uuid, String scene) {
        var player = server.getPlayerList().getPlayer(uuid);
        var level = player.serverLevel();
        if (arena == null) {
            arena = player.blockPosition().above(30);
            var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
            for (String cmd : new String[]{"time set 6000", "gamerule doDaylightCycle false", "weather clear",
                    "gamerule doMobSpawning false", "effect give @s minecraft:night_vision infinite 0 true"})
                server.getCommands().performPrefixedCommand(source, cmd);
        }
        player.setGameMode(GameType.SPECTATOR);
        for (BlockPos pos : BlockPos.betweenClosed(arena.offset(-9, -5, -9), arena.offset(9, 6, 9)))
            level.setBlock(pos, pos.getY() == arena.getY() - 5 ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), 2);
        if (actor != null) actor.discard();
        level.getEntitiesOfClass(Pig.class, new AABB(arena).inflate(30)).forEach(Pig::discard);
        VampireBatEntity bat = EntityInit.vampire_bat.get().create(level);
        bat.setPersistenceRequired();
        bat.setNoAi(true);
        bat.setResting(false);
        bat.setPos(Vec3.atBottomCenterOf(arena));
        if (scene.equals("roost")) {
            level.setBlock(arena.above(), Blocks.BLACKSTONE.defaultBlockState(), 2);
            bat.setResting(true);
        } else if (scene.equals("hunt")) {
            Pig prey = EntityType.PIG.create(level);
            prey.setNoAi(true);
            prey.setPersistenceRequired();
            prey.setInvulnerable(true);
            prey.setHealth(4.0F);
            prey.setPos(Vec3.atBottomCenterOf(arena.east(3).below(4)));
            level.addFreshEntity(prey);
        }
        level.addFreshEntity(bat);
        actor = bat;
    }

    private static void camera(MinecraftServer server, UUID uuid, double[] at) {
        var player = server.getPlayerList().getPlayer(uuid);
        Vec3 focus = actor.position().add(0, 0.4, 0);
        Vec3 eye = focus.add(at[0], at[1], at[2]);
        float yaw = (float) Math.toDegrees(Math.atan2(-(focus.x - eye.x), focus.z - eye.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(focus.y - eye.y, Math.hypot(focus.x - eye.x, focus.z - eye.z)));
        player.teleportTo(player.serverLevel(), eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
    }
}
