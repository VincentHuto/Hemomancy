package com.vincenthuto.hemomancy.common.antecedent;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/** A view into the equipped charm; never a second persisted item inventory. */
public final class TalismanSocket extends ItemStackHandler {
    private final Player player;
    public TalismanSocket(Player player) { super(1); this.player=player; }
    private ItemStack charm() { return HemoCapabilityAccess.requireEquipment(player).getStackInSlot(5); }
    @Override public ItemStack getStackInSlot(int slot) { return charm().getItem() instanceof VasculariumCharmItem ? charm().getOrDefault(DataComponentInit.CHARM_TALISMAN.get(),net.minecraft.world.item.component.ItemContainerContents.EMPTY).copyOne() : ItemStack.EMPTY; }
    @Override public void setStackInSlot(int slot,ItemStack stack) {
        var charm=charm().copy();
        if(!(charm.getItem() instanceof VasculariumCharmItem)) return;
        if(stack.isEmpty()) charm.remove(DataComponentInit.CHARM_TALISMAN.get());
        else charm.set(DataComponentInit.CHARM_TALISMAN.get(),net.minecraft.world.item.component.ItemContainerContents.fromItems(java.util.List.of(stack.copyWithCount(1))));
        HemoCapabilityAccess.requireEquipment(player).setStackInSlot(5,charm);
    }
    @Override public int getSlotLimit(int slot) { return 1; }
    @Override public boolean isItemValid(int slot,ItemStack stack) { return charm().getItem() instanceof VasculariumCharmItem && ListeningScarItem.awake(stack); }
    @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate) {
        if(stack.isEmpty() || !getStackInSlot(0).isEmpty() || !isItemValid(0,stack)) return stack;
        if(!simulate) setStackInSlot(0,stack.copyWithCount(1));
        return stack.getCount()==1?ItemStack.EMPTY:stack.copyWithCount(stack.getCount()-1);
    }
    @Override public ItemStack extractItem(int slot,int amount,boolean simulate) {
        if(amount<=0) return ItemStack.EMPTY;
        var result=getStackInSlot(0).copy();
        if(!simulate) setStackInSlot(0,ItemStack.EMPTY);
        return result;
    }
}
