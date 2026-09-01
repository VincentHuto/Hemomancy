package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.HematicIronBarsBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.HematicStakeBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BloodCrystalBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.event.BloodStructureFeedManager;
import com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneFootprint;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodStructureCraftingHelper;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HematicStakeBloodStructureGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private HematicStakeBloodStructureGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void formationPlacesAndRegistersAllSixStakeDirections(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		setServerPlayerLookup(player, true);
		UUID bloodlineId = UUID.randomUUID();
		ArrayList<UUID> members = new ArrayList<>();
		for (int i = 0; i < 5; i++) members.add(UUID.randomUUID());
		Bloodline bloodline = new Bloodline("Stake Test", player.getUUID(), bloodlineId, members);
		BloodlineSavedData.get(helper.getLevel().getServer().overworld()).registerBloodline(bloodline);
		HemoCapabilityAccess.requireBloodVolume(player).setBloodLine(bloodline);
		HemoCapabilityAccess.requireBloodVolume(player).setBloodVolume(1_000.0D);
		FoundingFaneSavedData fanes = FoundingFaneSavedData.get(helper.getLevel());
		fanes.consecrateHeart(player.getUUID(), helper.absolutePos(new BlockPos(8, 2, 8)));

		try {
			Direction[] directions = Direction.values();
			for (int index = 0; index < directions.length; index++) {
				Direction direction = directions[index];
				BlockPos bar = new BlockPos(3 + index % 3 * 4, 4 + index / 3 * 5, 8);
				BlockPos crystal = bar.relative(direction);
				BlockPos support = bar.relative(direction.getOpposite());
				helper.setBlock(support, Blocks.STONE);
				helper.setBlock(bar, BlockInit.hematic_iron_bars.get().defaultBlockState()
						.setValue(HematicIronBarsBlock.AXIS, direction.getAxis()));
				helper.setBlock(crystal, BlockInit.blood_crystal.get().defaultBlockState()
						.setValue(BloodCrystalBlock.FACING, direction));

				ItemStack catalyst = new ItemStack(ItemInit.sanguine_formation.get());
				double bloodBefore = HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume();
				helper.assertTrue(BloodStructureFeedManager.feedStructure(player, helper.getLevel(),
						helper.absolutePos(bar), catalyst, 100.0D),
						"The stake formation must accept " + direction + " projection");
				helper.assertTrue(catalyst.isEmpty(), "The Sanguine Formation must be consumed");
				helper.assertTrue(HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume() == bloodBefore - 100.0D,
						"The stake formation must consume 100 blood");
				for (int tick = 0; tick < 30; tick++) PendingBloodCraftManager.tick();

				helper.assertBlockPresent(BlockInit.hematic_stake.get(), bar);
				helper.assertBlockNotPresent(BlockInit.blood_crystal.get(), crystal);
				if (direction.getAxis().isVertical()) {
					helper.assertBlockProperty(bar, HematicStakeBlock.FACE,
							direction == Direction.UP ? AttachFace.FLOOR : AttachFace.CEILING);
				} else {
					helper.assertBlockProperty(bar, HematicStakeBlock.FACE, AttachFace.WALL);
					helper.assertBlockProperty(bar, HematicStakeBlock.FACING, direction);
				}
				helper.assertTrue(player.getUUID().equals(fanes.findOwnerForStake(helper.absolutePos(bar))),
						"The manifested stake must be registered to its Progenitor");
			}
			helper.succeed();
		} finally {
			fanes.remove(player.getUUID());
			BloodlineSavedData.get(helper.getLevel().getServer().overworld()).disbandBloodline(bloodlineId);
			setServerPlayerLookup(player, false);
			BloodStructureFeedManager.clear();
			PendingBloodCraftManager.clear();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void invalidFormationsFailBeforeConsumingBloodOrCatalyst(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		UUID bloodlineId = UUID.randomUUID();
		Bloodline bloodline = new Bloodline("Invalid Stake Test", player.getUUID(), bloodlineId, new ArrayList<>());
		BloodlineSavedData lines = BloodlineSavedData.get(helper.getLevel().getServer().overworld());
		lines.registerBloodline(bloodline);
		HemoCapabilityAccess.requireBloodVolume(player).setBloodLine(bloodline);
		HemoCapabilityAccess.requireBloodVolume(player).setBloodVolume(500.0D);
		FoundingFaneSavedData fanes = FoundingFaneSavedData.get(helper.getLevel());
		BlockPos heart = helper.absolutePos(new BlockPos(8, 2, 8));
		fanes.consecrateHeart(player.getUUID(), heart);
		ItemStack catalyst = new ItemStack(ItemInit.sanguine_formation.get());

		try {
			BlockPos bar = new BlockPos(8, 5, 8);
			placeFormation(helper, bar, Direction.UP, Direction.Axis.X, true);
			assertRejectedWithoutConsumption(helper, player, bar, catalyst, "perpendicular crystal");

			placeFormation(helper, bar, Direction.EAST, Direction.Axis.X, false);
			assertRejectedWithoutConsumption(helper, player, bar, catalyst, "missing support");

			UUID otherLeader = UUID.randomUUID();
			UUID otherLineId = UUID.randomUUID();
			Bloodline otherLine = new Bloodline("Other Progenitor", otherLeader, otherLineId,
					new ArrayList<>(java.util.List.of(player.getUUID())));
			lines.registerBloodline(otherLine);
			HemoCapabilityAccess.requireBloodVolume(player).setBloodLine(otherLine);
			fanes.consecrateHeart(otherLeader, heart);
			placeFormation(helper, bar, Direction.EAST, Direction.Axis.X, true);
			assertRejectedWithoutConsumption(helper, player, bar, catalyst, "non-Progenitor");
			lines.disbandBloodline(otherLineId);
			fanes.remove(otherLeader);

			HemoCapabilityAccess.requireBloodVolume(player).setBloodLine(bloodline);
			fanes.consecrateHeart(player.getUUID(), heart);
			for (int index = 0; index < FaneFootprint.BASE_STAKE_BUDGET + 1; index++) {
				helper.assertTrue(fanes.addStake(player.getUUID(), heart.offset(index + 1, 0, 0),
						FaneFootprint.BASE_STAKE_BUDGET + 1), "The test must fill the stake budget");
			}
			placeFormation(helper, bar, Direction.EAST, Direction.Axis.X, true);
			assertRejectedWithoutConsumption(helper, player, bar, catalyst, "full stake budget");
			helper.succeed();
		} finally {
			fanes.remove(player.getUUID());
			lines.disbandBloodline(bloodlineId);
			BloodStructureFeedManager.clear();
			PendingBloodCraftManager.clear();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void delayedInvalidationFallsBackToTheStakeItem(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		setServerPlayerLookup(player, true);
		UUID bloodlineId = UUID.randomUUID();
		Bloodline bloodline = new Bloodline("Delayed Stake Test", player.getUUID(), bloodlineId, new ArrayList<>());
		BloodlineSavedData lines = BloodlineSavedData.get(helper.getLevel().getServer().overworld());
		lines.registerBloodline(bloodline);
		HemoCapabilityAccess.requireBloodVolume(player).setBloodLine(bloodline);
		HemoCapabilityAccess.requireBloodVolume(player).setBloodVolume(500.0D);
		FoundingFaneSavedData fanes = FoundingFaneSavedData.get(helper.getLevel());
		fanes.consecrateHeart(player.getUUID(), helper.absolutePos(new BlockPos(8, 2, 8)));
		BlockPos bar = new BlockPos(8, 5, 8);
		placeFormation(helper, bar, Direction.EAST, Direction.Axis.X, true);

		try {
			ItemStack catalyst = new ItemStack(ItemInit.sanguine_formation.get());
			helper.assertTrue(BloodStructureFeedManager.feedStructure(player, helper.getLevel(),
					helper.absolutePos(bar), catalyst, 100.0D), "The valid formation must begin crafting");
			helper.setBlock(bar.west(), Blocks.AIR);
			for (int tick = 0; tick < 30; tick++) PendingBloodCraftManager.tick();

			helper.assertBlockNotPresent(BlockInit.hematic_stake.get(), bar);
			BlockPos absoluteBar = helper.absolutePos(bar);
			helper.assertTrue(!helper.getLevel().getEntitiesOfClass(ItemEntity.class,
					new AABB(absoluteBar).inflate(2.0D), entity -> entity.getItem().is(BlockInit.hematic_stake.get().asItem()))
					.isEmpty(), "A late invalidation must return the Hematic Stake as an item");
			helper.succeed();
		} finally {
			fanes.remove(player.getUUID());
			lines.disbandBloodline(bloodlineId);
			setServerPlayerLookup(player, false);
			BloodStructureFeedManager.clear();
			PendingBloodCraftManager.clear();
			player.discard();
		}
	}

	private static void placeFormation(GameTestHelper helper, BlockPos bar, Direction crystalDirection,
			Direction.Axis barAxis, boolean withSupport) {
		for (Direction direction : Direction.values()) helper.setBlock(bar.relative(direction), Blocks.AIR);
		if (withSupport) helper.setBlock(bar.relative(crystalDirection.getOpposite()), Blocks.STONE);
		helper.setBlock(bar, BlockInit.hematic_iron_bars.get().defaultBlockState()
				.setValue(HematicIronBarsBlock.AXIS, barAxis));
		helper.setBlock(bar.relative(crystalDirection), BlockInit.blood_crystal.get().defaultBlockState()
				.setValue(BloodCrystalBlock.FACING, crystalDirection));
	}

	private static void assertRejectedWithoutConsumption(GameTestHelper helper, ServerPlayer player, BlockPos bar,
			ItemStack catalyst, String reason) {
		double bloodBefore = HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume();
		int catalystBefore = catalyst.getCount();
		var match = BloodStructureCraftingHelper.findProjectionCraftMatch(player, helper.getLevel(),
				helper.absolutePos(bar), catalyst);
		helper.assertTrue(match.isPresent() && !match.orElseThrow().valid(), reason + " must be rejected");
		helper.assertTrue(HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume() == bloodBefore,
				reason + " must not drain blood");
		helper.assertTrue(catalyst.getCount() == catalystBefore, reason + " must not consume the catalyst");
	}

	private static ServerPlayer connectedPlayer(GameTestHelper helper) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), "stake-formation-player"), false);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				cookie.gameProfile(), ClientInformation.createDefault());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
			}
		};
		return player;
	}

	@SuppressWarnings("unchecked")
	private static void setServerPlayerLookup(ServerPlayer player, boolean present) {
		try {
			var field = net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");
			field.setAccessible(true);
			var players = (java.util.Map<UUID, ServerPlayer>) field.get(player.server.getPlayerList());
			if (present) players.put(player.getUUID(), player);
			else players.remove(player.getUUID(), player);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not register the stake GameTest player", exception);
		}
	}
}
