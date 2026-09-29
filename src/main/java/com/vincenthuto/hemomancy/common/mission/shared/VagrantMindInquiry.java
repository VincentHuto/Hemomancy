package com.vincenthuto.hemomancy.common.mission.shared;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ChoirKeeperEntity;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.MyelinBorerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryContext;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import com.vincenthuto.hemomancy.common.worldgen.structure.VagrantMindPiece;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public final class VagrantMindInquiry {
    private static final String DATA = "hemomancy:vagrant_mind_inquiry";
    private static final String MIND_VISITED = "MindVisited";
    private static final String BIOLOGY_OBSERVED = "BiologyObserved";
    private static final String MEMORY_REPORTED = "MemoryReported";
    private static final String BIOLOGY_REPORTED = "BiologyReported";
    private static final ResourceKey<Structure> MIND = ResourceKey.create(Registries.STRUCTURE,
            Hemomancy.rloc("vagrant_mind"));

    private VagrantMindInquiry() {}

    public static boolean eligible(ServerPlayer player) {
        ItemInquiryContext context = ItemInquiryContext.from(player);
        return context.degree() >= 6 && context.activeBlood() && !context.purifying()
                && !context.clarityUnlocked();
    }

    public static VagrantMindInquiryProgress progress(ServerPlayer player) {
        CompoundTag data = data(player);
        return new VagrantMindInquiryProgress(data.getBoolean(MIND_VISITED),
                data.getBoolean(BIOLOGY_OBSERVED), data.getBoolean(MEMORY_REPORTED),
                data.getBoolean(BIOLOGY_REPORTED));
    }

    public static void observeMind(ServerPlayer player) {
        if (progress(player).mindVisited() || player.isSpectator() || !Level.END.equals(player.level().dimension())) return;
        var start = player.serverLevel().structureManager().getStructureWithPieceAt(
                player.blockPosition(), holder -> holder.is(MIND));
        if (start.isValid() && start.getPieces().stream()
                .filter(VagrantMindPiece.class::isInstance).map(VagrantMindPiece.class::cast)
                .anyMatch(piece -> piece.containsInterior(player.blockPosition()))) {
            mark(player, MIND_VISITED);
        }
    }

    public static void observeBiology(ServerPlayer player) {
        if (progress(player).biologyObserved() || player.isSpectator()
                || !Level.END.equals(player.level().dimension())) return;
        AABB nearby = player.getBoundingBox().inflate(24);
        boolean organism = !player.level().getEntitiesOfClass(ChoirKeeperEntity.class, nearby).isEmpty()
                || !player.level().getEntitiesOfClass(MyelinBorerEntity.class, nearby).isEmpty();
        if (organism || nearbyChorus(player)) {
            mark(player, BIOLOGY_OBSERVED);
        }
    }

    private static boolean nearbyChorus(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -2, -4), center.offset(4, 4, 4))) {
            if (player.level().getBlockState(pos).is(Blocks.CHORUS_PLANT)
                    || player.level().getBlockState(pos).is(Blocks.CHORUS_FLOWER)) return true;
        }
        return false;
    }

    public static boolean reportMemory(ServerPlayer player, Entity teacher) {
        return report(player, teacher, HarbingerMnemonistEntity.class::isInstance,
                VagrantMindInquiryProgress::memoryReady, MEMORY_REPORTED,
                "hemomancy.vagrant_mind_inquiry.memory_recorded");
    }

    public static boolean reportBiology(ServerPlayer player, Entity teacher) {
        return report(player, teacher, HarbingerAlchemistEntity.class::isInstance,
                VagrantMindInquiryProgress::biologyReady, BIOLOGY_REPORTED,
                "hemomancy.vagrant_mind_inquiry.biology_recorded");
    }

    private static boolean report(ServerPlayer player, Entity teacher, Predicate<Entity> owner,
            Predicate<VagrantMindInquiryProgress> ready, String key, String message) {
        if (teacher == null || !owner.test(teacher) || !EarlyInitiation.near(player, teacher)
                || !eligible(player) || !ready.test(progress(player))) return false;
        mark(player, key);
        player.displayClientMessage(Component.translatable(message).withStyle(ChatFormatting.DARK_RED), false);
        return true;
    }

    private static void mark(ServerPlayer player, String key) {
        CompoundTag data = data(player);
        data.putBoolean(key, true);
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(DATA, data);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static CompoundTag data(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(DATA);
    }
}
