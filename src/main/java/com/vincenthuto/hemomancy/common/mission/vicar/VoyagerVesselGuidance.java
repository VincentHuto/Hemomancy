package com.vincenthuto.hemomancy.common.mission.vicar;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class VoyagerVesselGuidance {
    private static final TagKey<Structure> TARGETS = TagKey.create(Registries.STRUCTURE,
            Hemomancy.rloc("voyager_introduction_targets"));
    private static final ArrayDeque<Search> SEARCHES = new ArrayDeque<>();
    private static final Set<UUID> PENDING = new HashSet<>();
    private static final int MAX_PENDING = 8;
    private static final int SEARCH_RADIUS = 100;

    private VoyagerVesselGuidance() {}

    static boolean request(ServerPlayer player) {
        if (PENDING.contains(player.getUUID())) {
            tell(player, "bearing_searching");
            return true;
        }
        if (PENDING.size() >= MAX_PENDING) {
            tell(player, "bearing_busy");
            return false;
        }
        PENDING.add(player.getUUID());
        SEARCHES.addLast(new Search(player.getUUID(), player.server.overworld(), player.blockPosition()));
        tell(player, "bearing_searching");
        return true;
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        long started = System.nanoTime();
        for (int probes = 0; probes < 2 && !SEARCHES.isEmpty(); probes++) {
            Search search = SEARCHES.removeFirst();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(search.player);
            if (!mayReceive(player)) {
                PENDING.remove(search.player);
                continue;
            }
            try {
                if (search.offsets == null && !search.initialize()) {
                    finish(search, player, "bearing_unavailable");
                } else if (!search.offsets.hasNext()) {
                    finish(search, player, "bearing_missing");
                } else {
                    BlockPos offset = search.offsets.next();
                    BlockPos sample = search.from.offset(offset.getX() * search.spacing * 16, 0,
                            offset.getZ() * search.spacing * 16);
                    // Radius zero asks vanilla to validate one placement region, not the whole voyage.
                    BlockPos found = search.level.findNearestMapStructure(TARGETS, sample, 0, false);
                    if (!PENDING.contains(search.player) || !mayReceive(player)
                            || event.getServer().getPlayerList().getPlayer(search.player) != player) {
                        PENDING.remove(search.player);
                        continue;
                    }
                    if (found == null) SEARCHES.addLast(search);
                    else {
                        PENDING.remove(search.player);
                        tell(player, "bearing", found.getX(), found.getZ());
                    }
                }
            } catch (RuntimeException failure) {
                Hemomancy.LOGGER.error("Voyager vessel chart search failed", failure);
                finish(search, player, "bearing_unavailable");
            }
            if (System.nanoTime() - started >= 4_000_000L) break;
        }
    }

    private static void finish(Search search, ServerPlayer player, String key) {
        PENDING.remove(search.player);
        tell(player, key);
    }

    private static boolean mayReceive(ServerPlayer player) {
        return player != null && player.isAlive() && VoyagerIntroduction.eligibleForReferral(player)
                && Level.OVERWORLD.equals(player.level().dimension()) && !VoyagerIntroduction.observed(player)
                && !VoyagerIntroduction.progress(player).reported();
    }

    private static void tell(ServerPlayer player, String key, Object... arguments) {
        player.displayClientMessage(Component.translatable(
                "hemomancy.vicar.voyager_introduction." + key, arguments).withStyle(ChatFormatting.DARK_RED), false);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID player = event.getEntity().getUUID();
        PENDING.remove(player);
        SEARCHES.removeIf(search -> search.player.equals(player));
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        SEARCHES.clear();
        PENDING.clear();
    }

    private static final class Search {
        private final UUID player;
        private final ServerLevel level;
        private final BlockPos from;
        private int spacing;
        private Iterator<BlockPos.MutableBlockPos> offsets;

        private Search(UUID player, ServerLevel level, BlockPos from) {
            this.player = player;
            this.level = level;
            this.from = from.immutable();
        }

        private boolean initialize() {
            var targets = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(TARGETS).orElse(null);
            if (targets == null || targets.size() != 1) return false;
            var placements = level.getChunkSource().getGeneratorState().getPlacementsForStructure(targets.get(0));
            if (placements.size() != 1 || !(placements.getFirst() instanceof RandomSpreadStructurePlacement spread))
                return false;
            spacing = spread.spacing();
            offsets = BlockPos.spiralAround(BlockPos.ZERO, SEARCH_RADIUS, Direction.EAST, Direction.SOUTH).iterator();
            return true;
        }
    }
}
