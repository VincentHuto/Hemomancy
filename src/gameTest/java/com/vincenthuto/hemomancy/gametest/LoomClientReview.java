package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.Comparator;
import java.util.UUID;

/**
 * With hemomancy.loomReview (run loomReviewClient), stages Somatic Looms weaving their engrams in a disposable world
 * and captures them into build/loom-client-review/screenshots: a Humilis engram just unfurled, a keyed Summa engram
 * half threaded under fungal pressure, an east-facing Ganglionic Magister engram with a prism, a Cortical Perfectus
 * engram under its Chamber dome, an engram draped over broken ground, by day and night, from above and at eye
 * height, and a climax. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LoomClientReview {
    private static int ticks;
    private static int shot;
    /** The shot whose camera this cycle placed: only it may be captured. */
    private static int prepared = -1;
    private static BlockPos row;
    private static SomaticLoomBlockEntity humilis, summa, magister, perfectus, rough;
    private static FakePlayer summaWeaver;

    /** {loom, eye dx, dy, dz from the loom, look dy, night (1), climax (1)} */
    private record Shot(String name, int loom, double dx, double dy, double dz, double lookY, boolean night, boolean climax) {}

    private static final Shot[] SHOTS = {
            new Shot("humilis-above", 0, 0, 7.5, -7.5, 0, false, false),
            new Shot("summa-above", 1, 4.5, 7.5, -8.0, 0, false, false),
            new Shot("summa-eye", 1, 1.0, 1.7, -7.5, 0.2, false, false),
            new Shot("summa-overhead", 1, 0.0, 13.0, -1.0, 0, false, false),
            new Shot("magister-above", 2, 8.5, 7.5, 3.5, 0, false, false),
            new Shot("perfectus-above", 3, 0.0, 9.0, -9.5, 0, false, false),
            new Shot("perfectus-eye", 3, -2.0, 1.7, -7.0, 0.4, false, false),
            new Shot("rough-above", 4, 5.0, 7.0, -8.0, 0, false, false),
            new Shot("rough-grazing", 4, -6.5, 1.4, -4.0, 0, false, false),
            new Shot("summa-night", 1, 4.5, 7.5, -8.0, 0, true, false),
            new Shot("perfectus-night", 3, 0.0, 9.0, -9.5, 0, true, false),
            new Shot("humilis-listen", 0, 0, 0, 0, 0, false, false),
            new Shot("humilis-cursor", 0, 0, 0, 0, 0, false, false),
            // Close-ups of each marking an engram can carry, for the player guide.
            new Shot("close-fragment", 4, 0, 0, 0, 0, false, false),
            new Shot("close-home", 2, 0, 0, 0, 0, false, false),
            new Shot("close-scar", 2, 0, 0, 0, 0, false, false),
            new Shot("close-valve", 2, 0, 0, 0, 0, false, false),
            new Shot("close-keyed", 1, 0, 0, 0, 0, false, false),
            new Shot("close-rot", 1, 0, 0, 0, 0, false, false),
            new Shot("close-prism", 2, 0, 0, 0, 0, false, false),
            new Shot("close-grey", 2, 0, 0, 0, 0, false, false),
            new Shot("close-husk", 2, 0, 0, 0, 0, false, false),
            new Shot("close-false", 3, 0, 0, 0, 0, false, false),
            new Shot("close-held", 0, 0, 0, 0, 0, false, false),
            new Shot("close-refused", 0, 0, 0, 0, 0, false, false),
            // A weave cancelled at the loom dissolves rather than vanishing: caught early, and later.
            new Shot("perfectus-dissolve", 3, 0.0, 9.0, -9.5, 0, false, false),
            new Shot("magister-dissolve", 2, 8.5, 7.5, 3.5, 0, false, false),
            // Last: the climax finishes the Summa weave, so nothing may look at that engram after it.
            new Shot("summa-climax", 1, 4.5, 6.0, -7.5, 0.6, false, true)};

    private LoomClientReview() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.loomReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("loom-client-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = true;
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        if (++ticks == 40) {
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                row = player.blockPosition().offset(0, 0, 14);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String cmd : new String[]{"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
                        "weather clear", "gamerule doMobSpawning false", "kill @e[type=item]",
                        "kill @e[type=hemomancy:frayed_thread]"})
                    server.getCommands().performPrefixedCommand(source, cmd);
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(-40, -3, -40), row.offset(40, 8, 12)))
                    level.setBlock(pos, pos.getY() < row.getY() ? (pos.getZ() < row.getZ() - 14 ? Blocks.GRASS_BLOCK : Blocks.DEEPSLATE_TILES).defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);
                player.teleportTo(level, row.getX() + 0.5, row.getY() + 6, row.getZ() - 12, 0, 30);
                stage(level, player);
                Hemomancy.LOGGER.info("LOOM_REVIEW staged at {}", row);
            });
        }
        if (ticks < 260 || row == null) return;
        int local = (ticks - 260) % 70;
        if (local == 0) {
            if (shot == SHOTS.length) {
                mc.stop();
                return;
            }
            Shot s = SHOTS[shot];
            prepared = shot;
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                server.getCommands().performPrefixedCommand(source, s.night() ? "time set 18000" : "time set 6000");
                SomaticLoomBlockEntity loom = loom(s.loom());
                if (s.climax() && loom != null && !loom.isInClimax()) finish(loom, summaWeaver);
                var centre = loom == null ? row.getCenter() : loom.sigilCentre();
                double x = centre.x + s.dx(), y = centre.y + s.dy(), z = centre.z + s.dz();
                double fx = centre.x, fy = centre.y + s.lookY(), fz = centre.z;
                if (s.name().endsWith("listen") && loom != null && loom.getEngram() != null) {
                    // The weaver stands over a fragment with the staff and rests their gaze on it.
                    var node = loom.nodeWorld(loom.getEngram().fragment(0).node());
                    var toLoom = centre.subtract(node).normalize();
                    x = node.x - toLoom.x * 2.6 + 1.2;
                    y = node.y + 2.6;
                    z = node.z - toLoom.z * 2.6;
                    fx = node.x;
                    fy = node.y + 0.3;
                    fz = node.z;
                    player.getInventory().setItem(0, new ItemStack(ItemInit.living_staff.get()));
                    player.getInventory().selected = 0;
                }
                if (s.name().endsWith("cursor") && loom != null && loom.getEngram() != null) {
                    // Close over the ground between a fragment and the loom: the staff's pixel motes on the face.
                    var node = loom.nodeWorld(loom.getEngram().fragment(0).node());
                    var toLoom = centre.subtract(node).normalize();
                    fx = node.x + toLoom.x * 1.3;
                    fy = node.y;
                    fz = node.z + toLoom.z * 1.3;
                    x = fx - toLoom.x * 1.3 + 0.6;
                    y = node.y + 1.5;
                    z = fz - toLoom.z * 1.3;
                    player.getInventory().setItem(0, new ItemStack(ItemInit.living_staff.get()));
                    player.getInventory().selected = 0;
                }
                if (s.name().startsWith("close-") && loom != null && loom.getEngram() != null) {
                    String kind = s.name().substring("close-".length());
                    EngramBoard board = loom.getEngram();
                    int node = closeNode(loom, kind);
                    var focus = node < 0 ? centre : loom.nodeWorld(node);
                    if (kind.equals("held") || kind.equals("refused")) {
                        // The reviewer takes their first fragment's lane and draws it one node: the staff then points
                        // at the next node of its way home (allowed) or across the engram (refused).
                        int fragment = 0;
                        int[] path = board.solution(fragment);
                        if (board.lane(fragment).length == 1) {
                            loom.engramStep(player, path[0], false, true);
                            loom.engramStep(player, path[1], false, false);
                        }
                        focus = loom.nodeWorld(path[1]);
                        int aimAt = kind.equals("held") ? path[2] : refusedNear(loom, fragment, path[1]);
                        var target = loom.nodeWorld(aimAt);
                        var out = focus.subtract(centre).multiply(1, 0, 1).normalize();
                        x = focus.x + out.x * 1.8 + 0.4;
                        y = focus.y + 2.6;
                        z = focus.z + out.z * 1.8;
                        fx = target.x;
                        fy = target.y;
                        fz = target.z;
                        player.getInventory().setItem(0, new ItemStack(ItemInit.living_staff.get()));
                        player.getInventory().selected = 0;
                    } else {
                        if (node < 0) Hemomancy.LOGGER.info("LOOM_REVIEW no {} on loom {}", kind, s.loom());
                        var out = focus.subtract(centre).multiply(1, 0, 1);
                        out = out.lengthSqr() < 1.0E-4 ? new net.minecraft.world.phys.Vec3(0, 0, -1) : out.normalize();
                        if (kind.equals("rot")) focus = focus.add(out.scale(1.2));
                        x = focus.x + out.x * 1.35 + 0.4;
                        y = focus.y + 2.25;
                        z = focus.z + out.z * 1.35;
                        fx = focus.x;
                        fy = focus.y + 0.35;
                        fz = focus.z;
                        player.getInventory().setItem(0, ItemStack.EMPTY);
                    }
                }
                float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
                float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
                player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, yaw, pitch);
            });
        }
        Shot due = SHOTS[Math.min(shot, SHOTS.length - 1)];
        if (local == 4 && due.name().endsWith("dissolve") && prepared == shot) {
            // Cancelled once the camera has been looking a moment, as a player at the loom would be.
            server.execute(() -> {
                SomaticLoomBlockEntity loom = loom(due.loom());
                if (loom != null && loom.isWeaving()) loom.cancelRitual(null);
            });
        }
        int captureAt = due.climax() ? 30 : due.name().startsWith("perfectus-dissolve") ? 18
                : due.name().endsWith("dissolve") ? 34 : 50;
        if (local == captureAt && shot < SHOTS.length && prepared == shot) {
            Screenshot.grab(mc.gameDirectory, "engram-" + shot + "-" + SHOTS[shot].name() + ".png", mc.getMainRenderTarget(), message -> {});
            Hemomancy.LOGGER.info("LOOM_REVIEW captured {} {}", shot, SHOTS[shot].name());
            shot++;
        }
    }

    /** The node nearest the lane's head that the held lane could not step to. */
    private static int refusedNear(SomaticLoomBlockEntity te, int fragment, int head) {
        int[] stock = new int[8];
        for (EnumBloodTendency tend : EnumBloodTendency.values()) stock[tend.ordinal()] = te.getStoredEnzyme(tend);
        var at = te.nodeWorld(head);
        int best = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int n = 0; n < 24; n++) {
            if (n == head) continue;
            EngramBoard trial = EngramBoard.load(te.getEngram().save());
            var step = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules.sweep(trial, fragment, n, stock);
            if (step.moved() || step == com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules.Step.IDLE
                    || step == com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules.Step.LANDED) continue;
            double distance = te.nodeWorld(n).distanceTo(at);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = n;
            }
        }
        return best < 0 ? head : best;
    }

    /** A node of the engram carrying the marking a close-up shows, or -1. */
    private static int closeNode(SomaticLoomBlockEntity te, String kind) {
        EngramBoard board = te.getEngram();
        for (int i = 0; i < board.fragmentCount(); i++) {
            EngramBoard.Fragment f = board.fragment(i);
            boolean home = com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules.connected(board, i);
            boolean hit = switch (kind) {
                case "fragment" -> !f.intrusion() && !f.grey() && !home && board.lane(i).length == 1;
                case "home" -> !f.intrusion() && home;
                case "false" -> f.intrusion() && !f.premarked();
                case "husk" -> f.intrusion() && f.premarked();
                case "grey" -> f.grey();
                default -> false;
            };
            if (hit) return f.node();
        }
        EngramBoard.NodeKind want = switch (kind) {
            case "scar" -> EngramBoard.NodeKind.SCAR;
            case "valve" -> EngramBoard.NodeKind.MEMBRANE;
            case "prism" -> EngramBoard.NodeKind.PRISM;
            default -> null;
        };
        if (want != null) for (int n = 0; n < 24; n++) if (board.kind(n) == want) return n;
        if (kind.equals("keyed")) return 16 + board.rotation();
        if (kind.equals("rot")) return 4;
        return -1;
    }

    private static SomaticLoomBlockEntity loom(int index) {
        return switch (index) {
            case 0 -> humilis;
            case 1 -> summa;
            case 2 -> magister;
            case 3 -> perfectus;
            default -> rough;
        };
    }

    private static void stage(ServerLevel level, net.minecraft.server.level.ServerPlayer reviewer) {
        // The reviewer weaves this one themself, so its staff mark and a heard fragment can be captured.
        humilis = weave(level, row.offset(-30, 0, 0), Direction.NORTH, 0, "memory_weaving/memory_blood_shot", reviewer);

        summa = weave(level, row.offset(-10, 0, 0), Direction.NORTH, 0, recipeOfRank(level, EnumManipulationRank.SUMMA, 2));
        summaWeaver = weaverOf(summa);
        thread(summa, 0, 99);
        thread(summa, 1, 2);
        bend(summa, 2);
        Hemomancy.LOGGER.info("LOOM_REVIEW summa pressure={} keyed={}", summa.getEngram().pressure(), summa.getEngram().keyed());

        magister = weave(level, row.offset(10, 0, 0), Direction.EAST, 1, recipeOfRank(level, EnumManipulationRank.MAGISTER, 1));
        thread(magister, 0, 99);
        thread(magister, 1, 3);
        // As if a bloodline member had woven it already: its false fragments show as husks.
        magister.getEngram().premarkIntrusions();
        magister.sendUpdates();

        perfectus = weave(level, row.offset(30, 0, 0), Direction.NORTH, 2, "memory_weaving/memory_axonal_transduction");
        thread(perfectus, 0, 99);
        thread(perfectus, 1, 99);
        thread(perfectus, 2, 2);

        BlockPos roughPos = row.offset(0, 0, -26);
        roughen(level, roughPos);
        rough = weave(level, roughPos, Direction.SOUTH, 0, recipeOfRank(level, EnumManipulationRank.MEDIOCRITAS, 1));
        thread(rough, 0, 2);
        Hemomancy.LOGGER.info("LOOM_REVIEW humilis={} summa={} magister={} perfectus={} rough={}", humilis.isWeaving(),
                summa.isWeaving(), magister.isWeaving(), perfectus.isWeaving(), rough.isWeaving());
    }

    /** Broken ground round a loom: a raised step, a sunken pit, a low wall and a few scattered slabs and carpets. */
    private static void roughen(ServerLevel level, BlockPos centre) {
        BlockState stone = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        for (int dx = 2; dx <= 6; dx++) for (int dz = -6; dz <= 1; dz++) level.setBlock(centre.offset(dx, 0, dz), stone, 2);
        for (int dx = -6; dx <= -3; dx++) for (int dz = 0; dz <= 5; dz++) level.setBlock(centre.offset(dx, -1, dz), Blocks.AIR.defaultBlockState(), 2);
        for (int dz = -7; dz <= -5; dz++) {
            level.setBlock(centre.offset(-3, 0, dz), stone, 2);
            level.setBlock(centre.offset(-3, 1, dz), stone, 2);
        }
        level.setBlock(centre.offset(1, 0, 4), Blocks.DEEPSLATE_BRICK_SLAB.defaultBlockState(), 2);
        level.setBlock(centre.offset(-1, 0, -4), Blocks.RED_CARPET.defaultBlockState(), 2);
        level.setBlock(centre.offset(4, 1, 3), Blocks.MOSS_CARPET.defaultBlockState(), 2);
    }

    private static String recipeOfRank(ServerLevel level, EnumManipulationRank rank, int tendencies) {
        String fallback = null;
        for (RecipeHolder<MemoryWeavingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeInit.memory_weaving_type.get())
                .stream().sorted(Comparator.comparing(h -> h.id().toString())).toList()) {
            var result = holder.value().getResultItem(level.registryAccess()).getItem();
            if (!(result instanceof BloodMemoryItem memory) || memory.getManip() == null || memory.getManip().getRank() != rank) continue;
            if (!holder.value().getCatalysts().stream().allMatch(c -> c.getItems().length > 0)) continue;
            if (fallback == null) fallback = holder.id().getPath();
            if (holder.value().getRequiredTendencyCount() >= tendencies && holder.value().getTotalEnzymeRequirement() >= 2)
                return holder.id().getPath();
        }
        return fallback;
    }

    private static SomaticLoomBlockEntity weave(ServerLevel level, BlockPos pos, Direction facing, int stage, String recipe) {
        return weave(level, pos, facing, stage, recipe, null);
    }

    private static SomaticLoomBlockEntity weave(ServerLevel level, BlockPos pos, Direction facing, int stage, String recipe,
            net.minecraft.world.entity.player.Player payer) {
        var block = (SomaticLoomBlock) BlockInit.somatic_loom.get();
        var state = block.defaultBlockState().setValue(SomaticLoomBlock.FACING, facing).setValue(SomaticLoomBlock.STAGE, stage);
        level.setBlock(pos, state, 3);
        block.placeFillers(level, pos, state);
        var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
        var weaving = (MemoryWeavingRecipe) level.getRecipeManager().byKey(Hemomancy.rloc(recipe)).orElseThrow().value();
        te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        for (var catalyst : weaving.getCatalysts()) te.addItem(null, catalyst.getItems()[0].copyWithCount(1), null);
        CompoundTag tag = te.saveWithoutMetadata(level.registryAccess());
        CompoundTag stock = new CompoundTag();
        for (EnumBloodTendency tend : EnumBloodTendency.values()) stock.putInt(tend.name(), 120);
        tag.put("storedEnzymes", stock);
        te.loadWithComponents(tag, level.registryAccess());
        te.selectRecipe(null, Hemomancy.rloc(recipe));
        var weaver = new FakePlayer(level, new com.mojang.authlib.GameProfile(UUID.nameUUIDFromBytes(("review:" + pos).getBytes()), "loom_review"));
        var front = te.sigilCentre().add(facing.getStepX() * 2.5D, 0, facing.getStepZ() * 2.5D);
        weaver.moveTo(front.x, pos.getY(), front.z);
        var paying = payer == null ? weaver : payer;
        var v = HemoCapabilityAccess.getBloodVolume(paying);
        v.setActive(true);
        v.setBloodVolume(v.getMaxBloodVolume());
        te.tryChargeRitualBlood(paying, 10_000, false);
        WEAVERS.put(pos.immutable(), weaver);
        return te;
    }

    private static final java.util.Map<BlockPos, FakePlayer> WEAVERS = new java.util.HashMap<>();

    private static FakePlayer weaverOf(SomaticLoomBlockEntity te) {
        return WEAVERS.get(te.getBlockPos());
    }

    /** Threads the `index`th true fragment `steps` nodes along its reference lane (99: all the way home). */
    private static void thread(SomaticLoomBlockEntity te, int index, int steps) {
        EngramBoard board = te.getEngram();
        if (board == null) return;
        int seen = 0;
        for (int fragment = 0; fragment < board.fragmentCount(); fragment++) {
            if (board.fragment(fragment).intrusion()) continue;
            if (seen++ != index) continue;
            int[] path = board.solution(fragment);
            if (path == null) return;
            var weaver = weaverOf(te);
            te.engramStep(weaver, path[0], false, true);
            for (int k = 1; k < path.length && k <= steps; k++) te.engramStep(weaver, path[k], false, false);
            te.releaseEngram(weaver);
            return;
        }
    }

    /** Threads a reachable intrusion home `times` times, building fungal pressure. */
    private static void bend(SomaticLoomBlockEntity te, int times) {
        for (int i = 0; i < times; i++) {
            EngramBoard board = te.getEngram();
            int[] stock = new int[8];
            for (EnumBloodTendency tend : EnumBloodTendency.values()) stock[tend.ordinal()] = te.getStoredEnzyme(tend);
            for (int lie : LoomWeaveTestSupport.intrusions(board)) {
                int[] path = LoomWeaveTestSupport.homeward(board, lie, stock);
                if (path == null) continue;
                var weaver = weaverOf(te);
                te.engramStep(weaver, board.fragment(lie).node(), false, true);
                for (int k = 1; k < path.length; k++) te.engramStep(weaver, path[k], false, false);
                break;
            }
        }
    }

    /** Threads every true fragment home, so the climax begins. */
    private static void finish(SomaticLoomBlockEntity te, net.minecraft.world.entity.player.Player weaver) {
        EngramBoard board = te.getEngram();
        if (board == null) return;
        for (int fragment = 0; fragment < board.fragmentCount() && !te.isInClimax(); fragment++) {
            if (board.fragment(fragment).intrusion()) continue;
            int[] path = board.solution(fragment);
            int[] lane = te.getEngram().lane(fragment);
            if (lane[lane.length - 1] == com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.VESSEL) continue;
            // Pressing the lane's head (or its fragment) takes it; the rest of its reference threads it home.
            te.engramStep(weaver, lane[lane.length - 1], false, true);
            for (int k = lane.length; k < path.length; k++) te.engramStep(weaver, path[k], false, false);
        }
        Hemomancy.LOGGER.info("LOOM_REVIEW climax={}", te.isInClimax());
    }
}
