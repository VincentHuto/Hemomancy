package com.vincenthuto.hemomancy.gametest.playthrough;


import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueRewardClaims;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopic;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.SanguineMonolithDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRequestLimiter;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ChangeSelectedManipPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UseManipKeyPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueRewardChoicePacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueTopicOpenedPacket;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeRequestPacket;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeResultPacket;
import com.vincenthuto.hemomancy.common.capability.PendingDelivery;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketKickBloodlinePlayer;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction;
import com.vincenthuto.hemomancy.common.network.dialogue.ClosePendingWhisperPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenPendingWhisperPacket;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;
import com.vincenthuto.hemomancy.common.network.summon.PacketCrossbarRadialAction;
import com.vincenthuto.hemomancy.common.network.summon.PacketPuppeteersSpindleAction;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import com.vincenthuto.hemomancy.gametest.CapturedPlayer;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyFixtures;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneySnapshot;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/**
 * A player who plays: every action enters the server where the matching client action enters it, and records an
 * {@link Outcome} (also kept as {@link #last()}). One wrapper lives through deaths and relogs; each incarnation is a
 * fresh {@link CapturedPlayer} with the same profile, so the UUID, the advancements and the transcript carry over.
 * <p>
 * Every {@link BlockPos} this harness takes or returns is absolute: convert test-relative positions with
 * {@code helper.absolutePos}. The player never joins the level, so the level does not tick them:
 * {@link #tickPlayer(int)} posts the player tick events that {@code EarlyInitiation}, {@code PendingDelivery} and
 * {@code ChamberVisitService} listen for. The player, its fixture blocks and its journey snapshot are cleaned up when
 * the test ends.
 */
public final class PlaythroughPlayer {
	private static final String JOURNEY_PREFIX = "hemomancy.dev_test.journey.";
	private static final Method UPDATING_USING_ITEM = method(LivingEntity.class, "updatingUsingItem");
	private static final Field SPAWN_INVULNERABLE = field(ServerPlayer.class, "spawnInvulnerableTime");
	private static final Field ATTACK_STRENGTH = field(LivingEntity.class, "attackStrengthTicker");

	private final GameTestHelper helper;
	private final GameProfile profile;
	private final BlockPos home;
	private CapturedPlayer captured;
	/** Everything said to the player, across incarnations. */
	final List<Outcome.ChatLine> chatLog = new ArrayList<>();
	/** Every custom payload sent to the player, across incarnations. */
	final List<CustomPacketPayload> payloadLog = new ArrayList<>();
	private final Map<Integer, DialogueView> views = new HashMap<>();
	private final Map<BlockPos, DialogueView> blockViews = new HashMap<>();
	private boolean joined;
	/** Also in the level's entity storage ({@link #enterWorld()}). */
	private boolean inWorld;
	private final List<BlockPos> placed = new ArrayList<>();
	private Outcome last;
	private long nextClaimRequest;
	private Outcome.Probe holding;

	private PlaythroughPlayer(GameTestHelper helper, GameProfile profile, BlockPos home) {
		this.helper = helper;
		this.profile = profile;
		this.home = home.immutable();
	}

	/**
	 * A brand-new survival player standing at {@code standAt} (absolute): an empty inventory, no degree, inactive
	 * blood, and the journey baseline ({@code HemoJourneySnapshot.capture} then {@code resetForJourney}) so nothing
	 * left by another test carries in.
	 */
	public static PlaythroughPlayer create(GameTestHelper helper, BlockPos standAt) {
		DialogueEventProbe.listen();
		String id = UUID.randomUUID().toString().replace("-", "");
		GameProfile profile = new GameProfile(UUID.randomUUID(), "pt_" + id.substring(0, 10));
		PlaythroughPlayer player = new PlaythroughPlayer(helper, profile, standAt);
		player.incarnate(CapturedPlayer.create(helper, profile));
		PlaythroughCleanup.onEnd(helper, player::close);
		PlaythroughCleanup.worldOwnedBy(helper, player);
		// A craft left scheduled by a test that failed or timed out must not clear blocks in a later test here.
		PlaythroughCleanup.onEnd(helper, () -> PendingBloodCraftManager.cancelWithin(helper.getLevel(), helper.getBounds()));
		ServerPlayer server = player.player();
		server.moveTo(standAt.getX() + 0.5D, standAt.getY(), standAt.getZ() + 0.5D, 0.0F, 0.0F);
		server.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
				server.level().dimension().location().toString());
		var captured = HemoJourneySnapshot.capture(server);
		helper.assertTrue(captured.passed(), "journey snapshot: " + captured.message());
		var reset = HemoJourneySnapshot.resetForJourney(server);
		helper.assertTrue(reset.passed(), "journey reset: " + reset.message());
		server.moveTo(standAt.getX() + 0.5D, standAt.getY(), standAt.getZ() + 0.5D, 0.0F, 0.0F);
		server.setGameMode(GameType.SURVIVAL);
		server.getInventory().clearContent();
		server.setHealth(server.getMaxHealth());
		return player;
	}

	private void incarnate(CapturedPlayer next) {
		captured = next.online();
		ServerPlayer player = next.player;
		ServerGamePacketListenerImpl original = player.connection;
		// A second listener over the same connection: it keeps the components (for translation keys), then hands the
		// packet to the captured player's own listener, which keeps its text and payload lists as before.
		new ServerGamePacketListenerImpl(player.server, original.getConnection(), player,
				CommonListenerCookie.createInitial(profile, false)) {
			@Override
			public void send(Packet<?> packet) {
				record(packet);
				original.send(packet);
			}

			@Override
			public void send(Packet<?> packet, @Nullable PacketSendListener listener) {
				record(packet);
				original.send(packet, listener);
			}
		};
		set(SPAWN_INVULNERABLE, player, 0);
	}

	private void record(Packet<?> packet) {
		if (packet instanceof ClientboundCustomPayloadPacket custom) payloadLog.add(custom.payload());
		if (packet instanceof ClientboundSystemChatPacket text) chatLog.add(Outcome.ChatLine.of(text.content(), text.overlay()));
	}

	// --- Identity --------------------------------------------------------------------------------------------------

	public GameTestHelper helper() {
		return helper;
	}

	/** The current incarnation. Re-read it after {@link #respawn()}, {@link #endReturn()} or {@link #relog()}. */
	public ServerPlayer player() {
		return captured.player;
	}

	public CapturedPlayer captured() {
		return captured;
	}

	public UUID uuid() {
		return profile.getId();
	}

	public GameProfile profile() {
		return profile;
	}

	/** Where the player was made, and where they come back to after a respawn. */
	public BlockPos home() {
		return home;
	}

	/** The last outcome any action recorded. */
	public Outcome last() {
		return last;
	}

	void recordOutcome(Outcome outcome) {
		last = outcome;
	}

	// --- Moving (not under test) -----------------------------------------------------------------------------------

	/** ARRANGE: stands the player on {@code pos} (absolute), as if they had walked there. */
	public PlaythroughPlayer moveTo(BlockPos pos) {
		player().moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, player().getYRot(), player().getXRot());
		return this;
	}

	public PlaythroughPlayer moveTo(Vec3 pos) {
		player().moveTo(pos.x, pos.y, pos.z, player().getYRot(), player().getXRot());
		return this;
	}

	/** ARRANGE: walks up to {@code entity}, two blocks short, facing it. */
	public PlaythroughPlayer approach(Entity entity) {
		Vec3 from = entity.position();
		Vec3 offset = player().position().subtract(from).multiply(1, 0, 1);
		Vec3 direction = offset.lengthSqr() < 1.0E-4D ? new Vec3(1, 0, 0) : offset.normalize();
		moveTo(from.add(direction.scale(2.0D)));
		aim(entity.getEyePosition());
		return this;
	}

	/** Turns the player's head toward {@code point}. */
	public PlaythroughPlayer aim(Vec3 point) {
		player().lookAt(EntityAnchorArgument.Anchor.EYES, point);
		return this;
	}

	// --- Talking ---------------------------------------------------------------------------------------------------

	/** Right-clicks {@code npc} (its {@code mobInteract}, side effects and all) and reads the dialogue it served. */
	public DialogueView talkTo(Entity npc) {
		Optional<DialogueView> view = tryTalkTo(npc);
		helper.assertTrue(view.isPresent(), describe(npc) + " opens a dialogue when right-clicked; " + chatTail());
		return view.get();
	}

	/** Right-clicks {@code npc}; empty when it opened nothing. */
	public Optional<DialogueView> tryTalkTo(Entity npc) {
		Optional<DialogueView> view = Dialogue.tryOpen(captured, npc).map(DialogueView::new);
		view.ifPresent(v -> views.put(npc.getId(), v));
		return view;
	}

	/** The dialogue {@code npc} last served this player. */
	public Optional<DialogueView> lastView(Entity npc) {
		return Optional.ofNullable(views.get(npc.getId()));
	}

	/** The events the player can choose after right-clicking {@code npc} now. */
	public List<String> offeredEvents(Entity npc) {
		return talkTo(npc).reachableEvents();
	}

	/**
	 * Chooses an option the player was shown: {@code eventId} must be reachable and enabled in the tree {@code npc}
	 * last served (talk to them first). Sends what {@code DialogueScreen#selectOption} sends: the acknowledged claim
	 * payload for a reward that needs one, the plain option payload otherwise.
	 */
	public Outcome choose(Entity npc, String eventId) {
		DialogueView view = views.get(npc.getId());
		helper.assertTrue(view != null, "talk to " + describe(npc) + " before choosing " + eventId);
		helper.assertTrue(view.offers(eventId), describe(npc) + " offers " + eventId + "; it showed " + view.describe());
		return send(eventId, npc.getId(), npc, DialogueRewardClaims.requiresAcknowledgement(eventId), "choose " + eventId);
	}

	/** Sends {@code eventId} as the screen would, without checking it was offered. Negative tests only. */
	public Outcome forge(String eventId, int entityId) {
		return send(eventId, entityId, entity(entityId), DialogueRewardClaims.requiresAcknowledgement(eventId),
				"forge " + eventId + " @" + entityId);
	}

	/** Always the plain option payload, whatever the event. Negative tests only. */
	public Outcome forgeOption(String eventId, int entityId) {
		return send(eventId, entityId, entity(entityId), false, "forge option " + eventId + " @" + entityId);
	}

	/** Always the acknowledged claim payload, whatever the event. Negative tests only. */
	public Outcome forgeClaim(String eventId, int entityId) {
		return send(eventId, entityId, entity(entityId), true, "forge claim " + eventId + " @" + entityId);
	}

	private Outcome send(String eventId, int entityId, @Nullable Entity npc, boolean acknowledged, String label) {
		Outcome.Probe probe = Outcome.begin(this, npc, label);
		if (acknowledged) {
			DialogueRewardChoicePacket.handle(new DialogueRewardChoicePacket(eventId, entityId, ++nextClaimRequest),
					PayloadContexts.of(player()));
		} else {
			DialogueOptionPacket.handle(new DialogueOptionPacket(eventId, entityId), PayloadContexts.of(player()));
		}
		return probe.finish();
	}

	/**
	 * Opens a hub card the way the screen does: for an unread lore card it sends the read record
	 * ({@code DialogueTopicOpenedPacket}), which is what clears the card's unread sigil.
	 */
	public Outcome openTopic(Entity npc, String topicId) {
		DialogueView view = views.get(npc.getId());
		helper.assertTrue(view != null, "talk to " + describe(npc) + " before opening " + topicId);
		Optional<DialogueTopic> topic = view.topicById(topicId);
		helper.assertTrue(topic.isPresent(), describe(npc) + " shows card " + topicId + "; it showed " + view.describe());
		ResourceLocation dialogueId = view.tree().presentation().dialogueId();
		ResourceLocation readId = ResourceLocation.fromNamespaceAndPath(dialogueId.getNamespace(),
				dialogueId.getPath() + "/" + topic.get().readPath());
		Outcome.Probe probe = Outcome.begin(this, npc, "open topic " + topicId);
		DialogueTopicOpenedPacket.handle(new DialogueTopicOpenedPacket(readId, npc.getId()), PayloadContexts.of(player()));
		return probe.finish();
	}

	/**
	 * Asks for one trade card through the request payload (past the request rate limit, as a patient player would
	 * be), paying from {@code slots}. Returns the reply, or null when the payload's guards dropped the request
	 * unanswered. The outcome is {@link #last()}.
	 */
	@Nullable
	public NpcTradeResultPacket trade(Entity npc, ResourceLocation offerId, int... slots) {
		NpcTradeRequestLimiter.logout(player());
		Outcome.Probe probe = Outcome.begin(this, npc, "trade " + offerId);
		int before = captured.sent(NpcTradeResultPacket.class).size();
		NpcTradeRequestPacket.handle(new NpcTradeRequestPacket(npc.getId(), offerId, slots), PayloadContexts.of(player()));
		probe.finish();
		List<NpcTradeResultPacket> replies = captured.sent(NpcTradeResultPacket.class);
		return replies.size() > before ? replies.getLast() : null;
	}

	/** A trade request to the block speaker whose conversation is open (the Monolith, entity id -1); null when dropped. */
	public NpcTradeResultPacket tradeAtBlock(ResourceLocation offerId, int... slots) {
		NpcTradeRequestLimiter.logout(player());
		Outcome.Probe probe = Outcome.begin(this, null, "trade " + offerId + " at a block");
		int before = captured.sent(NpcTradeResultPacket.class).size();
		NpcTradeRequestPacket.handle(new NpcTradeRequestPacket(SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID, offerId, slots),
				PayloadContexts.of(player()));
		probe.finish();
		List<NpcTradeResultPacket> replies = captured.sent(NpcTradeResultPacket.class);
		return replies.size() > before ? replies.getLast() : null;
	}

	// --- Items and blocks ------------------------------------------------------------------------------------------

	/** ARRANGE: puts {@code stack} in the inventory, or at the player's feet when it is full. */
	public PlaythroughPlayer give(ItemStack stack) {
		if (!player().getInventory().add(stack)) player().drop(stack, false);
		return this;
	}

	/**
	 * Takes {@code item} in the main hand from the inventory, as a player picks a hotbar slot or swaps one in. Fails
	 * when the player has none.
	 */
	public ItemStack wield(ItemLike item) {
		var inventory = player().getInventory();
		if (player().getMainHandItem().is(item.asItem())) return player().getMainHandItem();
		for (int slot = 0; slot < inventory.items.size(); slot++) {
			if (!inventory.items.get(slot).is(item.asItem())) continue;
			if (slot < 9) {
				inventory.selected = slot;
			} else {
				ItemStack held = inventory.getSelected();
				inventory.items.set(inventory.selected, inventory.items.get(slot));
				inventory.items.set(slot, held);
			}
			return player().getMainHandItem();
		}
		if (player().getOffhandItem().is(item.asItem())) {
			ItemStack off = player().getOffhandItem();
			player().setItemInHand(InteractionHand.OFF_HAND, player().getMainHandItem());
			player().setItemInHand(InteractionHand.MAIN_HAND, off);
			return off;
		}
		helper.fail("the player has no " + item.asItem() + " to hold");
		return ItemStack.EMPTY;
	}

	/** Uses what is in {@code hand} in the air (right-click with no block in reach). */
	public Outcome use(InteractionHand hand) {
		Outcome.Probe probe = Outcome.begin(this, null, "use " + player().getItemInHand(hand).getItem());
		InteractionResult result = player().gameMode.useItem(player(), player().level(), player().getItemInHand(hand), hand);
		probe.note("result " + result);
		return probe.finish();
	}

	/** Holds {@code stack} in {@code hand} (taken from the inventory when it is there, else put in hand) and uses it. */
	public Outcome use(ItemStack stack, InteractionHand hand) {
		int slot = slotOf(stack);
		if (hand == InteractionHand.MAIN_HAND && slot >= 0) wield(stack.getItem());
		else player().setItemInHand(hand, stack);
		return use(hand);
	}

	/** Right-clicks the top face of {@code pos}. */
	public Outcome useOn(BlockPos pos) {
		return useOn(pos, Direction.UP);
	}

	/**
	 * Right-clicks {@code face} of {@code pos} the way the client does: the main hand on the block, then the main hand
	 * in the air, then the same with the offhand, stopping at the first that takes the action. Goes through
	 * {@code ServerPlayerGameMode.useItemOn}, so {@code RightClickBlock} subscribers ({@code MachineAccessEvents})
	 * run.
	 */
	public Outcome useOn(BlockPos pos, Direction face) {
		Outcome.Probe probe = Outcome.begin(this, null, (player().isShiftKeyDown() ? "sneak-use " : "use ")
				+ player().getMainHandItem().getItem() + " on " + player().level().getBlockState(pos).getBlock()
				+ " " + pos.toShortString());
		Vec3 hit = Vec3.atCenterOf(pos).add(face.getStepX() * 0.5D, face.getStepY() * 0.5D, face.getStepZ() * 0.5D);
		aim(hit);
		BlockHitResult target = new BlockHitResult(hit, face, pos, false);
		ServerPlayer player = player();
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			InteractionResult onBlock = player.gameMode.useItemOn(player, player.level(), stack, hand, target);
			if (onBlock.consumesAction()) {
				probe.note(hand + " on block: " + onBlock);
				return probe.finish();
			}
			if (onBlock == InteractionResult.FAIL) {
				probe.note(hand + " on block: FAIL");
				return probe.finish();
			}
			if (!player.getItemInHand(hand).isEmpty()) {
				InteractionResult inAir = player.gameMode.useItem(player, player.level(), player.getItemInHand(hand), hand);
				if (inAir.consumesAction()) {
					probe.note(hand + " in air: " + inAir);
					return probe.finish();
				}
			}
		}
		probe.note("nothing took the click");
		return probe.finish();
	}

	/** Right-clicks the top face of {@code pos} while sneaking. */
	public Outcome sneakUseOn(BlockPos pos) {
		return sneakUseOn(pos, Direction.UP);
	}

	public Outcome sneakUseOn(BlockPos pos, Direction face) {
		player().setShiftKeyDown(true);
		try {
			return useOn(pos, face);
		} finally {
			player().setShiftKeyDown(false);
		}
	}

	/**
	 * Places {@code item} (taken from the inventory) at {@code pos} by right-clicking the top of the block below.
	 * The block is removed when the test ends.
	 */
	public Outcome place(BlockPos pos, ItemLike item) {
		wield(item);
		Outcome outcome = useOn(pos.below(), Direction.UP);
		if (!player().level().getBlockState(pos).isAir()) placed.add(pos.immutable());
		return outcome;
	}

	/**
	 * Swings at {@code target} with a full attack charge, as a player who waited for the cooldown, with the held
	 * weapon's damage (see {@link #attack(Entity, Vec3)}).
	 */
	public Outcome attack(Entity target) {
		return attack(target, target.getBoundingBox().getCenter());
	}

	/**
	 * Swings at {@code target}, aimed at {@code point}. A captured player never ticks, so the living tick's equipment
	 * pass never adds the main-hand item's attribute modifiers: they are put on for the swing (as transient
	 * modifiers) and taken off after it, so a sword hits like a sword and not like a fist.
	 */
	public Outcome attack(Entity target, Vec3 point) {
		Outcome.Probe probe = Outcome.begin(this, target, "attack " + describe(target));
		aim(point);
		set(ATTACK_STRENGTH, player(), 1000);
		ServerPlayer player = player();
		List<Map.Entry<AttributeInstance, AttributeModifier>> worn = new ArrayList<>();
		player.getMainHandItem().forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
			AttributeInstance instance = player.getAttribute(attribute);
			if (instance != null && !instance.hasModifier(modifier.id())) {
				instance.addTransientModifier(modifier);
				worn.add(Map.entry(instance, modifier));
			}
		});
		try {
			player.attack(target);
		} finally {
			for (Map.Entry<AttributeInstance, AttributeModifier> entry : worn) entry.getKey().removeModifier(entry.getValue().id());
		}
		return probe.finish();
	}

	/**
	 * Holds right-click with {@code item} on {@code target} for {@code ticks} server ticks: aims, starts using it
	 * through {@code useItem}, then runs the living-entity use tick once per real server tick (so {@code onUseTick},
	 * use-duration completion and {@code finishUsingItem} all happen as they do in play), then lets go
	 * ({@code releaseUsingItem}) if the item is still in use. Ends early when the item stops being used by itself.
	 */
	public PlaythroughClock.Step hold(ItemLike item, int ticks, Entity target) {
		return holdUntil(item, ticks, () -> target.getBoundingBox().getCenter(), () -> false);
	}

	public PlaythroughClock.Step hold(ItemLike item, int ticks, BlockPos target) {
		Vec3 centre = Vec3.atCenterOf(target);
		return holdUntil(item, ticks, () -> centre, () -> false);
	}

	public PlaythroughClock.Step hold(ItemLike item, int ticks, Vec3 target) {
		return holdUntil(item, ticks, () -> target, () -> false);
	}

	/** Holds as {@link #hold} until {@code done} (checked before each tick) or {@code maxTicks} have passed. */
	public PlaythroughClock.Step holdUntil(ItemLike item, int maxTicks, java.util.function.Supplier<Vec3> target,
			BooleanSupplier done) {
		return new PlaythroughClock.Step() {
			@Override
			public boolean tick(int ticks) {
				if (ticks == 0) {
					startHold(item, target.get());
					if (!player().isUsingItem()) {
						releaseHold();
						return true;
					}
				}
				if (ticks >= maxTicks || done.getAsBoolean() || !player().isUsingItem()) {
					releaseHold();
					return true;
				}
				aim(target.get());
				holdTick();
				return false;
			}

			@Override
			public String label() {
				return "hold " + item.asItem() + " for " + maxTicks;
			}
		};
	}

	/** The first tick of a hold: wields {@code item}, aims at {@code aimAt} and starts using it. */
	public void startHold(ItemLike item, Vec3 aimAt) {
		wield(item);
		aim(aimAt);
		holding = Outcome.begin(this, null, "hold " + item.asItem());
		InteractionResult result = player().gameMode.useItem(player(), player().level(), player().getMainHandItem(),
				InteractionHand.MAIN_HAND);
		holding.note("use " + result + (player().isUsingItem() ? "" : ", not in use"));
	}

	/** One server tick of the use in progress. */
	public void holdTick() {
		invoke(UPDATING_USING_ITEM, player());
	}

	/** Lets go of right-click; finishes the hold's outcome. */
	public Outcome releaseHold() {
		if (player().isUsingItem()) player().releaseUsingItem();
		Outcome outcome = holding == null ? Outcome.begin(this, null, "release").finish() : holding.finish();
		holding = null;
		return outcome;
	}

	/** Casts the selected manipulation through the cast key's payload (this is how a cell hand is conjured). */
	public Outcome castSelected() {
		Outcome.Probe probe = Outcome.begin(this, null, "cast selected");
		UseManipKeyPacket.handle(new UseManipKeyPacket(), PayloadContexts.of(player()));
		return probe.finish();
	}

	/**
	 * Selects {@code manipulation} the way the cycle key does ({@code ChangeSelectedManipPacket}), pressing it until
	 * the manipulation is selected. Fails when it never comes round (it is not equipped).
	 */
	public Outcome select(String manipulation) {
		Outcome.Probe probe = Outcome.begin(this, null, "select " + manipulation);
		var known = HemoCapabilityAccess.getKnownManipulations(player());
		int presses = known.getEquippedManipNames().size() + 1;
		for (int i = 0; i < presses && !selected(manipulation); i++) {
			ChangeSelectedManipPacket.handle(new ChangeSelectedManipPacket(), PayloadContexts.of(player()));
		}
		Outcome outcome = probe.finish();
		helper.assertTrue(selected(manipulation), manipulation + " can be selected; " + outcome.describe());
		return outcome;
	}

	private boolean selected(String manipulation) {
		var selected = HemoCapabilityAccess.getKnownManipulations(player()).getSelectedManip();
		return selected != null && manipulation.equals(selected.getName());
	}

	/** Clicks a slot of {@code menu} as the container-click payload does. */
	public Outcome click(AbstractContainerMenu menu, int slot, int button, ClickType type) {
		Outcome.Probe probe = Outcome.begin(this, null, "click " + type + " slot " + slot);
		player().containerMenu = menu;
		if (!menu.stillValid(player())) {
			probe.note("menu is no longer valid for the player");
			return probe.finish();
		}
		menu.suppressRemoteUpdates();
		menu.clicked(slot, button, type, player());
		menu.resumeRemoteUpdates();
		menu.broadcastChanges();
		return probe.finish();
	}

	/** Shift-clicks a slot of {@code menu}. */
	public Outcome quickMove(AbstractContainerMenu menu, int slot) {
		return click(menu, slot, 0, ClickType.QUICK_MOVE);
	}

	/** Closes the open menu, as the close payload does. */
	public Outcome closeMenu() {
		Outcome.Probe probe = Outcome.begin(this, null, "close menu");
		player().doCloseContainer();
		return probe.finish();
	}

	/**
	 * Walks over each {@code item} drop within 3 blocks of {@code pos} ({@code ItemEntity#playerTouch}). A drop still
	 * in its pickup delay is waited out first; the outcome notes it.
	 */
	public Outcome pickUpNear(BlockPos pos, ItemLike item) {
		Outcome.Probe probe = Outcome.begin(this, null, "pick up " + item.asItem() + " near " + pos.toShortString());
		for (ItemEntity drop : player().serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3.0D),
				drop -> drop.getItem().is(item.asItem()))) {
			if (drop.hasPickUpDelay()) {
				drop.setNoPickUpDelay();
				probe.note("waited out a pickup delay");
			}
			drop.playerTouch(player());
		}
		return probe.finish();
	}

	/**
	 * Sleeps through a night in the bed at {@code bed}: night falls, {@code startSleepInBed}, the morning comes as the
	 * night skip brings it, and {@code stopSleepInBed(false, false)} wakes the player the way the server's night skip
	 * does. When the bed refuses the player with {@code OTHER_PROBLEM} (the player is not in the level), it posts
	 * {@code PlayerWakeUpEvent(false, false)} instead and the outcome notes it; any other refusal is noted and nothing
	 * is posted. Run it in a {@code _clock} batch.
	 */
	public Outcome sleepThroughNight(BlockPos bed) {
		Outcome.Probe probe = Outcome.begin(this, null, "sleep in " + bed.toShortString());
		ServerLevel level = player().serverLevel();
		long day = level.getDayTime();
		if (level.isDay()) {
			level.setDayTime(day - Math.floorMod(day, 24000L) + 13000L);
			// isDay() reads the sky darkness, which the level only recomputes on its next tick.
			level.updateSkyBrightness();
		}
		var result = player().startSleepInBed(bed);
		if (result.left().isPresent()) {
			Player.BedSleepingProblem problem = result.left().get();
			if (problem == Player.BedSleepingProblem.OTHER_PROBLEM) {
				probe.note("bed refused OTHER_PROBLEM; posted PlayerWakeUpEvent(false, false)");
				NeoForge.EVENT_BUS.post(new PlayerWakeUpEvent(player(), false, false));
			} else {
				probe.note("bed refused " + problem);
			}
			return probe.finish();
		}
		long night = level.getDayTime();
		level.setDayTime(night - Math.floorMod(night, 24000L) + 24000L);
		level.updateSkyBrightness();
		player().stopSleepInBed(false, false);
		return probe.finish();
	}

	/** Gets out of bed before morning ({@code stopSleepInBed(false, true)}). */
	public Outcome leaveBed() {
		Outcome.Probe probe = Outcome.begin(this, null, "leave bed");
		player().stopSleepInBed(false, true);
		return probe.finish();
	}

	/** Takes {@code amount} generic damage. */
	public Outcome damage(float amount) {
		Outcome.Probe probe = Outcome.begin(this, null, "damage " + amount);
		player().invulnerableTime = 0;
		boolean hurt = player().hurt(player().level().damageSources().generic(), amount);
		probe.note(hurt ? "hurt" : "not hurt");
		return probe.finish();
	}

	/** Dies to generic damage greater than the player's health. Follow with {@link #respawn()}. */
	public Outcome die() {
		Outcome.Probe probe = Outcome.begin(this, null, "die");
		player().invulnerableTime = 0;
		player().hurt(player().level().damageSources().generic(), player().getMaxHealth() * 10.0F + 100.0F);
		probe.note(player().isDeadOrDying() ? "dead" : "survived");
		return probe.finish();
	}

	/**
	 * Respawns as the server does after death: a new player with the same profile, {@code restoreFrom(old, false)}
	 * (which posts {@code PlayerEvent.Clone}; root persistent data outside {@code PlayerPersisted} is dropped), then
	 * {@code PlayerRespawnEvent}. The respawn point carries over as {@code PlayerList.respawn} carries it (unless its
	 * block is missing); the new player still stands at {@link #home()}. Returns this wrapper.
	 */
	public PlaythroughPlayer respawn() {
		return replace(false, false);
	}

	/** Returns from the End: as {@link #respawn()} with {@code restoreFrom(old, true)}. */
	public PlaythroughPlayer endReturn() {
		return replace(true, true);
	}

	private PlaythroughPlayer replace(boolean keepEverything, boolean fromEnd) {
		Outcome.Probe probe = Outcome.begin(this, null, fromEnd ? "end return" : "respawn");
		CapturedPlayer old = captured;
		incarnate(CapturedPlayer.create(helper, profile));
		ServerPlayer next = captured.player;
		next.restoreFrom(old.player, keepEverything);
		// As PlayerList.respawn: the respawn point carries over unless its block is missing.
		if (!old.player.findRespawnPositionAndUseSpawnBlock(keepEverything, DimensionTransition.DO_NOTHING).missingRespawnBlock()) {
			next.copyRespawnPosition(old.player);
		}
		copyJourneyKeys(old.player.getPersistentData(), next.getPersistentData());
		next.moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, 0.0F, 0.0F);
		next.setHealth(next.getMaxHealth());
		if (inWorld) {
			old.player.serverLevel().removePlayerImmediately(old.player, Entity.RemovalReason.DISCARDED);
			next.serverLevel().addNewPlayer(next);
		}
		if (joined) {
			old.leaveLevel();
			captured.joinLevel();
		}
		EventHooks.firePlayerRespawnEvent(next, fromEnd);
		old.close();
		views.clear();
		blockViews.clear();
		probe.finish();
		return this;
	}

	/**
	 * Logs out and back in: {@code PlayerLoggedOutEvent} for the old player (so {@code TransientServerStateEvents}
	 * clear their session state), then a new same-profile player loaded from the old one's save, and
	 * {@code PlayerLoggedInEvent} (migrations, pending deliveries, owner sync). Returns this wrapper.
	 */
	public PlaythroughPlayer relog() {
		Outcome.Probe probe = Outcome.begin(this, null, "relog");
		CapturedPlayer old = captured;
		EventHooks.firePlayerLoggedOut(old.player);
		CompoundTag save = old.player.saveWithoutId(new CompoundTag());
		old.offline();
		incarnate(CapturedPlayer.create(helper, profile));
		captured.player.load(save);
		joined = false;
		inWorld = false;
		EventHooks.firePlayerLoggedIn(captured.player);
		old.close();
		views.clear();
		blockViews.clear();
		probe.finish();
		return this;
	}

	/** Leaves the server's player lookup, as a logged-out player does, without the logout events. */
	public PlaythroughPlayer offline() {
		if (inWorld) player().serverLevel().removePlayerImmediately(player(), Entity.RemovalReason.UNLOADED_WITH_PLAYER);
		inWorld = false;
		joined = false;
		captured.offline();
		return this;
	}

	public PlaythroughPlayer online() {
		captured.online();
		return this;
	}

	/** ARRANGE: fills every main inventory slot except {@code exceptSlots} with an unstackable filler. */
	public PlaythroughPlayer fillInventory(int... exceptSlots) {
		var inventory = player().getInventory();
		outer:
		for (int slot = 0; slot < inventory.items.size(); slot++) {
			for (int except : exceptSlots) if (except == slot) continue outer;
			inventory.items.set(slot, new ItemStack(Items.SADDLE));
		}
		return this;
	}

	public PlaythroughPlayer creative() {
		player().setGameMode(GameType.CREATIVE);
		return this;
	}

	public PlaythroughPlayer survival() {
		player().setGameMode(GameType.SURVIVAL);
		return this;
	}

	/** Posts the player tick events {@code n} times, once per server tick. */
	public PlaythroughClock.Step tickPlayer(int n) {
		return new PlaythroughClock.Step() {
			private Outcome.Probe probe;

			@Override
			public boolean tick(int ticks) {
				if (ticks == 0) probe = Outcome.begin(PlaythroughPlayer.this, null, "tick player x" + n);
				if (ticks >= n) {
					probe.finish();
					return true;
				}
				tickOnce();
				if (ticks + 1 >= n) {
					probe.finish();
					return true;
				}
				return false;
			}

			@Override
			public String label() {
				return "tick player x" + n;
			}
		};
	}

	/** Posts {@code PlayerTickEvent.Pre} and {@code Post} once, now. */
	public void tickOnce() {
		EventHooks.firePlayerTickPre(player());
		EventHooks.firePlayerTickPost(player());
	}

	// --- Late-game entry points -------------------------------------------------------------------------------------

	/**
	 * Right-clicks {@code target} through {@code ServerPlayer.interactOn}, so {@code PlayerInteractEvent.EntityInteract}
	 * fires first (a rite helper's assignment, a Bloom Warden's amends, the Specimen Jar, a carousel captive) and only
	 * then the entity's own interaction. Needs no dialogue; the outcome notes the result. A dialogue it opened is kept
	 * for {@link #choose}.
	 */
	public Outcome interactOn(Entity target) {
		return interactOn(target, InteractionHand.MAIN_HAND);
	}

	public Outcome interactOn(Entity target, InteractionHand hand) {
		Outcome.Probe probe = Outcome.begin(this, target, "interact " + hand + " with " + describe(target));
		aim(target.getBoundingBox().getCenter());
		int before = captured.sent(OpenDialoguePacket.class).size();
		InteractionResult result = player().interactOn(target, hand);
		probe.note("result " + result);
		List<OpenDialoguePacket> opened = captured.sent(OpenDialoguePacket.class);
		if (opened.size() > before) views.put(target.getId(), new DialogueView(opened.getLast().tree()));
		return probe.finish();
	}

	/**
	 * Right-clicks the block at {@code pos} with an empty hand (a free hotbar slot is selected first) and reads the
	 * dialogue it served, such as the Sanguine Monolith's (its trees carry entity id
	 * {@code SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID}). Fails when the block opened nothing.
	 */
	public DialogueView talkToBlock(BlockPos pos) {
		Optional<DialogueView> view = tryTalkToBlock(pos);
		helper.assertTrue(view.isPresent(), "the block at " + pos.toShortString() + " opens a dialogue; " + chatTail());
		return view.get();
	}

	/** {@link #talkToBlock}, empty when the block opened nothing. */
	public Optional<DialogueView> tryTalkToBlock(BlockPos pos) {
		PlaythroughRites.emptyHand(this);
		return useOnForDialogue(pos);
	}

	/** Right-clicks {@code pos} holding what is in hand now and reads the dialogue. */
	public Optional<DialogueView> useOnForDialogue(BlockPos pos) {
		int before = captured.sent(OpenDialoguePacket.class).size();
		useOn(pos);
		List<OpenDialoguePacket> opened = captured.sent(OpenDialoguePacket.class);
		if (opened.size() <= before) return Optional.empty();
		DialogueView view = new DialogueView(opened.getLast().tree());
		blockViews.put(pos.immutable(), view);
		return Optional.of(view);
	}

	/** The dialogue the block at {@code pos} last served this player. */
	public Optional<DialogueView> lastBlockView(BlockPos pos) {
		return Optional.ofNullable(blockViews.get(pos.immutable()));
	}

	/**
	 * Chooses an option the block at {@code pos} last served this player (talk to it first): the acknowledged claim
	 * payload when the event needs one, else the plain option payload, both with the tree's entity id (-1 for the
	 * Monolith) as its screen sends them.
	 */
	public Outcome chooseAtBlock(BlockPos pos, String eventId) {
		DialogueView view = blockViews.get(pos.immutable());
		helper.assertTrue(view != null, "talk to the block at " + pos.toShortString() + " before choosing " + eventId);
		helper.assertTrue(view.offers(eventId), "the block at " + pos.toShortString() + " offers " + eventId
				+ "; it showed " + view.describe());
		return send(eventId, view.entityId(), null, DialogueRewardClaims.requiresAcknowledgement(eventId),
				"choose " + eventId + " at " + pos.toShortString());
	}

	/** The last whisper (entity id 0) served to the player: an {@code OpenDialoguePacket} or an inbox open. */
	public Optional<DialogueView> whisper() {
		for (int i = payloadLog.size() - 1; i >= 0; i--) {
			CustomPacketPayload payload = payloadLog.get(i);
			if (payload instanceof OpenPendingWhisperPacket open) return Optional.of(new DialogueView(open.tree()));
			if (payload instanceof OpenDialoguePacket open && open.tree().entityId() == 0) {
				return Optional.of(new DialogueView(open.tree()));
			}
		}
		return Optional.empty();
	}

	/**
	 * Opens the first queued whisper the way {@code /hemowhisper} (the chat link) does: {@code PendingWhispers.open}.
	 * The opened whisper is then {@link #whisper()}; its inbox id is {@link #openWhisperId()}.
	 */
	public Outcome openWhisper() {
		Outcome.Probe probe = Outcome.begin(this, null, "open whisper");
		probe.note(PendingWhispers.open(player()) ? "opened" : "nothing opened");
		return probe.finish();
	}

	/** The inbox id of the whisper last opened from the inbox, if any. */
	public Optional<UUID> openWhisperId() {
		for (int i = payloadLog.size() - 1; i >= 0; i--) {
			if (payloadLog.get(i) instanceof OpenPendingWhisperPacket open) return Optional.of(open.id());
		}
		return Optional.empty();
	}

	/** Chooses {@code eventId} in the last whisper served (it must be offered there), with entity id 0. */
	public Outcome chooseWhisper(String eventId) {
		Optional<DialogueView> view = whisper();
		helper.assertTrue(view.isPresent(), "a whisper was served before choosing " + eventId);
		helper.assertTrue(view.get().offers(eventId), "the whisper offers " + eventId + "; it showed " + view.get().describe());
		return send(eventId, 0, null, DialogueRewardClaims.requiresAcknowledgement(eventId), "choose whisper " + eventId);
	}

	/** Closes the inbox whisper {@code id} as the screen does when it closes ({@code ClosePendingWhisperPacket}). */
	public Outcome closeWhisper(UUID id) {
		Outcome.Probe probe = Outcome.begin(this, null, "close whisper " + id);
		ClosePendingWhisperPacket.handle(new ClosePendingWhisperPacket(id), PayloadContexts.of(player()));
		return probe.finish();
	}

	/** A copy of the queued whisper inbox, oldest first. */
	public ListTag pendingWhispers() {
		return PendingWhispers.entries(player());
	}

	/**
	 * Sends a bloodline ledger action ({@code PacketLedgerAction.ACTION_*}). The screen only opens from the signed
	 * ledger, so a carried one is taken in hand first; without one the payload's own guard drops the action.
	 */
	public Outcome ledgerAction(int action) {
		if (count(ItemInit.unsigned_ancestral_ledger.get()) > 0) wield(ItemInit.unsigned_ancestral_ledger.get());
		Outcome.Probe probe = Outcome.begin(this, null, "ledger action " + action);
		PacketLedgerAction.handle(new PacketLedgerAction(action), PayloadContexts.of(player()));
		return probe.finish();
	}

	/**
	 * The leader's kick from the ledger screen ({@code PacketKickBloodlinePlayer}). The kicked player is found
	 * through the server's player list, so they must have {@link #joinLevel() joined} to be told. On the GameTest server
	 * the leader's closing {@code kick.success} line cannot be sent (no profile cache); the outcome notes it.
	 */
	public Outcome kick(UUID member) {
		Outcome.Probe probe = Outcome.begin(this, null, "kick " + member);
		try {
			PacketKickBloodlinePlayer.handle(new PacketKickBloodlinePlayer(member), PayloadContexts.of(player()));
		} catch (NullPointerException exception) {
			// The GameTest server has no profile cache; the handler reads it for the leader's success line, after the
			// removal and the kicked player's notice. A real server has one, so only that last line is lost here.
			if (player().server.getProfileCache() != null) throw exception;
			probe.note("no profile cache on the GameTest server: the leader's kick.success line was not sent");
		}
		return probe.finish();
	}

	/**
	 * Uses the Harbinger Assignment Ledger (it must be carried) and reads the page it sent: the
	 * {@code OpenHarbingerAssignmentLedgerPacket}, with the Blood Beneath bar worked out as the screen works it.
	 */
	public LedgerView ledger() {
		int before = captured.sent(OpenHarbingerAssignmentLedgerPacket.class).size();
		wield(ItemInit.harbinger_assignment_ledger.get());
		use(InteractionHand.MAIN_HAND);
		List<OpenHarbingerAssignmentLedgerPacket> sent = captured.sent(OpenHarbingerAssignmentLedgerPacket.class);
		helper.assertTrue(sent.size() > before, "the Assignment Ledger opens; " + last());
		return new LedgerView(sent.getLast());
	}

	/** Clicks menu button {@code id} as {@code ServerboundContainerButtonClickPacket} does (Scriptorium rows, Spindle). */
	public Outcome clickButton(AbstractContainerMenu menu, int id) {
		Outcome.Probe probe = Outcome.begin(this, null, "click button " + id);
		player().containerMenu = menu;
		if (player().isSpectator() || !menu.stillValid(player())) {
			probe.note("menu is not valid for the player");
			return probe.finish();
		}
		boolean clicked = menu.clickMenuButton(player(), id);
		if (clicked) menu.broadcastChanges();
		probe.note(clicked ? "clicked" : "refused");
		return probe.finish();
	}

	/**
	 * A tap of the Marionette Crossbar (it must be carried): {@code use}, one use tick, then {@code releaseUsing},
	 * well under {@code MarionetteCrossbarItem.RADIAL_HOLD_TICKS}: how a player calls out or recalls their puppet.
	 */
	public Outcome crossbarCallOrRecall() {
		wield(ItemInit.marionette_crossbar.get());
		Outcome.Probe probe = Outcome.begin(this, null, "crossbar tap");
		InteractionResult result = player().gameMode.useItem(player(), player().level(), player().getMainHandItem(),
				InteractionHand.MAIN_HAND);
		probe.note("use " + result);
		if (player().isUsingItem()) {
			holdTick();
			player().releaseUsingItem();
		}
		return probe.finish();
	}

	/** A radial choice on the carried Crossbar ({@code PacketCrossbarRadialAction}). */
	public Outcome radial(PacketCrossbarRadialAction.Action action, String value) {
		wield(ItemInit.marionette_crossbar.get());
		UUID crossbar = MarionetteCrossbarItem.ensureCrossbarId(player().getMainHandItem());
		Outcome.Probe probe = Outcome.begin(this, null, "radial " + action + " " + value);
		PacketCrossbarRadialAction.handle(new PacketCrossbarRadialAction(crossbar, action, value), PayloadContexts.of(player()));
		return probe.finish();
	}

	/** Prepares {@code summon} in the open Puppeteer's Spindle ({@code PacketPuppeteersSpindleAction}). */
	public Outcome spindlePrepare(String summon) {
		Outcome.Probe probe = Outcome.begin(this, null, "spindle prepare " + summon);
		PacketPuppeteersSpindleAction.handle(new PacketPuppeteersSpindleAction(PacketPuppeteersSpindleAction.Action.PREPARE,
				summon), PayloadContexts.of(player()));
		return probe.finish();
	}

	/**
	 * Eats or drinks {@code item} (it must be carried): starts using it and runs the use tick until the item's use
	 * duration ends and {@code finishUsingItem} runs, all now. The outcome notes when it never finished.
	 */
	public Outcome consume(ItemLike item) {
		wield(item);
		Outcome.Probe probe = Outcome.begin(this, null, "consume " + item.asItem());
		ItemStack stack = player().getMainHandItem();
		int duration = stack.getUseDuration(player());
		InteractionResult result = player().gameMode.useItem(player(), player().level(), stack, InteractionHand.MAIN_HAND);
		probe.note("use " + result);
		for (int tick = 0; tick <= duration + 1 && player().isUsingItem(); tick++) holdTick();
		if (player().isUsingItem()) {
			probe.note("still in use after " + duration + " ticks; released");
			player().releaseUsingItem();
		}
		return probe.finish();
	}

	/** Breaks the block at {@code pos} as survival mining does ({@code gameMode.destroyBlock}, which posts BreakEvent). */
	public Outcome breakBlock(BlockPos pos) {
		Outcome.Probe probe = Outcome.begin(this, null, "break " + player().level().getBlockState(pos).getBlock() + " "
				+ pos.toShortString());
		aim(Vec3.atCenterOf(pos));
		probe.note(player().gameMode.destroyBlock(pos) ? "broken" : "not broken");
		return probe.finish();
	}

	/** Starts hitting the top of {@code pos} ({@code START_DESTROY_BLOCK}, so {@code LeftClickBlock} START fires). */
	public Outcome leftClickBlock(BlockPos pos) {
		Outcome.Probe probe = Outcome.begin(this, null, "left-click " + player().level().getBlockState(pos).getBlock() + " "
				+ pos.toShortString());
		aim(Vec3.atCenterOf(pos));
		player().gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, Direction.UP,
				player().level().getMaxBuildHeight(), 0);
		return probe.finish();
	}

	/**
	 * Opt-in: lists the player where level-wide player lookups find them ({@code PlayerList.getPlayers()} and the
	 * level's player list), as {@link CapturedPlayer#joinLevel()} does. Kept through {@link #respawn()}; dropped by
	 * {@link #offline()}, {@link #relog()} and the end of the test. The server never ticks the player, and
	 * {@code getEntitiesOfClass(Player.class, ...)} still does not find them.
	 */
	public PlaythroughPlayer joinLevel() {
		captured.joinLevel();
		joined = true;
		return this;
	}

	public boolean joinedLevel() {
		return captured.joinedLevel();
	}

	/**
	 * Opt-in, beyond {@link #joinLevel()}: the player is also added to the level's entity storage
	 * ({@code ServerLevel.addNewPlayer}), so {@code getEntitiesOfClass(Player.class, ...)}, entity traces and
	 * {@code level.getEntity(id)} find them, as they find a real player. The level then runs the light
	 * {@code ServerPlayer.tick} on them (never the connection's {@code doTick}, so the living tick still does not run).
	 * Kept through {@link #respawn()} (the new player takes the old one's place); dropped by {@link #relog()} and
	 * {@link #offline()} (call it again after a relog); the end of the test discards the player, which removes them.
	 */
	public PlaythroughPlayer enterWorld() {
		ServerLevel level = player().serverLevel();
		if (level.getEntity(player().getUUID()) == null) level.addNewPlayer(player());
		joinLevel();
		inWorld = true;
		return this;
	}

	public boolean inWorld() {
		return inWorld;
	}

	/** ARRANGE: moves the player into {@code level} at {@code pos} with the same UUID (the End for the Vagrant Mind). */
	public PlaythroughPlayer moveTo(ServerLevel level, BlockPos pos) {
		boolean rejoin = captured.joinedLevel();
		if (rejoin) captured.leaveLevel();
		player().setServerLevel(level);
		moveTo(pos);
		if (rejoin) captured.joinLevel();
		return this;
	}

	/** What {@code claim} still owes the player ({@code claim.owed()}); empty when nothing is owed. */
	public List<ItemStack> owed(PendingDelivery.Claim claim) {
		return claim.owed().apply(player());
	}

	// --- What the player has ---------------------------------------------------------------------------------------

	public int degree() {
		return HemoCapabilityAccess.getPlayerDegreeNumber(player());
	}

	public double blood() {
		return HemoCapabilityAccess.getBloodVolume(player()).getBloodVolume();
	}

	public boolean bloodActive() {
		return HemoCapabilityAccess.getBloodVolume(player()).isActive();
	}

	/** How many of {@code item} the player holds: inventory, armour, offhand and cursor. */
	public int count(ItemLike item) {
		return player().getInventory().countItem(item.asItem())
				+ (player().containerMenu.getCarried().is(item.asItem()) ? player().containerMenu.getCarried().getCount() : 0);
	}

	/** Whether the charm of vascularium sits in the vascular slot. */
	public boolean hasCharm() {
		return EarlyInitiation.attached(player());
	}

	/** Whether the player has the advancement; a path without a namespace is a Hemomancy one. */
	public boolean has(String advancement) {
		ResourceLocation id = advancement.contains(":") ? ResourceLocation.parse(advancement) : Hemomancy.rloc(advancement);
		return HarbingerAdvancementGranter.hasAdvancement(player(), id);
	}

	public boolean knows(String manipulation) {
		return HemoCapabilityAccess.getKnownManipulations(player()).getManipList().stream()
				.anyMatch(known -> known.getName().equals(manipulation));
	}

	/** Everything said to the player so far. */
	public List<Outcome.ChatLine> chat() {
		return List.copyOf(chatLog);
	}

	/** A bookmark in the transcript for {@link #chatSince}. */
	public int mark() {
		return chatLog.size();
	}

	public List<Outcome.ChatLine> chatSince(int mark) {
		return List.copyOf(chatLog.subList(Math.min(mark, chatLog.size()), chatLog.size()));
	}

	/** Every custom payload sent to the player so far, across incarnations. */
	public List<CustomPacketPayload> payloads() {
		return List.copyOf(payloadLog);
	}

	public <T extends CustomPacketPayload> List<T> sent(Class<T> type) {
		return payloadLog.stream().filter(type::isInstance).map(type::cast).toList();
	}

	// --- Ending ----------------------------------------------------------------------------------------------------

	private void close() {
		ServerPlayer player = player();
		if (player.level() != helper.getLevel()) player.setServerLevel(helper.getLevel());
		try {
			if (player.isUsingItem()) player.stopUsingItem();
			EventHooks.firePlayerLoggedOut(player);
		} finally {
			for (BlockPos pos : placed) {
				if (!player.level().getBlockState(pos).isAir()) player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			}
			if (player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY)) HemoJourneySnapshot.restore(player);
			DialogueEventProbe.forget(uuid());
			captured.close();
		}
	}

	// --- Helpers ---------------------------------------------------------------------------------------------------

	private String chatTail() {
		int from = Math.max(0, chatLog.size() - 3);
		return "last said " + chatLog.subList(from, chatLog.size());
	}

	@Nullable
	private Entity entity(int entityId) {
		return entityId == 0 ? null : player().level().getEntity(entityId);
	}

	private int slotOf(ItemStack stack) {
		var inventory = player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) if (inventory.getItem(slot) == stack) return slot;
		return -1;
	}

	static String describe(Entity entity) {
		return entity == null ? "nobody" : entity.getType().getDescriptionId() + "#" + entity.getId();
	}

	private static void copyJourneyKeys(CompoundTag from, CompoundTag to) {
		for (String key : from.getAllKeys()) {
			if (key.startsWith(JOURNEY_PREFIX) && !to.contains(key)) to.put(key, from.get(key).copy());
		}
	}

	private static Method method(Class<?> owner, String name) {
		try {
			Method method = owner.getDeclaredMethod(name);
			method.setAccessible(true);
			return method;
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not reach " + owner.getSimpleName() + "#" + name, exception);
		}
	}

	private static Field field(Class<?> owner, String name) {
		try {
			Field field = owner.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not reach " + owner.getSimpleName() + "#" + name, exception);
		}
	}

	private static void set(Field field, Object target, int value) {
		try {
			field.setInt(target, value);
		} catch (IllegalAccessException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static void invoke(Method method, Object target) {
		try {
			method.invoke(target);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not call " + method.getName(), exception);
		}
	}
}
