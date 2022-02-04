package com.vincenthuto.hemomancy.tile;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.container.MenuEarthlyTransfuser;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.init.FluidInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.init.RecipeTypeInit;
import com.vincenthuto.hutoslib.common.item.HLItemInit;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class BlockEntityEarthlyTransfuser extends BaseContainerBlockEntity
		implements WorldlyContainer, RecipeHolder, StackedContentsCompatible, IBloodTile {

	protected static final int SLOT_INPUT = 0;
	protected static final int SLOT_FUEL = 1;
	protected static final int SLOT_RESULT = 2;
	public static final int DATA_LIT_TIME = 0;
	private static final int[] SLOTS_FOR_UP = new int[] { 0 };
	private static final int[] SLOTS_FOR_DOWN = new int[] { 2 };
	private static final int[] SLOTS_FOR_SIDES = new int[] { 1 };
	private static final int[] SLOTS_FOR_EAST = new int[] { 3 };
	private static final int[] SLOTS_FOR_SOUTH = new int[] { 1 };
	public static final int DATA_LIT_DURATION = 1;
	public static final int DATA_COOKING_PROGRESS = 2;
	public static final int DATA_COOKING_TOTAL_TIME = 3;
	public static final int NUM_DATA_VALUES = 4;
	public static final int BURN_TIME_STANDARD = 200;
	public static final int BURN_COOL_SPEED = 2;
	static final String TAG_BLOOD_LEVEL = "bloodLevel";
	IBloodVolume volume = getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);

	public NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
	int litTime;
	int litDuration;
	int cookingProgress;
	int cookingTotalTime;
	protected final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int p_58431_) {
			switch (p_58431_) {
			case 0:
				return litTime;
			case 1:
				return litDuration;
			case 2:
				return cookingProgress;
			case 3:
				return cookingTotalTime;
			default:
				return 0;
			}
		}

		@Override
		public void set(int p_58433_, int p_58434_) {
			switch (p_58433_) {
			case 0:
				litTime = p_58434_;
				break;
			case 1:
				litDuration = p_58434_;
				break;
			case 2:
				cookingProgress = p_58434_;
				break;
			case 3:
				cookingTotalTime = p_58434_;
			}
		}

		@Override
		public int getCount() {
			return 4;
		}
	};
	private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();
	private final RecipeType<? extends AbstractCookingRecipe> recipeType = RecipeTypeInit.earthly_transfuser_recipe_type;

	public BlockEntityEarthlyTransfuser(BlockPos p_154992_, BlockState p_154993_) {
		super(BlockEntityInit.earthly_transfuser.get(), p_154992_, p_154993_);
	}

	@Override
	public void onLoad() {
		volume.setActive(true);
	}

	public IBloodVolume getBloodCapability() {
		return volume;
	}

	public double getBloodVolume() {
		return volume.getBloodVolume();
	}

	public double getMaxBloodVolume() {
		return volume.getMaxBloodVolume();
	}

	@Deprecated
	public static Map<Item, Integer> getFuel() {
		Map<Item, Integer> map = Maps.newLinkedHashMap();
		add(map, Blocks.COAL_BLOCK, 16000);
		add(map, Items.BLAZE_ROD, 2400);
		add(map, Items.COAL, 1600);
		add(map, Items.CHARCOAL, 1600);
		add(map, ItemTags.LOGS, 300);
		add(map, ItemTags.PLANKS, 300);
		add(map, ItemTags.WOODEN_STAIRS, 300);
		add(map, ItemTags.WOODEN_SLABS, 150);
		add(map, ItemTags.WOODEN_TRAPDOORS, 300);
		add(map, ItemTags.WOODEN_PRESSURE_PLATES, 300);
		add(map, Blocks.OAK_FENCE, 300);
		add(map, Blocks.BIRCH_FENCE, 300);
		add(map, Blocks.SPRUCE_FENCE, 300);
		add(map, Blocks.JUNGLE_FENCE, 300);
		add(map, Blocks.DARK_OAK_FENCE, 300);
		add(map, Blocks.ACACIA_FENCE, 300);
		add(map, Blocks.OAK_FENCE_GATE, 300);
		add(map, Blocks.BIRCH_FENCE_GATE, 300);
		add(map, Blocks.SPRUCE_FENCE_GATE, 300);
		add(map, Blocks.JUNGLE_FENCE_GATE, 300);
		add(map, Blocks.DARK_OAK_FENCE_GATE, 300);
		add(map, Blocks.ACACIA_FENCE_GATE, 300);
		add(map, Blocks.NOTE_BLOCK, 300);
		add(map, Blocks.BOOKSHELF, 300);
		add(map, Blocks.LECTERN, 300);
		add(map, Blocks.JUKEBOX, 300);
		add(map, Blocks.CHEST, 300);
		add(map, Blocks.TRAPPED_CHEST, 300);
		add(map, Blocks.CRAFTING_TABLE, 300);
		add(map, Blocks.DAYLIGHT_DETECTOR, 300);
		add(map, ItemTags.BANNERS, 300);
		add(map, Items.BOW, 300);
		add(map, Items.FISHING_ROD, 300);
		add(map, Blocks.LADDER, 300);
		add(map, ItemTags.SIGNS, 200);
		add(map, Items.WOODEN_SHOVEL, 200);
		add(map, Items.WOODEN_SWORD, 200);
		add(map, Items.WOODEN_HOE, 200);
		add(map, Items.WOODEN_AXE, 200);
		add(map, Items.WOODEN_PICKAXE, 200);
		add(map, ItemTags.WOODEN_DOORS, 200);
		add(map, ItemTags.BOATS, 1200);
		add(map, ItemTags.WOOL, 100);
		add(map, ItemTags.WOODEN_BUTTONS, 100);
		add(map, Items.STICK, 100);
		add(map, ItemTags.SAPLINGS, 100);
		add(map, Items.BOWL, 100);
		add(map, ItemTags.CARPETS, 67);
		add(map, Blocks.DRIED_KELP_BLOCK, 4001);
		add(map, Items.CROSSBOW, 300);
		add(map, Blocks.BAMBOO, 50);
		add(map, Blocks.DEAD_BUSH, 100);
		add(map, Blocks.SCAFFOLDING, 400);
		add(map, Blocks.LOOM, 300);
		add(map, Blocks.BARREL, 300);
		add(map, Blocks.CARTOGRAPHY_TABLE, 300);
		add(map, Blocks.FLETCHING_TABLE, 300);
		add(map, Blocks.SMITHING_TABLE, 300);
		add(map, Blocks.COMPOSTER, 300);
		add(map, Blocks.AZALEA, 100);
		add(map, Blocks.FLOWERING_AZALEA, 100);
		return map;
	}

	private static boolean isNeverAFurnaceFuel(Item item) {
		return ItemTags.NON_FLAMMABLE_WOOD.contains(item);
	}

	private static void add(Map<Item, Integer> map, Tag<Item> tag, int p_58373_) {
		for (Item item : tag.getValues()) {
			if (!isNeverAFurnaceFuel(item)) {
				map.put(item, p_58373_);
			}
		}

	}

	private static void add(Map<Item, Integer> p_58375_, ItemLike p_58376_, int p_58377_) {
		Item item = p_58376_.asItem();
		if (isNeverAFurnaceFuel(item)) {
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				throw Util.pauseInIde(
						new IllegalStateException("A developer tried to explicitly make fire resistant item "
								+ item.getName((ItemStack) null).getString() + " a furnace fuel. That will not work!"));
			}
		} else {
			p_58375_.put(item, p_58377_);
		}
	}

	private boolean isLit() {
		return this.litTime > 0;
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, this.items);
		this.litTime = tag.getInt("BurnTime");
		this.cookingProgress = tag.getInt("CookTime");
		this.cookingTotalTime = tag.getInt("CookTimeTotal");
		this.litDuration = this.getBurnDuration(this.items.get(1));
		CompoundTag compoundtag = tag.getCompound("RecipesUsed");
		for (String s : compoundtag.getAllKeys()) {
			this.recipesUsed.put(new ResourceLocation(s), compoundtag.getInt(s));
		}
		if (tag != null) {
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		}

	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("BurnTime", this.litTime);
		tag.putInt("CookTime", this.cookingProgress);
		tag.putInt("CookTimeTotal", this.cookingTotalTime);
		ContainerHelper.saveAllItems(tag, this.items);
		CompoundTag compoundtag = new CompoundTag();
		this.recipesUsed.forEach((p_58382_, p_58383_) -> {
			compoundtag.putInt(p_58382_.toString(), p_58383_);
		});
		tag.put("RecipesUsed", compoundtag);
		if (tag != null) {
			tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		}
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		if (tag != null) {
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		}
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, this.items);
		tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		return tag;
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		if (pkt.getTag() != null) {
			CompoundTag tag = pkt.getTag();
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		}

	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);

	}

	@SuppressWarnings("unchecked")
	public static void serverTick(Level level, BlockPos pos, BlockState p_155016_, BlockEntityEarthlyTransfuser te) {
		boolean flag = te.isLit();
		boolean flag1 = false;
		if (te.isLit()) {
			--te.litTime;
		}
		if (te.getBloodVolume() < te.getMaxBloodVolume() - 99) {
			ItemStack itemstack = te.items.get(1);
			if (te.isLit() || !itemstack.isEmpty() && !te.items.get(0).isEmpty()) {
				Recipe<?> recipe = level.getRecipeManager()
						.getRecipeFor((RecipeType<AbstractCookingRecipe>) te.recipeType, te, level).orElse(null);
				int i = te.getMaxStackSize();
				if (!te.isLit() && te.canBurn(recipe, te.items, i)) {
					te.litTime = te.getBurnDuration(itemstack);
					te.litDuration = te.litTime;
					if (te.isLit()) {
						flag1 = true;
						if (itemstack.hasContainerItem())
							te.items.set(1, itemstack.getContainerItem());
						else if (!itemstack.isEmpty()) {
							itemstack.getItem();
							itemstack.shrink(1);
							if (itemstack.isEmpty()) {
								te.items.set(1, itemstack.getContainerItem());
							}
						}
					}
				}
				if (te.isLit() && te.canBurn(recipe, te.items, i)) {
					++te.cookingProgress;

					if (te.cookingProgress == te.cookingTotalTime) {
						te.cookingProgress = 0;
						te.cookingTotalTime = getTotalCookTime(level, te.recipeType, te);
						if (te.burn(recipe, te.items, i)) {
							te.setRecipeUsed(recipe);
							te.getBloodCapability().fill(100);
							te.sendUpdates();
						}
						flag1 = true;
					}
				} else {
					te.cookingProgress = 0;
				}
			} else if (!te.isLit() && te.cookingProgress > 0) {
				te.cookingProgress = Mth.clamp(te.cookingProgress - 2, 0, te.cookingTotalTime);
			}

			if (flag != te.isLit()) {
				flag1 = true;
				p_155016_ = p_155016_.setValue(AbstractFurnaceBlock.LIT, Boolean.valueOf(te.isLit()));
				level.setBlock(pos, p_155016_, 3);
			}

			if (flag1) {
				setChanged(level, pos, p_155016_);
			}
		} else {
			te.litTime = 0;
			te.cookingProgress = 0;

			p_155016_ = p_155016_.setValue(AbstractFurnaceBlock.LIT, false);
			level.setBlock(pos, p_155016_, 3);
		}

	}

	@SuppressWarnings("unchecked")
	private boolean canBurn(@Nullable Recipe<?> p_155006_, NonNullList<ItemStack> p_155007_, int p_155008_) {
		if (!p_155007_.get(0).isEmpty() && p_155006_ != null) {
			ItemStack itemstack = ((Recipe<WorldlyContainer>) p_155006_).assemble(this);
			if (itemstack.isEmpty()) {
				return false;
			} else {
				ItemStack itemstack1 = p_155007_.get(2);
				if (itemstack1.isEmpty()) {
					return true;
				} else if (!itemstack1.sameItem(itemstack)) {
					return false;
				} else if (itemstack1.getCount() + itemstack.getCount() <= p_155008_
						&& itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) { // Forge fix:
					return true;
				} else {
					return itemstack1.getCount() + itemstack.getCount() <= itemstack.getMaxStackSize(); // Forge fix:
				}
			}
		} else {
			return false;
		}
	}

	private boolean burn(@Nullable Recipe<?> recipe, NonNullList<ItemStack> inventory, int p_155029_) {
		if (recipe != null && this.canBurn(recipe, inventory, p_155029_)) {
			ItemStack itemstack = inventory.get(0);
			@SuppressWarnings("unchecked")
			ItemStack itemstack1 = ((Recipe<WorldlyContainer>) recipe).assemble(this);
			ItemStack bucketstack = inventory.get(2);

			if (inventory.get(3).isEmpty()) {
				if (bucketstack.isEmpty()) {
					inventory.set(2, itemstack1.copy());
				} else if (bucketstack.is(itemstack1.getItem())) {
					bucketstack.grow(itemstack1.getCount());
				}
			} else {
				if (bucketstack.isEmpty()) {
					inventory.set(2, new ItemStack(ItemInit.bloody_flask.get()));
					inventory.get(3).shrink(1);
				} else {
					bucketstack.grow(itemstack1.getCount());
					this.cookingProgress = 0;
				}
			}
			itemstack.shrink(1);
			return true;
		} else {
			return false;
		}
	}

	protected int getBurnDuration(ItemStack p_58343_) {
		if (p_58343_.isEmpty()) {
			return 0;
		} else {
			p_58343_.getItem();
			return net.minecraftforge.common.ForgeHooks.getBurnTime(p_58343_, this.recipeType);
		}
	}

	@SuppressWarnings("unchecked")
	private static int getTotalCookTime(Level p_155010_, RecipeType<? extends AbstractCookingRecipe> p_155011_,
			Container p_155012_) {
		return p_155010_.getRecipeManager()
				.getRecipeFor((RecipeType<AbstractCookingRecipe>) p_155011_, p_155012_, p_155010_)
				.map(AbstractCookingRecipe::getCookingTime).orElse(200);
	}

	public static boolean isFuel(ItemStack p_58400_) {
		return net.minecraftforge.common.ForgeHooks.getBurnTime(p_58400_, null) > 0;
	}

	@Override
	public int[] getSlotsForFace(Direction direction) {
		switch (direction) {
		case DOWN:
			return SLOTS_FOR_DOWN;
		case EAST:
			return SLOTS_FOR_EAST;
		case SOUTH:
			return SLOTS_FOR_SOUTH;
		case UP:
			return SLOTS_FOR_UP;
		default:
			return SLOTS_FOR_SIDES;
		}
	}

	@Override
	public boolean canPlaceItemThroughFace(int p_58336_, ItemStack p_58337_, @Nullable Direction p_58338_) {
		return this.canPlaceItem(p_58336_, p_58337_);
	}

	@Override
	public boolean canTakeItemThroughFace(int p_58392_, ItemStack p_58393_, Direction p_58394_) {
		return (p_58394_ == Direction.DOWN && p_58392_ == 1)
				? p_58393_.is(Items.WATER_BUCKET) || p_58393_.is(Items.BUCKET)
				: true;
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.items) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int p_58328_) {
		return this.items.get(p_58328_);
	}

	@Override
	public ItemStack removeItem(int p_58330_, int p_58331_) {
		return ContainerHelper.removeItem(this.items, p_58330_, p_58331_);
	}

	@Override
	public ItemStack removeItemNoUpdate(int p_58387_) {
		return ContainerHelper.takeItem(this.items, p_58387_);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		ItemStack itemstack = this.items.get(slot);
		boolean flag = !stack.isEmpty() && stack.sameItem(itemstack) && ItemStack.tagMatches(stack, itemstack);
		this.items.set(slot, stack);
		if (stack.getCount() > this.getMaxStackSize()) {
			stack.setCount(this.getMaxStackSize());
		}
		if (slot == 0 && !flag) {
			this.cookingTotalTime = getTotalCookTime(this.level, this.recipeType, this);
			this.cookingProgress = 0;
			this.setChanged();
		}
		if (slot == 3 && volume.getBloodVolume() > 100) {
			if (stack.getItem() == HLItemInit.cured_clay_flask.get()) {
				stack.shrink(1);
				if (this.items.get(2).isEmpty()) {
					this.items.set(2, new ItemStack(ItemInit.bloody_flask.get()));
					volume.drain (100);
					sendUpdates();
				} else if (this.items.get(2).getItem() == ItemInit.bloody_flask.get()) {
					this.items.get(2).grow(1);
					volume.drain(100);
					sendUpdates();
				}
			}
		}
	}

	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	@Override
	public boolean stillValid(Player p_58340_) {
		return (this.level.getBlockEntity(this.worldPosition) != this) ? false
				: p_58340_.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D,
						this.worldPosition.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public boolean canPlaceItem(int p_58389_, ItemStack p_58390_) {
		return super.canPlaceItem(p_58389_, p_58390_);

	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	public void setRecipeUsed(@Nullable Recipe<?> p_58345_) {
		if (p_58345_ != null) {
			ResourceLocation resourcelocation = p_58345_.getId();
			this.recipesUsed.addTo(resourcelocation, 1);
		}
	}

	@Override
	@Nullable
	public Recipe<?> getRecipeUsed() {
		return null;
	}

	public void awardUsedRecipesAndPopExperience(ServerPlayer p_155004_) {
		List<Recipe<?>> list = this.getRecipesToAwardAndPopExperience(p_155004_.getLevel(), p_155004_.position());
		p_155004_.awardRecipes(list);
		this.recipesUsed.clear();
	}

	public List<Recipe<?>> getRecipesToAwardAndPopExperience(ServerLevel p_154996_, Vec3 p_154997_) {
		List<Recipe<?>> list = Lists.newArrayList();
		for (Entry<ResourceLocation> entry : this.recipesUsed.object2IntEntrySet()) {
			p_154996_.getRecipeManager().byKey(entry.getKey()).ifPresent((p_155023_) -> {
				list.add(p_155023_);
				createExperience(p_154996_, p_154997_, entry.getIntValue(),
						((AbstractCookingRecipe) p_155023_).getExperience());
			});
		}

		return list;
	}

	private static void createExperience(ServerLevel p_154999_, Vec3 p_155000_, int p_155001_, float p_155002_) {
		int i = Mth.floor(p_155001_ * p_155002_);
		float f = Mth.frac(p_155001_ * p_155002_);
		if (f != 0.0F && Math.random() < f) {
			++i;
		}

		ExperienceOrb.award(p_154999_, p_155000_, i);
	}

	@Override
	public void fillStackedContents(StackedContents p_58342_) {
		for (ItemStack itemstack : this.items) {
			p_58342_.accountStack(itemstack);
		}

	}

	LazyOptional<? extends net.minecraftforge.items.IItemHandler>[] handlers = SidedInvWrapper.create(this,
			Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH);

	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
		if (!this.remove && facing != null
				&& capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			switch (facing) {
			case UP:
				return handlers[0].cast();
			case DOWN:
				return handlers[1].cast();
			case EAST:
				return handlers[3].cast();
			case SOUTH:
				return handlers[4].cast();
			default:
				return handlers[2].cast();
			}
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		for (int x = 0; x < handlers.length; x++) {
			handlers[x].invalidate();
		}
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		this.handlers = net.minecraftforge.items.wrapper.SidedInvWrapper.create(this, Direction.UP, Direction.DOWN,
				Direction.NORTH, Direction.EAST, Direction.SOUTH);
	}

	@Override
	protected Component getDefaultName() {
		return new TranslatableComponent("container.hemomancy.transfuser");
	}

	@Override
	protected AbstractContainerMenu createMenu(int p_58627_, Inventory p_58628_) {
		return new MenuEarthlyTransfuser(p_58627_, p_58628_, this, this.dataAccess);
	}

}
