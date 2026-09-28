package com.vincenthuto.hemomancy.common.mission.alchemist;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.DegreeProgression;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ConcentratedBlood {
    public static final String CONTENT = "hemomancy:concentrated_blood";
    private static final String PENDING = "hemomancy:concentrated_blood_pending";
    private ConcentratedBlood() {}
    public static ItemStack create() {
        ItemStack stack = new ItemStack(ItemInit.bloody_vial.get());
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(CONTENT, true);
        tag.putBoolean(BloodVialItem.TAG_STATE, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }
    public static boolean is(ItemStack stack) {
        return stack.is(ItemInit.bloody_vial.get()) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(CONTENT);
    }
    public static boolean pending(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(PENDING);
    }
    private static void pending(Player player, boolean value) {
        var data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        data.putBoolean(PENDING, value);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
    }
    public static boolean canInject(ServerPlayer player) {
        return HemoCapabilityAccess.getPlayerDegreeNumber(player) == 1 && EarlyInitiation.attached(player)
                && FirstSeparationAssignment.isClaimed(player) && !pending(player)
                && ClinicalBloodKnowledge.eligible(player);
    }
    public static boolean inject(ServerPlayer player) {
        if (!canInject(player)) return false;
        pending(player, true);
        player.displayClientMessage(Component.translatable("hemomancy.initiation.blood_rest"), false);
        return true;
    }
    public static boolean completeSleep(ServerPlayer player, boolean completed) {
        if (!completed || !pending(player) || !EarlyInitiation.attached(player)
                || HemoCapabilityAccess.getPlayerDegreeNumber(player) != 1 || !ClinicalBloodKnowledge.eligible(player)) return false;
        if (!DegreeProgression.advance(player, 2)) return false;
        pending(player, false);
        player.displayClientMessage(Component.translatable("hemomancy.initiation.awakened"), false);
        return true;
    }
    public static boolean replace(ServerPlayer player, Entity npc) {
        if (!(npc instanceof HarbingerAlchemistEntity) || !EarlyInitiation.near(player, npc) || !canInject(player)) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) if (is(player.getInventory().getItem(i))) return false;
        EarlyInitiation.give(player, create());
        player.displayClientMessage(Component.translatable("hemomancy.initiation.blood_rest"), false);
        return true;
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event) { pending(event.getEntity(), pending(event.getOriginal())); }
}
