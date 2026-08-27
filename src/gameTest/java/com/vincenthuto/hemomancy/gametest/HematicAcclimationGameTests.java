package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
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
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HematicAcclimationGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";
	private static final double EPSILON = 0.000001D;

	private HematicAcclimationGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void entityTypeExposureAttenuatesDamageAndBloodTogether(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		Cow cow = helper.spawn(EntityType.COW, new BlockPos(1, 1, 1));
		Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 1, 1));
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setMaxBloodVolume(5000.0D);
			blood.setBloodVolume(0.0D);
			long now = helper.getLevel().getGameTime();
			HemoCapabilityAccess.getPowerGuardrails(player)
					.recordHematicExposure("minecraft:cow", 750.0D, now);
			double cowHealthBefore = cow.getHealth();

			double cowAbsorbed = BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, cow, 4.0D);
			double zombieAbsorbed = BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, zombie, 4.0D);

			assertClose(helper, 2.0D, cowAbsorbed, "Cow exposure did not halve absorbed blood");
			assertClose(helper, cowHealthBefore - 2.0D, cow.getHealth(), "Cow exposure did not halve drain damage");
			assertClose(helper, 4.0D, zombieAbsorbed, "Cow exposure leaked into the zombie pool");
			assertClose(helper, 16.0D, zombie.getHealth(), "Zombie damage was attenuated by another entity type");
			assertClose(helper, 6.0D, blood.getBloodVolume(), "Damage and blood yield diverged");
			helper.succeed();
		} finally {
			cow.discard();
			zombie.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void quarterAndSaturatedBandsControlOrdinaryDrain(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		Cow cow = helper.spawn(EntityType.COW, new BlockPos(1, 1, 1));
		Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 1, 1));
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setMaxBloodVolume(5000.0D);
			blood.setBloodVolume(0.0D);
			long now = helper.getLevel().getGameTime();
			var state = HemoCapabilityAccess.getPowerGuardrails(player);
			state.recordHematicExposure("minecraft:cow", 1250.0D, now);
			state.recordHematicExposure("minecraft:zombie", 1500.0D, now);
			double cowHealthBefore = cow.getHealth();

			double cowAbsorbed = BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, cow, 4.0D);
			double zombieAbsorbed = BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, zombie, 4.0D);

			assertClose(helper, 1.0D, cowAbsorbed, "Quarter band did not quarter absorbed blood");
			assertClose(helper, cowHealthBefore - 1.0D, cow.getHealth(), "Quarter band did not quarter drain damage");
			assertClose(helper, 0.0D, zombieAbsorbed, "Saturated target still yielded blood");
			assertClose(helper, 20.0D, zombie.getHealth(), "Saturated target still took drain damage");
			assertClose(helper, 1.0D, blood.getBloodVolume(), "Saturated target changed blood yield");
			helper.succeed();
		} finally {
			cow.discard();
			zombie.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void exposureRecordsOnlyHealthActuallyRemoved(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		Cow cow = helper.spawn(EntityType.COW, new BlockPos(1, 1, 1));
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setMaxBloodVolume(5000.0D);
			blood.setBloodVolume(0.0D);
			cow.setHealth(1.5F);

			double absorbed = BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, cow, 4.0D);
			double exposure = HemoCapabilityAccess.getPowerGuardrails(player)
					.hematicExposure("minecraft:cow", helper.getLevel().getGameTime());

			assertClose(helper, 1.5D, absorbed, "Drain credited more blood than remaining health");
			assertClose(helper, 1.5D, blood.getBloodVolume(), "Blood volume exceeded actual health removed");
			assertClose(helper, 1.5D, exposure, "Exposure exceeded actual health removed");
			helper.succeed();
		} finally {
			cow.discard();
			player.discard();
		}
	}

	private static void assertClose(GameTestHelper helper, double expected, double actual, String message) {
		helper.assertTrue(Math.abs(expected - actual) <= EPSILON,
				message + ": expected " + expected + " but got " + actual);
	}

	private static ServerPlayer player(GameTestHelper helper) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), "hematic-acclimation-test"), false);
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
