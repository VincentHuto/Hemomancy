package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.QliphothPomeItemRenderer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncPomeProgress;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.UUID;

/**
 * Edible reagent fruit dropped by Qliphoth Bloom trees. Renders as a dark
 * glowing orb matching the void-like spheres embedded in the tree's trunk.
 *
 * <p>Each pome is one of the nine Qliphoth husks — the corrupted shells of
 * the Qliphoth system. The husk index (0–8) is stored on the ItemStack NBT
 * key {@link #HUSK_INDEX_KEY}, assigned when the pome drops from the tree.
 * The bloom that produced the pome is recorded under {@link #BLOOM_ORIGIN_KEY}
 * as the bloom center's {@code BlockPos.asLong()} value.
 *
 * <p>Eating the pome grants a portable echo of the Bloom's aura:
 * <ul>
 *   <li>+300 blood volume (instant burst if blood system is active)</li>
 *   <li>Regeneration II for 12 seconds</li>
 *   <li>Darkness for 7 seconds (brief void-register perception)</li>
 *   <li>25 % manipulation cost reduction for 3 minutes</li>
 * </ul>
 *
 * <p>If the pome was severed by an Unstained pruning rite ({@link #TAINTED_KEY}
 * is set), the effects are inverted: Weakness II replaces the blood burst,
 * Darkness is prolonged to 15 seconds, and no empowerment is granted.
 *
 * <p>Consuming all nine pomes from a single bloom's lifecycle triggers the
 * Qliphoth Communion whisper and sets the communion flag on the player's
 * {@code IInitiatoryDegree} capability, unlocking the Rite of Apotheos
 * (the Eighth Degree gate).
 */
public class QliphothPomeItem extends Item implements HemoClientItemExtensionsProvider {

	// ── ItemStack NBT keys (written when the pome drops from the tree) ──

	/** Long value: the bloom-center {@code BlockPos.asLong()} this pome originated from. */
	public static final String BLOOM_ORIGIN_KEY = "hemomancy:bloom_origin";

	/**
	 * Int value 0–8: which of the nine Qliphoth husks this pome represents.
	 * Assigned in drop order from the tree.
	 */
	public static final String HUSK_INDEX_KEY = "hemomancy:husk_index";

	/**
	 * Boolean: set to {@code true} when the pome was severed by an Unstained
	 * pruning rite. Tainted pomes apply inverted / weakened effects.
	 */
	public static final String TAINTED_KEY = "hemomancy:tainted_pome";

	/**
	 * Boolean: set when a pome was picked directly from its Qliphoth Bloom.
	 * Bound pomes resist leaving the player's inventory until consumed.
	 */
	public static final String BOUND_KEY = "hemomancy:bound_pome";

	/** UUID: the player who grew and claimed the bloom fruit. */
	public static final String OWNER_KEY = "hemomancy:pome_owner";

	// Pome communion and empowerment data is stored in IInitiatoryDegree capability.

	// ── Effect constants ──

	private static final double BLOOD_BURST = 300.0;
	private static final int REGEN_DURATION_TICKS = 240;
	private static final int DARKNESS_DURATION_TICKS = 140;
	private static final int DARKNESS_TAINTED_DURATION_TICKS = 300;
	private static final long CREATIVE_TEST_BLOOM_ORIGIN = Long.MIN_VALUE;
	private static final String POME_MESSAGE_KEY_BASE = "message.hemomancy.qliphoth_pome.consume.";

	/** The nine Qliphoth husk names in consumption order (indices 0–8). */
	public static final String[] HUSK_NAMES = {
		"Nahemoth",
		"Samael",
		"Gamaliel",
		"Harab Serapel",
		"Golachab",
		"Thagirion",
		"A\u2019arab Zaraq",
		"Satariel",
		"Ghagiel"
	};

	public QliphothPomeItem(Properties properties) {
		super(properties);
	}

	private static CompoundTag getCustomData(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	public static ItemStack createPickedPomeStack(long bloomOrigin, int huskIndex, UUID ownerUUID) {
		ItemStack pomeStack = new ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.qliphoth_pome.get());
		CompoundTag tag = getCustomData(pomeStack);
		tag.putLong(BLOOM_ORIGIN_KEY, bloomOrigin);
		tag.putInt(HUSK_INDEX_KEY, huskIndex);
		tag.putBoolean(BOUND_KEY, true);
		tag.putUUID(OWNER_KEY, ownerUUID);
		pomeStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return pomeStack;
	}

	public static boolean isBoundPome(ItemStack stack) {
		return !stack.isEmpty()
				&& stack.is(com.vincenthuto.hemomancy.common.init.ItemInit.qliphoth_pome.get())
				&& getCustomData(stack).getBoolean(BOUND_KEY);
	}

	public static boolean isBoundPomeFromBloom(ItemStack stack, long bloomOrigin) {
		if (!isBoundPome(stack)) {
			return false;
		}
		CompoundTag tag = getCustomData(stack);
		return tag.contains(BLOOM_ORIGIN_KEY) && tag.getLong(BLOOM_ORIGIN_KEY) == bloomOrigin;
	}

	// ── Tooltip ──

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);

		CompoundTag tag = getCustomData(stack);
		boolean isTainted = tag.getBoolean(TAINTED_KEY);

		if (isTainted) {
			tooltip.add(Component.literal("A severed husk — cut before it was ready to fall.")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			tooltip.add(Component.literal("The void inside is dying, not opening.")
					.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		} else {
			// Show husk name and index if the pome was assigned one at drop time
			if (tag.contains(HUSK_INDEX_KEY)) {
				int huskIdx = tag.getInt(HUSK_INDEX_KEY);
				if (huskIdx >= 0 && huskIdx < HUSK_NAMES.length) {
					int ordinal = huskIdx + 1;
					tooltip.add(Component.literal(ordinalString(ordinal) + " of nine. Sweet in the way that TV static and silence are sweet.")
							.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
					tooltip.add(Component.literal("[" + HUSK_NAMES[huskIdx] + "]")
							.withStyle(ChatFormatting.DARK_PURPLE));
				} else {
					tooltip.add(Component.literal("One of nine. Sweet in the way that TV static and silence are sweet.")
							.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
				}
			} else {
				tooltip.add(Component.literal("One of nine. Sweet in the way that TV static and silence are sweet.")
						.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
			}
			tooltip.add(Component.literal("+300 Blood Volume")
					.withStyle(ChatFormatting.DARK_RED));
			tooltip.add(Component.literal("Regeneration II (12s)")
					.withStyle(ChatFormatting.RED));
			tooltip.add(Component.literal("Manipulation Cost -25% (3 min)")
					.withStyle(ChatFormatting.GOLD));
			tooltip.add(Component.literal("Darkness (7s)")
					.withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	// ── On-eat effects ──

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (!level.isClientSide && entity instanceof Player player && !canPlayerConsumeStack(stack, player)) {
			player.displayClientMessage(Component.literal("The pome resists your mouth. It belongs to the one who grew it.")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			return stack;
		}

		ItemStack result = super.finishUsingItem(stack, level, entity);

		if (!level.isClientSide && entity instanceof Player player) {
			CompoundTag itemTag = getCustomData(stack);
			boolean isTainted = itemTag.getBoolean(TAINTED_KEY);
			spawnPomePulse(player);

			if (isTainted) {
				applyTaintedEffects(player);
				playPomeSound(player, false);
			} else {
				applyNormalEffects(player, level, itemTag);
			}
		}

		return result;
	}

	private static boolean canPlayerConsumeStack(ItemStack stack, Player player) {
		CompoundTag tag = getCustomData(stack);
		boolean boundPome = tag.getBoolean(BOUND_KEY);
		boolean hasOwnerData = tag.hasUUID(OWNER_KEY);
		boolean ownerMatchesPlayer = hasOwnerData && tag.getUUID(OWNER_KEY).equals(player.getUUID());
		return QliphothPomeRules.canConsumePickedPome(boundPome, hasOwnerData, ownerMatchesPlayer,
				player.getAbilities().instabuild);
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	private static void spawnPomePulse(Player player) {
		if (player.level() instanceof ServerLevel serverLevel) {
			PacketHandler.sendPomePulse(player.position().add(0, player.getBbHeight() * 0.55, 0), 64.0, serverLevel);
		}
	}

	private static void playPomeSound(Player player, boolean communion) {
		if (player.level() instanceof ServerLevel serverLevel) {
			serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
					communion ? SoundInit.ITEM_QLIPHOPH_POME_COMMUNION.get() : SoundInit.ITEM_QLIPHOPH_POME_EAT.get(),
					SoundSource.PLAYERS, communion ? 1.25F : 0.9F, communion ? 0.82F : 1.0F);
		}
	}

	private static void applyTaintedEffects(Player player) {
		// Tainted pomes: inverted effects — weakness replaces blood burst,
		// darkness is longer (a dying shell rather than an opening one)
		player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
				200, 1, false, true, true));
		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
				DARKNESS_TAINTED_DURATION_TICKS, 0, false, true, true));
		player.displayClientMessage(
				Component.literal("A hollow, severed thing crumbles in your veins...")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
				true);
	}

	private static void applyNormalEffects(Player player, Level level, CompoundTag itemTag) {
		// ── Blood burst ──
		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
			if (volume.isActive()) {
				volume.fill(BLOOD_BURST);
				BloodVolumeEvents.syncVolume((ServerPlayer) player, volume);
			}
		});

		// ── Regeneration II burst ──
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
				REGEN_DURATION_TICKS, 1, false, true, true));

		// ── Darkness side-effect ──
		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
				DARKNESS_DURATION_TICKS, 0, false, true, true));

		// ── Timed manipulation discount ──
		long expiryTick = level.getGameTime()
				+ QliphothPomeRules.empowermentDurationTicks(SkillPointHelper.getQliphothGestationLevel(player));
		HemoCapabilityAccess.getInitiatoryDegree(player)
				.ifPresent(d -> d.setPomeEmpowermentExpiry(expiryTick));

		// Husk-flavoured eat message (localized, one line per pome 1-9)
		int huskIdx = resolveHuskIndex(player, itemTag);
		String consumeMessageKey = huskIdx >= 0
				? POME_MESSAGE_KEY_BASE + (huskIdx + 1)
				: POME_MESSAGE_KEY_BASE + "default";
		Component consumeMessage = Component.translatable(consumeMessageKey)
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
		if (huskIdx >= 0) {
			consumeMessage = consumeMessage.copy()
					.append(Component.literal(" [" + HUSK_NAMES[huskIdx] + "]")
							.withStyle(ChatFormatting.DARK_PURPLE));
		}
		player.displayClientMessage(
				consumeMessage,
				true);

		// ── Total pome counter (drives the HUD tracker) ──
		HemoCapabilityAccess.getInitiatoryDegree(player)
				.ifPresent(d -> d.incrementTotalPomesConsumed());

		// ── Per-bloom communion tracking (Apotheos gate) ──
		long bloomOrigin = itemTag.contains(BLOOM_ORIGIN_KEY)
				? itemTag.getLong(BLOOM_ORIGIN_KEY)
				: CREATIVE_TEST_BLOOM_ORIGIN;
		boolean completedCommunion = trackCommunionProgress((ServerPlayer) player, bloomOrigin);
		clearPendingPome(level, bloomOrigin);
		playPomeSound(player, completedCommunion);
		syncPomeProgress((ServerPlayer) player);
	}

	private static void clearPendingPome(Level level, long bloomOrigin) {
		if (bloomOrigin == CREATIVE_TEST_BLOOM_ORIGIN || !(level instanceof ServerLevel serverLevel)) {
			return;
		}
		QliphothBloomSavedData.get(serverLevel.getServer().overworld()).clearPendingPome(bloomOrigin);
	}

	private static int resolveHuskIndex(Player player, CompoundTag itemTag) {
		if (itemTag.contains(HUSK_INDEX_KEY)) {
			int huskIdx = itemTag.getInt(HUSK_INDEX_KEY);
			if (huskIdx >= 0 && huskIdx < HUSK_NAMES.length) {
				return huskIdx;
			}
		}

		return HemoCapabilityAccess.getInitiatoryDegree(player)
				.map(degree -> Math.max(0, Math.min(HUSK_NAMES.length - 1, degree.getTotalPomesConsumed())))
				.orElse(0);
	}

	/**
	 * Increments the per-bloom consumption counter for the given bloom origin.
	 * When the count reaches nine, fires the Qliphoth Communion whisper and
	 * sets the permanent communion flag on the InitiatoryDegree capability.
	 */
	private static boolean trackCommunionProgress(ServerPlayer player, long bloomOrigin) {
		return HemoCapabilityAccess.getInitiatoryDegree(player).map(degree -> {
			if (degree.isQliphothCommunionDone()) return false;
			int count = degree.recordPomeConsumed(bloomOrigin);
			if (count >= 9) {
				degree.setQliphothCommunionDone(true);
				if (QliphothPomeRules.shouldGrantFungalSpine(count, degree.hasFungalSpineGranted())) {
					ItemStack spine = new ItemStack(ItemInit.fungal_spine.get());
					degree.setFungalSpineGranted(true);
					InitiatoryDegreeEvents.syncDegree(player, degree);
					if (!player.getInventory().add(spine)) {
						player.drop(spine, false);
					}
					player.displayClientMessage(Component.literal(
							"The ninth husk opens. A calcified thread tears free from your back.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
				}
				PacketHandler.sendToPlayer(player, new OpenDialoguePacket(
						FungalWhisperDialogueTrees.qliphothCommunion()));
				return true;
			}
			return false;
		}).orElse(false);
	}

	private static void syncPomeProgress(ServerPlayer player) {
		int progress = HemoCapabilityAccess.getInitiatoryDegree(player)
				.map(degree -> degree.isQliphothCommunionDone() ? 9 : degree.getTotalPomesConsumed())
				.orElse(0);
		PacketHandler.sendToPlayer(player, new PacketSyncPomeProgress(progress));
	}

	/** Returns the English ordinal string for a number 1–9 (e.g. "First", "Second"). */
	private static String ordinalString(int n) {
		return switch (n) {
			case 1 -> "First";
			case 2 -> "Second";
			case 3 -> "Third";
			case 4 -> "Fourth";
			case 5 -> "Fifth";
			case 6 -> "Sixth";
			case 7 -> "Seventh";
			case 8 -> "Eighth";
			case 9 -> "Ninth";
			default -> String.valueOf(n) + "th";
		};
	}

	// ── Renderer ──

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new QliphothPomeItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}
}
