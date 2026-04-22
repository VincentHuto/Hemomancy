package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.block.BrazierBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.OrganEchoItem;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Iron Brazier — doubles as a ritual apparatus for
 * organ echo upgrades when reagents are sacrificed into its flames.
 *
 * <h3>Ritual Flow</h3>
 * <ol>
 *   <li>Brazier must be lit ({@link BrazierBlock#RITUAL_PHASE RITUAL_PHASE=1}).</li>
 *   <li>Player throws in reagent items (blood crystal shards, vivianite clusters).
 *       Each accepted reagent increments {@link #reagentsAccepted}.</li>
 *   <li>Once enough reagents are consumed ({@link #REQUIRED_REAGENTS}), the
 *       brazier enters the {@link BrazierBlock#RITUAL_PHASE RITUAL_PHASE=2}
 *       state — sanguine flames — and awaits an organ echo.</li>
 *   <li>Player right-clicks with an organ echo item to consume it and
 *       upgrade the corresponding organ on their capability. The brazier
 *       cools back to normal.</li>
 * </ol>
 */
public class IronBrazierBlockEntity extends BlockEntity {

	/** Number of reagent items required to activate sanguine flames. */
	public static final int REQUIRED_REAGENTS = 3;

	/** Ticks the sanguine flame state persists before timing out back to normal lit. */
	private static final int SANGUINE_TIMEOUT_TICKS = 600; // 30 seconds

	/** How many valid reagents have been tossed in since the last reset. */
	private int reagentsAccepted = 0;

	/** Server tick counter for the sanguine flame timeout. */
	private int sanguineTicks = 0;

	public IronBrazierBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.iron_brazier.get(), pos, state);
	}

	// ========================== TICK ==========================

	public static void serverTick(Level level, BlockPos pos, BlockState state, IronBrazierBlockEntity te) {
		int phase = state.getValue(BrazierBlock.RITUAL_PHASE);
		if (phase == 2) {
			// Sanguine flame timeout
			te.sanguineTicks++;
			if (te.sanguineTicks >= SANGUINE_TIMEOUT_TICKS) {
				te.resetRitual(level, pos, state);
			}
		}
	}

	// ========================== REAGENT ACCEPTANCE ==========================

	/**
	 * Attempts to accept a reagent item thrown into the brazier by the player.
	 * @return true if the item was consumed as a valid reagent
	 */
	public boolean tryAcceptReagent(Player player, ItemStack stack, Level level, BlockPos pos, BlockState state) {
		int phase = state.getValue(BrazierBlock.RITUAL_PHASE);
		if (phase != 1) return false; // Must be in normal-lit state

		if (!isValidReagent(stack)) return false;

		stack.shrink(1);
		reagentsAccepted++;
		markDirtyAndSync();

		if (reagentsAccepted >= REQUIRED_REAGENTS) {
			// Transition to sanguine flame state
			level.setBlock(pos, state.setValue(BrazierBlock.RITUAL_PHASE, 2), 3);
			sanguineTicks = 0;
			msg(player, "The brazier erupts with sanguine flames — offer an Echo to the fire.",
					ChatFormatting.DARK_RED);
		} else {
			int remaining = REQUIRED_REAGENTS - reagentsAccepted;
			msg(player, "The brazier consumes the offering... " + remaining + " more required.",
					ChatFormatting.GOLD);
		}
		return true;
	}

	/**
	 * Attempts to accept an organ echo item to complete the upgrade ritual.
	 * @return true if the echo was consumed and the organ was upgraded
	 */
	public boolean tryAcceptEcho(Player player, ItemStack stack, Level level, BlockPos pos, BlockState state) {
		int phase = state.getValue(BrazierBlock.RITUAL_PHASE);
		if (phase != 2) return false; // Must be in sanguine flame state

		if (!(stack.getItem() instanceof OrganEchoItem echoItem)) return false;

		EnumOrgan organ = echoItem.getOrgan();

		// Check and upgrade the organ on the player's capability
		boolean[] success = { false };
		HemoCapabilityAccess.getVisceralOrgans(player).ifPresent(organs -> {
			int currentLevel = organs.getOrganLevel(organ);
			if (currentLevel >= 3) {
				msg(player, "This organ echo has already reached its maximum refinement.",
						ChatFormatting.GOLD);
				return;
			}

			int newLevel = Math.min(currentLevel + 1, 3);
			organs.setOrganLevel(organ, newLevel);

			// Consume the echo item
			stack.shrink(1);

			String levelDesc = newLevel == 1 ? "imprinted" : "refined to level " + newLevel;
			if (organ == EnumOrgan.HEART) {
				msg(player, "The echo of your heart dissolves into the sanguine flames. "
						+ "Your muscles learn to beat without it \u2014 " + levelDesc + ".",
						ChatFormatting.DARK_RED);
			} else {
				msg(player, "The echo of your " + organ.getName().toLowerCase()
						+ " dissolves into the sanguine flames \u2014 " + levelDesc + ".",
						ChatFormatting.GOLD);
			}
			success[0] = true;
		});

		if (success[0]) {
			// Reset brazier back to normal lit state
			resetRitual(level, pos, state);
		}
		return success[0];
	}

	// ========================== HELPERS ==========================

	private boolean isValidReagent(ItemStack stack) {
		return stack.getItem() == com.vincenthuto.hemomancy.common.init.ItemInit.blood_crystal_shard.get()
				|| stack.getItem() == com.vincenthuto.hemomancy.common.init.ItemInit.vivianite_cluster.get();
	}

	private void resetRitual(Level level, BlockPos pos, BlockState state) {
		reagentsAccepted = 0;
		sanguineTicks = 0;
		// Go back to normal lit (phase 1)
		BlockState current = level.getBlockState(pos);
		if (current.getValue(BrazierBlock.RITUAL_PHASE) == 2) {
			level.setBlock(pos, current.setValue(BrazierBlock.RITUAL_PHASE, 1), 3);
		}
		markDirtyAndSync();
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

	public int getReagentsAccepted() { return reagentsAccepted; }
	public int getSanguineTicks() { return sanguineTicks; }

	// ========================== NBT ==========================

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("ReagentsAccepted", reagentsAccepted);
		tag.putInt("SanguineTicks", sanguineTicks);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		reagentsAccepted = tag.getInt("ReagentsAccepted");
		sanguineTicks = tag.getInt("SanguineTicks");
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