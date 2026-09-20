package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.component.ClairaudiographRecording;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.ClairaudiographBlock;
import com.vincenthuto.hemomancy.common.menu.tile.functional.ClairaudiographMenu;
import com.vincenthuto.hemomancy.common.network.ClairaudiographSoundPacket;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClairaudiographBlockEntity extends BlockEntity implements MenuProvider {
    private static long nextToken;
    private long startedAt;
    private final com.vincenthuto.hemomancy.common.antecedent.AntecedentPlayback observations = new com.vincenthuto.hemomancy.common.antecedent.AntecedentPlayback();
    public com.vincenthuto.hemomancy.common.antecedent.AncientRecordings.Program program() { return com.vincenthuto.hemomancy.common.antecedent.AncientRecordings.get(inventory.getStackInSlot(1)); }
    public int elapsed() { return level == null ? 0 : (int)(level.getGameTime()-startedAt); }
    public boolean playable() { return program()!=null || ClairaudiographCatalogue.allowed(recording())!=null; }

    private final java.util.Map<java.util.UUID, Long> previews = new java.util.HashMap<>();
    public boolean allowPreview(Player player) {
        long now = level.getGameTime();
        previews.values().removeIf(time -> now - time >= 20);
        if (previews.putIfAbsent(player.getUUID(), now) != null) return false;
        return true;
    }
    private boolean transaction, initialized, powered, playing, redstoneLoop;
    public boolean loop;
    public int progress;
    private int remaining;
    private long token, catalogueRevision;
    private ClairaudiographRecording selected;
    private ItemStack originalSample = ItemStack.EMPTY;
    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 ? BloodSampleData.isStorableSample(stack) : stack.is(ItemInit.ambergris_cylinder.get());
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return progress > 0 ? stack : super.insertItem(slot, stack, simulate);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return progress > 0 ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
        }
        @Override protected void onContentsChanged(int slot) {
            if(!transaction && level != null && !level.isClientSide) { cancelCarve(); if(slot == 1) stopPlayback(); }
            changed();
        }
    };
    public ClairaudiographBlockEntity(BlockPos pos, BlockState state) { super(BlockEntityInit.clairaudiograph.get(), pos, state); }
    public String source() { return BloodSampleData.rawSource(inventory.getStackInSlot(0)); }
    public ClairaudiographRecording recording() {
        var cylinder = inventory.getStackInSlot(1);
        return cylinder.is(ItemInit.ambergris_cylinder.get()) && cylinder.getCount() == 1
            ? cylinder.get(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get()) : null;
    }
    public boolean playing() { return playing; }
    public boolean startCarve(ClairaudiographRecording choice) {
        if(level == null || level.isClientSide || progress > 0 || playing || !validCarve(choice)) return false;
        selected = choice; originalSample = inventory.getStackInSlot(0).copy(); catalogueRevision = ClairaudiographCatalogue.revision(); progress = 1; changed(); return true;
    }
    private boolean validCarve(ClairaudiographRecording choice) {
        var sample = inventory.getStackInSlot(0); var cylinder = inventory.getStackInSlot(1);
        return choice != null && sample.getCount() == 1 && BloodSampleData.isStorableSample(sample)
            && BloodSampleData.entityType(sample) != null && source().equals(choice.source())
            && cylinder.is(ItemInit.ambergris_cylinder.get()) && cylinder.getCount() == 1
            && !cylinder.has(DataComponentInit.ANCIENT_RECORDING.get()) && !cylinder.has(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get()) && ClairaudiographCatalogue.allowed(choice) != null;
    }
    public void cancelCarve() { progress = 0; selected = null; originalSample = ItemStack.EMPTY; }
    public void startPlayback(boolean redstone) {
        if(playing || progress > 0) return;
        var choice = ClairaudiographCatalogue.allowed(recording());
        var program=program();
        if(choice == null && program == null) return;
        playing = true; redstoneLoop = redstone; remaining = program == null ? choice.interval() : program.duration(); token = ++nextToken;
        startedAt=level.getGameTime(); observations.clear();
        sendSound(false); changed();
    }
    public void stopPlayback() { observations.clear(); sendSound(true); playing = false; remaining = 0; redstoneLoop = false; changed(); }
    private void sendSound(boolean stop) {
        if(!(level instanceof ServerLevel server)) return;
        var recording = recording();
        var packet = new ClairaudiographSoundPacket(server.dimension().location(), worldPosition, token, stop, false,
            program()!=null ? program().sound() : recording == null ? "" : recording.sound(), recording == null ? 1 : recording.pitch(), program()==null ? "" : program().id(), Math.max(0,elapsed()));
        // Stops reach former listeners too; clients independently discard unloaded/out-of-range owners.
        if(stop) PacketDistributor.sendToPlayersInDimension(server, packet);
        else PacketDistributor.sendToPlayersNear(server, null, worldPosition.getX()+.5, worldPosition.getY()+.5, worldPosition.getZ()+.5, 32, packet);
    }
    public static void tick(Level level, BlockPos pos, BlockState state, ClairaudiographBlockEntity be) {
        if(level.isClientSide) { if(be.progress > 0) be.progress = Math.min(80, be.progress + 1); return; }
        var specimen = be.inventory.getStackInSlot(0);
        if (be.progress == 0 && specimen.getItem() instanceof com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem
                && com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.is(specimen))
            be.inventory.setStackInSlot(0,com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.migrate(specimen));
        boolean power = level.hasNeighborSignal(pos);
        if(!be.initialized) { be.initialized = true; be.powered = power; }
        else if(power != be.powered) {
            be.powered = power;
            if(power) be.startPlayback(true);
            else if(be.playing && be.loop && be.redstoneLoop) be.stopPlayback();
        }
        if(state.getValue(ClairaudiographBlock.POWERED) != power) level.setBlock(pos, state.setValue(ClairaudiographBlock.POWERED, power), 3);
        if(be.progress > 0) {
            if(be.catalogueRevision != ClairaudiographCatalogue.revision() || !be.validCarve(be.selected)
                    || !ItemStack.matches(be.originalSample, be.inventory.getStackInSlot(0))) { be.cancelCarve(); be.changed(); }
            else if(++be.progress > 80) {
                var cylinder = be.inventory.getStackInSlot(1).copy();
                cylinder.set(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get(), be.selected);
                var empty = BloodSampleData.emptyVessel(be.inventory.getStackInSlot(0));
                be.transaction = true;
                be.inventory.setStackInSlot(0, empty); be.inventory.setStackInSlot(1, cylinder);
                be.transaction = false; be.cancelCarve(); be.changed();
            }
        }
        if(be.playing) {
            var choice = ClairaudiographCatalogue.allowed(be.recording());
            var program=be.program();
            if(choice == null && program == null) be.stopPlayback();
            else {
                be.observations.tick(be);
                if(be.elapsed()%20==0) be.sendSound(false);
                if(--be.remaining <= 0) {
                    if(be.loop && (!be.redstoneLoop || power)) {
                        be.remaining=program==null?choice.interval():program.duration(); be.token=++nextToken;
                        be.startedAt=level.getGameTime(); be.observations.clear(); be.sendSound(false); be.changed();
                    } else be.stopPlayback();
                }
            }
        }
        boolean active = be.playing || be.progress > 0;
        var current = level.getBlockState(pos);
        if(current.getValue(ClairaudiographBlock.ACTIVE) != active) level.setBlock(pos, current.setValue(ClairaudiographBlock.ACTIVE, active), 3);
    }
    public void changed() {
        setChanged();
        if(level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }
    @Override public void onChunkUnloaded() { cancelCarve(); stopPlayback(); initialized = false; super.onChunkUnloaded(); }
    @Override public void setRemoved() { if(level != null && !level.isClientSide) { cancelCarve(); stopPlayback(); } super.setRemoved(); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries); tag.put("inventory", inventory.serializeNBT(registries)); tag.putBoolean("loop", loop);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries); transaction = true; inventory.deserializeNBT(registries, tag.getCompound("inventory")); transaction = false;
        loop = tag.getBoolean("loop"); cancelCarve(); playing = false; initialized = false;
        if(level != null && level.isClientSide) { progress = tag.getInt("progress"); playing = tag.getBoolean("playing"); startedAt=tag.getLong("startedAt"); }
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag = saveWithoutMetadata(registries); tag.putInt("progress", progress); tag.putBoolean("playing", playing); tag.putLong("startedAt",startedAt); return tag;
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public Component getDisplayName() { return Component.translatable("block.hemomancy.clairaudiograph"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new ClairaudiographMenu(id, inventory, this); }
}
