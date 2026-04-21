package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.List;
import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.render.item.QliphothPomeItemRenderer;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.network.PacketDistributor;

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
 * Qliphoth Communion whisper and sets the {@link #QLIPHOTH_COMMUNION_DONE_KEY}
 * flag on the player's persistent data, unlocking the Rite of Apotheos
 * (the Eighth Degree gate).
 */
public class QliphothPomeItem extends Item {

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

	// ── Player persistent-data keys ──

	/**
	 * Long value: world game-time tick at which the Pome Empowerment discount
	 * expires. A value of 0 (or absent) means no active empowerment.
	 */
	public static final String POME_EMPOWERMENT_KEY = "hemomancy:pome_empowerment_expiry";

	/**
	 * CompoundTag on the player's persistent data mapping bloom-origin longs
	 * (stored as string keys) to the number of pomes consumed from that bloom.
	 * Used to detect when the player has consumed all nine husks.
	 */
	public static final String POME_COMMUNION_PROGRESS_KEY = "hemomancy:pome_communion_progress";

	/**
	 * Boolean flag set to {@code true} when the player has completed the
	 * Qliphoth Communion (consumed all nine pomes from a single bloom).
	 */
	public static final String QLIPHOTH_COMMUNION_DONE_KEY = "hemomancy:qliphoth_communion";

	// ── Effect constants ──

	private static final double BLOOD_BURST = 300.0;
	private static final int REGEN_DURATION_TICKS = 240;
	private static final int DARKNESS_DURATION_TICKS = 140;
	private static final int DARKNESS_TAINTED_DURATION_TICKS = 300;
	private static final long EMPOWERMENT_DURATION_TICKS = 3600L;
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

	// ── Tooltip ──

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, world, tooltip, flag);

		CompoundTag tag = stack.getTag();
		boolean isTainted = tag != null && tag.getBoolean(TAINTED_KEY);

		if (isTainted) {
			tooltip.add(Component.literal("A severed husk — cut before it was ready to fall.")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			tooltip.add(Component.literal("The void inside is dying, not opening.")
					.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		} else {
			// Show husk name and index if the pome was assigned one at drop time
			if (tag != null && tag.contains(HUSK_INDEX_KEY)) {
				int huskIdx = tag.getInt(HUSK_INDEX_KEY);
				if (huskIdx >= 0 && huskIdx < HUSK_NAMES.length) {
					int ordinal = huskIdx + 1;
					tooltip.add(Component.literal(ordinalString(ordinal) + " of nine. Sweet in the way that the color black is sweet.")
							.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
					tooltip.add(Component.literal("[" + HUSK_NAMES[huskIdx] + "]")
							.withStyle(ChatFormatting.DARK_PURPLE));
				} else {
					tooltip.add(Component.literal("One of nine. Sweet in the way that the color black is sweet.")
							.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
				}
			} else {
				tooltip.add(Component.literal("One of nine. Sweet in the way that the color black is sweet.")
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
		ItemStack result = super.finishUsingItem(stack, level, entity);

		if (!level.isClientSide && entity instanceof Player player) {
			CompoundTag itemTag = stack.getTag();
			boolean isTainted = itemTag != null && itemTag.getBoolean(TAINTED_KEY);

			if (isTainted) {
				applyTaintedEffects(player);
			} else {
				applyNormalEffects(player, level, itemTag);
			}
		}

		return result;
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
		long expiryTick = level.getGameTime() + EMPOWERMENT_DURATION_TICKS;
		player.getPersistentData().putLong(POME_EMPOWERMENT_KEY, expiryTick);

		// Husk-flavoured eat message (localized, one line per pome 1-9)
		String consumeMessageKey = POME_MESSAGE_KEY_BASE + "default";
		if (itemTag != null && itemTag.contains(HUSK_INDEX_KEY)) {
			int huskIdx = itemTag.getInt(HUSK_INDEX_KEY);
			if (huskIdx >= 0 && huskIdx < HUSK_NAMES.length) {
				consumeMessageKey = POME_MESSAGE_KEY_BASE + (huskIdx + 1);
			}
		}
		player.displayClientMessage(
				Component.translatable(consumeMessageKey)
						.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
				true);

		// ── Per-bloom communion tracking ──
		if (itemTag != null && itemTag.contains(BLOOM_ORIGIN_KEY)) {
			long bloomOrigin = itemTag.getLong(BLOOM_ORIGIN_KEY);
			trackCommunionProgress((ServerPlayer) player, bloomOrigin);
		}
	}

	/**
	 * Increments the per-bloom consumption counter for the given bloom origin.
	 * When the count reaches nine, fires the Qliphoth Communion whisper and
	 * sets the permanent communion flag on the player's persistent data.
	 */
	private static void trackCommunionProgress(ServerPlayer player, long bloomOrigin) {
		CompoundTag persistentData = player.getPersistentData();

		// Don't track if communion already achieved
		if (persistentData.getBoolean(QLIPHOTH_COMMUNION_DONE_KEY)) return;

		CompoundTag progress = persistentData.contains(POME_COMMUNION_PROGRESS_KEY)
				? persistentData.getCompound(POME_COMMUNION_PROGRESS_KEY)
				: new CompoundTag();

		String bloomKey = String.valueOf(bloomOrigin);
		int count = progress.getInt(bloomKey) + 1;
		progress.putInt(bloomKey, count);
		persistentData.put(POME_COMMUNION_PROGRESS_KEY, progress);

		if (count >= 9) {
			// All nine husks consumed from this single bloom — Qliphoth Communion
			persistentData.putBoolean(QLIPHOTH_COMMUNION_DONE_KEY, true);

			DialogueTree communion = FungalWhisperDialogueTrees.qliphothCommunion();
			PacketHandler.CHANNELBLOODVOLUME.send(
					PacketDistributor.PLAYER.with(() -> player),
					new OpenDialoguePacket(communion));
		}
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
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new QliphothPomeItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		});
	}
}
