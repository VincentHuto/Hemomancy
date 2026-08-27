package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.goal.EmptyPulseGoal;
import com.vincenthuto.hemomancy.common.entity.boss.goal.HematicCollapseGoal;
import com.vincenthuto.hemomancy.common.entity.boss.saint.HarbingerSaintEncounterHooks;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
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
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HemorathGoalGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private HemorathGoalGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void rejectedCollapseDamagePreservesBloodDebt(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		HemorathEntity boss = EntityInit.hemorath.get().create(helper.getLevel());
		helper.assertTrue(boss != null, "Hemorath could not be created");
		try {
			player.setGameMode(GameType.CREATIVE);
			player.setPos(helper.absolutePos(net.minecraft.core.BlockPos.ZERO).getCenter());
			boss.setPos(player.position());
			boss.setTarget(player);
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.addDamage(20.0D);

			HematicCollapseGoal goal = new HematicCollapseGoal(boss);
			helper.assertTrue(goal.canUse(), "Hematic Collapse did not acquire its target");
			goal.start();
			for (int tick = 0; tick <= 60; tick++) goal.tick();

			helper.assertTrue(blood.getBloodDebt() == 20.0D,
					"Rejected Hematic Collapse damage consumed the player's blood debt");
			helper.succeed();
		} finally {
			boss.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void collapseStopsWhenItsTargetEscapesRange(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		HemorathEntity boss = EntityInit.hemorath.get().create(helper.getLevel());
		helper.assertTrue(boss != null, "Hemorath could not be created");
		try {
			boss.setPos(helper.absolutePos(BlockPos.ZERO).getCenter());
			player.setPos(boss.position());
			boss.setTarget(player);

			HematicCollapseGoal goal = new HematicCollapseGoal(boss);
			helper.assertTrue(goal.canUse(), "Hematic Collapse did not acquire its target");
			goal.start();
			player.setPos(boss.getX() + 33.0D, boss.getY(), boss.getZ());

			helper.assertTrue(!goal.canContinueToUse(),
					"Hematic Collapse continued after its target escaped the cast range");
			helper.succeed();
		} finally {
			boss.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void emptyPulseUsesItsCircularRadius(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		HemorathEntity boss = EntityInit.hemorath.get().create(helper.getLevel());
		helper.assertTrue(boss != null, "Hemorath could not be created");
		Cow near = helper.spawn(EntityType.COW, new BlockPos(3, 1, 1));
		Cow outside = helper.spawn(EntityType.COW, new BlockPos(6, 1, 6));
		try {
			boss.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getCenter());
			player.setPos(boss.position());
			boss.setTarget(player);

			EmptyPulseGoal goal = new EmptyPulseGoal(boss);
			helper.assertTrue(goal.canUse(), "Empty Pulse did not acquire its target");
			goal.start();
			for (int tick = 0; tick <= 25; tick++) goal.tick();

			helper.assertTrue(near.hasEffect(EffectInit.hemophagy),
					"Empty Pulse missed a victim inside its visible radius");
			helper.assertTrue(!outside.hasEffect(EffectInit.hemophagy),
					"Empty Pulse hit a victim outside its visible radius");
			helper.succeed();
		} finally {
			boss.discard();
			player.discard();
			near.discard();
			outside.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void failedHemorathSpawnPreservesBloodDebt(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.addDamage(20.0D);
		Consumer<EntityJoinLevelEvent> cancelHemorath = event -> {
			if (event.getEntity() instanceof HemorathEntity) event.setCanceled(true);
		};
		NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, cancelHemorath);
		try {
			helper.assertTrue(!HarbingerSaintEncounterHooks.spawnSaintBoss(
					helper.getLevel(), helper.absolutePos(BlockPos.ZERO), player),
					"Canceled Hemorath spawn unexpectedly succeeded");
			helper.assertTrue(blood.getBloodDebt() == 20.0D,
					"Failed Hemorath spawn cleared the player's blood debt");
			helper.succeed();
		} finally {
			NeoForge.EVENT_BUS.unregister(cancelHemorath);
			player.discard();
		}
	}

	private static ServerPlayer player(GameTestHelper helper) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), "hemorath-goal-test"), false);
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
