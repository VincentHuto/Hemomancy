package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class WitnessOrganItem extends HematicMemoryToolItem {
    private static final String TAG_SOUNDS = "WitnessSounds";
    private static final String TAG_SOUND = "Sound";
    private static final String TAG_POS = "Pos";
    private static final String TAG_VOLUME = "Volume";
    private static final String TAG_PITCH = "Pitch";
    private static final String TAG_MODE = "Mode";

    public WitnessOrganItem(Properties properties) {
        super(properties, 3);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !canUseHematicTool(player)) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.NOTE_BLOCK)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            ItemStack stack = context.getItemInHand();
            List<WitnessOrganRules.SoundMemory> sounds = readSounds(stack);
            WitnessOrganRules.append(sounds, new WitnessOrganRules.SoundMemory(
                    BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.NOTE_BLOCK_HARP.value()).toString(),
                    WitnessOrganRules.packPosition(pos.getX(), pos.getY(), pos.getZ()), 1.0F, 1.0F));
            writeSounds(stack, sounds);
            player.getCooldowns().addCooldown(this, WitnessOrganRules.RECORD_COOLDOWN_TICKS);
            player.displayClientMessage(Component.translatable("item.hemomancy.witness_organ.recorded", sounds.size())
                    .withStyle(ChatFormatting.DARK_RED), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            toggleMode(stack);
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.hemomancy.witness_organ.mode", mode(stack).name())
                        .withStyle(ChatFormatting.GRAY), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (!canUseHematicTool(player) || !drainBlood(player, WitnessOrganRules.BLOOD_COST_PER_PLAYBACK)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            for (WitnessOrganRules.SoundMemory memory : readSounds(stack)) {
                BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.parse(memory.sound())).ifPresent(sound ->
                        level.playSound(null, new BlockPos(LastDeathMemory.unpackX(memory.packedPosition()),
                                        LastDeathMemory.unpackY(memory.packedPosition()),
                                        LastDeathMemory.unpackZ(memory.packedPosition())),
                                sound, SoundSource.RECORDS, memory.volume(), memory.pitch()));
            }
            player.getCooldowns().addCooldown(this, mode(stack).shouldStopAfterPass() ? 20 : 80);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hemomancy.witness_organ.tooltip", readSounds(stack).size(),
                mode(stack).name()).withStyle(ChatFormatting.DARK_RED));
    }

    private static WitnessOrganRules.Mode mode(ItemStack stack) {
        String value = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(TAG_MODE);
        return "LOOP".equals(value) ? WitnessOrganRules.Mode.LOOP : WitnessOrganRules.Mode.ONCE;
    }

    private static void toggleMode(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(TAG_MODE, mode(stack) == WitnessOrganRules.Mode.ONCE ? "LOOP" : "ONCE");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static List<WitnessOrganRules.SoundMemory> readSounds(ItemStack stack) {
        List<WitnessOrganRules.SoundMemory> sounds = new ArrayList<>();
        ListTag list = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getList(TAG_SOUNDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            sounds.add(new WitnessOrganRules.SoundMemory(entry.getString(TAG_SOUND), entry.getLong(TAG_POS),
                    entry.getFloat(TAG_VOLUME), entry.getFloat(TAG_PITCH)));
        }
        return sounds;
    }

    private static void writeSounds(ItemStack stack, List<WitnessOrganRules.SoundMemory> sounds) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag list = new ListTag();
        for (WitnessOrganRules.SoundMemory memory : sounds) {
            CompoundTag entry = new CompoundTag();
            entry.putString(TAG_SOUND, memory.sound());
            entry.putLong(TAG_POS, memory.packedPosition());
            entry.putFloat(TAG_VOLUME, memory.volume());
            entry.putFloat(TAG_PITCH, memory.pitch());
            list.add(entry);
        }
        tag.put(TAG_SOUNDS, list);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
