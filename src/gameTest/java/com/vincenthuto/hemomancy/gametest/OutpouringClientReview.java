package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.CastingAnimationClientState;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringClientState;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPurpose;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.ParticleStatus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * With hemomancy.outpouringReview, previews Harbinger cast outpourings on the local player in a stone arena (a floor, a
 * wall behind and one to the left so blood has surfaces to mark) and captures build, peak and aftermath shots into
 * build/outpouring-client-review/screenshots. Run with outpouringReviewClient. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class OutpouringClientReview {
    private record Scene(String name, EnumBloodTendency school, EnumManipulationRank rank, EnumVeinSections section,
                         boolean build, boolean after, boolean night, boolean firstPerson, boolean armour, boolean minimal,
                         boolean corner) {
        Scene(String name, EnumBloodTendency school, EnumManipulationRank rank, EnumVeinSections section, boolean build,
              boolean after, boolean night, boolean firstPerson, boolean armour, boolean minimal) {
            this(name, school, rank, section, build, after, night, firstPerson, armour, minimal, false);
        }
    }
    private static final int LENGTH = 56, RELEASE = 12;
    private static final List<Scene> SCENES = scenes();
    private static int ticks, scene = -1;
    private static long sequence = 5000;
    private static BlockPos origin;

    private static List<Scene> scenes() {
        List<Scene> all = new ArrayList<>();
        EnumManipulationRank[] ranks = {EnumManipulationRank.HUMILIS, EnumManipulationRank.SUMMA, EnumManipulationRank.PERFECTUS};
        for (var school : EnumBloodTendency.values()) for (int r = 0; r < ranks.length; r++) {
            var section = EnumVeinSections.values()[(school.ordinal() + r) % 5];
            all.add(new Scene(name(school, ranks[r], section), school, ranks[r], section, true, true, false, false, false, false));
        }
        for (var section : EnumVeinSections.values())
            all.add(new Scene("sweep-" + name(EnumBloodTendency.ANIMUS, EnumManipulationRank.SUMMA, section),
                    EnumBloodTendency.ANIMUS, EnumManipulationRank.SUMMA, section, false, false, false, false, false, false));
        all.add(new Scene("night-lux", EnumBloodTendency.LUX, EnumManipulationRank.PERFECTUS, EnumVeinSections.HEART, false, true, true, false, false, false));
        all.add(new Scene("night-tenebris", EnumBloodTendency.TENEBRIS, EnumManipulationRank.PERFECTUS, EnumVeinSections.BODY, false, true, true, false, false, false));
        all.add(new Scene("fp-lux", EnumBloodTendency.LUX, EnumManipulationRank.PERFECTUS, EnumVeinSections.ARMS, false, false, false, true, false, false));
        all.add(new Scene("fp-tenebris", EnumBloodTendency.TENEBRIS, EnumManipulationRank.PERFECTUS, EnumVeinSections.BODY, false, false, false, true, false, false));
        all.add(new Scene("fp-flammeus", EnumBloodTendency.FLAMMEUS, EnumManipulationRank.MAGISTER, EnumVeinSections.ARMS, false, false, false, true, false, false));
        all.add(new Scene("armour-congeatio", EnumBloodTendency.CONGEATIO, EnumManipulationRank.SUMMA, EnumVeinSections.BODY, false, true, false, false, true, false));
        // Standing in the corner of the two walls, so pooled blood is pushed against both.
        all.add(new Scene("corner-animus", EnumBloodTendency.ANIMUS, EnumManipulationRank.PERFECTUS, EnumVeinSections.LEGS, false, true, false, false, false, false, true));
        all.add(new Scene("minimal-ferric", EnumBloodTendency.FERRIC, EnumManipulationRank.PERFECTUS, EnumVeinSections.LEGS, false, false, false, false, false, true));
        String only = System.getProperty("hemomancy.outpouringReview.only", "");
        return only.isEmpty() ? all : all.stream().filter(s -> java.util.Arrays.stream(only.split(",")).anyMatch(s.name::contains)).toList();
    }

    private static String name(EnumBloodTendency school, EnumManipulationRank rank, EnumVeinSections section) {
        return (school + "-" + rank + "-" + section).toLowerCase();
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.outpouringReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("outpouring-client-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = true;
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        if (++ticks == 40) {
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                origin = new BlockPos(player.getBlockX(), 150, player.getBlockZ());
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String cmd : new String[]{"gamemode creative", "time set 6000", "gamerule doDaylightCycle false",
                        "weather clear", "gamerule doMobSpawning false"})
                    server.getCommands().performPrefixedCommand(source, cmd);
                for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, 0, -8), origin.offset(8, 6, 8))) {
                    int dx = pos.getX() - origin.getX(), dy = pos.getY() - origin.getY(), dz = pos.getZ() - origin.getZ();
                    boolean floor = dy == 0, back = dz == -2 && dy <= 3 && Math.abs(dx) <= 3, side = dx == 2 && dy <= 3 && dz >= -2 && dz <= 2;
                    level.setBlock(pos, floor || back || side ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);
                }
                player.teleportTo(level, origin.getX() + .5, origin.getY() + 1, origin.getZ() + .5, 0, 0);
                Hemomancy.LOGGER.info("OUTPOURING_REVIEW arena at {}", origin);
            });
        }
        if (ticks < 120 || origin == null) return;
        int local = (ticks - 120) % LENGTH;
        if (local == 0) {
            if (scene >= 0) finish(mc, server, uuid, SCENES.get(scene));
            scene++;
            if (scene >= SCENES.size()) { Hemomancy.LOGGER.info("OUTPOURING_REVIEW done"); mc.stop(); return; }
            start(mc, server, uuid, SCENES.get(scene));
        }
        var s = SCENES.get(scene);
        long now = mc.level.getGameTime();
        if (local == 2) send(mc, s, CastPhase.OPENING, now, 10);
        if (local == 8) send(mc, s, CastPhase.HOLD, now, 30);
        if (local == 10 && s.build) shot(mc, s, "build");
        if (local == RELEASE) send(mc, s, CastPhase.RELEASE, now, 40);
        if (local == RELEASE + 4) shot(mc, s, "peak");
        if (local == RELEASE + 24 && s.after) shot(mc, s, "after");
    }

    private static void start(Minecraft mc, MinecraftServer server, java.util.UUID uuid, Scene s) {
        OutpouringClientState.clear();
        sequence++;
        mc.options.setCameraType(s.firstPerson ? CameraType.FIRST_PERSON : CameraType.THIRD_PERSON_FRONT);
        mc.options.particles().set(s.minimal ? ParticleStatus.MINIMAL : ParticleStatus.ALL);
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(uuid);
            var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
            server.getCommands().performPrefixedCommand(source, s.night ? "time set 18000" : "time set 6000");
            double x = origin.getX() + (s.corner ? 1.6 : .5), z = origin.getZ() + (s.corner ? -.4 : .5);
            player.teleportTo(player.serverLevel(), x, origin.getY() + 1, z, s.corner ? 45 : 0, s.firstPerson ? 20 : 0);
            if (s.armour) {
                player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
                player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
            }
        });
    }

    private static void finish(Minecraft mc, MinecraftServer server, java.util.UUID uuid, Scene s) {
        if (!s.armour) return;
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(uuid);
            for (var slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET})
                player.setItemSlot(slot, ItemStack.EMPTY);
        });
    }

    private static void send(Minecraft mc, Scene s, CastPhase phase, long now, int held) {
        CastingAnimationClientState.preview(CastingAnimationPacket.harbinger(mc.player.getId(), mc.player.getUUID(),
                OutpouringRules.previewId(s.section), sequence, s.school, null, s.rank, CastPurpose.AREA, CastStyle.SCHOOL,
                phase, now, now, held, 40, InteractionHand.MAIN_HAND));
    }

    private static void shot(Minecraft mc, Scene s, String moment) {
        String file = "outpouring-" + String.format("%02d", scene) + "-" + s.name + "-" + moment + ".png";
        Screenshot.grab(mc.gameDirectory, file, mc.getMainRenderTarget(), message -> {});
        Hemomancy.LOGGER.info("OUTPOURING_REVIEW captured {}", file);
    }
}
