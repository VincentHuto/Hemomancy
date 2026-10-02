package com.vincenthuto.hemomancy.common.mission.vicar;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryContext;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVoyagerEntity;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class VoyagerIntroduction {
    private static final String DATA = "hemomancy:voyager_introduction";
    private static final String OBSERVED = "Observed";
    private static final String REPORTED = "Reported";
    private static final ResourceKey<Structure> VOYAGER_VESSEL = ResourceKey.create(Registries.STRUCTURE,
            Hemomancy.rloc("harbinger_voyager_vessel"));

    private VoyagerIntroduction() {}

    public static boolean observed(ServerPlayer player) {
        return data(player).getBoolean(OBSERVED);
    }

    public static VoyagerIntroductionProgress progress(ServerPlayer player) {
        return new VoyagerIntroductionProgress(observed(player), data(player).getBoolean(REPORTED));
    }

    public static boolean canObserve(ServerPlayer player, Entity teacher) {
        ItemInquiryContext context = ItemInquiryContext.from(player);
        return context.degree() >= 1 && context.activeBlood() && !context.purifying()
                && !context.clarityUnlocked() && teacher instanceof HarbingerVoyagerEntity
                && EarlyInitiation.near(player, teacher) && fieldSite(teacher) && !observed(player);
    }

    public static boolean observe(ServerPlayer player, Entity teacher) {
        if (!canObserve(player, teacher)) return false;
        CompoundTag data = data(player);
        data.putBoolean(OBSERVED, true);
        save(player, data);
        player.displayClientMessage(Component.translatable("hemomancy.voyager.introduction.observed")
                .withStyle(ChatFormatting.DARK_RED), false);
        return true;
    }

    public static boolean eligibleForReferral(ServerPlayer player) {
        ItemInquiryContext context = ItemInquiryContext.from(player);
        return context.degree() >= 3 && context.activeBlood() && !context.purifying()
                && !context.clarityUnlocked();
    }

    public static boolean canReport(ServerPlayer player) {
        return eligibleForReferral(player) && progress(player).ready();
    }

    public static boolean canRequestBearing(ServerPlayer player, Entity teacher) {
        return teacher instanceof HarbingerVicarEntity && EarlyInitiation.near(player, teacher)
                && eligibleForReferral(player) && !observed(player) && !progress(player).reported()
                && Level.OVERWORLD.equals(player.level().dimension());
    }

    public static boolean tellBearing(ServerPlayer player, Entity teacher) {
        if (!canRequestBearing(player, teacher)) return false;
        return VoyagerVesselGuidance.request(player);
    }

    public static boolean report(ServerPlayer player, Entity teacher) {
        if (!(teacher instanceof HarbingerVicarEntity) || !EarlyInitiation.near(player, teacher)
                || !canReport(player)) return false;
        CompoundTag data = data(player);
        data.putBoolean(REPORTED, true);
        save(player, data);
        player.displayClientMessage(Component.translatable("hemomancy.vicar.voyager_introduction.recorded")
                .withStyle(ChatFormatting.DARK_RED), false);
        return true;
    }

    private static boolean fieldSite(Entity teacher) {
        if (!teacher.level().dimension().equals(Level.OVERWORLD)) return false;
        ResourceKey<Biome> biome = teacher.level().getBiome(teacher.blockPosition()).unwrapKey().orElse(null);
        if (BiomeInit.ERYTHROCORAL_REEF.equals(biome)) return true;
        boolean onVessel = teacher.getServer().overworld().structureManager().getStructureWithPieceAt(
                teacher.blockPosition(), holder -> holder.is(VOYAGER_VESSEL)).isValid();
        return isFieldSite(teacher.level().dimension(), biome, onVessel);
    }

    public static boolean isFieldSite(ResourceKey<Level> dimension, ResourceKey<Biome> biome,
            boolean onVessel) {
        return Level.OVERWORLD.equals(dimension)
                && (BiomeInit.ERYTHROCORAL_REEF.equals(biome) || onVessel);
    }

    private static CompoundTag data(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(DATA);
    }

    private static void save(ServerPlayer player, CompoundTag data) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(DATA, data);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
