package com.vincenthuto.hemomancy.common.tile.crafting;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.MycelialLanternMenu;
import com.vincenthuto.hemomancy.common.recipe.EnzymeFruitingRecipe;
import com.vincenthuto.hemomancy.common.tile.IBloodTile;
import com.vincenthuto.hemomancy.common.tile.IMultiBlockEntity;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

public class MycelialLanternBlockEntity extends BaseContainerBlockEntity implements IBloodTile, IMultiBlockEntity, WorldlyContainer {

    public static final String TAG_BLOOD_LEVEL = "bloodLevel";
    private static final float MAX_BLOOD = 4_000F;

    public static final int SLOT_CULTURE = MycelialLanternAutomationRules.SLOT_CULTURE;
    public static final int SLOT_BLOOD = MycelialLanternAutomationRules.SLOT_BLOOD;
    public static final int SLOT_OUTPUT = MycelialLanternAutomationRules.SLOT_OUTPUT;
    public static final int SLOT_EMPTY_CONTAINER = MycelialLanternAutomationRules.SLOT_EMPTY_CONTAINER;
    public static final int INVENTORY_SIZE = 4;

    private NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int craftingProgress;
    private int craftingTotalTime;

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
        boolean changed = te.processBloodSlot();
        EnzymeFruitingRecipe recipe = te.findRecipe();

        if (recipe == null) {
            if (te.craftingProgress != 0 || te.craftingTotalTime != 0) {
                te.craftingProgress = 0;
                te.craftingTotalTime = 0;
                changed = true;
            }
        } else {
            te.craftingTotalTime = recipe.getDuration();
            if (te.canWork(recipe)) {
                te.resolveVolume().subtractBloodVolume(recipe.getBloodPerTick());
                te.craftingProgress++;
                changed = true;
                if (te.craftingProgress >= recipe.getDuration()) {
                    te.finishRecipe(recipe);
                    te.craftingProgress = 0;
                }
            }
        }

        if (changed) {
            te.sendUpdates();
            setChanged(level, pos, state);
        }
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
        ItemStack bloodStack = inventory.get(SLOT_BLOOD);
        if (bloodStack.isEmpty()) return false;

        IBloodVolume vol = resolveVolume();
        if (vol == null || vol.isFull()) return false;

        if (bloodStack.getItem() instanceof BloodyFlaskItem flask) {
            double amount = flask.getAmount();
            double available = vol.getMaxBloodVolume() - vol.getBloodVolume();
            if (available < amount) return false;
            ItemStack outputStack = inventory.get(SLOT_EMPTY_CONTAINER);
            if (canAcceptEmptyFlask(outputStack)) {
                vol.addBloodVolume(amount);
                bloodStack.shrink(1);
                addEmptyFlask();
                return true;
            }
        } else if (bloodStack.getItem() instanceof BloodGourdItem) {
            IBloodVolume gourdVol = HemoCapabilityAccess.getBloodVolume(bloodStack).orElse(null);
            if (gourdVol != null && gourdVol.getBloodVolume() > 0) {
                double transfer = Math.min(gourdVol.getBloodVolume(), vol.getMaxBloodVolume() - vol.getBloodVolume());
                transfer = Math.min(transfer, 10);
                if (transfer > 0) {
                    gourdVol.subtractBloodVolume(transfer);
                    vol.addBloodVolume(transfer);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canAcceptEmptyFlask(ItemStack stack) {
        return stack.isEmpty()
                || (stack.getItem() == HLItemInit.cured_clay_flask.get()
                && stack.getCount() < stack.getMaxStackSize());
    }

    private void addEmptyFlask() {
        ItemStack outputStack = inventory.get(SLOT_EMPTY_CONTAINER);
        if (outputStack.isEmpty()) {
            inventory.set(SLOT_EMPTY_CONTAINER, new ItemStack(HLItemInit.cured_clay_flask.get()));
        } else {
            outputStack.grow(1);
        }
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
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CULTURE -> !findMatchingRecipe(stack).isEmpty();
            case SLOT_BLOOD -> stack.getItem() instanceof BloodyFlaskItem || stack.getItem() instanceof BloodGourdItem;
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

    public AABB getRenderBoundingBox() {
        return IMultiBlockEntity.computeMultiBlockAABB(this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, inventory, registries);
        craftingProgress = tag.getInt("CraftProgress");
        craftingTotalTime = tag.getInt("CraftTotalTime");
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
        IBloodVolume vol = resolveVolume();
        if (vol != null) tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, inventory, registries);
        tag.putInt("CraftProgress", craftingProgress);
        tag.putInt("CraftTotalTime", craftingTotalTime);
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
        IBloodVolume vol = resolveVolume();
        if (vol != null && tag.contains(TAG_BLOOD_LEVEL)) {
            vol.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        if (pkt.getTag() != null) {
            handleUpdateTag(pkt.getTag(), registries);
        }
    }

    @Override
    public void sendUpdates() {
        if (level == null) return;
        level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }
}
