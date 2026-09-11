package com.vincenthuto.hemomancy.gametest;

import com.google.gson.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueScreen;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialoguePresentation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Opt-in client operator for disposable gameplay worlds. Actions use client interaction paths.
 * Commands and fixture assists are logged, never silently counted as earned progression.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class GameplayCampaignDriver {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean started;
    private static int ticks;
    private static int releaseAt;
    private static final List<KeyMapping> held = new ArrayList<>();
    private static Path root;

    @SubscribeEvent public static void input(ClientTickEvent.Pre event) {
        if (!Boolean.getBoolean("hemomancy.gameplayDriver") || held.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        for (var key : held) key.setDown(true);
        // Virtual captured-pointer state permits Minecraft's own handleKeybinds /
        // continueAttack path in a background test window without moving the OS cursor.
        if (held.contains(mc.options.keyAttack)) try {
            var field = net.minecraft.client.MouseHandler.class.getDeclaredField("mouseGrabbed");
            field.setAccessible(true); field.setBoolean(mc.mouseHandler, true);
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException("Virtual mouse input unavailable", ex); }
    }

    @SubscribeEvent public static void chat(ClientChatReceivedEvent event) {
        if (Boolean.getBoolean("hemomancy.gameplayDriver") && root != null)
            log("message", event.getMessage().getString());
    }

    @SubscribeEvent public static void damage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.gameplayDriver") || root == null || event.getEntity().level().isClientSide) return;
        var entity = event.getEntity();
        JsonObject sample = new JsonObject();
        sample.addProperty("serverTick", entity.level().getGameTime());
        sample.addProperty("target", entity.getType().toString());
        sample.addProperty("targetId", entity.getId());
        sample.addProperty("health", entity.getHealth());
        sample.addProperty("damage", event.getNewDamage());
        sample.addProperty("source", event.getSource().getMsgId());
        var attacker = event.getSource().getEntity();
        if (attacker != null) sample.addProperty("attacker", attacker.getType()+" "+attacker.getId());
        log("damage", sample.toString());
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.gameplayDriver")) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (root == null) {
                root = mc.gameDirectory.toPath().resolve("operator");
                Files.createDirectories(root.resolve("inbox"));
                Files.createDirectories(root.resolve("done"));
            }
            ticks++;
            if (releaseAt > 0 && ticks >= releaseAt) release();
            if (!started && mc.screen instanceof TitleScreen) {
                started = true;
                mc.options.pauseOnLostFocus = false;
                log("startup", "Native client driver ready; world creation requires explicit operator action");
            }
            if (ticks % 5 == 0) {
                try (var stream = Files.list(root.resolve("inbox"))) {
                    for (Path path : stream.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                        JsonObject command = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                        try { execute(mc, command); log("action", command.toString()); }
                        catch (Exception ex) { log("action_error", command + " :: " + ex); }
                        Files.move(path, root.resolve("done").resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            if (ticks % 20 == 0) snapshot(mc);
        } catch (Exception ex) { Hemomancy.LOGGER.error("GAMEPLAY_DRIVER", ex); }
    }

    private static void execute(Minecraft mc, JsonObject c) throws Exception {
        String op = c.get("op").getAsString();
        switch (op) {
            case "create" -> {
                String name = c.get("name").getAsString();
                if (!name.matches("[a-zA-Z0-9_-]+")) throw new IllegalArgumentException("Unsafe world name");
                if (Files.exists(mc.gameDirectory.toPath().resolve("saves").resolve(name)))
                    throw new IllegalStateException("World already exists; use open");
                mc.createWorldOpenFlows().createFreshLevel(name,
                    new LevelSettings(name, GameType.SURVIVAL, false, Difficulty.NORMAL, true,
                        new GameRules(), WorldDataConfiguration.DEFAULT),
                    new WorldOptions(c.get("seed").getAsLong(), true, false),
                    WorldPresets::createNormalWorldDimensions, mc.screen);
            }
            case "open" -> mc.createWorldOpenFlows().openWorld(c.get("name").getAsString(), () -> {});
            case "capture" -> Screenshot.grab(mc.gameDirectory, safe(c.get("name").getAsString()) + ".png",
                    mc.getMainRenderTarget(), message -> log("capture", message.getString()));
            case "hover" -> {
                if (mc.screen == null) throw new IllegalStateException("No screen");
                double x = c.get("x").getAsDouble(), y = c.get("y").getAsDouble();
                var mouseX = net.minecraft.client.MouseHandler.class.getDeclaredField("xpos");
                var mouseY = net.minecraft.client.MouseHandler.class.getDeclaredField("ypos");
                mouseX.setAccessible(true); mouseY.setAccessible(true);
                mouseX.setDouble(mc.mouseHandler, x * mc.getWindow().getScreenWidth() / mc.screen.width);
                mouseY.setDouble(mc.mouseHandler, y * mc.getWindow().getScreenHeight() / mc.screen.height);
                mc.screen.mouseMoved(x, y);
            }
            case "click" -> {
                if (mc.screen == null) throw new IllegalStateException("No screen");
                double x = c.get("x").getAsDouble(), y = c.get("y").getAsDouble();
                int button = c.has("button") ? c.get("button").getAsInt() : 0;
                mc.screen.mouseClicked(x, y, button);
                if (mc.screen != null) mc.screen.mouseReleased(x, y, button);
            }
            case "key" -> {
                if (mc.screen == null) throw new IllegalStateException("No screen");
                mc.screen.keyPressed(c.get("key").getAsInt(), 0, c.has("mods") ? c.get("mods").getAsInt() : 0);
            }
            case "scroll" -> {
                if (mc.screen == null) throw new IllegalStateException("No screen");
                mc.screen.mouseScrolled(c.get("x").getAsDouble(), c.get("y").getAsDouble(), 0, c.get("amount").getAsDouble());
            }
            case "inventory" -> mc.setScreen(new InventoryScreen(mc.player));
            case "vascularView" -> com.vincenthuto.hemomancy.common.network.PacketHandler.sendToServer(
                    new com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.PacketOpenVascularView());
            case "dialogueFocused" -> {
                if (!(mc.screen instanceof DialogueScreen)) throw new IllegalStateException("No dialogue open");
                var field = DialogueScreen.class.getDeclaredField("tree");
                field.setAccessible(true);
                DialogueTree tree = (DialogueTree) field.get(mc.screen);
                log("assist", "Bypass broken topic navigation with original focused dialogue; choices still send normal packets");
                DialogueScreen.open(new DialogueTree(tree.speakerName(), tree.speakerIcon(),
                        tree.startNodeId(), tree.nodes(), tree.entityId(), tree.theme(), DialoguePresentation.focused()));
            }
            case "rebind" -> {
                KeyMapping key = Arrays.stream(mc.options.keyMappings)
                        .filter(k -> k.getName().equals(c.get("binding").getAsString())).findFirst().orElseThrow();
                key.setKey(com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(c.get("key").getAsInt()));
                KeyMapping.resetMapping(); mc.options.save();
                log("assist", "Explicit control rebind to isolate a reproduced default-key conflict");
            }
            case "close" -> { if (mc.screen != null) mc.screen.onClose(); }
            case "select" -> mc.player.getInventory().selected = c.get("slot").getAsInt();
            case "look" -> {
                mc.player.setYRot(c.get("yaw").getAsFloat()); mc.player.setXRot(c.get("pitch").getAsFloat());
            }
            case "aim" -> {
                Vec3 delta = new Vec3(c.get("x").getAsDouble(), c.get("y").getAsDouble(), c.get("z").getAsDouble()).subtract(mc.player.getEyePosition());
                mc.player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x, delta.z)));
                mc.player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x*delta.x+delta.z*delta.z))));
            }
            case "hold" -> {
                release();
                if (mc.screen == null) {
                    var miss = Minecraft.class.getDeclaredField("missTime");
                    miss.setAccessible(true);
                    if (miss.getInt(mc) > 10) miss.setInt(mc, 0);
                }
                for (var entry : c.getAsJsonArray("keys")) {
                    String name = entry.getAsString();
                    KeyMapping key = switch(name) {
                        case "forward" -> mc.options.keyUp;
                        case "back" -> mc.options.keyDown;
                        case "left" -> mc.options.keyLeft;
                        case "right" -> mc.options.keyRight;
                        case "jump" -> mc.options.keyJump;
                        case "sneak" -> mc.options.keyShift;
                        case "sprint" -> mc.options.keySprint;
                        case "use" -> mc.options.keyUse;
                        case "attack" -> mc.options.keyAttack;
                        default -> Arrays.stream(mc.options.keyMappings).filter(k -> k.getName().equals(name)).findFirst().orElseThrow();
                    };
                    key.setDown(true); held.add(key);
                    KeyMapping.click(key.getKey());
                }
                releaseAt = ticks + c.get("ticks").getAsInt();
            }
            case "release" -> release();
            case "use" -> {
                if (mc.hitResult instanceof EntityHitResult hit) {
                    var result = mc.gameMode.interactAt(mc.player, hit.getEntity(), hit, InteractionHand.MAIN_HAND);
                    if (!result.consumesAction()) mc.gameMode.interact(mc.player, hit.getEntity(), InteractionHand.MAIN_HAND);
                } else if (mc.hitResult instanceof BlockHitResult hit && hit.getType() != HitResult.Type.MISS) {
                    var result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                    if (!result.consumesAction()) mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                } else mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
            case "attack" -> {
                if (mc.hitResult instanceof EntityHitResult hit) mc.gameMode.attack(mc.player, hit.getEntity());
                else if (mc.hitResult instanceof BlockHitResult hit) mc.gameMode.startDestroyBlock(hit.getBlockPos(), hit.getDirection());
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
            case "slot" -> mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId,
                    c.get("slot").getAsInt(), c.has("button") ? c.get("button").getAsInt() : 0,
                    c.has("type") ? ClickType.valueOf(c.get("type").getAsString()) : ClickType.PICKUP, mc.player);
            case "command" -> mc.player.connection.sendCommand(c.get("value").getAsString());
            case "disconnect" -> { release(); mc.level.disconnect(); mc.disconnect(); mc.setScreen(new TitleScreen()); }
            case "stop" -> { release(); mc.stop(); }
            default -> throw new IllegalArgumentException("Unknown operator action " + op);
        }
    }

    private static void snapshot(Minecraft mc) throws Exception {
        JsonObject state = new JsonObject();
        state.addProperty("tick", ticks);
        state.addProperty("screen", mc.screen == null ? "world" : mc.screen.getClass().getSimpleName());
        state.addProperty("guiWidth", mc.getWindow().getGuiScaledWidth()); state.addProperty("guiHeight", mc.getWindow().getGuiScaledHeight());
        JsonArray widgets = new JsonArray();
        if (mc.screen != null) for (var child : mc.screen.children()) if (child instanceof AbstractWidget widget) {
            JsonObject w = new JsonObject(); w.addProperty("label", widget.getMessage().getString());
            w.addProperty("x", widget.getX()); w.addProperty("y", widget.getY());
            w.addProperty("width", widget.getWidth()); w.addProperty("height", widget.getHeight()); w.addProperty("active", widget.active); widgets.add(w);
        }
        state.add("widgets", widgets);
        if (mc.player != null && mc.level != null) {
            state.addProperty("position", mc.player.position().toString());
            state.addProperty("yaw", mc.player.getYRot()); state.addProperty("pitch", mc.player.getXRot());
            state.addProperty("health", mc.player.getHealth()); state.addProperty("food", mc.player.getFoodData().getFoodLevel());
            state.addProperty("dimension", mc.level.dimension().location().toString());
            state.addProperty("selected", mc.player.getInventory().selected);
            state.addProperty("hit", String.valueOf(mc.hitResult));
            if (mc.hitResult instanceof BlockHitResult hit) state.addProperty("hitBlock", mc.level.getBlockState(hit.getBlockPos()).toString()+" "+hit.getBlockPos());
            if (mc.hitResult instanceof EntityHitResult hit) state.addProperty("hitEntity", hit.getEntity().getType().toString()+" "+hit.getEntity().getId());
            JsonArray inventory = new JsonArray();
            for (int i=0;i<mc.player.getInventory().getContainerSize();i++) if (!mc.player.getInventory().getItem(i).isEmpty()) inventory.add(i+": "+mc.player.getInventory().getItem(i));
            state.add("inventory",inventory);
            JsonArray slots = new JsonArray();
            for (var slot : mc.player.containerMenu.slots) slots.add(slot.index+" @"+slot.x+","+slot.y+": "+slot.getItem());
            state.add("slots", slots);
            JsonArray entities = new JsonArray();
            for (var e : mc.level.entitiesForRendering()) if(e.distanceTo(mc.player)<48 && e!=mc.player) entities.add(e.getId()+" "+e.getType()+" "+e.position()+" "+e.getName().getString());
            state.add("nearby",entities);
            JsonArray living = new JsonArray();
            for (var entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof net.minecraft.world.entity.LivingEntity e) || e.distanceTo(mc.player) >= 48) continue;
                JsonObject entry = new JsonObject();
                entry.addProperty("id", e.getId()); entry.addProperty("type", e.getType().toString());
                entry.addProperty("x", e.getX()); entry.addProperty("y", e.getY()); entry.addProperty("z", e.getZ());
                entry.addProperty("health", e.getHealth()); entry.addProperty("maxHealth", e.getMaxHealth());
                entry.addProperty("armor", e.getArmorValue()); entry.addProperty("visible", mc.player.hasLineOfSight(e));
                living.add(entry);
            }
            state.add("living", living);
            if (mc.getSingleplayerServer()!=null) {
                UUID uuid=mc.player.getUUID();
                mc.getSingleplayerServer().execute(() -> {
                    var player=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid); if(player==null)return;
                    JsonObject server=new JsonObject();
                    server.addProperty("tick",player.serverLevel().getGameTime());
                    server.addProperty("degree",HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber());
                    var degree = HemoCapabilityAccess.requireInitiatoryDegree(player);
                    server.addProperty("archonPath", degree.getArchonPath().name());
                    server.addProperty("pomes", degree.getTotalPomesConsumed());
                    server.addProperty("communion", degree.isQliphothCommunionDone());
                    HemoCapabilityAccess.getBloodVolume(player).ifPresent(v -> {server.addProperty("blood",v.getBloodVolume());server.addProperty("capacity",v.getMaxBloodVolume());});
                    server.addProperty("persistent",player.getPersistentData().toString());
                    var rite = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(player.serverLevel()).getRite(uuid);
                    if (rite != null) {
                        JsonObject r = new JsonObject();
                        r.addProperty("recipe", rite.getRecipeId().toString()); r.addProperty("phase", rite.getPhase().name());
                        r.addProperty("phaseTicks", rite.getPhaseTicks()); r.addProperty("wave", rite.getCurrentWave());
                        r.addProperty("instability", rite.getInstability()); r.addProperty("reservoir", rite.getReservoirBloodMl());
                        r.add("anchors", JSON.toJsonTree(rite.getAnchorBloodMl()));
                        r.add("broken", JSON.toJsonTree(rite.getBrokenInstabilityAnchors()));
                        r.add("sigils", JSON.toJsonTree(rite.getSigilProgress()));
                        r.add("deck", JSON.toJsonTree(rite.getWaveDeck()));
                        server.add("rite", r);
                    }
                    try { write("server-state.json",server); } catch(Exception ex){log("state_error",ex.toString());}
                });
            }
        }
        write("state.json",state);
    }

    private static void release() { for(var key:held)key.setDown(false);held.clear();releaseAt=0; }
    private static String safe(String name) { return name.replaceAll("[^a-zA-Z0-9_-]","_"); }
    private static synchronized void write(String name,JsonObject value) throws Exception {
        Path temp=root.resolve(name+".tmp");Files.writeString(temp,JSON.toJson(value),StandardCharsets.UTF_8);
        Files.move(temp,root.resolve(name),StandardCopyOption.REPLACE_EXISTING);
    }
    private static synchronized void log(String kind,String message) {
        try {JsonObject entry=new JsonObject();entry.addProperty("time",java.time.Instant.now().toString());entry.addProperty("tick",ticks);entry.addProperty("kind",kind);entry.addProperty("message",message);
            Files.writeString(root.resolve("journal.jsonl"),entry.toString()+"\n",StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        } catch(Exception ex){Hemomancy.LOGGER.error("GAMEPLAY_JOURNAL",ex);}
    }
}
