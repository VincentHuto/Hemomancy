package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import net.minecraft.world.item.ItemStack;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncPossession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

/**
 * A Silentium's possession of a Gardens facsimile: the form, the Concentration left, the offerings that tune it, and
 * everything needed to put the caster back where they were (return point, saved inventory, surrogate, echo), and the
 * wheel's state: each gift's cooldown, the toggles and the return hold.
 * Kept on the player so a crash or logout mid-visit is always recoverable.
 */
public class Possession implements INBTSerializable<CompoundTag> {
	@Nullable
	private SurrogateForm form;
	private float concentration;
	private float max;
	private int shell;
	private boolean regen;
	private boolean totem;
	@Nullable
	private UUID surrogate;
	@Nullable
	private ResourceLocation surrogateDimension;
	@Nullable
	private UUID echo;
	@Nullable
	private ResourceLocation returnDimension;
	private Vec3 returnPos = Vec3.ZERO;
	private float returnYaw;
	private ListTag savedInventory = new ListTag();
	/** A stack a Thread-of-Will-era save moved aside; it comes home with the will. Nothing writes one any more. */
	private ItemStack legacyStash = ItemStack.EMPTY;
	/** Game time each wheel gift is ready again. The visit ends on logout, so these are never saved. */
	private final EnumMap<PossessionAction, Long> readyAt = new EnumMap<>(PossessionAction.class);
	private boolean still;
	private boolean climb = true;
	private boolean hover = true;
	/** Game time the return hold completes, or 0 when the will is not drawing home. */
	private long returnAt;
	private Vec3 returnFrom = Vec3.ZERO;
	private transient boolean syncDirty;

	public boolean active() { return form != null; }
	@Nullable public SurrogateForm form() { return form; }
	public float concentration() { return concentration; }
	public float max() { return max; }
	public int shell() { return shell; }
	public boolean regen() { return regen; }
	public boolean totem() { return totem; }
	@Nullable public UUID surrogate() { return surrogate; }
	@Nullable public ResourceLocation surrogateDimension() { return surrogateDimension; }
	@Nullable public UUID echo() { return echo; }
	@Nullable public ResourceLocation returnDimension() { return returnDimension; }
	public Vec3 returnPos() { return returnPos; }
	public float returnYaw() { return returnYaw; }
	public ListTag savedInventory() { return savedInventory; }

	public void begin(SurrogateForm form, float max, int shell, boolean regen, boolean totem, UUID surrogate,
			ResourceLocation surrogateDimension, UUID echo, ResourceLocation returnDimension, Vec3 returnPos,
			float returnYaw, ListTag savedInventory) {
		this.form = form;
		this.max = max;
		this.concentration = max;
		this.shell = shell;
		this.regen = regen;
		this.totem = totem;
		this.surrogate = surrogate;
		this.surrogateDimension = surrogateDimension;
		this.echo = echo;
		this.returnDimension = returnDimension;
		this.returnPos = returnPos;
		this.returnYaw = returnYaw;
		this.savedInventory = savedInventory;
		resetWheel();
		syncDirty = true;
	}

	public void end() {
		form = null;
		concentration = 0f;
		max = 0f;
		shell = 0;
		regen = false;
		totem = false;
		surrogate = null;
		surrogateDimension = null;
		echo = null;
		returnDimension = null;
		returnPos = Vec3.ZERO;
		savedInventory = new ListTag();
		legacyStash = ItemStack.EMPTY;
		resetWheel();
		syncDirty = true;
	}

	private void resetWheel() {
		readyAt.clear();
		still = false;
		climb = true;
		hover = true;
		returnAt = 0L;
		returnFrom = Vec3.ZERO;
	}

	public List<ItemStack> takeLegacyStash() {
		ItemStack taken = legacyStash;
		legacyStash = ItemStack.EMPTY;
		return taken.isEmpty() ? List.of() : List.of(taken);
	}

	public long readyAt(PossessionAction action) {
		return readyAt.getOrDefault(action, 0L);
	}

	public void setReadyAt(PossessionAction action, long gameTime) {
		readyAt.put(action, gameTime);
		syncDirty = true;
	}

	/** Whether a toggled action is on: Still starts off, Climb and Hover start on. Triggers are never on. */
	public boolean toggled(PossessionAction action) {
		return switch (action) {
			case STILL -> still;
			case CLIMB -> climb;
			case HOVER -> hover;
			default -> false;
		};
	}

	/** Flips a toggled action and returns its new state. */
	public boolean toggle(PossessionAction action) {
		switch (action) {
			case STILL -> still = !still;
			case CLIMB -> climb = !climb;
			case HOVER -> hover = !hover;
			default -> { return false; }
		}
		syncDirty = true;
		return toggled(action);
	}

	public boolean returning() { return returnAt > 0L; }
	public long returnAt() { return returnAt; }
	public Vec3 returnFrom() { return returnFrom; }

	public void beginReturn(long completesAt, Vec3 from) {
		returnAt = completesAt;
		returnFrom = from;
		syncDirty = true;
	}

	public void cancelReturn() {
		if (returnAt == 0L) return;
		returnAt = 0L;
		syncDirty = true;
	}

	public void setConcentration(float value) {
		float clamped = Math.max(0f, Math.min(max, value));
		if (clamped != concentration) {
			concentration = clamped;
			syncDirty = true;
		}
	}

	public void setShell(int value) {
		if (value != shell) {
			shell = value;
			syncDirty = true;
		}
	}

	/** Spends the totem once: Concentration is refilled. */
	public boolean spendTotem() {
		if (!totem) return false;
		totem = false;
		setConcentration(max);
		syncDirty = true;
		return true;
	}

	/** Client mirror for the HUD and the facsimile render, on the owner and on everyone tracking them. */
	public void setFromSync(@Nullable SurrogateForm form, float concentration, float max, int shell, boolean still,
			boolean climb, boolean hover, long returnAt, long[] readyAt) {
		this.form = form;
		this.max = max;
		this.concentration = concentration;
		this.shell = shell;
		this.still = still;
		this.climb = climb;
		this.hover = hover;
		this.returnAt = returnAt;
		this.readyAt.clear();
		for (PossessionAction action : PossessionAction.values()) {
			if (action.ordinal() < readyAt.length && readyAt[action.ordinal()] > 0L) {
				this.readyAt.put(action, readyAt[action.ordinal()]);
			}
		}
	}

	public boolean isSyncDirty() { return syncDirty; }

	public void sync(ServerPlayer player, boolean force) {
		if (!force && !syncDirty) return;
		PacketHandler.sendToPlayerAndTrackers(player, PacketSyncPossession.of(player.getId(), this));
		syncDirty = false;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		if (form == null) return tag;
		tag.putString("form", form.name());
		tag.putFloat("concentration", concentration);
		tag.putFloat("max", max);
		tag.putInt("shell", shell);
		tag.putBoolean("regen", regen);
		tag.putBoolean("totem", totem);
		if (surrogate != null) tag.putUUID("surrogate", surrogate);
		if (surrogateDimension != null) tag.putString("surrogate_dimension", surrogateDimension.toString());
		if (echo != null) tag.putUUID("echo", echo);
		if (returnDimension != null) tag.putString("return_dimension", returnDimension.toString());
		tag.putDouble("return_x", returnPos.x);
		tag.putDouble("return_y", returnPos.y);
		tag.putDouble("return_z", returnPos.z);
		tag.putFloat("return_yaw", returnYaw);
		tag.put("inventory", savedInventory);
		if (!legacyStash.isEmpty()) tag.put("stash", legacyStash.save(provider));
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		end();
		if (!tag.contains("form")) return;
		try {
			form = SurrogateForm.valueOf(tag.getString("form"));
		} catch (IllegalArgumentException unknown) {
			form = SurrogateForm.WOLF;
		}
		concentration = tag.getFloat("concentration");
		max = tag.getFloat("max");
		shell = tag.getInt("shell");
		regen = tag.getBoolean("regen");
		totem = tag.getBoolean("totem");
		surrogate = tag.hasUUID("surrogate") ? tag.getUUID("surrogate") : null;
		surrogateDimension = tag.contains("surrogate_dimension")
				? ResourceLocation.tryParse(tag.getString("surrogate_dimension")) : null;
		echo = tag.hasUUID("echo") ? tag.getUUID("echo") : null;
		returnDimension = tag.contains("return_dimension")
				? ResourceLocation.tryParse(tag.getString("return_dimension")) : null;
		returnPos = new Vec3(tag.getDouble("return_x"), tag.getDouble("return_y"), tag.getDouble("return_z"));
		returnYaw = tag.getFloat("return_yaw");
		savedInventory = tag.getList("inventory", Tag.TAG_COMPOUND);
		legacyStash = tag.contains("stash") ? ItemStack.parse(provider, tag.get("stash")).orElse(ItemStack.EMPTY)
				: ItemStack.EMPTY;
		syncDirty = true;
	}

	public BlockPos returnBlock() {
		return BlockPos.containing(returnPos);
	}
}
