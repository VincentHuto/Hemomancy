package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.HematicAcclimationRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.PowerGuardrailState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCharge;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.mission.alchemist.BloodResearch;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.BloodDrawRights;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A creature kept as a block (core-loop Track 5): the saved entity in a Specimen Jar or Specimen Vat, and what is left
 * of it, its Vigor. The vessel keeps a small living buffer (an {@code IBloodReservoir} that never clots); each second
 * the buffer feeds the creature a point of Vigor at a price its body sets, and Vigor falls slowly on its own. Holding
 * Absorption on the vessel draws: Temper in the creature's schools and a little blood, scaled by Vigor and cut by the
 * same acclimation rule as a wild creature, at a step of Vigor a pull. At zero the creature dies and the vessel holds
 * its husk. A hostile creature low on Vigor rattles its glass, and a strong one may break out.
 *
 * Only a kept creature lives this way: a vessel nobody placed (one a structure set down) has no owner, and its
 * specimen is preserved, neither fading nor giving.
 */
public final class KeptSpecimen {
	public static final String TAG_VIGOR = "Vigor";
	public static final String TAG_SETTLED = "VigorSettledAt";
	public static final String TAG_TENDED = "TendedUntil";
	public static final String TAG_HUSK = "Husk";
	public static final String TAG_MAX_HEALTH = "KeptMaxHealth";
	/** The brood (P6): the mate's vessel, whether this one bears, what the brood has been paid, and rest after a birth. */
	public static final String TAG_BROOD_MATE = "BroodMate";
	public static final String TAG_BROOD_BEARER = "BroodBearer";
	public static final String TAG_BROOD_PAID = "BroodPaid";
	public static final String TAG_BROOD_REST = "BroodRestUntil";
	private static final long UNSET = Long.MIN_VALUE;

	private CompoundTag specimen = new CompoundTag();
	private double vigor = VatVigorRules.CAPTURED;
	private long settledAt = UNSET;
	private long tendedUntil = UNSET;
	private boolean husk;
	private float maxHealth = -1.0F;
	private long lastDraw = UNSET;
	private int revision;
	/** Client only: when the creature last rattled its glass. */
	private long rattledAt = UNSET;
	/** The brood: the mate's vessel (unset when unpaired), whether this vessel bears, and what has been paid. */
	private long broodMate = UNSET;
	private boolean broodBearer;
	private double broodPaid;
	private long restUntil = UNSET;
	/** Client only: when a young last quickened in this vessel. */
	private long quickenedAt = UNSET;

	// ── State ──

	public boolean occupied() {
		return !specimen.isEmpty();
	}

	public boolean living() {
		return occupied() && !husk;
	}

	public boolean husk() {
		return occupied() && husk;
	}

	public CompoundTag specimen() {
		return specimen.copy();
	}

	/** Changes whenever the specimen or its husk state changes; the client keys its display entity on it. */
	public int revision() {
		return revision;
	}

	public double vigor() {
		return vigor;
	}

	@Nullable
	public EntityType<?> type() {
		return SpecimenJarData.getSpecimenEntityId(specimen).flatMap(BuiltInRegistries.ENTITY_TYPE::getOptional).orElse(null);
	}

	@Nullable
	public ResourceLocation typeId() {
		return SpecimenJarData.getSpecimenEntityId(specimen).orElse(null);
	}

	public boolean hostile() {
		EntityType<?> type = type();
		return type != null && type.getCategory() == MobCategory.MONSTER;
	}

	/** The creature's maximum health, as recorded when it was kept or read back from its saved attributes. */
	public float maxHealth() {
		if (maxHealth < 0.0F) maxHealth = savedMaxHealth(specimen);
		return maxHealth;
	}

	public List<EnumBloodTemper> schools(boolean clientSide) {
		return BloodProfileData.profile(type(), clientSide).tempers();
	}

	/**
	 * The weak second school this creature was born with as the offspring of a tainted parent, read from its saved
	 * {@code hemomancy:animal_taint} attachment, or null. A wild one gives it at half a share; so does a kept one.
	 */
	@Nullable
	public EnumBloodTemper inheritedSchool() {
		CompoundTag attachments = specimen.getCompound("neoforge:attachments");
		String name = attachments.getCompound("hemomancy:animal_taint").getString("inherited");
		if (name.isBlank()) return null;
		try {
			return EnumBloodTemper.valueOf(name.toUpperCase(java.util.Locale.ROOT));
		} catch (IllegalArgumentException unknown) {
			return null;
		}
	}

	/** Takes in a creature: its save, its maximum health, and a weakened creature's starting Vigor. */
	public void keep(CompoundTag saved, float maxHealth, double startVigor, long now) {
		specimen = saved == null ? new CompoundTag() : saved.copy();
		this.maxHealth = maxHealth;
		vigor = VatVigorRules.bounded(startVigor);
		settledAt = now;
		tendedUntil = UNSET;
		husk = false;
		lastDraw = UNSET;
		unpair();
		restUntil = UNSET;
		revision++;
	}

	public void clear() {
		specimen = new CompoundTag();
		vigor = VatVigorRules.CAPTURED;
		settledAt = UNSET;
		husk = false;
		maxHealth = -1.0F;
		unpair();
		restUntil = UNSET;
		revision++;
	}

	/** Brings Vigor to {@code now}. A specimen nobody keeps is preserved and only has its clock moved. */
	public void settle(long now, boolean kept) {
		if (!living()) {
			settledAt = now;
			return;
		}
		if (settledAt == UNSET || !kept) {
			settledAt = now;
			return;
		}
		vigor = VatVigorRules.settle(vigor, settledAt, now, tendedUntil);
		settledAt = now;
	}

	/** A Sanguine Vigil keeps this creature's Vigor from falling until {@code until}. */
	public void tendUntil(long until) {
		tendedUntil = Math.max(tendedUntil == UNSET ? until : tendedUntil, until);
	}

	public boolean tended(long now) {
		return tendedUntil != UNSET && tendedUntil >= now;
	}

	/** Client: the creature rattles its glass now (a block event). */
	public void rattle(long now) {
		rattledAt = now;
	}

	/** Client: how hard the creature is shaking, 1 just after a rattle and 0 after half a second. */
	public float shake(long now, float partialTick) {
		if (rattledAt == UNSET) return 0.0F;
		float age = (now - rattledAt) + partialTick;
		return age < 0.0F || age > 10.0F ? 0.0F : 1.0F - age / 10.0F;
	}

	/** For tests and the review harness: sets Vigor now. */
	public void setVigor(double vigor, long now) {
		this.vigor = VatVigorRules.bounded(vigor);
		settledAt = now;
	}

	/** Brings Vigor to {@code now} and takes {@code amount} from it: the brood's toll on a parent. */
	public void spendVigor(double amount, long now) {
		settle(now, true);
		vigor = VatVigorRules.bounded(vigor - amount);
	}

	// ── The brood (P6) ──

	public boolean paired() {
		return broodMate != UNSET;
	}

	/** The mate's vessel, or null when unpaired. */
	@Nullable
	public BlockPos broodMate() {
		return paired() ? BlockPos.of(broodMate) : null;
	}

	/** Whether this vessel bears the pair's young (and pays for it). */
	public boolean broodBearer() {
		return paired() && broodBearer;
	}

	/** What the brood has been paid so far, kept on the bearer. */
	public double broodPaid() {
		return broodPaid;
	}

	public long restUntil() {
		return restUntil;
	}

	public boolean resting(long now) {
		return restUntil != UNSET && now < restUntil;
	}

	public void pair(BlockPos mate, boolean bearer) {
		broodMate = mate.asLong();
		broodBearer = bearer;
		broodPaid = 0.0D;
	}

	/** Ends any pairing; what was paid is lost (rule 2: nothing is stored for the young). */
	public void unpair() {
		broodMate = UNSET;
		broodBearer = false;
		broodPaid = 0.0D;
	}

	public void restUntil(long until) {
		restUntil = until;
	}

	/**
	 * Pays {@code amount} of the brood from {@code buffer} (the bearer's own living buffer or its mate's) when the
	 * buffer holds it, and returns whether it was paid. A block tank feeding the brood, not a player's volume.
	 */
	public boolean payBrood(BlockBloodVolume buffer, double amount) {
		if (amount <= 0.0D || buffer.getBloodVolume() < amount) return false;
		buffer.drain(amount);
		broodPaid += amount;
		return true;
	}

	/** Client: a young quickened here now (a block event). */
	public void quicken(long now) {
		quickenedAt = now;
	}

	/** Client: how bright the quickening is, 1 at the birth and 0 a second and a half later. */
	public float quickening(long now, float partialTick) {
		if (quickenedAt == UNSET) return 0.0F;
		float age = (now - quickenedAt) + partialTick;
		return age < 0.0F || age > 30.0F ? 0.0F : 1.0F - age / 30.0F;
	}

	// ── The vessel's second ──

	/**
	 * Once a second: settle, feed a point of Vigor from the buffer, die at zero, rattle and break out. Returns true when
	 * anything the client draws changed.
	 */
	public boolean tick(ServerLevel level, BlockPos pos, BlockEntity vessel) {
		BlockBloodVolume buffer = ReservoirBlood.settled(vessel);
		boolean kept = buffer.owner() != null;
		long now = level.getGameTime();
		settle(now, kept);
		if (!living() || !kept) return false;
		boolean changed = false;
		double price = VatVigorRules.mlPerVigor(maxHealth());
		if (vigor <= VatVigorRules.MAX - 1.0D && buffer.getBloodVolume() >= price) {
			// The vessel's own living buffer feeding its creature: a block tank, not a player's volume.
			buffer.drain(price);
			vigor = VatVigorRules.bounded(vigor + 1.0D);
			changed = true;
		}
		if (vigor <= 0.0D) {
			die(level, pos);
			return true;
		}
		boolean hostile = hostile();
		RandomSource random = level.random;
		if (VatVigorRules.rattles(vigor, hostile) && random.nextDouble() < VatVigorRules.RATTLE_CHANCE) {
			level.playSound(null, pos, SoundEvents.GLASS_HIT, SoundSource.BLOCKS, 0.9F, 0.6F + random.nextFloat() * 0.3F);
			level.blockEvent(pos, vessel.getBlockState().getBlock(), 1, 0);
		}
		if (VatVigorRules.mayBreakOut(vigor, hostile, maxHealth()) && random.nextDouble() < VatVigorRules.BREAKOUT_CHANCE) {
			breakOut(level, pos, buffer);
			return true;
		}
		return changed;
	}

	private void die(ServerLevel level, BlockPos pos) {
		BlockPos mate = broodMate();
		husk = true;
		vigor = 0.0D;
		unpair();
		SpecimenBrood.releaseMate(level, mate, pos);
		revision++;
		level.playSound(null, pos, SoundEvents.HONEY_BLOCK_BREAK, SoundSource.BLOCKS, 0.8F, 0.6F);
		level.sendParticles(HemoParticleData.darkGlow(new ParticleColor(70, 20, 18)), pos.getX() + 0.5D, pos.getY() + 0.6D,
				pos.getZ() + 0.5D, 12, 0.2D, 0.3D, 0.2D, 0.01D);
	}

	/** The creature forces the glass: it is set free beside the vessel, angry at its keeper if they are near. */
	public Optional<Entity> breakOut(ServerLevel level, BlockPos pos, BlockBloodVolume buffer) {
		CompoundTag saved = specimen.copy();
		Optional<Entity> freed = SpecimenJarData.releaseSpecimen(level, pos, saved);
		if (freed.isEmpty()) return freed;
		BlockPos mate = broodMate();
		clear();
		SpecimenBrood.releaseMate(level, mate, pos);
		level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 0.8F);
		if (freed.get() instanceof Mob mob && buffer.owner() != null
				&& level.getPlayerByUUID(buffer.owner()) instanceof ServerPlayer keeper
				&& keeper.distanceToSqr(mob) < 32.0D * 32.0D && !keeper.isCreative()) {
			mob.setTarget(keeper);
			keeper.displayClientMessage(Component.translatable("message.hemomancy.menagerie.broke_out", mob.getDisplayName())
					.withStyle(ChatFormatting.RED), true);
		}
		return freed;
	}

	// ── Absorption ──

	/** What one held Absorption tick on the vessel does; see {@link #draw}. */
	public enum DrawOutcome { PULLED, WAITING, EMPTY, HUSK, PRESERVED, NOT_YOURS, SPENT, DORMANT }

	/**
	 * One tick of held Absorption on the vessel. A pull happens once every {@link VatVigorRules#DRAW_INTERVAL} ticks;
	 * the ticks between are {@link DrawOutcome#WAITING}, which still hold the channel on the vessel.
	 */
	public DrawOutcome draw(ServerLevel level, ServerPlayer player, BlockEntity vessel) {
		if (!occupied()) return DrawOutcome.EMPTY;
		if (husk) return DrawOutcome.HUSK;
		BlockBloodVolume buffer = ReservoirBlood.settled(vessel);
		if (buffer.owner() == null) return DrawOutcome.PRESERVED;
		if (!BloodDrawRights.mayDraw(player, vessel)) return DrawOutcome.NOT_YOURS;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (volume == null || !volume.isActive()) return DrawOutcome.DORMANT;
		long now = level.getGameTime();
		if (lastDraw != UNSET && now - lastDraw < VatVigorRules.DRAW_INTERVAL && now >= lastDraw) return DrawOutcome.WAITING;
		settle(now, true);
		if (!VatVigorRules.drawable(vigor)) return DrawOutcome.SPENT;
		lastDraw = now;
		String typeId = String.valueOf(typeId());
		PowerGuardrailState guardrails = HemoCapabilityAccess.getPowerGuardrails(player);
		double acclimation = HematicAcclimationRules.multiplier(guardrails.hematicExposure(typeId, now));
		// An unread kind gives half its Temper (CL-12); the blood is unchanged.
		double temper = VatVigorRules.temperPerDraw(vigor, acclimation)
				* BloodResearch.temperShare(BloodResearch.knows(player, typeId()));
		double blood = VatVigorRules.bloodPerDraw(vigor, acclimation);
		vigor = VatVigorRules.bounded(vigor - VatVigorRules.DRAW_STEP);
		List<EnumBloodTemper> schools = schools(false);
		TemperCharge.fillFromKept(player, schools, temper);
		EnumBloodTemper inherited = inheritedSchool();
		if (inherited != null && !schools.contains(inherited)) {
			TemperCharge.fillFromKept(player, List.of(inherited), 0.5D * temper / Math.max(1, schools.size()));
		}
		if (blood > 0.0D) {
			BloodFlowLedger.applyDirectIncome(player, volume, "menagerie_draw", "blood_source.hemomancy.menagerie_draw",
					BloodFlowContribution.Category.VESSEL, blood, VatVigorRules.DRAW_INTERVAL);
		}
		// What was drawn counts toward acclimation like blood from a wild creature: ten cows are not ten times one cow.
		guardrails.recordHematicExposure(typeId, temper + blood, now);
		if (now % 20L < VatVigorRules.DRAW_INTERVAL) {
			level.playSound(null, vessel.getBlockPos(), SoundEvents.HONEY_DRINK, SoundSource.BLOCKS, 0.4F, 0.7F);
		}
		BlockEntitySync.markAndSync(vessel);
		return DrawOutcome.PULLED;
	}

	/**
	 * A vial drawn from the kept creature: it pays in Vigor, not health. Returns the sampled creature's type id, or
	 * null when nothing could be drawn.
	 */
	@Nullable
	public ResourceLocation sample(ServerLevel level, BlockEntity vessel) {
		return sample(level, vessel, VatVigorRules.SAMPLE_COST);
	}

	/** A sample paid with {@code cost} Vigor: a vial's {@link VatVigorRules#SAMPLE_COST}, a syringe's less. */
	@Nullable
	public ResourceLocation sample(ServerLevel level, BlockEntity vessel, double cost) {
		if (!living()) return null;
		settle(level.getGameTime(), ReservoirBlood.settled(vessel).owner() != null);
		if (vigor < cost) return null;
		vigor = VatVigorRules.bounded(vigor - cost);
		BlockEntitySync.markAndSync(vessel);
		return typeId();
	}

	/** The husk's leavings: bone meal and a ruinous enzyme. Clears the vessel. */
	public List<ItemStack> takeHusk() {
		if (!husk()) return List.of();
		clear();
		return List.of(new ItemStack(net.minecraft.world.item.Items.BONE_MEAL, 3), new ItemStack(ItemInit.ruinous_enzyme.get()));
	}

	// ── Saving ──

	public void save(CompoundTag tag) {
		if (specimen.isEmpty()) return;
		tag.put(SpecimenJarData.TAG_SPECIMEN, specimen.copy());
		tag.putDouble(TAG_VIGOR, vigor);
		if (settledAt != UNSET) tag.putLong(TAG_SETTLED, settledAt);
		if (tendedUntil != UNSET) tag.putLong(TAG_TENDED, tendedUntil);
		if (husk) tag.putBoolean(TAG_HUSK, true);
		if (maxHealth >= 0.0F) tag.putFloat(TAG_MAX_HEALTH, maxHealth);
		if (paired()) {
			tag.putLong(TAG_BROOD_MATE, broodMate);
			if (broodBearer) tag.putBoolean(TAG_BROOD_BEARER, true);
			if (broodPaid > 0.0D) tag.putDouble(TAG_BROOD_PAID, broodPaid);
		}
		if (restUntil != UNSET) tag.putLong(TAG_BROOD_REST, restUntil);
	}

	public void load(CompoundTag tag) {
		CompoundTag loaded = tag.contains(SpecimenJarData.TAG_SPECIMEN, Tag.TAG_COMPOUND)
				? tag.getCompound(SpecimenJarData.TAG_SPECIMEN).copy() : new CompoundTag();
		boolean changed = !loaded.equals(specimen) || husk != tag.getBoolean(TAG_HUSK);
		specimen = loaded;
		vigor = tag.contains(TAG_VIGOR) ? VatVigorRules.bounded(tag.getDouble(TAG_VIGOR)) : VatVigorRules.CAPTURED;
		settledAt = tag.contains(TAG_SETTLED) ? tag.getLong(TAG_SETTLED) : UNSET;
		tendedUntil = tag.contains(TAG_TENDED) ? tag.getLong(TAG_TENDED) : UNSET;
		husk = tag.getBoolean(TAG_HUSK);
		maxHealth = tag.contains(TAG_MAX_HEALTH) ? tag.getFloat(TAG_MAX_HEALTH) : -1.0F;
		broodMate = tag.contains(TAG_BROOD_MATE) ? tag.getLong(TAG_BROOD_MATE) : UNSET;
		broodBearer = broodMate != UNSET && tag.getBoolean(TAG_BROOD_BEARER);
		broodPaid = broodMate != UNSET ? Math.max(0.0D, tag.getDouble(TAG_BROOD_PAID)) : 0.0D;
		restUntil = tag.contains(TAG_BROOD_REST) ? tag.getLong(TAG_BROOD_REST) : UNSET;
		// The brood changes nothing the display entity is built from, so it never bumps the revision.
		if (changed) revision++;
	}

	/**
	 * Writes the specimen with its Vigor into an item, so a vessel picked up and set down keeps its creature as it was.
	 * A rest after a birth goes with it; a pairing does not (a vessel lifted away loses its brood).
	 */
	public void saveToItem(ItemStack stack) {
		SpecimenJarData.setSpecimen(stack, specimen);
		SpecimenJarData.setKeptState(stack, vigor, husk, maxHealth, restUntil);
	}

	/** Reads a vessel item's creature back; a jar filled before Vigor existed starts at {@link VatVigorRules#CAPTURED}. */
	public void loadFromItem(ItemStack stack, long now) {
		CompoundTag saved = SpecimenJarData.getSpecimen(stack);
		CompoundTag kept = SpecimenJarData.getKeptState(stack);
		keep(saved, kept.contains(TAG_MAX_HEALTH) ? kept.getFloat(TAG_MAX_HEALTH) : savedMaxHealth(saved),
				kept.contains(TAG_VIGOR) ? kept.getDouble(TAG_VIGOR) : VatVigorRules.CAPTURED, now);
		husk = !saved.isEmpty() && kept.getBoolean(TAG_HUSK);
		if (husk) vigor = 0.0D;
		if (kept.contains(TAG_BROOD_REST)) restUntil = kept.getLong(TAG_BROOD_REST);
	}

	/** A saved creature's maximum health from its attributes, or its health, or 10 when neither was saved. */
	public static float savedMaxHealth(CompoundTag saved) {
		if (saved == null || saved.isEmpty()) return 0.0F;
		ListTag attributes = saved.getList("attributes", Tag.TAG_COMPOUND);
		for (int i = 0; i < attributes.size(); i++) {
			CompoundTag attribute = attributes.getCompound(i);
			if ("minecraft:generic.max_health".equals(attribute.getString("id"))) return (float) attribute.getDouble("base");
		}
		return saved.contains("Health") ? Math.max(1.0F, saved.getFloat("Health")) : 10.0F;
	}

	/** The keeper of {@code vessel} when they are online in its level, else null. */
	@Nullable
	public static Player keeper(ServerLevel level, BlockEntity vessel) {
		var owner = ReservoirBlood.settled(vessel).owner();
		return owner == null ? null : level.getPlayerByUUID(owner);
	}

	/** Records a creature's maximum health for {@link #keep}; a non-living entity counts as 10. */
	public static float maxHealthOf(Entity entity) {
		return entity instanceof LivingEntity living ? living.getMaxHealth() : 10.0F;
	}

	/** Drops the husk's leavings at the vessel, for a vessel broken while it holds one. */
	public void dropHusk(ServerLevel level, BlockPos pos) {
		for (ItemStack stack : takeHusk()) Block.popResource(level, pos, stack);
	}
}
