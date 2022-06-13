package com.vincenthuto.hemomancy.tile;

import java.util.Map;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.capa.player.tendency.BloodTendencyProvider;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.player.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.container.MenuVisceralRecaller;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.item.ItemEnzyme;
import com.vincenthuto.hemomancy.recipe.RecallerRecipe;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockEntityVisceralRecaller extends BaseContainerBlockEntity implements MenuProvider, IBloodTile {
	public NonNullList<ItemStack> contents = NonNullList.<ItemStack>withSize(5, ItemStack.EMPTY);
	public static final String TAG_BLOOD_LEVEL = "bloodLevel";
	public static final String TAG_BLOOD_TENDENCY = "tendency";
	public static final String TAG_RECIPE = "recipe";
	String recipePath = "";
	IBloodVolume volume = getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);
	IBloodTendency tendency = getCapability(BloodTendencyProvider.TENDENCY_CAPA)
			.orElseThrow(IllegalStateException::new);
	RecallerRecipe curRecipe = null;

	public BlockEntityVisceralRecaller(BlockPos pos, BlockState state) {
		super(BlockEntityInit.visceral_artificial_recaller.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state,
			BlockEntityVisceralRecaller self) {
	}

	public static void clientTick(Level level, BlockPos worldPosition, BlockState state,
			BlockEntityVisceralRecaller self) {
	}

	public RecallerRecipe getCurRecipe() {
		return curRecipe;
	}

	public ItemStack getResultItem() {
		return curRecipe != null ? curRecipe.getResultItem() : ItemStack.EMPTY;
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

	public IBloodTendency getTendCapability() {
		return tendency;
	}

	public Map<EnumBloodTendency, Float> getTendency() {
		return tendency.getTendency();
	}

	// NBT
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.contents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, this.contents);
		if (tag != null) {
			recipePath = tag.getString(TAG_RECIPE);
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			}
		}
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		ContainerHelper.saveAllItems(tag, this.contents);
		if (tag != null) {
			tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
			tag.putString(TAG_RECIPE, recipePath);

			for (EnumBloodTendency key : tendency.getTendency().keySet()) {
				if (tendency.getTendency().get(key) != null) {
					tag.putFloat(key.toString(), tendency.getTendency().get(key));
				} else {
					tag.putFloat(key.toString(), 0);
				}
			}
		}

	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		if (tag != null) {
			recipePath = tag.getString(TAG_RECIPE);
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			}
		}
	}

	@Override
	public final CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, this.contents);
		tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		tag.putString(TAG_RECIPE, recipePath);
		for (EnumBloodTendency key : tendency.getTendency().keySet()) {
			if (tendency.getTendency().get(key) != null) {
				tag.putFloat(key.toString(), tendency.getTendency().get(key));
			} else {
				tag.putFloat(key.toString(), 0);
			}
		}
		return tag;
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		if (pkt.getTag() != null) {
			CompoundTag tag = pkt.getTag();
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			recipePath = tag.getString(TAG_RECIPE);
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			}
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.visceral_recaller");
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory player) {
		return new MenuVisceralRecaller(id, player, this);
	}

	@Override
	public int getContainerSize() {
		return this.contents.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.contents) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int p_58328_) {
		return this.contents.get(p_58328_);
	}

	@Override
	public ItemStack removeItem(int pIndex, int pCount) {
		return ContainerHelper.removeItem(this.contents, pIndex, pCount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int pIndex) {
		return ContainerHelper.takeItem(this.contents, pIndex);
	}

	@Override
	public boolean stillValid(Player p_58340_) {
		return (this.level.getBlockEntity(this.worldPosition) != this) ? false
				: p_58340_.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D,
						this.worldPosition.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void clearContent() {
		this.contents.clear();
	}

	private void checkRecipe() {
		// this.tendency.getTendency().forEach((key, value) -> System.out.println(key +
		// ":" + value));
		this.tendency.getTendency();
		for (RecallerRecipe recipe : RecallerRecipe.getAllRecipes(level)) {
			if (recipe.getTendency().equals(this.tendency.getTendency())
					&& recipe.getIngredient().test(contents.get(1))) {
				// System.out.println("Matching Recipe: " + recipe);
				curRecipe = recipe;
				recipePath = ForgeRegistries.ITEMS.getKey( recipe.getResultItem().getItem()).getPath();
				break;
			} else {
				// System.out.println("No matching recipe found!");
				recipePath = "";
				curRecipe = null;
			}
		}
		// System.out.println(curRecipe);
	}

	public boolean hasValidRecipe() {
		return curRecipe != null;
	}

	public boolean addItem(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {

		if (stack.getItem() == ItemInit.lethian_dew.get()) {
			if (player == null || !player.getAbilities().instabuild) {
				stack.shrink(1);
				if (stack.isEmpty() && player != null) {
					player.setItemInHand(hand, ItemStack.EMPTY);
				}

			}
			getTendCapability().setTendency(RecallerRecipe.blank());
			checkRecipe();
			sendUpdates();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;

		}

		if (stack.getItem() instanceof ItemEnzyme) {
			ItemStack enzymeStack = stack.copy();
			if (enzymeStack.getItem() instanceof ItemEnzyme) {
				ItemEnzyme enzyme = (ItemEnzyme) enzymeStack.getItem();
				if (getTendency().get(enzyme.getTend()) < 1f) {
					tendency.addTendencyAlignment(enzyme.getTend(), 0.2f);
					stack.shrink(1);
				}
				// Adds a recycled chance
				if (contents.get(3).isEmpty()) {
					if (level.random.nextInt(20) % 7 == 0) {
						// contents.set(3, new ItemStack(ItemInit.recycled_enzyme.get()));
						ItemEntity recycl = new ItemEntity(level, getBlockPos().getX(), getBlockPos().getY(),
								getBlockPos().getZ(), new ItemStack(ItemInit.recycled_enzyme.get()));
						level.addFreshEntity(recycl);
					}
				}
			}
			checkRecipe();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}
		// Memory Slot add
		if (contents.get(0).isEmpty() && stack.getItem() == ItemInit.hematic_memory.get()) {
			ItemStack stackToAdd = stack.copy();
			stackToAdd.setCount(1);
			contents.set(0, stackToAdd);
			if (player == null || !player.getAbilities().instabuild) {
				stack.shrink(1);
				if (stack.isEmpty() && player != null) {
					player.setItemInHand(hand, ItemStack.EMPTY);
				}

			}
			checkRecipe();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;

		} // Item Slot add
		if (contents.get(1).isEmpty() && stack.getItem() != ItemInit.hematic_memory.get() && !stack.isEmpty()) {
			ItemStack stackToAdd = stack.copy();
			stackToAdd.setCount(1);
			contents.set(1, stackToAdd);
			if (player == null || !player.getAbilities().instabuild) {
				stack.shrink(1);
				if (stack.isEmpty() && player != null) {
					player.setItemInHand(hand, ItemStack.EMPTY);
				}

			}
			checkRecipe();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}
		// Remove item
		if (stack.isEmpty() && !contents.get(1).isEmpty()) {
			ItemStack copy = contents.get(1).copy();
			player.getInventory().placeItemBackInInventory(copy);
			contents.set(1, ItemStack.EMPTY);
			checkRecipe();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}
		// remove memory
		if (stack.isEmpty() && !contents.get(0).isEmpty()) {
			ItemStack copy = contents.get(0).copy();
			player.getInventory().placeItemBackInInventory(copy);
			contents.set(0, ItemStack.EMPTY);
			checkRecipe();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}

		else {
			return false;
		}

	}

	@Override
	public void setItem(int pIndex, ItemStack pStack) {
		// TODO Auto-generated method stub

	}

}