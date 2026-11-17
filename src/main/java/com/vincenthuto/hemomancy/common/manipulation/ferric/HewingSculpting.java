package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffGrowth;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Hematic Hewing as a Sculpting of Projection (core-loop Track 8): each pulse cuts a set shape out of the struck face,
 * a tunnel one wide and two tall, a three-by-three face while sneaking, or a stair's column when aimed steeply into a
 * wall ({@link StaffMiningRules#shapeFor}). It takes only what the tier the Living Staff's rings have earned can
 * harvest, never a block that holds a block entity (a chest, a machine), never an unbreakable one, and breaks each
 * through {@link PlayerBlockEdits#breakBlock} with a plain tool, so the drops are plain.
 */
public final class HewingSculpting implements VerbSculpting {
	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Block block
				&& hewable(player.level(), block.pos(), block.state(), LivingStaffGrowth.ring(player))
				&& !StrandSculpting.takesBlood(player.level(), block);
	}

	/** Whether a cut may take this block: solid, breakable, no block entity, within the earned tier. */
	public static boolean hewable(BlockGetter level, BlockPos pos, BlockState state, int ring) {
		if (state.isAir() || state.liquid() || state.hasBlockEntity()) return false;
		float hardness = state.getDestroySpeed(level, pos);
		return hardness >= 0.0F && LivingStaffGrowth.canHarvest(state, ring);
	}

	@Override
	public int cadence() {
		return StaffMiningRules.HEW_CADENCE;
	}

	/** The floor of a cut, what the HUD shows; the pulse itself is priced by the blocks it will cut. */
	@Override
	public double bloodPerPulse() {
		return StaffMiningRules.HEW_MIN_BLOOD_PER_CUT;
	}

	@Override
	public double bloodPerPulse(ServerPlayer player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block)) return bloodPerPulse();
		Level level = player.level();
		List<BlockPos> cut = cutCells(player, block);
		float[] hardnesses = new float[cut.size()];
		for (int i = 0; i < cut.size(); i++) hardnesses[i] = level.getBlockState(cut.get(i)).getDestroySpeed(level, cut.get(i));
		return StaffMiningRules.hewPrice(hardnesses);
	}

	/** The cells this cut takes that may be hewn now, the struck block first. */
	private static List<BlockPos> cutCells(ServerPlayer player, VerbTarget.Block block) {
		Level level = player.level();
		BlockPos struck = block.pos();
		Direction face = block.hit().getDirection();
		StaffMiningRules.HewShape shape = StaffMiningRules.shapeFor(player.isShiftKeyDown(), player.getXRot(), face.getAxis().isHorizontal());
		// A tunnel takes the block toward the body: the one below when the player stands lower than the struck block.
		boolean bodyBelow = player.getY() < struck.getY() - 0.01D;
		int ring = LivingStaffGrowth.ring(player);
		List<BlockPos> cells = new ArrayList<>();
		for (int[] offset : StaffMiningRules.cells(shape, face.getStepX(), face.getStepY(), face.getStepZ(), bodyBelow)) {
			BlockPos pos = struck.offset(offset[0], offset[1], offset[2]);
			if (level.isLoaded(pos) && hewable(level, pos, level.getBlockState(pos), ring)) cells.add(pos);
		}
		return cells;
	}

	/**
	 * A player who may not break the struck block is refused before any blood is paid. Cells of the cut beyond the struck
	 * block are still checked one by one as they break.
	 */
	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block)) return false;
		if (ManipulationCombatHelper.mayAlter(player, block.pos()) && PlayerBlockEdits.mayBreak(player, block.pos())) return true;
		player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
				.withStyle(ChatFormatting.DARK_RED), true);
		return false;
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(target instanceof VerbTarget.Block block) || !(player.level() instanceof ServerLevel level)) return;
		BlockPos struck = block.pos();
		Direction face = block.hit().getDirection();
		StaffMiningRules.HewShape shape = StaffMiningRules.shapeFor(player.isShiftKeyDown(), player.getXRot(), face.getAxis().isHorizontal());
		ItemStack tool = LivingStaffGrowth.harvestTool(LivingStaffGrowth.ring(player));
		int cut = 0;
		boolean refused = false;
		for (BlockPos pos : cutCells(player, block)) {
			if (PlayerBlockEdits.breakBlock(player, pos, tool)) cut++;
			else refused = true;
		}
		if (cut > 0) {
			Vec3 at = Vec3.atCenterOf(struck);
			Vec3 out = at.add(face.getStepX() * 0.9D, face.getStepY() * 0.9D + 0.2D, face.getStepZ() * 0.9D);
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.FERRIC_IMPACT, at, out,
					shape == StaffMiningRules.HewShape.FACE ? 1.4D : 0.8D, 12);
			level.playSound(null, struck, SoundEvents.DEEPSLATE_BRICKS_BREAK, SoundSource.PLAYERS, 0.9F, 0.8F);
		} else if (refused) {
			player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
					.withStyle(ChatFormatting.DARK_RED), true);
		}
	}
}
