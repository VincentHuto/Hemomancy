package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Vascular Dowsing — a FERRIC HUMILIS continuous manipulation that senses nearby ore veins
 * through stone, marking each ore block with glow motes in its vein's colour.
 *
 * Every vein of iron bleeds. The Ferric-tended can feel it pulse.
 */
public class VascularDowsingManip extends BloodManipulation {

	private static final int SCAN_RADIUS = 8; // blocks in each direction

	private static final ParticleColor ORE_IRON = new ParticleColor(199, 199, 199);
	private static final ParticleColor ORE_GOLD = new ParticleColor(255, 214, 0);
	private static final ParticleColor ORE_DIAMOND = new ParticleColor(46, 230, 237);
	private static final ParticleColor ORE_EMERALD = new ParticleColor(28, 186, 46);
	private static final ParticleColor ORE_LAPIS = new ParticleColor(26, 64, 204);
	private static final ParticleColor ORE_REDSTONE = new ParticleColor(230, 18, 18);
	private static final ParticleColor ORE_COPPER = new ParticleColor(184, 115, 51);
	private static final ParticleColor ORE_COAL = new ParticleColor(51, 51, 51);
	private static final ParticleColor ORE_GENERIC = new ParticleColor(166, 26, 26);

	public VascularDowsingManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel sLevel) || !(player instanceof ServerPlayer sPlayer)) return;

		BlockPos origin = player.blockPosition();
		List<BlockPos> found = new ArrayList<>();

		for (BlockPos p : BlockPos.betweenClosed(
				origin.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
				origin.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
			if (!world.hasChunkAt(p)) continue;
			BlockState state = world.getBlockState(p);
			if (isOre(state)) {
				found.add(p.immutable());
			}
		}

		for (BlockPos ore : found.stream().sorted(java.util.Comparator.comparingDouble(origin::distSqr)).limit(24).toList()) {
            PacketHandler.sendToPlayer(sPlayer, new com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket(
                    com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.ORE, -1,
                    net.minecraft.world.phys.Vec3.atCenterOf(ore), net.minecraft.world.phys.Vec3.atCenterOf(ore), .5F, 25, 1));
			// The vein's blood colour, as fine glow motes on the ore itself.
			sLevel.sendParticles(sPlayer, HemoParticleData.glow(oreColour(world.getBlockState(ore))), true,
					ore.getX() + 0.5, ore.getY() + 0.5, ore.getZ() + 0.5,
					12, 0.25, 0.25, 0.25, 0.0);
		}

		if (!found.isEmpty()) {
			BlockPos nearest = found.stream().min(java.util.Comparator.comparingDouble(origin::distSqr)).orElseThrow();
			player.displayClientMessage(HemoText.translatable("message.hemomancy.vascular_dowsing_manip.dowsing_ore_blocks_nearest_at",
					found.size(),
					world.getBlockState(nearest).getBlock().getName().getString(),
					nearest.getX(),
					nearest.getY(),
					nearest.getZ()), true);
			world.playSound(null, origin, SoundEvents.AMETHYST_BLOCK_CHIME,
					SoundSource.PLAYERS, 0.6f, 1.2f);
		} else {
			player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.hemomancy.vascular_dowsing_manip.dowsing_no_ore_within_8_blocks"), true);
			world.playSound(null, origin, SoundEvents.IRON_GOLEM_DAMAGE,
					SoundSource.PLAYERS, 0.4f, 2.0f);
		}
	        }
    }

	private static boolean isOre(BlockState state) {
		return state.is(net.neoforged.neoforge.common.Tags.Blocks.ORES) || state.is(BlockTags.IRON_ORES)
				|| state.is(BlockTags.GOLD_ORES)
				|| state.is(BlockTags.DIAMOND_ORES)
				|| state.is(BlockTags.EMERALD_ORES)
				|| state.is(BlockTags.LAPIS_ORES)
				|| state.is(BlockTags.REDSTONE_ORES)
				|| state.is(BlockTags.COPPER_ORES)
				|| state.is(BlockTags.COAL_ORES);
	}

	private static ParticleColor oreColour(BlockState state) {
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
