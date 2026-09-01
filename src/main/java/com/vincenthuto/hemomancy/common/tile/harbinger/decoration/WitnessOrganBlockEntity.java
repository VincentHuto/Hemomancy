package com.vincenthuto.hemomancy.common.tile.harbinger.decoration;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.WitnessOrganRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class WitnessOrganBlockEntity extends BlockEntity {
    private static final String TAG_SOUNDS = "WitnessSounds";
    private static final String TAG_SOUND = "Sound";
    private static final String TAG_POS = "Pos";
    private static final String TAG_VOLUME = "Volume";
    private static final String TAG_PITCH = "Pitch";
    private static final String TAG_PLAYING = "Playing";
    private static final String TAG_INDEX = "PlaybackIndex";
    private static final String TAG_DELAY = "PlaybackDelay";
    private static final int PLAYBACK_DELAY_TICKS = 6;

    private final List<WitnessOrganRules.SoundMemory> memories = new ArrayList<>();
    private boolean playing;
    private int playbackIndex;
    private int playbackDelay;

    public WitnessOrganBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.witness_organ.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WitnessOrganBlockEntity organ) {
        if (!organ.playing || organ.memories.isEmpty()) {
            return;
        }
        if (organ.playbackDelay-- > 0) {
            return;
        }
        organ.playMemory(organ.memories.get(organ.playbackIndex));
        organ.playbackIndex++;
        organ.playbackDelay = PLAYBACK_DELAY_TICKS;
        if (organ.playbackIndex >= organ.memories.size()) {
            organ.playbackIndex = 0;
            organ.playing = false;
        }
        organ.setChanged();
    }

    public void record(BlockPos notePos, int vanillaNoteId) {
        ResourceLocation soundId = BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.NOTE_BLOCK_HARP.value());
        WitnessOrganRules.append(memories, new WitnessOrganRules.SoundMemory(soundId.toString(),
                WitnessOrganRules.packPosition(notePos.getX(), notePos.getY(), notePos.getZ()),
                1.0F, WitnessOrganRules.notePitch(vanillaNoteId)));
        setChanged();
    }

    public void togglePlayback() {
        if (memories.isEmpty()) {
            playing = false;
            return;
        }
        playing = !playing;
        playbackIndex = 0;
        playbackDelay = 0;
        setChanged();
    }

    public void clear() {
        memories.clear();
        playing = false;
        playbackIndex = 0;
        playbackDelay = 0;
        setChanged();
    }

    public boolean isPlaying() {
        return playing;
    }

    public int getMemoryCount() {
        return memories.size();
    }

    private void playMemory(WitnessOrganRules.SoundMemory memory) {
        if (level == null) {
            return;
        }
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(memory.sound()));
        level.playSound(null, worldPosition, sound, SoundSource.RECORDS, memory.volume(), memory.pitch());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(TAG_SOUNDS, writeSounds());
        tag.putBoolean(TAG_PLAYING, playing);
        tag.putInt(TAG_INDEX, playbackIndex);
        tag.putInt(TAG_DELAY, playbackDelay);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        memories.clear();
        ListTag list = tag.getList(TAG_SOUNDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            memories.add(new WitnessOrganRules.SoundMemory(entry.getString(TAG_SOUND), entry.getLong(TAG_POS),
                    entry.getFloat(TAG_VOLUME), entry.getFloat(TAG_PITCH)));
        }
        playing = tag.getBoolean(TAG_PLAYING);
        playbackIndex = tag.getInt(TAG_INDEX);
        playbackDelay = tag.getInt(TAG_DELAY);
    }

    private ListTag writeSounds() {
        ListTag list = new ListTag();
        for (WitnessOrganRules.SoundMemory memory : memories) {
            CompoundTag entry = new CompoundTag();
            entry.putString(TAG_SOUND, memory.sound());
            entry.putLong(TAG_POS, memory.packedPosition());
            entry.putFloat(TAG_VOLUME, memory.volume());
            entry.putFloat(TAG_PITCH, memory.pitch());
            list.add(entry);
        }
        return list;
    }
}
