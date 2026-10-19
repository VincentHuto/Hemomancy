package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorGeometry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CardinalFocusBlockEntity extends BlockEntity {
	private static final String TAG_MEDIUM = "Medium";
	private static final String TAG_FLOOR = "FormedFloor";
	private static final String TAG_FLOOR_FORWARDS = "FormedFloorForwards";
	private static final String TAG_FLOOR_SINCE = "FormedFloorSince";
	private static final String TAG_SETTLED_CRAFT = "SettledCraft";
	private static final int FLOOR_CHECK_INTERVAL = 20;
	private BlockPos templeDisplay;
	private java.util.UUID templeHermit;
	private ItemStack medium = ItemStack.EMPTY;
	/** The last Working craft this Focus settled, saved with the chunk that lost its structure. */
	private java.util.UUID settledCraft;
	// The floor the server last matched here. Visual only: rites still match their own station.
	private ResourceLocation formedFloor;
	private Direction formedForwards = Direction.NORTH;
	private long formedSince;
	// Client-side memory of a floor that just broke, so its membrane withers instead of vanishing.
	private ResourceLocation fadingFloor;
	private Direction fadingForwards = Direction.NORTH;
	private long fadingSince;
	// Client-side start of the formation animation, when it must wait for a collapse or resume one; NaN uses the
	// server's formation time.
	private float clientFormStart = Float.NaN;
	// Client-side: how far the floor's heart has quickened for a rite held here, eased by the floor renderer.
	private float riteQuick;
	private float riteIntense;

	public CardinalFocusBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.cardinal_focus.get(), pos, state);
	}

	public void linkTempleDisplay(BlockPos display) {
		templeDisplay = display == null ? null : display.immutable();
		setChanged();
	}

	public BlockPos getTempleDisplay() {
		return templeDisplay;
	}

	public void linkTempleHermit(java.util.UUID hermit) {
		templeHermit = hermit;
		setChanged();
	}

	public java.util.UUID getTempleHermit() { return templeHermit; }

	public ItemStack getMediumDisplayStack() {
		return medium.copy();
	}

	public ItemStack getMediumForMatching() {
		return medium;
	}

	public boolean insertMedium(Player player, ItemStack stack) {
		if (stack.isEmpty() || !medium.isEmpty()) return false;
		medium = stack.copyWithCount(1);
		if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
		markDirtyAndSync();
		return true;
	}

	public ItemStack extractMedium() {
		if (medium.isEmpty()) return ItemStack.EMPTY;
		ItemStack extracted = medium.copy();
		medium = ItemStack.EMPTY;
		markDirtyAndSync();
		return extracted;
	}

	public boolean consumeMedium(Ingredient required) {
		if (required == null || required == Ingredient.EMPTY || required.isEmpty()) return medium.isEmpty();
		if (!required.test(medium)) return false;
		extractMedium();
		return true;
	}

	public boolean hasMedium() {
		return !medium.isEmpty();
	}

	public ResourceLocation getFormedFloor() { return formedFloor; }

	public Direction getFormedForwards() { return formedForwards; }

	public long getFormedSince() { return formedSince; }

	public ResourceLocation getFadingFloor() { return fadingFloor; }

	public Direction getFadingForwards() { return fadingForwards; }

	public long getFadingSince() { return fadingSince; }

	/** When the formed floor's formation animation starts on this client. */
	public float getFormationStart() { return Float.isNaN(clientFormStart) ? formedSince : clientFormStart; }

	/**
	 * The floor's heartbeat at this time: the resting lub-dub, quickened while a rite is held on the floor. The floor
	 * renderer sets how quickened it is each frame, and the membrane, its seats and the Focus's particles share it.
	 */
	public float heartbeat(double time) {
		return CardinalRiteFloorVisualRules.riteHeartbeat(time, riteQuick, riteIntense);
	}

	/** Client-side: how far a rite has quickened the floor's heart, each 0 to 1, already eased by the caller. */
	public void setRiteTempo(float quick, float intense) {
		riteQuick = quick;
		riteIntense = intense;
	}

	/** Whether a broken or replaced floor is still drawing back into the Focus at this time. */
	public boolean isCollapsing(float now) {
		return fadingFloor != null && !CardinalRiteFloorVisualRules.collapsed(now - fadingSince);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, CardinalFocusBlockEntity focus) {
		if (!(level instanceof ServerLevel server)
				|| Math.floorMod(level.getGameTime() + pos.asLong(), FLOOR_CHECK_INTERVAL) != 0) return;
		var formed = CardinalRiteStationMatcher.formedFloor(server, pos).orElse(null);
		ResourceLocation id = formed == null ? null : formed.floor().id();
		com.vincenthuto.hemomancy.common.rite.zone.RiteSpaces.onFloorCheck(server, pos, id);
		Direction forwards = formed == null ? Direction.NORTH : formed.forwards();
		if (java.util.Objects.equals(id, focus.formedFloor) && forwards == focus.formedForwards) return;
		boolean newFloor = !java.util.Objects.equals(id, focus.formedFloor);
		focus.formedFloor = id;
		focus.formedForwards = forwards;
		if (newFloor) {
			focus.formedSince = level.getGameTime();
			if (id != null) {
				level.playSound(null, pos, SoundInit.RITE_FLOOR_FORM.get(), SoundSource.BLOCKS, 1.3F, 1.0F);
			} else {
				// The membrane tearing back into the Focus: a quiet rupture, not a rite failing.
				level.playSound(null, pos, SoundInit.RITE_ANCHOR_BREAK.get(), SoundSource.BLOCKS, 0.45F, 0.7F);
			}
		}
		focus.markDirtyAndSync();
	}

	/** Accent particles for a formed floor; the renderer's membrane carries the effect. */
	public static void clientTick(Level level, BlockPos pos, BlockState state, CardinalFocusBlockEntity focus) {
		long now = level.getGameTime();
		boolean collapsing = focus.isCollapsing(now);
		ResourceLocation shown = collapsing ? focus.fadingFloor : focus.formedFloor;
		if (shown == null) return;
		CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(shown).orElse(null);
		if (floor == null) return;
		RandomSource random = level.random;
		float age = collapsing ? CardinalRiteFloorVisualRules.collapseAge(now - focus.fadingSince)
				: now - focus.getFormationStart();
		if (age < 0.0F) return;
		int half = CardinalRiteFloorGeometry.halfWidth(floor);
		var palette = CardinalRiteFloorVisualRules.palette(
				CardinalRiteFloorVisualRules.styleIndex(floor.style()));
		double cx = pos.getX() + 0.5D;
		double cz = pos.getZ() + 0.5D;
		double top = pos.getY() + 1.02D;
		if (age < CardinalRiteFloorVisualRules.FORM_TICKS) {
			// Sparks ride the spreading edge, or drift inward with it as a broken floor draws back.
			float radius = CardinalRiteFloorVisualRules.spreadRadius(age, half);
			double drift = collapsing ? -0.02D : 0.02D;
			for (int i = 0; i < 3; i++) {
				double angle = random.nextDouble() * Math.PI * 2.0D;
				double x = Math.cos(angle) * radius;
				double z = Math.sin(angle) * radius;
				if (Math.abs(x) > half + 0.5D || Math.abs(z) > half + 0.5D) continue;
				level.addParticle(HemoParticleData.ember(color(palette.glow()), 0.06F + random.nextFloat() * 0.04F,
								0.9F, 18 + random.nextInt(10)),
						cx + x, top, cz + z, Math.cos(angle) * drift, 0.03D + random.nextDouble() * 0.02D,
						Math.sin(angle) * drift);
			}
			return;
		}
		// A blood cell surfaces from the membrane and drifts up, more often while a rite quickens it.
		if (random.nextInt(focus.riteQuick > 0.5F ? 3 : 5) == 0) {
			double x = (random.nextDouble() * 2.0D - 1.0D) * (half + 0.4D);
			double z = (random.nextDouble() * 2.0D - 1.0D) * (half + 0.4D);
			level.addParticle(HemoParticleData.bloodCell(color(palette.vein())), cx + x, top, cz + z,
					0.0D, 0.012D, 0.0D);
		}
		float beat = focus.heartbeat(level.getGameTime());
		if (beat > 0.85F && random.nextInt(3) == 0) {
			level.addParticle(HemoParticleData.glow(color(palette.glow())), cx, top + 0.05D, cz,
					0.0D, 0.02D, 0.0D);
		}
	}

	private static ParticleColor color(int argb) {
		return new ParticleColor((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF);
	}

	public void markDirtyAndSync() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
		}
	}

	/**
	 * Witnesses that this Focus settled a Working craft. A rite reloaded from an
	 * older save than its chunk can then tell its structure was already taken.
	 */
	public void markCraftSettled(java.util.UUID riteId) {
		settledCraft = riteId;
		setChanged();
	}

	public boolean settledCraft(java.util.UUID riteId) {
		return riteId != null && riteId.equals(settledCraft);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (templeDisplay != null) tag.putLong("TempleDisplay", templeDisplay.asLong());
		if (templeHermit != null) tag.putUUID("TempleHermit", templeHermit);
		if (!medium.isEmpty()) tag.put(TAG_MEDIUM, medium.save(registries));
		if (settledCraft != null) tag.putUUID(TAG_SETTLED_CRAFT, settledCraft);
		if (formedFloor != null) {
			tag.putString(TAG_FLOOR, formedFloor.toString());
			tag.putString(TAG_FLOOR_FORWARDS, formedForwards.getSerializedName());
			tag.putLong(TAG_FLOOR_SINCE, formedSince);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		templeDisplay = tag.contains("TempleDisplay") ? BlockPos.of(tag.getLong("TempleDisplay")) : null;
		templeHermit = tag.hasUUID("TempleHermit") ? tag.getUUID("TempleHermit") : null;
		medium = tag.contains(TAG_MEDIUM)
				? ItemStack.parseOptional(registries, tag.getCompound(TAG_MEDIUM))
				: ItemStack.EMPTY;
		settledCraft = tag.hasUUID(TAG_SETTLED_CRAFT) ? tag.getUUID(TAG_SETTLED_CRAFT) : null;
		formedFloor = tag.contains(TAG_FLOOR) ? ResourceLocation.tryParse(tag.getString(TAG_FLOOR)) : null;
		Direction forwards = Direction.byName(tag.getString(TAG_FLOOR_FORWARDS));
		formedForwards = forwards == null || forwards.getAxis().isVertical() ? Direction.NORTH : forwards;
		formedSince = tag.getLong(TAG_FLOOR_SINCE);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		ResourceLocation previous = formedFloor;
		Direction previousForwards = formedForwards;
		float previousStart = getFormationStart();
		super.handleUpdateTag(tag, registries);
		loadAdditional(tag, registries);
		if (level == null) return;
		long now = level.getGameTime();
		if (previous != null && !java.util.Objects.equals(previous, formedFloor)) {
			// The old floor draws back into the Focus; a replacement only starts spreading once it has.
			fadingFloor = previous;
			fadingForwards = previousForwards;
			// A floor broken while still forming draws back from as far as it had spread.
			float reached = Math.min(1.0F, Math.max(0.0F, (now - previousStart) / CardinalRiteFloorVisualRules.FORM_TICKS));
			fadingSince = now - Math.round(CardinalRiteFloorVisualRules.COLLAPSE_TICKS * (1.0F - reached));
			clientFormStart = formedFloor == null ? Float.NaN : fadingSince + CardinalRiteFloorVisualRules.COLLAPSE_TICKS;
		} else if (previous == null && formedFloor != null && isCollapsing(now)) {
			if (formedFloor.equals(fadingFloor)) {
				// Repaired mid-collapse: grow back out from wherever the collapse had reached.
				clientFormStart = now - CardinalRiteFloorVisualRules.collapseAge(now - fadingSince);
				fadingFloor = null;
			} else {
				clientFormStart = fadingSince + CardinalRiteFloorVisualRules.COLLAPSE_TICKS;
			}
		} else if (previous == null && formedFloor != null) {
			// A fresh formation follows the server's timing.
			clientFormStart = Float.NaN;
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet,
			HolderLookup.Provider registries) {
		if (packet.getTag() != null) handleUpdateTag(packet.getTag(), registries);
	}
}
