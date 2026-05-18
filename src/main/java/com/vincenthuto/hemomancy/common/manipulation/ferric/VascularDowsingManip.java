package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Vascular Dowsing — a FERRIC HUMILIS quick manipulation that senses nearby ore veins
 * through stone, emitting coloured dust particles at each ore block's location.
 *
 * Every vein of iron bleeds. The Ferric-tended can feel it pulse.
 */
public class VascularDowsingManip extends BloodManipulation {

	private static final int SCAN_RADIUS = 8; // blocks in each direction

	// Ore-specific particle colours (RGB 0-1 range)
	private static final DustParticleOptions ORE_IRON      = new DustParticleOptions(new Vector3f(0.78f, 0.78f, 0.78f), 1.5f);
	private static final DustParticleOptions ORE_GOLD      = new DustParticleOptions(new Vector3f(1.00f, 0.84f, 0.00f), 1.5f);
	private static final DustParticleOptions ORE_DIAMOND   = new DustParticleOptions(new Vector3f(0.18f, 0.90f, 0.93f), 1.5f);
	private static final DustParticleOptions ORE_EMERALD   = new DustParticleOptions(new Vector3f(0.11f, 0.73f, 0.18f), 1.5f);
	private static final DustParticleOptions ORE_LAPIS     = new DustParticleOptions(new Vector3f(0.10f, 0.25f, 0.80f), 1.5f);
	private static final DustParticleOptions ORE_REDSTONE  = new DustParticleOptions(new Vector3f(0.90f, 0.07f, 0.07f), 1.5f);
	private static final DustParticleOptions ORE_COPPER    = new DustParticleOptions(new Vector3f(0.72f, 0.45f, 0.20f), 1.5f);
	private static final DustParticleOptions ORE_COAL      = new DustParticleOptions(new Vector3f(0.20f, 0.20f, 0.20f), 1.5f);
	private static final DustParticleOptions ORE_GENERIC   = new DustParticleOptions(new Vector3f(0.65f, 0.10f, 0.10f), 1.5f);

	public VascularDowsingManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel) || !(player instanceof ServerPlayer sPlayer)) return;

		BlockPos origin = player.blockPosition();
		List<BlockPos> found = new ArrayList<>();

		// Scan cube centered on player
		for (BlockPos p : BlockPos.betweenClosed(
				origin.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
				origin.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
			BlockState state = world.getBlockState(p);
			if (isOre(state)) {
				found.add(p.immutable());
			}
		}

		// Send coloured particle burst at each ore position, visible only to this player
		for (BlockPos ore : found) {
			DustParticleOptions colour = oreColour(world.getBlockState(ore));
			sLevel.sendParticles(sPlayer, colour, true,
					ore.getX() + 0.5, ore.getY() + 0.5, ore.getZ() + 0.5,
					12, 0.25, 0.25, 0.25, 0.0);
		}

		if (!found.isEmpty()) {
			world.playSound(null, origin, SoundEvents.AMETHYST_BLOCK_CHIME,
					SoundSource.PLAYERS, 0.6f, 1.2f);
		} else {
			world.playSound(null, origin, SoundEvents.IRON_GOLEM_DAMAGE,
					SoundSource.PLAYERS, 0.4f, 2.0f);
		}
	}

	private static boolean isOre(BlockState state) {
		return state.is(BlockTags.IRON_ORES)
				|| state.is(BlockTags.GOLD_ORES)
				|| state.is(BlockTags.DIAMOND_ORES)
				|| state.is(BlockTags.EMERALD_ORES)
				|| state.is(BlockTags.LAPIS_ORES)
				|| state.is(BlockTags.REDSTONE_ORES)
				|| state.is(BlockTags.COPPER_ORES)
				|| state.is(BlockTags.COAL_ORES);
	}

	private static DustParticleOptions oreColour(BlockState state) {
		if (state.is(BlockTags.IRON_ORES))     return ORE_IRON;
		if (state.is(BlockTags.GOLD_ORES))     return ORE_GOLD;
		if (state.is(BlockTags.DIAMOND_ORES))  return ORE_DIAMOND;
		if (state.is(BlockTags.EMERALD_ORES))  return ORE_EMERALD;
		if (state.is(BlockTags.LAPIS_ORES))    return ORE_LAPIS;
		if (state.is(BlockTags.REDSTONE_ORES)) return ORE_REDSTONE;
		if (state.is(BlockTags.COPPER_ORES))   return ORE_COPPER;
		if (state.is(BlockTags.COAL_ORES))     return ORE_COAL;
		return ORE_GENERIC;
	}
}
