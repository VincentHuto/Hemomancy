package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MycelialSynapseBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.event.BloodStructureFeedManager;
import com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.rite.structure.BloodStructureCraftingHelper;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity;
import com.vincenthuto.hemomancy.common.util.PatternOrigins;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/**
 * Mycelial Synapses: the pattern found on all six faces of the iron, the craft ring placing a synapse that points at
 * the crystal, the sanctum refusals, the budget held by a ring still closing, withering with the node, the inputs
 * returned on a break and the cut-off rule. The GameTest server has no Gardens level, so these run in the test level
 * past the Apotheos and dimension checks (refused there as {@code not_in_gardens}, which is asserted); the full feed in
 * the Gardens is covered by the heart-node review client. Sanctum records far outside the template carry no blocks.
 */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MycelialSynapseGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private MycelialSynapseGameTests() {
	}

	/** The plain 1x2 pattern matches with the crystal above, below or beside the iron; outside the Gardens it's refused. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void synapsePatternMatchesOnAllSixFaces(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BloodStructureRecipe recipe = recipe(helper);
		BlockPos iron = new BlockPos(8, 6, 8);
		for (Direction direction : Direction.values()) {
			clearAround(helper, iron);
			helper.setBlock(iron, BlockInit.hematic_iron_block.get());
			helper.setBlock(iron.relative(direction), BlockInit.sporite_crystal.get());
			var match = BloodStructureCraftingHelper.findStructurePatternAtHit(recipe, level, helper.absolutePos(iron));
			helper.assertTrue(match != null, "The synapse pattern must match with the crystal " + direction);
			helper.assertTrue(PatternOrigins.contains(match, recipe.getPattern().getBlockPattern(),
					helper.absolutePos(iron.relative(direction))), "The match must hold the crystal " + direction);
		}
		ServerPlayer player = apotheos(level, "synapse-overworld");
		try {
			var check = MycelialSynapseBlock.findFormation(level, player, helper.absolutePos(iron),
					helper.absolutePos(iron.relative(Direction.UP)));
			helper.assertTrue(check.formation() == null && check.refusal() != null
					&& check.refusal().getContents() instanceof TranslatableContents contents
					&& contents.getKey().equals("block.hemomancy.mycelial_synapse.not_in_gardens"),
					"Outside the Gardens a synapse is refused as such");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	/**
	 * A synapse on every face, each placed by the craft ring and pointing where its crystal stood; a seventh is over
	 * budget; breaking the node withers them all; a broken synapse returns its iron and crystal.
	 */
	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void synapsesRaiseOnEveryFaceThenWitherWithTheirNode(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = apotheos(level, "synapse-faces");
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		BlockPos heart = helper.absolutePos(new BlockPos(8, 2, 8));
		UUID owner = player.getUUID();
		BloodStructureRecipe recipe = recipe(helper);
		try {
			helper.setBlock(new BlockPos(8, 2, 8), BlockInit.heart_node.get());
			((HeartNodeBlockEntity) level.getBlockEntity(heart)).plant(owner, level.getGameTime());
			data.plant(owner, heart, level.getGameTime());
			BlockPos[] irons = { new BlockPos(3, 4, 3), new BlockPos(13, 4, 3), new BlockPos(3, 4, 13),
					new BlockPos(13, 4, 13), new BlockPos(8, 9, 3), new BlockPos(8, 9, 13) };
			Direction[] faces = Direction.values();
			for (int i = 0; i < faces.length; i++) {
				BlockPos iron = helper.absolutePos(irons[i]);
				BlockPos crystal = iron.relative(faces[i]);
				helper.setBlock(irons[i], BlockInit.hematic_iron_block.get());
				helper.setBlock(irons[i].relative(faces[i]), BlockInit.sporite_crystal.get());
				helper.assertTrue(MycelialSynapseBlock.sanctumRefusal(data, owner, iron) == null,
						"The envelope takes a synapse " + faces[i]);
				closeRing(level, recipe, player, iron, new MycelialSynapseBlock.Formation(owner, iron, crystal, faces[i]));
				helper.assertTrue(level.getBlockState(iron).is(BlockInit.mycelial_synapse.get()),
						"The iron becomes a synapse " + faces[i]);
				helper.assertTrue(level.getBlockState(iron).getValue(MycelialSynapseBlock.FACING) == faces[i],
						"The synapse points where the crystal stood " + faces[i]);
				helper.assertTrue(level.getBlockState(crystal).isAir(), "The crystal is consumed " + faces[i]);
				helper.assertTrue(data.entry(owner).synapses().contains(iron), "The synapse is recorded");
				var synapse = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.MycelialSynapseBlockEntity)
						level.getBlockEntity(iron);
				helper.assertTrue(owner.equals(synapse.owner()) && heart.equals(synapse.linkedHeart())
						&& synapse.active(level), "The synapse knows its owner and heart");
			}
			helper.assertTrue(data.entry(owner).synapses().size() == HeartNodeRules.SYNAPSE_BUDGET,
					"The six faces fill the budget");
			helper.assertTrue("budget".equals(MycelialSynapseBlock.sanctumRefusal(data, owner,
					helper.absolutePos(new BlockPos(8, 4, 4)))), "A seventh synapse is over budget");

			helper.setBlock(new BlockPos(8, 2, 8), Blocks.AIR);
			helper.assertTrue(data.entry(owner) == null, "The node's entry goes with it");
			for (BlockPos iron : irons) {
				helper.assertTrue(helper.getBlockState(iron).getValue(MycelialSynapseBlock.WITHERED),
						"Breaking the node withers every synapse it held");
			}
			BlockPos broken = helper.absolutePos(irons[0]);
			level.destroyBlock(broken, true);
			AABB around = new AABB(broken).inflate(2.0D);
			helper.assertTrue(!level.getEntitiesOfClass(ItemEntity.class, around,
					item -> item.getItem().is(BlockInit.hematic_iron_block.get().asItem())).isEmpty()
					&& !level.getEntitiesOfClass(ItemEntity.class, around,
					item -> item.getItem().is(BlockInit.sporite_crystal.get().asItem())).isEmpty(),
					"A broken synapse returns its iron and crystal");
			level.getEntitiesOfClass(ItemEntity.class, around).forEach(ItemEntity::discard);
			helper.succeed();
		} finally {
			data.remove(owner, heart);
			PendingBloodCraftManager.clear();
			player.discard();
		}
	}

	/**
	 * A lobe that would touch another owner's envelope is refused as foreign, one that reaches nothing as unconnected,
	 * and no heart at all as no_heart; a ring still closing holds its place in the budget.
	 */
	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void sanctumRefusalsAndAClosingRingHoldingTheBudget(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer alice = apotheos(level, "synapse-alice");
		ServerPlayer bob = apotheos(level, "synapse-bob");
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		BlockPos iron = helper.absolutePos(new BlockPos(8, 4, 8));
		BlockPos aliceHeart = iron.offset(20, 0, 0);
		BlockPos bobHeart = iron.offset(-20, 0, 0);
		try {
			helper.assertTrue("no_heart".equals(MycelialSynapseBlock.sanctumRefusal(data, bob.getUUID(), iron)),
					"No node, no synapse");
			data.plant(alice.getUUID(), aliceHeart, level.getGameTime());
			data.plant(bob.getUUID(), bobHeart, level.getGameTime());
			helper.assertTrue("foreign".equals(MycelialSynapseBlock.sanctumRefusal(data, bob.getUUID(), iron)),
					"Bob's lobe here would touch Alice's sanctum");
			helper.assertTrue("unconnected".equals(MycelialSynapseBlock.sanctumRefusal(data, bob.getUUID(),
					bobHeart.offset(-21, 0, 0))), "Too far from Bob's heart to chain");

			for (int i = 0; i < HeartNodeRules.SYNAPSE_BUDGET - 1; i++) {
				helper.assertTrue(data.addSynapse(bob.getUUID(), bobHeart.offset(-4, 0, i - 2)),
						"The test fills Bob's budget to one short");
			}
			BlockPos last = bobHeart.offset(-6, 0, 0);
			helper.assertTrue(MycelialSynapseBlock.sanctumRefusal(data, bob.getUUID(), last) == null,
					"One synapse is left in the budget");
			helper.setBlock(new BlockPos(8, 4, 8), BlockInit.hematic_iron_block.get());
			helper.setBlock(new BlockPos(8, 5, 8), BlockInit.sporite_crystal.get());
			var match = BloodStructureCraftingHelper.findStructurePatternAtHit(recipe(helper), level, iron);
			BlockPattern pattern = recipe(helper).getPattern().getBlockPattern();
			PendingBloodCraftManager.schedule(new PendingBloodCraftManager.PendingCraft(level, match,
					pattern.getWidth(), pattern.getHeight(), pattern.getDepth(), iron,
					new net.minecraft.world.item.ItemStack(BlockInit.mycelial_synapse.get()), 30, bob,
					new MycelialSynapseBlock.Formation(bob.getUUID(), last, iron.above(), Direction.UP)));
			helper.assertTrue("budget".equals(MycelialSynapseBlock.sanctumRefusal(data, bob.getUUID(), last.offset(0, 0, 3))),
					"A ring still closing holds the last place in the budget");
			helper.succeed();
		} finally {
			data.remove(alice.getUUID(), aliceHeart);
			data.remove(bob.getUUID(), bobHeart);
			PendingBloodCraftManager.clear();
			BloodStructureFeedManager.clear();
			alice.discard();
			bob.discard();
		}
	}

	/** Taking a middle synapse out forgets only that one: the one beyond keeps working, as a stake chain does. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void aCutOffSynapseKeepsWorking(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = apotheos(level, "synapse-chain");
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		UUID owner = player.getUUID();
		BlockPos middle = helper.absolutePos(new BlockPos(8, 4, 8));
		BlockPos heart = middle.offset(-32, 0, 0);
		BlockPos near = middle.offset(-14, 0, 0);
		BlockPos outer = middle.offset(14, 0, 0);
		try {
			data.plant(owner, heart, level.getGameTime());
			helper.assertTrue(data.addSynapse(owner, near), "The near synapse chains to the heart");
			helper.setBlock(new BlockPos(8, 4, 8), BlockInit.hematic_iron_block.get());
			helper.setBlock(new BlockPos(8, 5, 8), BlockInit.sporite_crystal.get());
			helper.assertTrue(new MycelialSynapseBlock.Formation(owner, middle, middle.above(), Direction.UP).place(level),
					"The middle synapse is raised");
			helper.assertTrue(data.addSynapse(owner, outer), "The outer synapse chains to the middle");

			helper.setBlock(new BlockPos(8, 4, 8), Blocks.AIR);
			helper.assertTrue(!data.entry(owner).synapses().contains(middle), "The middle record goes");
			helper.assertTrue(data.entry(owner).synapses().contains(outer), "The outer synapse keeps working");
			helper.assertTrue(data.entry(owner).footprint().connectedSynapses().equals(java.util.List.of(near)),
					"Only the near synapse still chains back to the heart");
			helper.succeed();
		} finally {
			data.remove(owner, heart);
			player.discard();
		}
	}

	/** Runs the formation through the craft ring, as a completed feed does. */
	private static void closeRing(ServerLevel level, BloodStructureRecipe recipe, ServerPlayer player, BlockPos iron,
			MycelialSynapseBlock.Formation formation) {
		BlockPattern pattern = recipe.getPattern().getBlockPattern();
		var match = BloodStructureCraftingHelper.findStructurePatternAtHit(recipe, level, iron);
		PendingBloodCraftManager.schedule(new PendingBloodCraftManager.PendingCraft(level, match,
				pattern.getWidth(), pattern.getHeight(), pattern.getDepth(), iron,
				new net.minecraft.world.item.ItemStack(BlockInit.mycelial_synapse.get()), 30, player, formation));
		for (int tick = 0; tick < 30; tick++) PendingBloodCraftManager.tick();
	}

	private static BloodStructureRecipe recipe(GameTestHelper helper) {
		BloodStructureRecipe recipe = BloodStructureRecipe.getStructureByLocation(helper.getLevel(),
				Hemomancy.rloc("blood_structure/mycelial_synapse"));
		if (recipe == null) throw new IllegalStateException("The synapse recipe must load");
		return recipe;
	}

	private static void clearAround(GameTestHelper helper, BlockPos pos) {
		helper.setBlock(pos, Blocks.AIR);
		for (Direction direction : Direction.values()) helper.setBlock(pos.relative(direction), Blocks.AIR);
	}

	private static ServerPlayer apotheos(ServerLevel level, String name) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), name), false);
		ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(),
				ClientInformation.createDefault());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(level.getServer(), connection, player, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
			}
		};
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		degree.setDegreeNumber(8);
		degree.setArchonPath(EnumArchonPath.APOTHEOS);
		HemoCapabilityAccess.getBloodVolume(player).setBloodVolume(2_000.0D);
		return player;
	}
}
