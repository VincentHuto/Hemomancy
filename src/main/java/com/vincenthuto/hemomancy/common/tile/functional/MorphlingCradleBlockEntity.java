package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MorphlingCradleBlockEntity extends BlockEntity {

	public enum State {
		ACTIVE, DORMANT
	}

	private enum MaturityStage {
		POLYP,
		JUVENILE,
		MATURE,
		PRIMAL;

		static MaturityStage fromStack(ItemStack stack) {
			int level = MorphlingItem.getMaturityLevel(stack);
			if (level >= 5) return PRIMAL;
			if (level >= 4) return MATURE;
			if (level >= 2) return JUVENILE;
			return POLYP;
		}
	}

	private static final int SWAP_COOLDOWN_TICKS = 40;
	private static final double OWNER_DRAIN_RANGE = 24.0;
	private static final double BLOODLINE_FALLBACK_MULT = 1.5;

	private static final double INTERNAL_BUFFER_MAX = 800.0;

	private static final double[] UPKEEP_COST = new double[] { 0.35, 0.7, 1.1, 1.8 };
	private static final double[] ACTION_COST = new double[] { 35.0, 60.0, 95.0, 150.0 };
	private static final int[] ACTION_INTERVAL = new int[] { 30, 24, 18, 14 };

	private static final double[] SUPPORT_AURA_RANGE = new double[] { 8.0, 11.0, 15.0, 20.0 };

	private static final int[] STAGE_AURA_INTERVAL = new int[] { 12, 8, 6, 4 };
	private static final double[] STAGE_AURA_RANGE = new double[] { 4.0, 7.0, 10.0, 14.0 };
	private static final float[] STAGE_DRAIN_PER_HIT = new float[] { 1.5f, 3.0f, 5.0f, 7.0f };
	private static final double[] STAGE_BUFFER_MAX = new double[] { 200.0, 600.0, 1200.0, 2000.0 };
	private static final double[] STAGE_DISTRIBUTION_RANGE = new double[] { 6.0, 10.0, 28.0, 40.0 };

	private ItemStack morphlingItem = ItemStack.EMPTY;
	private UUID ownerUUID = null;
	private State state = State.DORMANT;
	private int activationCooldown = 0;

	private double bloodBuffer = 0.0;
	private double internalBlood = 0.0;

	public MorphlingCradleBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.morphling_cradle.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState blockState, MorphlingCradleBlockEntity cradle) {
		cradle.tickServer();
	}

	public InteractionResult handleInteraction(Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		boolean holdingMorphling = held.getItem() instanceof MorphlingItem;

		if (morphlingItem.isEmpty()) {
			if (!holdingMorphling) return InteractionResult.PASS;
			return insertMorphling(player, hand);
		}

		if (!isOwner(player)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.morphling_cradle.owner_only"), true);
			return InteractionResult.CONSUME;
		}

		if (holdingMorphling && !ItemStack.isSameItemSameComponents(held, morphlingItem)) {
			return swapMorphling(player, hand);
		}

		if (held.isEmpty()) {
			return removeMorphling(player);
		}

		return InteractionResult.CONSUME;
	}

	public ItemStack removeMorphlingForDrop() {
		ItemStack copy = morphlingItem.copy();
		morphlingItem = ItemStack.EMPTY;
		ownerUUID = null;
		state = State.DORMANT;
		activationCooldown = 0;
		setChangedAndSync();
		return copy;
	}

	public ItemStack getHostedMorphling() {
		return morphlingItem;
	}

	public double receiveExternalBlood(double amount) {
		if (amount <= 0) return 0;
		double space = Math.max(0, INTERNAL_BUFFER_MAX - internalBlood);
		double accepted = Math.min(space, amount);
		if (accepted > 0) {
			internalBlood += accepted;
			setChanged();
		}
		return accepted;
	}

	private InteractionResult insertMorphling(Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		if (!(held.getItem() instanceof MorphlingItem)) return InteractionResult.PASS;
		ItemStack inserted = held.copy();
		inserted.setCount(1);
		morphlingItem = inserted;
		ownerUUID = player.getUUID();
		state = State.DORMANT;
		activationCooldown = 0;
		if (!player.getAbilities().instabuild) {
			held.shrink(1);
		}
		setChangedAndSync();
		return InteractionResult.CONSUME;
	}

	private InteractionResult removeMorphling(Player player) {
		if (morphlingItem.isEmpty()) return InteractionResult.PASS;
		ItemStack removed = morphlingItem.copy();
		morphlingItem = ItemStack.EMPTY;
		ownerUUID = null;
		state = State.DORMANT;
		activationCooldown = 0;
		if (!player.addItem(removed)) {
			player.drop(removed, false);
		}
		setChangedAndSync();
		return InteractionResult.CONSUME;
	}

	private InteractionResult swapMorphling(Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		if (!(held.getItem() instanceof MorphlingItem)) return InteractionResult.PASS;
		ItemStack outgoing = morphlingItem.copy();
		ItemStack incoming = held.copy();
		incoming.setCount(1);
		morphlingItem = incoming;
		state = State.DORMANT;
		activationCooldown = SWAP_COOLDOWN_TICKS;
		if (!player.getAbilities().instabuild) {
			held.shrink(1);
		}
		if (!player.addItem(outgoing)) {
			player.drop(outgoing, false);
		}
		setChangedAndSync();
		return InteractionResult.CONSUME;
	}

	private void tickServer() {
		if (level == null || level.isClientSide) return;

		if (morphlingItem.isEmpty() || ownerUUID == null) {
			setState(State.DORMANT);
			return;
		}

		if (activationCooldown > 0) {
			activationCooldown--;
			setState(State.DORMANT);
			setChanged();
			return;
		}

		MaturityStage stage = MaturityStage.fromStack(morphlingItem);
		int idx = stage.ordinal();

		if (stage == MaturityStage.POLYP) {
			setState(State.ACTIVE);
			runLeechAura(idx);
			distributeLeechBlood(idx);
			if (level.getGameTime() % ACTION_INTERVAL[idx] == 0) {
				runMorphlingAura(idx);
			}
			return;
		}

		if (!consumeOperationalBlood(UPKEEP_COST[idx])) {
			setState(State.DORMANT);
			return;
		}

		setState(State.ACTIVE);
		if (level.getGameTime() % ACTION_INTERVAL[idx] == 0) {
			if (!consumeOperationalBlood(ACTION_COST[idx])) {
				setState(State.DORMANT);
				return;
			}
			runMorphlingAura(idx);
		}
	}

	private void runMorphlingAura(int stageIndex) {
		if (!(morphlingItem.getItem() instanceof IMorphling morphling) || ownerUUID == null) {
			return;
		}

		Set<UUID> allowedRecipients = new HashSet<>();
		allowedRecipients.add(ownerUUID);
		if (level instanceof ServerLevel serverLevel) {
			BloodlineSavedData data = BloodlineSavedData.get(serverLevel.getServer().overworld());
			Bloodline line = data.getBloodlineForPlayer(ownerUUID);
			if (line != null && line.isValid()) {
				allowedRecipients.addAll(line.getPlayerUUIDS());
			}
		}

		double range = SUPPORT_AURA_RANGE[stageIndex];
		AABB area = new AABB(worldPosition).inflate(range);
		List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, area, Player::isAlive);
		for (Player nearby : nearbyPlayers) {
			if (allowedRecipients.contains(nearby.getUUID())) {
				morphling.onEquippedTick(nearby, morphlingItem);
			}
		}
	}

	private void runLeechAura(int stageIdx) {
		int interval = STAGE_AURA_INTERVAL[stageIdx];
		if (interval <= 0 || level.getGameTime() % interval != 0) return;

		double range = STAGE_AURA_RANGE[stageIdx];
		float drainPerHit = STAGE_DRAIN_PER_HIT[stageIdx];
		boolean canTargetPlayers = HemoServerConfig.MORPHLING_CRADLE_LEECH_TARGET_PLAYERS.get();

		List<LivingEntity> targets = getTargets(range, canTargetPlayers);
		for (LivingEntity target : targets) {
			if (target.hurt(level.damageSources().magic(), drainPerHit)) {
				double cap = STAGE_BUFFER_MAX[stageIdx];
				bloodBuffer = Mth.clamp(bloodBuffer + drainPerHit, 0, cap);
			}
		}
		if (!targets.isEmpty()) setChanged();
	}

	private void distributeLeechBlood(int stageIdx) {
		if (bloodBuffer <= 0) return;

		double range = STAGE_DISTRIBUTION_RANGE[stageIdx];
		if (range > 0) {
			for (BlockPos p : BlockPos.betweenClosed(
					worldPosition.offset((int) -range, (int) -range, (int) -range),
					worldPosition.offset((int) range, (int) range, (int) range))) {
				if (bloodBuffer <= 0) break;
				BlockEntity be = level.getBlockEntity(p);
				if (!(be instanceof MorphlingCradleBlockEntity other)) continue;
				if (other == this) continue;
				if (other.ownerUUID == null || !other.ownerUUID.equals(this.ownerUUID)) continue;
				double transferred = other.receiveExternalBlood(bloodBuffer);
				bloodBuffer -= transferred;
				if (bloodBuffer <= 0) break;
			}
		}

		if (bloodBuffer <= 0) return;
		ServerPlayer owner = getOwnerPlayer();
		if (owner != null) {
			HemoCapabilityAccess.getBloodVolume(owner).ifPresent(vol -> {
				if (vol.isActive() && !vol.isFull() && bloodBuffer > 0) {
					double before = vol.getBloodVolume();
					vol.fill(bloodBuffer);
					double moved = Math.max(0, vol.getBloodVolume() - before);
					bloodBuffer -= moved;
				}
			});
		}
		bloodBuffer = Mth.clamp(bloodBuffer, 0, STAGE_BUFFER_MAX[stageIdx]);
		setChanged();
	}

	private boolean consumeOperationalBlood(double amount) {
		if (amount <= 0) return true;

		double localTaken = Math.min(internalBlood, amount);
		internalBlood -= localTaken;
		double remaining = amount - localTaken;
		if (remaining <= 0) return true;

		ServerPlayer owner = getOwnerPlayer();
		if (owner != null && owner.level() == level && owner.isAlive()
				&& owner.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5)
				<= OWNER_DRAIN_RANGE * OWNER_DRAIN_RANGE) {
			var vol = HemoCapabilityAccess.getBloodVolume(owner).orElse(null);
			if (vol != null && vol.isActive() && vol.getBloodVolume() >= remaining) {
				return vol.drain(remaining);
			}
		}

		return drawFromBloodlinePool((float) (remaining * BLOODLINE_FALLBACK_MULT)) > 0;
	}

	private float drawFromBloodlinePool(float amount) {
		if (!(level instanceof ServerLevel serverLevel) || ownerUUID == null) return 0;
		if (!HemoServerConfig.BLOODLINE_POOL_ENABLED.get()) return 0;
		BloodlineSavedData data = BloodlineSavedData.get(serverLevel.getServer().overworld());
		Bloodline line = data.getBloodlineForPlayer(ownerUUID);
		if (line == null || !line.isValid()) return 0;
		return data.drawBlood(line.getBloodlineUUID(), amount);
	}

	private List<LivingEntity> getTargets(double range, boolean includePlayers) {
		AABB area = new AABB(worldPosition).inflate(range);
		return level.getEntitiesOfClass(LivingEntity.class, area, target -> {
			if (!target.isAlive()) return false;
			if (target instanceof Player player) {
				if (ownerUUID != null && player.getUUID().equals(ownerUUID)) return false;
				return includePlayers;
			}
			return target instanceof Monster;
		});
	}

	private ServerPlayer getOwnerPlayer() {
		if (level == null || level.getServer() == null || ownerUUID == null) return null;
		return level.getServer().getPlayerList().getPlayer(ownerUUID);
	}

	private boolean isOwner(Player player) {
		return ownerUUID != null && ownerUUID.equals(player.getUUID());
	}

	private void setState(State next) {
		if (state != next) {
			state = next;
			setChangedAndSync();
		}
	}

	private void setChangedAndSync() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		if (tag.contains("MorphlingItem")) {
			morphlingItem = ItemStack.parseOptional(registries, tag.getCompound("MorphlingItem"));
		} else {
			morphlingItem = ItemStack.EMPTY;
		}
		ownerUUID = tag.hasUUID("OwnerUUID") ? tag.getUUID("OwnerUUID") : null;
		if (tag.contains("State")) {
			try {
				state = State.valueOf(tag.getString("State"));
			} catch (IllegalArgumentException ignored) {
				state = State.DORMANT;
			}
		} else {
			state = State.DORMANT;
		}
		activationCooldown = tag.getInt("ActivationCooldown");
		bloodBuffer = tag.getDouble("BloodBuffer");
		internalBlood = tag.getDouble("InternalBlood");
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (!morphlingItem.isEmpty()) {
			tag.put("MorphlingItem", morphlingItem.save(registries));
		}
		if (ownerUUID != null) {
			tag.putUUID("OwnerUUID", ownerUUID);
		}
		tag.putString("State", state.name());
		tag.putInt("ActivationCooldown", activationCooldown);
		tag.putDouble("BloodBuffer", bloodBuffer);
		tag.putDouble("InternalBlood", internalBlood);
	}
}
