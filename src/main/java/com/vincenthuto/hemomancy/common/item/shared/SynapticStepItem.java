package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/** Short hops between visible synaptic nodes, including player-built networks. */
public final class SynapticStepItem extends Item {
	private static final LightningTestConfig ARC = new LightningTestConfig(
			LightningTestConfig.Backend.BOLT, 0xE8FFE84A, 0xE8FFE84A, 0xFFFFFFFF,
			32.0F, 0.0F, 0.0F, 0.0F, 42.0F, 1.4F, 5, 5, 0.045F, 0.009F, false, 0L, false, 8);
	private static final ParticleColor GOLD = new ParticleColor(255, 232, 74);

	public SynapticStepItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.hemomancy.synaptic_step.tooltip").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(level instanceof ServerLevel server)) return InteractionResultHolder.sidedSuccess(stack, true);
		if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
		BlockPos source = nearestSource(server, player);
		if (source == null) return failure(player, stack, "source");
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		double rayLength = 35.0;
		for (int step = 0; step <= 35; step++) {
			if (!server.hasChunkAt(BlockPos.containing(eye.add(look.scale(step))))) {
				rayLength = Math.max(0.0, step - 1.0);
				break;
			}
		}
		Vec3 end = eye.add(look.scale(rayLength));
		HitResult hit = server.clip(new ClipContext(eye, end,
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK)
			return failure(player, stack, "target");
		BlockPos destinationNode = blockHit.getBlockPos();
		if (source.equals(destinationNode) || source.distSqr(destinationNode) > 32 * 32
				|| !server.hasChunkAt(destinationNode) || !server.getBlockState(destinationNode).is(BlockInit.synaptic_node.get()))
			return failure(player, stack, "target");
		BlockPos standing = destinationNode.above();
		if (!server.hasChunkAt(standing) || !server.hasChunkAt(standing.above())
				|| !server.getBlockState(standing).isAir() || !server.getBlockState(standing.above()).isAir())
			return failure(player, stack, "blocked");
		Vec3 landing = new Vec3(standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5);
		if (!server.noCollision(player, player.getBoundingBox().move(landing.subtract(player.position()))))
			return failure(player, stack, "blocked");
		Vec3 from = player.position().add(0, 1, 0);
		player.teleportTo(landing.x, landing.y, landing.z);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0;
		player.getCooldowns().addCooldown(this, 200);
		LightningTesterSpawner.spawn(server, from, landing.add(0, 1, 0), ARC);
		server.sendParticles(HemoParticleData.glow(GOLD), landing.x, landing.y + 1, landing.z,
				16, 0.25, 0.5, 0.25, 0);
		server.playSound(null, landing.x, landing.y, landing.z, SoundInit.ITEM_SYNAPTIC_STEP_USE.get(),
				net.minecraft.sounds.SoundSource.PLAYERS, 0.9F, 1.1F);
		return InteractionResultHolder.success(stack);
	}

	private static BlockPos nearestSource(ServerLevel level, Player player) {
		BlockPos center = player.blockPosition();
		BlockPos nearest = null;
		double distance = 9.0;
		for (BlockPos node : BlockPos.betweenClosed(center.offset(-3, -3, -3), center.offset(3, 3, 3))) {
			if (!level.hasChunkAt(node) || !level.getBlockState(node).is(BlockInit.synaptic_node.get())) continue;
			double candidate = player.distanceToSqr(Vec3.atCenterOf(node));
			if (candidate < distance) {
				nearest = node.immutable();
				distance = candidate;
			}
		}
		return nearest;
	}

	private static InteractionResultHolder<ItemStack> failure(Player player, ItemStack stack, String reason) {
		player.displayClientMessage(Component.translatable("message.hemomancy.synaptic_step." + reason), true);
		return InteractionResultHolder.fail(stack);
	}
}
