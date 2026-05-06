package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

public class StainedChurchMapItem extends Item {
    private static final TagKey<Structure> TARGETS = TagKey.create(
            Registries.STRUCTURE, Hemomancy.rloc("stained_church_map_targets"));
    private static final int SEARCH_RADIUS = 100;

    public StainedChurchMapItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos target = serverLevel.findNearestMapStructure(TARGETS, player.blockPosition(), SEARCH_RADIUS, false);
        if (target == null) {
            player.displayClientMessage(
                    Component.translatable("item.hemomancy.stained_church_map.not_found")
                            .withStyle(ChatFormatting.GRAY),
                    false);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        int dx = target.getX() - player.blockPosition().getX();
        int dz = target.getZ() - player.blockPosition().getZ();
        int distance = Mth.floor(Math.sqrt(dx * dx + dz * dz));
        player.displayClientMessage(
                Component.translatable("item.hemomancy.stained_church_map.points",
                                directionName(dx, dz), distance, target.getX(), target.getZ())
                        .withStyle(ChatFormatting.DARK_RED),
                false);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.hemomancy.stained_church_map.tooltip")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.hemomancy.stained_church_map.tooltip.use")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    private static Component directionName(int dx, int dz) {
        if (dx == 0 && dz == 0) {
            return Component.translatable("direction.hemomancy.here");
        }
        String[] keys = {
                "direction.hemomancy.east",
                "direction.hemomancy.southeast",
                "direction.hemomancy.south",
                "direction.hemomancy.southwest",
                "direction.hemomancy.west",
                "direction.hemomancy.northwest",
                "direction.hemomancy.north",
                "direction.hemomancy.northeast"
        };
        double angle = Math.atan2(dz, dx);
        int index = Mth.floor(angle / (Math.PI / 4.0D) + 0.5D) & 7;
        return Component.translatable(keys[index]);
    }
}
