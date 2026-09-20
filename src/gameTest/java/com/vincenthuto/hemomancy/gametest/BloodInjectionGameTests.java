package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("blood_injection_validation")
@PrefixGameTestTemplate(false)
public final class BloodInjectionGameTests {
    private static net.minecraft.world.entity.player.Player educatedPlayer(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(1);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.clinicalBlood(player)
                .learn(com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress.Lesson.INJECTION);
        return player;
    }
    @GameTest(template = "empty")
    public static void injectionAnimationPacketsTrackOutcomeAndPhysicalHand(GameTestHelper h) {
        for (var dominant : net.minecraft.world.entity.HumanoidArm.values()) {
            for (var hand : InteractionHand.values()) {
                var capture = animationPlayer(h);
                var player = capture.player();
                player.setMainArm(dominant);
                var sample = vial("minecraft:pig");
                player.setItemInHand(hand, sample);
                sample.use(h.getLevel(), player, hand);
                for (int i = 0; i < 16; i++) player.doTick();
                h.assertTrue(capture.packets().size() == 2, "Expected start and exactly one impact: " + capture.packets());
                var start = capture.packets().getFirst();
                var impact = capture.packets().getLast();
                h.assertTrue(start.phase() == com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection.Phase.START
                        && impact.phase() == com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection.Phase.IMPACT,
                        "Successful injection cancelled or never impacted");
                h.assertTrue(start.hand() == hand && start.right() == ((hand == InteractionHand.MAIN_HAND)
                        == (dominant == net.minecraft.world.entity.HumanoidArm.RIGHT)), "Wrong physical injection arm");
                h.assertTrue(BloodSampleData.isFilled(start.vial()) && !BloodSampleData.isFilled(impact.vial()),
                        "Animated vial did not change from full to empty");
                for (var packet : capture.packets()) {
                    var buf = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), h.getLevel().registryAccess());
                    try {
                        var codec = com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection.STREAM_CODEC;
                        codec.encode(buf, packet);
                        var decoded = codec.decode(buf);
                        h.assertTrue(decoded.entityId() == player.getId() && decoded.startedAt() == start.startedAt()
                                && decoded.phase() == packet.phase() && decoded.hand() == hand
                                && decoded.right() == start.right() && decoded.tendency() == packet.tendency()
                                && ItemStack.matches(decoded.vial(), packet.vial()), "Animation packet lost presentation data");
                    } finally { buf.release(); }
                }
                player.discard();
            }
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void interruptedAndRejectedInjectionsSendCancelWithoutImpact(GameTestHelper h) {
        for (boolean release : new boolean[]{true, false}) {
            var capture = animationPlayer(h);
            var player = capture.player();
            var sample = vial("minecraft:pig");
            player.setItemInHand(InteractionHand.OFF_HAND, sample);
            sample.use(h.getLevel(), player, InteractionHand.OFF_HAND);
            for (int i = 0; i < 7; i++) player.doTick();
            if (release) player.releaseUsingItem();
            else {
                player.addEffect(new MobEffectInstance(EffectInit.transfusion_saturation, 400));
                for (int i = 0; i < 9; i++) player.doTick();
            }
            h.assertTrue(capture.packets().size() == 2 && capture.packets().getLast().phase()
                    == com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection.Phase.CANCEL,
                    "Interrupted/rejected use produced an impact or left animation active");
            h.assertTrue(BloodSampleData.isFilled(player.getOffhandItem()), "Cancellation consumed sample");
            player.discard();
        }
        h.succeed();
    }

    private static AnimationCapture animationPlayer(GameTestHelper h) {
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "vial-animation"), false);
        var player = new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(), h.getLevel(),
                cookie.gameProfile(), cookie.clientInformation());
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(1);
        com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.clinicalBlood(player)
                .learn(com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress.Lesson.INJECTION);
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        var packets = new java.util.ArrayList<com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection>();
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {
                if (packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket payload
                        && payload.payload() instanceof com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection animation)
                    packets.add(animation);
            }
        };
        player.setPos(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1))));
        h.getLevel().addNewPlayer(player);
        return new AnimationCapture(player, packets);
    }

    @GameTest(template = "empty")
    public static void switchingToAnIdenticalVialCancelsCosmeticRecovery(GameTestHelper h) {
        for (var hand : InteractionHand.values()) {
            var capture = animationPlayer(h);
            var player = capture.player();
            var sample = vial("minecraft:pig");
            player.setItemInHand(hand, sample);
            sample.use(h.getLevel(), player, hand);
            for (int i = 0; i < 16; i++) player.doTick();
            var identicalEmpty = player.getItemInHand(hand).copy();
            if (hand == InteractionHand.MAIN_HAND) {
                player.getInventory().items.set(1, identicalEmpty);
                player.getInventory().selected = 1;
            } else player.setItemInHand(hand, identicalEmpty);
            player.doTick();
            h.assertTrue(capture.packets().size() == 3 && capture.packets().getLast().phase()
                    == com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection.Phase.CANCEL,
                    "Switching to another identical vial did not cancel recovery");
            h.assertTrue(player.getItemInHand(hand) == identicalEmpty, "Cosmetic cancellation changed inventory");
            player.discard();
        }
        h.succeed();
    }

    private record AnimationCapture(net.minecraft.server.level.ServerPlayer player,
            java.util.List<com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection> packets) { }

    private static ItemStack vial(String source) {
        var stack = new ItemStack(ItemInit.bloody_vial.get());
        var tag = new CompoundTag();
        tag.putString("entity_type", source);
        tag.putString("addon_marker", "preserve");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
    @GameTest(template = "empty")
    public static void unreadableSamplesRemainFilled(GameTestHelper h) {
        for (var source : new String[]{"absent:creature", "Malformed source", ""}) {
            var stack = vial(source);
            h.assertTrue(BloodVialItem.getEntityType(stack) == null, "Unresolved source resolved");
            h.assertTrue(!VialRackItem.isEmptyVial(stack), "Missing source became empty glass");
            var before = stack.copy();
            var player = educatedPlayer(h);
            ItemInit.bloody_vial.get().onLeftClickEntity(stack, player, h.spawn(net.minecraft.world.entity.EntityType.PIG, 0, 2, 0));
            h.assertTrue(ItemStack.isSameItemSameComponents(before, stack), "Sampling overwrote unreadable blood");
            h.assertTrue(!BloodSampleData.identify(stack), "Unreadable sample was identified");
        }
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void identificationAndLegacyTags(GameTestHelper h) {
        var a = vial("minecraft:pig"); var b = a.copy();
        h.assertTrue(BloodSampleData.identify(a), "Registered source could not be examined");
        h.assertTrue(!ItemStack.isSameItemSameComponents(a,b), "Knowledge not part of identity");
        BloodSampleData.identify(b);
        h.assertTrue(ItemStack.isSameItemSameComponents(a,b), "Identical examinations differ");
        var snapshot = BloodInjectionData.snapshot(false);
        var pig = BloodSampleData.profile(a,false);
        var bear = BloodSampleData.profile(vial("minecraft:polar_bear"),false);
        h.assertTrue(pig.tendencies().contains(com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.ANIMUS), "Pig lost Animus");
        h.assertTrue(!bear.tendencies().contains(com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.ANIMUS), "Polar bear gained Animus");
        h.assertTrue(bear.tendencies().contains(com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.CONGEATIO), "Bear lost Congeatio");
        h.assertTrue(snapshot.resolve(bear).drawbacks().size() == 1, "Cold property lost Congeatio drawback");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void bothHandsReturnExactlyOneEmptyVessel(GameTestHelper h) {
        for (var hand : InteractionHand.values()) {
            var player = educatedPlayer(h);
            for(int slot=0;slot<player.getInventory().items.size();slot++) player.getInventory().items.set(slot,new ItemStack(Items.STONE,64));
            var stack = vial("minecraft:pig"); player.setItemInHand(hand,stack);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,600,2));
            stack.use(h.getLevel(),player,hand);
            h.assertTrue(player.isUsingItem(), "Filled vial did not start use");
            for (int i=0;i<16;i++) player.tick();
            var result = player.getItemInHand(hand);
            h.assertTrue(result.is(ItemInit.bloody_vial.get()) && result.getCount()==1 && !BloodSampleData.isFilled(result), "Vessel not returned once");
            h.assertTrue(player.hasEffect(EffectInit.transfusion_saturation), "No lockout");
            h.assertTrue(player.getEffect(MobEffects.REGENERATION).getAmplifier()==2, "Stronger buff replaced");
            h.assertTrue(result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("addon_marker").equals("preserve"), "Opaque data discarded");
        }
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void interruptedAndSaturatedUsesPreserveSample(GameTestHelper h) {
        var player = educatedPlayer(h);
        var stack=vial("minecraft:pig"); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        stack.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        for(int i=0;i<7;i++) player.tick();
        player.releaseUsingItem();
        h.assertTrue(BloodSampleData.isFilled(stack) && !player.hasEffect(EffectInit.transfusion_saturation), "Interrupted injection applied");
        stack.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        player.addEffect(new MobEffectInstance(EffectInit.transfusion_saturation,400));
        for(int i=0;i<16;i++) player.tick();
        h.assertTrue(BloodSampleData.isFilled(player.getMainHandItem()) && !player.hasEffect(MobEffects.REGENERATION), "Completion bypassed saturation");
        stack.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(!player.isUsingItem(), "Saturated start allowed");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void creativeRetainsSampleAndRackNeverInjects(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.CREATIVE); player.getAbilities().instabuild=true;
        var stack=vial("minecraft:pig"); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        stack.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        for(int i=0;i<16;i++) player.tick();
        h.assertTrue(player.getMainHandItem()==stack && BloodSampleData.isFilled(stack), "Creative sample consumed");
        h.assertTrue(player.hasEffect(EffectInit.transfusion_saturation), "Creative validation skipped");
        player.removeAllEffects();
        var rack=new ItemStack(ItemInit.vial_rack.get()); var vials=VialRackItem.getVials(rack);
        vials.set(0,stack.copy()); VialRackItem.setVials(rack,vials);
        player.setItemInHand(InteractionHand.MAIN_HAND,rack);
        rack.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(!player.isUsingItem() && !player.hasEffect(EffectInit.transfusion_saturation), "Rack injected");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void switchingHandsDeathAndSpectatorPreventCompletion(GameTestHelper h) {
        for (int scenario=0; scenario<3; scenario++) {
            var player=educatedPlayer(h);
            var stack=vial("minecraft:pig");player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            stack.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
            for(int i=0;i<8;i++) player.tick();
            if(scenario==0) {
                player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                player.setItemInHand(InteractionHand.OFF_HAND,stack);
            } else if(scenario==1) player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));
            else player.setHealth(0);
            for(int i=0;i<16;i++) player.tick();
            h.assertTrue(BloodSampleData.isFilled(stack) && !player.hasEffect(EffectInit.transfusion_saturation), "Interrupted actor injected");
        }
        var spectator=h.makeMockPlayer(GameType.SPECTATOR);
        var stack=vial("minecraft:pig");spectator.setItemInHand(InteractionHand.MAIN_HAND,stack);
        stack.use(h.getLevel(),spectator,InteractionHand.MAIN_HAND);
        h.assertTrue(!spectator.isUsingItem(), "Spectator injected");h.succeed();
    }
    @GameTest(template = "empty")
    public static void identificationPersistsAndSnapshotMatchesTooltipSelection(GameTestHelper h) {
        var sample=vial("minecraft:polar_bear");BloodSampleData.identify(sample);
        var loaded=ItemStack.parseOptional(h.getLevel().registryAccess(),(CompoundTag)sample.save(h.getLevel().registryAccess()));
        h.assertTrue(ItemStack.isSameItemSameComponents(sample,loaded), "Specimen data did not persist");
        var snapshot=BloodInjectionData.snapshot(false);
        var buf=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
        var codec=com.vincenthuto.hemomancy.common.network.BloodInjectionSyncPacket.STREAM_CODEC;
        codec.encode(buf,new com.vincenthuto.hemomancy.common.network.BloodInjectionSyncPacket(snapshot.json()));
        var packet=codec.decode(buf);buf.release();
        BloodInjectionData.receive(packet.definitions());
        var client=BloodInjectionData.snapshot(true);
        var profile=BloodSampleData.profile(sample,false);
        h.assertTrue(snapshot.resolve(profile).equals(client.resolve(profile)), "Client response disagreed with server");
        BloodInjectionData.clearClient();
        h.assertTrue(BloodInjectionData.snapshot(true).responses().isEmpty() && !BloodInjectionData.snapshot(false).responses().isEmpty(), "Disconnect crossed snapshot sides");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void resistancesMobilityAndHealingHaveOnlyTheirNamedBenefits(GameTestHelper h) {
        var player=educatedPlayer(h);
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2,6,2)));
        player.addEffect(new MobEffectInstance(EffectInit.cryoprotection,200));player.setTicksFrozen(200);player.tick();
        h.assertTrue(!player.canFreeze() && player.getTicksFrozen()==0, "Cryoprotection did not stop freezing");
        h.assertTrue(net.minecraft.world.level.block.PowderSnowBlock.canEntityWalkOnPowderSnow(player), "Powder snow traversal missing");
        player.removeAllEffects();player.addEffect(new MobEffectInstance(EffectInit.web_mobility,200));
        double x=player.getX();player.makeStuckInBlock(net.minecraft.world.level.block.Blocks.COBWEB.defaultBlockState(),new net.minecraft.world.phys.Vec3(.25,.05,.25));
        player.move(net.minecraft.world.entity.MoverType.SELF,new net.minecraft.world.phys.Vec3(1,0,0));
        h.assertTrue(player.getX()-x>.7 && player.getX()-x<.9, "Cobweb movement did not use reduced slowdown");
        player.removeAllEffects();double speed=player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        player.addEffect(new MobEffectInstance(EffectInit.poison_resistance,200));
        player.addEffect(new MobEffectInstance(MobEffects.POISON,100));
        h.assertTrue(!player.hasEffect(MobEffects.POISON) && player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)==speed, "Poison resistance added speed or admitted poison");
        player.addEffect(new MobEffectInstance(EffectInit.wither_resistance,200));player.addEffect(new MobEffectInstance(MobEffects.WITHER,100));
        h.assertTrue(!player.hasEffect(MobEffects.WITHER), "Wither resistance admitted wither");
        player.addEffect(new MobEffectInstance(EffectInit.impaired_recovery,200));player.setHealth(10);player.heal(4);
        h.assertTrue(Math.abs(player.getHealth()-12)<.01, "Incoming healing not halved");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void centrifugeStillAcceptsBalancedSamples(GameTestHelper h) {
        var pos=h.absolutePos(new net.minecraft.core.BlockPos(0,2,0));
        h.getLevel().setBlockAndUpdate(pos,BlockInit.vial_centrifuge.get().defaultBlockState());
        var machine=(com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity)h.getLevel().getBlockEntity(pos);
        machine.setItem(2,vial("minecraft:pig"));machine.setItem(6,vial("minecraft:pig"));
        h.assertTrue(machine.attemptStartup(null)==com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeStartupResult.SUCCESS,"Balanced samples failed startup");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void miningTargetsEarthAndStoneInsteadOfOreOrWood(GameTestHelper h) {
        var player=educatedPlayer(h);player.addEffect(new MobEffectInstance(EffectInit.earthen_mining,200));
        var blocks=new net.minecraft.world.level.block.Block[]{net.minecraft.world.level.block.Blocks.STONE,net.minecraft.world.level.block.Blocks.DIRT,
                net.minecraft.world.level.block.Blocks.IRON_ORE,net.minecraft.world.level.block.Blocks.OAK_LOG};
        for(int i=0;i<blocks.length;i++) {
            var event=new net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed(player,blocks[i].defaultBlockState(),4,net.minecraft.core.BlockPos.ZERO);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
            h.assertTrue(Math.abs(event.getNewSpeed()-(i<2?5:4))<.01,"Mining response affected wrong material: "+blocks[i]);
        }
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void projectileAndExplosionResistanceDoNotReduceOrdinaryDamage(GameTestHelper h) {
        var player=educatedPlayer(h);player.addEffect(new MobEffectInstance(EffectInit.ender_resistance,200));
        player.addEffect(new MobEffectInstance(EffectInit.explosion_resistance,200));
        var arrow=net.minecraft.world.entity.EntityType.ARROW.create(h.getLevel());
        var sources=new net.minecraft.world.damagesource.DamageSource[]{player.damageSources().arrow(arrow,null),player.damageSources().explosion(null,null),player.damageSources().generic()};
        for(int i=0;i<sources.length;i++) {
            player.setHealth(20);player.invulnerableTime=0;player.hurt(sources[i],8);
            h.assertTrue(Math.abs(player.getHealth()-(i<2?14:12))<.01,"Resistance changed wrong damage category: "+sources[i]);
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void twoTendencyFixtureKeepsBothResponsesAndDrawback(GameTestHelper h) {
        var profile=new BloodSampleData.Profile(net.minecraft.resources.ResourceLocation.parse("minecraft:pig"),java.util.List.of(
            com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.CONGEATIO,
            com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.ANIMUS),java.util.List.of(),true);
        var result=BloodInjectionData.snapshot(false).resolve(profile);
        h.assertTrue(result.benefits().size()==2 && result.benefits().getFirst().effect().toString().equals("minecraft:regeneration"),"Two-tendency order or response lost");
        h.assertTrue(result.drawbacks().size()==1 && result.drawbacks().getFirst().effect().toString().equals("minecraft:slowness"),"Congeatio drawback lost");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void packPriorityAndReloadRefreshTheResolvedAnswer(GameTestHelper h) throws Exception {
        var root=java.nio.file.Files.createTempDirectory(java.nio.file.Path.of("."),"blood-response-packs-");
        var packs=new java.util.ArrayList<net.minecraft.server.packs.PackResources>();
        for(int i=0;i<2;i++) {
            var path=root.resolve("pack"+i);var file=path.resolve("data/example/blood_injection/animus.json");
            java.nio.file.Files.createDirectories(file.getParent());
            java.nio.file.Files.writeString(file,"{\"tendency\":\"ANIMUS\",\"benefits\":[{\"effect\":\"minecraft:regeneration\",\"duration\":"+(i==0?200:400)+"}]}");
            packs.add(new net.minecraft.server.packs.PathPackResources(new net.minecraft.server.packs.PackLocationInfo("fixture"+i,
                net.minecraft.network.chat.Component.literal("fixture"),net.minecraft.server.packs.repository.PackSource.DEFAULT,java.util.Optional.empty()),path));
        }
        try(var manager=new net.minecraft.server.packs.resources.MultiPackResourceManager(net.minecraft.server.packs.PackType.SERVER_DATA,packs)) {
            var method=BloodInjectionData.class.getDeclaredMethod("prepare",net.minecraft.server.packs.resources.ResourceManager.class,net.minecraft.util.profiling.ProfilerFiller.class);
            method.setAccessible(true);
            var replacement=(BloodInjectionData.Snapshot)method.invoke(new BloodInjectionData(),manager,net.minecraft.util.profiling.InactiveProfiler.INSTANCE);
            var sample=vial("minecraft:pig");BloodSampleData.identify(sample);
            var profile=BloodSampleData.profile(sample,false);
            h.assertTrue(replacement.responses().size()==1 && replacement.resolve(profile).benefits().getFirst().duration()==400,"Pack priority failed");
            BloodInjectionData.receive(replacement.json());
            h.assertTrue(BloodSampleData.identified(sample) && BloodInjectionData.snapshot(true).resolve(profile).benefits().getFirst().duration()==400,"Reload failed to refresh identified answer");
            BloodInjectionData.clearClient();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void syringePreservesEmptyVialDataAndSkipsUnreadableSamples(GameTestHelper h) {
        var player=educatedPlayer(h);
        var rack=new ItemStack(ItemInit.vial_rack.get());var vials=VialRackItem.getVials(rack);
        vials.set(0,vial("missing:creature"));
        var empty=new ItemStack(ItemInit.bloody_vial.get());
        empty.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Field vessel"));
        var extra=new CompoundTag();extra.putString("addon_marker","preserve");empty.set(DataComponents.CUSTOM_DATA,CustomData.of(extra));
        vials.set(1,empty);VialRackItem.setVials(rack,vials);player.getInventory().setItem(1,rack);
        var syringe=new ItemStack(ItemInit.living_syringe.get());player.setItemInHand(InteractionHand.MAIN_HAND,syringe);
        syringe.getItem().interactLivingEntity(syringe,player,h.spawn(net.minecraft.world.entity.EntityType.PIG,0,3,0),InteractionHand.MAIN_HAND);
        var stored=ItemStack.parseOptional(h.getLevel().registryAccess(),syringe.get(DataComponents.CUSTOM_DATA).copyTag().getCompound("loaded_rack"));
        var loaded=VialRackItem.getVials(stored);
        h.assertTrue(BloodSampleData.rawSource(loaded.get(0)).equals("missing:creature"),"Syringe overwrote missing source");
        h.assertTrue(BloodSampleData.rawSource(loaded.get(1)).equals("minecraft:pig"),"Syringe did not fill next empty vial");
        h.assertTrue(loaded.get(1).getHoverName().getString().equals("Field vessel") && loaded.get(1).get(DataComponents.CUSTOM_DATA).copyTag().getString("addon_marker").equals("preserve"),"Syringe replaced opaque vial data");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void machineInteractionConsumesActionBeforeOffhandInjection(GameTestHelper h) {
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"injection-test"),false);
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        var pos=h.absolutePos(new net.minecraft.core.BlockPos(0,2,0));
        h.getLevel().setBlockAndUpdate(pos,BlockInit.vial_centrifuge.get().defaultBlockState());
        player.setPos(net.minecraft.world.phys.Vec3.atCenterOf(pos).add(0,1,-1));
        player.setItemInHand(InteractionHand.OFF_HAND,vial("minecraft:pig"));
        var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),net.minecraft.core.Direction.UP,pos,false);
        com.vincenthuto.hemomancy.common.event.MachineAccessEvents.awardMachineCrafted(player,BlockInit.vial_centrifuge.get());
        var result=player.gameMode.useItemOn(player,h.getLevel(),ItemStack.EMPTY,InteractionHand.MAIN_HAND,hit);
        h.assertTrue(result.consumesAction() && !player.isUsingItem() && BloodSampleData.isFilled(player.getOffhandItem()),"Machine interaction result="+result+", using="+player.isUsingItem()+", offhandFilled="+BloodSampleData.isFilled(player.getOffhandItem()));
        player.closeContainer();player.discard();h.succeed();
    }

}

