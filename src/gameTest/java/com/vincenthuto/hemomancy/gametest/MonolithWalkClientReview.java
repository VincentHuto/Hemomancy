package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.LidlessPassageClientState;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SanguineMonolithBlock;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineMonolithBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Opt-in native rendering check for the Monolith walk, restricted to the disposable monolith-walk-client world:
 * the whole stone, the cracks spreading, the fallen halves with fog, lightning and the portal from two angles,
 * the crossing with the black hole and the Seed ahead, and the exit with the Seed in hand.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MonolithWalkClientReview {
    private static final BlockPos STONE = new BlockPos(0, 102, 0);
    /** Ticks of settling before the schedule, so the world has loaded and the standing stone can be shot. */
    private static final int LEAD_IN = 40;
    private static int ticks;
    private static boolean walkingForward;
    private static int veilShots;
    private static int chamberTick = -1;
    private static int backTick = -1;
    private static Vec3 arrival = Vec3.ZERO;
    private static boolean setupQueued;
    private static volatile boolean prepared;

    @SubscribeEvent
    public static void input(ClientTickEvent.Pre event) {
        if (!Boolean.getBoolean("hemomancy.monolithWalkReview") || !prepared) return;
        Minecraft mc = Minecraft.getInstance();
        if (walkingForward) {
            mc.options.keyUp.setDown(true);
            return;
        }
        int walk = ticks - LEAD_IN;
        mc.options.keyUp.setDown((walk >= 330 && walk < 372) || (walk >= 380 && walk < 402) || (walk >= 416 && walk < 470));
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.monolithWalkReview")) return;
        Minecraft mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("monolith-walk-client")) return;
        if (mc.screen instanceof com.vincenthuto.hemomancy.client.screen.ChamberTransitionScreen && veilShots < 2) {
            veilShots++;
            capture(mc, "gate-veil-" + veilShots);
        }
        if (mc.screen instanceof AccessibilityOnboardingScreen) {
            mc.options.onboardAccessibility = false;
            mc.setScreen(new TitleScreen());
            return;
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof PauseScreen) mc.setScreen(null);
        if (!setupQueued) {
            setupQueued = true;
            var playerId = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> setup(mc.getSingleplayerServer().getPlayerList().getPlayer(playerId)));
            return;
        }
        if (!prepared) return;
        int tick = ticks++ - LEAD_IN;
        if (tick == -19) serverSide(mc, stone -> {
            // Something green in front of the stone, so the view out of it through the gate shows its tints.
            var level = stone.getLevel();
            for (BlockPos b : BlockPos.betweenClosed(new BlockPos(-4, 101, 2), new BlockPos(4, 101, 7)))
                level.setBlock(b, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            for (int x : new int[] { -3, 3 }) for (int y = 103; y <= 104; y++)
                level.setBlock(new BlockPos(x, y, 4), Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true), 3);
        });
        if (tick == -18) teleport(mc, 1.35, 102, 1.55, 140, 28);
        if (tick == -14) capture(mc, "monolith-tendrils-oblique");
        if (tick == -13) teleport(mc, 0.5, 102, -1.9, 0, 8);
        if (tick == -12) capture(mc, "monolith-back");
        if (tick == -11) teleport(mc, 0.5, 102, 2.3, 180, 22);
        if (tick == -10) capture(mc, "monolith-tendrils");
        if (tick == -9) teleport(mc, 0.5, 102, 4.5, 180, 0);
        if (tick == 1) serverSide(mc, stone -> { stone.startRise(stone.getLevel().getGameTime()); com.vincenthuto.hemomancy.common.util.BlockEntitySync.markAndSync(stone); });
        if (tick == 10) capture(mc, "monolith-rising");
        if (tick == 20) capture(mc, "monolith-rising-late");
        if (tick == 30) capture(mc, "monolith-whole");
        if (tick == 32) serverSide(mc, stone -> stone.beginBreak(player(mc)));
        if (tick == 140) capture(mc, "monolith-cracks");
        if (tick == 170) capture(mc, "monolith-cracks-late");
        if (tick == 188) capture(mc, "monolith-cracks-full");
        if (tick == 196) capture(mc, "monolith-falling");
        if (tick == 300) capture(mc, "monolith-fallen-front");
        if (tick == 302) teleport(mc, 2.6, 102, 2.6, 135, 26);
        if (tick == 314) capture(mc, "monolith-fallen-angle");
        if (tick == 316) teleport(mc, 0.5, 102, 2.0, 225, 6);
        if (tick == 324) capture(mc, "monolith-split-edge");
        if (tick == 325) teleport(mc, 0.5, 102, 4.5, 180, 0);
        if (tick == 345) capture(mc, "monolith-walk-start");
        if (tick == 374) capture(mc, "monolith-walk-third");
        if (tick == 404) capture(mc, "monolith-walk-two-thirds");
        if (tick == 412) capture(mc, "monolith-walk-near");
        if (tick == 416) mc.player.setYRot(215);
        if (tick == 424) capture(mc, "monolith-walk-turned");
        if (tick == 426) mc.player.setYRot(180);
        if (tick == 436) capture(mc, "monolith-walk-close-before");
        if (tick == 440) capture(mc, "monolith-walk-close");
        if (tick == 452) capture(mc, "monolith-walk-closest");
        if (tick == 474) capture(mc, "monolith-exit");
        if (tick == 476) mc.player.setYRot(0);
        if (tick == 486) capture(mc, "monolith-husk");
        gateway(mc, tick);
    }

    /**
     * The second half: the eye woken with blood, the walk in, the Chamber's corridor and gate, the walk back out. With
     * -PmonolithPath=silentium or apotheos the player takes that path first, for the themes and the overgrown husk.
     */
    private static void gateway(Minecraft mc, int tick) {
        boolean inChamber = mc.level.dimension().equals(com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.CHAMBER_OF_WILL);
        if (tick == 490) {
            teleport(mc, 0.5, 102, 4.5, 180, 0);
            String path = System.getProperty("hemomancy.monolithWalkPath", "none");
            mc.getSingleplayerServer().execute(() -> {
                var degree = HemoCapabilityAccess.getInitiatoryDegree(player(mc));
                if (!path.equals("none")) {
                    degree.setDegreeNumber(8);
                    degree.setArchonPath(path.equals("apotheos")
                            ? com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.APOTHEOS
                            : com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENTIUM);
                }
                HemoCapabilityAccess.getBloodVolume(player(mc)).setBloodVolume(1000);
            });
        }
        // Fed until it opens: client ticks can land on one server tick, which takes a feed only once.
        if (tick >= 500 && tick < 570) serverSide(mc, stone -> { if (!stone.gateOpen()) stone.takeProjectedBlood(player(mc), STONE, 10.0D, false); });
        if (tick == 520) capture(mc, "gate-charging");
        if (tick == 556) capture(mc, "gate-open");
        if (tick == 558) teleport(mc, 3.5, 102, 3.2, 135, 6);
        if (tick == 568) capture(mc, "gate-open-angle");
        if (tick == 570) teleport(mc, 0.5, 102, 1.6, 180, 0);
        if (tick == 574) walkingForward = true;
        if (tick == 590) capture(mc, "gate-walk-in");
        if (tick == 600) capture(mc, "gate-walk-in-late");
        if (inChamber && chamberTick < 0) {
            chamberTick = tick;
            walkingForward = false;
            mc.options.keyUp.setDown(false);
        }
        if (chamberTick >= 0) chamberScenes(mc, tick - chamberTick);
        if (tick > 2400) mc.stop();
    }

    private static void chamberScenes(Minecraft mc, int t) {
        if (t == 30) {
            capture(mc, "chamber-arrival");
            arrival = mc.player.position();
        }
        if (t == 32) teleport(mc, arrival.x, arrival.y, arrival.z, 0, 4);
        if (t == 42) capture(mc, "chamber-gate");
        if (t == 44) mc.getSingleplayerServer().execute(() -> {
            player(mc).setGameMode(GameType.CREATIVE);
            player(mc).getAbilities().flying = true;
            player(mc).onUpdateAbilities();
        });
        if (t == 48) teleport(mc, arrival.x + 9, arrival.y + 9, arrival.z - 7, 125, 34);
        if (t == 62) capture(mc, "chamber-overview");
        if (t == 64) teleport(mc, arrival.x, arrival.y + 14, arrival.z - 12, 180, 62);
        if (t == 76) capture(mc, "chamber-above");
        if (t == 78) teleport(mc, arrival.x, arrival.y + 1.2, arrival.z - 9, 0, 0);
        if (t == 88) capture(mc, "chamber-corridor");
        if (t == 90) mc.getSingleplayerServer().execute(() -> {
            // Something the owner built, for the eye to show later.
            var room = player(mc).serverLevel();
            var at = BlockPos.containing(arrival.x + 2, arrival.y, arrival.z - 4);
            room.setBlock(at, net.minecraft.world.level.block.Blocks.GOLD_BLOCK.defaultBlockState(), 3);
            room.setBlock(at.above(), net.minecraft.world.level.block.Blocks.GOLD_BLOCK.defaultBlockState(), 3);
            room.setBlock(BlockPos.containing(arrival.x - 2, arrival.y, arrival.z - 6), net.minecraft.world.level.block.Blocks.SEA_LANTERN.defaultBlockState(), 3);
            player(mc).getAbilities().flying = false;
            player(mc).onUpdateAbilities();
            player(mc).setGameMode(GameType.SURVIVAL);
        });
        if (t == 94) teleport(mc, arrival.x, arrival.y, arrival.z, 0, 0);
        if (t == 100) walkingForward = true;
        if (t == 118) capture(mc, "gate-walk-out-early");
        if (t == 130) capture(mc, "gate-walk-out");
        if (t > 100 && !mc.level.dimension().equals(com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.CHAMBER_OF_WILL) && backTick < 0) {
            backTick = t;
            walkingForward = false;
            mc.options.keyUp.setDown(false);
        }
        if (backTick >= 0 && t == backTick + 30) capture(mc, "gate-returned");
        if (backTick >= 0 && t == backTick + 32) teleport(mc, 2.4, 102, 2.3, 140, 22);
        if (backTick >= 0 && t == backTick + 46) capture(mc, "husk-close");
        if (backTick >= 0 && t == backTick + 48) teleport(mc, -0.2, 103.2, 2.1, 180, 42);
        if (backTick >= 0 && t == backTick + 60) capture(mc, "husk-mound");
        if (backTick >= 0 && t == backTick + 66) teleport(mc, 0.5, 102, 4.5, 180, 0);
        if (backTick >= 0 && t == backTick + 96) capture(mc, "husk-shut");
        if (backTick >= 0 && t >= backTick + 100 && t < backTick + 150) serverSide(mc, stone -> { if (!stone.gateOpen()) stone.takeProjectedBlood(player(mc), STONE, 10.0D, false); });
        if (backTick >= 0 && t == backTick + 130) capture(mc, "husk-feeding");
        if (backTick >= 0 && t == backTick + 151) serverSide(mc, stone -> {
            // A wall of water and glass behind the husk: what is behind the void stays behind it.
            var level = stone.getLevel();
            for (BlockPos b : BlockPos.betweenClosed(new BlockPos(-2, 102, -3), new BlockPos(3, 105, -3)))
                level.setBlock(b, (b.getX() + b.getY()) % 2 == 0 ? Blocks.WATER.defaultBlockState() : Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), 3);
        });
        if (backTick >= 0 && t == backTick + 154) capture(mc, "live-eye-front");
        if (backTick >= 0 && t == backTick + 155 && com.vincenthuto.hemomancy.client.render.world.chamberofwill.ChamberProxyView.target() != null)
            Screenshot.grab(mc.gameDirectory, "live-eye-front-texture.png", com.vincenthuto.hemomancy.client.render.world.chamberofwill.ChamberProxyView.target(),
                    message -> Hemomancy.LOGGER.info("MONOLITH_REVIEW {}", message.getString()));
        if (backTick >= 0 && t == backTick + 156) teleport(mc, 2.9, 102, 3.1, 140, 6);
        if (backTick >= 0 && t == backTick + 168) capture(mc, "live-eye-angle");
        if (backTick >= 0 && t == backTick + 170) teleport(mc, -2.9, 102, 2.6, 218, 6);
        if (backTick >= 0 && t == backTick + 182) capture(mc, "live-eye-side");
        if (backTick >= 0 && t == backTick + 184) teleport(mc, 0.5, 102, 1.4, 180, 0);
        if (backTick >= 0 && t == backTick + 196) capture(mc, "live-eye-close");
        if (backTick >= 0 && t == backTick + 198) teleport(mc, 0.5, 102, 2.4, 180, 0);
        if (backTick >= 0 && t == backTick + 210) capture(mc, "live-eye-near");
        if (backTick >= 0 && t == backTick + 214) mc.stop();
    }

    private static ServerPlayer player(Minecraft mc) {
        return mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
    }

    private static void serverSide(Minecraft mc, java.util.function.Consumer<SanguineMonolithBlockEntity> action) {
        mc.getSingleplayerServer().execute(() -> {
            if (player(mc).serverLevel().getBlockEntity(STONE) instanceof SanguineMonolithBlockEntity stone) action.accept(stone);
        });
    }

    private static void teleport(Minecraft mc, double x, double y, double z, float yaw, float pitch) {
        var playerId = mc.player.getUUID();
        mc.getSingleplayerServer().execute(() -> {
            ServerPlayer p = mc.getSingleplayerServer().getPlayerList().getPlayer(playerId);
            p.teleportTo(p.serverLevel(), x, y, z, yaw, pitch);
        });
    }

    private static void capture(Minecraft mc, String name) {
        var view = LidlessPassageClientState.local();
        Hemomancy.LOGGER.info("MONOLITH_REVIEW {} crossing={} mode={} progress={} yaw={} position={}", name, view != null,
                view == null ? null : view.mode(), LidlessPassageClientState.progress(1.0F), mc.player.getYRot(), mc.player.position());
        var playerId = mc.player.getUUID();
        mc.getSingleplayerServer().execute(() -> {
            ServerPlayer p = mc.getSingleplayerServer().getPlayerList().getPlayer(playerId);
            boolean broken = p.serverLevel().getBlockEntity(STONE) instanceof SanguineMonolithBlockEntity stone && stone.isBroken();
            boolean seedInside = p.serverLevel().getBlockEntity(STONE) instanceof SanguineMonolithBlockEntity stone && stone.seedInside();
            String gate = p.serverLevel().getBlockEntity(STONE) instanceof SanguineMonolithBlockEntity stone
                    ? "gateOpen=" + stone.gateOpen() + " proxy=" + (stone.proxy() == null ? "none" : stone.proxy().blocks().length + " blocks")
                    + " admits=" + stone.admits(p) : "no stone";
            Hemomancy.LOGGER.info("MONOLITH_REVIEW server {} crossing={} progress={} broken={} seedInside={} seedInHand={} position={} {}",
                    name, LidlessPassageService.isCrossing(p), LidlessPassageService.progress(p), broken, seedInside,
                    p.getInventory().countItem(ItemInit.qliphoth_seed.get()), p.position(), gate);
        });
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(),
                message -> Hemomancy.LOGGER.info("MONOLITH_REVIEW {}", message.getString()));
    }

    private static void setup(ServerPlayer p) {
        var level = p.serverLevel();
        for (BlockPos b : BlockPos.betweenClosed(new BlockPos(-8, 100, -8), new BlockPos(8, 101, 8)))
            level.setBlock(b, Blocks.STONE.defaultBlockState(), 3);
        for (BlockPos b : BlockPos.betweenClosed(new BlockPos(-8, 102, -8), new BlockPos(8, 108, 8)))
            level.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
        BlockState stone = BlockInit.sanguine_monolith.get().defaultBlockState().setValue(SanguineMonolithBlock.FACING, Direction.SOUTH);
        level.setBlock(STONE, stone, 3);
        ((SanguineMonolithBlock) stone.getBlock()).placeFillers(level, STONE, stone);
        HemoCapabilityAccess.getInitiatoryDegree(p).setDegreeNumber(7);
        HemoCapabilityAccess.getBloodVolume(p).setActive(true);
        p.setGameMode(GameType.SURVIVAL);
        p.teleportTo(level, 0.5, 102, 4.5, 180, 0);
        Hemomancy.LOGGER.info("MONOLITH_REVIEW setup");
        prepared = true;
    }
}
