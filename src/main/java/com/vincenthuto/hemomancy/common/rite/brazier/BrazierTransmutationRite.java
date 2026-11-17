package com.vincenthuto.hemomancy.common.rite.brazier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierMount;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemoryEntryKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemorySlotRef;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.LivingWeaponGraftRite;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.PyreticForgeSculpting;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.recipe.BrazierTransmutationRecipe;
import com.vincenthuto.hemomancy.common.rite.MemoryBrazierRite;
import com.vincenthuto.hemomancy.common.rite.ScarBrazierRite;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;

/**
 * The brazier as the general transformation block: a recipe-driven rite ({@code hemomancy:brazier_transmutation})
 * tried after the Memory, Scar and Living Weapon Graft rites, and only for offerings none of them claims. It keeps the
 * shared lifecycle: ordinary insertion, Projection to light, held Absorption through the channel, sneak to retrieve.
 * The manipulation the player has selected is part of the match, so the same offering becomes different things under
 * Pyretic Forge, Glacial Grasp or Vital Effusion; a recipe that names none works under any selection. With Pyretic
 * Forge selected, an offering no recipe names is smelted by its vanilla smelting recipe.
 */
public final class BrazierTransmutationRite implements BrazierRite<BrazierTransmutationRite.Plan> {
	public static final BrazierTransmutationRite INSTANCE = new BrazierTransmutationRite();
	public static final BloodAccounting.Source TRANSMUTATION = BloodAccounting.Source.of("brazier_transmutation",
			Category.RITE, false);
	/** The blood a plain Pyretic Forge smelting in the bowl costs. */
	public static final double SMELTING_COST = 15.0D;
	private static final ResourceLocation NONE = Hemomancy.rloc("none");

	private BrazierTransmutationRite() {
	}

	/**
	 * What completion makes: the recipe (empty for a plain smelting), the result, by-product, the catalyst and how many
	 * of it the off hand must hold, and the price.
	 */
	public record Plan(Optional<ResourceLocation> recipe, ItemStack result, ItemStack byproduct,
			Optional<Ingredient> catalyst, int catalystCount, double bloodCost) {
	}

	@Override
	public boolean accepts(ItemStack offering) {
		return !offering.isEmpty() && !MemoryBrazierRite.INSTANCE.accepts(offering)
				&& !ScarBrazierRite.INSTANCE.accepts(offering) && !LivingWeaponGraftRite.INSTANCE.accepts(offering);
	}

	@Override
	public Gate<Plan> preflight(Channel channel) {
		ServerPlayer player = channel.player();
		if (!channel.lit() || channel.maxAmount() <= 0.0D || !BloodAbsorptionItem.isChannelingBloodAbsorption(player)) {
			return Gate.refuse(true, false);
		}
		Optional<Plan> plan = plan(channel.level(), channel.offering(), selectedManipulation(player));
		if (plan.isEmpty()) return Gate.refuse(true, false);
		Plan found = plan.get();
		ItemStack offHand = player.getOffhandItem();
		if (found.catalyst().isPresent()
				&& (!found.catalyst().get().test(offHand) || offHand.getCount() < found.catalystCount())) {
			Component wanted = found.catalystCount() > 1
					? Component.translatable("message.hemomancy.brazier_transmutation.needs_catalysts",
							found.catalystCount(), catalystName(found.catalyst().get()))
					: Component.translatable("message.hemomancy.brazier_transmutation.needs_catalyst",
							catalystName(found.catalyst().get()));
			BrazierRites.messageEverySecond(channel.level(), player, wanted, ChatFormatting.DARK_RED);
			return Gate.refuse(true, true);
		}
		if (!HemoCapabilityAccess.getBloodVolume(player).isActive()
				|| !BloodAccounting.canPayInFull(player, found.bloodCost(), TRANSMUTATION)) {
			BrazierRites.messageEverySecond(channel.level(), player,
					Component.translatable("message.hemomancy.brazier_transmutation.needs_blood", (int) Math.ceil(found.bloodCost())),
					ChatFormatting.DARK_RED);
			return Gate.refuse(true, true);
		}
		String key = "transmute:" + found.recipe().map(ResourceLocation::toString).orElse("smelt");
		return Gate.pass(key, found);
	}

	@Override
	public Completion complete(Channel channel, Plan plan) {
		ServerLevel level = channel.level();
		ServerPlayer player = channel.player();
		BloodAccounting.Payment payment = BloodAccounting.pay(player, plan.bloodCost(), TRANSMUTATION);
		if (plan.bloodCost() > 0.0D && !payment.satisfied()) return Completion.kept(true);
		if (plan.catalyst().isPresent() && !player.getAbilities().instabuild) {
			player.getItemInHand(InteractionHand.OFF_HAND).shrink(plan.catalystCount());
		}
		Vec3 bowl = BrazierMount.bowl(level.getBlockState(channel.pos()), channel.pos(), 0.75D);
		give(level, player, bowl, plan.result());
		if (!plan.byproduct().isEmpty()) give(level, player, bowl, plan.byproduct());
		level.playSound(null, channel.pos(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.6F, 0.9F);
		level.sendParticles(HemoParticleData.ember(new ParticleColor(236, 70, 40), 0.08F, 0.9F, 24),
				bowl.x, bowl.y, bowl.z, 18, 0.25D, 0.2D, 0.25D, 0.03D);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.FORGE, bowl.add(0.0D, 0.4D, 0.0D), bowl, 0.8D, 18);
		player.displayClientMessage(Component.translatable("message.hemomancy.brazier_transmutation.done",
				plan.result().getHoverName()).withStyle(ChatFormatting.DARK_RED), true);
		return Completion.CONSUMED;
	}

	/**
	 * What {@code offering} becomes under {@code selected}: a recipe naming the selection first, then one that names
	 * none, then (Pyretic Forge only) its vanilla smelting. Empty when nothing transmutes it.
	 */
	public static Optional<Plan> plan(Level level, ItemStack offering, ResourceLocation selected) {
		Optional<RecipeHolder<BrazierTransmutationRecipe>> recipe = BrazierTransmutationRecipe.getAllRecipes(level).stream()
				.filter(holder -> holder.value().matches(offering, selected))
				.min(Comparator.<RecipeHolder<BrazierTransmutationRecipe>>comparingInt(
								holder -> holder.value().sculpting().isPresent() ? 0 : 1)
						.thenComparing(holder -> holder.id().toString()));
		if (recipe.isPresent()) {
			BrazierTransmutationRecipe value = recipe.get().value();
			return Optional.of(new Plan(Optional.of(recipe.get().id()), value.result(),
					value.byproduct().orElse(ItemStack.EMPTY), value.catalyst(), value.catalystCount(), value.bloodCost()));
		}
		if (PyreticForgeSculpting.ID.equals(selected)) {
			return PyreticForgeSculpting.smelting(level, offering).map(smelt -> new Plan(Optional.empty(),
					smelt.value().getResultItem(level.registryAccess()).copy(), ItemStack.EMPTY, Optional.empty(), 1,
					SMELTING_COST));
		}
		return Optional.empty();
	}

	/** The id of the player's selected, equipped manipulation, or {@code hemomancy:none}. */
	public static ResourceLocation selectedManipulation(ServerPlayer player) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		if (known == null) return NONE;
		MemorySlotRef ref = known.getSelectedMemoryRef();
		BloodManipulation chosen = known.getSelectedManip();
		if (ref == null || ref.kind() != MemoryEntryKind.MANIPULATION || chosen == null || chosen == BloodManipulation.BLANK) {
			return NONE;
		}
		BloodManipulation registered = ManipulationInit.getByName(chosen.getName());
		if (registered == null || !known.isManipEquipped(registered)) return NONE;
		return Hemomancy.rloc(registered.getName());
	}

	private static Component catalystName(Ingredient catalyst) {
		ItemStack[] items = catalyst.getItems();
		return items.length == 0 ? Component.empty() : items[0].getHoverName();
	}

	private static void give(ServerLevel level, ServerPlayer player, Vec3 at, ItemStack stack) {
		ItemStack copy = stack.copy();
		player.getInventory().add(copy);
		if (copy.isEmpty()) return;
		ItemEntity entity = new ItemEntity(level, at.x, at.y, at.z, copy);
		entity.setDeltaMovement(Vec3.ZERO);
		level.addFreshEntity(entity);
	}
}
