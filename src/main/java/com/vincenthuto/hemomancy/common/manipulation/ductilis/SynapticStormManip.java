package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Comparator;

public class SynapticStormManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 60;

	public SynapticStormManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override public int getRequiredChargeTicks() { return CHARGE_TICKS; }

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, getRequiredChargeTicks() <= 0 ? 1 : heldTicks / getRequiredChargeTicks())) {

		if (!(world instanceof ServerLevel level)) return;
        float charge = ManipulationCastingRules.chargeFraction(heldTicks, CHARGE_TICKS);
        int limit=ManipulationScalingRules.scaledCount(1,8,heldTicks,CHARGE_TICKS);
        int paralysis=ManipulationScalingRules.scaledInt(10,60,heldTicks,CHARGE_TICKS);
        var candidates=level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(18),
                e -> e.distanceToSqr(player)<=18*18 && (ConductionManager.canHarm(player,e)
                        || e instanceof com.vincenthuto.hemomancy.common.entity.summon.FerricConstructEntity f
                        && ConductionManager.usableRelay(player,f))).stream()
                .sorted(Comparator.comparing(LivingEntity::getUUID)).limit(256).toList();
        var byId=new java.util.HashMap<java.util.UUID,LivingEntity>();
        candidates.forEach(e->byId.put(e.getUUID(),e));
        var visited=new java.util.HashSet<java.util.UUID>();
        var struck=new java.util.ArrayList<LivingEntity>();
        var successful=new java.util.ArrayList<LivingEntity>();
        Discharge discharge=new Discharge();
        discharge.suppressReactiveArcs();
        LivingEntity previous=player;
        for (int hops=0;hops<DuctilisRules.MAX_TRAVERSAL && struck.size()<limit;hops++) {
            var endpoints=new java.util.ArrayList<DuctilisRules.Hop>();
            for (LivingEntity candidate:candidates) {
                boolean relay=candidate instanceof com.vincenthuto.hemomancy.common.entity.summon.FerricConstructEntity;
                if (relay && !ConductionManager.energized((com.vincenthuto.hemomancy.common.entity.summon.FerricConstructEntity)candidate)) continue;
                var endpoint=candidate.getEyePosition();
                endpoints.add(new DuctilisRules.Hop(candidate.getUUID(),endpoint.x,endpoint.y,endpoint.z,
                        ConductionManager.conductive(candidate),relay));
            }
            var source=previous.getEyePosition();
            var next=DuctilisRules.nextHop(endpoints,source.x,source.y,source.z,hops==0,
                    previous!=player && ConductionManager.conductive(previous),visited,
                    n -> ConductionManager.visible(level,source,byId.get(n.id()).getEyePosition(),player));
            if (next==null) break;
            LivingEntity target=byId.get(next.id()); visited.add(next.id());
            DuctilisLightningEffects.conductiveArc(previous,target,hops);
            previous=target;
            if (next.relay()) {
                ConductionManager.energizeRelay(player,(com.vincenthuto.hemomancy.common.entity.summon.FerricConstructEntity)target,discharge);
                continue;
            }
            // Failed damage still consumes one victim slot, so invulnerable entities cannot expand the chain.
            struck.add(target);
            if (ConductionManager.claimHit(player,target,discharge)
                    && ManipulationCombatHelper.hurt(this,player,target,level,2.0F+6.0F*charge)) {
                successful.add(target);
                Paralysis.apply(target,paralysis);
                ConductionManager.energizeTouching(player,target,discharge);
            }
        }
        if (visited.isEmpty()) ConductionManager.energizeAimed(player,18,discharge);
        // Marks influence the chosen chain; they cannot create a second, unbounded path.
        discharge.finishChain(level.getGameTime());

            }
    }
}
