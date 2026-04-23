package com.vincenthuto.hemomancy.common.tile.crafting;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.google.common.collect.Lists;
import com.vincenthuto.hemomancy.common.capability.player.white_humor.IWhiteHumorVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.PallidRetortMenu;
import com.vincenthuto.hemomancy.common.recipe.DistillationRecipe;
import com.vincenthuto.hemomancy.common.tile.IWhiteHumorTile;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Ghastly Alembic Block Entity — a blood distillery powered by fire below.
 * <p>
 * Slots:
 * <ul>
 *   <li>0 = Input ingredient (meat, blood items, etc.)</li>
 *   <li>1 = Flask slot (cured clay flasks to bottle blood)</li>
 *   <li>2 = Result output</li>
 * </ul>
 * Heat source: checks the block directly below for fire, soul fire, lit campfire,
 * lit soul campfire, lava, magma, or crimson flames.
 */
public class PallidRetortBlockEntity extends BaseContainerBlockEntity
        implements WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible, IWhiteHumorTile {

    // Slot indices
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FLASK = 1;
    public static final int SLOT_RESULT = 2;
    public static final int SLOT_CATALYST = 3;
    public static final int SLOT_FLASK_OUTPUT = 4;
    public static final int NUM_SLOTS = 5;
    // Container data indices
    public static final int DATA_HEATED = 0;
    public static final int DATA_COOKING_PROGRESS = 1;
    public static final int DATA_COOKING_TOTAL_TIME = 2;
    public static final int NUM_DATA_VALUES = 3;
    public static final int BURN_TIME_STANDARD = 200;
    static final String TAG_WHITE_HUMOR_LEVEL = "whiteHumorLevel";
    // Hopper / sided access
    private static final int[] SLOTS_FOR_UP = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_FOR_DOWN = new int[]{SLOT_RESULT, SLOT_FLASK_OUTPUT};
    private static final int[] SLOTS_FOR_SIDES = new int[]{SLOT_FLASK, SLOT_CATALYST};

    // ---- Fields ----
    private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();
    public NonNullList<ItemStack> items = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
    int cookingProgress;
    int cookingTotalTime;
    private boolean heated;
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_HEATED -> heated ? 1 : 0;
                case DATA_COOKING_PROGRESS -> cookingProgress;
                case DATA_COOKING_TOTAL_TIME -> cookingTotalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_HEATED -> heated = value != 0;
                case DATA_COOKING_PROGRESS -> cookingProgress = value;
                case DATA_COOKING_TOTAL_TIME -> cookingTotalTime = value;
            }
        }

        @Override
        public int getCount() {
            return NUM_DATA_VALUES;
        }
    };

    // ---- Constructor ----

    public PallidRetortBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.pallid_retort.get(), pos, state);
    }

    // ---- Heat source detection ----

    /**
     * Returns true if the block directly below is a valid cold source.
     */
    public static boolean isHeatSource(Level level, BlockPos alembicPos) {
        BlockPos below = alembicPos.below();
        BlockState belowState = level.getBlockState(below);
        if (belowState.is(Blocks.SOUL_FIRE)) {
            return true;
        } else if (belowState.is(Blocks.SOUL_CAMPFIRE)) {
            return belowState.getValue(CampfireBlock.LIT);
        } else if (belowState.is(Blocks.BLUE_ICE)) {
            return true;
        } else {
            return false;
        }
    }

    // ---- Capability (lazy) ----

    private static int getTotalCookTime(Level level, PallidRetortBlockEntity te) {
        return level.getRecipeManager()
                .getAllRecipesFor(RecipeInit.distillation_recipe_type.get())
                .stream()
                .filter(h -> h.value().isPallid() && h.value().matchesItems(te.items.get(SLOT_INPUT), te.items.get(SLOT_CATALYST)))
                .mapToInt(h -> h.value().getCookingTime())
                .findFirst()
                .orElse(200);
    }

    // ---- Ticking ----

    @Nullable
    private static RecipeHolder<DistillationRecipe> findMatchingRecipe(Level level, PallidRetortBlockEntity te) {
        return level.getRecipeManager()
                .getAllRecipesFor(RecipeInit.distillation_recipe_type.get())
                .stream()
                .filter(h -> h.value().isPallid() && h.value().matchesItems(te.items.get(SLOT_INPUT), te.items.get(SLOT_CATALYST)))
                .findFirst()
                .orElse(null);
    }

    private static void createExperience(ServerLevel level, Vec3 pos, int count, float xpPerItem) {
        int i = Mth.floor(count * xpPerItem);
        float f = Mth.frac(count * xpPerItem);
        if (f != 0.0F && Math.random() < f) {
            ++i;
        }
        ExperienceOrb.award(level, pos, i);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PallidRetortBlockEntity te) {
        boolean wasHeated = te.heated;
        te.heated = isHeatSource(level, pos);
        boolean dirty = false;

        IWhiteHumorVolume vol = te.resolveVolume();
        if (vol == null) return;

        if (vol.getWhiteHumorVolume() < vol.getMaxWhiteHumorVolume() - 99) {
            if (te.heated && !te.items.get(SLOT_INPUT).isEmpty()) {
                RecipeHolder<DistillationRecipe> recipe = findMatchingRecipe(level, te);
                int maxStack = te.getMaxStackSize();

                if (te.canBurn(level.registryAccess(), recipe, te.items, maxStack)) {
                    ++te.cookingProgress;
                    if (te.cookingProgress >= te.cookingTotalTime) {
                        te.cookingProgress = 0;
                        te.cookingTotalTime = getTotalCookTime(level, te);
                        if (te.burn(level.registryAccess(), recipe, te.items, maxStack)) {
                            te.setRecipeUsed(recipe);
                            vol.fill(100);
                            te.sendUpdates();
                        }
                        dirty = true;
                    }
                } else {
                    te.cookingProgress = 0;
                }
            } else if (!te.heated && te.cookingProgress > 0) {
                // Cool down when no heat
                te.cookingProgress = Mth.clamp(te.cookingProgress - 2, 0, te.cookingTotalTime);
            }
        } else {
            // Blood tank full — stop processing
            te.cookingProgress = 0;
        }

        // Update LIT blockstate
        if (wasHeated != te.heated) {
            dirty = true;
            state = state.setValue(AbstractFurnaceBlock.LIT, te.heated);
            level.setBlock(pos, state, 3);
        }

        // Drain stored blood into flasks (independent of cooking)
        tryDrainBloodIntoFlask(te);

        // Fill blood from bloody flasks (independent of cooking)
        tryFillBloodFromFlask(te);

        if (dirty) {
            setChanged(level, pos, state);
        }
    }

    /**
     * Called from serverTick — drains stored blood into flasks in the flask slot,
     * one flask per tick if conditions are met.
     */
    private static void tryDrainBloodIntoFlask(PallidRetortBlockEntity te) {
        ItemStack flaskStack = te.items.get(SLOT_FLASK);
        if (flaskStack.isEmpty() || flaskStack.getItem() != HLItemInit.cured_clay_flask.get()) return;

        IWhiteHumorVolume vol = te.resolveVolume();
        if (vol == null || vol.getWhiteHumorVolume() < 100) return;

        ItemStack resultStack = te.items.get(SLOT_RESULT);
        if (resultStack.isEmpty()) {
            flaskStack.shrink(1);
            te.items.set(SLOT_RESULT, new ItemStack(ItemInit.bloody_flask.get()));
            vol.drain(100);
            te.sendUpdates();
        } else if (resultStack.getItem() == ItemInit.bloody_flask.get()
                && resultStack.getCount() < resultStack.getMaxStackSize()) {
            flaskStack.shrink(1);
            resultStack.grow(1);
            vol.drain(100);
            te.sendUpdates();
        }
    }

    // ---- Recipe logic ----

    /**
     * Called from serverTick — consumes bloody flasks in the flask slot to fill
     * the blood reservoir, outputting empty clay flasks to the flask output slot.
     */
    private static void tryFillBloodFromFlask(PallidRetortBlockEntity te) {
        ItemStack flaskStack = te.items.get(SLOT_FLASK);
        if (flaskStack.isEmpty()) return;
        if (!(flaskStack.getItem() instanceof BloodyFlaskItem flask)) return;

        IWhiteHumorVolume vol = te.resolveVolume();
        if (vol == null || vol.isFull()) return;

        double amount = flask.getAmount();
        if (vol.getWhiteHumorVolume() + amount > vol.getMaxWhiteHumorVolume()) return;

        // Check if we can output the empty flask
        ItemStack outputStack = te.items.get(SLOT_FLASK_OUTPUT);
        if (outputStack.isEmpty()
                || (outputStack.getItem() == HLItemInit.cured_clay_flask.get()
                && outputStack.getCount() < outputStack.getMaxStackSize())) {
            // Consume the bloody flask
            flaskStack.shrink(1);
            vol.addWhiteHumorVolume(amount);
            // Output empty flask
            if (outputStack.isEmpty()) {
                te.items.set(SLOT_FLASK_OUTPUT, new ItemStack(HLItemInit.cured_clay_flask.get()));
            } else {
                outputStack.grow(1);
            }
            te.sendUpdates();
        }
    }

    @Nullable
    private IWhiteHumorVolume resolveVolume() {
        return HemoCapabilityAccess.getWhiteHumorVolume(this).orElse(null);
    }

    // ---- Flask filling from stored blood ----

    private boolean canBurn(RegistryAccess registryAccess, @Nullable RecipeHolder<DistillationRecipe> recipeHolder, NonNullList<ItemStack> inv, int maxStack) {
        if (inv.get(SLOT_INPUT).isEmpty() || recipeHolder == null) return false;

        ItemStack result = recipeHolder.value().getResultItem(registryAccess).copy();
        if (result.isEmpty()) return false;

        ItemStack currentResult = inv.get(SLOT_RESULT);
        if (currentResult.isEmpty()) return true;
        if (!ItemStack.isSameItem(currentResult, result)) return false;
        int totalCount = currentResult.getCount() + result.getCount();
        return totalCount <= maxStack && totalCount <= currentResult.getMaxStackSize();
    }

    private boolean burn(RegistryAccess registryAccess, @Nullable RecipeHolder<DistillationRecipe> recipeHolder, NonNullList<ItemStack> inv, int maxStack) {
        if (recipeHolder == null || !canBurn(registryAccess, recipeHolder, inv, maxStack)) return false;

        DistillationRecipe recipe = recipeHolder.value();
        ItemStack input = inv.get(SLOT_INPUT);
        ItemStack recipeResult = recipe.getResultItem(registryAccess).copy();
        ItemStack flaskStack = inv.get(SLOT_FLASK);
        ItemStack resultStack = inv.get(SLOT_RESULT);

        // If a flask is present and result slot can accept a bloody flask
        if (!flaskStack.isEmpty() && flaskStack.getItem() == HLItemInit.cured_clay_flask.get()) {
            if (resultStack.isEmpty()) {
                inv.set(SLOT_RESULT, new ItemStack(ItemInit.bloody_flask.get()));
                flaskStack.shrink(1);
            } else if (resultStack.getItem() == ItemInit.bloody_flask.get()
                    && resultStack.getCount() < resultStack.getMaxStackSize()) {
                resultStack.grow(1);
                flaskStack.shrink(1);
            } else {
                // Result slot has something incompatible or is full — just put recipe result
                if (resultStack.isEmpty()) {
                    inv.set(SLOT_RESULT, recipeResult.copy());
                } else if (ItemStack.isSameItem(resultStack, recipeResult)) {
                    resultStack.grow(recipeResult.getCount());
                }
            }
        } else {
            // No flask — output the recipe result directly
            if (resultStack.isEmpty()) {
                inv.set(SLOT_RESULT, recipeResult.copy());
            } else if (ItemStack.isSameItem(resultStack, recipeResult)) {
                resultStack.grow(recipeResult.getCount());
            }
        }

        input.shrink(1);
        return true;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack existing = this.items.get(slot);
        boolean sameItem = !stack.isEmpty() && ItemStack.isSameItemSameTags(existing, stack);
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        // Reset cooking progress when input changes
        if (slot == SLOT_INPUT && !sameItem) {
            this.cookingTotalTime = (this.level != null) ? getTotalCookTime(this.level, this) : BURN_TIME_STANDARD;
            this.cookingProgress = 0;
            this.setChanged();
        }
    }

    // ---- Menu creation ----

    @Override
    protected AbstractContainerMenu createMenu(int windowId, Inventory playerInv) {
        return new PallidRetortMenu(windowId, playerInv, this, this.dataAccess);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.hemomancy.pallid_retort");
    }

    // ---- Blood helpers ----

    public IWhiteHumorVolume getWhiteHumorCapability() {
        IWhiteHumorVolume vol = resolveVolume();
        if (vol == null) throw new IllegalStateException("White Humor capability not available yet");
        return vol;
    }

    public double getWhiteHumorVolume() {
        IWhiteHumorVolume vol = resolveVolume();
        return vol != null ? vol.getWhiteHumorVolume() : 0;
    }

    public double getMaxWhiteHumorVolume() {
        IWhiteHumorVolume vol = resolveVolume();
        return vol != null ? vol.getMaxWhiteHumorVolume() : 0;
    }

    public boolean isHeated() {
        return heated;
    }

    // ---- Experience / Recipe used ----

    public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
        List<RecipeHolder<?>> holders = Lists.newArrayList();
        for (Entry<ResourceLocation> entry : this.recipesUsed.object2IntEntrySet()) {
            player.serverLevel().getRecipeManager().byKey(entry.getKey()).ifPresent(holder -> {
                holders.add(holder);
                if (holder.value() instanceof DistillationRecipe gar) {
                    createExperience(player.serverLevel(), player.position(), entry.getIntValue(), gar.getExperience());
                }
            });
        }
        player.awardRecipes(holders);
        this.recipesUsed.clear();
    }

    @Override
    @Nullable
    public RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
        if (recipe != null) {
            this.recipesUsed.addTo(recipe.id(), 1);
        }
    }

    // ---- Container impl ----

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(this.items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public boolean stillValid(Player player) {
        return (this.level.getBlockEntity(this.worldPosition) == this)
                && player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        for (ItemStack stack : this.items) {
            contents.accountStack(stack);
        }
    }

    // ---- WorldlyContainer ----

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return switch (direction) {
            case UP -> SLOTS_FOR_UP;
            case DOWN -> SLOTS_FOR_DOWN;
            default -> SLOTS_FOR_SIDES;
        };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_RESULT) return false;
        if (slot == SLOT_FLASK_OUTPUT) return false;
        if (slot == SLOT_FLASK) {
            return stack.getItem() == HLItemInit.cured_clay_flask.get()
                    || stack.getItem() instanceof BloodyFlaskItem;
        }
        if (slot == SLOT_CATALYST) return true; // any item allowed as catalyst
        return true; // SLOT_INPUT
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == SLOT_RESULT || slot == SLOT_FLASK_OUTPUT;
    }

    // ---- Load / Save ----

    @Override
    public void onLoad() {
        IWhiteHumorVolume vol = resolveVolume();
        if (vol != null) {
            vol.setActive(true);
            vol.setMaxWhiteHumorVolume(2000f);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
        this.heated = tag.getBoolean("Heated");
        this.cookingProgress = tag.getInt("CookTime");
        this.cookingTotalTime = tag.getInt("CookTimeTotal");
        CompoundTag recipesTag = tag.getCompound("RecipesUsed");
        for (String s : recipesTag.getAllKeys()) {
            this.recipesUsed.put(ResourceLocation.parse(s), recipesTag.getInt(s));
        }
        IWhiteHumorVolume vol = resolveVolume();
        if (vol != null) {
            vol.setWhiteHumorVolume(tag.getFloat(TAG_WHITE_HUMOR_LEVEL));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Heated", this.heated);
        tag.putInt("CookTime", this.cookingProgress);
        tag.putInt("CookTimeTotal", this.cookingTotalTime);
        ContainerHelper.saveAllItems(tag, this.items);
        CompoundTag recipesTag = new CompoundTag();
        this.recipesUsed.forEach((key, val) -> recipesTag.putInt(key.toString(), val));
        tag.put("RecipesUsed", recipesTag);
        IWhiteHumorVolume vol = resolveVolume();
        if (vol != null) {
            tag.putDouble(TAG_WHITE_HUMOR_LEVEL, vol.getWhiteHumorVolume());
        }
    }

    // ---- Sync ----

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Heated", this.heated);
        tag.putInt("CookTime", this.cookingProgress);
        tag.putInt("CookTimeTotal", this.cookingTotalTime);
        ContainerHelper.saveAllItems(tag, this.items);
        CompoundTag recipesTag = new CompoundTag();
        this.recipesUsed.forEach((key, val) -> recipesTag.putInt(key.toString(), val));
        tag.put("RecipesUsed", recipesTag);
        IWhiteHumorVolume vol = resolveVolume();
        if (vol != null) {
            tag.putDouble(TAG_WHITE_HUMOR_LEVEL, vol.getWhiteHumorVolume());
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag != null) {
            IWhiteHumorVolume vol = resolveVolume();
            if (vol != null) {
                vol.setWhiteHumorVolume(tag.getFloat(TAG_WHITE_HUMOR_LEVEL));
            }
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        if (pkt.getTag() != null) {
            IWhiteHumorVolume vol = resolveVolume();
            if (vol != null) {
                vol.setWhiteHumorVolume(pkt.getTag().getFloat(TAG_WHITE_HUMOR_LEVEL));
            }
        }
    }

    @Override
    public void sendUpdates() {
        level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }

}
