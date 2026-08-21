package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.BloodInfusionManager;
import com.vincenthuto.hemomancy.common.event.BloodInfusionRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BloodInfusionGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private BloodInfusionGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void stoneCollapsesIntoOneVenousStoneAfterFiftyBlood(GameTestHelper helper) {
		BloodInfusionManager.clear();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
		ServerPlayer player = player(helper);
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(100.0D);

		for (int i = 0; i < 10; i++) {
			helper.assertTrue(BloodInfusionManager.feedBlock(player, helper.getLevel(), pos, 5.0D),
					"stone infusion was not handled");
		}
		helper.assertTrue(blood.getBloodVolume() == 50.0D, "stone infusion did not consume exactly 50 blood");
		for (int i = 0; i < BloodInfusionRules.COLLAPSE_TICKS - 1; i++) {
			BloodInfusionManager.tick(helper.getLevel());
		}
		helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.STONE),
				"source block disappeared before the collapse finished");

		BloodInfusionManager.tick(helper.getLevel());

		helper.assertTrue(!helper.getLevel().getBlockState(pos).is(Blocks.STONE),
				"source block remained after the collapse finished");
		long drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.0D),
				item -> item.getItem().is(BlockInit.venous_stone.get().asItem())).size();
		helper.assertTrue(drops == 1L, "stone infusion did not drop exactly one venous stone");
		player.discard();
		BloodInfusionManager.clear();
		helper.succeed();
	}

	private static ServerPlayer player(GameTestHelper helper) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), "blood-infusion-test"), false);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				cookie.gameProfile(), cookie.clientInformation());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
			}
		};
		return player;
	}
}
