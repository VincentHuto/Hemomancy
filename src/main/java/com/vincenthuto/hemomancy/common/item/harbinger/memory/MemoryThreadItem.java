package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class MemoryThreadItem extends HematicMemoryToolItem {
    private static final String TAG_ACTIVE = "MemoryThreadActive";
    private static final String TAG_DIMENSION = "MemoryThreadDimension";
    private static final String TAG_POINTS = "MemoryThreadPoints";
    private static final String TAG_POINT = "Pos";
    private static final DustParticleOptions THREAD_DUST = new DustParticleOptions(new Vector3f(0.85F, 0.03F, 0.01F), 0.65F);

    public MemoryThreadItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            setActive(stack, false, level);
            setPoints(stack, List.of());
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.hemomancy.memory_thread.clear")
                        .withStyle(ChatFormatting.GRAY), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (!canUseHematicTool(player) || !drainBlood(player, MemoryThreadRules.BLOOD_COST)) {
            return InteractionResultHolder.fail(stack);
        }
        boolean next = !isActive(stack);
        setActive(stack, next, level);
        if (next) {
            appendCurrentPosition(stack, player);
        }
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable(next
                    ? "item.hemomancy.memory_thread.start" : "item.hemomancy.memory_thread.stop")
                    .withStyle(ChatFormatting.DARK_RED), true);
            player.getCooldowns().addCooldown(this, MemoryThreadRules.USE_COOLDOWN_TICKS);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!isActive(stack) || level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        if (!level.dimension().location().toString().equals(dimension(stack))) {
            setActive(stack, false, level);
            return;
        }
        if (player.tickCount % 10 == 0 && selected) {
            appendCurrentPosition(stack, player);
        }
        if (selected && player.tickCount % 20 == 0 && level instanceof ServerLevel serverLevel) {
            renderRoute(serverLevel, readPoints(stack));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hemomancy.memory_thread.tooltip", readPoints(stack).size())
                .withStyle(ChatFormatting.DARK_RED));
    }

    private static void appendCurrentPosition(ItemStack stack, Player player) {
        List<Long> points = readPoints(stack);
        BlockPos pos = player.blockPosition();
        MemoryThreadRules.appendRoutePoint(points, MemoryThreadRules.packPosition(pos.getX(), pos.getY(), pos.getZ()));
        setPoints(stack, points);
    }

    private static void renderRoute(ServerLevel level, List<Long> points) {
        for (int i = Math.max(0, points.size() - 48); i < points.size(); i++) {
            long packed = points.get(i);
            Vec3 pos = Vec3.atCenterOf(new BlockPos(LastDeathMemory.unpackX(packed), LastDeathMemory.unpackY(packed), LastDeathMemory.unpackZ(packed)));
            level.sendParticles(THREAD_DUST, pos.x, pos.y + 0.05D, pos.z, 1, 0.03D, 0.01D, 0.03D, 0.0D);
        }
    }

    public static boolean isActive(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(TAG_ACTIVE);
    }

    public static String dimension(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(TAG_DIMENSION);
    }

    private static void setActive(ItemStack stack, boolean active, Level level) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(TAG_ACTIVE, active);
        tag.putString(TAG_DIMENSION, level.dimension().location().toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static List<Long> readPoints(ItemStack stack) {
        List<Long> points = new ArrayList<>();
        ListTag list = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getList(TAG_POINTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            points.add(list.getCompound(i).getLong(TAG_POINT));
        }
        return points;
    }

    private static void setPoints(ItemStack stack, List<Long> points) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag list = new ListTag();
        for (long point : points) {
            CompoundTag entry = new CompoundTag();
            entry.putLong(TAG_POINT, point);
            list.add(entry);
        }
        tag.put(TAG_POINTS, list);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
