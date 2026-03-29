package com.vincenthuto.hemomancy.common.tile;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.world.phys.AABB;

import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.item.EnzymeItem;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.recipe.RecallerRecipe;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VisceralRecallerBlockEntity extends BlockEntity implements IBloodTile {
	public static final String TAG_BLOOD_LEVEL = "bloodLevel";
	public static final String TAG_BLOOD_TENDENCY = "tendency";
	public static final String TAG_RECIPE = "recipe";

	public static void clientTick(Level level, BlockPos worldPosition, BlockState state,
			VisceralRecallerBlockEntity self) {
	}

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state,
			VisceralRecallerBlockEntity self) {
	}

	public NonNullList<ItemStack> contents = NonNullList.<ItemStack>withSize(2, ItemStack.EMPTY);
	String recipePath = "";
	IBloodVolume volume = getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);

	IBloodTendency tendency = getCapability(BloodTendencyProvider.TENDENCY_CAPA)
			.orElseThrow(IllegalStateException::new);

	RecallerRecipe curRecipe = null;

	public VisceralRecallerBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.visceral_artificial_recaller.get(), pos, state);
	}

	@Override
	public AABB getRenderBoundingBox() {
		// Effects render up to ~3 blocks above and ~4 blocks out horizontally.
		// Expand the culling AABB so Minecraft never frustum-culls the renderer
		// while the effects are still in view.
		BlockPos pos = getBlockPos();
		return new AABB(pos).inflate(5.0, 5.0, 5.0);
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

		// Blood container handling — fill the block's blood volume
		if (stack.getItem() instanceof BloodyFlaskItem flask) {
			if (!volume.isFull()) {
				float amount = flask.getAmount();
				volume.addBloodVolume(amount);
				if (player == null || !player.getAbilities().instabuild) {
					stack.shrink(1);
					if (stack.isEmpty() && player != null) {
						player.setItemInHand(hand, ItemStack.EMPTY);
					}
				}
				sendUpdates();
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
				return true;
			}
			return false;
		}

		// Blood gourd handling — drain blood from the gourd into the block
		if (stack.getItem() instanceof BloodGourdItem) {
			IBloodVolume gourdVolume = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
			if (gourdVolume != null && gourdVolume.getBloodVolume() > 0 && !volume.isFull()) {
				double transfer = Math.min(gourdVolume.getBloodVolume(), volume.getMaxBloodVolume() - volume.getBloodVolume());
				if (transfer > 0) {
					gourdVolume.subtractBloodVolume(transfer);
					volume.addBloodVolume(transfer);
					sendUpdates();
					VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
					return true;
				}
			}
			return false;
		}

		if (stack.getItem() instanceof EnzymeItem) {
			ItemStack enzymeStack = stack.copy();
			if (enzymeStack.getItem() instanceof EnzymeItem) {
				EnzymeItem enzyme = (EnzymeItem) enzymeStack.getItem();
				if (getTendency().get(enzyme.getTend()) < 1f) {
					tendency.addTendencyAlignment(enzyme.getTend(), 0.2f);
					stack.shrink(1);
				}
				// Adds a recycled chance
				if (level.random.nextInt(20) % 7 == 0) {
					ItemEntity recycl = new ItemEntity(level, getBlockPos().getX(), getBlockPos().getY(),
							getBlockPos().getZ(), new ItemStack(ItemInit.recycled_enzyme.get()));
					level.addFreshEntity(recycl);
				}
			}
			checkRecipe();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}

		// Memory Slot add (slot 0)
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
		}

		// Item Slot add (slot 1)
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

		return false;
	}

	/**
	 * Removes items when the player interacts with an empty hand.
	 * Shift-click removes from slot 0 (memory), normal click removes from slot 1 (item).
	 */
	public boolean removeItem(@Nullable Player player, boolean isCrouching) {
		if (player == null) return false;

		// Shift-click: remove memory (slot 0)
		if (isCrouching && !contents.get(0).isEmpty()) {
			ItemStack copy = contents.get(0).copy();
			player.getInventory().placeItemBackInInventory(copy);
			contents.set(0, ItemStack.EMPTY);
			checkRecipe();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}

		// Normal click: remove item (slot 1)
		if (!isCrouching && !contents.get(1).isEmpty()) {
			ItemStack copy = contents.get(1).copy();
			player.getInventory().placeItemBackInInventory(copy);
			contents.set(1, ItemStack.EMPTY);
			checkRecipe();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}

		return false;
	}

	/**
	 * Drops all contents as item entities. Called when the block is broken.
	 */
	public void dropContents() {
		if (level != null && !level.isClientSide) {
			for (ItemStack stack : contents) {
				if (!stack.isEmpty()) {
					Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
				}
			}
			contents.clear();
		}
	}

	public void clearTendency() {
		getTendCapability().setTendency(RecallerRecipe.blank());
		checkRecipe();
		sendUpdates();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

	private void checkRecipe() {
		this.tendency.getTendency();
		for (RecallerRecipe recipe : RecallerRecipe.getAllRecipes(level)) {
			if (recipe.getTendency().equals(this.tendency.getTendency())
					&& recipe.getIngredient().test(contents.get(1))) {
				curRecipe = recipe;
				recipePath = HLTextUtils.getItemRegistryName(recipe.getResultItem(level.registryAccess()).getItem());
				break;
			} else {
				recipePath = "";
				curRecipe = null;
			}
		}
	}

	public IBloodVolume getBloodCapability() {
		return volume;
	}

	public double getBloodVolume() {
		return volume.getBloodVolume();
	}

	public RecallerRecipe getCurRecipe() {
		return curRecipe;
	}

	public double getMaxBloodVolume() {
		return volume.getMaxBloodVolume();
	}

	public ItemStack getResultItem() {
		return curRecipe != null ? curRecipe.getResultItem(level.registryAccess()) : ItemStack.EMPTY;
	}

	public IBloodTendency getTendCapability() {
		return tendency;
	}

	public Map<EnumBloodTendency, Float> getTendency() {
		return tendency.getTendency();
	}

	public boolean hasValidRecipe() {
		return curRecipe != null;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
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

	// NBT
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.contents = NonNullList.withSize(2, ItemStack.EMPTY);
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
	public void onLoad() {
		volume.setActive(true);
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
	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

}

