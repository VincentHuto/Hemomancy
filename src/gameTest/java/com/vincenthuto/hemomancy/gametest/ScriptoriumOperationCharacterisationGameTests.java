package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumProvenance;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.EnzymaticScriptoriumMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * Characterisation of every Enzymatic Scriptorium menu operation before enchanting and denaturing move from the menu
 * into the block entity (step 11, item 5): selecting enzymes, the three modes at each tier and degree, the three
 * inscriptions in each mode, denaturing and each refusal, a rite-locked station and a stranger's click.
 *
 * <p>Each scenario uses a fresh station and player with the enchantment seed and the curse roll fixed. After every
 * click the fingerprint records its result, every synced menu value, the item's enchantments and provenance, every
 * slot, the station's blood, the player's levels and recorded station practice, and the station's saved data.
 */
@GameTestHolder("scriptorium_operation_validation")
@PrefixGameTestTemplate(false)
public final class ScriptoriumOperationCharacterisationGameTests {
	private static final String GOLDEN = "/scriptorium/operation-fingerprints.txt";
	private static final int DUCTILIS = EnumBloodTendency.DUCTILIS.ordinal();

	private ScriptoriumOperationCharacterisationGameTests() {
	}

	private record Setup(int stage, int degree, boolean cursedItem, boolean shard, double blood) {
		static Setup basic() {
			return new Setup(0, 3, false, false, 1000.0D);
		}
	}

	@GameTest(template = "empty", timeoutTicks = 200, batch = "scriptorium_operations")
	public static void everyScriptoriumOperationKeepsItsOutcome(GameTestHelper helper) {
		List<String> lines = new ArrayList<>();
		scenario(helper, lines, "select cycles and caps at three", Setup.basic(), (s) -> {
			for (int i = 0; i < 5; i++) s.click(DUCTILIS);
			s.click(0);
			s.click(1);
		});
		for (int stage = 0; stage <= 2; stage++) {
			int tier = 3 + stage * 2;
			for (int degree : new int[] {3, 5, 7}) {
				scenario(helper, lines, "modes at tier " + tier + " degree " + degree, new Setup(stage, degree, false, false, 1000.0D),
						(s) -> {
							s.click(21);
							s.click(22);
							s.click(20);
						});
			}
		}
		for (int stage = 0; stage <= 2; stage++) {
			int degree = 3 + stage * 2;
			for (int mode = 0; mode <= stage; mode++) {
				for (int row = 0; row < 3; row++) {
					int chosenMode = mode;
					int chosenRow = row;
					scenario(helper, lines, "inscription " + (row + 1) + " in mode " + mode + " at tier " + degree,
							new Setup(stage, degree, false, false, 1000.0D), (s) -> {
								for (int i = 0; i < 3; i++) s.click(DUCTILIS);
								if (chosenMode > 0) s.click(20 + chosenMode);
								s.click(8 + chosenRow);
							});
				}
			}
		}
		// A target needs an enchantment rolled at its maximum level, which depends on the loaded enzymes.
		for (int mode = 1; mode <= 2; mode++) {
			for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
				int chosenMode = mode;
				scenario(helper, lines, "targeted inscription 3 with " + tendency + " in mode " + mode,
						new Setup(mode == 1 ? 1 : 2, mode == 1 ? 5 : 7, false, false, 1000.0D), (s) -> {
							for (int i = 0; i < 3; i++) s.click(tendency.ordinal());
							s.click(20 + chosenMode);
							s.click(10);
						});
			}
		}
		scenario(helper, lines, "inscription without enough blood", new Setup(0, 3, false, false, 50.0D), (s) -> {
			for (int i = 0; i < 3; i++) s.click(DUCTILIS);
			s.click(8);
		});
		scenario(helper, lines, "denature a cursed item", new Setup(0, 3, true, true, 1000.0D), (s) -> s.click(11));
		scenario(helper, lines, "denature without a curse", new Setup(0, 3, false, true, 1000.0D), (s) -> s.click(11));
		scenario(helper, lines, "denature without a shard", new Setup(0, 3, true, false, 1000.0D), (s) -> s.click(11));
		scenario(helper, lines, "denature without blood", new Setup(0, 3, true, true, 200.0D), (s) -> s.click(11));
		scenario(helper, lines, "denature below degree three", new Setup(0, 2, true, true, 1000.0D), (s) -> s.click(11));
		scenario(helper, lines, "a rite-locked station refuses", Setup.basic(), (s) -> {
			s.station.setRiteLocked(true);
			s.click(DUCTILIS);
			s.click(8);
			s.click(11);
		});
		scenario(helper, lines, "a stranger's click is refused", Setup.basic(), (s) -> {
			try (CapturedPlayer stranger = CapturedPlayer.create(helper)) {
				stranger.player.setPos(s.player.position());
				s.lines.add("  stranger select -> " + s.menu.clickMenuButton(stranger.player, DUCTILIS));
				s.lines.add("  stranger inscribe -> " + s.menu.clickMenuButton(stranger.player, 8));
			}
			s.record("after stranger");
		});
		PlayerFingerprints.assertGolden(helper, GOLDEN, "scriptorium-operation-fingerprints.actual.txt",
				String.join("\n", lines) + "\n");
		helper.succeed();
	}

	private static final class Scene {
		final GameTestHelper helper;
		final ServerPlayer player;
		final EnzymaticScriptoriumBlockEntity station;
		final EnzymaticScriptoriumMenu menu;
		final List<String> lines;
		private int clicks;

		Scene(GameTestHelper helper, ServerPlayer player, EnzymaticScriptoriumBlockEntity station, List<String> lines) {
			this.helper = helper;
			this.player = player;
			this.station = station;
			this.menu = new EnzymaticScriptoriumMenu(1, player.getInventory(), station);
			this.lines = lines;
		}

		void click(int button) {
			// The curse roll uses the actor's random: fix it before every click.
			player.getRandom().setSeed(9001L + clicks++);
			boolean result = menu.clickMenuButton(player, button);
			menu.broadcastChanges();
			lines.add("  click " + button + " -> " + result);
			record("after " + button);
		}

		void record(String label) {
			StringBuilder data = new StringBuilder();
			for (int offer = 0; offer < 3; offer++) {
				data.append(" offer").append(offer).append("=").append(menu.offerCost(offer))
						.append("/t").append(menu.targetId(offer)).append("/c").append(menu.clueId(offer))
						.append("@").append(menu.clueLevel(offer)).append("/e[");
				for (int tube = 0; tube < 8; tube++) data.append(tube == 0 ? "" : ",").append(menu.enzymeCost(offer, tube));
				data.append("]");
			}
			StringBuilder selected = new StringBuilder();
			for (int tube = 0; tube < 8; tube++) selected.append(tube == 0 ? "" : ",").append(menu.selected(tube));
			ItemStack item = station.getItem(EnzymaticScriptoriumBlockEntity.ITEM);
			TreeMap<String, Integer> enchantments = new TreeMap<>();
			item.getEnchantments().entrySet().forEach(entry -> enchantments.put(
					entry.getKey().unwrapKey().map(key -> key.location().getPath()).orElse("?"), entry.getIntValue()));
			StringBuilder slots = new StringBuilder();
			for (int slot = 0; slot < 11; slot++) slots.append(slot == 0 ? "" : ",").append(station.getItem(slot).getCount());
			var progress = HemoCapabilityAccess.stationUpgrades(player);
			CompoundTag saved = station.saveWithoutMetadata(helper.getLevel().registryAccess());
			saved.remove("MachineIdentity");
			lines.add("    " + label + ": mode=" + menu.mode() + " tier=" + menu.tier() + " degree=" + menu.playerDegree()
					+ " selected=[" + selected + "]" + data
					+ " item=" + enchantments + " provenance=" + item.get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get())
					+ " slots=[" + slots + "] blood=" + station.getBloodVolume() + " levels=" + player.experienceLevel
					+ " practice=" + progress.hasUsed(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT)
					+ "/" + progress.hasUsed(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT_TARGETED)
					+ " saved=" + saved);
		}
	}

	private static void scenario(GameTestHelper helper, List<String> lines, String name, Setup setup, Consumer<Scene> steps) {
		ServerLevel level = helper.getLevel();
		BlockPos position = helper.absolutePos(new BlockPos(8, 3, 8));
		for (BlockPos pos : BlockPos.betweenClosed(position.offset(-3, 0, -3), position.offset(3, 2, 3))) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
		}
		level.setBlockAndUpdate(position, BlockInit.enzymatic_scriptorium.get().defaultBlockState()
				.setValue(EnzymaticScriptoriumBlock.STAGE, setup.stage()));
		for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
			level.setBlockAndUpdate(position.offset(offset), Blocks.BOOKSHELF.defaultBlockState());
		}
		var station = (EnzymaticScriptoriumBlockEntity) level.getBlockEntity(position);
		station.receiveBlood(setup.blood());
		for (int i = 0; i < 8; i++) {
			station.setItem(i, new ItemStack(EnumBloodTendency.getRepEnzyme(EnumBloodTendency.values()[i]), 3));
		}
		ItemStack item = new ItemStack(Items.DIAMOND_PICKAXE);
		if (setup.cursedItem()) {
			item.set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(), new ScriptoriumProvenance("sanguine_appetite", 1, 0));
			item.enchant(level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
					.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY), 2);
		}
		station.setItem(EnzymaticScriptoriumBlockEntity.ITEM, item);
		station.setItem(EnzymaticScriptoriumBlockEntity.LAPIS, new ItemStack(Items.LAPIS_LAZULI, 3));
		if (setup.shard()) station.setItem(EnzymaticScriptoriumBlockEntity.SHARD, new ItemStack(ItemInit.blood_crystal_shard.get()));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 2.5D);
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(setup.degree());
			player.giveExperienceLevels(40);
			// The offers come from the player's enchantment seed: fix it.
			player.getRandom().setSeed(1234L);
			player.onEnchantmentPerformed(ItemStack.EMPTY, 0);
			lines.add(name);
			Scene scene = new Scene(helper, player, station, lines);
			scene.menu.broadcastChanges();
			scene.record("opened");
			steps.accept(scene);
		}
	}
}
