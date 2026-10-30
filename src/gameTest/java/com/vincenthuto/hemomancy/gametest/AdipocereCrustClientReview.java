package com.vincenthuto.hemomancy.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.AdipocereCrustBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.world.AdipocereCrustDecoration;
import com.vincenthuto.hemomancy.common.world.AdipocereCrustSavedData;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * With hemomancy.adipocereCrustReview (run adipocereCrustReviewClient, then drop
 * {"op":"create","name":"WaxReview","seed":4242} into the operator inbox), flies a spectator to the nearest stronghold
 * and then the nearest ancient city, lets the chunks load and decorate, checks every crust in the structure's loaded
 * pieces hangs on a masonry wall, and captures one up close to build/adipocere-crust-review/screenshots. Density is
 * raised to 10 for the review so a shot exists. Logs ADIPOCERE_REVIEW PASS/FAIL per check. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class AdipocereCrustClientReview {
	private static final int START = 60;
	private static final int STEP = 600;
	private static final int SETTLE = 400;
	private static final List<ResourceKey<Structure>> HOSTS = List.of(BuiltinStructures.STRONGHOLD,
			BuiltinStructures.ANCIENT_CITY);
	private static int ticks;

	private AdipocereCrustClientReview() {}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.adipocereCrustReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("adipocere-crust-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		MinecraftServer server = mc.getSingleplayerServer();
		UUID uuid = mc.player.getUUID();
		if (++ticks == 40) {
			server.execute(() -> {
				HemoServerConfig.ADIPOCERE_CRUST_DENSITY.set(10.0);
				ServerPlayer player = server.getPlayerList().getPlayer(uuid);
				var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
				for (String cmd : new String[] {"gamemode spectator", "difficulty peaceful", "time set 18000",
						"gamerule doDaylightCycle false", "gamerule doMobSpawning false"})
					server.getCommands().performPrefixedCommand(source, cmd);
				player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100000, 0, false, false));
				Hemomancy.LOGGER.info("ADIPOCERE_REVIEW staged");
			});
		}
		if (ticks < START) return;
		int index = (ticks - START) / STEP;
		int local = (ticks - START) % STEP;
		if (index >= HOSTS.size()) {
			mc.stop();
			return;
		}
		ResourceKey<Structure> host = HOSTS.get(index);
		String name = host.location().getPath();
		if (local == 0) server.execute(() -> fly(server.getPlayerList().getPlayer(uuid), host));
		if (local == SETTLE) server.execute(() -> frame(server.getPlayerList().getPlayer(uuid), host));
		if (local == SETTLE + 60) {
			Screenshot.grab(mc.gameDirectory, "adipocere-" + index + "-" + name + ".png", mc.getMainRenderTarget(),
					message -> {});
			Hemomancy.LOGGER.info("ADIPOCERE_REVIEW captured {} {}", index, name);
		}
	}

	/** Moves the reviewer to the nearest start of {@code host}, so its chunks load and the pass runs. */
	private static void fly(ServerPlayer player, ResourceKey<Structure> host) {
		ServerLevel level = player.serverLevel();
		var holder = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolderOrThrow(host);
		Pair<BlockPos, ?> found = level.getChunkSource().getGenerator().findNearestMapStructure(level,
				HolderSet.direct(holder), player.blockPosition(), 100, false);
		if (found == null) {
			Hemomancy.LOGGER.info("ADIPOCERE_REVIEW FAIL no {} within reach", host.location());
			return;
		}
		BlockPos at = found.getFirst();
		int y = host == BuiltinStructures.ANCIENT_CITY ? -40 : 20;
		player.teleportTo(level, at.getX() + 0.5, y, at.getZ() + 0.5, 0f, 30f);
		Hemomancy.LOGGER.info("ADIPOCERE_REVIEW flew to {} at {}", host.location(), at);
	}

	/** Checks the crusts in the loaded pieces and stands the reviewer in front of the nearest one. */
	private static void frame(ServerPlayer player, ResourceKey<Structure> host) {
		ServerLevel level = player.serverLevel();
		Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getOrThrow(host);
		StructureStart start = level.structureManager().getStructureAt(player.blockPosition(), structure);
		if (!start.isValid()) {
			start = nearestStart(level, player.blockPosition(), structure);
		}
		if (start == null || !start.isValid()) {
			Hemomancy.LOGGER.info("ADIPOCERE_REVIEW FAIL no loaded {} start near the reviewer", host.location());
			return;
		}
		List<BlockPos> crusts = new ArrayList<>();
		int loadedChunks = 0;
		int misplaced = 0;
		for (StructurePiece piece : start.getPieces()) {
			BoundingBox box = piece.getBoundingBox();
			for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(),
					box.maxZ())) {
				if (level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null) continue;
				BlockState state = level.getBlockState(pos);
				if (!state.is(BlockInit.adipocere_crust.get())) continue;
				Direction facing = state.getValue(AdipocereCrustBlock.FACING);
				if (!level.getBlockState(pos.relative(facing.getOpposite())).is(AdipocereCrustDecoration.WALLS))
					misplaced++;
				crusts.add(pos.immutable());
			}
		}
		for (var chunk : new java.util.HashSet<>(start.getPieces().stream()
				.flatMap(piece -> chunksOf(piece.getBoundingBox()).stream()).toList())) {
			if (level.getChunkSource().getChunkNow(chunk.x, chunk.z) != null) loadedChunks++;
		}
		Hemomancy.LOGGER.info("ADIPOCERE_REVIEW {}: {} crusts in {} loaded chunks; {} chunks recorded in this dimension",
				host.location(), crusts.size(), loadedChunks, AdipocereCrustSavedData.get(level).size());
		Hemomancy.LOGGER.info("ADIPOCERE_REVIEW {} every {} crust hangs on masonry ({} misplaced)",
				misplaced == 0 ? "PASS" : "FAIL", host.location(), misplaced);
		if (crusts.isEmpty()) {
			Hemomancy.LOGGER.info("ADIPOCERE_REVIEW FAIL no crust to frame in {}", host.location());
			return;
		}
		BlockPos here = player.blockPosition();
		BlockPos crust = crusts.stream().min((a, b) -> Double.compare(a.distSqr(here), b.distSqr(here))).orElseThrow();
		Direction facing = level.getBlockState(crust).getValue(AdipocereCrustBlock.FACING);
		double x = crust.getX() + 0.5 + facing.getStepX() * 2.2;
		double z = crust.getZ() + 0.5 + facing.getStepZ() * 2.2;
		player.teleportTo(level, x, crust.getY() - 0.9, z, facing.getOpposite().toYRot(), 10f);
		Hemomancy.LOGGER.info("ADIPOCERE_REVIEW framed crust at {} facing {}", crust, facing);
	}

	private static StructureStart nearestStart(ServerLevel level, BlockPos pos, Structure structure) {
		ChunkPos centre = new ChunkPos(pos);
		for (int r = 0; r <= 6; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (level.getChunkSource().getChunkNow(centre.x + dx, centre.z + dz) == null) continue;
					var starts = level.structureManager().startsForStructure(
							new ChunkPos(centre.x + dx, centre.z + dz), s -> s == structure);
					if (!starts.isEmpty()) return starts.get(0);
				}
			}
		}
		return null;
	}

	private static List<ChunkPos> chunksOf(BoundingBox box) {
		List<ChunkPos> chunks = new ArrayList<>();
		for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++) {
			for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) chunks.add(new ChunkPos(x, z));
		}
		return chunks;
	}
}
