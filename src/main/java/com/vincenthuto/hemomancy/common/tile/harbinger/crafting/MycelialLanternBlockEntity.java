package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.world.farm.BloodFedGrowthRules;
import com.vincenthuto.hemomancy.common.world.farm.ClochePlanting;
import net.minecraft.server.level.ServerLevel;

import com.vincenthuto.hemomancy.common.tile.HemoContainerBlockEntity;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.MycelialLanternMenu;
import com.vincenthuto.hemomancy.common.recipe.EnzymeFruitingRecipe;
import com.vincenthuto.hemomancy.common.tile.BloodContainerTransfer;
import com.vincenthuto.hemomancy.common.tile.IBloodContainerSlotAccess;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.shared.IMultiBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

public class MycelialLanternBlockEntity extends HemoContainerBlockEntity implements IBloodReservoir, IBloodContainerSlotAccess, IMultiBlockEntity, WorldlyContainer {

    public static final String TAG_BLOOD_LEVEL = "bloodLevel";
    /** A working buffer (core-loop Track 3): eight enzymes' worth at 150 mL each (CB-13). */
    private static final float MAX_BLOOD = 1_200F;

    public static final int SLOT_CULTURE = MycelialLanternAutomationRules.SLOT_CULTURE;
    public static final int SLOT_BLOOD = MycelialLanternAutomationRules.SLOT_BLOOD;
    public static final int SLOT_OUTPUT = MycelialLanternAutomationRules.SLOT_OUTPUT;
    public static final int SLOT_EMPTY_CONTAINER = MycelialLanternAutomationRules.SLOT_EMPTY_CONTAINER;
    public static final int INVENTORY_SIZE = 4;

    private NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int craftingProgress;
    private int craftingTotalTime;
    /** The cloche's planted crop (core-loop Track 7): its stage and the ticks since it last grew. */
    private int plantStage;
    private int plantTicks;

    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> craftingProgress;
                case 1 -> craftingTotalTime;
                case 2 -> (int) getBloodVolume();
                case 3 -> (int) getMaxBloodVolume();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> craftingProgress = value;
                case 1 -> craftingTotalTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public MycelialLanternBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.mycelial_lantern.get(), pos, state);
    }

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

    public static void clientTick(Level level, BlockPos pos, BlockState state, MycelialLanternBlockEntity te) {
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MycelialLanternBlockEntity te) {
        boolean syncChanged = te.processBloodSlot();
        boolean worldChanged = syncChanged;
        EnzymeFruitingRecipe recipe = te.findRecipe();

        if (recipe == null) {
            if (te.craftingProgress != 0 || te.craftingTotalTime != 0) {
                te.craftingProgress = 0;
                te.craftingTotalTime = 0;
                worldChanged = true;
            }
            if (level instanceof ServerLevel server && te.tendPlant(server)) {
                worldChanged = true;
                syncChanged = true;
            }
        } else {
            if (te.craftingTotalTime != recipe.getDuration()) {
                te.craftingTotalTime = recipe.getDuration();
                worldChanged = true;
            }
            if (te.canWork(recipe)) {
                te.resolveVolume().subtractBloodVolume(recipe.getBloodPerTick());
                te.craftingProgress++;
                worldChanged = true;
                if (te.craftingProgress >= recipe.getDuration()) {
                    te.finishRecipe(recipe);
                    te.craftingProgress = 0;
                    syncChanged = true;
                }
            }
        }

        if (worldChanged) {
            setChanged(level, pos, state);
        }
        if (syncChanged) {
            te.sendUpdates();
        }
    }

    /**
     * The cloche (core-loop Track 7): seeds in the culture slot grow under the bell at the top level of fed ground, one
     * stage every {@link BloodFedGrowthRules#CLOCHE_STAGE_TICKS} that the buffer can pay
     * {@link BloodFedGrowthRules#CLOCHE_STAGE_ML} of fresh blood for. A ripe crop goes to the output and is sown again.
     * Returns whether anything a client sees changed.
     */
    private boolean tendPlant(ServerLevel server) {
        BlockState seedling = ClochePlanting.seedling(inventory.get(SLOT_CULTURE));
        if (seedling == null) {
            boolean had = plantStage != 0 || plantTicks != 0;
            plantStage = 0;
            plantTicks = 0;
            return had;
        }
        if (!BloodFedGrowthRules.clocheStageDue(++plantTicks)) return false;
        IBloodVolume vol = resolveVolume();
        int last = ClochePlanting.lastStage(seedling);
        if (plantStage >= last) {
            ItemStack harvest = ClochePlanting.harvest(server, worldPosition, ClochePlanting.atStage(seedling, last),
                    inventory.get(SLOT_CULTURE));
            if (!harvest.isEmpty() && !fitsOutput(harvest)) {
                plantTicks = BloodFedGrowthRules.CLOCHE_STAGE_TICKS;
                return false;
            }
            if (ClochePlanting.keepsFruiting(seedling)) {
                if (!payStage(vol)) return false;
            } else {
                plantStage = 0;
            }
            plantTicks = 0;
            addOutput(harvest);
            return true;
        }
        if (!payStage(vol)) {
            plantTicks = BloodFedGrowthRules.CLOCHE_STAGE_TICKS;
            return false;
        }
        plantStage++;
        plantTicks = 0;
        return true;
    }

    /** One growth stage's fresh blood from the buffer; false when there is not that much. */
    private static boolean payStage(@Nullable IBloodVolume vol) {
        if (vol == null || vol.getBloodVolume() < BloodFedGrowthRules.CLOCHE_STAGE_ML) return false;
        return vol.drain(BloodFedGrowthRules.CLOCHE_STAGE_ML);
    }

    private boolean fitsOutput(ItemStack stack) {
        ItemStack output = inventory.get(SLOT_OUTPUT);
        return output.isEmpty() || ItemStack.isSameItemSameComponents(output, stack)
                && output.getCount() + stack.getCount() <= output.getMaxStackSize();
    }

    private void addOutput(ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemStack output = inventory.get(SLOT_OUTPUT);
        if (output.isEmpty()) inventory.set(SLOT_OUTPUT, stack.copy());
        else output.grow(stack.getCount());
    }

    /** The planted crop as the bell shows it, or null when spores (or nothing) sit in the culture slot. */
    @Nullable
    public BlockState plantedState() {
        BlockState seedling = ClochePlanting.seedling(inventory.get(SLOT_CULTURE));
        return seedling == null ? null : ClochePlanting.atStage(seedling, plantStage);
    }

    public int getPlantStage() {
        return plantStage;
    }

    @Nullable
    private EnzymeFruitingRecipe findRecipe() {
        if (level == null) return null;
        ItemStack culture = inventory.get(SLOT_CULTURE);
        if (culture.isEmpty()) return null;
        for (RecipeHolder<EnzymeFruitingRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(RecipeInit.enzyme_fruiting_type.get())) {
            EnzymeFruitingRecipe recipe = holder.value();
            if (recipe.matchesCulture(culture)) return recipe;
        }
        return null;
    }

    private boolean canWork(EnzymeFruitingRecipe recipe) {
        IBloodVolume vol = resolveVolume();
        return vol != null
                && vol.getBloodVolume() >= recipe.getBloodPerTick()
                && recipe.canFitOutput(inventory.get(SLOT_OUTPUT));
    }

    private void finishRecipe(EnzymeFruitingRecipe recipe) {
        ItemStack result = recipe.getResultItemRaw().copy();
        ItemStack output = inventory.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            inventory.set(SLOT_OUTPUT, result);
        } else if (ItemStack.isSameItemSameComponents(output, result)) {
            output.grow(result.getCount());
        }
    }

    private boolean processBloodSlot() {
        return processBloodContainerInputSlot(this, this);
    }

    public ItemStack getCultureStack() {
        return inventory.get(SLOT_CULTURE);
    }

    public ItemStack getOutputStack() {
        return inventory.get(SLOT_OUTPUT);
    }

    public int getCraftingProgress() {
        return craftingProgress;
    }

    public int getCraftingTotalTime() {
        return craftingTotalTime;
    }

    public double getBloodVolume() {
        IBloodVolume vol = resolveVolume();
        return vol != null ? vol.getBloodVolume() : 0;
    }

    public double getMaxBloodVolume() {
        IBloodVolume vol = resolveVolume();
        return vol != null ? vol.getMaxBloodVolume() : 0;
    }

    @Nullable
    public IBloodVolume getBloodCapability() {
        return resolveVolume();
    }

    @Override
    public int getBloodContainerInputSlot() {
        return SLOT_BLOOD;
    }

    @Override
    public int getEmptyBloodContainerOutputSlot() {
        return SLOT_EMPTY_CONTAINER;
    }

    @Override
    public boolean acceptsBloodGourdInput() {
        return true;
    }

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(inventory, slot, amount);
        if (!stack.isEmpty()) setChanged();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack old = inventory.get(slot);
        boolean same = !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, old);
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        if (slot == SLOT_CULTURE && !same) {
            craftingProgress = 0;
            craftingTotalTime = 0;
            plantStage = 0;
            plantTicks = 0;
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CULTURE -> !findMatchingRecipe(stack).isEmpty() || ClochePlanting.plantable(stack);
            case SLOT_BLOOD -> BloodContainerTransfer.isFilledBloodContainer(stack) || BloodContainerTransfer.isBloodGourd(stack);
            default -> false;
        };
    }

    private java.util.List<EnzymeFruitingRecipe> findMatchingRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) return java.util.List.of();
        return level.getRecipeManager().getAllRecipesFor(RecipeInit.enzyme_fruiting_type.get())
                .stream()
                .map(RecipeHolder::value)
                .filter(recipe -> recipe.matchesCulture(stack))
                .toList();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        inventory = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv) {
        return new MycelialLanternMenu(id, playerInv, this, dataAccess);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.hemomancy.mycelial_lantern");
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return MycelialLanternAutomationRules.slotsFor(toAutomationSide(side));
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return direction != null
                && MycelialLanternAutomationRules.canInsert(slot, toAutomationSide(direction))
                && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return MycelialLanternAutomationRules.canExtract(slot, toAutomationSide(direction));
    }

    private MycelialLanternAutomationRules.Side toAutomationSide(Direction side) {
        if (side == Direction.UP) return MycelialLanternAutomationRules.Side.TOP;
        if (side == Direction.DOWN) return MycelialLanternAutomationRules.Side.BOTTOM;
        return MycelialLanternAutomationRules.Side.SIDE;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, inventory, registries);
        craftingProgress = tag.getInt("CraftProgress");
        craftingTotalTime = tag.getInt("CraftTotalTime");
        plantStage = tag.getInt("PlantStage");
        plantTicks = tag.getInt("PlantTicks");
        IBloodVolume vol = resolveVolume();
        if (vol != null && tag.contains(TAG_BLOOD_LEVEL)) {
            vol.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, inventory, registries);
        tag.putInt("CraftProgress", craftingProgress);
        tag.putInt("CraftTotalTime", craftingTotalTime);
        tag.putInt("PlantStage", plantStage);
        tag.putInt("PlantTicks", plantTicks);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, inventory, registries);
        tag.putInt("CraftProgress", craftingProgress);
        tag.putInt("CraftTotalTime", craftingTotalTime);
        tag.putInt("PlantStage", plantStage);
        tag.putInt("PlantTicks", plantTicks);
        IBloodVolume vol = resolveVolume();
        if (vol != null) tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, inventory, registries);
        craftingProgress = tag.getInt("CraftProgress");
        craftingTotalTime = tag.getInt("CraftTotalTime");
        plantStage = tag.getInt("PlantStage");
        plantTicks = tag.getInt("PlantTicks");
        IBloodVolume vol = resolveVolume();
        if (vol != null && tag.contains(TAG_BLOOD_LEVEL)) {
            vol.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        if (pkt.getTag() != null) {
            handleUpdateTag(pkt.getTag(), registries);
        }
    }

}
