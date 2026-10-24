package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.tile.HemoContainerBlockEntity;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumBalance;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumCurseRules;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumProvenance;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.item.harbinger.EnzymeItem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.EnzymaticScriptoriumMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeableStation;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class EnzymaticScriptoriumBlockEntity extends HemoContainerBlockEntity
        implements com.vincenthuto.hemomancy.common.tile.IBloodReservoir, UpgradeableStation {
    public static final double BLOOD_CAPACITY = 2000;
    public static final int ITEM = 8;
    public static final int LAPIS = 9;
    public static final int SHARD = 10;
    /** Blood an inscription draws from the tank, by row. */
    private static final int[] INSCRIPTION_BLOOD = {100, 200, 300};
    /** Blood denaturing draws from the tank. */
    public static final int DENATURE_BLOOD = 300;
    private NonNullList<ItemStack> items = NonNullList.withSize(11, ItemStack.EMPTY);
    private final int[] selected = new int[8];
    private boolean riteLocked;
    private UUID machineIdentity = UUID.randomUUID();
    private UUID lastUpgradeRite;

    public EnzymaticScriptoriumBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.enzymatic_scriptorium.get(), pos, state);
    }

    @Override public com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume getBloodCapability() {
        var volume = com.vincenthuto.hemomancy.common.tile.IBloodReservoir.super.getBloodCapability();
        volume.setActive(true);
        volume.setMaxBloodVolume(BLOOD_CAPACITY);
        return volume;
    }

    @Override public void onLoad() {
        super.onLoad();
        getBloodCapability();
    }

    public int selected(int index) { return selected[index]; }
    public int selectedCount() {
        int count = 0;
        for (int value : selected) count += value;
        return count;
    }

    public boolean select(int index) {
        if (riteLocked || index < 0 || index >= 8) return false;
        int stored = items.get(index).getCount();
        selected[index] = selected[index] >= stored || selectedCount() >= 3 ? 0 : selected[index] + 1;
        BlockEntitySync.markAndSync(this);
        return true;
    }

    public void consumeEnzymes(int[] costs) {
        for (int i = 0; i < 8; i++) {
            items.get(i).shrink(costs[i]);
            selected[i] = 0;
        }
        BlockEntitySync.markAndSync(this);
    }

    public static int inscriptionBlood(int row) {
        return INSCRIPTION_BLOOD[row];
    }

    /**
     * Inscribes the offer the actor's menu rolled for this row: the item takes the offer's enchantments, a strained
     * inscription may curse it, and the station spends its blood, lapis and the offer's enzymes while the actor pays
     * the row in levels. The actor's practice records the enchant, and a targeted one.
     *
     * @return whether it took; a refused inscription spends nothing
     */
    public boolean inscribe(Player actor, int row, int cost, List<EnchantmentInstance> offer, int[] enzymeCosts,
            boolean targeted) {
        ItemStack input = getItem(ITEM);
        ItemStack lapis = getItem(LAPIS);
        var blood = getBloodCapability();
        if (cost <= 0 || offer == null || offer.isEmpty() || !hasEnzymes(enzymeCosts)
                || input.isEmpty() || !input.isEnchantable() || lapis.getCount() < row + 1
                || (!actor.getAbilities().instabuild && actor.experienceLevel < cost)
                || blood == null || blood.getBloodVolume() < INSCRIPTION_BLOOD[row]) return false;
        int atCap = 0;
        int excess = 0;
        for (EnchantmentInstance entry : offer) {
            input.enchant(entry.enchantment, entry.level);
            int natural = entry.enchantment.value().getMaxLevel();
            if (entry.level >= natural) atCap++;
            excess += Math.max(0, entry.level - natural);
        }
        int usedEnzymes = 0;
        for (int i = 0; i < 8; i++) usedEnzymes += enzymeCosts[i];
        int strain = ScriptoriumBalance.totalStrain(usedEnzymes, offer.size(), atCap, excess);
        String curse = "";
        int severity = 0;
        if (actor.getRandom().nextInt(100) < strain) {
            curse = ScriptoriumCurseRules.applicableCurse(input);
            severity = curse.isBlank() ? 0 : ScriptoriumCurseRules.severity(excess);
        }
        input.set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(), new ScriptoriumProvenance(curse, severity, excess));
        blood.drain(INSCRIPTION_BLOOD[row]);
        lapis.shrink(row + 1);
        consumeEnzymes(enzymeCosts);
        actor.onEnchantmentPerformed(input, row + 1);
        if (actor instanceof ServerPlayer serverActor) {
            var stationProgress = HemoCapabilityAccess.stationUpgrades(serverActor);
            stationProgress.recordUse(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT);
            // A package with a target raised one enchantment above its roll (mode 1 or 2).
            if (targeted) stationProgress.recordUse(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT_TARGETED);
        }
        BlockEntitySync.markAndSync(this);
        return true;
    }

    /**
     * Denatures a cursed red item: every enchantment but its curses goes, and its provenance comes clean. Needs
     * Degree 3, a blood crystal shard and {@value #DENATURE_BLOOD} mL in the tank.
     *
     * @return whether it took; a refused denaturing spends nothing
     */
    public boolean denature(Player actor) {
        ItemStack input = getItem(ITEM);
        ScriptoriumProvenance provenance = input.get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        ItemStack shard = getItem(SHARD);
        var blood = getBloodCapability();
        if (provenance == null || provenance.curse().isBlank() || shard.isEmpty()
                || blood == null || blood.getBloodVolume() < DENATURE_BLOOD
                || !ProgressionGates.hasDegree(actor, 3)) return false;
        EnchantmentHelper.updateEnchantments(input, mutable ->
                mutable.removeIf(enchantment -> !enchantment.is(EnchantmentTags.CURSE)));
        input.set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(), ScriptoriumProvenance.CLEAN);
        blood.drain(DENATURE_BLOOD);
        shard.shrink(1);
        BlockEntitySync.markAndSync(this);
        return true;
    }

    private boolean hasEnzymes(int[] costs) {
        for (int i = 0; i < 8; i++) if (items.get(i).getCount() < costs[i]) return false;
        return true;
    }

    @Override public boolean isRiteLocked() { return riteLocked; }
    // A held Scriptorium's blood is part of its upgrade snapshot, so it must not move mid-rite.
    @Override public boolean canReceiveBlood() { return !riteLocked; }
    @Override public boolean canProvideBlood() { return !riteLocked; }
    @Override public void setRiteLocked(boolean value) { riteLocked = value; BlockEntitySync.markAndSync(this); }

    @Override public UpgradeStation upgradeStation() { return UpgradeStation.SCRIPTORIUM; }
    @Override public UUID machineIdentity() { return machineIdentity; }
    @Override public boolean readyForUpgradeRite(ServerLevel level, Direction riteForward) { return !riteLocked; }

    @Override public CompoundTag upgradeSnapshot(HolderLookup.Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        tag.remove("RiteLocked");
        tag.remove("LastUpgradeRite");
        tag.remove("Selected");
        tag.remove("neoforge:attachments");
        // Blood lives in the attachment; keep it in the snapshot under its old key so a mid-rite change still aborts.
        tag.putDouble("BloodVolume", getBloodVolume());
        return tag;
    }

    @Override public boolean wasUpgradedBy(UUID riteId) { return riteId != null && riteId.equals(lastUpgradeRite); }
    @Override public void markUpgradedBy(UUID riteId) { lastUpgradeRite = riteId; BlockEntitySync.markAndSync(this); }

    @Override public void setItem(int slot, ItemStack stack) {
        if (riteLocked) return;
        if (slot < 8) {
            if (!stack.isEmpty() && (!(stack.getItem() instanceof EnzymeItem enzyme)
                    || enzyme.getTend() != EnumBloodTendency.values()[slot])) return;
            if (stack.getCount() > 64) stack.setCount(64);
            selected[slot] = Math.min(selected[slot], stack.getCount());
        }
        items.set(slot, stack);
        BlockEntitySync.markAndSync(this);
    }

    @Override public ItemStack removeItem(int slot, int amount) {
        if (riteLocked) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (slot < 8) selected[slot] = Math.min(selected[slot], items.get(slot).getCount());
        BlockEntitySync.markAndSync(this);
        return result;
    }

    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (riteLocked) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.takeItem(items, slot);
        if (slot < 8) selected[slot] = 0;
        BlockEntitySync.markAndSync(this);
        return result;
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.hemomancy.enzymatic_scriptorium"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new EnzymaticScriptoriumMenu(id, inventory, this);
    }
    @Override public int getContainerSize() { return 11; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public boolean stillValid(Player player) {
        return !isRemoved() && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5,
                worldPosition.getZ() + .5) <= 64;
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(11, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        int[] saved = tag.getIntArray("Selected");
        for (int i = 0; i < 8; i++) selected[i] = i < saved.length ? Math.clamp(saved[i], 0, Math.min(3, items.get(i).getCount())) : 0;
        riteLocked = tag.getBoolean("RiteLocked");
        if (tag.hasUUID("MachineIdentity")) machineIdentity = tag.getUUID("MachineIdentity");
        lastUpgradeRite = tag.hasUUID("LastUpgradeRite") ? tag.getUUID("LastUpgradeRite") : null;
        // Saves before the attachment became the only store still carry their own copy.
        if (tag.contains("BloodVolume"))
            getBloodCapability().setBloodVolume(Math.clamp(tag.getDouble("BloodVolume"), 0, BLOOD_CAPACITY));
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putIntArray("Selected", selected);
        tag.putBoolean("RiteLocked", riteLocked);
        tag.putUUID("MachineIdentity", machineIdentity);
        if (lastUpgradeRite != null) tag.putUUID("LastUpgradeRite", lastUpgradeRite);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        if (packet.getTag() != null) loadAdditional(packet.getTag(), registries);
    }
}
