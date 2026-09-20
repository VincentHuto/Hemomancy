package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.*;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryContext;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.PacketSyncClinicalBlood;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import static com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress.Lesson;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ClinicalBloodKnowledge {
    private ClinicalBloodKnowledge() {}

    public static boolean eligible(Player player) {
        var context = ItemInquiryContext.from(player);
        return context.degree() >= 1 && !context.purifying() && !context.clarityUnlocked();
    }
    public static boolean sample(ItemStack stack) {
        return stack.getItem() instanceof BloodVialItem && BloodSampleData.isStorableSample(stack)
                && BloodSampleData.entityType(stack) != null;
    }
    public static void collected(Player player, ItemStack vial) {
        if (!(player instanceof ServerPlayer server) || !eligible(player) || !sample(vial)) return;
        var progress = HemoCapabilityAccess.clinicalBlood(player);
        if (progress.collected) return;
        progress.collected = true;
        unlockEntry(server, "specimen");
        HarbingerAdvancementGranter.grantIfNotDone(server, Hemomancy.rloc("hemomancy/clinical_collected"));
        sync(server);
    }
    public static boolean holdsSample(Player player) {
        return sample(player.getMainHandItem()) || sample(player.getOffhandItem());
    }
    public static boolean canLearn(ServerPlayer player, Lesson lesson) {
        return eligible(player) && HemoCapabilityAccess.clinicalBlood(player)
                .canLearn(lesson, HemoCapabilityAccess.getPlayerDegreeNumber(player))
                && (lesson != Lesson.MICROSCOPE || holdsSample(player));
    }
    public static boolean teach(ServerPlayer player, Entity npc, Lesson lesson) {
        boolean teacher = switch (lesson.teacher) {
            case "alchemist" -> npc instanceof HarbingerAlchemistEntity;
            case "artificer" -> npc instanceof HarbingerArtificerEntity;
            case "mnemonist" -> npc instanceof HarbingerMnemonistEntity;
            default -> false;
        };
        if (!teacher || !npc.isAlive() || npc.distanceToSqr(player) > 64 || !canLearn(player, lesson)) return false;
        HemoCapabilityAccess.clinicalBlood(player).learn(lesson);
        for (String path : recipes(lesson)) player.server.getRecipeManager().byKey(Hemomancy.rloc(path))
                .ifPresent(recipe -> player.awardRecipes(List.of(recipe)));
        unlockEntry(player, lesson.key());
        if (lesson == Lesson.CLAIRAUDIOGRAPH) LiberKnowledgeHelper.unlockEntry(player,
                Hemomancy.rloc("fanesanguinium/the_infection/pages/clairaudiograph"), HemomancyDiscoverySource.DIALOGUE);
        HarbingerAdvancementGranter.grantIfNotDone(player, Hemomancy.rloc("hemomancy/clinical_" + lesson.key()));
        sync(player);
        return true;
    }
    public static List<String> recipes(Lesson lesson) {
        return lesson == Lesson.CLAIRAUDIOGRAPH ? List.of("clairaudiograph", "ambergris_cylinder")
                : lesson.recipe == null ? List.of() : List.of(lesson.recipe);
    }
    public static boolean recipeVisible(Player player, ResourceLocation recipe) {
        if (!recipe.getNamespace().equals(Hemomancy.MOD_ID)) return true;
        for (Lesson lesson : Lesson.values()) if (recipes(lesson).contains(recipe.getPath()))
            return player != null && (player.isCreative() || eligible(player)
                    && HemoCapabilityAccess.clinicalBlood(player).knows(lesson));
        return true;
    }
    public static boolean canInject(Player player) {
        return player.isCreative() || eligible(player) && HemoCapabilityAccess.clinicalBlood(player).knows(Lesson.INJECTION);
    }
    public static void examined(ServerPlayer player, ItemStack vial) {
        if (!eligible(player) || !sample(vial) || !BloodSampleData.identified(vial)) return;
        var progress = HemoCapabilityAccess.clinicalBlood(player);
        if (!progress.recordExamination(BloodSampleData.sourceId(vial).toString())) return;
        unlockEntry(player, "microscopy");
        HarbingerAdvancementGranter.grantIfNotDone(player, Hemomancy.rloc("hemomancy/clinical_examined"));
        if (progress.sourceCount() >= 3) {
            unlockEntry(player, "ordered_collection");
            HarbingerAdvancementGranter.grantIfNotDone(player, Hemomancy.rloc("hemomancy/clinical_three_sources"));
        }
        sync(player);
    }
    public static void cabinetTransfer(Player player, boolean inserted) {
        if (!(player instanceof ServerPlayer server) || !eligible(player)) return;
        var progress = HemoCapabilityAccess.clinicalBlood(player);
        boolean changed = inserted ? !progress.cabinetInserted : !progress.cabinetWithdrawn;
        if (inserted) progress.cabinetInserted = true;
        else progress.cabinetWithdrawn = true;
        if (changed) sync(server);
    }
    public static void met(ServerPlayer player, String teacher) {
        if (!eligible(player)) return;
        var progress = HemoCapabilityAccess.clinicalBlood(player);
        boolean changed = false;
        collected(player, player.getMainHandItem());
        collected(player, player.getOffhandItem());
        if (teacher.equals("artificer") && !progress.artificerMet) { progress.artificerMet = true; changed = true; }
        if (teacher.equals("mnemonist") && !progress.mnemonistMet) { progress.mnemonistMet = true; changed = true; }
        if (changed) sync(player);
    }
    public static void unlockEntry(ServerPlayer player, String lesson) {
        LiberKnowledgeHelper.unlockEntry(player, entry(lesson), HemomancyDiscoverySource.DIALOGUE);
    }
    public static ResourceLocation entry(String lesson) {
        return Hemomancy.rloc("fanesanguinium/the_infection/pages/clinical_" + lesson);
    }
    public static void sync(ServerPlayer player) {
        PacketHandler.sendToPlayer(player, new PacketSyncClinicalBlood(
                HemoCapabilityAccess.clinicalBlood(player).serializeNBT(player.registryAccess())));
    }
    @SubscribeEvent public static void crafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !eligible(player)) return;
        if (event.getCrafting().is(BlockInit.phlebotomists_cabinet.get().asItem())) {
            HemoCapabilityAccess.clinicalBlood(player).cabinetCrafted = true;
            sync(player);
        }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0 || !eligible(player)) return;
        var progress = HemoCapabilityAccess.clinicalBlood(player);
        boolean changed = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!progress.collected) collected(player, stack);
            if (!progress.hematicIronObtained && stack.is(ItemInit.hematic_iron_scrap.get())) {
                progress.hematicIronObtained = true;
                changed = true;
            }
        }
        if (changed) sync(player);
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (Lesson lesson : Lesson.values()) for (String path : recipes(lesson))
                player.server.getRecipeManager().byKey(Hemomancy.rloc(path)).ifPresent(recipe -> {
                    if (HemoCapabilityAccess.clinicalBlood(player).knows(lesson)) player.awardRecipes(List.of(recipe));
                    else if (!player.isCreative()) player.resetRecipes(List.of(recipe));
                });
            sync(player);
        }
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }
}
