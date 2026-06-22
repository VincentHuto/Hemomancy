package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public final class BloodContainerTransfer {

    private BloodContainerTransfer() {
    }

    public static boolean fillReservoirFromSlot(
            Container container, IBloodReservoir reservoir, IBloodContainerSlotAccess slots) {
        return fillReservoirFromSlot(
                container,
                reservoir == null ? null : reservoir.getBloodCapability(),
                slots.getBloodContainerInputSlot(),
                slots.getEmptyBloodContainerOutputSlot(),
                slots.acceptsBloodGourdInput(),
                slots.getBloodGourdInputTransferRate());
    }

    private static boolean fillReservoirFromSlot(Container container, @Nullable IBloodVolume reservoir,
                                                 int filledContainerSlot, int emptyContainerOutputSlot,
                                                 boolean acceptBloodGourds, double gourdTransferRate) {
        if (reservoir == null || reservoir.isFull()) return false;

        ItemStack bloodStack = container.getItem(filledContainerSlot);
        if (bloodStack.isEmpty()) return false;

        if (bloodStack.getItem() instanceof BloodyFlaskItem flask) {
            double amount = flask.getAmount();
            if (!canAcceptFilledContainer(
                    reservoir.getBloodVolume(), reservoir.getMaxBloodVolume(), amount)) {
                return false;
            }

            ItemStack outputStack = container.getItem(emptyContainerOutputSlot);
            if (!canAcceptEmptyFlask(outputStack)) return false;

            bloodStack.shrink(1);
            reservoir.addBloodVolume(amount);
            addEmptyFlask(container, emptyContainerOutputSlot);
            return true;
        }

        if (acceptBloodGourds && bloodStack.getItem() instanceof BloodGourdItem) {
            IBloodVolume gourdVolume = HemoCapabilityAccess.getBloodVolume(bloodStack).orElse(null);
            if (gourdVolume == null) return false;

            double transfer = calculateGourdTransfer(
                    gourdVolume.getBloodVolume(),
                    reservoir.getBloodVolume(),
                    reservoir.getMaxBloodVolume(),
                    gourdTransferRate);
            if (transfer <= 0) return false;

            gourdVolume.subtractBloodVolume(transfer);
            reservoir.addBloodVolume(transfer);
            return true;
        }

        return false;
    }

    public static boolean drainReservoirIntoGourdSlot(Container container, IBloodReservoir reservoir,
                                                      int bloodGourdSlot, double transferRate) {
        if (reservoir == null) return false;
        IBloodVolume reservoirVolume = reservoir.getBloodCapability();
        if (reservoirVolume == null) return false;

        ItemStack gourdStack = container.getItem(bloodGourdSlot);
        if (!(gourdStack.getItem() instanceof BloodGourdItem)) return false;

        IBloodVolume gourdVolume = HemoCapabilityAccess.getBloodVolume(gourdStack).orElse(null);
        if (gourdVolume == null) return false;

        double gourdMax = ((BloodGourdItem) gourdStack.getItem()).getMaxBlood();
        if (gourdMax > 0) {
            gourdVolume.setMaxBloodVolume(gourdMax);
            if (gourdVolume.getBloodVolume() > gourdMax) {
                gourdVolume.setBloodVolume(gourdMax);
            }
        }

        double transfer = calculateGourdTransfer(
                reservoirVolume.getBloodVolume(),
                gourdVolume.getBloodVolume(),
                gourdMax > 0 ? gourdMax : gourdVolume.getMaxBloodVolume(),
                transferRate);
        if (transfer <= 0) return false;

        reservoirVolume.drain(transfer);
        gourdVolume.fill(transfer);
        reservoir.sendUpdates();
        return true;
    }

    public static boolean isFilledBloodContainer(ItemStack stack) {
        return stack.getItem() instanceof BloodyFlaskItem;
    }

    public static boolean isBloodGourd(ItemStack stack) {
        return stack.getItem() instanceof BloodGourdItem;
    }

    public static boolean canAcceptFilledContainer(double currentVolume, double maxVolume, double containerAmount) {
        return containerAmount > 0 && maxVolume - currentVolume >= containerAmount;
    }

    public static double calculateGourdTransfer(double sourceVolume, double targetVolume, double targetMaxVolume, double transferRate) {
        if (sourceVolume <= 0 || transferRate <= 0) return 0;
        double missing = targetMaxVolume - targetVolume;
        if (missing <= 0) return 0;
        return Math.min(Math.min(sourceVolume, missing), transferRate);
    }

    private static boolean canAcceptEmptyFlask(ItemStack stack) {
        return stack.isEmpty()
                || (stack.getItem() == HLItemInit.cured_clay_flask.get()
                && stack.getCount() < stack.getMaxStackSize());
    }

    private static void addEmptyFlask(Container container, int outputSlot) {
        ItemStack outputStack = container.getItem(outputSlot);
        if (outputStack.isEmpty()) {
            container.setItem(outputSlot, new ItemStack(HLItemInit.cured_clay_flask.get()));
        } else {
            outputStack.grow(1);
        }
    }
}
