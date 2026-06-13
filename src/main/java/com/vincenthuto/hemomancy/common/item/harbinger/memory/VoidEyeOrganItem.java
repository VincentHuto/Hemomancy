package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class VoidEyeOrganItem extends HematicMemoryToolItem {
    private static final int BLOOD_COST = 35;

    public VoidEyeOrganItem(Properties properties) {
        super(properties, 3);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canUseHematicTool(player) || !drainBlood(player, BLOOD_COST)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, p) ->
                    ChestMenu.threeRows(id, inventory, p.getEnderChestInventory()),
                    Component.translatable("container.enderchest")));
            if (VoidEyeOrganRules.shouldApplyCooldownAfterUse()) {
                player.getCooldowns().addCooldown(this, 20 * 60);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            if (VoidEyeOrganRules.shouldConsumeOnUse(player.getAbilities().instabuild)) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hemomancy.void_eye_organ.tooltip").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
