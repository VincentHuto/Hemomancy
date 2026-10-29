package com.vincenthuto.hemomancy.common.tile.harbinger.puzzle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelFocusBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Mark;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.MarkKind;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.PulsePylonBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SpawnMarkBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.WitnessStoneBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.HarbingerPathPermissions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A Chapel's focus: which deed of the Saint's life it tells (its kind and its slot on the ring door), whether it has
 * been completed, the marks its trial plays on across the wing, and the state of a trial in progress. One D8 player at
 * a time takes the trial; leaving the wing, dying or answering wrong resets it. A completed Chapel stays complete.
 *
 * <p>The marks come from the template ({@code Marks}: offsets from the focus as authored facing north, with a kind and
 * an index) and turn with the block's facing. Beholding flashes its witnesses and wants them walked back in order;
 * Litany is read at its stone and answered here; Downfall starts at its gate and raises each wave from its seals as
 * the challenger advances; Repose wants stillness inside its ring while its pylons fire.
 *
 * <p>Where a mark stands on its set piece (Track 6), the trial drives it: a witness stone's eye lights while the memory
 * touches it, a Downfall seal seeps while its wave is due, and a pulse pylon's head winds its charge up before each
 * pulse. A mark whose block is not that set piece is left alone.
 */
public class ChapelFocusBlockEntity extends HemoBlockEntity {
	public static final Item[] LITANY_ITEMS = {Items.BONE, Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.INK_SAC,
			Items.GLOWSTONE_DUST, Items.REDSTONE};
	private static final ParticleColor CRIMSON = new ParticleColor(168, 36, 54);
	private static final ParticleColor PALE = new ParticleColor(226, 206, 200);
	/** Putriciel's purge: Flammeus embers and their soot instead of the pale pulse. */
	private static final ParticleColor PURGE_EMBER = new ParticleColor(226, 30, 46);
	private static final ParticleColor PURGE_SOOT = new ParticleColor(20, 10, 12);
	/** Velorum's pulse: cold blue-white motes and fine frost that sinks and settles, instead of the pale pulse. */
	private static final ParticleColor FROST_MOTE = new ParticleColor(196, 224, 255);
	private static final ParticleColor FROST_FINE = new ParticleColor(236, 246, 255);

	public enum Phase { IDLE, SHOW, ANSWER, ACTIVE }

	private ChapelRules.Kind kind = ChapelRules.Kind.BEHOLDING;
	private int slot;
	private String saint = "HEMORATH";
	private boolean complete;
	private Phase phase = Phase.IDLE;
	@Nullable
	private UUID challenger;
	private long seed;
	private int step;
	private int timer;
	private int progress;
	private int cooldown;
	private int lastWitness = -1;
	private int lastOffer = -100;
	@Nullable
	private Item lastOfferItem;
	private final List<UUID> wave = new ArrayList<>();
	private final List<Mark> marks = new ArrayList<>();
	/** Players standing at the Litany stone now, so its words are shown once per approach. Not saved. */
	private final Set<UUID> readers = new HashSet<>();
	private double lastX;
	private double lastZ;

	public ChapelFocusBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.chapel_focus.get(), pos, state);
	}

	public ChapelRules.Kind kind() { return kind; }
	public int slot() { return slot; }
	public boolean complete() { return complete; }
	public Phase phase() { return phase; }
	public List<Mark> marks() { return List.copyOf(marks); }

	public void configure(ChapelRules.Kind kind, int slot) {
		this.kind = kind;
		this.slot = slot;
		setChanged();
	}

	/** Replaces the wing's marks (offsets as authored, facing north); the generator writes them, tests set them. */
	public void setMarks(List<Mark> marks) {
		this.marks.clear();
		this.marks.addAll(marks);
		setChanged();
	}

	/** Where a mark stands in the world. */
	public BlockPos markPos(Mark mark) {
		Direction facing = getBlockState().hasProperty(ChapelFocusBlock.FACING)
				? getBlockState().getValue(ChapelFocusBlock.FACING) : Direction.NORTH;
		return ChapelRules.place(worldPosition, mark.offset(), facing);
	}

	private List<Mark> marksOf(MarkKind kind) {
		return ChapelRules.marksOf(marks, kind);
	}

	/** A D8 player takes the trial; returns false (with a message) when it cannot begin now. */
	public boolean begin(ServerPlayer player) {
		if (complete) {
			tell(player, "message.hemomancy.chapel.complete", ChatFormatting.GRAY);
			return false;
		}
		if (!ChapelRules.mayAttempt(HemoCapabilityAccess.getPlayerDegreeNumber(player))) {
			tell(player, "message.hemomancy.chapel.refused", ChatFormatting.DARK_RED);
			return false;
		}
		if (phase != Phase.IDLE || cooldown > 0) return false;
		if (kind == ChapelRules.Kind.BEHOLDING && marksOf(MarkKind.WITNESS).isEmpty()) {
			tell(player, "message.hemomancy.chapel.unmarked", ChatFormatting.GRAY);
			return false;
		}
		lastX = player.getX();
		lastZ = player.getZ();
		start(player.getUUID(), kind == ChapelRules.Kind.LITANY
				? ChapelRules.litanySeed(worldPosition.asLong(), slot) : player.getRandom().nextLong());
		tell(player, "message.hemomancy.chapel.begin." + kind.name().toLowerCase(), ChatFormatting.DARK_PURPLE);
		if (kind == ChapelRules.Kind.LITANY && marksOf(MarkKind.LITANY_STONE).isEmpty()) {
			player.displayClientMessage(litanyText(player), false);
		}
		return true;
	}

	/** The focus touched while it waits. A Downfall with a gate is walked from that gate, not begun at its end. */
	public void touch(ServerPlayer player) {
		if (!complete && kind == ChapelRules.Kind.DOWNFALL && !marksOf(MarkKind.START).isEmpty()) {
			tell(player, "message.hemomancy.chapel.downfall.gate", ChatFormatting.DARK_PURPLE);
			return;
		}
		begin(player);
	}

	/** Starts a trial with a known seed; the challenger may be null when a test drives the trial. */
	public void start(@Nullable UUID challenger, long seed) {
		this.challenger = challenger;
		this.seed = seed;
		step = 0;
		timer = 0;
		progress = 0;
		lastWitness = -1;
		lastOffer = -100;
		lastOfferItem = null;
		phase = switch (kind) {
			case BEHOLDING -> Phase.SHOW;
			case LITANY -> Phase.ANSWER;
			case DOWNFALL, REPOSE -> Phase.ACTIVE;
		};
		if (kind == ChapelRules.Kind.DOWNFALL && level instanceof ServerLevel server) spawnWave(server);
		setChanged();
	}

	/** The Litany's inscription, in Abocipher unless the reader has learned it. */
	private Component litanyText(ServerPlayer player) {
		List<Integer> order = ChapelRules.litanyOrder(ChapelRules.litanySeed(worldPosition.asLong(), slot));
		MutableComponent text = Component.translatable("message.hemomancy.chapel.litany.inscription",
				LITANY_ITEMS[order.get(0)].getDescription(), LITANY_ITEMS[order.get(1)].getDescription(),
				LITANY_ITEMS[order.get(2)].getDescription());
		boolean literate = LiberKnowledgeHelper.hasEntry(player, LiberEntryDefinitions.ABOCIPHER_LITERACY);
		return literate ? text.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
				: text.withStyle(Style.EMPTY.withFont(Hemomancy.rloc("abocipher")).withColor(ChatFormatting.DARK_RED));
	}

	/** A Litany offering, given by right-clicking the focus; returns whether it was taken. */
	public boolean offer(ItemStack stack, @Nullable Player player) {
		if (kind != ChapelRules.Kind.LITANY || phase != Phase.ANSWER || stack.isEmpty()) return false;
		boolean litanyItem = false;
		for (Item item : LITANY_ITEMS) litanyItem |= stack.is(item);
		// A held right-click repeats the use: the item just taken, offered again at once, is that repeat, not an answer.
		if (!litanyItem || (lastOfferItem != null && stack.is(lastOfferItem) && timer - lastOffer < 10)) return false;
		Item wanted = LITANY_ITEMS[ChapelRules.litanyOrder(seed).get(step)];
		if (!stack.is(wanted)) {
			fail(player);
			return false;
		}
		if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
		lastOffer = timer;
		lastOfferItem = wanted;
		step++;
		chime(worldPosition, 0.8f + step * 0.15f);
		if (step >= 3) finish(player);
		else setChanged();
		return true;
	}

	/** A witness reached during a Beholding's answer, by its index among the wing's witnesses. */
	public void touchWitness(int witness, @Nullable Player player) {
		if (kind != ChapelRules.Kind.BEHOLDING || phase != Phase.ANSWER || witness == lastWitness) return;
		// lastWitness below -1 marks a witness left behind; reaching it again counts as a new touch.
		lastWitness = witness;
		if (sequence().get(step) != witness) {
			fail(player);
			return;
		}
		step++;
		List<Mark> witnesses = marksOf(MarkKind.WITNESS);
		chime(witness < witnesses.size() ? markPos(witnesses.get(witness)) : worldPosition, 0.8f + step * 0.15f);
		if (step >= ChapelRules.BEHOLDING_STEPS) finish(player);
		else setChanged();
	}

	private List<Integer> sequence() {
		return ChapelRules.beholdingSequence(seed, Math.max(1, marksOf(MarkKind.WITNESS).size()));
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ChapelFocusBlockEntity focus) {
		if (focus.cooldown > 0) focus.cooldown--;
		if (focus.complete) return;
		ServerLevel server = (ServerLevel) level;
		if (focus.phase == Phase.IDLE) {
			if (level.getGameTime() % 10 == 0) focus.watchWing(server);
			return;
		}
		ServerPlayer challenger = focus.challenger == null ? null
				: server.getServer().getPlayerList().getPlayer(focus.challenger);
		double reach = ChapelRules.reach(focus.marks);
		if (focus.challenger != null && (challenger == null || !challenger.isAlive() || challenger.level() != level
				|| challenger.distanceToSqr(Vec3.atBottomCenterOf(pos)) > reach * reach)) {
			focus.fail(challenger);
			return;
		}
		focus.timer++;
		if (focus.kind == ChapelRules.Kind.LITANY && level.getGameTime() % 10 == 0) focus.watchWing(server);
		switch (focus.phase) {
			case SHOW -> focus.tickShow(server);
			case ANSWER -> focus.tickAnswer(challenger);
			case ACTIVE -> {
				if (focus.kind == ChapelRules.Kind.DOWNFALL) focus.tickDownfall(server, challenger);
				else focus.tickRepose(server, challenger);
			}
			default -> { }
		}
	}

	/** Between trials: the Litany stone shows its words to whoever comes up to it; the Downfall's gate starts the fall. */
	private void watchWing(ServerLevel level) {
		if (kind == ChapelRules.Kind.LITANY) {
			for (Mark stone : marksOf(MarkKind.LITANY_STONE)) {
				Vec3 at = Vec3.atBottomCenterOf(markPos(stone));
				Set<UUID> near = new HashSet<>();
				for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class,
						new AABB(at, at).inflate(ChapelRules.STONE_REACH), Player::isAlive)) {
					near.add(player.getUUID());
					if (readers.add(player.getUUID())) {
						player.displayClientMessage(litanyText(player), false);
						chime(markPos(stone), 0.6f);
					}
				}
				readers.retainAll(near);
			}
		} else if (kind == ChapelRules.Kind.DOWNFALL && cooldown == 0) {
			for (Mark gate : marksOf(MarkKind.START)) {
				Vec3 at = Vec3.atBottomCenterOf(markPos(gate));
				for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class,
						new AABB(at, at).inflate(ChapelRules.GATE_REACH), Player::isAlive)) {
					if (!player.isSpectator()
							&& ChapelRules.mayAttempt(HemoCapabilityAccess.getPlayerDegreeNumber(player))) {
						begin(player);
						return;
					}
				}
			}
		}
	}

	private void tickShow(ServerLevel level) {
		if (timer % ChapelRules.BEHOLDING_FLASH_TICKS != 0) return;
		int index = timer / ChapelRules.BEHOLDING_FLASH_TICKS - 1;
		List<Integer> sequence = sequence();
		if (index >= sequence.size()) {
			lightWitnesses(-1);
			phase = Phase.ANSWER;
			timer = 0;
			ServerPlayer challenger = this.challenger == null ? null
					: level.getServer().getPlayerList().getPlayer(this.challenger);
			lastWitness = challenger == null ? -1 : witnessAt(challenger);
			setChanged();
			return;
		}
		List<Mark> witnesses = marksOf(MarkKind.WITNESS);
		if (sequence.get(index) >= witnesses.size()) return;
		lightWitnesses(sequence.get(index));
		BlockPos witness = markPos(witnesses.get(sequence.get(index)));
		level.sendParticles(HemoParticleData.glow(CRIMSON), witness.getX() + 0.5, witness.getY() + 0.9,
				witness.getZ() + 0.5, 40, 0.2, 0.6, 0.2, 0.02);
		level.sendParticles(HemoParticleData.glow(PALE), witness.getX() + 0.5, witness.getY() + 1.6,
				witness.getZ() + 0.5, 12, 0.1, 0.3, 0.1, 0.01);
		level.playSound(null, witness, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.6f, 0.6f + index * 0.2f);
	}

	private void tickAnswer(@Nullable ServerPlayer challenger) {
		int limit = kind == ChapelRules.Kind.LITANY ? ChapelRules.LITANY_ANSWER_TICKS : ChapelRules.ANSWER_TICKS;
		if (timer > limit) {
			fail(challenger);
			return;
		}
		if (kind != ChapelRules.Kind.BEHOLDING || challenger == null) return;
		int witness = witnessAt(challenger);
		if (witness >= 0) touchWitness(witness, challenger);
		else if (lastWitness >= 0 && step < ChapelRules.BEHOLDING_STEPS) lastWitness = -2 - lastWitness;
	}

	/** The witness the player stands at, or -1. The steles are solid, so "at" means pressed against one. */
	private int witnessAt(Player player) {
		List<Mark> witnesses = marksOf(MarkKind.WITNESS);
		for (int i = 0; i < witnesses.size(); i++) {
			BlockPos witness = markPos(witnesses.get(i));
			double dx = player.getX() - (witness.getX() + 0.5);
			double dz = player.getZ() - (witness.getZ() + 0.5);
			if (Math.abs(player.getY() - witness.getY()) < 2.5 && ChapelRules.atWitness(dx, dz)) return i;
		}
		return -1;
	}

	/**
	 * The gauntlet: {@code step} counts the waves risen. Once a wave is down, the next rises when the challenger comes
	 * up to its seals; with every wave down, the fall ends at the focus.
	 */
	private void tickDownfall(ServerLevel level, @Nullable ServerPlayer challenger) {
		if (timer % 10 != 0) return;
		wave.removeIf(id -> {
			Entity mob = level.getEntity(id);
			return mob == null || !mob.isAlive();
		});
		markDueSeals();
		if (!wave.isEmpty()) return;
		if (step < ChapelRules.DOWNFALL_WAVES) {
			if (challenger == null || near(challenger, ChapelRules.seals(marks, step), ChapelRules.WAVE_TRIGGER)) {
				spawnWave(level);
			}
			return;
		}
		if (challenger == null || challenger.distanceToSqr(Vec3.atBottomCenterOf(worldPosition))
				<= ChapelRules.FINISH_REACH * ChapelRules.FINISH_REACH) {
			finish(challenger);
		}
	}

	private boolean near(Player player, List<Mark> at, double reach) {
		if (at.isEmpty()) return true;
		for (Mark mark : at) {
			if (player.distanceToSqr(Vec3.atBottomCenterOf(markPos(mark))) <= reach * reach) return true;
		}
		return false;
	}

	/** Raises wave {@code step} from its seals (beside the focus when the wing has none) and counts it risen. */
	private void spawnWave(ServerLevel level) {
		EntityType<?> type = switch (saint) {
			case "SERAPHAE" -> EntityType.VEX;
			case "PUTRICIEL" -> EntityInit.bog_revenant.get();
			case "VELORUM" -> EntityType.STRAY;
			default -> EntityInit.hematic_construct.get();
		};
		List<Mark> seals = ChapelRules.seals(marks, step);
		for (int i = 0; i < ChapelRules.DOWNFALL_WAVE_SIZE; i++) {
			BlockPos at = seals.isEmpty() ? worldPosition.relative(Direction.from2DDataValue(i)).above()
					: markPos(seals.get(i % seals.size()));
			Entity spawned = type.spawn(level, at, MobSpawnType.EVENT);
			if (spawned instanceof Mob mob) mob.setPersistenceRequired();
			if (spawned != null) wave.add(spawned.getUUID());
			level.sendParticles(HemoParticleData.glow(CRIMSON), at.getX() + 0.5, at.getY() + 0.1, at.getZ() + 0.5, 16,
					0.4, 0.05, 0.4, 0.02);
		}
		level.playSound(null, seals.isEmpty() ? worldPosition : markPos(seals.get(0)), SoundEvents.WITHER_SPAWN,
				SoundSource.HOSTILE, 0.4f, 1.4f);
		step++;
		markDueSeals();
		setChanged();
	}

	/** Stillness inside the ring while the pylons fire; each pulse reaches every player in the hall. */
	private void tickRepose(ServerLevel level, @Nullable ServerPlayer challenger) {
		chargePylons(ChapelRules.pylonCharge(timer));
		if (challenger == null) return;
		double moveSqr = (challenger.getX() - lastX) * (challenger.getX() - lastX)
				+ (challenger.getZ() - lastZ) * (challenger.getZ() - lastZ);
		lastX = challenger.getX();
		lastZ = challenger.getZ();
		List<Mark> circles = marksOf(MarkKind.CIRCLE);
		BlockPos centre = circles.isEmpty() ? worldPosition : markPos(circles.get(0));
		Vec3 middle = Vec3.atBottomCenterOf(centre);
		boolean near = challenger.distanceToSqr(middle) <= ChapelRules.REPOSE_RADIUS * ChapelRules.REPOSE_RADIUS;
		boolean still = near && ChapelRules.still(moveSqr);
		boolean silentium = HarbingerPathPermissions.isSilentium(HemoCapabilityAccess.getPlayerDegreeNumber(challenger),
				HemoCapabilityAccess.getInitiatoryDegree(challenger).getArchonPath());
		progress = still ? progress + ChapelRules.reposeGain(silentium, true) : 0;
		if (timer % ChapelRules.REPOSE_PULSE_TICKS == 0) {
			for (Player inside : level.getEntitiesOfClass(Player.class,
					new AABB(middle, middle).inflate(ChapelRules.PULSE_REACH))) {
				float damage = ChapelRules.pulseDamage(inside.getHealth());
				if (damage > 0) inside.hurt(level.damageSources().magic(), damage);
				inside.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
			}
			// Putriciel's pulse is a purge: embers and soot rather than the pale light. Velorum's is frost settling.
			boolean purge = "PUTRICIEL".equals(saint);
			boolean frost = "VELORUM".equals(saint);
			List<Mark> pylons = marksOf(MarkKind.PYLON);
			for (Mark pylon : pylons) {
				Vec3 from = Vec3.atCenterOf(markPos(pylon));
				Vec3 path = middle.add(0, 1.0, 0).subtract(from);
				for (int i = 0; i <= 8; i++) {
					Vec3 at = from.add(path.scale(i / 8.0));
					if (purge) {
						level.sendParticles(HemoParticleData.emberSpark(PURGE_EMBER, 0.08f, 0.9f, 20), at.x, at.y, at.z,
								2, 0.05, 0.05, 0.05, 0.01);
						level.sendParticles(HemoParticleData.darkGlow(PURGE_SOOT), at.x, at.y + 0.1, at.z, 1, 0.05, 0.05,
								0.05, 0.0);
					} else if (frost) {
						settleFrost(level, at.x, at.y, at.z);
					} else {
						level.sendParticles(HemoParticleData.glow(PALE), at.x, at.y, at.z, 3, 0.05, 0.05, 0.05, 0.0);
					}
				}
				level.playSound(null, markPos(pylon), SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.5f, 1.6f);
			}
			if (purge) {
				level.sendParticles(HemoParticleData.emberSpark(PURGE_EMBER, 0.1f, 0.9f, 24), middle.x, middle.y + 0.2,
						middle.z, 24, ChapelRules.REPOSE_RADIUS * 0.6, 0.05, ChapelRules.REPOSE_RADIUS * 0.6, 0.02);
				level.sendParticles(HemoParticleData.darkGlow(PURGE_SOOT), middle.x, middle.y + 0.4, middle.z, 14,
						ChapelRules.REPOSE_RADIUS * 0.6, 0.1, ChapelRules.REPOSE_RADIUS * 0.6, 0.01);
			} else if (frost) {
				double spread = ChapelRules.REPOSE_RADIUS * 0.6;
				for (int i = 0; i < 24; i++) {
					settleFrost(level, middle.x + (level.random.nextDouble() * 2 - 1) * spread,
							middle.y + 0.15 + level.random.nextDouble() * 0.5,
							middle.z + (level.random.nextDouble() * 2 - 1) * spread);
				}
			} else {
				level.sendParticles(HemoParticleData.glow(PALE), middle.x, middle.y + 0.2, middle.z, 30,
						ChapelRules.REPOSE_RADIUS * 0.6, 0.05, ChapelRules.REPOSE_RADIUS * 0.6, 0.01);
			}
		}
		if (progress >= ChapelRules.REPOSE_TICKS) finish(challenger);
	}

	/** One cold mote and a grain of frost that sinks from it (a count of 0 makes the offsets its motion). */
	private static void settleFrost(ServerLevel level, double x, double y, double z) {
		level.sendParticles(HemoParticleData.glow(FROST_MOTE), x, y, z, 1, 0.06, 0.04, 0.06, 0.0);
		level.sendParticles(HemoParticleData.emberSpark(FROST_FINE, 0.07f, 0.7f, 50), x, y, z, 0, 0.0, -1.0, 0.0,
				0.012);
	}

	private void fail(@Nullable Player player) {
		if (level instanceof ServerLevel server) {
			for (UUID id : wave) {
				Entity mob = server.getEntity(id);
				if (mob != null) mob.discard();
			}
		}
		wave.clear();
		phase = Phase.IDLE;
		challenger = null;
		cooldown = ChapelRules.FAIL_COOLDOWN_TICKS;
		restSetPieces(false);
		if (player != null) tell(player, "message.hemomancy.chapel.failed", ChatFormatting.DARK_RED);
		if (level != null) level.playSound(null, worldPosition, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),
				SoundSource.BLOCKS, 0.8f, 0.6f);
		setChanged();
	}

	private void finish(@Nullable Player player) {
		complete = true;
		phase = Phase.IDLE;
		challenger = null;
		wave.clear();
		restSetPieces(true);
		if (level != null) {
			level.setBlock(worldPosition, getBlockState().setValue(ChapelFocusBlock.LIT, true), Block.UPDATE_ALL);
			level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 0.7f);
			if (level instanceof ServerLevel server) {
				server.sendParticles(HemoParticleData.glow(CRIMSON), worldPosition.getX() + 0.5,
						worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5, 60, 0.4, 1.0, 0.4, 0.03);
				RingDoorBlockEntity.chapelCompleted(server, worldPosition);
			}
		}
		if (player != null) tell(player, "message.hemomancy.chapel.completed", ChatFormatting.GOLD);
		BlockEntitySync.markAndSync(this);
	}

	/** Lights the witness {@code lit} (an index among the witnesses) and darkens the rest; -1 darkens all. */
	private void lightWitnesses(int lit) {
		List<Mark> witnesses = marksOf(MarkKind.WITNESS);
		for (int i = 0; i < witnesses.size(); i++) {
			setOnMark(witnesses.get(i), ChapelFocusBlockEntity::isWitness, WitnessStoneBlock.LIT, i == lit);
		}
	}

	/** A witness mark stands on a witness stone or, in Velorum's Procession, on a witness devotee's lower half. */
	private static boolean isWitness(BlockState state) {
		return state.is(BlockInit.witness_stone.get()) || state.is(BlockInit.witness_devotee.get());
	}

	/** The seals of the wave about to rise seep while it waits on the challenger; every other seal is quiet. */
	private void markDueSeals() {
		boolean waiting = phase == Phase.ACTIVE && wave.isEmpty() && step < ChapelRules.DOWNFALL_WAVES;
		List<Mark> due = waiting ? ChapelRules.seals(marks, step) : List.of();
		for (Mark seal : marksOf(MarkKind.SPAWN)) {
			setOnMark(seal, BlockInit.spawn_mark.get(), SpawnMarkBlock.DUE, due.contains(seal));
		}
	}

	private void chargePylons(int charge) {
		for (Mark pylon : marksOf(MarkKind.PYLON)) {
			setOnMark(pylon, BlockInit.pulse_pylon.get(), PulsePylonBlock.CHARGE, charge);
		}
	}

	/** The set pieces at rest when a trial ends; a completed Beholding leaves its witnesses' eyes open. */
	private void restSetPieces(boolean completed) {
		List<Mark> witnesses = marksOf(MarkKind.WITNESS);
		for (Mark witness : witnesses) {
			setOnMark(witness, ChapelFocusBlockEntity::isWitness, WitnessStoneBlock.LIT, completed);
		}
		markDueSeals();
		chargePylons(0);
	}

	/** Sets a property on the set piece at a mark, when the mark stands on that set piece. */
	private <T extends Comparable<T>> void setOnMark(Mark mark, Block piece, Property<T> property, T value) {
		setOnMark(mark, state -> state.is(piece), property, value);
	}

	/** Sets a property on the block at a mark, when that block is one of the set pieces {@code piece} accepts. */
	private <T extends Comparable<T>> void setOnMark(Mark mark, Predicate<BlockState> piece, Property<T> property,
			T value) {
		if (level == null || level.isClientSide) return;
		BlockPos at = markPos(mark);
		BlockState state = level.getBlockState(at);
		if (piece.test(state) && !state.getValue(property).equals(value)) {
			level.setBlock(at, state.setValue(property, value), Block.UPDATE_CLIENTS);
		}
	}

	private void chime(BlockPos at, float pitch) {
		if (level != null) level.playSound(null, at, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1f, pitch);
	}

	private static void tell(Player player, String key, ChatFormatting colour) {
		player.displayClientMessage(Component.translatable(key).withStyle(colour, ChatFormatting.ITALIC), true);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putString("Kind", kind.name());
		tag.putInt("Slot", slot);
		tag.putString("Saint", saint);
		tag.putBoolean("Complete", complete);
		tag.putString("Phase", phase.name());
		if (challenger != null) tag.putUUID("Challenger", challenger);
		tag.putLong("Seed", seed);
		tag.putInt("Step", step);
		tag.putInt("Progress", progress);
		ListTag mobs = new ListTag();
		for (UUID id : wave) mobs.add(NbtUtils.createUUID(id));
		tag.put("Wave", mobs);
		ListTag marked = new ListTag();
		for (Mark mark : marks) {
			CompoundTag entry = new CompoundTag();
			entry.putInt("x", mark.offset().getX());
			entry.putInt("y", mark.offset().getY());
			entry.putInt("z", mark.offset().getZ());
			entry.putString("Kind", mark.kind().name());
			entry.putInt("Index", mark.index());
			marked.add(entry);
		}
		tag.put("Marks", marked);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		try {
			kind = ChapelRules.Kind.valueOf(tag.getString("Kind"));
		} catch (IllegalArgumentException unknown) {
			kind = ChapelRules.Kind.BEHOLDING;
		}
		slot = tag.getInt("Slot");
		if (tag.contains("Saint")) saint = tag.getString("Saint");
		complete = tag.getBoolean("Complete");
		try {
			phase = Phase.valueOf(tag.getString("Phase"));
		} catch (IllegalArgumentException unknown) {
			phase = Phase.IDLE;
		}
		challenger = tag.hasUUID("Challenger") ? tag.getUUID("Challenger") : null;
		seed = tag.getLong("Seed");
		step = tag.getInt("Step");
		progress = tag.getInt("Progress");
		wave.clear();
		for (Tag id : tag.getList("Wave", Tag.TAG_INT_ARRAY)) wave.add(NbtUtils.loadUUID(id));
		marks.clear();
		for (Tag element : tag.getList("Marks", Tag.TAG_COMPOUND)) {
			CompoundTag entry = (CompoundTag) element;
			try {
				marks.add(new Mark(new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z")),
						MarkKind.valueOf(entry.getString("Kind")), entry.getInt("Index")));
			} catch (IllegalArgumentException unknown) {
				// A mark kind this version does not know is left out rather than guessed at.
			}
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		tag.putString("Kind", kind.name());
		tag.putInt("Slot", slot);
		tag.putBoolean("Complete", complete);
		return tag;
	}
}
