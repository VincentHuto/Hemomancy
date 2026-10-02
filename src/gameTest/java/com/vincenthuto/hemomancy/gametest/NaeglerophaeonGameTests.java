package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.worldgen.VagrantMindEncounterData;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NaeglerophaeonGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE)
	public static void boundBossDeathPermanentlyClearsItsMind(GameTestHelper h) {
		BlockPos node = new BlockPos(3, 2, 3);
		h.setBlock(node, BlockInit.synaptic_node.get());
		var boss = h.spawn(EntityInit.naeglerophaeon.get(), node.above());
		BlockPos origin = h.absolutePos(new BlockPos(3, 3, 3));
		boss.bindMind(origin, 37L, List.of(h.absolutePos(node)));
		VagrantMindEncounterData data = VagrantMindEncounterData.get(h.getLevel());
		data.active(origin, boss.getUUID(), boss.blockPosition());
		boss.hurt(h.getLevel().damageSources().generic(), 1000.0F);
		h.assertTrue(data.encounter(origin).defeated(), "a killed resident must never respawn");
		h.succeed();
	}

	@GameTest(template = "empty")
	public static void synapticStepTravelsBetweenBuiltNodes(GameTestHelper h) {
		BlockPos source = new BlockPos(2, 4, 2);
		BlockPos destination = new BlockPos(7, 4, 2);
		h.setBlock(source, BlockInit.synaptic_node.get());
		h.setBlock(destination, BlockInit.synaptic_node.get());
		var player = h.makeMockPlayer(GameType.SURVIVAL);
		BlockPos start = h.absolutePos(source.above());
		player.setPos(start.getX() + 0.5, start.getY(), start.getZ() + 0.5);
		var destinationPos = h.absolutePos(destination);
		var aim = new net.minecraft.world.phys.Vec3(destinationPos.getX() + 0.5,
				destinationPos.getY() + 0.99, destinationPos.getZ() + 0.5).subtract(player.getEyePosition());
		player.setYRot((float) Math.toDegrees(Math.atan2(aim.z, aim.x)) - 90.0F);
		player.setXRot((float) -Math.toDegrees(Math.atan2(aim.y, Math.hypot(aim.x, aim.z))));
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.synaptic_step.get()));
		BlockPos obstruction = new BlockPos(5, 5, 2);
		h.setBlock(obstruction, net.minecraft.world.level.block.Blocks.BARRIER);
		var blocked = ItemInit.synaptic_step.get().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
		h.assertTrue(blocked.getResult() == net.minecraft.world.InteractionResult.FAIL
				&& player.blockPosition().equals(start)
				&& !player.getCooldowns().isOnCooldown(ItemInit.synaptic_step.get()),
				"a blocked sightline must not move the player or spend the jump cooldown");
		h.setBlock(obstruction, net.minecraft.world.level.block.Blocks.AIR);
		var eye = player.getEyePosition();
		var hit = h.getLevel().clip(new net.minecraft.world.level.ClipContext(eye,
				eye.add(player.getLookAngle().scale(35)), net.minecraft.world.level.ClipContext.Block.COLLIDER,
				net.minecraft.world.level.ClipContext.Fluid.NONE, player));
		var result = ItemInit.synaptic_step.get().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
		h.assertTrue(player.blockPosition().closerThan(h.absolutePos(destination.above()), 1.5),
				"the player must arrive on the targeted node: result=" + result.getResult()
						+ " source=" + start + " destination=" + h.absolutePos(destination)
						+ " actual=" + player.position() + " eye=" + eye
						+ " hit=" + hit.getType() + ":" + hit.getBlockPos()
						+ " hitState=" + h.getLevel().getBlockState(hit.getBlockPos())
						+ " landing=" + h.getBlockState(destination.above())
						+ " headroom=" + h.getBlockState(destination.above(2)));
		h.assertTrue(player.getCooldowns().isOnCooldown(ItemInit.synaptic_step.get()),
				"a successful jump must apply cooldown");
		h.succeed();
	}
}
