package com.vincenthuto.hemomancy.common.tile.crafting;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.item.EnzymeItem;
import com.vincenthuto.hemomancy.common.item.RecycledEnzymeItem;
import com.vincenthuto.hemomancy.common.item.scar.ItemFungalScar;
import com.vincenthuto.hemomancy.common.item.scar.ItemImmatureFungalScar;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.MycelialCrucibleMenu;
import com.vincenthuto.hemomancy.common.recipe.FungalScarCultivationRecipe;
import com.vincenthuto.hemomancy.common.tile.IBloodTile;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Block entity for the Mycelial Crucible.
 *
 * <p><b>Phase 1 — Implantation</b><br>
 * When the center slot holds a valid ingredient (any item required by a matching
 * {@link FungalScarCultivationRecipe}) and the required enzymes are present:
 * <ol>
 *   <li>Flat blood cost is deducted at start.</li>
 *   <li>A countdown of {@code phase1Duration} ticks runs, draining
 *       {@link #BLOOD_COST_PER_TICK_PHASE1} per tick.</li>
 *   <li>On completion, center + enzymes are consumed and an
 *       {@link ItemImmatureFungalScar} is placed in the output slot.</li>
 * </ol>
 *
 * <p><b>Phase 2 — Maturation</b><br>
 * When the center slot holds an {@link ItemImmatureFungalScar} and enzymes are
 * present:
 * <ol>
 *   <li>Aligned enzymes advance {@code MatureProgress} on the item's NBT.</li>
 *   <li>Each enzyme batch triggers a short processing countdown.</li>
 *   <li>When {@code MatureProgress >= MatureThreshold}, the immature scar is
 *       consumed and the finished scar appears in the output.</li>
 * </ol>
 *
 * <p>Blood runs dry → cultivation pauses; progress is not lost.
 */
public class MycelialCrucibleBlockEntity extends BaseContainerBlockEntity implements IBloodTile {

    // ── Constants ─────────────────────────────────────────────────────────────

    static final String TAG_BLOOD_LEVEL        = "bloodLevel";
    private static final float MAX_BLOOD       = 8_000f;
    private static final float BLOOD_COST_PER_TICK_PHASE1 = 1.5f;
    private static final float BLOOD_COST_PER_TICK_PHASE2 = 1.5f;
    /** Minimum aligned enzyme power per item (used as fallback). */
    private static final float DEFAULT_ENZYME_POWER = 100f;

    // ── Slot layout ───────────────────────────────────────────────────────────
    // 0   = center (ingredient / immature scar)
    // 1-4 = enzyme feeding slots
    // 5   = output
    // 6   = blood flask / gourd input
    // 7   = empty flask output

    public static final int SLOT_CENTER        = 0;
    public static final int SLOT_ENZYME_START  = 1;
    public static final int SLOT_ENZYME_END    = 4;
    public static final int SLOT_OUTPUT        = 5;
    public static final int SLOT_BLOOD         = 6;
    public static final int SLOT_FLASK_OUTPUT  = 7;
    public static final int INVENTORY_SIZE     = 8;

    // ── State ─────────────────────────────────────────────────────────────────

    public NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    /** Remaining ticks in the current processing countdown (counts down to 0). */
    int craftingProgress;
    int craftingTotalTime;
    /**
     * 0 = idle, 1 = Phase 1 implantation, 2 = Phase 2 enzyme processing batch.
     */
    int mode;

    // ── ContainerData (synced to client) ──────────────────────────────────────

    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> craftingProgress;
                case 1 -> craftingTotalTime;
                case 2 -> mode;
                case 3 -> getMatureProgressForSync();
                case 4 -> getMatureThresholdForSync();
                default -> 0;
            };
        }

        @Override
        public int getCount() { return 5; }

        @Override
        public void set(int index, int val) {
            switch (index) {
                case 0 -> craftingProgress = val;
                case 1 -> craftingTotalTime = val;
                case 2 -> mode = val;
            }
        }
    };

    private int getMatureProgressForSync() {
        ItemStack center = inventory.get(SLOT_CENTER);
        if (center.getItem() instanceof ItemImmatureFungalScar scar) {
            return scar.getMatureProgress(center);
        }
        return 0;
    }

    private int getMatureThresholdForSync() {
        ItemStack center = inventory.get(SLOT_CENTER);
        if (center.getItem() instanceof ItemImmatureFungalScar scar) {
            return scar.getMatureThreshold(center);
        }
        return 1; // avoid div-by-zero on client
    }

    // ── Construction ──────────────────────────────────────────────────────────

    public MycelialCrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.mycelial_crucible.get(), pos, state);
    }

    // ── Capability access ─────────────────────────────────────────────────────

    @Nullable
    private IBloodVolume resolveVolume() {
        return HemoCapabilityAccess.getBloodVolume(this).orElse(null);
    }

    @Override
    public void onLoad() {
        IBloodVolume vol = resolveVolume();
        if (vol != null) {
            vol.setActive(true);
            vol.setMaxBloodVolume(MAX_BLOOD);
        }
    }

    // ── Ticking ───────────────────────────────────────────────────────────────

    public static void clientTick(Level level, BlockPos pos, BlockState state,
            MycelialCrucibleBlockEntity te) {
        // client-side particle effects can be added here later
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            MycelialCrucibleBlockEntity te) {
        te.processBloodSlot();

        boolean changed = false;

        if (te.craftingProgress > 0) {
            // Active countdown — consume blood each tick
            IBloodVolume vol = te.resolveVolume();
            float costPerTick = (te.mode == 1) ? BLOOD_COST_PER_TICK_PHASE1 : BLOOD_COST_PER_TICK_PHASE2;
            if (vol != null && vol.getBloodVolume() >= costPerTick) {
                vol.subtractBloodVolume(costPerTick);
                te.craftingProgress--;
                changed = true;

                // Spore particle burst
                if (te.craftingProgress % 10 == 0) {
                    HLParticleUtils.spawnPoof((ServerLevel) level, pos, ParticleTypes.MYCELIUM);
                }

                if (te.craftingProgress <= 0) {
                    HLParticleUtils.spawnPoof((ServerLevel) level, pos, ParticleTypes.REVERSE_PORTAL);
                    te.finishCrafting();
                    changed = true;
                }
            }
            // Not enough blood → pause (no reset)
        } else {
            // Try to start a new craft
            if (te.tryStartPhase1()) {
                changed = true;
            } else if (te.tryStartPhase2Batch()) {
                changed = true;
            } else {
                if (te.mode != 0) {
                    te.mode = 0;
                    changed = true;
                }
            }
        }

        if (changed) {
            te.sendUpdates();
            setChanged(level, pos, state);
        }
    }

    // ── Blood slot processing ─────────────────────────────────────────────────

    private void processBloodSlot() {
        ItemStack bloodStack = inventory.get(SLOT_BLOOD);
        if (bloodStack.isEmpty()) return;

        IBloodVolume vol = resolveVolume();
        if (vol == null || vol.isFull()) return;

        if (bloodStack.getItem() instanceof BloodyFlaskItem flask) {
            double amount    = flask.getAmount();
            double available = vol.getMaxBloodVolume() - vol.getBloodVolume();
            if (available > 0) {
                ItemStack outputStack = inventory.get(SLOT_FLASK_OUTPUT);
                if (outputStack.isEmpty()
                        || (outputStack.getItem() == HLItemInit.cured_clay_flask.get()
                                && outputStack.getCount() < outputStack.getMaxStackSize())) {
                    vol.addBloodVolume(Math.min(amount, available));
                    bloodStack.shrink(1);
                    if (outputStack.isEmpty()) {
                        inventory.set(SLOT_FLASK_OUTPUT, new ItemStack(HLItemInit.cured_clay_flask.get()));
                    } else {
                        outputStack.grow(1);
                    }
                    sendUpdates();
                }
            }
        } else if (bloodStack.getItem() instanceof BloodGourdItem) {
            IBloodVolume gourdVol = HemoCapabilityAccess.getBloodVolume(bloodStack).orElse(null);
            if (gourdVol != null && gourdVol.getBloodVolume() > 0) {
                double transfer = Math.min(gourdVol.getBloodVolume(),
                        vol.getMaxBloodVolume() - vol.getBloodVolume());
                transfer = Math.min(transfer, 10);
                if (transfer > 0) {
                    gourdVol.subtractBloodVolume(transfer);
                    vol.addBloodVolume(transfer);
                    sendUpdates();
                }
            }
        }
    }

    // ── Phase 1 ───────────────────────────────────────────────────────────────

    @Nullable
    private FungalScarCultivationRecipe findPhase1Recipe() {
        if (level == null) return null;
        ItemStack center = inventory.get(SLOT_CENTER);
        if (center.isEmpty()) return null;
        // Center must NOT already be an immature scar (that is Phase 2 input)
        if (center.getItem() instanceof ItemImmatureFungalScar) return null;
        // Output must be clear
        if (!inventory.get(SLOT_OUTPUT).isEmpty()) return null;

        for (var holder : level.getRecipeManager()
                .getAllRecipesFor(com.vincenthuto.hemomancy.common.init.RecipeInit.fungal_scar_cultivation_type.get())) {
            FungalScarCultivationRecipe recipe = holder.value();
            // The center slot must hold the ItemFungalScar that this recipe produces (finished scar)
            // — i.e. the player inserts the scar they want to grow and enzymes matching its tendency.
            // Check: center item is the finished scar described by this recipe, AND aligned enzyme present.
            if (center.getItem() == recipe.getResultItemStack().getItem()
                    && hasAlignedEnzyme(recipe.getTendency())) {
                return recipe;
            }
        }
        return null;
    }

    private boolean tryStartPhase1() {
        FungalScarCultivationRecipe recipe = findPhase1Recipe();
        if (recipe == null) return false;

        // Flat blood cost at start
        IBloodVolume vol = resolveVolume();
        if (vol == null || vol.getBloodVolume() < recipe.getBloodCostPhase1()) return false;
        vol.subtractBloodVolume(recipe.getBloodCostPhase1());

        craftingProgress = recipe.getPhase1Duration();
        craftingTotalTime = recipe.getPhase1Duration();
        mode = 1;
        // Store recipe tendency in the center slot NBT so we know what to produce
        storePendingRecipe(recipe);
        return true;
    }

    /** Stores the recipe id + tendency into the center stack's custom data. */
    private void storePendingRecipe(FungalScarCultivationRecipe recipe) {
        ItemStack center = inventory.get(SLOT_CENTER);
        if (center.isEmpty()) return;
        CompoundTag tag = center.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString("PendingCultivationRecipe", recipe.getId().toString());
        center.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // ── Phase 2 ───────────────────────────────────────────────────────────────

    private boolean tryStartPhase2Batch() {
        ItemStack center = inventory.get(SLOT_CENTER);
        if (center.isEmpty() || !(center.getItem() instanceof ItemImmatureFungalScar immatureScar))
            return false;
        if (!inventory.get(SLOT_OUTPUT).isEmpty()) return false;

        EnumBloodTendency scarTendency = ItemImmatureFungalScar.getTendency(center);
        if (scarTendency == null) return false;

        // Collect aligned enzymes
        int enzymeCount = 0;
        for (int i = SLOT_ENZYME_START; i <= SLOT_ENZYME_END; i++) {
            if (isAlignedEnzyme(inventory.get(i), scarTendency)) enzymeCount++;
        }
        if (enzymeCount == 0) return false;

        // Duration proportional to enzyme count
        int totalTime = 100 + enzymeCount * 60;
        craftingProgress = totalTime;
        craftingTotalTime = totalTime;
        mode = 2;
        return true;
    }

    // ── Finish crafting ───────────────────────────────────────────────────────

    private void finishCrafting() {
        if (mode == 1) {
            finishPhase1();
        } else if (mode == 2) {
            finishPhase2Batch();
        }
        craftingProgress = 0;
        craftingTotalTime = 0;
        mode = 0;
        sendUpdates();
    }

    private void finishPhase1() {
        if (level == null) return;
        // Consume all enzymes in enzyme slots
        for (int i = SLOT_ENZYME_START; i <= SLOT_ENZYME_END; i++) {
            inventory.get(i).shrink(1);
        }
        // Find the matching recipe from the center stack's stored recipe id
        ItemStack center = inventory.get(SLOT_CENTER);
        FungalScarCultivationRecipe recipe = null;
        if (!center.isEmpty()) {
            CompoundTag tag = center.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (tag.contains("PendingCultivationRecipe")) {
                String recipeId = tag.getString("PendingCultivationRecipe");
                for (var holder : level.getRecipeManager().getAllRecipesFor(
                        com.vincenthuto.hemomancy.common.init.RecipeInit.fungal_scar_cultivation_type.get())) {
                    if (holder.value().getId().toString().equals(recipeId)) {
                        recipe = holder.value();
                        break;
                    }
                }
            }
        }
        if (recipe == null) return;

        // Consume center item
        inventory.get(SLOT_CENTER).shrink(1);

        // Produce immature scar
        ItemStack immature = recipe.getImmatureResult();
        if (immature.getItem() instanceof ItemImmatureFungalScar immatureScarItem) {
            net.minecraft.resources.ResourceLocation targetId =
                    BuiltInRegistries.ITEM.getKey(
                            recipe.getResultItemStack().getItem());
            immatureScarItem.initThreshold(immature, recipe.getTendency(),
                    recipe.getMaturationThreshold(), targetId);
        }
        inventory.set(SLOT_OUTPUT, immature);
    }

    private void finishPhase2Batch() {
        ItemStack center = inventory.get(SLOT_CENTER);
        if (center.isEmpty() || !(center.getItem() instanceof ItemImmatureFungalScar immatureScar)) return;

        EnumBloodTendency scarTendency = ItemImmatureFungalScar.getTendency(center);
        if (scarTendency == null) return;
        float totalEnzymePower = 0f;

        for (int i = SLOT_ENZYME_START; i <= SLOT_ENZYME_END; i++) {
            ItemStack stack = inventory.get(i);
            if (isAlignedEnzyme(stack, scarTendency)) {
                totalEnzymePower += getEnzymePower(stack);
                stack.shrink(1);
            }
        }

        // Advance maturation progress
        int current = immatureScar.getMatureProgress(center);
        int next    = current + (int) totalEnzymePower;
        immatureScar.setMatureProgress(center, next);

        // Check if fully mature
        if (immatureScar.isMature(center)) {
            // Find the matching recipe and produce the finished scar
            finishPhase2Complete(center, immatureScar);
        }
    }

    private void finishPhase2Complete(ItemStack immatureStack,
            ItemImmatureFungalScar immatureScarItem) {
        if (level == null) return;
        CompoundTag tag = immatureStack.getOrDefault(DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        String targetScarId = tag.getString(ItemImmatureFungalScar.TAG_TARGET_SCAR_ID);

        FungalScarCultivationRecipe matchingRecipe = null;
        for (var holder : level.getRecipeManager().getAllRecipesFor(
                com.vincenthuto.hemomancy.common.init.RecipeInit.fungal_scar_cultivation_type.get())) {
            FungalScarCultivationRecipe r = holder.value();
            net.minecraft.resources.ResourceLocation resultId =
                    BuiltInRegistries.ITEM.getKey(
                            r.getResultItemStack().getItem());
            if (resultId != null && resultId.toString().equals(targetScarId)) {
                matchingRecipe = r;
                break;
            }
        }
        if (matchingRecipe == null) return;

        inventory.set(SLOT_CENTER, ItemStack.EMPTY);
        inventory.set(SLOT_OUTPUT, matchingRecipe.getResultItemStack().copy());
    }

    // ── Enzyme helpers ────────────────────────────────────────────────────────

    private boolean hasAlignedEnzyme(EnumBloodTendency tendency) {
        for (int i = SLOT_ENZYME_START; i <= SLOT_ENZYME_END; i++) {
            if (isAlignedEnzyme(inventory.get(i), tendency)) return true;
        }
        return false;
    }

    private static boolean isAlignedEnzyme(ItemStack stack, EnumBloodTendency tendency) {
        if (stack.getItem() instanceof EnzymeItem enzyme)
            return enzyme.getTend() == tendency;
        if (stack.getItem() instanceof RecycledEnzymeItem recycled)
            return recycled.getTend() == tendency;
        return false;
    }

    private static float getEnzymePower(ItemStack stack) {
        if (stack.getItem() instanceof EnzymeItem enzyme)
            return enzyme.getAmount();
        if (stack.getItem() instanceof RecycledEnzymeItem recycled)
            return recycled.getAmount();
        return DEFAULT_ENZYME_POWER;
    }

    // ── Inventory helpers (renderer) ──────────────────────────────────────────

    public ItemStack getCenterStack()    { return inventory.get(SLOT_CENTER); }

    // ── Container implementation ──────────────────────────────────────────────

    @Override public int getContainerSize()             { return INVENTORY_SIZE; }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : inventory) { if (!s.isEmpty()) return false; }
        return true;
    }

    @Override public ItemStack getItem(int slot)          { return inventory.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(inventory, slot, amount);
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack old = inventory.get(slot);
        boolean same = !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, old);
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        // Reset progress if inputs change during active crafting
        if (!same && (slot == SLOT_CENTER || (slot >= SLOT_ENZYME_START && slot <= SLOT_ENZYME_END))) {
            if (craftingProgress > 0) {
                craftingProgress  = 0;
                craftingTotalTime = 0;
                mode              = 0;
                setChanged();
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override public void clearContent()                              { inventory.clear(); }
    @Override protected NonNullList<ItemStack> getItems()             { return inventory; }
    @Override protected void setItems(NonNullList<ItemStack> items)   { inventory = items; }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv) {
        return new MycelialCrucibleMenu(id, playerInv, this, dataAccess);
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Mycelial Crucible");
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, inventory, registries);
        craftingProgress  = tag.getInt("CraftProgress");
        craftingTotalTime = tag.getInt("CraftTotalTime");
        mode              = tag.getInt("Mode");
        IBloodVolume vol  = resolveVolume();
        if (vol != null && tag.contains(TAG_BLOOD_LEVEL)) {
            vol.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, inventory, registries);
        tag.putInt("CraftProgress",  craftingProgress);
        tag.putInt("CraftTotalTime", craftingTotalTime);
        tag.putInt("Mode",           mode);
        IBloodVolume vol = resolveVolume();
        if (vol != null) tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, inventory, registries);
        tag.putInt("CraftProgress",  craftingProgress);
        tag.putInt("CraftTotalTime", craftingTotalTime);
        tag.putInt("Mode",           mode);
        IBloodVolume vol = resolveVolume();
        if (vol != null) tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, inventory, registries);
        craftingProgress  = tag.getInt("CraftProgress");
        craftingTotalTime = tag.getInt("CraftTotalTime");
        mode              = tag.getInt("Mode");
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt,
            HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        if (pkt.getTag() != null) {
            CompoundTag tag = pkt.getTag();
            IBloodVolume vol = resolveVolume();
            if (vol != null && tag.contains(TAG_BLOOD_LEVEL)) {
                vol.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
            }
        }
    }

    // ── Blood access ──────────────────────────────────────────────────────────

    @Nullable public IBloodVolume getBloodCapability() { return resolveVolume(); }

    public double getBloodVolume() {
        IBloodVolume vol = resolveVolume();
        return vol != null ? vol.getBloodVolume() : 0;
    }

    public double getMaxBloodVolume() {
        IBloodVolume vol = resolveVolume();
        return vol != null ? vol.getMaxBloodVolume() : 0;
    }

    @Override
    public void sendUpdates() {
        if (level == null) return;
        level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }
}
