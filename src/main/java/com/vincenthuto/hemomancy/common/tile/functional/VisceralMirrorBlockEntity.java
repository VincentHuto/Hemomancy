package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;
import com.vincenthuto.hemomancy.common.capability.player.visceral.IVisceralOrgans;
import com.vincenthuto.hemomancy.common.capability.player.visceral.VisceralOrgansProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.visceral.VisceralMirrorUpdatePacket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Block entity for the Visceral Mirror — a ritualistic apparatus that allows
 * an initiated hemomancer to gaze into their own reflection and coalesce
 * spectral echoes of their organs for modification.
 *
 * <h3>Extraction Flow (GUI-driven)</h3>
 * <ol>
 *   <li><b>Screen opened</b> — the player right-clicks the mirror, opening a
 *       GUI that displays all organs and their current status.</li>
 *   <li><b>Channeling</b> — the player selects an organ and clicks "Begin
 *       Extraction". Blood is drained over time, with progress shown in the
 *       GUI's progress bar.</li>
 *   <li><b>Extracting</b> — the echo is pulled through the mirror; the player
 *       takes damage proportional to the organ's tier.</li>
 *   <li><b>Complete</b> — an "Echo of" organ item drops and the player's
 *       capability is updated.</li>
 * </ol>
 */
public class VisceralMirrorBlockEntity extends BlockEntity {

	// ========================== CONSTANTS ==========================

	/** Minimum initiatory degree required to use the mirror. */
	private static final int REQUIRED_DEGREE = 3;

	/** Blood cost per organ tier during extraction. */
	private static final double BLOOD_COST_PER_TIER = 500.0;

	/** Ticks of channeling per organ tier. */
	private static final int CHANNEL_TICKS_PER_TIER = 100;

	/** Max distance squared the player can be from the mirror during ritual. */
	private static final double MAX_RITUAL_DISTANCE_SQ = 25.0;

	/** Ticks after which the mirror "forgets" its selection if nobody interacts. */
	private static final int SELECTION_TIMEOUT_TICKS = 200;

	// ========================== STATE ==========================

	public enum RitualPhase { IDLE, CHANNELING, EXTRACTING, COMPLETE }

	private RitualPhase phase = RitualPhase.IDLE;
	private EnumOrgan targetOrgan = null;
	private int ritualTicks = 0;
	private int totalRitualTicks = 0;

	/**
	 * Index into {@link EnumOrgan#values()} that indicates which organ is
	 * currently "revealed" in the mirror surface. -1 means no organ is
	 * selected (mirror dormant).
	 */
	private int selectedOrganIndex = -1;

	/** Tick timestamp of the last time the player interacted with the mirror. */
	private long lastInteractionTick = 0;

	public VisceralMirrorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.visceral_mirror.get(), pos, state);
	}

	// ========================== TICK ==========================

	public static void serverTick(Level level, BlockPos pos, BlockState state, VisceralMirrorBlockEntity te) {
		if (te.phase == RitualPhase.IDLE || te.phase == RitualPhase.COMPLETE) return;

		Player player = te.findNearestInitiatedPlayer();
		if (player == null) {
			te.cancelRitual(null);
			return;
		}

		if (te.isPlayerTooFar(player)) {
			te.cancelRitual(player);
			return;
		}

		if (te.phase == RitualPhase.CHANNELING) {
			te.tickChanneling(player);
		} else if (te.phase == RitualPhase.EXTRACTING) {
			te.tickExtracting(player);
		}

		// Send progress updates to the player's screen every 5 ticks
		if (te.ritualTicks % 5 == 0 && player instanceof ServerPlayer sp) {
			te.sendProgressUpdate(sp, "");
		}
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, VisceralMirrorBlockEntity te) {
		// Client-side particle effects could go here in the future
	}

	// ========================== RITUAL LOGIC ==========================

	/**
	 * Attempts to begin the organ extraction ritual for the given organ.
	 * Called from the C2S extract packet when the player clicks "Begin
	 * Extraction" in the GUI.
	 * @return true if the ritual was successfully started
	 */
	public boolean startRitual(Player player, EnumOrgan organ) {
		if (level == null || level.isClientSide) return false;
		if (phase != RitualPhase.IDLE) return false;

		// Check initiatory degree
		int degree = HemoCapabilityAccess.getInitiatoryDegree(player)
				.map(d -> d.getDegreeNumber()).orElse(0);
		if (degree < REQUIRED_DEGREE) {
			sendStatusToPlayer(player, "You lack the initiatory degree to peer into the visceral mirror.");
			return false;
		}

		// Check tier requirement
		if (organ.getTier() > degree) {
			sendStatusToPlayer(player, "Your degree is insufficient for an organ of this tier.");
			return false;
		}

		// Check if already at max modification
		IVisceralOrgans organs = player.getCapability(VisceralOrgansProvider.ORGANS_CAPA)
				.orElse(null);
		if (organs != null && organs.getOrganLevel(organ) >= 3) {
			sendStatusToPlayer(player, "This organ has already reached its maximum modification.");
			return false;
		}

		// Check if player already holds an echo of this organ
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack invStack = player.getInventory().getItem(i);
			if (!invStack.isEmpty()
					&& invStack.getItem() instanceof com.vincenthuto.hemomancy.common.item.OrganEchoItem echoItem
					&& echoItem.getOrgan() == organ) {
				sendStatusToPlayer(player, "An echo of this organ already lingers on your person.");
				return false;
			}
		}

		// Check blood cost
		double cost = organ.getTier() * BLOOD_COST_PER_TIER;
		IBloodVolume vol = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (vol == null || vol.getBloodVolume() < cost) {
			sendStatusToPlayer(player, "Insufficient blood to sustain the extraction ritual.");
			return false;
		}

		// Begin ritual
		this.targetOrgan = organ;
		this.totalRitualTicks = organ.getTier() * CHANNEL_TICKS_PER_TIER;
		this.ritualTicks = 0;
		this.phase = RitualPhase.CHANNELING;
		this.selectedOrganIndex = -1;
		markDirtyAndSync();

		if (player instanceof ServerPlayer sp) {
			sendProgressUpdate(sp, "The echo stirs within your reflection...");
		}
		return true;
	}

	/** Cancels the active ritual. */
	public void cancelRitual(Player player) {
		if (phase != RitualPhase.IDLE) {
			phase = RitualPhase.IDLE;
			targetOrgan = null;
			ritualTicks = 0;
			totalRitualTicks = 0;
			selectedOrganIndex = -1;
			if (player instanceof ServerPlayer sp) {
				sendProgressUpdate(sp, "The ritual collapses.");
			}
			markDirtyAndSync();
		}
	}

	private void tickChanneling(Player player) {
		ritualTicks++;

		// Drain blood gradually
		double drainPerTick = (targetOrgan.getTier() * BLOOD_COST_PER_TIER) / totalRitualTicks;
		HemoCapabilityAccess.getBloodVolume(player).ifPresent(vol -> {
			if (!vol.drain(drainPerTick)) {
				if (player instanceof ServerPlayer sp) {
					sendProgressUpdate(sp, "Your blood runs dry. The ritual fails.");
				}
				cancelRitual(player);
			}
		});

		if (phase == RitualPhase.IDLE) return; // Was cancelled in drain check

		if (ritualTicks >= totalRitualTicks) {
			phase = RitualPhase.EXTRACTING;
			ritualTicks = 0;
			markDirtyAndSync();
			if (player instanceof ServerPlayer sp) {
				sendProgressUpdate(sp, "The echo takes shape...");
			}
		}
	}

	private void tickExtracting(Player player) {
		ritualTicks++;

		// Extraction takes 60 ticks (3 seconds) with damage
		if (ritualTicks % 20 == 0) {
			float damage = targetOrgan.getTier() * 2.0f;
			player.hurt(player.damageSources().magic(), damage);
		}

		if (ritualTicks >= 60) {
			completeExtraction(player);
		}
	}

	private void completeExtraction(Player player) {
		if (targetOrgan == null) {
			cancelRitual(player);
			return;
		}

		// Check the player doesn't already hold an echo of this organ
		boolean alreadyHasEcho = false;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack invStack = player.getInventory().getItem(i);
			if (!invStack.isEmpty() && invStack.getItem() instanceof com.vincenthuto.hemomancy.common.item.OrganEchoItem echoItem
					&& echoItem.getOrgan() == targetOrgan) {
				alreadyHasEcho = true;
				break;
			}
		}

		if (alreadyHasEcho) {
			if (player instanceof ServerPlayer sp) {
				sendProgressUpdate(sp, "An echo of this organ already lingers on your person.");
			}
			phase = RitualPhase.IDLE;
			targetOrgan = null;
			ritualTicks = 0;
			totalRitualTicks = 0;
			markDirtyAndSync();
			return;
		}

		// Drop the corresponding echo item — the organ is NOT upgraded here.
		// The player must take the echo to a lit brazier with reagents to
		// complete the organ upgrade ritual.
		ItemStack organItem = getOrganItem(targetOrgan);
		if (!organItem.isEmpty()) {
			ItemEntity entity = new ItemEntity(level,
					worldPosition.getX() + 0.5, worldPosition.getY() + 1.5,
					worldPosition.getZ() + 0.5, organItem);
			entity.setDeltaMovement(0, 0.1, 0);
			level.addFreshEntity(entity);
		}

		String completionMsg = "Echo of " + targetOrgan.getName().toLowerCase()
				+ " coalesced. Offer it to sanguine flames.";

		phase = RitualPhase.COMPLETE;
		markDirtyAndSync();

		if (player instanceof ServerPlayer sp) {
			sendProgressUpdate(sp, completionMsg);
		}

		// Reset to idle
		targetOrgan = null;
		ritualTicks = 0;
		totalRitualTicks = 0;
		phase = RitualPhase.IDLE;
	}

	private ItemStack getOrganItem(EnumOrgan organ) {
		return switch (organ) {
			case SPLEEN -> new ItemStack(ItemInit.echo_of_spleen.get());
			case LIVER -> new ItemStack(ItemInit.echo_of_liver.get());
			case LUNGS -> new ItemStack(ItemInit.echo_of_lungs.get());
			case KIDNEYS -> new ItemStack(ItemInit.echo_of_kidneys.get());
			case HEART -> new ItemStack(ItemInit.echo_of_heart.get());
		};
	}

	// ========================== HELPERS ==========================

	private Player findNearestInitiatedPlayer() {
		if (level == null) return null;
		return level.getNearestPlayer(
				worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
				worldPosition.getZ() + 0.5, 5.0, false);
	}

	private boolean isPlayerTooFar(Player player) {
		return player.distanceToSqr(
				worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
				worldPosition.getZ() + 0.5) > MAX_RITUAL_DISTANCE_SQ;
	}

	/**
	 * Sends a VisceralMirrorUpdatePacket to the given player with the current
	 * ritual state and an optional status message.
	 */
	private void sendProgressUpdate(ServerPlayer player, String message) {
		PacketHandler.sendToPlayer(player, new VisceralMirrorUpdatePacket(worldPosition, phase, ritualTicks,
						totalRitualTicks, targetOrgan, message));
	}

	/**
	 * Sends a status-only update (idle phase) to reject an extraction attempt
	 * with a message the screen can display.
	 */
	private void sendStatusToPlayer(Player player, String message) {
		if (player instanceof ServerPlayer sp) {
			PacketHandler.sendToPlayer(sp, new VisceralMirrorUpdatePacket(worldPosition, RitualPhase.IDLE,
							0, 0, null, message));
		}
	}

	private void markDirtyAndSync() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	// ========================== ACCESSORS ==========================

	public RitualPhase getPhase() { return phase; }
	public EnumOrgan getTargetOrgan() { return targetOrgan; }
	public int getRitualTicks() { return ritualTicks; }
	public int getTotalRitualTicks() { return totalRitualTicks; }
	public int getSelectedOrganIndex() { return selectedOrganIndex; }
	public float getRitualProgress() {
		if (totalRitualTicks <= 0) return 0;
		return (float) ritualTicks / totalRitualTicks;
	}

	// ========================== NBT ==========================

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("Phase", phase.ordinal());
		tag.putInt("RitualTicks", ritualTicks);
		tag.putInt("TotalRitualTicks", totalRitualTicks);
		tag.putInt("SelectedOrganIndex", selectedOrganIndex);
		tag.putLong("LastInteractionTick", lastInteractionTick);
		if (targetOrgan != null) {
			tag.putString("TargetOrgan", targetOrgan.name());
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		int phaseOrd = tag.getInt("Phase");
		phase = phaseOrd >= 0 && phaseOrd < RitualPhase.values().length
				? RitualPhase.values()[phaseOrd] : RitualPhase.IDLE;
		ritualTicks = tag.getInt("RitualTicks");
		totalRitualTicks = tag.getInt("TotalRitualTicks");
		selectedOrganIndex = tag.getInt("SelectedOrganIndex");
		lastInteractionTick = tag.getLong("LastInteractionTick");
		if (tag.contains("TargetOrgan")) {
			targetOrgan = EnumOrgan.valueOf(tag.getString("TargetOrgan"));
		}
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		saveAdditional(tag);
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
