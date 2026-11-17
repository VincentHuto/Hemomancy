package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCharge;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.manipulation.BloodFlowVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.mortem.CarrionRules.Remains;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * Carrion Communion as a Sculpting of Absorption (core-loop round two P4): held toward the dead, it draws from them. The
 * dead hold no living blood, so it gives no blood: it gives Mortem Temper and a little health. Rotten flesh and bones
 * lying on the ground ({@code #hemomancy:carrion_remains}) go one a pulse by hand; a skull or a bone block
 * ({@code #hemomancy:carrion_blocks}) is drawn whole after a few pulses, as the player's edit, and drops nothing. Each
 * pulse still costs blood and burns half a Mortem Temper like any Sculpting, so it pays for itself only in Temper. The R
 * cast (draining afflicted enemies round the caster) is unchanged.
 */
public final class CarrionDrawSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 4.0D;
	public static final int CADENCE = 10;
	public static final double REACH_SCALE = 1.5D;
	public static final TagKey<Item> REMAINS = TagKey.create(Registries.ITEM, Hemomancy.rloc("carrion_remains"));
	public static final TagKey<Block> BLOCKS = TagKey.create(Registries.BLOCK, Hemomancy.rloc("carrion_blocks"));

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.ITEMS, VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return switch (target) {
			case VerbTarget.Items items -> items.items().stream().anyMatch(item -> item.isAlive() && item.getItem().is(REMAINS));
			case VerbTarget.Block block -> block.state().is(BLOCKS) && !StrandSculpting.takesBlood(player.level(), block);
			default -> false;
		};
	}

	static Remains of(ItemStack stack) {
		return stack.is(Items.BONE) ? Remains.BONE : Remains.FLESH;
	}

	static Remains of(BlockState state) {
		return state.is(Blocks.BONE_BLOCK) ? Remains.BONE_BLOCK : Remains.SKULL;
	}

	/**
	 * Remains on the ground are always admitted. A block is taken out of the world, so a player who may not break it is
	 * refused before any blood is paid, and again on every pulse (CN-13).
	 */
	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block)) return true;
		if (ManipulationCombatHelper.mayAlter(player, block.pos()) && PlayerBlockEdits.mayBreak(player, block.pos())) return true;
		player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
				.withStyle(ChatFormatting.DARK_RED), true);
		return false;
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(player.level() instanceof ServerLevel level)) return;
		switch (target) {
			case VerbTarget.Items items -> drawItems(level, player, items, pulse);
			case VerbTarget.Block block -> drawBlock(level, player, block, pulse);
			default -> {
			}
		}
	}

	private static void drawItems(ServerLevel level, ServerPlayer player, VerbTarget.Items items, SculptingPulse pulse) {
		int budget = CarrionRules.itemsPerPulse(pulse.workScale());
		int drawn = 0;
		for (ItemEntity item : items.items()) {
			if (drawn >= budget) break;
			if (!item.isAlive() || !item.getItem().is(REMAINS)) continue;
			ItemStack stack = item.getItem();
			Remains remains = of(stack);
			int take = Math.min(budget - drawn, stack.getCount());
			ItemStack left = stack.copy();
			left.shrink(take);
			if (left.isEmpty()) item.discard();
			else item.setItem(left);
			for (int i = 0; i < take; i++) feed(player, remains, pulse);
			drawn += take;
			draw(level, player, item.position().add(0.0D, 0.15D, 0.0D), pulse);
		}
	}

	private static void drawBlock(ServerLevel level, ServerPlayer player, VerbTarget.Block block, SculptingPulse pulse) {
		BlockPos pos = block.pos();
		Remains remains = of(block.state());
		draw(level, player, Vec3.atCenterOf(pos), pulse);
		if (!CarrionRules.drawsWhole(remains, pulse.index())) return;
		if (PlayerBlockEdits.lift(player, pos) == null) {
			player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
					.withStyle(ChatFormatting.DARK_RED), true);
			return;
		}
		feed(player, remains, pulse);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.BONE, Vec3.atBottomCenterOf(pos), Vec3.atCenterOf(pos), 0.6D, 24);
		level.playSound(null, pos, SoundEvents.BONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.9F, 0.7F);
	}

	/** One remain consumed: its Mortem Temper and a little health to the player. */
	private static void feed(ServerPlayer player, Remains remains, SculptingPulse pulse) {
		TemperCharge.fillFromCarrion(player, CarrionRules.temper(remains, pulse.workScale()));
		player.heal(CarrionRules.HEAL_PER_REMAIN);
		HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_CARRION_DRAWN);
	}

	/** What the dead give rises from them and is drawn into the hand. */
	private static void draw(ServerLevel level, ServerPlayer player, Vec3 from, SculptingPulse pulse) {
		BloodFlowVisuals.from(player, from, player.getId() * 131L + pulse.index(), BloodFlowPacket.Style.COMMUNION);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.MORTEM_CONJURE, from, from, 0.45D, 16);
		level.playSound(null, BlockPos.containing(from), SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 0.35F, 0.6F);
	}

	@Override
	public int cadence() {
		return CADENCE;
	}

	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}

	@Override
	public double reachScale() {
		return REACH_SCALE;
	}
}
