package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemoryEquipRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryPrimingRules;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.item.component.TinctureDoseData;
import com.vincenthuto.hemomancy.common.mission.alchemist.BodyAnswersAssignment;
import com.vincenthuto.hemomancy.common.mission.mnemonist.MnemonicReliquaryProgression;
import com.vincenthuto.hemomancy.common.mission.shared.MnemonicRecipeKnowledge;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;
import java.util.function.Supplier;

public class MuscleMemoryTinctureItem extends Item {
    private final MuscleMemory memory;
    private final int maximumDoses;
    private final Supplier<Item> emptyContainer;

    public MuscleMemoryTinctureItem(Properties properties, MuscleMemory memory, int maximumDoses,
            Supplier<Item> emptyContainer) {
        super(properties.stacksTo(1));
        this.memory = memory;
        this.maximumDoses = Math.max(1, maximumDoses);
        this.emptyContainer = emptyContainer;
    }

    public MuscleMemory memory() { return memory; }
    public int maximumDoses() { return maximumDoses; }

    public int remainingDoses(ItemStack stack) {
        TinctureDoseData data = stack.get(DataComponentInit.TINCTURE_DOSES.get());
        return TinctureDoseRules.normalizeRemaining(data == null ? null : data.remaining(), maximumDoses);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hemomancy.tincture_" + memory.id()));
        tooltip.add(Component.literal("Thelemic Memory • " + sectionName() + " • 5:00 reserve per dose"));
        tooltip.add(Component.literal(trimCost(memory.bloodCost()) + " blood • "
                + trimCost(memory.vascularStrain()) + " vascular strain per trigger"));
        tooltip.add(Component.translatable("tooltip.hemomancy.tincture_doses",
                remainingDoses(stack), maximumDoses).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        int remaining = TinctureDoseRules.consume(remainingDoses(stack));
        if (living instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            var state = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
            state.learnAndAddReserve(memory, MuscleMemoryPrimingRules.TICKS_PER_DOSE);
            HemoCapabilityAccess.getKnownManipulations(player).ifPresent(known -> {
                MemoryEquipRules.autoEquipMuscleMemory(known.getEquippedManipNames(), memory,
                        ManipSlotHelper.getMaxSlots(player));
				MnemonicReliquaryProgression.onCapacityChanged(player, known);
                PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
            });
            MuscleMemoryEvents.sync(player);
            if (memory == MuscleMemory.SANGUINE_FISTS) {
                BodyAnswersAssignment.markComplete(player);
                MnemonicRecipeKnowledge.awardCatalogue(player);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        Player player = living instanceof Player found ? found : null;
        if (player == null || !player.getAbilities().instabuild) {
            if (remaining == 0) {
                stack.shrink(1);
                ItemStack empty = new ItemStack(emptyContainer.get());
				level.gameEvent(living, GameEvent.DRINK, living.getEyePosition());
				if (stack.isEmpty()) return empty;
                if (player != null && !player.getInventory().add(empty)) player.drop(empty, false);
            } else {
                stack.set(DataComponentInit.TINCTURE_DOSES.get(), new TinctureDoseData(remaining, maximumDoses));
            }
        }
        level.gameEvent(living, GameEvent.DRINK, living.getEyePosition());
        return stack;
    }

    private String sectionName() {
        String value = memory.section().name().toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static String trimCost(double cost) {
		return String.format(java.util.Locale.ROOT, "%.2f", cost)
				.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }
}
