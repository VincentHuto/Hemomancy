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
		// Ordinary damage cannot skip the Overload finale; kill damage stands in for a finished fight.
		boss.kill();
		h.assertTrue(data.encounter(origin).defeated(), "a killed resident must never respawn");
		h.succeed();
	}

	@GameTest(template = "empty")
	public static void dormantSynapticStepCannotTravelBetweenBuiltNodes(GameTestHelper h) {
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
		h.assertTrue(blocked.getResult() == net.minecraft.world.InteractionResult.PASS
				&& player.blockPosition().equals(start)
				&& !player.getCooldowns().isOnCooldown(ItemInit.synaptic_step.get()),
				"a dormant tool must remain inert with an obstructed route");
		h.setBlock(obstruction, net.minecraft.world.level.block.Blocks.AIR);
		var result = ItemInit.synaptic_step.get().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
		h.assertTrue(result.getResult() == net.minecraft.world.InteractionResult.PASS && player.blockPosition().equals(start),
				"removing the obstruction must not restore the retired teleport");
		h.assertTrue(!player.getCooldowns().isOnCooldown(ItemInit.synaptic_step.get()),
				"a dormant tool must not spend a cooldown");
		h.succeed();
	}
}
