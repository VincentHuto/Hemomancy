package com.vincenthuto.hemomancy.common.item.shared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creative-mode tool for capturing multiblock structures and exporting them as
 * blood_structure_recipe or cardinal_rite_recipe JSON files. Right-click two
 * corners of a built structure in the world and the item scans the region,
 * assigns key characters to each unique block type, and writes ready-to-use
 * recipe JSONs to {@code hemomancy_scans/} in the game directory.
 * <p>
 * Usage:
 * <ol>
 *   <li>Right-click a block to set corner 1 (indicated by enchant glint).</li>
 *   <li>Right-click a second block to scan the region between the two corners.</li>
 *   <li>Sneak+right-click at any time to clear a stored corner.</li>
 * </ol>
 */
public class StructureScannerItem extends Item {

	private static final String TAG_POS1_X = "ScanPos1X";
	private static final String TAG_POS1_Y = "ScanPos1Y";
	private static final String TAG_POS1_Z = "ScanPos1Z";
	private static final String TAG_HAS_POS1 = "ScanHasPos1";

	private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final int MAX_VOLUME = 10000;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public StructureScannerItem(Properties properties) {
		super(properties);
	}

	// ========================= Core Logic =========================

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		Player player = ctx.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}
		if (!player.isCreative()) {
			player.displayClientMessage(
					Component.literal("Structure Scanner requires creative mode!")
							.withStyle(ChatFormatting.RED),
					true);
			return InteractionResult.FAIL;
		}

		ItemStack stack = ctx.getItemInHand();
		BlockPos clickedPos = ctx.getClickedPos();
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

		// Sneak + click: clear stored corner
		if (player.isShiftKeyDown()) {
			if (tag.getBoolean(TAG_HAS_POS1)) {
				tag.putBoolean(TAG_HAS_POS1, false);
				stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
				player.displayClientMessage(
						Component.literal("Scanner reset.").withStyle(ChatFormatting.YELLOW), true);
			}
			return InteractionResult.SUCCESS;
		}

		if (!tag.getBoolean(TAG_HAS_POS1)) {
			// First click – store corner 1
			tag.putInt(TAG_POS1_X, clickedPos.getX());
			tag.putInt(TAG_POS1_Y, clickedPos.getY());
			tag.putInt(TAG_POS1_Z, clickedPos.getZ());
			tag.putBoolean(TAG_HAS_POS1, true);
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

			player.displayClientMessage(
					Component.literal("Corner 1 set: " + clickedPos.toShortString())
							.withStyle(ChatFormatting.GREEN),
					true);
			return InteractionResult.SUCCESS;
		}

		// Second click – scan region
		BlockPos pos1 = new BlockPos(tag.getInt(TAG_POS1_X), tag.getInt(TAG_POS1_Y), tag.getInt(TAG_POS1_Z));
		tag.putBoolean(TAG_HAS_POS1, false);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

		return scanRegion(player, level, pos1, clickedPos);
	}

	/**
	 * Scans all blocks in the axis-aligned bounding box between {@code pos1} and
	 * {@code pos2}, builds the pattern, and writes recipe JSONs.
	 */
	private InteractionResult scanRegion(Player player, Level level, BlockPos pos1, BlockPos pos2) {
		int minX = Math.min(pos1.getX(), pos2.getX());
		int minY = Math.min(pos1.getY(), pos2.getY());
		int minZ = Math.min(pos1.getZ(), pos2.getZ());
		int maxX = Math.max(pos1.getX(), pos2.getX());
		int maxY = Math.max(pos1.getY(), pos2.getY());
		int maxZ = Math.max(pos1.getZ(), pos2.getZ());

		int sizeX = maxX - minX + 1;
		int sizeY = maxY - minY + 1;
		int sizeZ = maxZ - minZ + 1;

		if ((long) sizeX * sizeY * sizeZ > MAX_VOLUME) {
			player.displayClientMessage(
					Component.literal("Region too large! Max " + MAX_VOLUME + " blocks.")
							.withStyle(ChatFormatting.RED),
					true);
			return InteractionResult.FAIL;
		}

		// Collect unique blocks and assign key characters
		Map<Block, String> blockToChar = new LinkedHashMap<>();
		int charIdx = 0;

		for (int z = minZ; z <= maxZ; z++) {
			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x <= maxX; x++) {
					Block block = level.getBlockState(new BlockPos(x, y, z)).getBlock();
					if (block == Blocks.AIR) {
						continue;
					}
					if (!blockToChar.containsKey(block)) {
						if (charIdx >= CHAR_POOL.length()) {
							player.displayClientMessage(
									Component.literal(
											"Too many unique block types (>" + CHAR_POOL.length() + ")!")
											.withStyle(ChatFormatting.RED),
									true);
							return InteractionResult.FAIL;
						}
						blockToChar.put(block, String.valueOf(CHAR_POOL.charAt(charIdx++)));
					}
				}
			}
		}

		if (blockToChar.isEmpty()) {
			player.displayClientMessage(
					Component.literal("No non-air blocks in the selected region!")
							.withStyle(ChatFormatting.RED),
					true);
			return InteractionResult.FAIL;
		}

		// Build pattern: [aisle / Z] [row / Y] = string of chars along X
		// Row 0 must be the TOP (highest Y) to match BlockPatternBuilder.aisle() convention.
		// Iterate Y from maxY down to minY so the array is top-to-bottom.
		String[][] pattern = new String[sizeZ][sizeY];
		for (int z = minZ; z <= maxZ; z++) {
			for (int y = maxY; y >= minY; y--) {
				StringBuilder row = new StringBuilder();
				for (int x = minX; x <= maxX; x++) {
					Block block = level.getBlockState(new BlockPos(x, y, z)).getBlock();
					if (block == Blocks.AIR) {
						row.append(' ');
					} else {
						row.append(blockToChar.get(block));
					}
				}
				pattern[z - minZ][maxY - y] = row.toString();
			}
		}

		// Determine a sensible hitBlock (the block at the second corner click)
		Block hitBlock = level.getBlockState(pos2).getBlock();
		if (hitBlock == Blocks.AIR) {
			hitBlock = blockToChar.keySet().iterator().next();
		}

		// Generate both recipe flavours
		JsonObject bloodJson = buildBloodStructureJson(pattern, blockToChar, hitBlock);
		JsonObject riteJson = buildCardinalRiteJson(pattern, blockToChar);

		// Write files
		String stamp = String.valueOf(System.currentTimeMillis());
		MinecraftServer server = level.getServer();
		File baseDir = server != null ? server.getServerDirectory().toFile() : new File(".");
		File outDir = new File(baseDir, "hemomancy_scans");
		if (!outDir.exists() && !outDir.mkdirs()) {
			player.displayClientMessage(
					Component.literal("Could not create hemomancy_scans output directory.")
							.withStyle(ChatFormatting.RED),
					false);
			return InteractionResult.FAIL;
		}

		String bloodFile = "blood_structure_" + stamp + ".json";
		String riteFile = "cardinal_rite_" + stamp + ".json";

		try {
			writeJson(new File(outDir, bloodFile), bloodJson);
			writeJson(new File(outDir, riteFile), riteJson);
		} catch (IOException e) {
			player.displayClientMessage(
					Component.literal("Error writing files: " + e.getMessage()).withStyle(ChatFormatting.RED), false);
			return InteractionResult.FAIL;
		}

		// Feedback
		player.displayClientMessage(
				Component.literal("Structure scanned! " + sizeX + "x" + sizeY + "x" + sizeZ + " ("
						+ blockToChar.size() + " block types)").withStyle(ChatFormatting.GOLD),
				false);
		player.displayClientMessage(
				Component.literal("  Blood structure → hemomancy_scans/" + bloodFile)
						.withStyle(ChatFormatting.GREEN),
				false);
		player.displayClientMessage(
				Component.literal("  Cardinal rite   → hemomancy_scans/" + riteFile)
						.withStyle(ChatFormatting.GREEN),
				false);
		player.displayClientMessage(
				Component.literal("Block key:").withStyle(ChatFormatting.AQUA), false);
		for (Map.Entry<Block, String> entry : blockToChar.entrySet()) {
			ResourceLocation id = BuiltInRegistries.BLOCK.getKey(entry.getKey());
			player.displayClientMessage(
					Component.literal("  " + entry.getValue() + " = " + id).withStyle(ChatFormatting.GRAY), false);
		}

		return InteractionResult.SUCCESS;
	}

	// ========================= JSON Builders =========================

	private JsonObject buildBloodStructureJson(String[][] pattern, Map<Block, String> blockToChar, Block hitBlock) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "hemomancy:blood_structure_recipe");
		json.addProperty("bloodCost", 100);
		json.addProperty("heldItem", "hemomancy:sanguine_formation");

		ResourceLocation hitId = BuiltInRegistries.BLOCK.getKey(hitBlock);
		json.addProperty("hitBlock", hitId.toString());

		json.add("pattern", encodePattern(pattern));
		json.add("key", encodeKey(blockToChar));

		JsonObject result = new JsonObject();
		result.addProperty("item", "hemomancy:CHANGE_ME");
		json.add("result", result);
		return json;
	}

	private JsonObject buildCardinalRiteJson(String[][] pattern, Map<Block, String> blockToChar) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "hemomancy:cardinal_rite_recipe");
		json.addProperty("bloodCost", 500);
		json.addProperty("riteType", "lesser");
		json.addProperty("riteName", "CHANGE_ME");
		json.addProperty("riteDescription", "CHANGE_ME");

		json.add("pattern", encodePattern(pattern));
		json.add("key", encodeKey(blockToChar));

		JsonObject result = new JsonObject();
		result.addProperty("item", "hemomancy:CHANGE_ME");
		json.add("result", result);
		return json;
	}

	private static JsonArray encodePattern(String[][] pattern) {
		JsonArray outer = new JsonArray();
		for (String[] aisle : pattern) {
			JsonArray aisleArr = new JsonArray();
			for (String row : aisle) {
				aisleArr.add(row);
			}
			outer.add(aisleArr);
		}
		return outer;
	}

	private static JsonObject encodeKey(Map<Block, String> blockToChar) {
		JsonObject key = new JsonObject();
		for (Map.Entry<Block, String> entry : blockToChar.entrySet()) {
			JsonObject blockObj = new JsonObject();
			ResourceLocation id = BuiltInRegistries.BLOCK.getKey(entry.getKey());
			blockObj.addProperty("block", id.toString());
			key.add(entry.getValue(), blockObj);
		}
		return key;
	}

	// ========================= File I/O =========================

	private static void writeJson(File file, JsonObject json) throws IOException {
		try (FileWriter writer = new FileWriter(file)) {
			GSON.toJson(json, writer);
		}
	}

	// ========================= Tooltip / Visual =========================

	public void addScannerStateTooltip(ItemStack stack, List<Component> tooltip) {
		CustomData structureData = stack.get(DataComponents.CUSTOM_DATA);
		if (structureData == null) {
			return;
		}

		CompoundTag tag = structureData.copyTag();
		if (tag.getBoolean(TAG_HAS_POS1)) {
			String pos = tag.getInt(TAG_POS1_X) + ", " + tag.getInt(TAG_POS1_Y) + ", " + tag.getInt(TAG_POS1_Z);
			tooltip.add(Component.literal("Corner 1: " + pos).withStyle(ChatFormatting.GREEN));
			tooltip.add(Component.literal("Click corner 2 to scan!").withStyle(ChatFormatting.YELLOW));
		}
	}


	/** Shows enchantment glint while a corner is stored. */
	@Override
	public boolean isFoil(ItemStack stack) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		return customData != null && customData.copyTag().getBoolean(TAG_HAS_POS1);
	}
}
