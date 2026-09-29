package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryContext;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.HashSet;

public final class OverworldFungalSurvey {
    private static final String DATA = "hemomancy:overworld_fungal_survey";
    private static final String VISITED = "Visited";
    private static final String REPORTED = "Reported";
    private static final TagKey<Item> SPECIMENS = TagKey.create(BuiltInRegistries.ITEM.key(),
            Hemomancy.rloc("overworld_fungal_survey_specimens"));

    private OverworldFungalSurvey() {}

    public static void observeLocation(ServerPlayer player) {
        recordVisit(player, player.level().dimension(),
                player.level().getBiome(player.blockPosition()).unwrapKey().orElse(null));
    }

    public static void recordVisit(ServerPlayer player, ResourceKey<Level> dimension,
            ResourceKey<Biome> biome) {
        if (visited(player) || !Level.OVERWORLD.equals(dimension)
                || !BiomeInit.FUNGAL_GARDENS.equals(biome)) return;
        CompoundTag data = data(player);
        data.putBoolean(VISITED, true);
        save(player, data);
    }

    public static boolean visited(ServerPlayer player) {
        return data(player).getBoolean(VISITED);
    }

    public static boolean reported(ServerPlayer player) {
        return data(player).getBoolean(REPORTED);
    }

    public static boolean eligible(ServerPlayer player) {
        ItemInquiryContext context = ItemInquiryContext.from(player);
        return context.degree() >= 2 && context.activeBlood() && !context.purifying()
                && !context.clarityUnlocked();
    }

    public static OverworldFungalSurveyProgress progress(ServerPlayer player) {
        var specimens = new HashSet<ResourceLocation>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var stack = player.getInventory().getItem(slot);
            if (stack.is(SPECIMENS)) specimens.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
        boolean reported = reported(player);
        return new OverworldFungalSurveyProgress(visited(player), reported ? Math.max(2, specimens.size())
                : specimens.size(), reported);
    }

    public static boolean report(ServerPlayer player, Entity teacher) {
        if (!(teacher instanceof HarbingerAlchemistEntity) || !EarlyInitiation.near(player, teacher)
                || !eligible(player) || !progress(player).ready()) return false;
        CompoundTag data = data(player);
        data.putBoolean(REPORTED, true);
        save(player, data);
        player.displayClientMessage(Component.translatable("hemomancy.alchemist.fungal_survey.recorded")
                .withStyle(ChatFormatting.DARK_RED), false);
        return true;
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
