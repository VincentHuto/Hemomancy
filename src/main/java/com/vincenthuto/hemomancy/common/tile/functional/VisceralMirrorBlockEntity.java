package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;
import com.vincenthuto.hemomancy.common.capability.player.visceral.IVisceralOrgans;
import com.vincenthuto.hemomancy.common.capability.player.visceral.VisceralOrgansProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Visceral Mirror — a ritualistic apparatus that allows
 * an initiated hemomancer to gaze into their own reflection and coalesce
 * spectral echoes of their organs for modification.
 *
 * <h3>Organ Selection</h3>
 * <p>The mirror tracks which organ is currently "revealed" in its surface.
 * The player cycles through organs with right-clicks (standing, empty hand),
 * receiving a status preview for each. Once the desired organ is visible,
 * the player crouches and right-clicks to commit and begin the ritual.</p>
 *
 * <h3>Extraction Phases</h3>
 * <ol>
 *   <li><b>Idle</b> — the player browses organs in the mirror.</li>
 *   <li><b>Channeling</b> — the player must stay near the mirror as blood is
 *       drained to sustain the ritual.</li>
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
		// Time out idle selections if nobody interacts
		if (te.phase == RitualPhase.IDLE && te.selectedOrganIndex >= 0) {
			if (level.getGameTime() - te.lastInteractionTick > SELECTION_TIMEOUT_TICKS) {
				te.selectedOrganIndex = -1;
				te.markDirtyAndSync();
			}
		}

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
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, VisceralMirrorBlockEntity te) {
		// Client-side particle effects could go here in the future
	}

	// ========================== ORGAN SELECTION ==========================

	/**
	 * Advances the mirror to reveal the next organ and shows the player a
	 * status preview including the organ's name, current modification level,
	 * and whether it is eligible for extraction.
	 */
	public void cycleOrgan(Player player) {
		if (level == null || level.isClientSide) return;
		if (phase != RitualPhase.IDLE) return;

		EnumOrgan[] organs = EnumOrgan.values();
		selectedOrganIndex = (selectedOrganIndex + 1) % organs.length;
		lastInteractionTick = level.getGameTime();

		EnumOrgan organ = organs[selectedOrganIndex];
		showOrganPreview(player, organ);
		markDirtyAndSync();
	}

	/**
	 * Attempts to confirm the currently selected organ and begin extraction.
	 * @return true if the ritual was successfully started
	 */
	public boolean confirmSelection(Player player) {
		if (level == null || level.isClientSide) return false;
		if (phase != RitualPhase.IDLE) return false;

		if (selectedOrganIndex < 0) {
			msg(player, "The mirror is dormant. Gaze into it first to reveal an organ.",
					ChatFormatting.GRAY);
			return false;
		}

		EnumOrgan organ = EnumOrgan.values()[selectedOrganIndex];
		return startRitual(player, organ);
	}

	/**
	 * Returns the organ currently revealed in the mirror, or {@code null}
	 * if the mirror is dormant (no selection).
	 */
	public EnumOrgan getSelectedOrgan() {
		if (selectedOrganIndex < 0 || selectedOrganIndex >= EnumOrgan.values().length) return null;
		return EnumOrgan.values()[selectedOrganIndex];
	}

	/**
	 * Shows the player a detailed preview of the given organ's status.
	 */
	private void showOrganPreview(Player player, EnumOrgan organ) {
		int degree = player.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA)
				.map(d -> d.getDegreeNumber()).orElse(0);
		IVisceralOrgans organs = player.getCapability(VisceralOrgansProvider.ORGANS_CAPA)
				.orElse(null);

		int currentLevel = organs != null ? organs.getOrganLevel(organ) : 0;
		double bloodCost = organ.getTier() * BLOOD_COST_PER_TIER;
		double currentBlood = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.map(IBloodVolume::getBloodVolume).orElse(0.0);

		// Check if player already holds an echo of this organ
		boolean hasEcho = false;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack invStack = player.getInventory().getItem(i);
			if (!invStack.isEmpty()
					&& invStack.getItem() instanceof com.vincenthuto.hemomancy.common.item.OrganEchoItem echoItem
					&& echoItem.getOrgan() == organ) {
				hasEcho = true;
				break;
			}
		}

		// Build status line: "◆ Heart (Tier 4) — Lv.0/3 — 2000 blood"
		MutableComponent line = Component.literal("\u25C6 ")
				.withStyle(ChatFormatting.DARK_PURPLE)
				.append(Component.literal(organ.getName())
						.withStyle(ChatFormatting.LIGHT_PURPLE))
				.append(Component.literal(" (Tier " + organ.getTier() + ")")
						.withStyle(ChatFormatting.GRAY))
				.append(Component.literal(" \u2014 Lv." + currentLevel + "/3")
						.withStyle(currentLevel >= 3 ? ChatFormatting.GOLD : ChatFormatting.WHITE))
				.append(Component.literal(" \u2014 " + (int) bloodCost + " blood")
						.withStyle(currentBlood >= bloodCost ? ChatFormatting.GREEN : ChatFormatting.RED));

		// Eligibility check
		if (currentLevel >= 3) {
			line = line.append(Component.literal(" [MAX]").withStyle(ChatFormatting.GOLD));
		} else if (hasEcho) {
			line = line.append(Component.literal(" [Echo already held]").withStyle(ChatFormatting.GOLD));
		} else if (organ.getTier() > degree) {
			line = line.append(Component.literal(" [Degree too low]").withStyle(ChatFormatting.RED));
		} else if (currentBlood < bloodCost) {
			line = line.append(Component.literal(" [Not enough blood]").withStyle(ChatFormatting.RED));
		} else {
			line = line.append(Component.literal(" [Sneak-click to extract]").withStyle(ChatFormatting.GREEN));
		}

		player.displayClientMessage(line, true);
	}

	// ========================== RITUAL LOGIC ==========================

	/**
	 * Attempts to begin the organ extraction ritual for the given organ.
	 * @return true if the ritual was successfully started
	 */
	public boolean startRitual(Player player, EnumOrgan organ) {
		if (level == null || level.isClientSide) return false;
		if (phase != RitualPhase.IDLE) return false;

		// Check initiatory degree
		int degree = player.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA)
				.map(d -> d.getDegreeNumber()).orElse(0);
		if (degree < REQUIRED_DEGREE) {
			msg(player, "You lack the initiatory degree to peer into the visceral mirror.",
					ChatFormatting.RED);
			return false;
		}

		// Check tier requirement
		if (organ.getTier() > degree) {
			msg(player, "Your degree is insufficient for an organ of this tier.",
					ChatFormatting.RED);
			return false;
		}

		// Check if already at max modification
		IVisceralOrgans organs = player.getCapability(VisceralOrgansProvider.ORGANS_CAPA)
				.orElse(null);
		if (organs != null && organs.getOrganLevel(organ) >= 3) {
			msg(player, "This organ has already reached its maximum modification.",
					ChatFormatting.GOLD);
			return false;
		}

		// Check if player already holds an echo of this organ
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack invStack = player.getInventory().getItem(i);
			if (!invStack.isEmpty()
					&& invStack.getItem() instanceof com.vincenthuto.hemomancy.common.item.OrganEchoItem echoItem
					&& echoItem.getOrgan() == organ) {
				msg(player, "An echo of this organ already lingers on your person.",
						ChatFormatting.GOLD);
				return false;
			}
		}

		// Check blood cost
		double cost = organ.getTier() * BLOOD_COST_PER_TIER;
		IBloodVolume vol = player.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
		if (vol == null || vol.getBloodVolume() < cost) {
			msg(player, "Insufficient blood to sustain the extraction ritual.",
					ChatFormatting.RED);
			return false;
		}

		// Begin ritual
		this.targetOrgan = organ;
		this.totalRitualTicks = organ.getTier() * CHANNEL_TICKS_PER_TIER;
		this.ritualTicks = 0;
		this.phase = RitualPhase.CHANNELING;
		this.selectedOrganIndex = -1; // Clear selection once committed
		msg(player, "You gaze into the visceral mirror... an echo stirs within your reflection.",
				ChatFormatting.DARK_RED);
		markDirtyAndSync();
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
			if (player != null) {
				msg(player, "The mirror's surface clouds over. The ritual collapses.",
						ChatFormatting.RED);
			}
			markDirtyAndSync();
		}
	}

	private void tickChanneling(Player player) {
		ritualTicks++;

		// Drain blood gradually
		double drainPerTick = (targetOrgan.getTier() * BLOOD_COST_PER_TIER) / totalRitualTicks;
		player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(vol -> {
			if (!vol.drain(drainPerTick)) {
				msg(player, "Your blood runs dry. The ritual fails.", ChatFormatting.RED);
				cancelRitual(player);
			}
		});

		if (phase == RitualPhase.IDLE) return; // Was cancelled in drain check

		if (ritualTicks >= totalRitualTicks) {
			phase = RitualPhase.EXTRACTING;
			ritualTicks = 0;
			msg(player, "Your hand reaches through the mirror. The echo of your "
					+ targetOrgan.getName().toLowerCase() + " takes shape...", ChatFormatting.DARK_PURPLE);
			markDirtyAndSync();
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
			msg(player, "An echo of this organ already lingers on your person.",
					ChatFormatting.GOLD);
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

		if (targetOrgan == EnumOrgan.HEART) {
			msg(player, "An echo of your heart coalesces from the mirror \u2014 "
					+ "take it to a brazier of sanguine flames to complete the rite.",
					ChatFormatting.DARK_RED);
		} else {
			msg(player, "An echo of your " + targetOrgan.getName().toLowerCase()
					+ " coalesces from the mirror \u2014 "
					+ "offer it to sanguine flames to refine it.",
					ChatFormatting.GOLD);
		}

		phase = RitualPhase.COMPLETE;
		markDirtyAndSync();

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

	private void msg(Player player, String text, ChatFormatting color) {
		player.displayClientMessage(Component.literal(text).withStyle(color), true);
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
