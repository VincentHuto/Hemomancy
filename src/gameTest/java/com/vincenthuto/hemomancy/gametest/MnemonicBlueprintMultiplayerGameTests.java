package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.itemhandler.MnemonicFolioItemHandler;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintItem;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ImprintMnemonicBlueprintPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MnemonicBlueprintMultiplayerGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";
	private static final MnemonicBlueprintTarget DRIED_GOURD = new MnemonicBlueprintTarget(
			MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE, Hemomancy.rloc("blood_structure/dried_gourd"));
	private static final MnemonicBlueprintTarget COVENANT_THRONE = new MnemonicBlueprintTarget(
			MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE, Hemomancy.rloc("blood_structure/covenant_throne"));
	private static final MnemonicBlueprintTarget THRESHOLD_MINOR_FLOOR = new MnemonicBlueprintTarget(
			MnemonicBlueprintTarget.Type.CARDINAL_RITE, Hemomancy.rloc("threshold_minor"));

	private MnemonicBlueprintMultiplayerGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void folioContentsSynchronizeThroughTheItemStackNetworkCodec(GameTestHelper helper) {
		ItemStack serverFolio = new ItemStack(ItemInit.mnemonic_folio.get());
		MnemonicFolioItemHandler serverHandler = handler(serverFolio);
		serverHandler.insertItem(0, MnemonicBlueprintItem.create(ItemInit.mnemonic_blueprint.get(), DRIED_GOURD), false);
		serverHandler.insertItem(29, new ItemStack(ItemInit.mnemonic_blueprint.get(), 4), false);
		serverHandler.save();

		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		ItemStack.STREAM_CODEC.encode(buffer, serverFolio);
		ItemStack clientFolio = ItemStack.STREAM_CODEC.decode(buffer);
		MnemonicFolioItemHandler clientHandler = handler(clientFolio);

		helper.assertTrue(DRIED_GOURD.equals(MnemonicBlueprintItem.getTarget(clientHandler.getStackInSlot(0))),
				"The synchronized folio must preserve a filled blueprint target in its exact slot");
		helper.assertTrue(clientHandler.getStackInSlot(29).getCount() == 4
					&& MnemonicBlueprintItem.isBlank(clientHandler.getStackInSlot(29)),
				"The synchronized folio must preserve blank blueprint counts in its exact slot");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void imprintValidationRejectsSpoofedAndLockedTargetsWithoutCrossPlayerMutation(GameTestHelper helper) {
		ServerPlayer sender = testPlayer(helper, "blueprint-sender");
		ServerPlayer observer = testPlayer(helper, "blueprint-observer");
		try {
			sender.getInventory().add(new ItemStack(ItemInit.mnemonic_blueprint.get(), 2));
			observer.getInventory().add(new ItemStack(ItemInit.mnemonic_blueprint.get(), 2));
			MnemonicBlueprintTarget spoofed = new MnemonicBlueprintTarget(
					MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE, Hemomancy.rloc("blood_structure/not_a_recipe"));

			helper.assertTrue(ImprintMnemonicBlueprintPacket.process(sender, spoofed)
						== ImprintMnemonicBlueprintPacket.Result.INVALID_TARGET,
					"A client-supplied nonexistent recipe id must be rejected by the server");
			helper.assertTrue(ImprintMnemonicBlueprintPacket.process(sender, COVENANT_THRONE)
						== ImprintMnemonicBlueprintPacket.Result.INVALID_TARGET,
					"A valid recipe above the sender's degree must be rejected by the server");
			helper.assertTrue(blankCount(sender) == 2 && filledCount(sender) == 0,
					"Rejected requests must not consume or create anything for the sender");
			helper.assertTrue(blankCount(observer) == 2 && filledCount(observer) == 0,
					"One player's rejected request must not mutate another player's inventory");
			helper.succeed();
		} finally {
			sender.discard();
			observer.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void validImprintConsumesOnlyTheSendingPlayersBlank(GameTestHelper helper) {
		ServerPlayer sender = testPlayer(helper, "valid-blueprint-sender");
		ServerPlayer observer = testPlayer(helper, "valid-blueprint-observer");
		try {
			sender.getInventory().add(new ItemStack(ItemInit.mnemonic_blueprint.get(), 2));
			observer.getInventory().add(new ItemStack(ItemInit.mnemonic_blueprint.get(), 2));

			helper.assertTrue(ImprintMnemonicBlueprintPacket.process(sender, DRIED_GOURD)
						== ImprintMnemonicBlueprintPacket.Result.IMPRINTED,
					"An unlocked loaded structure must imprint successfully");
			helper.assertTrue(blankCount(sender) == 1 && filledCount(sender) == 1,
					"A successful request must exchange exactly one sender blank for one filled blueprint");
			helper.assertTrue(blankCount(observer) == 2 && filledCount(observer) == 0,
					"A successful request must not consume another player's blank blueprint");
			helper.succeed();
		} finally {
			sender.discard();
			observer.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void registeredRitualFloorCanBeImprintedAsARiteBlueprint(GameTestHelper helper) {
		ServerPlayer sender = testPlayer(helper, "floor-blueprint-sender");
		try {
			sender.getInventory().add(new ItemStack(ItemInit.mnemonic_blueprint.get()));

			helper.assertTrue(ImprintMnemonicBlueprintPacket.process(sender, THRESHOLD_MINOR_FLOOR)
						== ImprintMnemonicBlueprintPacket.Result.IMPRINTED,
					"A registered ritual floor must be accepted as a rite blueprint target");
			helper.assertTrue(blankCount(sender) == 0 && filledCount(sender) == 1,
					"Imprinting a ritual floor must exchange exactly one blank blueprint");
			ItemStack filled = sender.getInventory().items.stream()
					.filter(stack -> stack.is(ItemInit.mnemonic_blueprint.get()) && !MnemonicBlueprintItem.isBlank(stack))
					.findFirst().orElse(ItemStack.EMPTY);
			helper.assertTrue(THRESHOLD_MINOR_FLOOR.equals(MnemonicBlueprintItem.getTarget(filled)),
					"The filled blueprint must preserve the selected ritual floor id");
			helper.succeed();
		} finally {
			sender.discard();
		}
	}

	private static MnemonicFolioItemHandler handler(ItemStack folio) {
		Object capability = folio.getCapability(Capabilities.ItemHandler.ITEM);
		if (capability instanceof MnemonicFolioItemHandler handler) {
			handler.loadIfNotLoaded();
			return handler;
		}
		throw new IllegalStateException("Mnemonic Folio item handler capability is unavailable");
	}

	private static int blankCount(ServerPlayer player) {
		return player.getInventory().items.stream()
				.filter(stack -> stack.is(ItemInit.mnemonic_blueprint.get()) && MnemonicBlueprintItem.isBlank(stack))
				.mapToInt(ItemStack::getCount).sum();
	}

	private static int filledCount(ServerPlayer player) {
		return (int) player.getInventory().items.stream()
				.filter(stack -> stack.is(ItemInit.mnemonic_blueprint.get()) && !MnemonicBlueprintItem.isBlank(stack)).count();
	}

	private static ServerPlayer testPlayer(GameTestHelper helper, String name) {
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				new GameProfile(UUID.randomUUID(), name), ClientInformation.createDefault()) {
			@Override protected ItemCooldowns createItemCooldowns() { return new ItemCooldowns(); }
			@Override public void displayClientMessage(Component message, boolean overlay) { }
		};
		BlockPos position = helper.absolutePos(new BlockPos(1, 2, 1));
		player.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
		return player;
	}
}
