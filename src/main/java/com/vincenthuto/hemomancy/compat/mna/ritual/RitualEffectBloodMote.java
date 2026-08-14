package com.vincenthuto.hemomancy.compat.mna.ritual;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.sound.SFX;
import com.mna.entities.utility.PresentItem;
import com.vincenthuto.hemomancy.client.particle.factory.AbsorbedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.block.harbinger.EngramBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.compat.mna.item.MnAPluginItemInit;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RitualEffectBloodMote extends RitualEffect {
	public RitualEffectBloodMote(ResourceLocation ritualName) {
		super(ritualName);
	}

	@Override
	public boolean applyStartCheckInCreative() {
		return true;
	}

	@Override
	protected boolean applyRitualEffect(IRitualContext context) {

		Player player = context.getCaster();
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				.orElseThrow(NullPointerException::new);
		if (!volume.wouldOverstrain(1000)) {
			volume.drain(1000);
			this.SpawnItem(context.getCenter(), context.getLevel());
			return true;
		} else {
			ItemStack offHandItem = player.getOffhandItem();
			if (offHandItem.getItem() instanceof BloodGourdItem gourd) {
				IBloodVolume itemstackCap = HemoCapabilityAccess.getBloodVolume(offHandItem)
						.orElseThrow(NullPointerException::new);
				if (!itemstackCap.wouldOverstrain(1000)) {
					itemstackCap.drain(1000);

					return true;
				}
			}
		}
		return false;
	}

	protected void SpawnItem(BlockPos ritualCenter, Level world) {
		PresentItem entity = new PresentItem(world, (float) ritualCenter.getX() + 0.5f, ritualCenter.above().getY(),
				(float) ritualCenter.getZ() + 0.5f, this.getOutputStack());
		world.addFreshEntity((Entity) entity);
	}

	@Override
	public Component canRitualStart(IRitualContext context) {
		if (!this.areEngramsPresent(context)) {
			return Component.literal("This ritual requires")
					.append(Component.literal(" MORE").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.RED)
							.withStyle(ChatFormatting.UNDERLINE))
					.withStyle(ChatFormatting.RESET).append(Component.literal(" from you..."));
		}
		return null;
	}

	private boolean areEngramsPresent(IRitualContext context) {
		BlockPos a = context.getCenter().offset(-1, 0, 0);
		BlockPos b = context.getCenter().offset(1, 0, 0);
		BlockPos c = context.getCenter().offset(0, 0, -1);
		BlockPos d = context.getCenter().offset(0, 0, 1);
		return checkPos(context, a) && checkPos(context, b) && checkPos(context, c) && checkPos(context, d);
	}

	public boolean checkPos(IRitualContext context, BlockPos pos) {
		BlockState state = context.getLevel().getBlockState(pos);
		if (state.getBlock() instanceof EngramBlock engram) {
			return state.getValue(EngramBlock.LIT);
		}
		return false;
	}

	@Override
	public SoundEvent getLoopSound(IRitualContext context) {
		return SFX.Loops.ENDER;
	}

	@Override
	public boolean spawnRitualParticles(IRitualContext context) {

		Level world = context.getLevel();

		double time = world.getGameTime() * 0.2f;

		BlockPos center = context.getCenter();
		Vector3 centerVec = new Vector3(center.getX() + .5, center.getY(), center.getZ() + .5);
		List<ParticleColor> chakraColors = new ArrayList<ParticleColor>();
		Collections.addAll(chakraColors, new ParticleColor(162, 86, 160), new ParticleColor(96, 96, 186),
				new ParticleColor(66, 184, 212), new ParticleColor(110, 200, 80), new ParticleColor(255, 165, 44),
				new ParticleColor(243, 124, 59), new ParticleColor(229, 60, 81));

		for (int j = 0; j < chakraColors.size(); j++) {
			world.addParticle(AbsorbedBloodCellParticleFactory.createData(chakraColors.get(j)),
					centerVec.x + Math.sin(time + j) + HLParticleUtils.inRange(-0.1, 0.1),
					centerVec.y + (j * 0.5) + 0.1f + HLParticleUtils.inRange(-0.1, 0.1),
					centerVec.z + Math.cos(time + j) + HLParticleUtils.inRange(-0.1, 0.1), 0, -0.05, 0);
			
			world.addParticle(AbsorbedBloodCellParticleFactory.createData(chakraColors.get(j)),
					centerVec.x + Math.sin(time + j) + HLParticleUtils.inRange(-0.1, 0.1),
					centerVec.y + (j * 0.5) + 0.1f + HLParticleUtils.inRange(-0.1, 0.1),
					centerVec.z + Math.cos(time + j) + HLParticleUtils.inRange(-0.1, 0.1), 0, -0.05, 0);



		}

		double radius = context.getRecipe().getLowerBound();
		for (float i = 0.0f; i < 360.0f; i += 15.0f) {
			double angleR = Math.toRadians(i);
			double offsetX = Math.cos(angleR) * radius;
			double offsetZ = Math.sin(angleR) * radius;
			Vector3 start = centerVec.add(offsetX, 0.0, offsetZ);
			context.getLevel().addParticle(
					(ParticleOptions) new MAParticleType(ParticleInit.ENDER.get()),
					start.x, start.y, start.z, center.getX() + .5, center.getY() + 1, center.getZ() + .5);
		}

		return true;
	}

	public ItemStack getOutputStack() {
		return new ItemStack((ItemLike) MnAPluginItemInit.mote_blood.get());
	}

	@Override
	protected int getApplicationTicks(IRitualContext arg0) {
		return 0;
	}
}
