package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemoryEquipRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemoryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemoryPrimingRules;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.component.TinctureDoseData;
import com.vincenthuto.hemomancy.common.mission.alchemist.BodyAnswersAssignment;
import com.vincenthuto.hemomancy.common.mission.mnemonist.MnemonicReliquaryProgression;
import com.vincenthuto.hemomancy.common.mission.shared.MnemonicRecipeKnowledge;
import com.vincenthuto.hemomancy.common.util.HemoText;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
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

/**
 * One item per Thelemic Memory. A stack is a flask unless its dose component says it holds a jug's worth, which also
 * decides its name, its icon and the empty vessel it returns.
 */
public class ThelemicMemoryTinctureItem extends Item {
    private final ThelemicMemory memory;

    public ThelemicMemoryTinctureItem(Properties properties, ThelemicMemory memory) {
        super(properties.stacksTo(1));
        this.memory = memory;
    }

    public ThelemicMemory memory() { return memory; }

    public static int maximumDoses(ItemStack stack) {
        TinctureDoseData data = stack.get(DataComponentInit.TINCTURE_DOSES.get());
        return data == null ? ThelemicMemoryPrimingRules.FLASK_DOSES : data.maximum();
    }

    public static boolean isJug(ItemStack stack) {
        return maximumDoses(stack) >= ThelemicMemoryPrimingRules.JUG_DOSES;
    }

    /** A full jug of {@code tincture}. */
    public static ItemStack jugOf(Item tincture) {
        ItemStack jug = new ItemStack(tincture);
        jug.set(DataComponentInit.TINCTURE_DOSES.get(),
                new TinctureDoseData(ThelemicMemoryPrimingRules.JUG_DOSES, ThelemicMemoryPrimingRules.JUG_DOSES));
        return jug;
    }

    public int remainingDoses(ItemStack stack) {
        TinctureDoseData data = stack.get(DataComponentInit.TINCTURE_DOSES.get());
        return TinctureDoseRules.normalizeRemaining(data == null ? null : data.remaining(), maximumDoses(stack));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack) + (isJug(stack) ? ".jug" : ""));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hemomancy.tincture_" + memory.id()));
        tooltip.add(Component.translatable("tooltip.hemomancy.thelemic_memory_tincture.thelemic_memory_s_5_00_reserve", sectionName()));
        tooltip.add(HemoText.translatable("tooltip.hemomancy.thelemic_memory_tincture.blood_vascular_strain_per_trigger",
        		trimCost(memory.bloodCost()),
        		trimCost(memory.vascularStrain())));
        tooltip.add(Component.translatable("tooltip.hemomancy.tincture_doses",
                remainingDoses(stack), maximumDoses(stack)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        int remaining = TinctureDoseRules.consume(remainingDoses(stack));
        if (living instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            var state = HemoCapabilityAccess.thelemicMemory(player);
            state.learnAndAddReserve(memory, ThelemicMemoryPrimingRules.TICKS_PER_DOSE);
            var known = HemoCapabilityAccess.getKnownManipulations(player);
                            MemoryEquipRules.autoEquipThelemicMemory(known.getEquippedManipNames(), memory,
                                    ManipSlotHelper.getMaxSlots(player));
            				MnemonicReliquaryProgression.onCapacityChanged(player, known);
                            KnownManipulationEvents.syncPlayerEvent(player);
            ThelemicMemoryEvents.sync(player);
            if (memory == ThelemicMemory.SANGUINE_FISTS) {
                BodyAnswersAssignment.markComplete(player);
                MnemonicRecipeKnowledge.awardCatalogue(player);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        Player player = living instanceof Player found ? found : null;
        if (player == null || !player.getAbilities().instabuild) {
            if (remaining == 0) {
                ItemStack empty = new ItemStack(isJug(stack) ? ItemInit.cured_clay_jug.get() : HLItemInit.cured_clay_flask.get());
                stack.shrink(1);
				level.gameEvent(living, GameEvent.DRINK, living.getEyePosition());
				if (stack.isEmpty()) return empty;
                if (player != null && !player.getInventory().add(empty)) player.drop(empty, false);
            } else {
                stack.set(DataComponentInit.TINCTURE_DOSES.get(), new TinctureDoseData(remaining, maximumDoses(stack)));
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
