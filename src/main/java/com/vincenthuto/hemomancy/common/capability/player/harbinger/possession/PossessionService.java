package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.entity.PossessionEchoEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperEvents;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import com.vincenthuto.hemomancy.common.util.FlightArbiter;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Begins and ends a Silentium's possession. Beginning stills the surrogate, leaves the echo, saves the inventory and
 * carries the will into the Gardens; ending (clean or broken) reverses every step, so nothing is ever left behind
 * except what the spec leaves on purpose: a broken surrogate dies, and an armor stand's gear stays in the Gardens.
 * Both are staged through {@link PossessionSequences} so the cinematics play; the steps here are what its cut and
 * settle ticks run. Logout and login recovery use the immediate {@link #returnNow}.
 */
public final class PossessionService {
	private static final ResourceLocation SPEED = Hemomancy.rloc("possession_speed");
	private static final ResourceLocation JUMP = Hemomancy.rloc("possession_jump");
	private static final ResourceLocation KNOCKBACK = Hemomancy.rloc("possession_knockback");
	private static final ParticleColor SHADOW = new ParticleColor(40, 30, 48);
	private static final int[] ROOM_HEIGHTS = {0, 1, -1, 2, -2, 3};
	private static final EquipmentSlot[] GEAR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
			EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};

	private PossessionService() {}

	/** The surrogate nearest the rite's centre, or null: only the nine exact vanilla types count. */
	@Nullable
	public static LivingEntity findSurrogate(ServerLevel level, AABB bounds, BlockPos center) {
		SurrogateLedger ledger = SurrogateLedger.get(level.getServer());
		return level.getEntitiesOfClass(LivingEntity.class, bounds, entity -> !(entity instanceof Player)
						&& entity.isAlive() && formOf(entity) != null && !ledger.isHeld(entity.getUUID()))
				.stream().min(Comparator.comparingDouble(entity -> entity.distanceToSqr(Vec3.atCenterOf(center))))
				.orElse(null);
	}

	/**
	 * The surrogate the rite takes for {@code caster}: the nearest one their Blood Binding holds, or, when none is held,
	 * the nearest one at all, so the refusal can say it is not bound.
	 */
	@Nullable
	public static LivingEntity findSurrogate(ServerLevel level, AABB bounds, BlockPos center, ServerPlayer caster) {
		SurrogateLedger ledger = SurrogateLedger.get(level.getServer());
		LivingEntity held = level.getEntitiesOfClass(LivingEntity.class, bounds, entity -> !(entity instanceof Player)
						&& entity.isAlive() && formOf(entity) != null && !ledger.isHeld(entity.getUUID())
						&& holds(caster, entity))
				.stream().min(Comparator.comparingDouble(entity -> entity.distanceToSqr(Vec3.atCenterOf(center))))
				.orElse(null);
		return held != null ? held : findSurrogate(level, bounds, center);
	}

	/** Whether the caster holds this surrogate: bound by their Blood Binding, or an armor stand, which cannot wander. */
	public static boolean holds(ServerPlayer caster, LivingEntity surrogate) {
		return formOf(surrogate) == SurrogateForm.ARMOR_STAND || SurrogateTethers.boundBy(surrogate, caster.getUUID());
	}

	@Nullable
	public static SurrogateForm formOf(Entity entity) {
		return SurrogateForm.fromEntityTypeId(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
	}

	/** A pet belongs to its owner: another player's tamed mob is never a surrogate. */
	public static boolean mayTake(ServerPlayer caster, LivingEntity surrogate) {
		return !(surrogate instanceof net.minecraft.world.entity.OwnableEntity pet) || pet.getOwnerUUID() == null
				|| pet.getOwnerUUID().equals(caster.getUUID());
	}

	/** Why this caster cannot begin a possession here, or null. */
	@Nullable
	public static Component refusal(ServerPlayer caster, @Nullable LivingEntity surrogate) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(caster);
		boolean silentium = com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.HarbingerPathPermissions
				.isSilentium(HemoCapabilityAccess.getPlayerDegreeNumber(caster), degree.getArchonPath());
		if (!silentium) return Component.translatable("message.hemomancy.possession.not_silentium");
		if (caster.hasEffect(EffectInit.mind_broken)) return Component.translatable("message.hemomancy.possession.mind_broken");
		if (HemoCapabilityAccess.isPossessing(caster)) return Component.translatable("message.hemomancy.possession.already");
		if (surrogate == null) return Component.translatable("message.hemomancy.possession.no_surrogate");
		if (!mayTake(caster, surrogate)) return Component.translatable("message.hemomancy.possession.not_your_pet");
		if (!holds(caster, surrogate)) return Component.translatable("message.hemomancy.possession.not_bound");
		if (com.vincenthuto.hemomancy.common.encounter.ChamberVisitService.isActive(caster)
				|| FungalGardenTravelHelper.isProjectionActive(caster))
			return Component.translatable("message.hemomancy.possession.elsewhere");
		if (caster.server.getLevel(FungalGardenTravelHelper.FUNGAL_GARDENS) == null)
			return Component.translatable("message.hemomancy.possession.no_gardens");
		return null;
	}

	/** As {@link #begin(ServerLevel, ServerPlayer, LivingEntity, PossessionRules.Offerings, BlockPos)}, framed on the caster. */
	public static boolean begin(ServerLevel level, ServerPlayer caster, LivingEntity surrogate,
			PossessionRules.Offerings offerings) {
		return begin(level, caster, surrogate, offerings, caster.blockPosition());
	}

	/**
	 * Begins the departure: the surrogate is stilled and held, and the echo rises under the caster in their own skin.
	 * The will crosses at the departure's cut ({@link #cross}), tuned by the offerings the rite's braziers burned;
	 * {@code site} is the rite's Focus, where the medium is spent.
	 */
	public static boolean begin(ServerLevel level, ServerPlayer caster, LivingEntity surrogate,
			PossessionRules.Offerings offerings, BlockPos site) {
		SurrogateForm form = formOf(surrogate);
		if (form == null || PossessionSequences.isStaged(caster) || refusal(caster, surrogate) != null) return false;

		PossessionEchoEntity echo = EntityInit.possession_echo.get().create(level);
		if (echo == null) return false;

		still(surrogate);
		SurrogateLedger.get(level.getServer()).hold(surrogate.getUUID(), caster.getUUID());
		echo.moveTo(caster.getX(), caster.getY(), caster.getZ(), caster.getYRot(), 0f);
		echo.setOwner(caster.getUUID());
		echo.setManifest(1.0F);
		level.addFreshEntity(echo);
		caster.closeContainer();

		HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(level, caster.getUUID(), site);
		Vec3 daemonFrom = daemon != null ? daemon.position() : Vec3.atBottomCenterOf(site).add(0.0, 2.5, 0.0);
		PossessionSequences.startBegin(caster, echo, surrogate, offerings, site, daemonFrom);
		return true;
	}

	/**
	 * The departure's cut: the inventory is set aside, the attachment begins and the will is carried into the Gardens.
	 * {@code from} and {@code yaw} are where the caster stood when the rite completed; the echo stands there.
	 */
	static boolean cross(ServerPlayer caster, LivingEntity surrogate, PossessionRules.Offerings offerings, Vec3 from,
			float yaw) {
		SurrogateForm form = formOf(surrogate);
		ServerLevel gardens = caster.server.getLevel(FungalGardenTravelHelper.FUNGAL_GARDENS);
		if (form == null || gardens == null || HemoCapabilityAccess.isPossessing(caster)) return false;
		ServerLevel level = caster.serverLevel();
		UUID echo = PossessionSequences.echoOf(caster);
		if (echo == null) return false;

		surrogate.setInvisible(true);
		List<ItemStack> standGear = new ArrayList<>();
		if (surrogate instanceof ArmorStand stand && form.wearsStandGear()) {
			for (EquipmentSlot slot : GEAR) {
				standGear.add(stand.getItemBySlot(slot).copy());
				stand.setItemSlot(slot, ItemStack.EMPTY);
			}
		}

		caster.closeContainer();
		caster.inventoryMenu.removed(caster);
		ListTag saved = new ListTag();
		if (!form.keepsInventory()) {
			saved = caster.getInventory().save(new ListTag());
			caster.getInventory().clearContent();
			caster.getInventory().selected = 0;
		}
		for (int i = 0; i < standGear.size(); i++) caster.setItemSlot(GEAR[i], standGear.get(i));

		HemoCapabilityAccess.getPossession(caster).begin(form, offerings.pool(form), offerings.shellHits(),
				offerings.regen(), offerings.totem(), surrogate.getUUID(), surrogate.level().dimension().location(), echo,
				level.dimension().location(), from, yaw, saved);

		BlockPos landing = FungalGardenTravelHelper.findSafePos(gardens, Mth.floor(from.x), Mth.floor(from.z));
		caster.teleportTo(gardens, landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5, yaw, caster.getXRot());
		applyForm(caster, form, true);
		caster.refreshDimensions();
		makeRoom(gardens, caster);
		HemoCapabilityAccess.getPossession(caster).sync(caster, true);
		caster.displayClientMessage(Component.translatable("message.hemomancy.possession.begin")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
		return true;
	}

	/** A clean return, staged: the body collapses in the Gardens and the caster re-manifests at the echo. */
	public static void returnClean(ServerPlayer player) {
		if (HemoCapabilityAccess.isPossessing(player)) PossessionSequences.startReturn(player, false, 0);
	}

	/** A snapped return, staged: the surrogate dies, and the caster is left Mind Broken for {@code mindBrokenTicks}. */
	public static void eject(ServerPlayer player, int mindBrokenTicks) {
		if (HemoCapabilityAccess.isPossessing(player)) PossessionSequences.startReturn(player, true, mindBrokenTicks);
	}

	/**
	 * Returns the will at once and without scenes, for logout and login recovery: any running sequence is ended
	 * first, then a possession still open is finished cleanly.
	 */
	public static void returnNow(ServerPlayer player) {
		PossessionSequences.abandon(player);
		if (HemoCapabilityAccess.isPossessing(player)) finish(player, false, 0, false);
	}

	/** As {@link #eject}, at once: for a blow nothing may absorb. */
	public static void ejectNow(ServerPlayer player, int mindBrokenTicks) {
		PossessionSequences.abandon(player);
		if (HemoCapabilityAccess.isPossessing(player)) finish(player, true, mindBrokenTicks, false);
	}

	/** A return's cut: everything but the echo and the surrogate, which wait for the settle tick. */
	static void finishStaged(ServerPlayer player, boolean broken, int mindBrokenTicks) {
		finish(player, broken, mindBrokenTicks, true);
	}

	private static void finish(ServerPlayer player, boolean broken, int mindBrokenTicks, boolean staged) {
		Possession possession = HemoCapabilityAccess.getPossession(player);
		SurrogateForm form = possession.form();
		if (form == null) return;
		ServerLevel here = player.serverLevel();

		player.closeContainer();
		player.inventoryMenu.removed(player);
		if (form.wearsStandGear()) leaveStandGear(here, player);
		List<ItemStack> found = new ArrayList<>();
		Inventory inventory = player.getInventory();
		if (!form.keepsInventory()) {
			for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
				ItemStack stack = inventory.getItem(slot);
				if (!stack.isEmpty()) found.add(stack.copy());
			}
			inventory.clearContent();
			inventory.load(possession.savedInventory());
		}
		found.addAll(possession.takeLegacyStash());
		applyForm(player, form, false);

		ServerLevel home = levelOr(player, possession.returnDimension(), player.server.overworld());
		Vec3 back = possession.returnDimension() == null ? Vec3.atBottomCenterOf(home.getSharedSpawnPos())
				: possession.returnPos();
		player.teleportTo(home, back.x, back.y, back.z, possession.returnYaw(), player.getXRot());
		for (ItemStack stack : found) {
			if (!inventory.add(stack) && !stack.isEmpty()) {
				home.addFreshEntity(new ItemEntity(home, back.x, back.y + 0.5, back.z, stack));
			}
		}

		if (!staged) {
			if (possession.echo() != null && home.getEntity(possession.echo()) instanceof PossessionEchoEntity echo) {
				echo.discard();
			}
			settleSurrogate(player.server, possession.surrogate(), possession.surrogateDimension(), broken);
		} else if (possession.surrogate() != null) {
			// Recorded now, so a crash before the settle tick still carries out the verdict.
			SurrogateLedger.get(player.server).settle(possession.surrogate(), broken);
		}

		possession.end();
		player.refreshDimensions();
		possession.sync(player, true);
		if (!staged) {
			home.sendParticles(HemoParticleData.glow(SHADOW), back.x, back.y + 1.0, back.z, 24, 0.3, 0.6, 0.3, 0.02);
			home.playSound(null, BlockPos.containing(back), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1f,
					broken ? 0.5f : 0.9f);
		}
		if (broken) {
			player.addEffect(new MobEffectInstance(EffectInit.mind_broken, mindBrokenTicks, 0, false, true, true));
			FungalWhisperEvents.triggerWhisper(player, player.getRandom().nextInt(3));
			player.displayClientMessage(Component.translatable("message.hemomancy.possession.broken")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		} else {
			player.displayClientMessage(Component.translatable("message.hemomancy.possession.returned")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
		}
	}

	/** Carries out a finished visit's verdict on its surrogate, now if it is loaded, or when it next loads. */
	static void settleSurrogate(MinecraftServer server, @Nullable UUID surrogate, @Nullable ResourceLocation dimension,
			boolean broken) {
		if (surrogate == null) return;
		SurrogateLedger ledger = SurrogateLedger.get(server);
		ledger.settle(surrogate, broken);
		ServerLevel level = dimension == null ? null
				: server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
		if (level != null && level.getEntity(surrogate) instanceof LivingEntity body) settle(body, ledger);
	}

	/**
	 * At login, a caster who is not possessing holds no surrogate: one still held for them was stilled by a departure
	 * a crash cut short, and is let go.
	 */
	public static void releaseOrphans(ServerPlayer player) {
		if (HemoCapabilityAccess.isPossessing(player) || PossessionSequences.isStaged(player)) return;
		SurrogateLedger ledger = SurrogateLedger.get(player.server);
		for (UUID body : ledger.heldBy(player.getUUID())) {
			ledger.settle(body, false);
			for (ServerLevel level : player.server.getAllLevels()) {
				if (level.getEntity(body) instanceof LivingEntity surrogate) settle(surrogate, ledger);
			}
		}
	}

	/**
	 * The landing is found for a player's size; a wide or tall body may not fit there. Moves the body to the nearest
	 * spot within a few blocks where all of it fits on solid ground, so it does not start inside a wall.
	 */
	private static void makeRoom(ServerLevel level, ServerPlayer player) {
		if (level.noCollision(player)) return;
		AABB box = player.getBoundingBox();
		Vec3 at = player.position();
		for (int radius = 0; radius <= 4; radius++) {
			for (int dy : ROOM_HEIGHTS) {
				for (int dx = -radius; dx <= radius; dx++) {
					for (int dz = -radius; dz <= radius; dz++) {
						if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
						Vec3 to = new Vec3(Math.floor(at.x) + dx + 0.5, Math.floor(at.y) + dy, Math.floor(at.z) + dz + 0.5);
						BlockPos floor = BlockPos.containing(to.x, to.y - 0.5, to.z);
						if (level.noCollision(player, box.move(to.subtract(at)))
								&& level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
							player.teleportTo(to.x, to.y, to.z);
							return;
						}
					}
				}
			}
		}
	}

	private static ServerLevel levelOr(ServerPlayer player, @Nullable ResourceLocation dimension, ServerLevel fallback) {
		if (dimension == null) return fallback;
		ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
		return level == null ? fallback : level;
	}

	/** The armor stand's gear stays where the facsimile stood, on a fresh stand: the Gardens keep what came in. */
	private static void leaveStandGear(ServerLevel level, ServerPlayer player) {
		ArmorStand stand = new ArmorStand(level, player.getX(), player.getY(), player.getZ());
		boolean any = false;
		for (EquipmentSlot slot : GEAR) {
			ItemStack worn = player.getItemBySlot(slot);
			if (worn.isEmpty()) continue;
			stand.setItemSlot(slot, worn.copy());
			player.setItemSlot(slot, ItemStack.EMPTY);
			any = true;
		}
		if (any) level.addFreshEntity(stand);
	}

	private static void still(LivingEntity surrogate) {
		if (surrogate instanceof Mob mob) {
			mob.setNoAi(true);
			mob.setPersistenceRequired();
			mob.setTarget(null);
		}
		surrogate.setInvulnerable(true);
		surrogate.setDeltaMovement(Vec3.ZERO);
	}

	/** Carries out a surrogate's verdict, if it has one that is not still held; called whenever it is seen. */
	public static void settle(LivingEntity body, SurrogateLedger ledger) {
		SurrogateLedger.Verdict verdict = ledger.verdict(body.getUUID());
		if (verdict == null || verdict == SurrogateLedger.Verdict.HELD) return;
		ledger.forget(body.getUUID());
		if (verdict == SurrogateLedger.Verdict.KILL) {
			body.setInvulnerable(false);
			body.kill();
		} else {
			release(body);
		}
	}

	static void release(LivingEntity surrogate) {
		SurrogateTethers.release(surrogate);
		if (surrogate instanceof Mob mob) mob.setNoAi(false);
		surrogate.setInvulnerable(false);
		surrogate.setInvisible(false);
	}

	/** Holds or drops the form's body: speed, jump, the golem's knockback, and flight for the winged forms. */
	public static void applyForm(Player player, SurrogateForm form, boolean present) {
		AttributeModifiers.sync(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED, form.speed(),
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, present && form.speed() != 0.0);
		AttributeModifiers.sync(player.getAttribute(Attributes.JUMP_STRENGTH), JUMP, form.jump(),
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, present && form.jump() != 0.0);
		AttributeModifiers.sync(player.getAttribute(Attributes.ATTACK_KNOCKBACK), KNOCKBACK, 1.5,
				AttributeModifier.Operation.ADD_VALUE, present && form == SurrogateForm.GOLEM);
		applyFlight(player, form, present);
	}

	/** A winged form flies while its Hover toggle is on; turning Hover off, or leaving the body, grounds it. */
	public static void applyFlight(Player player, SurrogateForm form, boolean present) {
		if (!form.flies()) return;
		if (present && HemoCapabilityAccess.getPossession(player).toggled(PossessionAction.HOVER)) {
			FlightArbiter.grant(player, FlightArbiter.Source.POSSESSION);
		} else {
			FlightArbiter.revoke(player, FlightArbiter.Source.POSSESSION);
		}
	}

	public static boolean inGardens(Level level) {
		return level.dimension().equals(FungalGardenTravelHelper.FUNGAL_GARDENS);
	}
}
