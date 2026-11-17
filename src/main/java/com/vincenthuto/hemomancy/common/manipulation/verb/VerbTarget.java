package com.vincenthuto.hemomancy.common.manipulation.verb;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The one thing a verb is aimed at, as {@link VerbTargeting#pick} found it. Both sides build it the same way, so the
 * hand's stream lands where the server acts.
 */
public sealed interface VerbTarget {
	VerbTargetKind kind();

	/** Where the verb meets the target, in world space. */
	Vec3 point();

	/**
	 * Stays the same while the player keeps aiming at the same thing (the same block, fluid, creature or heap of items,
	 * or open air), so a Sculpting session survives a wobble of the aim but ends when the target changes.
	 */
	String key();

	record Block(BlockHitResult hit, BlockState state) implements VerbTarget {
		public BlockPos pos() {
			return hit.getBlockPos();
		}

		@Override
		public VerbTargetKind kind() {
			return VerbTargetKind.BLOCK;
		}

		@Override
		public Vec3 point() {
			return hit.getLocation();
		}

		@Override
		public String key() {
			return "block:" + pos().asLong();
		}
	}

	record Fluid(BlockPos pos, FluidState fluid, Vec3 point) implements VerbTarget {
		@Override
		public VerbTargetKind kind() {
			return VerbTargetKind.FLUID;
		}

		@Override
		public String key() {
			return "fluid:" + pos.asLong();
		}
	}

	record Creature(LivingEntity living, Vec3 point) implements VerbTarget {
		@Override
		public VerbTargetKind kind() {
			return VerbTargetKind.CREATURE;
		}

		@Override
		public String key() {
			return "creature:" + living.getId();
		}
	}

	/** Item entities lying together; {@code focus} is the one the aim found, the list holds it and its neighbours. */
	record Items(List<ItemEntity> items, Vec3 focus) implements VerbTarget {
		public Items {
			items = List.copyOf(items);
		}

		@Override
		public VerbTargetKind kind() {
			return VerbTargetKind.ITEMS;
		}

		@Override
		public Vec3 point() {
			return focus;
		}

		@Override
		public String key() {
			return "items:" + (items.isEmpty() ? -1 : items.getFirst().getId());
		}
	}

	/** Open air at full reach: nothing stood in the way. */
	record Air(Vec3 point) implements VerbTarget {
		@Override
		public VerbTargetKind kind() {
			return VerbTargetKind.AIR;
		}

		@Override
		public String key() {
			return "air";
		}
	}
}
