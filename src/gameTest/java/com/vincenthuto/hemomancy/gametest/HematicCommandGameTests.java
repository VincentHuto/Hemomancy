package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HematicCommandGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private HematicCommandGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void commandEligibilityRejectsBloodlessAndPowerfulBodies(GameTestHelper helper) {
		Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		Skeleton skeleton = helper.spawn(EntityType.SKELETON, new BlockPos(2, 1, 1));
		Ravager ravager = helper.spawn(EntityType.RAVAGER, new BlockPos(3, 1, 1));
		try {
			helper.assertTrue(HematicCommandManager.canCommand(zombie), "Ordinary blood-bearing mob was rejected");
			helper.assertTrue(!HematicCommandManager.canCommand(skeleton), "Bloodless skeleton accepted command");
			helper.assertTrue(!HematicCommandManager.canCommand(ravager), "Powerful ravager accepted command");
			helper.succeed();
		} finally {
			zombie.discard();
			skeleton.discard();
			ravager.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void rebukeClearsTargetsAndImpressmentReplacesThePriorBody(GameTestHelper helper) {
		ServerPlayer caster = player(helper);
		Zombie first = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		Zombie second = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 1, 1));
		first.setTarget(caster);
		helper.assertTrue(HematicCommandManager.rebuke(caster, first), "Rebuke rejected a valid target");
		try {
			helper.assertTrue(first.getTarget() == null && HematicCommandManager.isRebuked(first),
					"Rebuke did not clear and suppress the caster target");
			helper.assertTrue(HematicCommandManager.impress(caster, first), "Impressment rejected the first target");
			helper.assertTrue(HematicCommandManager.impress(caster, second), "Impressment rejected the replacement target");
			helper.assertTrue(!HematicCommandManager.isImpressed(first, caster),
					"The previous impressed body remained under command");
			helper.assertTrue(HematicCommandManager.isImpressed(second, caster),
					"The replacement impressed body was not tracked");
			helper.succeed();
		} finally {
			HematicCommandManager.clearSessionState();
			first.discard();
			second.discard();
			caster.discard();
		}
	}

	private static ServerPlayer player(GameTestHelper helper) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), "hematic-command-test"), false);
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
