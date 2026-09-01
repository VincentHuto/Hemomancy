package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCastingRules;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BloodNeedleManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 20;
	private final Mode mode;

	public BloodNeedleManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		this(name, cost, alignLevel, xpCost, type, rank, tendency, section, Mode.BASELINE);
	}

	public BloodNeedleManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section, Mode mode) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
		this.mode = mode;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		getAction(player, world, heldItemMainhand, position, getRequiredChargeTicks());
	}

	@Override
	public int getRequiredChargeTicks() {
		return mode == Mode.LANCE ? 30 : CHARGE_TICKS;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks) {
		Vec3 vector3d = player.getLookAngle();
		Vector3 vector3f = new Vector3(vector3d);
		float strength = ManipulationCastingRules.chargeFraction(chargeTicks, getRequiredChargeTicks());
		int count = switch (mode) {
			case FAN -> Math.max(1, (int) Math.ceil(24 * strength));
			case LANCE -> 3;
			default -> Math.max(1, (int) Math.ceil((world.random.nextInt(11) + 10) * strength));
		};
		for (int i = 0; i < count; i++) {
			BloodNeedleEntity needle = new BloodNeedleEntity(world, player, heldItemMainhand);
			needle.setDamageTendency(getTend());
			needle.setSecondaryDamageTendency(getSecondaryTend());
			if (mode == Mode.LANCE) {
				needle.configurePiercing((byte) 3);
				needle.shoot(vector3f.x, vector3f.y, vector3f.z, 8.0F, 0.25F);
			} else if (mode == Mode.FAN) {
				needle.shoot(vector3f.x, vector3f.y, vector3f.z, 6.0F, 22.0F);
			} else {
				needle.shoot(vector3f.x, vector3f.y, vector3f.z, world.random.nextInt(5) + 4,
						world.random.nextInt(20) - world.random.nextInt(20));
			}
			world.addFreshEntity(needle);
		}
	}

	public enum Mode {
		BASELINE,
		FAN,
		LANCE
	}

}
