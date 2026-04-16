package com.vincenthuto.hemomancy.common.tile.puzzle;

import com.vincenthuto.hemomancy.common.block.puzzle.OfferingGateBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class BloodBasinBlockEntity extends BlockEntity {

    private static final double CAPACITY = 20.0;
    private static final double DRAIN_FRACTION = 0.10;
    private static final double OVERFILL_THRESHOLD = 1.25;
    private static final int SCAN_RADIUS = 12;
    private static final int REQUIRED_BASINS = 4;

    private double fillAmount = 0.0;

    public BloodBasinBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.blood_basin.get(), pos, state);
    }

    public void interact(Player player, Level level, BlockPos pos) {
        double drain = player.getMaxHealth() * DRAIN_FRACTION;
        if (player.getHealth() <= drain + 1.0f) {
            if (player instanceof ServerPlayer sp) {
                sp.displayClientMessage(
                    Component.translatable("message.hemomancy.blood_basin.insufficient_blood")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
                    true);
            }
            return;
        }

        player.hurt(level.damageSources().magic(), (float) drain);
        fillAmount += drain;
        setChanged();

        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0f, 0.8f);

        if (player instanceof ServerPlayer sp) {
            int pct = (int) Math.min(100, (fillAmount * 100.0) / CAPACITY);
            sp.displayClientMessage(
                Component.translatable("message.hemomancy.blood_basin.offered", pct)
                    .withStyle(ChatFormatting.DARK_RED),
                true);
        }

        checkCompletion(level, pos, player);
    }

    private void checkCompletion(Level level, BlockPos pos, Player player) {
        List<BloodBasinBlockEntity> basins = findNearbyBasins(level, pos);
        if (basins.size() < REQUIRED_BASINS) return;

        double totalFill = basins.stream().mapToDouble(b -> b.fillAmount).sum();
        double totalRequired = CAPACITY * basins.size();

        if (totalFill > totalRequired * OVERFILL_THRESHOLD) {
            triggerBacklash(player, level);
            basins.forEach(b -> { b.fillAmount = 0; b.setChanged(); });
            return;
        }

        if (basins.stream().allMatch(b -> b.fillAmount >= CAPACITY)) {
            openGates(level, pos);
            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 0.7f);
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(
                    Component.translatable("message.hemomancy.blood_basin.gates_open")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            }
        }
    }

    private List<BloodBasinBlockEntity> findNearbyBasins(Level level, BlockPos center) {
        List<BloodBasinBlockEntity> found = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                center.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
            if (level.getBlockEntity(p) instanceof BloodBasinBlockEntity basin) {
                found.add(basin);
            }
        }
        return found;
    }

    private void triggerBacklash(Player player, Level level) {
        float dmg = (float) (player.getMaxHealth() * 0.30);
        player.hurt(level.damageSources().magic(), dmg);
        player.knockback(2.0, 0, 0);
        level.playSound(null, player.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 1.0f, 0.5f);
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("message.hemomancy.hematic_backlash"));
        }
    }

    private void openGates(Level level, BlockPos basinPos) {
        for (BlockPos p : BlockPos.betweenClosed(
                basinPos.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                basinPos.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(BlockInit.offering_gate.get()) && !bs.getValue(OfferingGateBlock.OPEN)) {
                level.setBlock(p, bs.setValue(OfferingGateBlock.OPEN, true), 3);
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fillAmount = tag.getDouble("FillAmount");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("FillAmount", fillAmount);
    }

    public double getFillAmount() {
        return fillAmount;
    }

    public double getCapacity() {
        return CAPACITY;
    }
}
