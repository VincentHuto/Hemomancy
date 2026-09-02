package com.vincenthuto.hemomancy.gametest;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.serializer.CardinalRiteRecipeSerializer;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HarbingerPilotGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private HarbingerPilotGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodStructureLocked(GameTestHelper helper) {
		runScenario(helper, "blood_structure_locked");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodStructureUnlocked(GameTestHelper helper) {
		runScenario(helper, "blood_structure_unlocked");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void artificerAssignmentReady(GameTestHelper helper) {
		runScenario(helper, "artificer_assignment_ready");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void artificerRewardClaimed(GameTestHelper helper) {
		runScenario(helper, "artificer_reward_claimed");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void uninitiatedCannotPassBloodcraftDegreeGate(GameTestHelper helper) {
		runScenario(helper, "uninitiated_cannot_pass_bloodcraft_degree_gate");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void sanguineInitiationRecipeLoaded(GameTestHelper helper) {
		runScenario(helper, "sanguine_initiation_recipe_loaded");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void sanguineInitiationDegreeMapping(GameTestHelper helper) {
		runScenario(helper, "sanguine_initiation_degree_mapping");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void cardinalRiteMediaLoaded(GameTestHelper helper) {
		runScenario(helper, "cardinal_rite_media_loaded");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void cardinalRiteMediumSerializerSupportsTagsAndNetwork(GameTestHelper helper) {
		CardinalRiteRecipeSerializer serializer = (CardinalRiteRecipeSerializer)
				RecipeInit.cardinal_rite_recipe_serializer.get();
		var ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
		String base = """
				{"id":"hemomancy:cardinal_rite/medium_codec_test","bloodCost":0,
				 "riteType":"minor","riteName":"Medium Test","riteDescription":"",
				 "required_degree":0,"unstained":true,"floor":"hemomancy:dominion_minor"%s}
				""";
		try {
			CardinalRiteRecipe tagged = serializer.codec().codec().parse(ops,
					JsonParser.parseString(base.formatted(",\"medium\":{\"tag\":\"minecraft:planks\"}")))
					.getOrThrow(message -> new IllegalStateException(message));
			helper.assertTrue(tagged.getMedium().test(new ItemStack(Items.OAK_PLANKS)),
					"A tag-authored medium must match an item in that tag");

			CardinalRiteRecipe absent = serializer.codec().codec().parse(ops,
					JsonParser.parseString(base.formatted("")))
					.getOrThrow(message -> new IllegalStateException(message));
			helper.assertTrue(!absent.hasMedium(), "The medium field must remain optional");

			var invalid = serializer.codec().codec().parse(ops,
					JsonParser.parseString(base.formatted(",\"medium\":{}")));
			helper.assertTrue(invalid.error().isPresent(), "An empty medium definition must be rejected");

			RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(
					Unpooled.buffer(), helper.getLevel().registryAccess());
			serializer.streamCodec().encode(buffer, tagged);
			CardinalRiteRecipe decoded = serializer.streamCodec().decode(buffer);
			helper.assertTrue(decoded.getMedium().test(new ItemStack(Items.OAK_PLANKS)),
					"The recipe stream codec must preserve a tag-authored medium");
			helper.succeed();
		} catch (RuntimeException exception) {
			helper.fail("Cardinal Rite medium serializer contract failed: " + exception);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void cardinalFocusStoresOneMediumItem(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		helper.getLevel().setBlock(pos, BlockInit.cardinal_focus.get().defaultBlockState(), 3);
		Object focus = helper.getLevel().getBlockEntity(pos);
		ServerPlayer player = detachedTestPlayer(helper);
		try {
			var insert = focus.getClass().getMethod("insertMedium", net.minecraft.world.entity.player.Player.class,
					ItemStack.class);
			var display = focus.getClass().getMethod("getMediumDisplayStack");
			var extract = focus.getClass().getMethod("extractMedium");

			ItemStack iron = new ItemStack(Items.IRON_NUGGET, 3);
			helper.assertTrue((boolean) insert.invoke(focus, player, iron), "The empty Focus must accept a medium");
			helper.assertTrue(iron.getCount() == 2, "Survival insertion must move exactly one held item");
			ItemStack seated = (ItemStack) display.invoke(focus);
			helper.assertTrue(seated.is(Items.IRON_NUGGET) && seated.getCount() == 1,
					"The Focus must store exactly one medium item");

			ItemStack gold = new ItemStack(Items.GOLD_NUGGET, 2);
			helper.assertTrue(!(boolean) insert.invoke(focus, player, gold),
					"An occupied Focus must reject another medium");
			helper.assertTrue(gold.getCount() == 2, "Rejected insertion must not consume an item");
			helper.assertTrue(((ItemStack) extract.invoke(focus)).is(Items.IRON_NUGGET),
					"Extraction must return the seated medium");

			player.getAbilities().instabuild = true;
			helper.assertTrue((boolean) insert.invoke(focus, player, gold),
					"The emptied Focus must accept a creative player's medium");
			helper.assertTrue(gold.getCount() == 2,
					"Creative insertion must not shrink the held stack");
			var focusEntity = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity) focus;
			var restored = new com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity(
					pos, helper.getLevel().getBlockState(pos));
			restored.handleUpdateTag(focusEntity.getUpdateTag(helper.getLevel().registryAccess()),
					helper.getLevel().registryAccess());
			helper.assertTrue(restored.getMediumForMatching().is(Items.GOLD_NUGGET),
					"The synchronized Focus update tag must preserve its one-item medium");
			helper.succeed();
		} catch (ReflectiveOperationException exception) {
			helper.fail("Cardinal Focus medium storage API is missing: " + exception);
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void sanguineInitiationReplacesLinkedMortalDisplay(GameTestHelper helper) {
		BlockPos focusPos = helper.absolutePos(new BlockPos(1, 1, 1));
		BlockPos displayPos = focusPos.offset(2, 0, 0);
		helper.getLevel().setBlock(focusPos, BlockInit.cardinal_focus.get().defaultBlockState(), 3);
		helper.getLevel().setBlock(displayPos, BlockInit.mortal_display.get().defaultBlockState(), 3);
		var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity)
				helper.getLevel().getBlockEntity(focusPos);
		focus.linkTempleDisplay(displayPos);

		try {
			var replace = Class.forName(
					"com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents")
					.getDeclaredMethod("replaceLinkedTempleDisplay", net.minecraft.server.level.ServerLevel.class,
							BlockPos.class);
			replace.setAccessible(true);
			replace.invoke(null, helper.getLevel(), focusPos);
			helper.assertTrue(helper.getLevel().getBlockState(focusPos).is(BlockInit.cardinal_focus.get()),
					"Sanguine Initiation must leave the Cardinal Focus in place");
			helper.assertTrue(helper.getLevel().getBlockState(displayPos)
					.is(BlockInit.placed_blood_stained_stone.get()),
					"Sanguine Initiation must replace the linked Mortal Display");
			helper.succeed();
		} catch (ReflectiveOperationException exception) {
			helper.fail("Sanguine Initiation temple-display replacement failed: " + exception);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void rootedVeinRiteReplacesFocusAndGrowsVeinAbove(GameTestHelper helper) {
		BlockPos focusPos = helper.absolutePos(new BlockPos(1, 1, 1));
		helper.getLevel().setBlock(focusPos, BlockInit.cardinal_focus.get().defaultBlockState(), 3);

		try {
			var complete = Class.forName(
					"com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents")
					.getDeclaredMethod("completeRootedVein", net.minecraft.server.level.ServerLevel.class,
							BlockPos.class);
			complete.setAccessible(true);
			complete.invoke(null, helper.getLevel(), focusPos);
			helper.assertTrue(helper.getLevel().getBlockState(focusPos).is(BlockInit.venous_stone.get()),
					"The rite must replace its Cardinal Focus with Venous Stone");
			helper.assertTrue(helper.getLevel().getBlockState(focusPos.above()).is(BlockInit.earthen_vein.get()),
					"The rite must grow an Earthen Vein one block above its Cardinal Focus");
			helper.succeed();
		} catch (ReflectiveOperationException exception) {
			helper.fail("Rooted Vein completion effect is missing: " + exception);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void cardinalRiteMediumMatchingIsExact(GameTestHelper helper) {
		try {
			Class<?> rules = Class.forName("com.vincenthuto.hemomancy.common.rite.CardinalRiteMediumRules");
			var matches = rules.getMethod("matches", Ingredient.class, ItemStack.class);
			var votary = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
					helper.getLevel(), Hemomancy.rloc("cardinal_rite/votary_rite"));
			var initiate = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
					helper.getLevel(), Hemomancy.rloc("cardinal_rite/initiate_rite"));
			helper.assertTrue(votary != null && initiate != null, "Medium-matching fixture recipes must load");
			helper.assertTrue((boolean) matches.invoke(null, votary.getMedium(), new ItemStack(Items.IRON_NUGGET)),
					"The authored iron medium must match");
			helper.assertTrue(!(boolean) matches.invoke(null, votary.getMedium(), new ItemStack(Items.GOLD_NUGGET)),
					"A different seated item must not match");
			helper.assertTrue((boolean) matches.invoke(null, initiate.getMedium(), ItemStack.EMPTY),
					"A recipe without a medium must match an empty Focus");
			helper.assertTrue(!(boolean) matches.invoke(null, initiate.getMedium(), new ItemStack(Items.IRON_NUGGET)),
					"A recipe without a medium must reject an occupied Focus");
			helper.succeed();
		} catch (ReflectiveOperationException exception) {
			helper.fail("Cardinal Rite medium matching rules are missing: " + exception);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void cardinalRiteMediumConsumptionIsAtomic(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		helper.getLevel().setBlock(pos, BlockInit.cardinal_focus.get().defaultBlockState(), 3);
		var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity)
				helper.getLevel().getBlockEntity(pos);
		ServerPlayer player = detachedTestPlayer(helper);
		try {
			var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
					helper.getLevel(), Hemomancy.rloc("cardinal_rite/votary_rite"));
			var consume = Class.forName("com.vincenthuto.hemomancy.common.rite.CardinalRiteMediumRules")
					.getMethod("consume",
							com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity.class,
							Ingredient.class);
			helper.assertTrue(recipe != null, "Votary medium fixture recipe must load");

			focus.insertMedium(player, new ItemStack(Items.GOLD_NUGGET));
			helper.assertTrue(!(boolean) consume.invoke(null, focus, recipe.getMedium()),
					"A mismatched medium must not be consumed");
			helper.assertTrue(focus.getMediumForMatching().is(Items.GOLD_NUGGET),
					"A failed consumption must leave the seated medium intact");

			focus.extractMedium();
			focus.insertMedium(player, new ItemStack(Items.IRON_NUGGET));
			helper.assertTrue((boolean) consume.invoke(null, focus, recipe.getMedium()),
					"The matching medium must be consumed");
			helper.assertTrue(!focus.hasMedium(), "Successful consumption must empty the Focus");

			focus.insertMedium(player, new ItemStack(Items.GOLD_NUGGET));
			helper.assertTrue(!(boolean) consume.invoke(null, focus, Ingredient.EMPTY),
					"A recipe without a medium must fail atomically when the Focus is occupied");
			helper.assertTrue(focus.getMediumForMatching().is(Items.GOLD_NUGGET),
					"Unexpected media must remain recoverable after failed completion validation");
			focus.extractMedium();
			helper.assertTrue((boolean) consume.invoke(null, focus, Ingredient.EMPTY),
					"A recipe without a medium must validate an empty Focus");
			helper.succeed();
		} catch (ReflectiveOperationException exception) {
			helper.fail("Atomic Cardinal Rite medium consumption is missing: " + exception);
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void cardinalFocusSelectsQliphothRootVisualsOnlyForSeedMedium(GameTestHelper helper) {
		try {
			Class<?> rules = Class.forName(
					"com.vincenthuto.hemomancy.common.rite.CardinalFocusMediumVisualRules");
			var emitsRoots = rules.getMethod("emitsQliphothRoots", ItemStack.class);
			helper.assertTrue((boolean) emitsRoots.invoke(null, new ItemStack(ItemInit.qliphoth_seed.get())),
					"A seated Qliphoth Seed must enable its root tendril visual");
			helper.assertTrue(!(boolean) emitsRoots.invoke(null, new ItemStack(Items.IRON_NUGGET)),
					"Other Cardinal Focus media must not emit Qliphoth roots");
			helper.succeed();
		} catch (ReflectiveOperationException exception) {
			helper.fail("Cardinal Focus Qliphoth medium visual selection is missing: " + exception);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void administrativeKillRemovesProtectedNpcs(GameTestHelper helper) {
		List<Mob> npcs = List.of(
				EntityInit.unstained_zealot.get().create(helper.getLevel()),
				EntityInit.unstained_guardian.get().create(helper.getLevel()),
				EntityInit.unstained_acolyte.get().create(helper.getLevel()),
				EntityInit.unstained_scout.get().create(helper.getLevel()),
				EntityInit.harbinger_hermit.get().create(helper.getLevel()),
				EntityInit.harbinger_alchemist.get().create(helper.getLevel()),
				EntityInit.harbinger_artificer.get().create(helper.getLevel()),
				EntityInit.harbinger_cicatrix_anchorite.get().create(helper.getLevel()),
				EntityInit.harbinger_mnemonist.get().create(helper.getLevel()),
				EntityInit.harbinger_vicar.get().create(helper.getLevel()),
				EntityInit.harbinger_voyager.get().create(helper.getLevel()),
				EntityInit.harbinger_votary_wayfarer.get().create(helper.getLevel()));
		try {
			for (Mob npc : npcs) {
				helper.getLevel().addFreshEntity(npc);
				npc.kill();
				helper.assertTrue(npc.isDeadOrDying() || npc.isRemoved(),
						npc.getType() + " must honor administrative kill commands");
			}
			helper.succeed();
		} finally {
			npcs.forEach(Mob::discard);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void creativeStaffEscrowRemovesAndSynchronizesHeldStaff(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		try {
			player.getAbilities().instabuild = true;
			player.getInventory().selected = 0;
			player.getInventory().setItem(0, new ItemStack(ItemInit.living_staff.get()));
			StaffRemovalListener listener = new StaffRemovalListener();
			player.inventoryMenu.addSlotListener(listener);
			listener.reset();

			ItemStack captured = CardinalRiteStaffEscrow.capture(player);

			helper.assertTrue(captured.is(ItemInit.living_staff.get()),
					"The exact Living Staff must enter rite escrow");
			helper.assertTrue(player.getInventory().getItem(0).isEmpty(),
					"Creative mode must not retain the planted Living Staff");
			helper.assertTrue(listener.sawEmptySlot,
					"Staff removal must be synchronized immediately to the creative client");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void creativeFungalSpineBypassesProgression(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		try {
			player.setGameMode(GameType.CREATIVE);
			player.getPersistentData().putBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING, true);
			var degree = HemoCapabilityAccess.requireInitiatoryDegree(player);
			degree.setDegreeNumber(0);
			degree.setFungalSpineGranted(false);
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(false);
			blood.setBloodVolume(0.0D);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.fungal_spine.get()));

			ItemInit.fungal_spine.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

			helper.assertTrue(player.getPersistentData().contains(FungalGardenTravelHelper.RETURN_X),
					"Creative use must pass the progression gates and retain a return point");
			helper.assertTrue(player.getPersistentData().getBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING)
					&& degree.getDegreeNumber() == 0 && !degree.hasFungalSpineGranted()
					&& !blood.isActive() && blood.getBloodVolume() == 0.0D,
					"Creative use must not mutate progression or blood state");
			helper.assertTrue(!FungalGardenTravelHelper.isProjectionActive(player),
					"Creative travel must not start the progression projection");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	private static void runScenario(GameTestHelper helper, String id) {
		HemoTestScenario scenario = HemoTestScenarioCatalog.find(id)
				.orElseThrow(() -> new IllegalArgumentException("Unknown scenario " + id));
		ServerPlayer player = detachedTestPlayer(helper);
		try {
			scenario.setup().apply(player);
			HemoTestResult result = scenario.verify().check(player);
			helper.assertTrue(result.passed(), result.message());
			helper.succeed();
		} finally {
			scenario.clear().apply(player);
			player.discard();
		}
	}

	private static ServerPlayer detachedTestPlayer(GameTestHelper helper) {
		return new ServerPlayer(
				helper.getLevel().getServer(),
				helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "hemomancy-test-player"),
				ClientInformation.createDefault());
	}

	private static ServerPlayer connectedTestPlayer(GameTestHelper helper) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), "fungal-spine-test-player"), false);
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

	private static final class StaffRemovalListener implements ContainerListener {
		private boolean sawEmptySlot;

		@Override
		public void slotChanged(AbstractContainerMenu menu, int slot, ItemStack stack) {
			if (stack.isEmpty()) sawEmptySlot = true;
		}

		@Override
		public void dataChanged(AbstractContainerMenu menu, int slot, int value) {
		}

		private void reset() {
			sawEmptySlot = false;
		}
	}

}
