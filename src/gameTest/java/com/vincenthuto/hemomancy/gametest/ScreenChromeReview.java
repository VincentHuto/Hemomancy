package com.vincenthuto.hemomancy.gametest;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.item.BloodlinePoolScreen;
import com.vincenthuto.hemomancy.client.screen.item.LedgerScreen;
import com.vincenthuto.hemomancy.client.screen.item.VascularStatusScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.SSCScreen;
import com.vincenthuto.hemomancy.client.screen.tile.functional.SynapticLoadoutScreen;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemorySlotRef;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemoryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemoryPrimingRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncSkills;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.DendriticDistributorBlockEntity;
import com.vincenthuto.hemomancy.common.menu.ScryingDiagnosticsMenuProvider;
import com.vincenthuto.hemomancy.common.menu.TemperamentViewMenuProvider;
import com.vincenthuto.hemomancy.common.menu.VascularViewMenuProvider;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarMenuProvider;
import com.vincenthuto.hemomancy.common.menu.tile.functional.FungalImplantMenuProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * With hemomancy.screenChromeReview, opens every screen that draws Hemomancy screen chrome (borders, slot wells, vein
 * backgrounds) and captures each one's first frame. The frame is drawn by hand into a black target the moment the
 * screen opens, before the game renders it, so the frame-counted vein animation and the blurred world behind it are
 * identical from run to run: a before and an after run of a pristine ScreenChromeReview save compare pixel for pixel.
 * Shots land in the game directory's screenshots folder as {@code chrome-<label>-<scene>.png}. Excluded from release
 * jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ScreenChromeReview {
    private static final String SAVE = "ScreenChromeReview";
    private static final int WARM_UP_TICKS = 80;
    private static final int SCENE_TIMEOUT = 60;
    private static final int OPEN_DELAY = 10;
    private static final long PINNED_GAME_TIME = 48_000L;

    /**
     * Opens one screen: {@code client} runs on the render thread; otherwise {@code prepare} runs on the integrated
     * server, and {@code server} runs there {@link #OPEN_DELAY} ticks later, once the client has the placed block or
     * held item.
     */
    private record Scene(String name, Class<? extends Screen> screen, Runnable client, Consumer<ServerPlayer> prepare,
                         Consumer<ServerPlayer> server) {
        static Scene client(String name, Class<? extends Screen> screen, Supplier<Screen> make) {
            return new Scene(name, screen, () -> Minecraft.getInstance().setScreen(make.get()), null, null);
        }

        static Scene server(String name, String screenClass, Consumer<ServerPlayer> open) {
            return new Scene(name, screenClass(screenClass), null, player -> {}, open);
        }

        static Scene server(String name, String screenClass, Consumer<ServerPlayer> prepare, Consumer<ServerPlayer> open) {
            return new Scene(name, screenClass(screenClass), null, prepare, open);
        }
    }

    private static final List<Scene> SCENES = List.of(
            Scene.client("bloodline_pool", BloodlinePoolScreen.class, BloodlinePoolScreen::new),
            Scene.client("ledger", LedgerScreen.class, LedgerScreen::new),
            Scene.client("vascular_status", VascularStatusScreen.class, VascularStatusScreen::new),
            Scene.client("unstained_progress", UnstainedProgressScreen.class, UnstainedProgressScreen::new),
            Scene.client("ssc", SSCScreen.class, () -> new SSCScreen(BlockPos.ZERO, 640, 1000, List.of())),
            Scene.client("harbinger_progress", com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen.class,
                    com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen::new),
            Scene.client("heart_node", com.vincenthuto.hemomancy.client.screen.tile.functional.HeartNodeScreen.class,
                    () -> new com.vincenthuto.hemomancy.client.screen.tile.functional.HeartNodeScreen(
                            HeartNodeScreenScenes.sampleScreenState(BlockPos.ZERO, java.util.UUID.nameUUIDFromBytes(new byte[] {7}), false))),
            Scene.client("succession_residents", com.vincenthuto.hemomancy.client.screen.item.SuccessionResidentsScreen.class,
                    () -> new com.vincenthuto.hemomancy.client.screen.item.SuccessionResidentsScreen(null)),
            Scene.client("synaptic_loadout", SynapticLoadoutScreen.class, () -> new SynapticLoadoutScreen(BlockPos.ZERO)),
            Scene.server("temperament_view", "item.TemperamentViewScreen", p -> p.openMenu(new TemperamentViewMenuProvider())),
            Scene.server("scrying_diagnostics", "item.ScryingDiagnosticsScreen", p -> p.openMenu(new ScryingDiagnosticsMenuProvider())),
            Scene.server("vascular_view", "item.VascularViewScreen", p -> p.openMenu(new VascularViewMenuProvider())),
            Scene.server("harbinger_equipment", "tile.functional.HarbingerEquipmentScreen", p -> p.openMenu(new ScarMenuProvider(true))),
            Scene.server("spore_implant", "tile.functional.SporeImplantScreen", p -> p.openMenu(new FungalImplantMenuProvider())),
            item("morphling_jar", "item.living.MorphlingJarScreen", ItemInit.morphling_jar),
            item("structure_spawner", "item.StructureSpawnerScreen", ItemInit.structure_spawner),
            block("scar_station", "tile.crafting.scar.ScarStationScreen", BlockInit.scar_station),
            block("vial_centrifuge", "tile.crafting.VialCentrifugeScreen", BlockInit.vial_centrifuge),
            block("ghastly_alembic", "tile.crafting.GhastlyAlembicScreen", BlockInit.ghastly_alembic),
            block("pallid_retort", "tile.crafting.PallidRetortScreen", BlockInit.pallid_retort),
            block("masons_effigy", "tile.functional.MasonsEffigyScreen", BlockInit.mason_effigy),
            block("morphling_incubator", "tile.crafting.MorphlingIncubatorScreen", BlockInit.morphling_incubator),
            block("puppeteers_spindle", "item.PuppeteersSpindleScreen", BlockInit.puppeteers_spindle),
            block("mnemonic_reliquary", "tile.functional.MnemonicReliquaryScreen", BlockInit.mnemonic_reliquary),
            block("mycelial_crucible", "tile.crafting.MycelialCrucibleScreen", BlockInit.mycelial_crucible),
            block("mycelial_lantern", "tile.crafting.MycelialLanternScreen", BlockInit.mycelial_lantern),
            block("enzymatic_scriptorium", "tile.crafting.EnzymaticScriptoriumScreen", BlockInit.enzymatic_scriptorium),
            Scene.server("enzymatic_scriptorium_stocked", "tile.crafting.EnzymaticScriptoriumScreen",
                    ScreenChromeReview::placeStockedScriptorium, ScreenChromeReview::openStockedScriptorium),
            block("resonant_forge", "tile.crafting.ResonantForgeScreen", BlockInit.resonant_forge),
            Scene.server("resonant_forge_precision", "tile.crafting.ResonantForgeScreen",
                    p -> placeLoadedForge(p, 1), ScreenChromeReview::openStockedScriptorium),
            Scene.server("resonant_forge_masterwork", "tile.crafting.ResonantForgeScreen",
                    p -> placeLoadedForge(p, 2), ScreenChromeReview::openStockedScriptorium),
            block("clairaudiograph", "tile.functional.ClairaudiographScreen", BlockInit.clairaudiograph),
            block("phlebotomists_cabinet", "tile.functional.PhlebotomistsCabinetScreen", BlockInit.phlebotomists_cabinet),
            Scene.server("specimen_vessel", "tile.functional.SpecimenVesselScreen", ScreenChromeReview::placeVessel,
                    ScreenChromeReview::openVessel),
            // Last, because it gives the reviewer a bloodline that the ledger and pool scenes would otherwise show.
            new Scene("bloodline_pool_fane", BloodlinePoolScreen.class, ScreenChromeReview::openPoolWithFane, null, null),
            // A full pool greys the Donate button (PT-42).
            new Scene("bloodline_pool_full", BloodlinePoolScreen.class, ScreenChromeReview::openFullPool, null, null),
            // The leader's petition row (PT-35).
            new Scene("ledger_petitions", LedgerScreen.class, ScreenChromeReview::openLedgerWithPetitions, null, null),
            stockedSynapticLoadouts(),
            // Last of all: it teaches the reviewer every memory, so the Reliquary's drawers, sockets and veins fill.
            stockedReliquary());

    private static void reviewLine() {
        Minecraft minecraft = Minecraft.getInstance();
        java.util.UUID leader = minecraft.player.getUUID();
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getBloodVolume(minecraft.player).setBloodLine(
                new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline("Chrome Review",
                        leader, java.util.UUID.nameUUIDFromBytes(new byte[] {9}), new java.util.ArrayList<>(List.of(leader))));
    }

    private static void openFullPool() {
        reviewLine();
        com.vincenthuto.hemomancy.client.data.BloodlinePoolClientData.set(10_000.0F, 10_000.0F, 2);
        Minecraft.getInstance().setScreen(new BloodlinePoolScreen());
    }

    private static void openLedgerWithPetitions() {
        reviewLine();
        com.vincenthuto.hemomancy.client.data.BloodlinePetitionClientData.set(List.of(
                new com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodlinePetitions.Petition(
                        java.util.UUID.nameUUIDFromBytes(new byte[] {21}), "Ashwell"),
                new com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodlinePetitions.Petition(
                        java.util.UUID.nameUUIDFromBytes(new byte[] {22}), "Corvane")));
        Minecraft.getInstance().setScreen(new LedgerScreen());
    }

    /** The pool screen with a sample fane on its map: four stakes (the last cut off) and a neighbour's fane. */
    private static void openPoolWithFane() {
        Minecraft minecraft = Minecraft.getInstance();
        java.util.UUID leader = minecraft.player.getUUID();
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getBloodVolume(minecraft.player).setBloodLine(
                new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline("Chrome Review",
                        leader, java.util.UUID.nameUUIDFromBytes(new byte[] {9}), List.of(leader)));
        BlockPos heart = minecraft.player.blockPosition().offset(12, 0, -8);
        com.vincenthuto.hemomancy.client.data.FaneBoundaryClientData.setEntries(List.of(
                new com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncFaneBoundaries.Entry(heart,
                        List.of(heart.offset(62, 0, 10), heart.offset(110, 0, 40), heart.offset(-20, 0, 64),
                                heart.offset(-150, 0, -30)), 32.0F, leader,
                        com.vincenthuto.hemomancy.common.event.worldevent.FaneBoundaryRelation.MEMBER),
                new com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncFaneBoundaries.Entry(
                        heart.offset(-90, 0, -130), List.of(), 32.0F, java.util.UUID.nameUUIDFromBytes(new byte[] {11}),
                        com.vincenthuto.hemomancy.common.event.worldevent.FaneBoundaryRelation.OUTSIDER)));
        minecraft.setScreen(new BloodlinePoolScreen());
    }

    private static int ticks;
    private static int scene = -1;
    private static int sceneTicks;
    private static boolean captured;
    private static int shots;
    private static BlockPos anchor;

    private ScreenChromeReview() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.screenChromeReview")) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        if (!mc.getSingleplayerServer().getWorldData().getLevelName().equals(SAVE))
            throw new IllegalStateException("Screen chrome review requires the disposable " + SAVE + " save");
        mc.options.pauseOnLostFocus = false;
        int tick = ticks++;
        if (tick == 0) {
            mc.options.guiScale().set(2);
            mc.resizeDisplay();
            var server = mc.getSingleplayerServer();
            var uuid = mc.player.getUUID();
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String cmd : new String[]{"time set 6000", "gamerule doDaylightCycle false", "weather clear",
                        "gamerule doMobSpawning false", "effect clear @s", "tick freeze"})
                    server.getCommands().performPrefixedCommand(source, cmd);
                player.setGameMode(GameType.CREATIVE);
                anchor = player.blockPosition().above(40);
                player.teleportTo(player.serverLevel(), anchor.getX() + .5, anchor.getY(), anchor.getZ() + .5, 0, 0);
            });
        }
        if (tick < WARM_UP_TICKS || anchor == null) return;
        if (scene >= 0 && !captured && ++sceneTicks < SCENE_TIMEOUT) {
            Scene current = SCENES.get(scene);
            if (sceneTicks == OPEN_DELAY && current.server != null) onServer(mc, current.server);
            return;
        }
        if (scene >= 0 && !captured) Hemomancy.LOGGER.warn("SCREEN_CHROME_REVIEW {} did not open", SCENES.get(scene).name);
        if (mc.screen != null) {
            mc.player.closeContainer();
            mc.setScreen(null);
        }
        if (++scene == SCENES.size()) {
            Hemomancy.LOGGER.info("SCREEN_CHROME_REVIEW captured {}", shots);
            mc.stop();
            return;
        }
        captured = false;
        sceneTicks = 0;
        Scene next = SCENES.get(scene);
        if (next.client != null) next.client.run();
        else onServer(mc, next.prepare);
    }

    private static void onServer(Minecraft mc, Consumer<ServerPlayer> action) {
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        server.execute(() -> action.accept(server.getPlayerList().getPlayer(uuid)));
    }

    /** Draws the opening screen's first frame before the game does. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void opening(ScreenEvent.Opening event) {
        if (!Boolean.getBoolean("hemomancy.screenChromeReview") || scene < 0 || scene >= SCENES.size() || captured) return;
        Scene current = SCENES.get(scene);
        Screen screen = event.getNewScreen();
        if (screen == null || screen.getClass() != current.screen) return;
        captured = true;
        try {
            capture(screen, current.name);
            if (current.name.equals("synaptic_loadout_stocked")) checkSynapticNavigation(screen);
        } catch (RuntimeException e) {
            Hemomancy.LOGGER.error("SCREEN_CHROME_REVIEW {} failed", current.name, e);
        }
    }

    private static void capture(Screen screen, String name) {
        var mc = Minecraft.getInstance();
        var window = mc.getWindow();
        int width = window.getGuiScaledWidth(), height = window.getGuiScaledHeight();
        var target = mc.getMainRenderTarget();
        target.bindWrite(true);
        RenderSystem.clearColor(0, 0, 0, 1);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        // Screens that animate on game time read the same tick in every run.
        mc.level.setGameTime(PINNED_GAME_TIME);
        screen.init(mc, width, height);
        float far = ClientHooks.getGuiFarPlane();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, (float) (window.getWidth() / window.getGuiScale()),
                (float) (window.getHeight() / window.getGuiScale()), 0, 1000, far), VertexSorting.ORTHOGRAPHIC_Z);
        var modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.translation(0, 0, 10000F - far);
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        var graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
        screen.renderWithTooltip(graphics, -1000, -1000, 0);
        graphics.flush();
        modelView.popMatrix();
        RenderSystem.applyModelViewMatrix();
        String label = System.getProperty("hemomancy.screenChromeReviewLabel", "after");
        Screenshot.grab(mc.gameDirectory, "chrome-" + label + "-" + name + ".png", target, message -> {});
        shots++;
        Hemomancy.LOGGER.info("SCREEN_CHROME_REVIEW captured {}", name);
    }

    private static void checkSynapticNavigation(Screen screen) {
        EditBox name = screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast)
                .findFirst().orElseThrow();
        Button save = synapticButton(screen, "overwrite");
        Button apply = synapticButton(screen, "apply");
        Button empty = synapticButton(screen, "numbered_pattern", 3);
        Button locked = synapticButton(screen, "numbered_pattern", 7);
        if (!name.getValue().equals("Long Descent") || !apply.active || locked.active) {
            throw new IllegalStateException("Expected the Distributor's saved slot and a locked seventh socket");
        }
        clickWidget(screen, locked);
        clickWidget(screen, save);
        if (!name.getValue().equals("Long Descent") || !save.getMessage().equals(synapticLabel("confirm"))) {
            throw new IllegalStateException("Locked slot accepted input or overwrite skipped confirmation");
        }
        clickWidget(screen, empty);
        if (!name.getValue().equals("Loadout 3") || apply.active || !save.getMessage().equals(synapticLabel("save"))) {
            throw new IllegalStateException("Selecting an empty socket did not clear the overwrite prompt");
        }
        clickWidget(screen, name);
        screen.keyPressed(GLFW.GLFW_KEY_LEFT, 0, 0);
        if (!name.getValue().equals("Loadout 3") || apply.active) {
            throw new IllegalStateException("The name field did not keep the cursor key");
        }
        clickWidget(screen, synapticButton(screen, "numbered_pattern", 2));
        if (!name.getValue().equals("Long Descent") || !apply.active) {
            throw new IllegalStateException("The saved slot did not restore its preview and controls");
        }
        Hemomancy.LOGGER.info("SCREEN_CHROME_REVIEW checked synaptic slot selection and overwrite confirmation");
    }

    private static Button synapticButton(Screen screen, String key, Object... args) {
        Component label = synapticLabel(key, args);
        return screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getMessage().equals(label)).findFirst().orElseThrow();
    }

    private static Component synapticLabel(String key, Object... args) {
        return Component.translatable("screen.hemomancy.synaptic_loadout." + key, args);
    }

    private static void clickWidget(Screen screen, AbstractWidget widget) {
        screen.mouseClicked(widget.getX() + widget.getWidth() / 2.0, widget.getY() + widget.getHeight() / 2.0,
                GLFW.GLFW_MOUSE_BUTTON_LEFT);
    }

    private static Scene stockedSynapticLoadouts() {
        return Scene.server("synaptic_loadout_stocked", "tile.functional.SynapticLoadoutScreen", player -> {
            BlockPos pos = anchor.relative(Direction.SOUTH, 2);
            player.serverLevel().setBlockAndUpdate(pos, BlockInit.dendritic_distributor.get().defaultBlockState());
            if (player.serverLevel().getBlockEntity(pos) instanceof DendriticDistributorBlockEntity distributor) {
                distributor.setSelectedSlot(1);
            }

            var known = HemoCapabilityAccess.getKnownManipulations(player);
            String heart = MemorySlotRef.thelemicMemory(ThelemicMemory.SECOND_PULSE).storageKey();
            String legs = MemorySlotRef.thelemicMemory(ThelemicMemory.COURSING_LEGS).storageKey();
            known.setEquippedManipNames(List.of("blood_shot", "blood_rush", "hemorrhage", heart));
            known.setSelectedMemoryRef(MemorySlotRef.manipulation("blood_shot"));
            known.setLoadout(1, ManipulationLoadout.of("Long Descent", legs,
                    List.of("sanguine_mending", "glacial_grasp", legs, "deadly_gaze", "hematic_flare"), 1));
            known.setLoadout(3, ManipulationLoadout.of("Close Quarters", "hemorrhage", List.of("hemorrhage", heart), 3));
            KnownManipulationEvents.syncPlayerEvent(player);

            var skills = HemoCapabilityAccess.getSkillProgress(player);
            skills.setSkill(SkillPointInit.skill_synaptic_memory, EnumSkillStates.UNLOCKED, 2);
            PacketHandler.sendToPlayer(player, new PacketSyncSkills(skills.toSyncTag()));
        }, player -> {
            BlockPos pos = anchor.relative(Direction.SOUTH, 2);
            Minecraft.getInstance().execute(() -> SynapticLoadoutScreen.openScreen(pos));
        });
    }

    /** Fills the drawers and tincture rack, with both memory types carried and one free socket. */
    private static Scene stockedReliquary() {
        Scene plain = block("mnemonic_reliquary_stocked", "tile.functional.MnemonicReliquaryScreen", BlockInit.mnemonic_reliquary);
        return new Scene(plain.name(), plain.screen(), null, player -> {
            for (var manipulation : ManipulationInit.MANIPS_TYPE_REGISTRY)
                com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper
                        .grantCreativeMemory(player, manipulation, null);
            var known = HemoCapabilityAccess.getKnownManipulations(player);
            var thelemic = HemoCapabilityAccess.thelemicMemory(player);
            for (ThelemicMemory memory : ThelemicMemory.values())
                thelemic.learnAndAddReserve(memory, ThelemicMemoryPrimingRules.TICKS_PER_DOSE);
            known.setEquippedManipNames(List.of(ManipulationInit.blood_shot.get().getName(),
                    MemorySlotRef.thelemicMemory(ThelemicMemory.SECOND_PULSE).storageKey()));
            ThelemicMemoryEvents.sync(player);
            com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents.syncPlayerEvent(player);
            plain.prepare().accept(player);
        }, plain.server());
    }

    private static Scene block(String name, String screenClass, Supplier<? extends Block> block) {
        BlockPos[] pos = new BlockPos[1];
        return Scene.server(name, screenClass, player -> {
            var level = player.serverLevel();
            pos[0] = anchor.relative(Direction.SOUTH, 2);
            level.setBlockAndUpdate(pos[0].below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pos[0], block.get().defaultBlockState());
        }, player -> {
            var level = player.serverLevel();
            var hit = new BlockHitResult(Vec3.atCenterOf(pos[0]), Direction.NORTH, pos[0], false);
            level.getBlockState(pos[0]).useWithoutItem(level, player, hit);
            if (player.containerMenu == player.inventoryMenu && level.getBlockEntity(pos[0]) instanceof MenuProvider provider)
                player.openMenu(provider, pos[0]);
        });
    }

    /** An Eightfold Scriptorium with stocked jars, a sword on the book and three doses metered into the feed. */
    private static void placeStockedScriptorium(ServerPlayer player) {
        var level = player.serverLevel();
        BlockPos pos = anchor.relative(Direction.SOUTH, 2);
        level.setBlockAndUpdate(pos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos, BlockInit.enzymatic_scriptorium.get().defaultBlockState()
                .setValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock.STAGE, 1));
        if (level.getBlockEntity(pos) instanceof com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity station) {
            var tempers = com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper.values();
            int[] stock = {64, 40, 12, 0, 30, 8, 52, 20};
            for (int i = 0; i < 8; i++)
                if (stock[i] > 0)
                    station.setItem(i, new ItemStack(com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament
                            .EnumBloodTemper.getRepEnzyme(tempers[i]), stock[i]));
            station.setItem(com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity.ITEM,
                    new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
            station.setItem(com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity.LAPIS,
                    new ItemStack(net.minecraft.world.item.Items.LAPIS_LAZULI, 9));
            station.select(0);
            station.select(0);
            station.select(6);
            station.getBloodCapability().setBloodVolume(1200);
            com.vincenthuto.hemomancy.common.util.BlockEntitySync.markAndSync(station);
        }
    }

    /** A Resonant Forge at the given stage with a pickaxe on the anvil, an enchanted sword at the rest and blood in the trough. */
    private static void placeLoadedForge(ServerPlayer player, int stage) {
        var level = player.serverLevel();
        BlockPos pos = anchor.relative(Direction.SOUTH, 2);
        level.setBlockAndUpdate(pos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        level.removeBlock(pos, false);
        level.setBlockAndUpdate(pos, BlockInit.resonant_forge.get().defaultBlockState()
                .setValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.StationTierProperty.STAGE, stage));
        if (level.getBlockEntity(pos) instanceof com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity forge) {
            forge.setItem(com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity.APPLICATION_ITEM,
                    new ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE));
            ItemStack sword = new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
            level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .getHolder(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS)
                    .ifPresent(sharpness -> sword.enchant(sharpness, 3));
            forge.setItem(com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity.GRINDING_ITEM, sword);
            forge.setItem(com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity.GRINDING_CYLINDER,
                    new ItemStack(ItemInit.wax_cylinder.get()));
            forge.getBloodCapability().setBloodVolume(600 + 300 * stage);
            com.vincenthuto.hemomancy.common.util.BlockEntitySync.markAndSync(forge);
        }
    }

    private static void openStockedScriptorium(ServerPlayer player) {
        BlockPos pos = anchor.relative(Direction.SOUTH, 2);
        if (player.serverLevel().getBlockEntity(pos) instanceof MenuProvider provider)
            player.openMenu(provider, pos);
    }

    /** RV-60: a Specimen Vat south of the reviewer keeping a cow at 64 Vigor over a 345 mL buffer. */
    private static void placeVessel(ServerPlayer player) {
        var level = player.serverLevel();
        BlockPos base = anchor.relative(Direction.SOUTH, 2);
        level.setBlockAndUpdate(base.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        var lower = BlockInit.specimen_vat.get().defaultBlockState()
                .setValue(com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenVatBlock.FACING, Direction.NORTH);
        level.setBlock(base, lower, 3);
        level.setBlock(base.above(), lower.setValue(com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenVatBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 3);
        if (level.getBlockEntity(base) instanceof com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVatBlockEntity vat) {
            vat.buffer().setOwner(player.getUUID());
            var cow = net.minecraft.world.entity.EntityType.COW.create(level);
            if (cow != null) {
                vat.kept().keep(com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData.captureEntity(cow),
                        cow.getMaxHealth(), 64.0D, level.getGameTime());
                cow.discard();
            }
            vat.buffer().setBloodVolume(345.0D);
            com.vincenthuto.hemomancy.common.util.BlockEntitySync.markAndSync(vat);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    private static void openVessel(ServerPlayer player) {
        var level = player.serverLevel();
        BlockPos base = anchor.relative(Direction.SOUTH, 2);
        if (level.getBlockEntity(base) instanceof com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVatBlockEntity vat)
            com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessels.useEmptyHand(level, base, vat, player, vat::fits);
    }

    /** Puts the item in hand, then uses it once the client holds it too (some menus read the held stack). */
    private static Scene item(String name, String screenClass, Supplier<? extends net.minecraft.world.item.Item> item) {
        return Scene.server(name, screenClass,
                player -> player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item.get())),
                player -> player.getMainHandItem().use(player.serverLevel(), player, InteractionHand.MAIN_HAND));
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Screen> screenClass(String name) {
        try {
            return (Class<? extends Screen>) Class.forName("com.vincenthuto.hemomancy.client.screen." + name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
