package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.BloodFlowVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.vincenthuto.hemomancy.common.entity.summon.IBloodConstruct;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Set;

/**
 * Sanguine Mending as a Sculpting of Projection (core-loop round two P4): blood worked into what it is held on mends it.
 * A dropped tool or piece of armour gets its durability back a little each pulse; an injured construct in
 * {@code #hemomancy:mendable_constructs} is healed when it is the caster's ally, or an unowned golem that is fighting no
 * player; and an
 * anvil is beaten back a stage (damaged to chipped to whole) after eight paid pulses on it. The R cast (repairing the
 * held item) is unchanged, and worn or held gear is left to it.
 */
public final class SanguineMendingSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 20.0D;
	public static final int CADENCE = 10;
	/** Durability one pulse restores by hand: two millilitres a point. */
	public static final int REPAIR_PER_PULSE = 10;
	/** Health one pulse gives a construct by hand. */
	public static final float HEAL_PER_PULSE = 3.0F;
	/** Paid pulses on one anvil that take it back a stage. */
	public static final int PULSES_PER_STAGE = 8;
	public static final TagKey<EntityType<?>> MENDABLE = TagKey.create(Registries.ENTITY_TYPE, Hemomancy.rloc("mendable_constructs"));

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.ITEMS, VerbTargetKind.CREATURE, VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return switch (target) {
			case VerbTarget.Items items -> damaged(items).isPresent();
			case VerbTarget.Creature creature -> mendable(player, creature.living());
			case VerbTarget.Block block -> mended(block.state()).isPresent() && !StrandSculpting.takesBlood(player.level(), block);
			default -> false;
		};
	}

	/** The first item in the heap that has durability to give back. */
	static Optional<ItemEntity> damaged(VerbTarget.Items items) {
		return items.items().stream().filter(item -> item.isAlive() && repairable(item.getItem())).findFirst();
	}

	static boolean repairable(ItemStack stack) {
		return !stack.isEmpty() && stack.isDamageableItem() && stack.isDamaged();
	}

	/**
	 * An injured construct of the mendable kind that is the caster's ally by the combat rule, or an unowned golem that
	 * is not targeting any player (NQ-04-11, CQ-17).
	 */
	public static boolean mendable(Player player, LivingEntity living) {
		if (!living.getType().is(MENDABLE) || !living.isAlive() || living.getHealth() >= living.getMaxHealth()) return false;
		return ManipulationCombatHelper.allied(player, living) || alliedConstruct(player, living) || unownedPeacefulGolem(living);
	}

	/** A Ferric wall or pillar is its maker's: mendable by its maker and the maker's allies. */
	private static boolean alliedConstruct(Player player, LivingEntity living) {
		return living instanceof IBloodConstruct construct && construct.getCreator() instanceof Player creator
				&& ManipulationCombatHelper.allied(player, creator);
	}

	private static boolean unownedPeacefulGolem(LivingEntity living) {
		return living instanceof AbstractGolem golem && !(golem instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null)
				&& !(golem.getTarget() instanceof Player);
	}

	/** An anvil one stage less worn, when {@code state} is a worn anvil. */
	static Optional<BlockState> mended(BlockState state) {
		if (state.is(Blocks.DAMAGED_ANVIL)) {
			return Optional.of(Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING)));
		}
		if (state.is(Blocks.CHIPPED_ANVIL)) {
			return Optional.of(Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING)));
		}
		return Optional.empty();
	}

	/**
	 * Items and constructs are always admitted. An anvil changes block, so a player who may not break it is refused
	 * before any blood is paid, and again on every pulse (CN-13).
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
			case VerbTarget.Items items -> damaged(items).ifPresent(item -> {
				ItemStack stack = item.getItem().copy();
				int repaired = Math.max(1, Mth.floor(REPAIR_PER_PULSE * pulse.workScale() + 0.5D));
				stack.setDamageValue(Math.max(0, stack.getDamageValue() - repaired));
				item.setItem(stack);
				spark(level, player, item.position().add(0.0D, 0.1D, 0.0D), pulse);
			});
			case VerbTarget.Creature creature -> {
				LivingEntity living = creature.living();
				living.heal((float) (HEAL_PER_PULSE * pulse.workScale()));
				spark(level, player, living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D), pulse);
			}
			case VerbTarget.Block block -> {
				BlockPos pos = block.pos();
				spark(level, player, Vec3.atBottomCenterOf(pos.above()), pulse);
				if ((pulse.index() + 1) % PULSES_PER_STAGE != 0) return;
				mended(level.getBlockState(pos)).ifPresent(next -> {
					if (PlayerBlockEdits.replace(player, pos, next)) {
						level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.7F, 1.1F);
						ManipulationVisuals.burst(level, ManipulationVisuals.Form.FERRIC_IMPACT, Vec3.atBottomCenterOf(pos.above()),
								Vec3.atCenterOf(pos), 0.7D, 16);
					}
				});
			}
			default -> {
			}
		}
	}

	/** Blood runs from the hand to the work and plates of blood-iron set on it; a soft ring of the anvil. */
	private static void spark(ServerLevel level, ServerPlayer player, Vec3 at, SculptingPulse pulse) {
		BloodFlowVisuals.toward(player, at, player.getId() * 31L + pulse.index(), BloodFlowPacket.Style.LIVING);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.FERRIC_CONJURE, at, at, 0.45D, 14);
		level.playSound(null, BlockPos.containing(at), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.18F, 1.6F);
	}

	@Override
	public int cadence() {
		return CADENCE;
	}

	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}
}
