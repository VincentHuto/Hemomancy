package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.menu.tile.functional.MasonsEffigyMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class MasonsEffigyBlockEntity extends BlockEntity implements MenuProvider {
	private static final String TAG_STATE = "EffigyState";
	private static final String TAG_SELECTED_SCARS = "SelectedScars";
	private static final String TAG_PENDING_MOTIF = "PendingMotif";
	private static final String TAG_PENDING_BLOOD = "PendingBlood";
	public static final double BLOOD_PER_SCAR = 500.0D;
	private EffigyState effigyState = EffigyState.UNMARKED;
	private final List<ResourceLocation> selectedScarIds = new ArrayList<>();
	private ItemStack pendingMotif = ItemStack.EMPTY;
	private double pendingBlood;

	public enum EffigyState {
		UNMARKED,
		MARKED,
		INSCRIBED,
		INTERTWINED,
		FRUITING
	}

	public MasonsEffigyBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.mason_effigy.get(), pos, state);
	}

	public EffigyState getEffigyState() {
		return effigyState;
	}

	public List<ResourceLocation> getSelectedScarIds() {
		return List.copyOf(selectedScarIds);
	}

	public void setSelectedScarIds(Collection<ResourceLocation> scarIds) {
		selectedScarIds.clear();
		for (ResourceLocation id : scarIds) {
			if (id != null && !selectedScarIds.contains(id)
					&& selectedScarIds.size() < MasonsEffigyMenu.MAX_SELECTED_SCARS) {
				selectedScarIds.add(id);
			}
		}
		markDirtyAndSync();
	}

	public boolean hasPendingMotif() {
		return !pendingMotif.isEmpty();
	}

	public ItemStack getPendingMotifDisplayStack() {
		return pendingMotif.copy();
	}

	public double getPendingBlood() {
		return pendingBlood;
	}

	public double getRequiredBlood() {
		return selectedScarIds.size() * BLOOD_PER_SCAR;
	}

	public boolean beginMotifRitual(Player player, ItemStack stack) {
		if (level == null || selectedScarIds.isEmpty() || hasPendingMotif()) {
			return false;
		}
		pendingMotif = new ItemStack(ItemInit.runic_motif_paper.get());
		pendingBlood = 0.0D;
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		markDirtyAndSync();
		return true;
	}

	public boolean tryReceiveProjectedBlood(Player player, double maxAmount, boolean livingStaff) {
		return receiveProjectedBlood(player, maxAmount, livingStaff) > 0.0D;
	}

	public double receiveProjectedBlood(Player player, double maxAmount, boolean livingStaff) {
		if (player == null || level == null || !hasPendingMotif() || selectedScarIds.isEmpty()) {
			if (player != null && level != null && level.getGameTime() % 20L == 0L) {
				player.displayClientMessage(Component.literal(!hasPendingMotif()
						? "Place motif paper on the Effigy before projecting blood."
						: "Select scars on the Effigy before charging the motif.").withStyle(ChatFormatting.RED), true);
			}
			return 0.0D;
		}
		IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (playerVolume == null || !playerVolume.isActive() || playerVolume.getBloodVolume() <= 0) {
			if (level.getGameTime() % 20L == 0L) {
				player.displayClientMessage(Component.literal("No blood answers the Effigy.")
						.withStyle(ChatFormatting.DARK_RED), true);
			}
			return 0.0D;
		}
		double needed = Math.max(0.0D, getRequiredBlood() - pendingBlood);
		if (needed <= 0.0D) {
			completeMotifRitual();
			return 0.0D;
		}
		double amount = Math.min(Math.min(maxAmount, needed), playerVolume.getBloodVolume());
		if (amount <= 0.0D) {
			return 0.0D;
		}
		playerVolume.subtractBloodVolume(amount);
		playerVolume.addBloodSpend(amount);
		pendingBlood += amount;
		if (livingStaff && level.getGameTime() % 8L == 0L) {
			level.playSound(null, worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.25F, 0.75F);
		}
		if (pendingBlood >= getRequiredBlood() - 0.001D) {
			completeMotifRitual();
			player.displayClientMessage(Component.literal("The Effigy casts off a completed scar pattern.")
					.withStyle(ChatFormatting.DARK_RED), true);
		} else {
			if (level.getGameTime() % 10L == 0L) {
				player.displayClientMessage(Component.literal("Effigy blood charge: " + (int) pendingBlood + "/"
						+ (int) getRequiredBlood()).withStyle(ChatFormatting.DARK_RED), true);
			}
			markDirtyAndSync();
		}
		return amount;
	}

	private void completeMotifRitual() {
		ItemStack pattern = ItemScarPattern.createPreparedPattern(selectedScarIds);
		pendingMotif = ItemStack.EMPTY;
		pendingBlood = 0.0D;
		if (level instanceof ServerLevel serverLevel && !pattern.isEmpty()) {
			ItemEntity patternEntity = new ItemEntity(serverLevel, worldPosition.getX() + 0.5D,
					worldPosition.getY() + 1.25D, worldPosition.getZ() + 0.5D, pattern);
			double angle = serverLevel.random.nextDouble() * Math.PI * 2.0D;
			patternEntity.setDeltaMovement(Math.cos(angle) * 0.16D, 0.36D, Math.sin(angle) * 0.16D);
			patternEntity.setDefaultPickUpDelay();
			level.addFreshEntity(patternEntity);
			HLParticleUtils.spawnPoof(serverLevel, worldPosition.above(),
					BloodCellParticleFactory.createData(ParticleColor.BLOOD));
			PacketHandler.sendSanguineOmenEffect(Vec3.atCenterOf(worldPosition).add(0.0D, 0.25D, 0.0D), 18.0D,
					serverLevel, 24, 0.35F);
			level.playSound(null, worldPosition, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.8F, 0.65F);
		}
		markDirtyAndSync();
	}

	public void updateStateFor(Player player) {
		int known = HemoCapabilityAccess.getScarState(player).map(scars -> scars.getKnownCerebralScars().size()).orElse(0);
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		EffigyState next = EffigyState.UNMARKED;
		if (degree >= 6 && known >= 4) {
			next = EffigyState.FRUITING;
		} else if (known >= 4) {
			next = EffigyState.INTERTWINED;
		} else if (known >= 2) {
			next = EffigyState.INSCRIBED;
		} else if (known >= 1) {
			next = EffigyState.MARKED;
		}
		if (next != effigyState) {
			effigyState = next;
			markDirtyAndSync();
		}
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.hemomancy.mason_effigy");
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		List<ResourceLocation> known = HemoCapabilityAccess.getScarState(player)
				.map(scars -> sortedIds(scars.getKnownCerebralScars()))
				.orElseGet(List::of);
		List<ResourceLocation> active = HemoCapabilityAccess.getScarState(player)
				.map(scars -> sortedIds(scars.getActiveCerebralScars()))
				.orElseGet(List::of);
		return new MasonsEffigyMenu(id, inventory, worldPosition, known, active);
	}

	@Override
	public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
		if (menu instanceof MasonsEffigyMenu effigyMenu) {
			writeIds(buffer, effigyMenu.getOpeningKnownScarIds());
			writeIds(buffer, effigyMenu.getOpeningActiveScarIds());
		} else {
			writeIds(buffer, List.of());
			writeIds(buffer, List.of());
		}
	}

	private static List<ResourceLocation> sortedIds(Collection<ResourceLocation> ids) {
		List<ResourceLocation> sorted = new ArrayList<>(ids);
		sorted.sort(Comparator.comparing(ResourceLocation::toString));
		return sorted;
	}

	private static void writeIds(RegistryFriendlyByteBuf buffer, List<ResourceLocation> ids) {
		buffer.writeVarInt(ids.size());
		for (ResourceLocation id : ids) {
			buffer.writeResourceLocation(id);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putString(TAG_STATE, effigyState.name());
		ListTag selected = new ListTag();
		for (ResourceLocation id : selectedScarIds) {
			selected.add(StringTag.valueOf(id.toString()));
		}
		tag.put(TAG_SELECTED_SCARS, selected);
		if (!pendingMotif.isEmpty()) {
			tag.put(TAG_PENDING_MOTIF, pendingMotif.save(registries));
		}
		tag.putDouble(TAG_PENDING_BLOOD, pendingBlood);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		if (tag.contains(TAG_STATE)) {
			try {
				effigyState = EffigyState.valueOf(tag.getString(TAG_STATE));
			} catch (IllegalArgumentException ignored) {
				effigyState = EffigyState.UNMARKED;
			}
		}
		selectedScarIds.clear();
		ListTag selected = tag.getList(TAG_SELECTED_SCARS, Tag.TAG_STRING);
		for (int i = 0; i < selected.size() && selectedScarIds.size() < MasonsEffigyMenu.MAX_SELECTED_SCARS; i++) {
			ResourceLocation id = ResourceLocation.tryParse(selected.getString(i));
			if (id != null && !selectedScarIds.contains(id)) {
				selectedScarIds.add(id);
			}
		}
		pendingMotif = tag.contains(TAG_PENDING_MOTIF)
				? ItemStack.parseOptional(registries, tag.getCompound(TAG_PENDING_MOTIF))
				: ItemStack.EMPTY;
		pendingBlood = tag.getDouble(TAG_PENDING_BLOOD);
	}

	private void markDirtyAndSync() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
