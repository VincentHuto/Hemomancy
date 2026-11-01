package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtHospitality;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeAppraisals.Grade;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@link NpcTradeService.Party} over a real player. Room is simulated on a copy of the main inventory with the price
 * already taken out; insertion is confirmed by counting the inventory before and after, because
 * {@link Inventory#add} reports success for a Creative discard.
 */
final class PlayerTradeParty implements NpcTradeService.Party {
	private final ServerPlayer player;
	private final Entity speaker;

	PlayerTradeParty(ServerPlayer player, Entity speaker) {
		this.player = player;
		this.speaker = speaker;
	}

	private NonNullList<ItemStack> items() {
		return player.getInventory().items;
	}

	private ItemStack stack(int slot) {
		return slot >= 0 && slot < items().size() ? items().get(slot) : ItemStack.EMPTY;
	}

	@Override
	public boolean refusedBySide(String speakerId) {
		return NpcTradeSides.refuses(speakerId, player);
	}

	@Override
	public Optional<String> requirementFailure(NpcTradeRequirements requirements, int rapport) {
		return requirements.firstFailure(player, rapport);
	}

	@Override
	public int slotCount() {
		return items().size();
	}

	@Override
	public boolean matches(int slot, NpcTradeMatch match) {
		return match.matches(stack(slot));
	}

	@Override
	public int count(int slot) {
		return stack(slot).getCount();
	}

	@Override
	public Grade grade(int slot, NpcTradeAppraisal appraisal) {
		return NpcTradeAppraisals.grade(stack(slot), appraisal);
	}

	@Override
	public boolean stained(String speakerId, int slot) {
		return NpcTradeSides.refusesStained(speakerId, stack(slot));
	}

	@Override
	public boolean bloodActive() {
		return ProgressionGates.isBloodActive(player);
	}

	@Override
	public boolean canPayInFull(int amount, BloodAccounting.Source source) {
		return BloodAccounting.canPayInFull(player, amount, source);
	}

	@Override
	public boolean pay(int amount, BloodAccounting.Source source) {
		return BloodAccounting.pay(player, amount, source).satisfied();
	}

	@Override
	public Optional<String> customRefusal(String act) {
		if (!(speaker instanceof CourtMemberEntity member)) return Optional.of(CourtHospitality.REFUSED);
		return CourtHospitality.customRefusal(player, member, act);
	}

	@Override
	public boolean isCustomOffering(int slot) {
		return speaker instanceof CourtMemberEntity member
				&& CourtHospitality.isCustomOffering(stack(slot), member.role().house());
	}

	@Override
	public void acceptCustom(int slot) {
		if (speaker instanceof CourtMemberEntity member) {
			CourtHospitality.acceptCustom(player, member.role().house(), stack(slot));
			player.getInventory().setChanged();
		}
	}

	@Override
	public boolean fits(List<NpcTradeService.Take> takes, List<NpcTradeGive> gives) {
		List<ItemStack> copy = new ArrayList<>(items().size());
		for (ItemStack stack : items()) copy.add(stack.copy());
		for (NpcTradeService.Take take : takes) copy.get(take.slot()).shrink(take.amount());
		for (NpcTradeGive give : gives) {
			if (!place(copy, give.toStack(player.registryAccess()))) return false;
		}
		return true;
	}

	/** Places {@code stack} into {@code slots} the way an inventory fills: matching stacks first, then empty slots. */
	static boolean place(List<ItemStack> slots, ItemStack stack) {
		int remaining = stack.getCount();
		int max = stack.getMaxStackSize();
		for (ItemStack held : slots) {
			if (remaining <= 0) break;
			if (held.isEmpty() || !ItemStack.isSameItemSameComponents(held, stack) || held.getCount() >= max) continue;
			int moved = Math.min(remaining, max - held.getCount());
			held.grow(moved);
			remaining -= moved;
		}
		for (int i = 0; i < slots.size() && remaining > 0; i++) {
			if (!slots.get(i).isEmpty()) continue;
			int moved = Math.min(remaining, max);
			slots.set(i, stack.copyWithCount(moved));
			remaining -= moved;
		}
		return remaining <= 0;
	}

	@Override
	public void take(int slot, int amount) {
		stack(slot).shrink(amount);
		player.getInventory().setChanged();
	}

	@Override
	public int insert(NpcTradeGive give) {
		ItemStack stack = give.toStack(player.registryAccess());
		int before = countSame(stack);
		player.getInventory().add(stack.copy());
		player.getInventory().setChanged();
		return Math.max(0, countSame(stack) - before);
	}

	private int countSame(ItemStack like) {
		Inventory inventory = player.getInventory();
		int total = 0;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack held = inventory.getItem(i);
			if (ItemStack.isSameItemSameComponents(held, like)) total += held.getCount();
		}
		return total;
	}

	@Override
	public void drop(NpcTradeGive give, int count) {
		ItemStack stack = give.toStack(player.registryAccess()).copyWithCount(count);
		ItemEntity drop = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
		drop.setNoPickUpDelay();
		player.level().addFreshEntity(drop);
	}
}
