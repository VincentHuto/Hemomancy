package com.vincenthuto.hemomancy.common.tile;

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
 * an initiated hemomancer to reach into their own reflection and extract organs
 * for modification.
 *
 * <p>The extraction ritual proceeds in phases:</p>
 * <ol>
 *   <li><b>Idle</b> — player right-clicks to begin, selecting an organ.</li>
 *   <li><b>Channeling</b> — the player must stay near the mirror as blood is
 *       drained to sustain the extraction.</li>
 *   <li><b>Extracting</b> — the organ is pulled through the mirror; the player
 *       takes damage proportional to the organ's tier.</li>
 *   <li><b>Complete</b> — the extracted organ item drops and the player's
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

	// ========================== STATE ==========================

	public enum RitualPhase { IDLE, CHANNELING, EXTRACTING, COMPLETE }

	private RitualPhase phase = RitualPhase.IDLE;
	private EnumOrgan targetOrgan = null;
	private int ritualTicks = 0;
	private int totalRitualTicks = 0;

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
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, VisceralMirrorBlockEntity te) {
		// Client-side particle effects could go here in the future
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
		msg(player, "You gaze into the visceral mirror... your reflection parts its flesh.",
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
			msg(player, "Your hand reaches through the mirror. You grasp the "
					+ targetOrgan.getName().toLowerCase() + "...", ChatFormatting.DARK_PURPLE);
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

		// Update the player's organ capability
		player.getCapability(VisceralOrgansProvider.ORGANS_CAPA).ifPresent(organs -> {
			int currentLevel = organs.getOrganLevel(targetOrgan);
			int newLevel = Math.min(currentLevel + 1, 3);
			organs.setOrganLevel(targetOrgan, newLevel);

			// Drop the corresponding organ item
			ItemStack organItem = getOrganItem(targetOrgan);
			if (!organItem.isEmpty()) {
				ItemEntity entity = new ItemEntity(level,
						worldPosition.getX() + 0.5, worldPosition.getY() + 1.5,
						worldPosition.getZ() + 0.5, organItem);
				entity.setDeltaMovement(0, 0.1, 0);
				level.addFreshEntity(entity);
			}

			String levelDesc = newLevel == 1 ? "extracted" : "modified to level " + newLevel;
			if (targetOrgan == EnumOrgan.HEART) {
				msg(player, "Through sheer force of will, you command your muscles to contract "
						+ "rhythmically. Your blood flows still — without a heart.",
						ChatFormatting.DARK_RED);
			} else {
				msg(player, "The " + targetOrgan.getName().toLowerCase() + " has been "
						+ levelDesc + ".", ChatFormatting.GOLD);
			}
		});

		phase = RitualPhase.COMPLETE;
		markDirtyAndSync();

		// Reset after a short delay
		targetOrgan = null;
		ritualTicks = 0;
		totalRitualTicks = 0;
		phase = RitualPhase.IDLE;
	}

	private ItemStack getOrganItem(EnumOrgan organ) {
		return switch (organ) {
			case SPLEEN -> new ItemStack(ItemInit.extracted_spleen.get());
			case LIVER -> new ItemStack(ItemInit.extracted_liver.get());
			case LUNGS -> new ItemStack(ItemInit.extracted_lungs.get());
			case KIDNEYS -> new ItemStack(ItemInit.extracted_kidneys.get());
			case HEART -> new ItemStack(ItemInit.extracted_heart.get());
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
