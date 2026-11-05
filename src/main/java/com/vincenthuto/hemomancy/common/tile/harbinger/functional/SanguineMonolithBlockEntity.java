package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.MonolithFragmentDropRules;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SanguineMonolithBlock;
import com.vincenthuto.hemomancy.common.tile.shared.IMultiBlockEntity;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPortal;
import com.vincenthuto.hemomancy.common.block.shared.LidlessTheme;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.encounter.ChamberProxySnapshot;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.item.harbinger.SeedBinding;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hemomancy.common.util.StandingSpaces;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberDiscoveryEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinitions;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.item.EntityQliphothSeedItem;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.core.BlockPos;
import com.vincenthuto.hemomancy.common.util.ClientBridge;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class SanguineMonolithBlockEntity extends HemoBlockEntity
		implements IMultiBlockEntity, com.vincenthuto.hemomancy.common.tile.ProjectedBloodSink {

	/** Duration of the placement rise animation in ticks (~1.5 s). */
	public static final int RISE_DURATION_TICKS = 30;
	/** The ticks the third eye takes to open or close. */
	public static final int EYE_TICKS = 24;
	/** Fed ticks of Blood Projection that wake the eye, and what each one costs. */
	public static final int GATE_CHARGE_TICKS = MonolithEyeRules.CHARGE_TICKS;
	public static final double GATE_BLOOD_PER_TICK = 2.5D;
	/** How long the woken eye stays open before it closes on its own. */
	public static final int GATE_OPEN_TICKS = 200;
	/** How long it stands open behind someone walking out of it. */
	private static final int GATE_EXIT_TICKS = 60;
	private static final int GATE_FEED_GRACE = 4;
	private static final BloodAccounting.Source GATE = BloodAccounting.Source.of("monolith_gate", BloodFlowContribution.Category.WORLD, true, false);
	private static final ParticleColor SPORE_MOTE = new ParticleColor(214, 112, 28);

	/** Ticks since the monolith was placed; used for ambient glow animation. */
	private int tickCount = 0;
	/** Number of tier-7+ interactions, used for instability/explosion behavior. */
	private int archonInteractions = 0;

	/**
	 * World game-time when this monolith was first placed, or {@code -1} if
	 * unknown (pre-existing block, loaded from save before this feature existed).
	 */
	private long placedGameTime = -1L;

	/** Ticks since the break began, or -1 while the stone is whole. Counts on both sides; the server's count wins. */
	private int breakTicks = -1;
	/** The Archon who broke the stone: the only one the gap between the halves admits. */
	@Nullable
	private UUID owner;
	/** Whether the halves have fallen (the block state carries it too, for collision and loot). */
	private boolean broken;
	/** Whether the Seed has been taken out of the void; while false the portal stays open. */
	private boolean seedTaken;
	/** Whether the owner has woken the eye and it stands open for them. */
	private boolean gateOpen;
	private int gateOpenTicks;
	/** Fed ticks toward waking the eye; bleeds off when the feeding stops. */
	private int gateCharge;
	/** The charge as this side shows it: walks toward {@link #gateCharge} a tick at a time ({@link MonolithEyeRules}). */
	private int shownCharge;
	private int previousShownCharge;
	/** The light in the cracks, in ticks of {@link MonolithEyeRules#CHARGE_TICKS}; a fresh break is lit. */
	private int glowTicks = MonolithEyeRules.CHARGE_TICKS;
	private int previousGlowTicks = MonolithEyeRules.CHARGE_TICKS;
	private long lastFed = Long.MIN_VALUE / 2;
	/** How open the eye is, in ticks of {@link #EYE_TICKS}; counted on both sides. */
	private int eye;
	private int previousEye;
	/** An Apotheos has projected at this stone: it is infested for good. */
	private boolean overgrown;
	private LidlessTheme theme = LidlessTheme.QLIPHOTH;
	/** The owner's room as last seen, for the eye to show; null until the husk is a gateway. */
	@Nullable
	private ChamberProxySnapshot proxy;
	/** Client only: the game time of the next lightning strike the renderer owes the broken stone. */
	public long clientNextStrike = -1L;

	public SanguineMonolithBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.sanguine_monolith.get(), pos, state);
	}

	public AABB getRenderBoundingBox() {
		return IMultiBlockEntity.computeMultiBlockAABB(this).inflate(3.5, 1.0, 3.5);
	}

	public int getTickCount() {
		return tickCount;
	}

	public int getArchonInteractions() {
		return archonInteractions;
	}

	/** Increments and returns the count of archon-tier interactions. */
	public int incrementArchonInteractions() {
		archonInteractions++;
		setChanged();
		return archonInteractions;
	}

	/** Called server-side immediately after placement to record the rise start time. */
	public void startRise(long gameTime) {
		this.placedGameTime = gameTime;
		setChanged();
	}

	/**
	 * Returns a value in [0, 1] representing how far the rise animation has
	 * progressed. Returns 1.0 if the animation is complete or was never started.
	 */
	public float getRiseProgress(float partialTick) {
		if (level == null || placedGameTime < 0) return 1.0f;
		float ticks = (level.getGameTime() - placedGameTime) + partialTick;
		return Math.min(1.0f, ticks / RISE_DURATION_TICKS);
	}

	// ── The break ─────────────────────────────────────────────────────────────

	/** The Archon's second press: the stone starts to crack for them. A breaking or broken stone ignores it. */
	public void beginBreak(ServerPlayer player) {
		if (isBreaking() || isBroken()) return;
		breakTicks = 0;
		owner = player.getUUID();
		broken = false;
		seedTaken = false;
		theme = LidlessTheme.of(player);
		BlockEntitySync.markAndSync(this);
	}

	public boolean isBreaking() {
		return breakTicks >= 0 && !broken;
	}

	public boolean isBroken() {
		return broken;
	}

	public int breakTicks() {
		return breakTicks;
	}

	/** 0 while whole, 1 when the halves have come to rest. */
	public float breakProgress(float partialTick) {
		if (breakTicks < 0) return 0.0F;
		if (broken) return 1.0F;
		return Math.min(1.0F, (breakTicks + partialTick) / SanguineMonolithBlock.BREAK_TICKS);
	}

	/** How far the cracks have spread: 0 whole, 1 at the split. */
	public float crackProgress(float partialTick) {
		if (breakTicks < 0) return 0.0F;
		if (broken) return 1.0F;
		return Math.min(1.0F, (breakTicks + partialTick) / SanguineMonolithBlock.CRACK_TICKS);
	}

	/** Whether the Seed is still in the void behind the halves. */
	public boolean seedInside() {
		return broken && !seedTaken;
	}

	/**
	 * Whether the gap between the halves lets this player through: the owner, while the Seed waits or the eye is open,
	 * and only from the front, where the eye is. From behind the stone stays solid to them.
	 */
	public boolean admits(Player player) {
		return (seedInside() || gateOpen) && owner != null && owner.equals(player.getUUID()) && fromFront(player);
	}

	/** Whether the player stands on the front side of the stone's plane (its centre counts as the front). */
	public boolean fromFront(Player player) {
		Vec3 facingVec = Vec3.atLowerCornerOf(front().getNormal());
		return player.position().subtract(Vec3.atBottomCenterOf(worldPosition)).dot(facingVec) >= -0.05D;
	}

	public boolean gateOpen() {
		return gateOpen;
	}

	public boolean overgrown() {
		return overgrown;
	}

	public LidlessTheme theme() {
		return theme;
	}

	@Nullable
	public ChamberProxySnapshot proxy() {
		return proxy;
	}

	/** Takes the owner's room afresh for the eye to show: when it wakes, when it opens for an exit, when it is linked. */
	private void refreshProxy() {
		if (owner == null || !(level instanceof ServerLevel server)) return;
		ChamberOfWillManager manager = ChamberOfWillManager.get(server.getServer());
		if (!manager.hasGateway(owner)) return;
		proxy = ChamberProxySnapshot.take(manager, server.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL), owner,
				server.getServer());
		// And the other way: the overworld around this stone, for the walk back out.
		manager.storeOutside(owner, com.vincenthuto.hemomancy.common.encounter.HuskOutsideSnapshot.take(server, worldPosition, front()),
				server.getServer());
	}

	public Direction front() {
		return getBlockState().getValue(SanguineMonolithBlock.FACING);
	}

	/** How open the third eye is: 0 shut, 1 fully open. While it is being fed the slit trembles a little wider. */
	public float opening(float partialTick) {
		float eased = HemoEasing.smoothstep(0.0F, 1.0F, HemoEasing.lerp(previousEye, eye, partialTick) / EYE_TICKS);
		return Math.max(eased, MonolithEyeRules.tremble(HemoEasing.lerp(previousShownCharge, shownCharge, partialTick)));
	}

	/** How lit the cracks are: 0 dark, 1 blood-hot to the top of the halves. */
	public float glow(float partialTick) {
		return HemoEasing.lerp(previousGlowTicks, glowTicks, partialTick) / MonolithEyeRules.CHARGE_TICKS;
	}

	private void stepEye() {
		previousEye = eye;
		eye = Math.clamp(eye + (seedInside() || gateOpen ? 1 : -1), 0, EYE_TICKS);
		previousShownCharge = shownCharge;
		shownCharge = MonolithEyeRules.shownCharge(shownCharge, gateCharge, gateOpen,
				HemoEasing.smoothstep(0.0F, 1.0F, (float) eye / EYE_TICKS));
		previousGlowTicks = glowTicks;
		glowTicks = MonolithEyeRules.glowTicks(glowTicks, shownCharge, seedInside() || gateOpen);
	}

	/** Where someone leaving the crossing stands: the cell on {@code side}, else somewhere near it. */
	public Vec3 standingBeside(Player player, Direction side) {
		if (!(level instanceof ServerLevel server)) return Vec3.atBottomCenterOf(worldPosition.relative(side));
		BlockPos cell = worldPosition.relative(side);
		// Someone walking back from the Chamber may be the first thing to need this cell's chunk, and a husk can stand on
		// a chunk's edge: an unloaded cell reads as nowhere safe to stand.
		server.getChunkAt(cell);
		Vec3 stand = Vec3.atBottomCenterOf(cell);
		if (StandingSpaces.safe(player, server, stand)) return stand;
		Vec3 near = StandingSpaces.near(player, server, cell.below());
		if (near == null) near = StandingSpaces.near(player, server, worldPosition.relative(side.getOpposite()).below());
		return near != null ? near : Vec3.atBottomCenterOf(worldPosition.above(2));
	}

	/** The eye stands open a moment behind someone walking out of it. */
	public void openBriefly() {
		gateOpen = true;
		gateOpenTicks = GATE_EXIT_TICKS;
		refreshProxy();
		BlockEntitySync.markAndSync(this);
	}

	/**
	 * Blood projected at a husk whose Seed is gone: its owner wakes the eye. Each fed tick pays and adds charge, and
	 * full charge opens it. An Apotheos's blood starts a fungal infestation in the stone the first time, for good.
	 */
	@Override
	public double takeProjectedBlood(Player player, BlockPos hitPos, double rate, boolean livingStaff) {
		if (!(level instanceof ServerLevel world) || !(player instanceof ServerPlayer server)) return 0.0D;
		if (!broken || seedInside() || gateOpen) return 0.0D;
		if (owner == null || !owner.equals(server.getUUID())) {
			server.displayClientMessage(Component.translatable("hemomancy.monolith.gate.refused"), true);
			return 0.0D;
		}
		if (world.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
			server.displayClientMessage(Component.translatable("hemomancy.monolith.gate.inside"), true);
			return 0.0D;
		}
		if (ChamberVisitService.isAway(server)) {
			server.displayClientMessage(Component.translatable("hemomancy.monolith.gate.elsewhere"), true);
			return 0.0D;
		}
		long now = world.getGameTime();
		if (lastFed == now) return 0.0D;
		var volume = HemoCapabilityAccess.getBloodVolume(server);
		if (!volume.isActive() || volume.getBloodVolume() < GATE_BLOOD_PER_TICK) {
			server.displayClientMessage(Component.translatable("hemomancy.monolith.gate.no_blood"), true);
			return 0.0D;
		}
		BloodAccounting.pay(server, GATE_BLOOD_PER_TICK, GATE);
		lastFed = now;
		theme = LidlessTheme.of(server);
		if (theme == LidlessTheme.APOTHEOS && !overgrown) {
			overgrown = true;
			server.displayClientMessage(Component.translatable("hemomancy.monolith.overgrown"), false);
			Vec3 at = Vec3.atCenterOf(worldPosition).add(0.0D, 0.5D, 0.0D);
			world.sendParticles(HemoParticleData.glow(SPORE_MOTE), at.x, at.y, at.z, 40, 0.9D, 0.8D, 0.9D, 0.02D);
			BlockEntitySync.markAndSync(this);
		}
		gateCharge++;
		if (gateCharge % 4 == 0) {
			Vec3 at = Vec3.atCenterOf(worldPosition).add(0.0D, 0.5D, 0.0D);
			world.sendParticles(HemoParticleData.glow(new ParticleColor(150, 10, 12)), at.x, at.y, at.z, 3, 0.1D, 0.5D, 0.1D, 0.0D);
		}
		if (gateCharge >= GATE_CHARGE_TICKS) {
			gateCharge = 0;
			gateOpen = true;
			gateOpenTicks = GATE_OPEN_TICKS;
			refreshProxy();
			world.playSound(null, worldPosition, SoundInit.ITEM_SYNAPTIC_STEP_USE.get(), SoundSource.BLOCKS, 1.0F, 0.5F);
			BlockEntitySync.markAndSync(this);
		} else if (gateCharge % 4 == 0) {
			BlockEntitySync.markAndSync(this);
		}
		return GATE_BLOOD_PER_TICK;
	}

	private void tickGate() {
		if (gateOpen && --gateOpenTicks <= 0) {
			gateOpen = false;
			BlockEntitySync.markAndSync(this);
		}
		if (gateCharge > 0 && level != null && level.getGameTime() - lastFed > GATE_FEED_GRACE) {
			gateCharge = Math.max(0, gateCharge - 2);
			if (gateCharge % 4 == 0) BlockEntitySync.markAndSync(this);
		}
	}

	/** This stone is going: the owner's corridor gate no longer leads here. */
	public void forgetGateway() {
		if (owner != null && level instanceof ServerLevel server) {
			ChamberOfWillManager.get(server.getServer()).clearGateway(owner, server.dimension().location(), worldPosition);
		}
	}

	private void linkGateway(ServerPlayer player) {
		if (level instanceof ServerLevel server && !server.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
			ChamberOfWillManager.get(server.getServer()).linkGateway(player.getUUID(), server.dimension().location(), worldPosition);
			refreshProxy();
		}
	}

	@Nullable
	public UUID owner() {
		return owner;
	}

	/** The portal between the halves. */
	public LidlessPortal portal() {
		return new MonolithPortal();
	}

	/**
	 * Hands the Seed to the player: into the inventory, or floating at their feet when nothing fits. Closes the
	 * portal.
	 */
	public void takeSeed(ServerPlayer player) {
		if (!seedInside()) return;
		seedTaken = true;
		ItemStack seed = new ItemStack(ItemInit.qliphoth_seed.get());
		SeedBinding.bind(seed, player.getUUID(), player.getGameProfile().getName());
		linkGateway(player);
		LiberDiscoveryEvents.unlockForItem(player, seed.copy());
		// Through the item handler, never Inventory.add: in Creative that reports success while discarding the stack.
		ItemStack remainder = net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(
				new net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper(player.getInventory()), seed, false);
		if (!remainder.isEmpty()) dropSeed(player.level(), player.position().add(0.0D, 0.25D, 0.0D), remainder);
		BlockEntitySync.markAndSync(this);
	}

	/** The Seed as its own item entity, at once: the plain drop would only become one on a later tick. */
	private static void dropSeed(Level level, Vec3 at, ItemStack stack) {
		EntityQliphothSeedItem entity = new EntityQliphothSeedItem(EntityInit.qliphoth_seed_item.get(), level, at.x, at.y, at.z, stack);
		entity.setDefaultPickUpDelay();
		level.addFreshEntity(entity);
	}

	/** Drops the Seed at the stone when the husk is broken with the Seed still inside. */
	public void spillSeed(Level level) {
		if (!seedInside() || level.isClientSide) return;
		seedTaken = true;
		ItemStack seed = new ItemStack(ItemInit.qliphoth_seed.get());
		// A stone broken with its Seed inside still gives that Seed only to the Archon who opened it.
		if (owner != null && level.getServer() != null) {
			String name = level.getServer().getProfileCache() == null ? owner.toString()
					: level.getServer().getProfileCache().get(owner).map(com.mojang.authlib.GameProfile::getName).orElse(owner.toString());
			SeedBinding.bind(seed, owner, name);
		}
		dropSeed(level, Vec3.atCenterOf(worldPosition), seed);
		setChanged();
	}

	/** Server-side tick. */
	public void tick() {
		tickCount++;
		if (tickCount % 100 == 0) {
			setChanged();
		}
		if (isBreaking()) {
			breakTicks++;
			if (breakTicks == SanguineMonolithBlock.CRACK_TICKS) blast();
			if (breakTicks >= SanguineMonolithBlock.BREAK_TICKS) fall();
			else if (breakTicks % 20 == 0) BlockEntitySync.markAndSync(this);
		}
		if (broken) {
			tickGate();
			stepEye();
		}
	}

	/** The cracks have run their course: the stone blasts apart, the same tick the halves are thrown open. */
	private void blast() {
		if (level == null || broken) return;
		int fragments = MonolithFragmentDropRules.rollFragmentCount(level.random);
		Direction facing = getBlockState().getValue(SanguineMonolithBlock.FACING);
		Block.popResource(level, worldPosition.relative(facing.getClockWise()), new ItemStack(ItemInit.monolith_fragment.get(), fragments / 2));
		Block.popResource(level, worldPosition.relative(facing.getCounterClockWise()), new ItemStack(ItemInit.monolith_fragment.get(), fragments - fragments / 2));
		if (level instanceof ServerLevel serverLevel) {
			SanguineMonolithBlock.shatterBurst(serverLevel, worldPosition);
		}
		BlockEntitySync.markAndSync(this);
	}

	/** The halves have landed: the state turns broken and the gap between them opens. */
	private void fall() {
		if (level == null || broken) return;
		broken = true;
		BlockState state = getBlockState();
		if (state.hasProperty(SanguineMonolithBlock.BROKEN) && !state.getValue(SanguineMonolithBlock.BROKEN)) {
			level.setBlock(worldPosition, state.setValue(SanguineMonolithBlock.BROKEN, true), Block.UPDATE_ALL);
		}
		BlockEntitySync.markAndSync(this);
	}

	/** Client-side tick: spawns ground-eruption particles during the rise window; counts the break between syncs. */
	public void clientTick() {
		Level lvl = getLevel();
		if (lvl == null || !lvl.isClientSide) return;
		if (isBreaking()) breakTicks++;
		if (broken) stepEye();
		if (placedGameTime < 0) return;

		long ticksAlive = lvl.getGameTime() - placedGameTime;
		if (ticksAlive < 0 || ticksAlive >= RISE_DURATION_TICKS) return;

		// The ground gives way around the stone as it comes up: its own block breaking, with umbral fog rolling out.
		float progress = ticksAlive / (float) RISE_DURATION_TICKS;
		float strength = 1.0f - progress * 0.7f;
		BlockState ground = lvl.getBlockState(worldPosition.below());
		boolean alongX = !getBlockState().hasProperty(SanguineMonolithBlock.FACING)
				|| getBlockState().getValue(SanguineMonolithBlock.FACING).getAxis() == Direction.Axis.Z;
		double halfX = alongX ? 0.5 : 0.25;
		double halfZ = alongX ? 0.25 : 0.5;
		int debris = (int) (9 * strength) + 2;
		if (!ground.isAir()) {
			for (int i = 0; i < debris; i++) {
				var edge = riseEdge(lvl, halfX, halfZ);
				lvl.addParticle(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK, ground),
						edge.x, edge.y, edge.z,
						(edge.x - worldPosition.getX() - 0.5) * 0.5, 0.12 + lvl.random.nextDouble() * 0.28,
						(edge.z - worldPosition.getZ() - 0.5) * 0.5);
			}
		}
		int fog = lvl.random.nextFloat() < 0.75f * strength ? 2 : 1;
		for (int i = 0; i < fog; i++) {
			var edge = riseEdge(lvl, halfX, halfZ);
			var drift = new net.minecraft.world.phys.Vec3((edge.x - worldPosition.getX() - 0.5) * 0.05, 0.012 + lvl.random.nextDouble() * 0.02,
					(edge.z - worldPosition.getZ() - 0.5) * 0.05);
			ClientBridge.get().umbralMist(edge, drift, 0.22f + lvl.random.nextFloat() * 0.2f, 24 + lvl.random.nextInt(16));
		}
	}

	/** A random point on the ground just outside the stone's footprint, where it is breaking through. */
	private net.minecraft.world.phys.Vec3 riseEdge(Level lvl, double halfX, double halfZ) {
		double along = lvl.random.nextDouble() * 2.0 - 1.0;
		double out = 1.0 + lvl.random.nextDouble() * 0.25;
		double x;
		double z;
		// Weighted by edge length, so the long faces throw up the most.
		if (lvl.random.nextDouble() < halfX / (halfX + halfZ)) {
			x = along * halfX;
			z = (lvl.random.nextBoolean() ? halfZ : -halfZ) * out;
		} else {
			x = (lvl.random.nextBoolean() ? halfX : -halfX) * out;
			z = along * halfZ;
		}
		return new net.minecraft.world.phys.Vec3(worldPosition.getX() + 0.5 + x, worldPosition.getY() + 0.05 + lvl.random.nextDouble() * 0.1,
				worldPosition.getZ() + 0.5 + z);
	}

	private void writeBreak(CompoundTag tag) {
		tag.putInt("BreakTicks", breakTicks);
		if (owner != null) tag.putUUID("Owner", owner);
		tag.putBoolean("Broken", broken);
		tag.putBoolean("SeedTaken", seedTaken);
		tag.putBoolean("GateOpen", gateOpen);
		tag.putInt("GateOpenTicks", gateOpenTicks);
		tag.putInt("GateCharge", gateCharge);
		tag.putBoolean("Overgrown", overgrown);
		tag.putInt("Theme", theme.ordinal());
		if (proxy != null) {
			CompoundTag room = new CompoundTag();
			proxy.write(room);
			tag.put("Proxy", room);
		}
	}

	private void readBreak(CompoundTag tag) {
		breakTicks = tag.contains("BreakTicks") ? tag.getInt("BreakTicks") : -1;
		owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
		broken = tag.getBoolean("Broken");
		seedTaken = tag.getBoolean("SeedTaken");
		gateOpen = tag.getBoolean("GateOpen");
		gateOpenTicks = tag.getInt("GateOpenTicks");
		gateCharge = tag.getInt("GateCharge");
		overgrown = tag.getBoolean("Overgrown");
		theme = LidlessTheme.byOrdinal(tag.getInt("Theme"));
		proxy = tag.contains("Proxy") ? ChamberProxySnapshot.read(tag.getCompound("Proxy")) : null;
		// Loaded with the world, not a sync: the eye starts where it was left.
		if (level == null) {
			eye = seedInside() || gateOpen ? EYE_TICKS : 0;
			previousEye = eye;
			glowTicks = seedInside() || gateOpen ? MonolithEyeRules.CHARGE_TICKS : 0;
			previousGlowTicks = glowTicks;
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag tag = super.getUpdateTag(provider);
		tag.putInt("TickCount", tickCount);
		tag.putInt("ArchonInteractions", archonInteractions);
		if (placedGameTime >= 0) tag.putLong("PlacedGameTime", placedGameTime);
		writeBreak(tag);
		return tag;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		tag.putInt("TickCount", tickCount);
		tag.putInt("ArchonInteractions", archonInteractions);
		if (placedGameTime >= 0) tag.putLong("PlacedGameTime", placedGameTime);
		writeBreak(tag);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		tickCount = tag.getInt("TickCount");
		archonInteractions = tag.getInt("ArchonInteractions");
		placedGameTime = tag.contains("PlacedGameTime") ? tag.getLong("PlacedGameTime") : -1L;
		readBreak(tag);
	}

	/** The gap between the fallen halves: the owner walks a seed crossing and takes the Seed at its end. */
	private final class MonolithPortal implements LidlessPortal {
		@Override
		public BlockPos anchor() {
			return worldPosition;
		}

		@Override
		public Direction facing() {
			return getBlockState().getValue(SanguineMonolithBlock.FACING);
		}

		@Override
		public boolean admits(ServerPlayer player) {
			return SanguineMonolithBlockEntity.this.admits(player);
		}

		@Override
		public boolean towardFront(ServerPlayer player) {
			Vec3 facingVec = Vec3.atLowerCornerOf(facing().getNormal());
			return player.position().subtract(Vec3.atBottomCenterOf(worldPosition)).dot(facingVec) < 0;
		}

		@Override
		public LidlessPassageService.Mode mode() {
			return seedInside() ? LidlessPassageService.Mode.SEED : LidlessPassageService.Mode.GATE;
		}

		@Override
		public boolean stillValid(ServerLevel level) {
			return level.getBlockEntity(worldPosition) == SanguineMonolithBlockEntity.this && broken;
		}

		@Override
		public int length() {
			return seedInside() ? LidlessPortal.super.length() : LidlessPassageService.GATEWAY_LENGTH;
		}

		@Override
		public LidlessTheme theme(ServerPlayer player) {
			return LidlessTheme.of(player);
		}

		@Override
		public void completed(ServerPlayer player, boolean towardFront) {
			if (seedInside()) {
				takeSeed(player);
				if (FungalWhisperDialogueTrees.shouldOfferMemoWhisper(player, MemoDefinitions.FUNGAL_WHISPER_TRUTH)) {
					PacketHandler.sendToPlayer(player, new OpenDialoguePacket(FungalWhisperDialogueTrees.postMonolithShatter()));
				}
				return;
			}
			gateOpen = false;
			linkGateway(player);
			BlockEntitySync.markAndSync(SanguineMonolithBlockEntity.this);
			Direction from = towardFront ? facing().getOpposite() : facing();
			Vec3 stand = standingBeside(player, from);
			if (!ChamberVisitService.beginGatewayVisit(player, stand, from.toYRot())) {
				// Something took the visit away between stepping in and arriving: they step back out.
				player.teleportTo(stand.x, stand.y, stand.z);
			}
		}
	}
}
