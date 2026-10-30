package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.heartnode.HeartNodeSettingsPacket;
import com.vincenthuto.hemomancy.common.network.heartnode.PacketOpenHeartNodeScreen;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import com.vincenthuto.hemomancy.common.world.HeartNodeService;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/**
 * The Heart-node screen's server side: who it opens for, what the snapshot carries, and the settings packet's guards.
 * The GameTest server has no Gardens level, so the node is planted in the test level; nothing checked here reads the
 * dimension. The client screen itself is proved by the heart-node review client.
 */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HeartNodeScreenGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";
	private static final BlockPos NODE = new BlockPos(8, 2, 8);

	private HeartNodeScreenGameTests() {
	}

	/** The owner gets the screen with their sanctum's state; a stranger is told it isn't theirs and gets nothing. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void onlyTheOwnerOpensTheirNode(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		BlockPos heart = helper.absolutePos(NODE);
		try (CapturedPlayer owner = CapturedPlayer.create(helper); CapturedPlayer stranger = CapturedPlayer.create(helper)) {
			HeartNodeBlockEntity node = plant(helper, owner);
			BlockPos waiting = helper.absolutePos(new BlockPos(4, 2, 4));
			BlockPos gone = helper.absolutePos(new BlockPos(12, 2, 4));
			helper.setBlock(new BlockPos(4, 2, 4), BlockInit.fruiting_body_cache.get());
			data.addCache(owner.player.getUUID(), waiting);
			data.addCache(owner.player.getUUID(), gone);
			try {
				standBeside(helper, owner, 1);
				HeartNodeService.open(owner.player, node);
				PacketOpenHeartNodeScreen opened = owner.payloads.stream()
						.filter(PacketOpenHeartNodeScreen.class::isInstance).map(PacketOpenHeartNodeScreen.class::cast)
						.findFirst().orElse(null);
				helper.assertTrue(opened != null, "The owner's screen opens");
				helper.assertTrue(heart.equals(opened.pos()) && owner.player.getUUID().equals(opened.owner())
						&& !opened.readOnly() && HeartNodeSettings.DEFAULT.equals(opened.settings()),
						"The owner's screen is their node, editable, showing its settings");
				helper.assertTrue(opened.caches().equals(List.of(waiting)),
						"A cache still standing is listed; a record whose block is gone is dropped: " + opened.caches());
				helper.assertTrue(data.caches(owner.player.getUUID()).equals(List.of(waiting)),
						"The stale record is forgotten");

				standBeside(helper, stranger, 1);
				HeartNodeService.open(stranger.player, node);
				helper.assertTrue(stranger.payloads.stream().noneMatch(PacketOpenHeartNodeScreen.class::isInstance),
						"A stranger's screen stays shut");
				helper.assertTrue(stranger.said().contains("answers to someone else"),
						"The stranger is told whose it is: " + stranger.said());

				helper.setBlock(new BlockPos(4, 2, 4), Blocks.AIR);
				helper.assertTrue(data.caches(owner.player.getUUID()).isEmpty(),
						"Opening or breaking a cache takes it off the waiting list");
				helper.succeed();
			} finally {
				data.remove(owner.player.getUUID(), heart);
				data.removeCache(waiting);
				data.removeCache(gone);
				helper.setBlock(NODE, Blocks.AIR);
			}
		}
	}

	/**
	 * Only the owner's packets from within block reach change the settings. A stranger's packet, the owner's from out
	 * of reach and one aimed at a block that is not the node are ignored, and no setting touches the sanctum's rules.
	 */
	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void settingsPacketsAreGuarded(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		BlockPos heart = helper.absolutePos(NODE);
		try (CapturedPlayer owner = CapturedPlayer.create(helper); CapturedPlayer stranger = CapturedPlayer.create(helper)) {
			plant(helper, owner);
			UUID id = owner.player.getUUID();
			HeartNodeSettings dark = new HeartNodeSettings(0, 0);
			try {
				double strengthBefore = data.ownerStrength(id, heart.east(3));
				boolean strangerBefore = data.strangerTo(stranger.player.getUUID(), heart.east(3));

				standBeside(helper, stranger, 1);
				helper.assertFalse(HeartNodeSettingsPacket.accept(stranger.player, new HeartNodeSettingsPacket(heart, dark)),
						"A stranger's packet is ignored");
				helper.assertTrue(HeartNodeSettings.DEFAULT.equals(data.entry(id).settings()), "and changes nothing");

				standBeside(helper, owner, 20);
				helper.assertFalse(HeartNodeSettingsPacket.accept(owner.player, new HeartNodeSettingsPacket(heart, dark)),
						"The owner's packet from out of reach is ignored");
				helper.assertTrue(HeartNodeSettings.DEFAULT.equals(data.entry(id).settings()), "and changes nothing");

				standBeside(helper, owner, 1);
				helper.assertFalse(HeartNodeSettingsPacket.accept(owner.player,
						new HeartNodeSettingsPacket(heart.east(2), dark)), "A packet aimed beside the node is ignored");
				helper.assertTrue(HeartNodeSettingsPacket.accept(owner.player, new HeartNodeSettingsPacket(heart, dark)),
						"The owner's packet in reach is applied");
				helper.assertTrue(dark.equals(data.entry(id).settings()), "and saved on the entry");

				helper.assertTrue(data.ownerStrength(id, heart.east(3)) == strengthBefore
								&& data.strangerTo(stranger.player.getUUID(), heart.east(3)) == strangerBefore
								&& data.insideOwnEnvelope(id, heart.east(3)),
						"With everything off the owner's benefit and the stranger's refusal are unchanged");
				helper.succeed();
			} finally {
				data.remove(id, heart);
				helper.setBlock(NODE, Blocks.AIR);
			}
		}
	}

	private static HeartNodeBlockEntity plant(GameTestHelper helper, CapturedPlayer owner) {
		ServerLevel level = helper.getLevel();
		BlockPos heart = helper.absolutePos(NODE);
		helper.setBlock(NODE, BlockInit.heart_node.get());
		HeartNodeBlockEntity node = (HeartNodeBlockEntity) level.getBlockEntity(heart);
		node.plant(owner.player.getUUID(), level.getGameTime());
		HeartNodeSavedData.get(level).plant(owner.player.getUUID(), heart, level.getGameTime());
		return node;
	}

	/** Puts the player {@code distance} blocks south of the node, on its level. */
	private static void standBeside(GameTestHelper helper, CapturedPlayer player, int distance) {
		BlockPos heart = helper.absolutePos(NODE);
		player.player.moveTo(heart.getX() + 0.5D, heart.getY(), heart.getZ() + 0.5D + distance, 0f, 0f);
	}
}
