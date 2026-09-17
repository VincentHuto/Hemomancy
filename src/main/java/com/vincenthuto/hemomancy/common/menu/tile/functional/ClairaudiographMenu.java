package com.vincenthuto.hemomancy.common.menu.tile.functional;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.item.component.ClairaudiographRecording;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.*;
import com.vincenthuto.hemomancy.common.network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import java.util.*;
public class ClairaudiographMenu extends AbstractContainerMenu {
    public final ClairaudiographBlockEntity machine;
    private final Player owner;
    public List<ClairaudiographRecording> choices = List.of();
    public String source = "";
    public long version;
    private long catalogue = -1;
    public int progress, playing, looping;
    public ClairaudiographMenu(int id, Inventory inv, FriendlyByteBuf b) { this(id,inv,(ClairaudiographBlockEntity)inv.player.level().getBlockEntity(b.readBlockPos())); }
    public ClairaudiographMenu(int id, Inventory inv, ClairaudiographBlockEntity be) {
        super(ContainerInit.clairaudiograph.get(),id); machine = be; owner = inv.player;
        for(int i=0;i<2;i++) addSlot(new SlotItemHandler(be.inventory,i,10+i*28,26) {
            @Override public boolean mayPlace(ItemStack stack) { return machine.progress == 0 && super.mayPlace(stack); }
            @Override public boolean mayPickup(Player player) { return machine.progress == 0; }
            @Override public int getMaxStackSize() { return 1; }
        });
        for(int row=0;row<3;row++) for(int col=0;col<9;col++) addSlot(new Slot(inv,9+row*9+col,8+col*18,150+row*18));
        for(int col=0;col<9;col++) addSlot(new Slot(inv,col,8+col*18,208));
        addDataSlots(new ContainerData() {
            public int get(int i) { return switch(i) { case 0 -> be.progress; case 1 -> be.playing()?1:0; default -> be.loop?1:0; }; }
            public void set(int i,int value) { if(i==0) progress=value; else if(i==1) playing=value; else looping=value; }
            public int getCount() { return 3; }
        });
    }
    @Override public boolean stillValid(Player p) {
        return machine != null && !machine.isRemoved() && p.isAlive() && !p.isSpectator() && p.level()==machine.getLevel()
            && p.level().getBlockEntity(machine.getBlockPos())==machine && p.distanceToSqr(machine.getBlockPos().getCenter())<=64;
    }
    private void refresh() {
        if(owner.level().isClientSide) return;
        if(catalogue != ClairaudiographCatalogue.revision() || !source.equals(machine.source())) {
            catalogue = ClairaudiographCatalogue.revision(); source = machine.source(); version++;
            choices = ClairaudiographCatalogue.choices(source).stream().map(ClairaudiographCatalogue.Choice::recording).toList();
            if(owner instanceof ServerPlayer sp) PacketHandler.sendToPlayer(sp,new ClairaudiographChoicesPacket(containerId,version,source,choices));
        }
    }
    @Override public void broadcastChanges() { refresh(); super.broadcastChanges(); }
    public void action(Player p, long displayedVersion, int action, int index) {
        if(p.level().isClientSide || p.containerMenu!=this || !stillValid(p)) return;
        refresh();
        if(displayedVersion != version) return;
        if(action == 3) { machine.stopPlayback(); return; }
        if(action == 4) { machine.loop = !machine.loop; machine.changed(); return; }
        if(action == 2) {
            if(ClairaudiographCatalogue.allowed(machine.recording()) == null) p.displayClientMessage(net.minecraft.network.chat.Component.translatable("gui.hemomancy.clairaudiograph.unavailable"),true);
            else machine.startPlayback(false);
            return;
        }
        if(index<0 || index>=choices.size() || machine.progress>0 || machine.playing()) return;
        var choice=choices.get(index);
        if(!source.equals(machine.source()) || ClairaudiographCatalogue.allowed(choice)==null
            || !com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData.isStorableSample(machine.inventory.getStackInSlot(0))
            || com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData.entityType(machine.inventory.getStackInSlot(0))==null) return;
        if(action==1) machine.startCarve(choice);
        if(action==0 && p instanceof ServerPlayer sp) {
            if(!machine.allowPreview(p)) return;
            PacketHandler.sendToPlayer(sp,new ClairaudiographSoundPacket(p.level().dimension().location(),machine.getBlockPos(),0,false,true,choice.sound(),choice.pitch()));
        }
    }
    @Override public ItemStack quickMoveStack(Player p,int index) {
        if(!stillValid(p) || index<0 || index>=slots.size() || machine.progress>0) return ItemStack.EMPTY;
        var slot=slots.get(index); if(!slot.hasItem() || !slot.mayPickup(p)) return ItemStack.EMPTY;
        var stack=slot.getItem(); var copy=stack.copy();
        if(index<2 ? !moveItemStackTo(stack,2,slots.size(),true) : !moveItemStackTo(stack,0,2,false)) return ItemStack.EMPTY;
        if(stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged(); slot.onTake(p,stack); return copy;
    }
}
