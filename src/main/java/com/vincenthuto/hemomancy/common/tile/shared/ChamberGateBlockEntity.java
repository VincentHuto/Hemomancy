package com.vincenthuto.hemomancy.common.tile.shared;

import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPortal;
import com.vincenthuto.hemomancy.common.block.shared.LidlessTheme;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import com.vincenthuto.hemomancy.common.encounter.HuskOutsideSnapshot;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The corridor's end in a Chamber of Will: a sealed eye until its owner has a husk to walk back to, then a standing
 * portal. Its renderer draws that portal and the corridor's arches, so it also carries the room's radius.
 */
public class ChamberGateBlockEntity extends HemoBlockEntity {
	/** The ticks the eye takes to open or close. */
	public static final int EYE_TICKS = 24;
	/** How far either side of the centre line the portal's plane takes a body. */
	private static final double PLANE_HALF_WIDTH = 1.6D;
	private static final double PLANE_DEPTH = 0.6D;
	private static final int REFRESH_TICKS = 20;

	@Nullable
	private UUID owner;
	private int radius = ChamberOfWillManager.BASE_ROOM_RADIUS;
	private boolean linked;
	private LidlessTheme theme = LidlessTheme.QLIPHOTH;
	private int eye;
	private float previousEye;
	/** The overworld around the husk this gate leads back to, as the manager last took it; the client's copy. */
	@Nullable
	private HuskOutsideSnapshot outside;

	public ChamberGateBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.chamber_gate.get(), pos, state);
	}

	/** The manager telling the gate whose it is and how large a room it stands in. */
	public void configure(UUID owner, int radius) {
		if (owner.equals(this.owner) && radius == this.radius) return;
		this.owner = owner;
		this.radius = radius;
		refresh(true);
	}

	public int radius() {
		return radius;
	}

	public boolean linked() {
		return linked;
	}

	public LidlessTheme theme() {
		return theme;
	}

	@Nullable
	public UUID owner() {
		return owner;
	}

	/** How open the eye is: 0 sealed, 1 fully open. */
	public float opening(float partialTick) {
		return HemoEasing.smoothstep(0.0F, 1.0F, HemoEasing.lerp(previousEye, eye, partialTick) / EYE_TICKS);
	}

	private void stepEye() {
		previousEye = eye;
		eye = Math.clamp(eye + (linked ? 1 : -1), 0, EYE_TICKS);
	}

	public void clientTick() {
		stepEye();
	}

	public void serverTick() {
		if (!(level instanceof ServerLevel server) || owner == null) return;
		stepEye();
		if (server.getGameTime() % REFRESH_TICKS == 0) refresh(false);
		if (!linked) return;
		ServerPlayer player = server.getServer().getPlayerList().getPlayer(owner);
		if (player == null || player.level() != server || Math.abs(player.getY() - worldPosition.getY()) > 2.0D) return;
		double dx = player.getX() - (worldPosition.getX() + 0.5D);
		double dz = player.getZ() - (worldPosition.getZ() + 0.5D);
		if (Math.abs(dx) <= PLANE_HALF_WIDTH && Math.abs(dz) <= PLANE_DEPTH) LidlessPassageService.offer(player, portal());
	}

	private void refresh(boolean force) {
		if (!(level instanceof ServerLevel server) || owner == null) return;
		boolean nowLinked = ChamberOfWillManager.get(server.getServer()).hasGateway(owner);
		ServerPlayer player = server.getServer().getPlayerList().getPlayer(owner);
		LidlessTheme nowTheme = player == null ? theme : LidlessTheme.of(player);
		if (force || nowLinked != linked || nowTheme != theme) {
			linked = nowLinked;
			theme = nowTheme;
			BlockEntitySync.markAndSync(this);
		}
	}

	public LidlessPortal portal() {
		return new GatePortal();
	}

	/** The overworld around the husk, for the walk back; null until the husk has taken it. */
	@Nullable
	public HuskOutsideSnapshot outside() {
		return outside;
	}

	private void write(CompoundTag tag) {
		if (owner != null) tag.putUUID("Owner", owner);
		tag.putInt("Radius", radius);
		tag.putBoolean("Linked", linked);
		tag.putInt("Theme", theme.ordinal());
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag tag = super.getUpdateTag(provider);
		write(tag);
		if (owner != null && level instanceof ServerLevel server) {
			HuskOutsideSnapshot taken = ChamberOfWillManager.get(server.getServer()).outside(owner);
			if (taken != null) {
				CompoundTag out = new CompoundTag();
				taken.write(out);
				tag.put("Outside", out);
			}
		}
		return tag;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		write(tag);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
		radius = tag.contains("Radius") ? tag.getInt("Radius") : ChamberOfWillManager.BASE_ROOM_RADIUS;
		linked = tag.getBoolean("Linked");
		theme = LidlessTheme.byOrdinal(tag.getInt("Theme"));
		if (tag.contains("Outside")) outside = HuskOutsideSnapshot.read(tag.getCompound("Outside"));
	}

	/** The walk back: six blocks of void from the corridor's end to the husk. */
	private final class GatePortal implements LidlessPortal {
		@Override
		public BlockPos anchor() {
			return worldPosition;
		}

		@Override
		public Direction facing() {
			return Direction.NORTH;
		}

		@Override
		public boolean admits(ServerPlayer player) {
			return linked && player.getUUID().equals(owner) && !ChamberVisitService.isObservational(player);
		}

		@Override
		public boolean towardFront(ServerPlayer player) {
			return false;
		}

		@Override
		public LidlessPassageService.Mode mode() {
			return LidlessPassageService.Mode.RETURN;
		}

		@Override
		public boolean stillValid(ServerLevel level) {
			return level.getBlockEntity(worldPosition) == ChamberGateBlockEntity.this;
		}

		@Override
		public void completed(ServerPlayer player, boolean towardFront) {
			ChamberVisitService.walkOutThroughGate(player);
		}

		@Override
		public int length() {
			return LidlessPassageService.GATEWAY_LENGTH;
		}

		@Override
		public LidlessTheme theme(ServerPlayer player) {
			return LidlessTheme.of(player);
		}
	}
}
