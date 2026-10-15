package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramAim;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramTracing;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * The local weaver's staff over their engram, each client tick: where it points (fine glow motes settle on the exact
 * pixel of the block face, the aimed node is marked, and the motes turn red where the held lane could not go), and which
 * fragment the weaver's gaze is resting on long enough to hear.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class EngramAimClient {
	private static final ParticleColor PALE = new ParticleColor(255, 214, 190);
	private static final ParticleColor REFUSED = new ParticleColor(255, 46, 34);

	private EngramAimClient() {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || mc.level == null || mc.isPaused()) return;
		if (!(player.getMainHandItem().getItem() instanceof LivingStaffItem)) {
			EngramClientState.setAim(null);
			EngramClientState.gaze(null, -1, 0);
			return;
		}
		float now = mc.level.getGameTime();
		EngramTracing.Aim trace = EngramTracing.aim(player, false);
		if (trace == null) {
			EngramClientState.setAim(null);
		} else {
			SomaticLoomBlockEntity loom = trace.loom();
			EngramBoard board = loom.getEngram();
			EngramWeaveRules.Step step = EngramWeaveRules.Step.IDLE;
			int active = loom.engramActive();
			if (board != null && trace.node() >= 0 && active >= 0 && active < board.fragmentCount()) {
				EngramBoard trial = EngramBoard.load(board.save());
				step = EngramWeaveRules.sweep(trial, active, trace.node(), stock(loom));
			}
			EngramClientState.setAim(new EngramClientState.Aim(loom.getBlockPos(), trace.node(), trace.sx(), trace.sz(), step));
			boolean refused = !step.moved() && step != EngramWeaveRules.Step.IDLE && step != EngramWeaveRules.Step.LANDED;
			ParticleColor colour = refused ? REFUSED : active >= 0 && board != null && active < board.fragmentCount()
					? EngramGlyphs.particleColour(EngramWeaveRules.colour(board, active)) : PALE;
			mark(mc, loom, trace, colour, refused, EngramTracing.isTracing(player));
		}
		EngramTracing.Aim gaze = EngramTracing.aim(player, true);
		int fragment = -1;
		if (gaze != null && gaze.node() >= 0 && gaze.loom().getEngram() != null)
			fragment = gaze.loom().getEngram().fragmentAt(gaze.node());
		EngramClientState.gaze(gaze == null ? null : gaze.loom().getBlockPos(), fragment, now);
	}

	/**
	 * Fine glow motes on the exact sixteenth of the block face the staff touches: one a tick to mark it, two while a
	 * lane is drawn (they linger, so a sweep leaves a dotted trail), three scattering wider where the step is refused.
	 */
	private static void mark(Minecraft mc, SomaticLoomBlockEntity loom, EngramTracing.Aim trace, ParticleColor colour,
			boolean refused, boolean drawing) {
		double[] p;
		if (trace.point() != null && trace.face() != null) {
			Direction face = trace.face();
			p = EngramAim.onPixel(trace.point().x, trace.point().y, trace.point().z, face.getStepX(), face.getStepY(), face.getStepZ());
		} else {
			Vec3 at = EngramSigilMesh.of(loom).pixel(trace.sx(), trace.sz());
			p = new double[]{at.x, at.y, at.z};
		}
		RandomSource random = mc.level.random;
		int motes = refused ? 3 : drawing ? 2 : 1;
		double drift = refused ? 0.008D : 0.0025D;
		for (int i = 0; i < motes; i++)
			mc.level.addParticle(HemoParticleData.glow(colour), p[0], p[1], p[2], random.nextGaussian() * drift,
					0.002D + random.nextDouble() * 0.003D, random.nextGaussian() * drift);
	}

	private static int[] stock(SomaticLoomBlockEntity loom) {
		int[] stock = new int[8];
		for (EnumBloodTendency tend : EnumBloodTendency.values()) stock[tend.ordinal()] = loom.getStoredEnzyme(tend);
		return stock;
	}
}
