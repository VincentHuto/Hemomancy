package com.vincenthuto.hemomancy.common.tile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.recipe.PolypRecipes;
import com.vincenthuto.hemomancy.common.recipe.RecipePolyp;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.common.block.entity.SimpleInventoryBlockEntity;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MorphlingIncubatorBlockEntity extends SimpleInventoryBlockEntity implements IBloodTile {

	static final String TAG_BLOOD_LEVEL = "bloodLevel";
	private static final int CRAFT_TIME = 200;

	int spinningProgress;
	int spinningTotalTime;
	int canSpin;

	public final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> spinningProgress;
				case 1 -> spinningTotalTime;
				case 2 -> canSpin;
				default -> 0;
			};
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public void set(int index, int val) {
			switch (index) {
				case 0 -> spinningProgress = val;
				case 1 -> spinningTotalTime = val;
				case 2 -> canSpin = val;
			}
		}
	};

	public MorphlingIncubatorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.morphling_incubator.get(), pos, state, 5);
	}

	// ---- Lazy capability access (avoids crash in field initializer) ----

	@Nullable
	private IBloodVolume resolveVolume() {
		return getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
	}

	// ---- Ticking ----

	public static void clientTick(Level level, BlockPos worldPosition, BlockState state,
			MorphlingIncubatorBlockEntity self) {
	}

	@Override
	public void onLoad() {
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			vol.setActive(true);
			vol.setMaxBloodVolume(2000f);
		}
	}

	// ---- Blood container handling ----

	@Override
	public boolean addItem(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {
		IBloodVolume vol = resolveVolume();

		// Blood flask handling — fill the block's blood volume
		if (stack.getItem() instanceof BloodyFlaskItem flask) {
			if (vol != null && !vol.isFull()) {
				float amount = flask.getAmount();
				vol.addBloodVolume(amount);
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
			if (vol != null && gourdVolume != null && gourdVolume.getBloodVolume() > 0 && !vol.isFull()) {
				double transfer = Math.min(gourdVolume.getBloodVolume(), vol.getMaxBloodVolume() - vol.getBloodVolume());
				if (transfer > 0) {
					gourdVolume.subtractBloodVolume(transfer);
					vol.addBloodVolume(transfer);
					sendUpdates();
					VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
					return true;
				}
			}
			return false;
		}

		// Everything else — delegate to the base inventory logic
		return super.addItem(player, stack, hand);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState blockState, MorphlingIncubatorBlockEntity te) {
		if (te.spinningProgress > 0) {
			te.spinningProgress--;
			te.sendUpdates();
			setChanged(level, pos, blockState);
			HLParticleUtils.spawnPoof((ServerLevel) level, pos, ParticleTypes.MYCELIUM);

			if (te.spinningProgress <= 1) {
				HLParticleUtils.spawnPoof((ServerLevel) level, pos, ParticleTypes.REVERSE_PORTAL);
				te.outputResults();
			}
		}
	}

	// ---- Inventory helpers ----

	public ItemStack getCenterStack() {
		return inventory.get(0);
	}

	public List<ItemStack> getCatalystStacks() {
		return inventory.subList(1, inventory.size());
	}

	/**
	 * Collects all non-empty catalyst items (slots 1-4) for recipe matching.
	 */
	private List<Item> collectCatalystItems() {
		List<Item> items = new ArrayList<>();
		for (int i = 1; i < inventory.size(); i++) {
			ItemStack stack = inventory.get(i);
			if (!stack.isEmpty()) {
				items.add(stack.getItem());
			}
		}
		return items;
	}

	// ---- Recipe matching ----

	/**
	 * Finds the matching PolypRecipe for the current inventory contents.
	 * Slot 0 must contain a morphling polyp. Slots 1-4 supply the catalyst ingredients.
	 */
	@Nullable
	public RecipePolyp findMatchingRecipe() {
		ItemStack center = getCenterStack();
		if (center.isEmpty() || center.getItem() != ItemInit.morphling_polyp.get()) {
			return null;
		}
		List<Item> catalysts = collectCatalystItems();
		if (catalysts.isEmpty()) {
			return null;
		}
		for (RecipePolyp recipe : PolypRecipes.POLYPRECIPES) {
			if (catalysts.size() == recipe.getIngr().size() && catalysts.containsAll(recipe.getIngr())) {
				return recipe;
			}
		}
		return null;
	}

	// ---- Crafting ----

	/**
	 * Attempts to start incubation. Validates that a recipe match exists before
	 * beginning the crafting timer.
	 *
	 * @return true if crafting began, false otherwise
	 */
	public boolean attemptStartup() {
		if (spinningProgress > 0) {
			return false;
		}
		RecipePolyp recipe = findMatchingRecipe();
		if (recipe != null) {
			spinningProgress = CRAFT_TIME;
			spinningTotalTime = CRAFT_TIME;
			sendUpdates();
			return true;
		}
		return false;
	}

	/**
	 * Called when the crafting timer reaches zero. Matches the recipe again (in case
	 * contents changed mid-craft), clears consumed ingredients, and drops the result.
	 */
	private void outputResults() {
		if (spinningProgress > 1) return;

		RecipePolyp recipe = findMatchingRecipe();
		if (recipe == null) {
			// Recipe no longer valid — abort, keep items
			spinningProgress = 0;
			sendUpdates();
			return;
		}

		// Consume all slots
		for (int i = 0; i < inventory.size(); i++) {
			inventory.set(i, ItemStack.EMPTY);
		}

		// Spawn result entity
		double dx = Mth.randomBetween(level.random, -0.2F, 0.2F);
		double dy = Mth.randomBetween(level.random, -0.2F, 0.2F);
		double dz = Mth.randomBetween(level.random, -0.2F, 0.2F);
		ItemStack result = new ItemStack(recipe.getOutput());
		getLevel().addFreshEntity(new ItemEntity(level,
				worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5,
				result, dx, dy, dz));

		spinningProgress = 0;
		sendUpdates();
	}

	// ---- Serialization ----

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		this.spinningProgress = tag.getInt("SpinTime");
		this.spinningTotalTime = tag.getInt("SpinTimeTotal");
	}

	@Override
	public void load(CompoundTag pTag) {
		super.load(pTag);
		this.spinningProgress = pTag.getInt("SpinTime");
		this.spinningTotalTime = pTag.getInt("SpinTimeTotal");
		IBloodVolume vol = resolveVolume();
		if (vol != null && pTag.contains(TAG_BLOOD_LEVEL)) {
			vol.setBloodVolume(pTag.getFloat(TAG_BLOOD_LEVEL));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag pTag) {
		super.saveAdditional(pTag);
		pTag.putInt("SpinTime", this.spinningProgress);
		pTag.putInt("SpinTimeTotal", this.spinningTotalTime);
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			pTag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
		}
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		tag.putInt("SpinTime", this.spinningProgress);
		tag.putInt("SpinTimeTotal", this.spinningTotalTime);
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
		}
		return tag;
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		if (pkt.getTag() != null) {
			CompoundTag tag = pkt.getTag();
			IBloodVolume vol = resolveVolume();
			if (vol != null) {
				vol.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			}
		}
	}

	// ---- Blood access ----

	@Nullable
	public IBloodVolume getBloodCapability() {
		return resolveVolume();
	}

	public double getBloodVolume() {
		IBloodVolume vol = resolveVolume();
		return vol != null ? vol.getBloodVolume() : 0;
	}

	public double getMaxBloodVolume() {
		IBloodVolume vol = resolveVolume();
		return vol != null ? vol.getMaxBloodVolume() : 0;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.hemomancy.morphlingincubator");
	}

}
