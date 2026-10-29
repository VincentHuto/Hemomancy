package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.entity.PossessionEchoEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import com.vincenthuto.hemomancy.common.util.FlightArbiter;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Begins and ends a Silentium's possession. Beginning stills the surrogate, leaves the echo, saves the inventory and
 * carries the will into the Gardens; ending (clean or broken) reverses every step, so nothing is ever left behind
 * except what the spec leaves on purpose: a broken surrogate dies, and an armor stand's gear stays in the Gardens.
 */
public final class PossessionService {
	private static final ResourceLocation SPEED = Hemomancy.rloc("possession_speed");
	private static final ResourceLocation JUMP = Hemomancy.rloc("possession_jump");
	private static final ResourceLocation KNOCKBACK = Hemomancy.rloc("possession_knockback");
	private static final ParticleColor SHADOW = new ParticleColor(40, 30, 48);
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
		if (com.vincenthuto.hemomancy.common.encounter.ChamberVisitService.isActive(caster)
				|| FungalGardenTravelHelper.isProjectionActive(caster))
			return Component.translatable("message.hemomancy.possession.elsewhere");
		if (caster.server.getLevel(FungalGardenTravelHelper.FUNGAL_GARDENS) == null)
			return Component.translatable("message.hemomancy.possession.no_gardens");
		return null;
	}

	/** Casts {@code caster}'s will into {@code surrogate}'s facsimile, consuming the offerings lying in {@code bounds}. */
	public static boolean begin(ServerLevel level, ServerPlayer caster, LivingEntity surrogate, AABB bounds) {
		SurrogateForm form = formOf(surrogate);
		ServerLevel gardens = caster.server.getLevel(FungalGardenTravelHelper.FUNGAL_GARDENS);
		if (form == null || gardens == null || refusal(caster, surrogate) != null) return false;

		PossessionEchoEntity echo = EntityInit.possession_echo.get().create(level);
		if (echo == null) return false;
		boolean regen = consumeOffering(level, bounds, ItemInit.mnemonic_ambergris.get().asItem());
		boolean shell = consumeOffering(level, bounds, BlockInit.hematic_iron_block.get().asItem());
		boolean totem = consumeOffering(level, bounds, Items.TOTEM_OF_UNDYING);
		boolean wax = consumeOffering(level, bounds, ItemInit.adipocere.get());

		still(surrogate);
		SurrogateLedger.get(level.getServer()).hold(surrogate.getUUID(), caster.getUUID());
		List<ItemStack> standGear = new ArrayList<>();
		if (surrogate instanceof ArmorStand stand && form.wearsStandGear()) {
			for (EquipmentSlot slot : GEAR) {
				standGear.add(stand.getItemBySlot(slot).copy());
				stand.setItemSlot(slot, ItemStack.EMPTY);
			}
		}

		echo.moveTo(caster.getX(), caster.getY(), caster.getZ(), caster.getYRot(), 0f);
		echo.setOwner(caster.getUUID());
		level.addFreshEntity(echo);
		level.sendParticles(HemoParticleData.glow(SHADOW), caster.getX(), caster.getY() + 1.0, caster.getZ(), 24,
				0.3, 0.6, 0.3, 0.02);

		caster.closeContainer();
		caster.inventoryMenu.removed(caster);
		ListTag saved = new ListTag();
		if (!form.keepsInventory()) {
			saved = caster.getInventory().save(new ListTag());
			caster.getInventory().clearContent();
			caster.getInventory().selected = 0;
		}
		for (int i = 0; i < standGear.size(); i++) caster.setItemSlot(GEAR[i], standGear.get(i));

		Vec3 from = caster.position();
		HemoCapabilityAccess.getPossession(caster).begin(form,
				form.pool() + (wax ? PossessionRules.ADIPOCERE_BONUS : 0), shell ? PossessionRules.SHELL_HITS : 0,
				regen, totem, surrogate.getUUID(), level.dimension().location(), echo.getUUID(),
				level.dimension().location(), from, caster.getYRot(), saved);

		BlockPos landing = FungalGardenTravelHelper.findSafePos(gardens, caster.getBlockX(), caster.getBlockZ());
		caster.teleportTo(gardens, landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5, caster.getYRot(),
				caster.getXRot());
		applyForm(caster, form, true);
		ItemStack thread = new ItemStack(ItemInit.thread_of_will.get());
		if (!caster.getInventory().add(thread)) {
			HemoCapabilityAccess.getPossession(caster).stash(caster.getInventory().getItem(0).copy());
			caster.getInventory().setItem(0, thread);
		}
		HemoCapabilityAccess.getPossession(caster).sync(caster, true);
		caster.displayClientMessage(Component.translatable("message.hemomancy.possession.begin")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
		return true;
	}

	/** A clean return: the surrogate is released unharmed. */
	public static void returnClean(ServerPlayer player) {
		finish(player, false, 0);
	}

	/** A snapped return: the surrogate dies, and the caster is left Mind Broken for {@code mindBrokenTicks}. */
	public static void eject(ServerPlayer player, int mindBrokenTicks) {
		finish(player, true, mindBrokenTicks);
	}

	private static void finish(ServerPlayer player, boolean broken, int mindBrokenTicks) {
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
				if (!stack.isEmpty() && !stack.is(ItemInit.thread_of_will.get())) found.add(stack.copy());
			}
			inventory.clearContent();
			inventory.load(possession.savedInventory());
		}
		removeThreads(inventory);
		found.addAll(possession.takeStash());
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

		if (possession.echo() != null && home.getEntity(possession.echo()) instanceof PossessionEchoEntity echo) {
			echo.discard();
		}
		if (possession.surrogate() != null) {
			SurrogateLedger ledger = SurrogateLedger.get(player.server);
			ledger.settle(possession.surrogate(), broken);
			ServerLevel surrogateLevel = levelOr(player, possession.surrogateDimension(), home);
			if (surrogateLevel.getEntity(possession.surrogate()) instanceof LivingEntity body) settle(body, ledger);
		}

		possession.end();
		possession.sync(player, true);
		home.sendParticles(HemoParticleData.glow(SHADOW), back.x, back.y + 1.0, back.z, 24, 0.3, 0.6, 0.3, 0.02);
		home.playSound(null, BlockPos.containing(back), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1f,
				broken ? 0.5f : 0.9f);
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
			if (worn.isEmpty() || worn.is(ItemInit.thread_of_will.get())) continue;
			stand.setItemSlot(slot, worn.copy());
			player.setItemSlot(slot, ItemStack.EMPTY);
			any = true;
		}
		if (any) level.addFreshEntity(stand);
	}

	private static void removeThreads(Inventory inventory) {
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot).is(ItemInit.thread_of_will.get())) inventory.setItem(slot, ItemStack.EMPTY);
		}
	}

	private static boolean consumeOffering(ServerLevel level, AABB bounds, net.minecraft.world.item.Item item) {
		for (ItemEntity offering : level.getEntitiesOfClass(ItemEntity.class, bounds, entity -> entity.getItem().is(item))) {
			ItemStack stack = offering.getItem();
			stack.shrink(1);
			if (stack.isEmpty()) offering.discard();
			else offering.setItem(stack);
			level.sendParticles(HemoParticleData.glow(SHADOW), offering.getX(), offering.getY() + 0.3, offering.getZ(),
					8, 0.1, 0.1, 0.1, 0.01);
			return true;
		}
		return false;
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

	private static void release(LivingEntity surrogate) {
		if (surrogate instanceof Mob mob) mob.setNoAi(false);
		surrogate.setInvulnerable(false);
	}

	/** Holds or drops the form's body: speed, jump, the golem's knockback, and flight for the winged forms. */
	public static void applyForm(Player player, SurrogateForm form, boolean present) {
		AttributeModifiers.sync(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED, form.speed(),
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, present && form.speed() != 0.0);
		AttributeModifiers.sync(player.getAttribute(Attributes.JUMP_STRENGTH), JUMP, form.jump(),
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, present && form.jump() != 0.0);
		AttributeModifiers.sync(player.getAttribute(Attributes.ATTACK_KNOCKBACK), KNOCKBACK, 1.5,
				AttributeModifier.Operation.ADD_VALUE, present && form == SurrogateForm.GOLEM);
		if (form.flies()) {
			if (present) FlightArbiter.grant(player, FlightArbiter.Source.POSSESSION);
			else FlightArbiter.revoke(player, FlightArbiter.Source.POSSESSION);
		}
	}

	public static boolean inGardens(Level level) {
		return level.dimension().equals(FungalGardenTravelHelper.FUNGAL_GARDENS);
	}
}
