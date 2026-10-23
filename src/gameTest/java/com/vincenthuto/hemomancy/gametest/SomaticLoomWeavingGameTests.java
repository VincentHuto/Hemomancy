package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.weave.FrayedThreadEntity;
import com.vincenthuto.hemomancy.common.entity.weave.ThreadAttacks;
import com.vincenthuto.hemomancy.common.entity.weave.ThreadHazardEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramRecollections;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramTracing;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@GameTestHolder("somatic_loom_validation")
@PrefixGameTestTemplate(false)
public final class SomaticLoomWeavingGameTests {
    private static final String AXONAL = "memory_weaving/memory_axonal_transduction";
    private static final String LIVING_CIRCUIT = "memory_weaving/memory_living_circuit";
    private static final int PLENTY = 150;

    /** A stocked loom with its weave chosen, and a weaver beside it. */
    private record Setup(BlockPos pos, SomaticLoomBlockEntity loom, FakePlayer weaver, String recipe) {}

    private static UUID weaverId(String name) {
        return UUID.nameUUIDFromBytes(("loom-test:" + name).getBytes(StandardCharsets.UTF_8));
    }

    /** Axonal Transduction is a PERFECTUS memory (4 Ductilis, 600 mL), so its fixture loom is Cortical (stage 2). */
    private static Setup axonal(GameTestHelper helper, int ductilisSpans) {
        return setup(helper, AXONAL, ductilisSpans, 2, "axonal_weaver");
    }

    private static Setup setup(GameTestHelper helper, String recipe, int spans, int stage, String weaverName) {
        var level = helper.getLevel();
        MemoryWeavingRecipe weaving = (MemoryWeavingRecipe) level.getRecipeManager().byKey(Hemomancy.rloc(recipe)).orElseThrow().value();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var block = (SomaticLoomBlock) BlockInit.somatic_loom.get();
        // A fresh loom each time: re-placing the same state would keep the last fixture's contents.
        level.removeBlock(pos, false);
        level.setBlock(pos, block.defaultBlockState().setValue(SomaticLoomBlock.STAGE, stage), 3);
        var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
        te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        for (var catalyst : weaving.getCatalysts()) te.addItem(null, catalyst.getItems()[0].copyWithCount(1), null);
        CompoundTag tag = te.saveWithoutMetadata(level.registryAccess());
        CompoundTag stock = new CompoundTag();
        for (EnumBloodTendency tend : EnumBloodTendency.values())
            if (weaving.getEnzymeRequirement(tend) > 0) stock.putInt(tend.name(), spans);
        tag.put("storedEnzymes", stock);
        te.loadWithComponents(tag, level.registryAccess());
        helper.assertTrue(te.selectRecipe(null, Hemomancy.rloc(recipe)), recipe + " should be selectable at stage " + stage);
        var weaver = new FakePlayer(level, new GameProfile(weaverId(weaverName), weaverName));
        weaver.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() - 2.5);
        var volume = HemoCapabilityAccess.getBloodVolume(weaver);
        volume.setActive(true);
        volume.setBloodVolume(volume.getMaxBloodVolume());
        return new Setup(pos, te, weaver, recipe);
    }

    private static void pay(GameTestHelper helper, Setup s) {
        helper.assertTrue(s.loom.tryChargeRitualBlood(s.weaver, 10_000, false) && s.loom.isWeaving(),
                "Paying the full blood cost should unfurl the engram");
    }

    private static void tick(GameTestHelper helper, Setup s, int ticks) {
        for (int i = 0; i < ticks; i++)
            SomaticLoomBlockEntity.serverTick(helper.getLevel(), s.pos, helper.getLevel().getBlockState(s.pos), s.loom);
    }

    private static int[] stock(Setup s) {
        int[] out = new int[8];
        for (EnumBloodTendency tend : EnumBloodTendency.values()) out[tend.ordinal()] = s.loom.getStoredEnzyme(tend);
        return out;
    }

    private static int trueFragments(EngramBoard board) {
        return (int) board.fragments().stream().filter(f -> !f.intrusion()).count();
    }

    /** The first weaving recipe a base loom weaves whose memory has `rank`. */
    private static String recipeOfRank(GameTestHelper helper, EnumManipulationRank rank) {
        var level = helper.getLevel();
        for (RecipeHolder<MemoryWeavingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeInit.memory_weaving_type.get())
                .stream().sorted(java.util.Comparator.comparing(h -> h.id().toString())).toList()) {
            var result = holder.value().getResultItem(level.registryAccess()).getItem();
            if (result instanceof BloodMemoryItem memory && memory.getManip() != null && memory.getManip().getRank() == rank
                    && holder.value().getCatalysts().stream().allMatch(c -> c.getItems().length > 0))
                return holder.id().getPath();
        }
        throw new IllegalStateException("No " + rank + " weave");
    }

    //  Unfurling and paying

    @GameTest(template = "empty")
    public static void payingUnfurlsTheWeaversEngramAndSpendsNoThreadYet(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        EngramBoard preview = s.loom.engramPreview(s.weaver);
        pay(helper, s);
        EngramBoard board = s.loom.getEngram();
        helper.assertTrue(board != null && trueFragments(board) == 4, "Axonal's four Ductilis units become four true fragments");
        helper.assertTrue(java.util.Arrays.equals(preview.reference(), board.reference()), "the paid engram is the one previewed");
        helper.assertTrue(s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS) == 60 && s.loom.getSpun(EnumBloodTendency.DUCTILIS) == 0,
                "nothing is spent until the engram completes");
        helper.assertTrue(!LoomWeaveTestSupport.intrusions(board).isEmpty(), "a Perfectus engram recalls false fragments");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aStockShortOfTheEngramsReferenceRefusesTheBlood(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        int reference = s.loom.engramPreview(s.weaver).reference()[EnumBloodTendency.DUCTILIS.ordinal()];
        Setup tight = axonal(helper, reference - 1);
        double before = HemoCapabilityAccess.getBloodVolume(tight.weaver).getBloodVolume();
        helper.assertTrue(!tight.loom.tryChargeRitualBlood(tight.weaver, 10_000, false) && !tight.loom.isWeaving(),
                "one span short of the reference, the loom takes no blood");
        helper.assertTrue(HemoCapabilityAccess.getBloodVolume(tight.weaver).getBloodVolume() == before, "no blood was taken");
        Setup exact = axonal(helper, reference);
        pay(helper, exact);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void enzymesBecomeThreeSpansAndTheVialHoldsSixtyFour(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, BlockInit.somatic_loom.get().defaultBlockState(), 3);
        var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
        te.addItem(null, new ItemStack(ItemInit.vivacious_enzyme.get()), null);
        helper.assertTrue(te.getStoredEnzyme(EnumBloodTendency.ANIMUS) == 3, "one enzyme spins three spans");
        var crouching = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        crouching.setShiftKeyDown(true);
        te.addItem(crouching, new ItemStack(ItemInit.vivacious_enzyme.get(), 64), null);
        helper.assertTrue(te.getStoredEnzyme(EnumBloodTendency.ANIMUS) == SomaticLoomBlockEntity.ENZYME_CAPACITY,
                "a full vial holds sixty-four enzymes' thread");
        helper.succeed();
    }

    //  Threading

    @GameTest(template = "empty")
    public static void threadingTheEngramCrystallizesTheMemoryAndSpendsItsLanes(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        pay(helper, s);
        int reference = s.loom.getEngram().reference()[EnumBloodTendency.DUCTILIS.ordinal()];
        helper.assertTrue(LoomWeaveTestSupport.weaveAll(helper.getLevel(), s.pos, s.loom, s.weaver), "The weave should finish");
        helper.assertTrue(s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS) == 60 - reference,
                "exactly the threaded lanes' spans are spent: " + s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS));
        boolean delivered = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(s.pos).inflate(3)).stream()
                .anyMatch(item -> item.getItem().is(ItemInit.memory_axonal_transduction.get()));
        helper.assertTrue(delivered, "The finished memory should crystallize");
        helper.assertTrue(EngramRecollections.get(helper.getLevel()).hasWoven(s.weaver.getUUID(), Hemomancy.rloc(AXONAL).toString()),
                "the weaver's bloodline now remembers this memory");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cuttingALaneBackRefundsItsSpans(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        pay(helper, s);
        EngramBoard board = s.loom.getEngram();
        int fragment = 0;
        int[] path = board.solution(fragment);
        s.loom.engramStep(s.weaver, path[0], false, true);
        s.loom.engramStep(s.weaver, path[1], false, false);
        s.loom.engramStep(s.weaver, path[2], false, false);
        helper.assertTrue(s.loom.getSpun(EnumBloodTendency.DUCTILIS) == 2, "two steps hold two spans");
        s.loom.engramStep(s.weaver, path[1], true, true);
        helper.assertTrue(s.loom.getSpun(EnumBloodTendency.DUCTILIS) == 1, "cutting back returns a span");
        s.loom.engramStep(s.weaver, path[0], true, true);
        helper.assertTrue(s.loom.getSpun(EnumBloodTendency.DUCTILIS) == 0 && s.loom.getEngram().lane(fragment).length == 1,
                "a sneaking press on the fragment clears its lane");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void onlyTheWeaverThreads(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        pay(helper, s);
        var stranger = new FakePlayer(helper.getLevel(), new GameProfile(weaverId("stranger"), "stranger"));
        int[] path = s.loom.getEngram().solution(0);
        s.loom.engramStep(stranger, path[0], false, true);
        helper.assertTrue(!s.loom.engramStep(stranger, path[1], false, false).moved(), "a helper cannot thread the weaver's engram");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aLivingStaffPointedAtTheEngramTakesAndThreadsALane(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        pay(helper, s);
        tick(helper, s, 1);   // the loom registers its unfurled engram where staves look for it
        Player weaver = s.weaver;
        weaver.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.living_staff.get()));
        EngramBoard board = s.loom.getEngram();
        int[] path = board.solution(0);
        aimAt(weaver, s.loom.nodeWorld(path[0]));
        weaver.getItemInHand(InteractionHand.MAIN_HAND).use(helper.getLevel(), weaver, InteractionHand.MAIN_HAND);
        helper.assertTrue(EngramTracing.isTracing(weaver), "pressing the staff on a fragment starts a trace");
        helper.assertTrue(s.loom.engramActive() == 0, "the pressed fragment's lane is taken");
        aimAt(weaver, s.loom.nodeWorld(path[1]));
        EngramTracing.tick(weaver);
        helper.assertTrue(s.loom.getEngram().lane(0).length == 2, "holding the staff over the next node threads the lane there");
        EngramTracing.end(weaver);
        helper.assertTrue(!EngramTracing.isTracing(weaver) && s.loom.engramActive() < 0, "letting go drops the lane");
        helper.succeed();
    }

    /** Stands `player` two blocks back and above a point on the engram and looks at it. */
    private static void aimAt(Player player, Vec3 target) {
        Vec3 from = target.add(0, 2.2, -2.0);
        player.moveTo(from.x, from.y - player.getEyeHeight(), from.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target);
    }

    @GameTest(template = "empty")
    public static void choosingAWeaveTellsThePayerWhatTheirOwnEngramCosts(GameTestHelper helper) {
        var feedback = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        var level = helper.getLevel();
        var profile = new GameProfile(weaverId("feedback_weaver"), "feedback_weaver");
        var player = new FakePlayer(level, profile) {
            @Override
            public void displayClientMessage(net.minecraft.network.chat.Component message, boolean overlay) {
                feedback.add(message);
            }
        };
        Setup s = axonal(helper, 3);
        int reference = s.loom.engramPreview(player).reference()[EnumBloodTendency.DUCTILIS.ordinal()];
        s.loom.provideTendencyFeedback(player);
        String said = String.join(" | ", feedback.stream().map(net.minecraft.network.chat.Component::getString).toList());
        helper.assertTrue(said.contains("your engram needs " + reference + " spans"),
                "the selection names this weaver's own engram cost: " + said);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void enzymesTopUpTheVialsMidWeaveWithoutCancellingIt(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        pay(helper, s);
        double charged = s.loom.getRitualBloodCharged();
        // The real block-use path checks machine access (a loom you have crafted); a creative weaver passes it.
        s.weaver.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        var hit = new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(s.pos), net.minecraft.core.Direction.UP, s.pos, false);
        // One enzyme, the ordinary way.
        ItemStack one = new ItemStack(ItemInit.neurotic_enzyme.get());
        s.weaver.setItemInHand(InteractionHand.MAIN_HAND, one);
        var result = s.weaver.gameMode.useItemOn(s.weaver, helper.getLevel(), one, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(s.loom.isWeaving() && s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS) == 63,
                "an enzyme added mid-weave tops up its vial: " + s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS) + " (" + result
                        + ", mode " + s.weaver.gameMode.getGameModeForPlayer() + ", block " + helper.getLevel().getBlockState(s.pos).getBlock() + ")");
        // A stack, with the sneaking bulk gesture: it must never fall through to the loom's cancel.
        ItemStack stack = new ItemStack(ItemInit.neurotic_enzyme.get(), 4);
        s.weaver.setItemInHand(InteractionHand.MAIN_HAND, stack);
        s.weaver.setShiftKeyDown(true);
        s.weaver.gameMode.useItemOn(s.weaver, helper.getLevel(), stack, InteractionHand.MAIN_HAND, hit);
        s.weaver.setShiftKeyDown(false);
        helper.assertTrue(s.loom.isWeaving() && s.loom.getRitualBloodCharged() == charged,
                "a sneaking insert keeps the paid weave and its blood");
        helper.assertTrue(s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS) == 75, "the stack tops the vial up in bulk");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void switchingAwayFromTheStaffEndsItsTrace(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        pay(helper, s);
        tick(helper, s, 1);
        Player weaver = s.weaver;
        weaver.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.living_staff.get()));
        aimAt(weaver, s.loom.nodeWorld(s.loom.getEngram().solution(0)[0]));
        weaver.getItemInHand(InteractionHand.MAIN_HAND).use(helper.getLevel(), weaver, InteractionHand.MAIN_HAND);
        helper.assertTrue(EngramTracing.isTracing(weaver), "the staff traces");
        weaver.stopUsingItem();
        helper.assertTrue(!EngramTracing.isTracing(weaver) && s.loom.engramActive() < 0,
                "a staff that stops being used (a slot change, a death) lets go of its lane");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void anIntrusionKeepsItsWindupAcrossASave(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        FrayedThreadEntity bite = mortalIntrusion(helper, weaver, EnumManipulationRank.PERFECTUS);
        bite.tick();
        int windup = bite.windup();
        CompoundTag saved = bite.saveWithoutId(new CompoundTag());
        var reloaded = new FrayedThreadEntity(com.vincenthuto.hemomancy.common.init.EntityInit.frayed_thread.get(), helper.getLevel());
        reloaded.load(saved);
        helper.assertTrue(windup > 0 && reloaded.windup() == windup, "a reloaded bite keeps its tell: " + reloaded.windup());
        for (int i = 0; i < windup + 2; i++) bite.tick();
        CompoundTag spent = bite.saveWithoutId(new CompoundTag());
        var afterwards = new FrayedThreadEntity(com.vincenthuto.hemomancy.common.init.EntityInit.frayed_thread.get(), helper.getLevel());
        afterwards.load(spent);
        helper.assertTrue(bite.unravelling() > 0 && afterwards.unravelling() > 0, "a spent bite reloads still unravelling, never striking again");
        bite.discard();
        helper.succeed();
    }

    //  Pausing and saving

    @GameTest(template = "empty")
    public static void leavingPausesTheEngramAndKeepsItsLanes(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        pay(helper, s);
        int[] path = s.loom.getEngram().solution(0);
        s.loom.engramStep(s.weaver, path[0], false, true);
        s.loom.engramStep(s.weaver, path[1], false, false);
        s.weaver.moveTo(s.pos.getX() + 40, s.pos.getY(), s.pos.getZ());
        tick(helper, s, 3);
        helper.assertTrue(s.loom.isPaused() && s.loom.getEngram().lane(0).length == 2, "the engram freezes with its lanes");
        helper.assertTrue(!s.loom.engramStep(s.weaver, path[2], false, true).moved(), "nothing threads while paused");
        s.weaver.moveTo(s.pos.getX() + 0.5, s.pos.getY(), s.pos.getZ() - 2.5);
        tick(helper, s, 2);
        s.loom.engramStep(s.weaver, path[1], false, true);
        helper.assertTrue(!s.loom.isPaused() && s.loom.engramStep(s.weaver, path[2], false, false).moved(), "returning resumes");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aMidWeaveSaveRestoresTheEngram(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        pay(helper, s);
        int[] path = s.loom.getEngram().solution(0);
        s.loom.engramStep(s.weaver, path[0], false, true);
        s.loom.engramStep(s.weaver, path[1], false, false);
        CompoundTag saved = s.loom.getEngram().save();
        var registries = helper.getLevel().registryAccess();
        s.loom.loadWithComponents(s.loom.saveWithoutMetadata(registries), registries);
        helper.assertTrue(s.loom.isWeaving() && s.loom.getEngram().save().equals(saved)
                && s.loom.getWeaver().equals(s.weaver.getUUID()), "the engram, its lanes and the weaver survive a reload");
        helper.assertTrue(s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS) == 60, "spans are not rescaled on reload");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void witnessedWeaveSavesRecallAFreshEngram(GameTestHelper helper) {
        Setup s = axonal(helper, 0);
        CompoundTag tag = s.loom.saveWithoutMetadata(helper.getLevel().registryAccess());
        tag.putInt("weaveSaveVersion", 1);
        CompoundTag stock = new CompoundTag();
        stock.putInt(EnumBloodTendency.DUCTILIS.name(), 2);
        tag.put("storedEnzymes", stock);
        ListTag threads = new ListTag();
        for (int i = 0; i < 4; i++) {
            CompoundTag thread = new CompoundTag();
            thread.putString("tendency", EnumBloodTendency.DUCTILIS.name());
            thread.putString("state", i == 0 ? "WOVEN" : "HELD");
            threads.add(thread);
        }
        tag.put("weaveThreads", threads);
        CompoundTag spun = new CompoundTag();
        spun.putInt(EnumBloodTendency.DUCTILIS.name(), 3);
        tag.put("spun", spun);
        tag.putInt("craftPhase", SomaticLoomBlockEntity.PHASE_WEAVING);
        tag.putDouble("ritualBloodCharged", 600);
        tag.remove("engram");
        tag.remove("weaver");
        s.loom.loadWithComponents(tag, helper.getLevel().registryAccess());
        helper.assertTrue(s.loom.isWeaving() && s.loom.getEngram() == null, "a witnessed weave loads paid, awaiting its engram");
        helper.assertTrue(s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS) == (2 + 3) * EngramWeaveRules.SPANS_PER_ENZYME,
                "stored enzymes and the thread still on the pirns return to the vials as spans");
        adoptAndRecall(helper, s);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void preWitnessedOrbSavesRecallAFreshEngram(GameTestHelper helper) {
        Setup s = axonal(helper, 0);
        CompoundTag tag = s.loom.saveWithoutMetadata(helper.getLevel().registryAccess());
        tag.putInt("weaveSaveVersion", 0);
        tag.put("storedEnzymes", new CompoundTag());
        ListTag orbs = new ListTag();
        for (int i = 0; i < 4; i++) {
            CompoundTag orb = new CompoundTag();
            orb.putString("tendency", EnumBloodTendency.DUCTILIS.name());
            orb.putBoolean("completed", i == 0);
            orbs.add(orb);
        }
        tag.put("ritualOrbs", orbs);
        tag.putInt("craftPhase", SomaticLoomBlockEntity.PHASE_WEAVING);
        tag.putDouble("ritualBloodCharged", 600);
        tag.remove("engram");
        tag.remove("weaver");
        s.loom.loadWithComponents(tag, helper.getLevel().registryAccess());
        helper.assertTrue(s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS) == 3 * EngramWeaveRules.SPANS_PER_ENZYME,
                "unwoven orbs return their enzyme as spans");
        adoptAndRecall(helper, s);
        helper.succeed();
    }

    /** An orphaned weave is taken up by the nearest player, who recalls an engram its vials can always afford. */
    private static void adoptAndRecall(GameTestHelper helper, Setup s) {
        var adopter = new FakePlayer(helper.getLevel(), new GameProfile(weaverId("adopter"), "loom_adopter"));
        adopter.moveTo(s.pos.getX() + 0.5, s.pos.getY(), s.pos.getZ() - 2.5);
        helper.getLevel().addNewPlayer(adopter);
        try {
            tick(helper, s, 3);
            EngramBoard board = s.loom.getEngram();
            helper.assertTrue(adopter.getUUID().equals(s.loom.getWeaver()) && board != null, "the adopter recalls a fresh engram");
            helper.assertTrue(s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS) >= board.reference()[EnumBloodTendency.DUCTILIS.ordinal()],
                    "a migrated weave's vials are topped up to its reference");
            helper.assertTrue(LoomWeaveTestSupport.weaveAll(helper.getLevel(), s.pos, s.loom, adopter), "and it can be finished");
        } finally {
            helper.getLevel().removePlayerImmediately(adopter, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
    }

    @GameTest(template = "empty")
    public static void threadsSavedByTheWitnessedWeaveUnravel(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        var legacy = new FrayedThreadEntity(com.vincenthuto.hemomancy.common.init.EntityInit.frayed_thread.get(), helper.getLevel());
        legacy.moveTo(s.pos.getX() + 2, s.pos.getY() + 1, s.pos.getZ());
        helper.getLevel().addFreshEntity(legacy);
        legacy.tick();
        helper.assertTrue(legacy.isRemoved(), "a fighting thread from an old save unravels as soon as it loads");
        helper.succeed();
    }

    //  Mistakes, by rank

    /** The first intrusion with a legal way home (scars can wall a lie in, harmlessly), or -1. */
    private static int reachableIntrusion(Setup s) {
        EngramBoard board = s.loom.getEngram();
        for (int lie : LoomWeaveTestSupport.intrusions(board))
            if (LoomWeaveTestSupport.homeward(board, lie, stock(s)) != null) return lie;
        return -1;
    }

    /** Threads a reachable intrusion home along any legal path; returns the spans its lane held. */
    private static int threadIntrusion(GameTestHelper helper, Setup s) {
        EngramBoard board = s.loom.getEngram();
        int lie = reachableIntrusion(s);
        helper.assertTrue(lie >= 0, "an intrusion has a legal way home");
        int[] path = LoomWeaveTestSupport.homeward(board, lie, stock(s));
        s.loom.engramStep(s.weaver, board.fragment(lie).node(), false, true);
        for (int k = 1; k < path.length; k++) s.loom.engramStep(s.weaver, path[k], false, false);
        return path.length - 1;
    }

    /** A paid weave of `recipe` whose engram has a reachable intrusion, trying a few weavers' engrams. */
    private static Setup withReachableIntrusion(GameTestHelper helper, String recipe, int stage) {
        for (int attempt = 0; attempt < 12; attempt++) {
            Setup s = setup(helper, recipe, PLENTY, stage, "intrusion_weaver_" + attempt);
            pay(helper, s);
            if (reachableIntrusion(s) >= 0) return s;
            s.loom.cancelRitual(null);
        }
        throw new IllegalStateException("No engram of " + recipe + " with a reachable intrusion in twelve weavers");
    }

    @GameTest(template = "empty")
    public static void aMediocritasIntrusionBurnsItsSpansAndSnaps(GameTestHelper helper) {
        Setup s = withReachableIntrusion(helper, recipeOfRank(helper, EnumManipulationRank.MEDIOCRITAS), 0);
        EngramBoard board = s.loom.getEngram();
        int lie = reachableIntrusion(s);
        EnumBloodTendency tend = EnumBloodTendency.values()[board.fragment(lie).colour()];
        int before = s.loom.getStoredEnzyme(tend);
        int spans = threadIntrusion(helper, s);
        helper.assertTrue(s.loom.getEngram().lane(lie).length == 1, "the false lane tears loose");
        helper.assertTrue(s.loom.getStoredEnzyme(tend) == before - spans, "its spans burn from the surplus");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void summaIntrusionsBuildFungalPressureThatBoilsOver(GameTestHelper helper) {
        Setup s = withReachableIntrusion(helper, recipeOfRank(helper, EnumManipulationRank.SUMMA), 0);
        int scarsBefore = scars(s.loom.getEngram());
        int fragmentsBefore = s.loom.getEngram().fragmentCount();
        threadIntrusion(helper, s);
        helper.assertTrue(s.loom.getEngram().pressure() == 1, "a false memory threaded home builds pressure");
        threadIntrusion(helper, s);
        threadIntrusion(helper, s);
        EngramBoard board = s.loom.getEngram();
        helper.assertTrue(board.pressure() == 0 && (scars(board) > scarsBefore || board.fragmentCount() > fragmentsBefore),
                "at three the pressure boils over into a scar or a new false fragment");
        helper.succeed();
    }

    private static int scars(EngramBoard board) {
        int n = 0;
        for (int node = 0; node < EngramSigilGeometry.VESSEL; node++) if (board.kind(node) == EngramBoard.NodeKind.SCAR) n++;
        return n;
    }

    @GameTest(template = "empty")
    public static void aPerfectusIntrusionBitesItsWeaver(GameTestHelper helper) {
        Setup s = withReachableIntrusion(helper, AXONAL, 2);
        int before = helper.getLevel().getEntitiesOfClass(FrayedThreadEntity.class, new AABB(s.pos).inflate(12)).size();
        threadIntrusion(helper, s);
        var bites = helper.getLevel().getEntitiesOfClass(FrayedThreadEntity.class, new AABB(s.pos).inflate(12));
        helper.assertTrue(bites.size() == before + 1, "a failed Harbinger's memory tears loose");
        helper.assertTrue(bites.getLast().hunts(s.weaver) && bites.getLast().windup() > 0, "it winds up at its weaver, in plain sight");
        bites.forEach(net.minecraft.world.entity.Entity::discard);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theBloodlineRemembersWhichFragmentsAreFalse(GameTestHelper helper) {
        var level = helper.getLevel();
        UUID weaver = weaverId("bloodline_weaver"), elder = weaverId("bloodline_elder");
        Bloodline line = new Bloodline("Engram Test Line", elder, UUID.randomUUID(), new java.util.ArrayList<>(List.of(elder, weaver)));
        var lines = BloodlineSavedData.get(level.getServer().overworld());
        lines.registerBloodline(line);
        try {
            EngramRecollections.get(level).record(elder, Hemomancy.rloc(AXONAL).toString());
            Setup kin = setup(helper, AXONAL, PLENTY, 2, "bloodline_weaver");
            pay(helper, kin);
            EngramBoard board = kin.loom.getEngram();
            helper.assertTrue(LoomWeaveTestSupport.intrusions(board).stream().allMatch(i -> board.fragment(i).premarked()),
                    "a kinsman of one who wove it sees its false fragments as husks");
        } finally {
            lines.disbandBloodline(line.getBloodlineUUID());
        }
        helper.succeed();
    }

    //  The bite's harm, floored at every rank

    /** A Blood Shot weave whose weaver can be hurt (a mock player), with an intrusion of `rank` raised at its loom. */
    private static FrayedThreadEntity mortalIntrusion(GameTestHelper helper, Player weaver, EnumManipulationRank rank) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.removeBlock(pos, false);
        level.setBlock(pos, BlockInit.somatic_loom.get().defaultBlockState(), 3);
        var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
        te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        te.addItem(null, new ItemStack(ItemInit.bleeding_bulb.get()), null);
        te.addItem(null, new ItemStack(ItemInit.vivacious_enzyme.get()), null);
        helper.assertTrue(te.selectRecipe(null, Hemomancy.rloc("memory_weaving/memory_blood_shot")), "Blood Shot should be selectable");
        weaver.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() - 2.5);
        var volume = HemoCapabilityAccess.getBloodVolume(weaver);
        volume.setActive(true);
        volume.setBloodVolume(volume.getMaxBloodVolume());
        helper.assertTrue(te.tryChargeRitualBlood(weaver, 50, false) && te.isWeaving(), "Blood Shot weave should begin");
        // Active blood makes the damage hooks treat the mock as a ServerPlayer; the weave no longer needs it.
        HemoCapabilityAccess.getBloodVolume(weaver).setActive(false);
        return FrayedThreadEntity.spawnIntrusion(level, te, EnumBloodTendency.ANIMUS, te.nodeWorld(0), rank);
    }

    @GameTest(template = "empty")
    public static void tendencyHitsApplyTheirSchoolsHarm(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        FrayedThreadEntity thread = mortalIntrusion(helper, weaver, EnumManipulationRank.PERFECTUS);
        var victim = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        victim.moveTo(thread.getX() + 1, thread.getY(), thread.getZ());
        ThreadAttacks.onBoltHit(EnumBloodTendency.CONGEATIO, thread, victim, 1f);
        helper.assertTrue(victim.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN)
                && victim.getTicksFrozen() > 0, "Congeatio slows and freezes");
        victim.invulnerableTime = 0;
        float before = victim.getHealth();
        ThreadAttacks.onBoltHit(EnumBloodTendency.LUX, thread, victim, 1f);
        helper.assertTrue(victim.getHealth() < before && victim.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING),
                "Lux lances burn and mark");
        ThreadAttacks.onBoltHit(EnumBloodTendency.FLAMMEUS, thread, victim, 1f);
        helper.assertTrue(victim.getRemainingFireTicks() > 0, "Flammeus ignites");
        thread.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyRanksBiteBlacksOutInsteadOfKilling(GameTestHelper helper) {
        for (EnumManipulationRank rank : new EnumManipulationRank[]{EnumManipulationRank.HUMILIS, EnumManipulationRank.PERFECTUS}) {
            var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            FrayedThreadEntity thread = mortalIntrusion(helper, weaver, rank);
            weaver.setHealth(6.0F);
            weaver.hurt(thread.damageSources().mobAttack(thread), 20.0F);
            helper.assertTrue(weaver.isAlive() && weaver.getHealth() >= SomaticWeaveRules.NON_LETHAL_FLOOR - 0.01F,
                    rank + " bite stops at the floor");
            thread.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void otherAttackersAreNotFloored(GameTestHelper helper) {
        var victim = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        victim.setHealth(6.0F);
        victim.getPersistentData().putLong("hemomancy:weave_harm_until", helper.getLevel().getGameTime() + 100);
        victim.getPersistentData().putBoolean("hemomancy:weave_lethal", false);
        var zombie = new net.minecraft.world.entity.monster.Zombie(helper.getLevel());
        victim.hurt(helper.getLevel().damageSources().mobAttack(zombie), 20.0F);
        helper.assertTrue(victim.isDeadOrDying(), "A zombie near a weave still kills");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lavaStillKillsDuringAWeave(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        FrayedThreadEntity thread = mortalIntrusion(helper, weaver, EnumManipulationRank.SUMMA);
        ThreadAttacks.onBoltHit(EnumBloodTendency.FLAMMEUS, thread, weaver, 1f);
        weaver.invulnerableTime = 0;
        weaver.setHealth(6.0F);
        weaver.hurt(helper.getLevel().damageSources().lava(), 20.0F);
        helper.assertTrue(weaver.isDeadOrDying(), "Lava is not the weave's harm and must not be floored");
        thread.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theFireABiteLitIsFloored(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        FrayedThreadEntity thread = mortalIntrusion(helper, weaver, EnumManipulationRank.PERFECTUS);
        ThreadAttacks.onBoltHit(EnumBloodTendency.FLAMMEUS, thread, weaver, 1f);
        weaver.invulnerableTime = 0;
        weaver.setHealth(6.0F);
        weaver.hurt(helper.getLevel().damageSources().onFire(), 20.0F);
        helper.assertTrue(weaver.isAlive() && weaver.getHealth() >= SomaticWeaveRules.NON_LETHAL_FLOOR - 0.01F,
                "Burning lit by a bite stops at the floor, even at Perfectus");
        thread.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aMissedBiteOpensNoHarmWindow(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        FrayedThreadEntity thread = mortalIntrusion(helper, weaver, EnumManipulationRank.PERFECTUS);
        ThreadAttacks.perform(thread, weaver);
        helper.assertTrue(!weaver.getPersistentData().contains("hemomancy:weave_harm_until"),
                "Firing a bolt must not open the harm window before it lands");
        thread.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ferricSpikesAndTheirLaunchAreFloored(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        FrayedThreadEntity thread = mortalIntrusion(helper, weaver, EnumManipulationRank.PERFECTUS);
        ThreadAttacks.spikes(thread, weaver, 1f);
        var spike = new com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike(
                com.vincenthuto.hemomancy.common.init.EntityInit.iron_spike.get(), helper.getLevel());
        var orphanSpikeHit = new net.minecraft.world.damagesource.DamageSource(
                helper.getLevel().damageSources().generic().typeHolder(), spike, null);
        weaver.setHealth(6.0F);
        weaver.hurt(orphanSpikeHit, 20.0F);
        helper.assertTrue(weaver.isAlive() && weaver.getHealth() >= SomaticWeaveRules.NON_LETHAL_FLOOR - 0.01F,
                "A spike whose intrusion is gone is still the weave's harm");
        ThreadAttacks.spikes(thread, weaver, 1f);
        weaver.invulnerableTime = 0;
        weaver.setHealth(6.0F);
        weaver.hurt(helper.getLevel().damageSources().fall(), 20.0F);
        helper.assertTrue(weaver.isAlive(), "Landing from a Ferric launch is floored");
        thread.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void helpersAreNotHuntedByBoltsOrHazards(GameTestHelper helper) {
        var weaver = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        FrayedThreadEntity thread = mortalIntrusion(helper, weaver, EnumManipulationRank.PERFECTUS);
        var level = helper.getLevel();
        var ally = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "loom_ally"));
        ally.moveTo(thread.getX() + 2, thread.getY() - 1, thread.getZ());
        level.addNewPlayer(ally);
        try {
            var bolt = new com.vincenthuto.hemomancy.common.entity.weave.ThreadBoltEntity(level, thread, EnumBloodTendency.ANIMUS, 1f);
            helper.assertTrue(bolt.mayStrike(weaver) && !bolt.mayStrike(ally), "Bolts strike only the weaver");
            ThreadHazardEntity miasma = ThreadHazardEntity.spawn(level, thread, ally.position(), EnumBloodTendency.MORTEM, 2.5f, 80, 1f);
            for (int i = 0; i < 21; i++) miasma.tick();
            helper.assertTrue(!ally.hasEffect(net.minecraft.world.effect.MobEffects.POISON), "Hazards spare helpers");
            ThreadAttacks.markWeaveHarm(ally, thread);
            helper.assertTrue(!ally.getPersistentData().contains("hemomancy:weave_loom"), "Helpers carry no weave floor");
        } finally {
            level.removePlayerImmediately(ally, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
        thread.discard();
        helper.succeed();
    }

    //  Stages, rites, practice and the dome

    @GameTest(template = "empty")
    public static void upgradeRitesWaitForAPaidWeave(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        var level = helper.getLevel();
        helper.assertTrue(s.loom.readyForUpgradeRite(level, net.minecraft.core.Direction.NORTH),
                "A chosen but unpaid weave does not hold the loom from a rite");
        pay(helper, s);
        helper.assertTrue(!s.loom.readyForUpgradeRite(level, net.minecraft.core.Direction.NORTH),
                "A paid weave must finish or be cancelled before the loom is upgraded");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aRiteHeldLoomRefusesEdits(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        s.loom.setRiteLocked(true);
        helper.assertTrue(!s.loom.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null)
                && !s.loom.selectRecipe(null, Hemomancy.rloc(AXONAL))
                && !s.loom.tryChargeRitualBlood(s.weaver, 600, false)
                && !s.loom.removeItem(s.weaver, false) && !s.loom.isWeaving(),
                "A loom held by a rite takes no inputs, choices or blood");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cancellingAWeaveLosesTheBloodButNotTheThread(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        pay(helper, s);
        int[] path = s.loom.getEngram().solution(0);
        s.loom.engramStep(s.weaver, path[0], false, true);
        s.loom.engramStep(s.weaver, path[1], false, false);
        s.loom.cancelRitual(s.weaver);
        helper.assertTrue(!s.loom.isWeaving() && s.loom.getEngram() == null && s.loom.getRitualBloodCharged() == 0,
                "the engram withdraws and the paid blood is gone");
        helper.assertTrue(s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS) == 60, "no thread is lost");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void completingAWeaveRecordsLoomPractice(GameTestHelper helper) {
        Setup s = setup(helper, LIVING_CIRCUIT, 60, 1, "circuit_weaver");
        var progress = HemoCapabilityAccess.stationUpgrades(s.weaver);
        pay(helper, s);
        helper.assertTrue(LoomWeaveTestSupport.weaveAll(helper.getLevel(), s.pos, s.loom, s.weaver), "The weave completes");
        helper.assertTrue(progress.hasUsed(com.vincenthuto.hemomancy.common.station.UpgradeStation.LOOM,
                com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog.LOOM_WEAVE)
                && progress.hasUsed(com.vincenthuto.hemomancy.common.station.UpgradeStation.LOOM,
                com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog.LOOM_WEAVE_GANGLIONIC),
                "A Magister weave on a Ganglionic loom proves both loom practices");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theFirstLessonsBloodShotTakesExactlyOneEnzyme(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, BlockInit.somatic_loom.get().defaultBlockState(), 3);
        var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
        te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        te.addItem(null, new ItemStack(ItemInit.bleeding_bulb.get()), null);
        te.addItem(null, new ItemStack(ItemInit.vivacious_enzyme.get()), null);
        helper.assertTrue(te.selectRecipe(null, Hemomancy.rloc("memory_weaving/memory_blood_shot")), "Blood Shot selectable");
        // Any weaver at all: a Humilis engram's lanes are always the shortest, three spans per fragment.
        for (int i = 0; i < 6; i++) {
            var fake = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "first_weaver_" + i));
            helper.assertTrue(te.engramPreview(fake).reference()[EnumBloodTendency.ANIMUS.ordinal()] == 3,
                    "Blood Shot's engram costs one enzyme for every weaver");
        }
        var fake = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "base_weaver"));
        var volume = HemoCapabilityAccess.getBloodVolume(fake);
        volume.setActive(true);
        volume.setBloodVolume(volume.getMaxBloodVolume());
        fake.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() - 2.5);
        helper.assertTrue(te.tryChargeRitualBlood(fake, 50, false), "Blood Shot weave begins");
        helper.assertTrue(LoomWeaveTestSupport.weaveAll(level, pos, te, fake), "The weave completes");
        var progress = HemoCapabilityAccess.stationUpgrades(fake);
        helper.assertTrue(progress.hasUsed(com.vincenthuto.hemomancy.common.station.UpgradeStation.LOOM,
                com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog.LOOM_WEAVE)
                && !progress.hasUsed(com.vincenthuto.hemomancy.common.station.UpgradeStation.LOOM,
                com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog.LOOM_WEAVE_GANGLIONIC),
                "A Humilis weave proves only the first loom practice");
        helper.assertTrue(te.getStoredEnzyme(EnumBloodTendency.ANIMUS) == 0, "its one enzyme is spent");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void refinedMemoriesNeedAnUpgradedLoom(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var block = (SomaticLoomBlock) BlockInit.somatic_loom.get();
        int[][] cases = {{0, 0, 0}, {1, 1, 0}, {2, 1, 1}};   // stage, Magister allowed, Perfectus allowed
        for (int[] c : cases) {
            level.setBlock(pos, block.defaultBlockState().setValue(SomaticLoomBlock.STAGE, c[0]), 3);
            var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
            te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
            te.addItem(null, new ItemStack(net.minecraft.world.item.Items.REDSTONE_LAMP), null);
            helper.assertTrue(te.selectRecipe(null, Hemomancy.rloc(LIVING_CIRCUIT)) == (c[1] == 1),
                    "Stage " + c[0] + " Magister selection");
            level.removeBlock(pos, false);
            level.setBlock(pos, block.defaultBlockState().setValue(SomaticLoomBlock.STAGE, c[0]), 3);
            te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
            te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
            te.addItem(null, new ItemStack(ItemInit.naeglerophaeon_ganglion.get()), null);
            helper.assertTrue(te.selectRecipe(null, Hemomancy.rloc(AXONAL)) == (c[2] == 1),
                    "Stage " + c[0] + " Perfectus selection");
            level.removeBlock(pos, false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aLoomBroughtBackToBaseRefusesItsRefinedWeave(GameTestHelper helper) {
        Setup s = setup(helper, LIVING_CIRCUIT, 60, 1, "circuit_weaver");
        var level = helper.getLevel();
        level.setBlock(s.pos, level.getBlockState(s.pos).setValue(SomaticLoomBlock.STAGE, 0), 3);
        s.loom.refreshRecipe();
        helper.assertTrue(!s.loom.tryChargeRitualBlood(s.weaver, 10_000, false) && !s.loom.isWeaving()
                && s.loom.getCurRecipe() == null, "A base loom will not take blood for a Magister weave");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theEngramGrowsWithTheLoom(GameTestHelper helper) {
        Setup s = axonal(helper, 60);
        pay(helper, s);
        double tip = s.loom.nodeWorld(0).subtract(s.loom.sigilCentre()).horizontalDistance();
        helper.assertTrue(Math.abs(tip - EngramWeaveRules.tipRadius(2)) < 1e-6, "a Cortical engram spreads to its full radius");
        helper.assertTrue(s.loom.sigilCentre().y == s.pos.getY(), "the engram lies on the loom's own floor");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aCorticalWeaveCarriesTheWeaversChamberTheme(GameTestHelper helper) {
        var chambers = com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.get(helper.getLevel().getServer());
        Setup s = axonal(helper, 60);
        pay(helper, s);
        helper.assertTrue(com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.THEME_WILL_DEFAULT.equals(s.loom.getWeaverTheme()),
                "A weaver without a Chamber weaves under the default sky");
        s.loom.cancelRitual(null);
        chambers.setSkyThemeOverride(s.weaver, com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.THEME_QLIPHOTH_COMMUNION);
        helper.assertTrue(s.loom.selectRecipe(null, Hemomancy.rloc(AXONAL)), "Axonal reselected");
        pay(helper, s);
        helper.assertTrue(com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.THEME_QLIPHOTH_COMMUNION.equals(s.loom.getWeaverTheme()),
                "The dome takes the weaver's own Chamber sky");
        var registries = helper.getLevel().registryAccess();
        s.loom.loadWithComponents(s.loom.saveWithoutMetadata(registries), registries);
        helper.assertTrue(com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.THEME_QLIPHOTH_COMMUNION.equals(s.loom.getWeaverTheme()),
                "The theme survives a reload");
        chambers.clearSkyThemeOverride(s.weaver);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cyclingStepsPastMemoriesTheLoomCannotWeave(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlock(pos, BlockInit.somatic_loom.get().defaultBlockState(), 3);
        var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
        te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        te.addItem(null, new ItemStack(net.minecraft.world.item.Items.BLUE_ICE), null);
        var cycler = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "loom_cycler"));
        java.util.Set<String> reached = new java.util.HashSet<>();
        for (int click = 0; click < 8; click++) {
            te.chooseNextRecipe(cycler);
            if (te.getCurRecipe() != null)
                reached.add(BuiltInRegistries.ITEM.getKey(te.getCurRecipe().getResultItem(level.registryAccess()).getItem()).getPath());
        }
        helper.assertTrue(reached.containsAll(List.of("memory_cryogenic_pulse", "memory_glacial_grasp", "memory_glacial_rampart"))
                        && !reached.contains("memory_absolute_stillness"),
                "Cycling reaches every memory the loom can weave and never readies a refused one: " + reached);
        helper.succeed();
    }
}
