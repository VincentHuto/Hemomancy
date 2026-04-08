package com.vincenthuto.hemomancy.common.capability.player.unstained;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class UnstainedProgressProvider implements ICapabilitySerializable<Tag> {

    public static final Capability<IUnstainedProgress> UNSTAINED_CAPA =
            CapabilityManager.get(new CapabilityToken<IUnstainedProgress>() {});

    public static IUnstainedProgress getProgress(Player player) {
        return player.getCapability(UNSTAINED_CAPA)
                .orElseThrow(IllegalStateException::new);
    }

    private LazyOptional<IUnstainedProgress> instance = LazyOptional.of(UnstainedProgress::new);


    @Override
    public void deserializeNBT(Tag nbt) {
        readNBT(instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return UNSTAINED_CAPA.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        return writeNBT(instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")));
    }

    private CompoundTag writeNBT(IUnstainedProgress inst) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("begunPurification", inst.hasBegunPurification());
        tag.putFloat("purity", inst.getPurity());
        tag.putBoolean("clarityUnlocked", inst.hasClarityUnlocked());
        tag.putFloat("clarity", inst.getClarity());
        return tag;
    }

    private void readNBT(IUnstainedProgress inst, Tag nbt) {
        if (nbt instanceof CompoundTag tag) {
            inst.setBegunPurification(tag.getBoolean("begunPurification"));
            inst.setPurity(tag.getFloat("purity"));
            inst.setClarityUnlocked(tag.getBoolean("clarityUnlocked"));
            inst.setClarity(tag.getFloat("clarity"));
        }
    }
}
