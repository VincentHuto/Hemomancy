package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import net.minecraft.world.Container;

import javax.annotation.Nullable;

public interface IBloodReservoirContainer extends IBloodTile, Container {

    @Nullable
    IBloodVolume getBloodCapability();

    int getBloodContainerInputSlot();

    int getEmptyBloodContainerOutputSlot();

    default boolean acceptsBloodGourdInput() {
        return false;
    }

    default double getBloodGourdInputTransferRate() {
        return 10D;
    }

    default boolean processBloodContainerInputSlot() {
        return BloodContainerTransfer.fillReservoirFromSlot(
                this,
                getBloodCapability(),
                getBloodContainerInputSlot(),
                getEmptyBloodContainerOutputSlot(),
                acceptsBloodGourdInput(),
                getBloodGourdInputTransferRate());
    }

    default double getBloodVolume() {
        IBloodVolume volume = getBloodCapability();
        return volume != null ? volume.getBloodVolume() : 0;
    }

    default double getMaxBloodVolume() {
        IBloodVolume volume = getBloodCapability();
        return volume != null ? volume.getMaxBloodVolume() : 0;
    }
}
