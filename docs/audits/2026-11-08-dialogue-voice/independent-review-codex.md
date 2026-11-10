# Hemomancy dialogue pass 4 independent review

## 1. Verdict

The pass is better in places, especially where it gives someone a particular thing to notice or do. I would keep the Scout's empty hand after she gives up her journal, the Alchemist looking at the player's hands, the Court colony speaking in the singular when it lets go of the line, and the Hermit's plain goodbye. I would hold the batch. Fix the applied Hermit's opening and rank sequence first, then correct the Alchemist's promotion cue, preserve tested teaching terms, and read the two Court packs together by encounter. The broad shift from oracle speeches to amiable asides makes some of the cast easier to read and others easier to confuse. Let the infection, the cost of the work, and the speaker's choices carry more of the voice.

## 2. Blind notes

- Hermit: He keeps telling me what the scene means after the scene has already shown it. The empty chest and the light coming out of it are enough. The end of the vigil gets three or four separate speeches. The clearest thing he says is that the charm can still come off before initiation. I want more of that man's attention to what I might actually do next.
- Guardian and Scout: The Guardian turns even an apple into a series of labels. I believe the part about having eaten one when the medicine ran out. The Scout sounds more like a person when she talks about her sister or what she watched the creatures do. "Not a threat. Not prey. Nothing." works for me here. She has a reason to be stuck on it.
- Mnemonist: The archive is an actual way of thinking for this speaker, especially when they have to file their own voice. Then everything starts sounding like a line engraved over an archive door. The heron and the month of waiting tell me more than the shelf maxim. The vessel instructions need the same care as the memory story because I have to perform them.
- Vicar: I can hear a person trained to qualify what he knows. The long sentences work when the qualification matters, as with the two roads beyond Archon. They bog down when a travel direction has to pass through a lesson about scars or duty. There is a nasty institutional fact in the missing chapel. The spade is where the author starts talking over it.
- Fungal Entity: The hemoglobin, pulsing architecture and sensation inside the skull are the best material here. Some replies sound like a lecturer telling me the true meaning of an allegory it just made up. Greenhouse, seed and bloom in succession make it sound more pleased with the metaphor than interested in what my body is doing.
- Court A: Actual customs give these people ways to speak. The Gardener feeds, the Linesman asks before tying you in, and the colony has to drop a strand to speak as one. Keep those distinctions. Several greetings still feel like the same script for certifying how far I swam, and the Gardener asks if I've eaten often enough to make hospitality feel like a button prompt. The refusal when you threaten the beds does more for the house than another endearment.

## 3. Where I disagree with the audit

### Arithmetic and interpretation

- Recomputed with `scan.py` on each original and each original overlaid with its final proposal. Outside the Hermit there are **2,195 group entries, 2,189 unique old keys, 1,435 proposed entries and 1,430 unique proposed keys**. The audit's unique totals are right. The cadence and scanner totals come from pooled group entries, with duplicates and different proposed versions still in the pool. Old text has **2,113 nonempty scanner lines, 5,116 sentences, 40.75% short sentences, 356 hits, 16.85 hits per 100 lines**. The overlays have **2,112 lines, 4,968 sentences, 10.64 words per sentence, 28.95% short sentences, 177 hits, 8.38 per 100**. The loss of one spoken line is the Vicar's newly bracketed entrance direction. Including the Hermit changes the rate to **18.17 → 8.79**. Label the denominator, stage-direction treatment and overlap explicitly.
- Sample cross-checks, before → after. Guardian/Scout **4.06 → 6.84** words per sentence and **44 → 21** hits. Mnemonist **7.58 → 11.23**, **34 → 16**. Monolith **6.42 → 10.81**, **6 → 3**. Hermit **7.60 → 9.60**, **35 → 12**. Vicar goes the other way on cadence, **16.51 → 14.02**, as the pass divides compound sentences. Fungal Entity goes from **19.7% → 26.4%** short sentences, successors from **40.3% → 45.1%**. None of these directions gives a quality verdict by itself.
- The 13 hand-count rows sum to **2,145**. The claim that the scanner catches about one in six is unsupported. It compares totals from two different classification systems without matching individual hits. A line can have several hits in either count. Reviewers used different units for repeated families. For the 16 groups, **131 of the 202** hits removed are from the automatic short-closer rule alone (**252 → 121**). The improvement in the score is substantially an improvement in that proxy.
- The ranking is an editorial ordering. Rewrite share is an intervention rate, and a brief that encourages large rewrites will move that group up. I would move the Fungal Entity ahead of the Monolith for revision priority, because the Entity's late dialogue defines the player's understanding of the whole mod. I would move the Scout below the Guardian for voice intervention. The Scout's repetitions have a local cause and there are only a few urgent scenes. The old Scout line about what the creatures take her for is better than the diagnosis that they cannot see her. The Guardian's old ambient injunction works in isolation. Neither needs to become a repeated signature.
- The October sheets plausibly explain some of the problem. Artificer `armature.line2` is almost a demonstration of the old hammer-cadence instruction, and copying example sentences is direct evidence of reuse. The pass has no control for role templates, shared keys, repeated UI states, scene order or a writer's defaults. Much of the Court repetition maps to shared state and the same legal rule. The current brief still asks for an object bank, rhythm, Words and numeric targets. Several new sheets prescribe a long sentence followed by a short one. That can reproduce the same problem with different timing. The audit's statement that every sheet has moved past the October recipe is premature.

### What the scanner measures

- `plain()` discards all square-bracket spans, so gesture repetition and a substantial portion of Fungal, Lady and Hermit presentation are invisible. Its sentence split leaves punctuation before a closing quote attached to the following sentence and can split on abbreviations or ellipses. Its contraction regex accepts possessives such as “Vesper's”. The contrast map's older figures also use a different sentence definition. Avoid comparing the two tool outputs as though they were a single time series.
- `short_closer` is a final sentence of at most four whitespace-separated words on a multi-sentence line. It catches useful commands such as the Vicar's “Inject it, then sleep.” in `vicar.votary.degree_hint`. It misses the Hermit's six-word joke “Nobody is waiting for the chair” and the long reflective close in the Mnemonist's `what_it_cost.line2` (eight). A natural operational correction can match P1, while “Joining the Order is another matter” escapes. The alternation for “veil” can count the proper name Crimson Veil. The zero post-pass score for personification does not account for what the Hermit's heart “will let you” do or what the Monolith's frame “can remember”.
- `facts()` reports old keys missing from a sparse proposal if called with the full old file. I compared only proposed keys against the matching old strings. It collects a **set** of old token strings and asks whether each is a **substring** of the new line. `500` survives a change to `1,500`. Two `%s` placeholders can become one without a hit. `3,` and `5,` show up as tokens, and “Full Hematic Iron” can disappear by changing sentence position and case. It never tests new claims, claims that keep the same vocabulary, reordered steps or whether a handoff is reachable.
- The method is easy to optimize against. Lengthening a punchline to five words or replacing “not X, but Y” with “X is another matter” gets a lower score for the same dramatic move. The corpus exhibits both outcomes. I found no evidence about whether a writer aimed for the score. Treat the numbers as search leads.

### Application order and test pins

- The audit's **47** fingerprint rows are reproducible. I matched complete old strings anywhere inside each `said` field. Mnemonist rows **212–213** have two messages, so looking for `said=[<old text>]` alone incorrectly gives **45**. There are 13 Mnemonist, 12 Artificer, 10 Alchemist, 6 Fungal Entity, 3 Vicar and 3 Zealot/Acolyte rows. No additional affected rows surfaced in this file by that method. A source comparison predicts changes to saved dialogue output. It does not verify runtime state.
- Missing from the audit's checklist: `CondenserTeachingResourceTest.everyFormationLessonNamesTheCauldron` pins lowercase `cauldron` across seven lesson/ledger keys, six of which are applied or proposed in this pass. It also pins `Venous Stone` in `vicar.neophyte.projection_hint`. The present proposals keep both. `PallidVesselDialogueTest` also checks `Sanguine Formation` in `alchemist.initiate.loom_raising`. `PlaythroughVotaryGameTests` explicitly checks `Alchemist`, `Concentrated Blood` and `sleep` in `vicar.votary.degree_hint`. Keep these in the revision checklist.
- The claimed “half a story” / no “eighth” pin for `vicar.archon.final_truth` is misattributed in this checkout. `PlaythroughArchonGameTests` asserts the key is reachable. `AuditDecisionsResourceTest` bans “eighth” in other keys, including `vicar.initiate.saints_lore`. Both HEAD and the proposal say “eighth” in `archon.final_truth`. Keep the rumor and make its provenance clear.
- `shatterBeforeCornerstone` is a conditional acceptance check. It accepts either a warning containing both `cornerstone` and `fragment` across the two relevant lines or enough guaranteed drops. It is worth inspecting as a player warning independently of which branch makes the check pass.
- The plan's early small batches, paired Court review, shared-key resolution and check of changed briefs are sound. “Worst first” describes neither the ranking nor the risks. I would put the applied Hermit and the shared Vicar/Alchemist teaching routes ahead of the cosmetic Monolith pass. Resolve five conflicting recruit proposals before landing either teacher copy. Review the Fungal Entity, Monolith and Vicar endgame branch together for who can know what. Keep the original and proposed text as separate artifacts until a family has had its content and voice checks. Then run focused tests and review the fingerprint diff before updating the goldens. Preserve the audit's native read at the end.

## 4. New or surviving patterns

Counts below are lines matching the stated screen over all 16 group overlays, with each group entry counted separately. They measure the example pattern, with judgment about individual lines kept separate.

- **Softening a statement about knowledge. 2 → 4, across 2 → 3 groups.** Exact phrases “I will not pretend”, “I won't pretend”, “for what it is worth”, case insensitive. Hermit `hemomancy.hermit.adept.scar_lore` ends “I will not pretend it is comfortable.” His `hemomancy.hermit.archon_wisdom.prompt` ends “for what it is worth to you.” Vicar `hemomancy.vicar.archon.final_truth` says “I do not know what the Monolith has shown you, Archon, and I will not pretend to.” Voyager `hemomancy.voyager.item_inquiry.unknown` says “I don't know that one, and I won't pretend to.” The Vicar has reason to be careful. The Voyager can go straight to the depth and findspot.
- **Dry tags after the line has done its work. 0 → 6, four groups**, on the phrases “I've stopped correcting”, “I'll deny”, “nobody is waiting”, “I would only be telling”, “bread out here”, and “more than I'd admit”. Hermit `hemomancy.trade.hermit.thanks` ends “Nobody is waiting for the chair.” Artificer `hemomancy.artificer.item_inquiry.hematic_armature.line1` ends “People call it a forge. I've stopped correcting them.” Mnemonist `hemomancy.dialogue.rapport.mnemonist.what_it_cost.line2` ends “I take it out more than I'd admit.” Ringmaster `hemomancy.circus.ringmaster.line.succession_complete` uses “I'll deny that if you repeat it” before handing over the ring. This phrase screen illustrates the tendency and is deliberately too narrow to estimate all jokes. The Mnemonist's concealment belongs to the speaker and can stay. The hat quip softens the transfer of control at a poor moment.
- **Domestic inventory. 14 → 22, two → four groups**, matching the standalone words “chair”, “bench” or “bread” after stripping stage directions. Examples include Hermit `hemomancy.hermit.votary.line2`, “He stayed three days and ate all my bread”, and Alchemist `hemomancy.alchemist.first_separation.next_distillation`, “I'll keep them under the bench.” The second is mechanically plausible as a metaphor for held tools, and “bench” often means actual equipment. The issue is concentrating colour on food, furnishings and tired hospitality in the same progression run. The Hermit can be kindly and still make a visitor deal with a living heart out of its body.
- **Contrast in a change of subject. 0 → 1 exact “another matter” match**, Hermit `hemomancy.hermit.no_blood.line3`, “Joining the Order is another matter.” It does separate two mechanics the player needs to distinguish. Artificer `hemomancy.artificer.monolithic_armature.line2`, “Silent Archon cloth is a different vow, and a different conversation”, uses the same move without that phrase. Neither is a scan hit. Keep functional contrast and cut the authorial flourish when it displaces a useful distinction.
- **Court referral still starts from the referral. 14 → 14**, among `.greet.referred` keys matching variants of someone/they sent, told, showed, gave, or said, including “sent down”. Arbor `hemomancy.court.arbor.greet.referred` says “They told you of us up there”; Tidewarden `hemomancy.court.tidewarden.greet.referred` says “They said you'd be coming”; Host `hemomancy.court.host.greet.referred` says “Somebody sent you, then”; Bellwarden `hemomancy.court.bellwarden.greet.referred` says “Someone sent you down to the bell.” More distinctive second sentences help, but the state readout still occupies the opening across both packs.
- **Court reassures after refusal. 0 → 5 in a narrow screen** matching “nobody/no one” with “will/is going to” and a verb for pursuit or harm, permitting “here”, “in the bell” or “behind me” in between. Braced elder `hemomancy.court.braced_elder.shunned.line2` says “Nobody behind me will follow you”; Veiled resident `hemomancy.court.veiled_resident.shunned.line2` says “Nobody will hurt you”; Bellwarden `hemomancy.court.bellwarden.shunned.line2` says “Nobody in the bell will come after you.” The wider promise of peaceful withdrawal already existed in the old text. The new surface formula is what increased. Separately **8 → 8** one-switch refusals contain “already” or “once”. The one-switch rule should remain legible. Some speakers can simply acknowledge the refusal in their own terms if a shared explanation also displays.
- **Practical length.** Across the proposals, **83** lines gained at least 40 characters and grew at least 50% after removing colour codes, with stage directions included. The Monolith accounts for **18 of 47** proposals on this screen. `hemomancy.monolith.lore.what_opens.line1` is **51 → 132**. Anchorite `hemomancy.anchorite.d5.cause_strain` is **156 → 257**. Guardian `hemomancy.guardian.item_inquiry.lethean_dew.line1` is **102 → 180**. Ringmaster `hemomancy.circus.ringmaster.line.liberation_complete` is **30 → 117**. `DialogueScreen` wraps the node and scrolls the content. Extra lines still cost viewport space and can push the choices down. These are character counts, with no claim about observed clipping.

### Voice separation test

I read pairs by situation with keys and explicit speaker, house or signature ability names hidden where those would give away the answer. This was a qualitative self-test by a reader who already knew the cast. It tests the underlying distinction more severely than normal play, where the names, staging and NPC are visible.

| Pair or set | What survived masking |
|---|---|
| Zealot / Acolyte | “Have you started skipping days?” reads as someone checking compliance. “Are you sleeping all right?” reads as someone checking the person. Good separation in the middle of purification. Generic newcomer referrals would be difficult to place. Avoid making the Acolyte permanently apologetic or the Zealot an interrogator in every branch. |
| Guardian / Scout | “Pick your shot” and the item advice deal in immediate safety. The Scout's notes, paper and boot are about an observation she needs someone to preserve. The revised newcomer lines have nearly the same instruction, but the Scout has a reason to break it off. Keep her fragment sequence and her line about her sister. |
| Court shore | “I have let go of it now, so that I can speak to you with one voice” identifies the colony. “There's slack on this line if you want it” identifies the Linesman's consent. Edge-swimmer's group imperative is distinguishable from the Tidewarden checking the passage. The rule recital alone would not identify the Braced elder or Nursery tender. |
| Court deep | The Host's “Don't tell me which” and the Veiled resident's “I will not talk much” approach privacy from opposite sides. A sealed room full of sleepers identifies the Bellwarden. The residents' alternate generic shunning reassurances, the two Kindling attendants' formulas and the Rooted elder's switch refusal are much harder to place. |
| Circus faculty | Acrobat thinks in recovery after a fall, Strongman in whether a body can protect a child, Stilt Walker in who is looking where, and Dagger Thrower in what a distraction buys. Those disagreements are stronger than length or contraction targets. The Fire Breather's calm sequence and Threadkeeper's inventory are hard to place when the apparatus is masked. The teachers' generic observations about teaching and scattered camps should stay unattributed if shared by code. |
| Outpost teachers / Monolith / Lady | Alchemist measures and handles specimens, Mnemonist recognizes a memory's shape, Anchorite asks where a route fails, Artificer cares whether gear survives being worn, and Vicar accounts for what his office can authorize. Strong when the line is about the work. The Monolith's new “they wrote down everything” aside and sense of having missed visitors bring it close to ordinary recollection. The Lady's patient cold should be distinct from the Entity's reproductive accounting, even without `I` and `we`. |

My taste on tone: the mod can accommodate a recruit who came for soup and a Gardener who feeds people. The Acolyte's soup story explains why someone would stay in the Church without sharing all its beliefs. It serves character better than a metaphor about a lodger in an instruction node. The fungal wet strand in the skull and the Hermit's nerves growing along a scar retain the right physical register. I found no clear God/Satan framing in the passages sampled. The Lady's “They pray to me as if I were a goddess” describes what others do, consistent with her saying what she is. Her “I keep a few of them” goes further toward personal predation than a protective power's warning and deserves a canon decision. The Fungal Entity can be sinister while speaking literally about its tissues. Removing “child” in the address helps. Removing biological agency from living architecture would hurt.
## 5. Ratings

“Apply” means the sampled prose can land once shared content and test checks are done. “Apply with fixes” means a small set of corrections is enough. “Redo” means reread the family in scene order before selecting lines. Ratings cover the supplied groups and separate their speakers where attribution is clear. Court roles are rated individually. Shared Circus and succession text has its own row.

| Pack | Speaker | Rating | Main problem | Most important fix |
|---|---|---|---|---|
| Hermit | Harbinger Hermit | redo | The opening and ranks repeatedly supply the same tired hospitality. | Center the visible heart, the personal blessing and the choice to leave. |
| Vicar | Harbinger Vicar | apply with fixes | He gives one proof sequence as if ordered and frames rumor with a stock disclaimer. | Keep rank, chronology and institutional responsibility explicit. |
| Alchemist | Harbinger Alchemist | apply with fixes | The Neophyte greeting implies the promotion already happened. | Put the second Degree in the Vicar's future remit. |
| Mnemonist | Harbinger Mnemonist | apply with fixes | Some clinical and memory revelations end in a shelf prop or a wink. | Keep the lesson's concrete observation and use the archive where it bears on identity. |
| Artificer | Hematic Artificer | apply with fixes | Hot bowls and a working frame are specific. General self-deprecation muddies them. | Cut comic tails from fitting instructions. |
| Vein-Mason | Cicatrix Anchorite | apply with fixes | A precise strain test gets a long performance of menace. | Give the mechanism and excluded damage sources directly. |
| Monolith | Sanguine Monolith | redo | Growth in line length makes distinct stages sound like the same storyteller. | Rework the fifth, sixth and seventh Degree sequence as changes in agency and access. |
| Voyager/Wayfarer | Harbinger Voyager | apply with fixes | Caution becomes a reflexive denial of pretence. | Ask for an observation the player could actually report. |
| Voyager/Wayfarer | Votary Wayfarer | apply | The apprentice's eagerness works, with a few extra joke tags. | Let the observation stand when it already reveals inexperience. |
| Zealot/Acolyte | Zealot | apply with fixes | A good compliance voice occasionally lectures through living-aura metaphors. | Say what Verdigris Aura or Silver Ward actually does. |
| Zealot/Acolyte | Acolyte | apply with fixes | Personal warmth becomes a long illustration or an invented universal testimonial. | Use individual sympathy and state the abstinence consequence exactly. |
| Guardian/Scout | Guardian | apply with fixes | Some direct field answers gained more anecdote than instruction. | Keep the practical clause and let a short order survive where warranted. |
| Guardian/Scout | Unstained Scout | apply with fixes | A carefully limited observation becomes certainty about perception. | Restore the distinction between being seen and being classified as threat or prey. |
| Lady/Annetta | Our Lady of Still Waters | apply with fixes | Watchful patience becomes either repetition or direct predation. | Keep the warning about danger within her established jurisdiction. |
| Lady/Annetta | Annetta Knowles | apply with fixes | “Behind me” implies positioning with uncertain gameplay value. | Keep the immediate support instruction grounded in the encounter. |
| Fungal Entity | Fungal Entity | redo | A nursery metaphor survives intact and the voice adds questions it immediately answers. | Rework revelation pairs around literal bodily processes and strategic omission. |
| Circus | Ringmaster | apply with fixes | Victory and defeat both pick up conversational theatre business. | Give ownership and finality their weight. |
| Circus | Acrobat | apply | The recovery and rigging perspective is distinct. | Keep the return path visible in practical instructions. |
| Circus | Dagger Thrower | apply with fixes | Some questions are applause cues. | Explain the three-dagger volley before judging a pupil. |
| Circus | Stilt Walker | apply | Attention and misdirection are legible in the lesson. | Keep the pupil's escape route central. |
| Circus | Strongman | apply with fixes | “I want to see if you'll do the same” turns a protection lesson into a courage test. | Make the criterion whom the Hulk protects. |
| Circus | Beast Tamer | apply with fixes | The old-dogs comparison extends a long order. | Keep the distinction between a built Hound and an animal in the history. |
| Circus | Fire Breather | apply | The scorch line and the bodily sequence serve the encounter. | Keep the action sequence intact. |
| Circus | Threadkeeper | apply with fixes | Crossbar checks and ledger classification crowd one line. | Keep the instrument and test sequence prominent. |
| Circus | Retired Understudy and shared faculty lines | apply with fixes | Shared copy can sound like a specific faculty member. | Separate the Understudy's memory remit from the common display copy. |
| Court A | Gardener | apply with fixes | Hospitality can devolve into answering every prompt with food. | Keep refusal tied to the beds and let an action convey care. |
| Court A | Arbor | apply with fixes | Referral echoes the arrival summary used elsewhere. | Start from the many and the light already on the visitor. |
| Court A | Lightkeeper | apply with fixes | Claims first lesson through a generic referral opener. | Lead with what the Lightkeeper can teach here. |
| Court A | Tidewarden | apply | A sounded passage belongs to this post. | Keep the safety instruction ahead of the referral. |
| Court A | Braced elder | apply with fixes | The one-move refusal sounds like the Nursery tender's rule. | Tie the refusal to what this elder holds open or shuts. |
| Court A | Edge-swimmer | apply | Short plural orders do useful work. | Preserve movement with the school as the basis of the voice. |
| Court A | Nursery tender | apply with fixes | Switch refusals fill space a nursery action could occupy. | Give the legal condition once and put the young in view. |
| Court A | Linesman | apply with fixes | The referral asks for a source and then answers nothing about it. | Offer slack and let the player choose. |
| Court A | Lamp-colony | apply | Letting go to speak in the singular is strong. | Let reconnecting be the action in refusal. |
| Court B | Host | apply with fixes | A privacy ethic is distinctive, the referral setup generic. | Make discretion the first thing the guest encounters. |
| Court B | Veiled resident | apply with fixes | Promises and referral language make a reticent speaker explain themselves. | Give the threshold and withdrawal in fewer words. |
| Court B | Bellwarden | apply with fixes | The sealed dome is specific, the pursuit promise interchangeable. | Use the lower door and protection of sleepers as the scene's action. |
| Court B | Sleeper | apply | All 11 sampled keys are untouched. The mantle and breathing carry the encounter. | Preserve the mostly silent baseline beside the Bellwarden. |
| Court B | Marrow-tender | apply with fixes | Refusal can sound like the whole Court's generic contagion notice. | Keep attention on the fall and what could spread there. |
| Court B | Rooted elder | apply with fixes | Good embodied sensation disappears in legal recitation. | Let the elder register water and disturbance before the rule. |
| Court B | Offering-bearer | apply with fixes | Referral and hospitality overlap with the Head-tender's gatekeeping. | Give the visitor a safe place to observe the feeding. |
| Court B | Kindled elder | apply | Short fragments fit an elder whose concern is whether this body becomes a head. | Keep the uncertainty of Hydra's decision. |
| Court B | Head-tender | apply with fixes | Some welcomes and refusals sound like a gate with a title attached. | Make the threshold and preparations carry the authority. |
| Court B | Marimo's chorus | apply with fixes | A growing initiation line ends in a Gardener-like prompt. | Keep the sequence and make consent to join the walls unmistakable. |
| Successors | Successors and Misbegotten | apply with fixes | The novice personas skew contemporary and the fractured voices can be over-normalized. | Preserve professional competence in the first and broken ownership in the second. |
| Successors | Teacher and recruit lines | apply with fixes | Some recruitment text speaks after the commit while the player is still choosing. | Use future or conditional tense before acceptance and resolve the overlap once. |

## 6. Accuracy by key

The full keys below take the `hemomancy.` prefix unless shown otherwise. “Confirmed” distinguishes observed text or source behavior from a claim about how it feels in play.

| Key or family | Status | Evidence and consequence |
|---|---|---|
| `alchemist.neophyte.line2` | **Confirmed regression** | “The Vicar has your bloodcraft proof and the second Degree” speaks as if proof is already in hand. The Neophyte tree is Degree 1; `FirstBloodcraftAssignment.promote` advances to 2 only after `canClaim`. The old text assigns jurisdiction without presupposing proof. |
| `hermit.neophyte.hint`, `vicar.neophyte.first_bloodcraft_reward.unready` | **Confirmed handoff, one clarity issue** | The Vicar's Degree hint and separate Bloodcraft lessons carry the four proofs. `HarbingerAssignmentLedgerScreen` renders absorption, formation, Venous Stone and either structure with live progress and help text. `FirstBloodcraftAssignment` records them independently, with 500 mL cumulative absorption. The shortened lines have a real destination. The Hermit presupposes that the player has spoken to the Vicar about the tasks and has the ledger. Phrase it so the Vicar can still be the next stop. |
| `vicar.neophyte.degree_hint`, `vicar.neophyte.first_bloodcraft_reward.unready`, `vicar.item_inquiry.sanguine_condenser.line1` | **Confirmed ambiguity** | The full hint says “Then” before the structure, while the reminder says any order. The four proofs do not have a gate order. For the cauldron, `SanguineCondenserBlockEntity.receiveProjectedBlood` records contributions, and collection checks that the collector contributed. “One you filled yourself” can be read as sole ownership or supplying every drop. Neither is required by that check. “Project your blood into an empty cauldron, then collect the formation yourself” teaches a reliable path without inventing exclusivity. |
| `hermit.no_blood.duty.line2` | **Confirmed precision and formatting loss** | The per-player gate is real. `TempleOathRules.canClaimHeart` compares that player's blessed Hermit UUID with the Display's linked Hermit and checks unclaimed state. “Once I have blessed you, the heart will let you take it” preserves the need for blessing but leaves the personal rule implicit. The old `§o` is gone. Restore the distinction through permission and reserve italic emphasis for the last personal request. |
| `hermit.no_blood.heart_lore`, `dialogue.event.hermit_passing`, Hermit rank greetings | **Confirmed text changes** | “I cut it out” is supported by lore §5.2. The line lost the name Mortal Display where it used to say it explicitly. The passing line removes the outpost and teaching destination while adding “The others are expecting you”. The route gives a waybill to an outpost. I found no evidence that anyone notified that outpost. Crimson Veil, Hematic Covenant, Incarnadine Fane, Sanguine Brotherhood, Crimson Lodge, Bloodline Covenant and Hematic Order also drop from individual greeting keys. Moving a title out of a greeting can improve it. The brief requires accounting for where the player still learns each canonical name. |
| `hermit.tendency_lore` | **Confirmed unsupported quantifier** | “Many carry a second current” became “most”. The token check cannot see this. Revert to “many” unless someone checks the full manipulation definitions and intentionally changes the teaching claim. |
| `hermit.votary.hint`, `hermit.adept.hint`, `hermit.illuminatus.hint` | **Confirmed destinations, inherited imprecision** | The D2 ledger text `step.settle_concentrated_blood.desc` and the Vicar name First Separation, a completed distillation, injection and sleep. `ConcentratedBlood` checks completion/claim state. “Let them watch” is a figure of speech that can mislead about proximity. The Vein-Mason's tree/lessons actually carry the carving and Brazier work; avoid implying any additional prerequisite for Crimson Lodge from the Hermit's sequencing alone. The Vicar's dynamic `withCovenantGuidance` and ledger `covenant_written.desc` do give the in-boundary personal Bloodwell route, including the fifth Degree. |
| `alchemist.purifying.explain` | **Confirmed route exists, suspect player guidance** | The Vicar has a purifying tree with warning and chosen-path branches. It is not an Unstained teaching route. “Ask the Vicar if you want it explained” leaves the subject implicit and redirects a player seeking this Alchemist's response. Either name what the Vicar can tell them or keep the refusal self-contained. |
| `mnemonist.chamber.line1` | **Confirmed presupposition; reachability concern** | The original names the Chamber when asked and says sleep “may” take the player there. The proposal assumes the player already dreams about it. The system allows early visits. I did not establish that every player on this node has visited. |
| `acolyte.corrupted.line2` | **Confirmed mechanic, unsupported anecdote** | `BloodManipulation` calls `PurityGainEvents.onBloodManipulationUsed`. A cast after purification starts resets the timer for abstinence purity. The hook does not subtract purity already earned. “Everyone here who gave in” is new testimony, and “harder to get free” could imply a more general setback. Name the wait. |
| `scout.what_happened.line1`, `scout.observation_hint.line1` | **Confirmed losses and changed inference** | The scout's remit loses “what they eat”. The observation loses the creatures circling and actively distinguishes neither prey nor threat anymore. “They don't see me at all” asserts perception failure, though walking over a boot establishes indifference equally well. The empty hand in the farewell is a good correction and should stay. |
| `guardian.item_inquiry.warhammer.line2` | **Suspected overreach** | “Whoever it is, you'll be carrying them out after” turns containment into a universal evacuation instruction. The Church can favor containment and extraction without promising this outcome against every target. The original is ambiguous about what is to be saved. |
| `voyager.court.known.alchemist` | **Confirmed loss / new inference** | The Order keeping a list is removed. “I'd guess it cost them something to say so” invents a suggestion of reluctance and spends the line on that inference. The provenance of the strains is more useful. |
| `lady.cleansing.v1.line1`, `annetta.event.cured` | **Suspected canon/tone risk; confirmed source distinction** | Lore §8.1/§8.6 locates the Lady's defensive nature and the later worry about eradication. Her personally keeping drowned people is new in this passage. For Annetta, lore §11 can support the back. `AnnettaKnowlesEntity.tickPurge` broadcasts the text at TEAR and emits particles, with no creature spawn in that branch. Creature spawning in the mutation sequence is a different beat. Treat the new wording as canon narration rather than an observed spawn location. |
| `circus.school.circus_acrobat.lesson`, `circus.school.circus_knife_thrower.lesson`, `circus.school.circus_beast_tamer.lesson` | **Confirmed equivalent numbers** | Arabic numerals 3, 3 and 4 become “third” and “fourth”. The only other removed Arabic numerals in a separate numeric-token comparison were the two 500 mL handoffs above. The comparison does not measure word numbers or quantities implied by grammar. |
| `whisper.illuminatus.v0.follow`, `whisper.illuminatus.v1.line2`, `communion.v4.name2`, `vicar.uninitiated.line1` | **Confirmed format changes** | In order, code sequences change from two to one `§7§o` runs, three to one `§7§o` runs, three to one `§4§o` runs, and no codes to `§7`. The first three lose markup runs even when their repeated codes have the same values. The Vicar's stage conversion is deliberate. Review emphasis placement before deciding whether to keep it. Comparing ordered `%s` / numbered `%s` sequences found no changes. |
| `communion.v1.schism1`, `communion.v2.infection2` | **Suspected framing drift** | The old Schism explanation already has “we cut them back”. The proposal sharpens it to “We thinned you.” The next line still credits the breakaways with finishing the cut. Keep the split in agency. “We have counted them all” is new omniscience flavour. The entity can count connected nodes, but the proposal need not make that claim to explain spread. |
| `dialogue.recruit.alchemist.accept`, `dialogue.recruit.vicar.accept`, `dialogue.recruit.release.prompt` | **Confirmed context** | The overlap must resolve in favour of pre-acceptance timing for recruitment, as the audit recognized. For release, `DialogueHubFactory` builds a synthetic NPC node with the `promptKey` in its lines and the confirmation as a separate option. `DialogueScreen.renderNode` renders these node lines before its response options. First-person NPC prose is compatible with this path. Check equivalent `.prompt` contexts for station lessons instead of giving all suffix matches one voice. |

## 7. Selective alternatives

Three to five lines per supplied speaker pack. Current text is quoted verbatim. A few alternatives restore a stronger old line. The Hermit's duty and the Entity's name restore colour runs missing from the proposals.

### Hermit

**`hemomancy.hermit.no_blood.line3`**

- **Proposal**  That is my heart on §cthe Mortal Display§r. Stand still and you can hear it. With my blessing you may take it, and it will wake your blood. Joining the Order is another matter. The Vicar does the asking, and you can still say no to him.
- **Revision**  My heart is still beating on §cthe Mortal Display§r. I'll give you leave to take it. Your blood will wake. If you decide to join us, ask the Vicar to initiate you.
- **Reason**  Keeps the body in the room and separates waking from joining.

**`hemomancy.hermit.no_blood.duty.line2`**

- **Proposal**  Once I have blessed you, the heart will let you take it. Think it over first, if you need to. When it is yours, come back to me and I will tell you where to go. And do not look so sorry about it. I have been sitting here a long time waiting for someone to come and take it.
- **Revision**  My blessing is for you. Anyone else will have to ask me for their own. Take the heart when you're ready. §oCome back to me before you leave.
- **Reason**  Restores the personal gate and italic cue without a chosen-one speech.

**`hemomancy.hermit.votary.line2`**

- **Proposal**  A Votary? You sound steadier than the last one who came through here. He stayed three days and ate all my bread.
- **Revision**  You've been received as a Votary of the Hematic Covenant. I can hear the change in your pulse from here.
- **Reason**  Keeps the rank and observation and drops the bread anecdote.

**`hemomancy.hermit.tendency_lore`**

- **Proposal**  Every manipulation pulls your blood one way, and most pull a little in a second direction underneath. Use a kind long enough and your blood starts leaning that way by itself, and toward the tendencies that run beside it. Mine leaned so far I stopped arguing with it.
- **Revision**  Every manipulation pulls your blood one way. Many pull a little in a second direction. As you master them, the pull takes hold in you and in the tendencies that run alongside it.
- **Reason**  Restores the old quantifier and the link to mastery.

**`hemomancy.hermit.neophyte.hint`**

- **Proposal**  The Vicar will have given you his four tasks, and your ledger keeps that list better than I can. The one people get stuck on is the cauldron. Fill it with projected blood and take the Sanguine Formation that condenses in it. Then go back to him.
- **Revision**  Ask the Vicar for the four proofs if you haven't heard them. They're in the ledger. For the cauldron, project your own blood into it and take out the Sanguine Formation when it sets. Then go back to him.
- **Reason**  Keeps the working handoff and avoids assuming the player already has the list.

### Vicar

**`hemomancy.vicar.neophyte.degree_hint`**

- **Proposal**  Four proofs stand between you and the Second Degree, and the ledger keeps count of each. Absorb 500 mL of blood. Condense a Sanguine Formation in a cauldron. Project Venous Stone. Then use a blood formation to craft either a Liber Sanguinium or a Hematic Iron Block. When the ledger shows all four, bring it to me.
- **Revision**  For the Second Degree I need four proofs, in any order. Absorb 500 mL of blood, condense a Sanguine Formation in a cauldron, project Venous Stone, and form either a Liber Sanguinium or a Hematic Iron Block by Blood Projection. Bring me the ledger when all four are recorded.
- **Reason**  Removes the implied order and names the actual crafting method.

**`hemomancy.vicar.neophyte.first_bloodcraft_reward.unready`**

- **Proposal**  Not yet. Open your ledger and tell me which proof is still blank, because I need all four, in any order. If it is the cauldron, remember it must be one you filled yourself, and you must collect the Sanguine Formation from it.
- **Revision**  I need all four proofs, in any order. Your ledger will tell you what's missing. For the cauldron, project your blood into it and collect the Sanguine Formation yourself.
- **Reason**  Retains both contribution and collection without implying sole ownership.

**`hemomancy.vicar.archon.final_truth`**

- **Proposal**  §4§oI do not know what the Monolith has shown you, Archon, and I will not pretend to. §rI was charged to pass one thing on to Archons, and it is a saying, nothing firmer: that the Order never had seven degrees but eight, and that the eighth is silence. Past that there is only hearsay. The oldest accounts name two roads beyond Archon, Silentium and Apotheosis, and agree on one point, that nobody who walked either was ever again someone the Lodge could recognize. Whether the roads are real, I have no warrant to say. §4§oTo me they are legends. §rThat is the whole of what I was given.
- **Revision**  §4§oI do not know what the Monolith showed you, Archon. §rThis is the saying I was charged to pass on: there are eight Degrees, and the eighth is silence. The oldest accounts name Silentium and Apotheosis. The accounts say the Lodge never again recognized anyone who took either road. Whether those roads are real, I have no warrant to say. §4§oTo me they are legends. §r
- **Reason**  Keeps all colour runs and the hearsay boundary, and drops the disclaimer and closing verdict.

### Alchemist

**`hemomancy.alchemist.neophyte.line2`**

- **Proposal**  Neophyte, are you? Then the Vicar has your bloodcraft proof and the second Degree, and I've got something smaller. Draw a few clean samples from ordinary animals and hang on to them. You'll want them for the Centrifuge later.
- **Revision**  The Vicar takes your bloodcraft proof for the second Degree. While you're working on it, draw a few clean samples from ordinary animals. Keep them for the Centrifuge.
- **Reason**  Gives the Neophyte something useful without announcing promotion.

**`hemomancy.alchemist.purifying.explain`**

- **Proposal**  §7[The Alchemist doesn't look up]§r Ask the Vicar if you want it explained. Every hour you spend on that path is an hour you aren't learning these machines, and I've got a rack to finish.
- **Revision**  §7[The Alchemist doesn't look up]§r I can't teach you the machines while you're purging the blood they work with.
- **Reason**  Keeps the refusal with its owner and avoids an opaque referral.

**`hemomancy.alchemist.item_inquiry.bloody_flask.line1`**

- **Proposal**  A Bloody Flask carries blood from one place to another. It has nothing to do with the syringe-and-rack assay, so don't file it there.
- **Revision**  Use a Bloody Flask to carry blood. Keep the syringe and rack for samples you mean to assay.
- **Reason**  Conveys the equipment distinction in less space.

### Mnemonist

**`hemomancy.mnemonist.chamber.line1`**

- **Proposal**  The room you sometimes dream about is your Chamber of Will. In the first Degrees, ordinary sleep may give you a minute or two there.
- **Revision**  That's the Chamber of Will. In the first Degrees you may spend a minute or two there during ordinary sleep.
- **Reason**  Keeps the conditional and the original time.

**`hemomancy.dialogue.mnemonist.conductive_mark_recognized`**

- **Proposal**  Your first weave has the conductive pattern in it, the same one the Alchemist measured in Ductilis tissue. They measured it and I heard it, and it annoys us both that we agree. It's one Noetic Memory, and we got to it by two routes.
- **Revision**  I heard the conductive pattern in your first weave. The Alchemist measured that same pattern in Ductilis tissue. Two routes to one Noetic Memory.
- **Reason**  The different methods carry the disagreement without an invented shared reaction.

**`hemomancy.dialogue.event.mnemonist_woven_vessel_complete`**

- **Proposal**  The Mnemonist writes the blank vessel into the index and blots the page. At the Loom, choose Blood Shot: one bulb, 50 mL of blood, one Vivacious Enzyme. Keep a Living Staff in hand. When the engram unfurls around the Loom, rest your eyes on the fragment until you hear it. Then hold the staff on it and sweep its thread along the star's lines into the Loom.
- **Revision**  The Mnemonist indexes the blank vessel. At the Loom, choose Blood Shot: one bulb, 50 mL of blood, one Vivacious Enzyme. Keep a Living Staff in hand. When the engram unfurls around the Loom, rest your eyes on the fragment until you hear it. Then hold the staff on it and sweep its thread along the star's lines into the Loom.
- **Reason**  Restores the shorter pinned text and all of the use instructions.

### Artificer

**`hemomancy.artificer.armature.line2`**

- **Proposal**  Put the armor on and load the bowls with reagent. Fill the reservoir, step into the frame and hold still. It takes a while, so don't scratch your nose.
- **Revision**  Put on the armor and load the bowls with reagent. Fill the reservoir, step into the frame and hold still until it's done.
- **Reason**  Keeps the sequence and ends on what the player has to do.

**`hemomancy.artificer.monolithic_armature.line2`**

- **Proposal**  This is where Blood Lust splits from the common form. Proboscis takes you Edacious, Fervent Husk Sheolic, Ambergris Phantasmal. Silent Archon cloth is a different vow, and a different conversation.
- **Revision**  Blood Lust branches here. Proboscis for Edacious, Fervent Husk for Sheolic, Ambergris for Phantasmal. Silent Archon cloth takes its own vow.
- **Reason**  Keeps every fork and drops the conversational pivot.

**`hemomancy.dialogue.event.artificer_hematic_iron_fitting_unready`**

- **Proposal**  The Artificer walks round you once and shakes his head. 'Where's the rest of it? I want the full Hematic Iron set on you before I fit anything.'
- **Revision**  The Artificer looks at the gaps. 'Full Hematic Iron set, on your body. Then I'll fit it.'
- **Reason**  The old refusal is short, precise and already in the golden.

### Vein-Mason

**`hemomancy.anchorite.d5.cause_strain`**

- **Proposal**  [The Vein-Mason taps one of the vessel diagrams chalked on the wall] Now I want you hurt, and hurt the right way. Use a Thelemic Memory until one section turns Varicose. It has to be your body's own instruction. A sword cut or projected will does not count.
- **Revision**  Use a Thelemic Memory until one section turns Varicose. I need the strain your body makes by carrying its own instruction. Wounds from steel or projected will do not count.
- **Reason**  Retains the excluded sources and removes the prologue.

**`hemomancy.anchorite.refusal.line1`**

- **Proposal**  Still-water has been in you. I can see it in your nails. A scar cut into that body would fight itself, and cruelly.
- **Revision**  Still-water has touched you. A scar cut into that body would fight itself, and cruelly.
- **Reason**  Keeps the physical consequence and drops an unsupported visible symptom.

**`hemomancy.anchorite.d6.referral`**

- **Proposal**  A scar can route an instruction through you. What the instruction says is a Mnemonist's craft, and I would only be guessing. Take my question to one of them.
- **Revision**  I can cut a route for an instruction. Take my question to the Mnemonist who can teach you to write it.
- **Reason**  Clear division of the work without a softener.

### Monolith

**`hemomancy.monolith.lore.what_opens.line1`**

- **Proposal**  The stone splits and falls open, and what I have been holding all this time is let go. You will see what I have been looking out of.
- **Revision**  I stop being a stone. What I hold comes loose.
- **Reason**  Keeps the change of state in two spare clauses.

**`hemomancy.monolith.lore.where_found.line2`**

- **Proposal**  They cut it free and closed me around it, and they wrote down everything they saw. They never managed to keep it asleep, and it stirs whenever a hand like yours is on me.
- **Revision**  They cut it free and closed me around it. They wrote down what they saw. It stayed awake.
- **Reason**  Keeps extraction, enclosure and record without inventing a reactive visitor test.

**`hemomancy.monolith.lore.who_stood_here.line1`**

- **Proposal**  §7[The warmth under your hand does not change.]§r Those who came this far. The stone weighed each of them as it is weighing you now, and it kept every weight.
- **Revision**  §7[The warmth under your hand does not change.]§r The stone weighed everyone who stood here. It still has their weights. Their faces are gone.
- **Reason**  Restores the limit on its memory and keeps the markup.

### Voyager / Wayfarer

**`hemomancy.voyager.item_inquiry.unknown`**

- **Proposal**  I don't know that one, and I won't pretend to. Where did you find it, and how deep?
- **Revision**  Where did you find it, and how deep? I don't know that one.
- **Reason**  A useful field question replaces the disclaimer.

**`hemomancy.voyager.court.known.alchemist`**

- **Proposal**  I gather your Alchemist sent you seaward already. They're right that the Court raises these strains, and I'd guess it cost them something to say so.
- **Revision**  Your Alchemist sent you out here, then. The Order keeps a list of the strains. The Court raises them.
- **Reason**  Restores where the Order's knowledge ends and the Court's work begins.

**`hemomancy.votary_wayfarer.voyage.line2`**

- **Proposal**  I read everything in the archive about ships, and none of it said the floor moves all the time. Somebody should have warned me.
- **Revision**  I read every account of sailing I could find. The deck's been moving under me since I came aboard.
- **Reason**  Lets the student's first encounter with the deck supply the humour.

### Zealot / Acolyte

**`hemomancy.acolyte.corrupted.line2`**

- **Proposal**  Do be careful, though. The old current still comes if you call it, and everyone here who gave in found it harder to get free after.
- **Revision**  If you use a blood manipulation now, you'll have to start the wait for abstinence over. It can be hard to leave it alone at first.
- **Reason**  Names the actual setback and keeps the Acolyte's sympathy.

**`hemomancy.acolyte.tainted.line2`**

- **Proposal**  One of the older women here told me it's like a house after a lodger moves out, much too quiet to begin with, and then one day it's just your house again.
- **Revision**  People tell me that hollow gets easier to sit with. It can take a while.
- **Reason**  Answers the previous line's discomfort without another miniature story.

**`hemomancy.zealot.tainted.line2`**

- **Proposal**  Your Verdigris Aura will come up more readily now. It is the first line your blood has to learn to stay behind.
- **Revision**  Your Verdigris Aura comes up more readily now. You're early in the work. Keep at it.
- **Reason**  Retains the activation change and gives the Zealot a direct assessment.

### Guardian / Scout

**`hemomancy.guardian.item_inquiry.lethean_dew.line1`**

- **Proposal**  Lethean Dew comes out of a Stillwater Condenser. Bottled water goes in, with Ghost Pipe growing nearby. Nobody gathers it off the grass at some holy dawn, whatever the stories say.
- **Revision**  Put bottled water in a Stillwater Condenser with Ghost Pipe growing nearby. That's how we make Lethean Dew.
- **Reason**  Keeps the ingredients and equipment and cuts the sermon about holy dawn.

**`hemomancy.scout.observation_hint.line1`**

- **Proposal**  They hunt. They scratch at the walls, same as outside. §oThey don't see me at all.§r One walked over my boot yesterday and never slowed.
- **Revision**  They hunt for food, push at the walls, circle each other. Same as outside. §oNot a threat. Not prey. Nothing.§r One walked right over my boot.
- **Reason**  Retains the behaviours and her uncertainty while preserving the good concrete addition.

**`hemomancy.scout.what_happened.line1`**

- **Proposal**  I followed them in. The Church sends scouts. We learn what things like these are, and whether there's any reasoning with them. I'd had a day in here with my notes. Then the way out came down. §oI never found another.§r
- **Revision**  I followed them in. The Church wanted to know what they were, what they ate, whether they could be reasoned with. I'd taken notes for a day when the exit came down. §oI never found it again.§r
- **Reason**  Restores the scout's remit and keeps the chronology and italic emphasis.

### Our Lady / Annetta

**`hemomancy.lady.cleansing.v0.line2`**

- **Proposal**  §b§oIt comes out slowly. A river takes a stain out of a stone the same way, so slowly that nobody watching would see it happen. I have time to watch.
- **Revision**  §b§oThe blood is coming out of you slowly. I can see where it has let go.
- **Reason**  Places her patience in her observation and reduces the long simile.

**`hemomancy.lady.cleansing.v1.line1`**

- **Proposal**  §b§oThe blood never felt dangerous while you had it. Nothing that quiet does. People wade into still water every summer thinking the same, and I keep a few of them.
- **Revision**  §b§oThe blood can feel harmless while you carry it. People wade into still water every summer thinking the same. Some go in too far to touch bottom.
- **Reason**  Keeps the danger of apparently quiet things without claiming the Lady takes people.

**`hemomancy.annetta.cured_support.line2`**

- **Proposal**  Stay behind me if you can. I can slow it and keep you breathing. You'll have to finish what the mercy started.
- **Revision**  Stay close. I can slow it and keep you breathing. You'll have to finish it.
- **Reason**  Keeps the support range cue and gives the frightened ally a direct request.

### Fungal Entity

**`hemomancy.communion.v1.schism1`**

- **Proposal**  §4§oThe Schism? We thinned you. Some branches grew too ambitious, so we cut them back, as any grower would.
- **Revision**  §4§oSome branches of the Order grew too far. We cut them back at the Schism. Part of what we cut away kept growing.
- **Reason**  Retains the Entity's claim and sets up the breakaways' agency.

**`hemomancy.communion.v3.line3`**

- **Proposal**  §4§oTo us a blood temple is a greenhouse, with the Hermit's heart planted in it for a seed. What came up out of the heart you took, Archon, was you.
- **Revision**  §4§oOur tissue runs through the temple. You took the Hermit's heart, Archon. Look what it has grown into.
- **Reason**  Moves from greenhouse allegory to infection while keeping the heart and rank.

**`hemomancy.communion.v3.line3.apotheos`**

- **Proposal**  §4§oTo us a blood temple is a greenhouse, with the Hermit's heart planted in it for a seed. What came up out of the heart you took, Apotheos, was you, and now you are in flower.
- **Revision**  §4§oOur tissue runs through the temple. You took the Hermit's heart, Apotheos. We can see what has ripened in you.
- **Reason**  Keeps the post-choice difference and the reproductive perspective.

**`hemomancy.communion.v4.name2`**

- **Proposal**  §4§oYou could never say that aloud with a throat like yours. It is in your blood now, and it will stay there.
- **Revision**  §4§oA throat like yours cannot shape that name. Your blood §4§ohas it§4§o now.
- **Reason**  Restores all markup runs and anchors the emphasis in the transmission.

### Circus

**`hemomancy.circus.ringmaster.line.succession_complete`**

- **Proposal**  There's my successor. The hat suits you better than it suited me, and I'll deny that if you repeat it. The ring is yours to call now.
- **Revision**  You're my successor. The ring is yours to call now.
- **Reason**  The handover can land without the hat joke.

**`hemomancy.circus.ringmaster.line.liberation_complete`**

- **Proposal**  You're the one who cut a carousel down. There's no act left in this ring for you, so watch from the back if you stay.
- **Revision**  You broke the carousel. There is no act left for you here.
- **Reason**  Keeps the finality of the original while naming what the player did.

**`hemomancy.circus.school.circus_strongman.welcome`**

- **Proposal**  Plant a Hulk where the trouble's going to land and it'll stand there till it breaks. I want to see if you'll do the same.
- **Revision**  Plant a Hulk where the trouble's going to land. It'll stand there till it breaks. Tell me who you mean to get out while it holds.
- **Reason**  Gives the teacher a protection criterion instead of testing bravado.

**`hemomancy.circus.school.circus_beast_tamer.welcome`**

- **Proposal**  When a pack loses one of its own it changes shape, and a Hound does the same when it ruptures. Handle the body gently, and once it bursts, let its temporary Curs finish the chase, the way my old dogs finish one.
- **Revision**  Handle the Hound gently until it ruptures. Its Curs only last a little while. Let them finish the chase.
- **Reason**  Keeps the rupture, temporary bodies and handling without prolonging the tutorial with a simile.

### Court A

**`hemomancy.court.arbor.greet.referred`**

- **Proposal**  They told you of us up there, and you came down through all that water to see. Now you are standing in the light with the rest of us.
- **Revision**  You have come a long way down to us. Stand in the light with the many.
- **Reason**  Starts where the Arbor meets the player rather than restating referral.

**`hemomancy.court.linesman.greet.referred`**

- **Proposal**  Who sent you down? Whoever it was, they knew where the camp hangs. There's slack on this line if you want it.
- **Revision**  There's slack on this strand. Take it if you want me to tie you in.
- **Reason**  Makes consent the greeting.

**`hemomancy.court.nursery_tender.join.settled`**

- **Proposal**  You've changed houses once already, haven't you? Then I can't take you. The custom only gives one move, and you've had it.
- **Revision**  I can't take you into the Wake. The Court gives each of us one change of house, and you've had yours. Mind the cups on your way out.
- **Reason**  Keeps the rule and locates the refusal in the nursery.

**`hemomancy.court.braced_elder.shunned.line2`**

- **Proposal**  Go back out the way you came in. Nobody behind me will follow you.
- **Revision**  Back through the passage, guest. I will keep it open until you're clear. Nobody behind me will follow.
- **Reason**  Keeps the promise and makes the person holding the way out visible.

### Court B

**`hemomancy.court.host.greet.referred`**

- **Proposal**  Somebody sent you, then. Keep their name, I don't need it. There's a room for you just past the screen.
- **Revision**  There's a room for you just past the screen. Whoever sent you can keep their name.
- **Reason**  Leads with hospitality and keeps the role's discretion.

**`hemomancy.court.veiled_resident.greet.referred`**

- **Proposal**  Somebody told you where we are. Who was it?
- **Revision**  Who told you where to find us?
- **Reason**  The resident wants one thing and asks for it.

**`hemomancy.court.bellwarden.shunned.line2`**

- **Proposal**  I will not open the lower door for you. Nobody in the bell will come after you.
- **Revision**  The lower door stays shut. Go back by the way you came. No one in the bell will follow.
- **Reason**  Uses the physical boundary and keeps peaceful withdrawal explicit.

**`hemomancy.court.presence.marimo.chorus.5`**

- **Proposal**  Marimo is slow with everything. You'll be fed first, and belong a while after that, and the place in the walls comes last of all, if you ever want it. Are you hungry?
- **Revision**  Marimo takes time. We feed you first. Later you'll belong here, and after that there can be a place in the walls, if you want it.
- **Reason**  Preserves the stages and the opt-in and avoids borrowing the Gardener's question.

### Successors / Recruits

**`hemomancy.succession.personal_identity`**

- **Proposal**  I got the craft from a teacher, all of it at once. Everything else about me I'm still finding out, mostly by accident.
- **Revision**  I have my teacher's craft. The life I'm making with it is mine.
- **Reason**  Keeps the identity distinction without the shared rueful ending.

**`hemomancy.dialogue.recruit.alchemist.accept`**

- **Proposal**  All right. Say yes and my expertise goes into your pool. Every drop I put in there has the Order's craft behind it, so please don't spend it on anything stupid.
- **Revision**  If you accept, I'll add my blood and what I've learned to your pool. I expect you to use it carefully.
- **Reason**  Keeps the timing correction and speaks from professional responsibility.

**`hemomancy.succession.teaching.artificer`**

- **Proposal**  Degree 5. Raise a Vacant Effigy's frame beside a Cardinal Focus on a Greater Working floor. Lay it out: Venous Stone in the center, Hematic Iron on its four edges, Conscious Mass at its four corners. Seat a Sanguine Formation. Armor Stand and Blood Stained Stone go in lit braziers. Feed it 250 mL. Then carry the effigy to a Venous Stone plinth beside the Cardinal Focus on a Greater Dominion floor. It's heavier than it looks.
- **Revision**  At Degree 5, raise a Vacant Effigy's frame beside a Cardinal Focus on a Greater Working floor. Lay it out: Venous Stone in the center, Hematic Iron on its four edges, Conscious Mass at its four corners. Seat a Sanguine Formation. Set an Armor Stand and Blood Stained Stone in lit braziers. Feed it 250 mL. Then carry the effigy to a Venous Stone plinth beside the Cardinal Focus on a Greater Dominion floor.
- **Reason**  Keeps all materials, tiers and steps and drops the weight joke.

**`hemomancy.dialogue.recruit.release.prompt`**

- **Proposal**  Ending the pact takes me out of your bloodline. If that is what you have decided, say it plainly.
- **Revision**  End this pact and I leave your bloodline. Is that what you want?
- **Reason**  The actual NPC prompt can ask for confirmation directly.

## 8. Changes to the brief, shared rules and sheets

### Brief

- Define the unit before drafting. Record speaker, trigger, route and line kind for each candidate. Distinguish a reply, an option label, a prompt shown as NPC speech, a stage direction, a system notice and a subtitle. Resolve shared keys and reviewer ownership at intake. Collect teasers and trade opens as a separate surface with explicit attribution.
- Ask for an old-text diagnosis before showing the Hermit as an example. Give contrasting examples from several encounter types, including one fragment that works, one good instructional contrast and one line to leave alone. Each editor should identify the player action, the speaker's immediate intention and the relevant knowledge limit.
- Classify changes as voice, factual correction, deletion, new colour, new fact, presentation change or instruction handoff. For a handoff record the exact receiving node and any prerequisite to reaching it. Check old tokens and new assertions. Record the source of a correction, including existing errors such as misleading purity language.
- Compare on the merged overlay and also run a cross-speaker check after resolving collisions. Publish denominators and count kinds. A flagged line needs a reason, and a cleared line needs context. Avoid making a low hit rate an acceptance criterion.
- Add an explicit presentation check. For any line that grows by at least 40 characters and 50%, inspect its adjacent lines and response choices at native dialogue scale. Keep exact key, spoken text and role coverage separate in the validation notes.

### Replacement wording for the shared rules

> Give each line a job in its encounter. Start with what the speaker sees, needs, refuses, or can teach at that moment. End when that job is done.
>
> Keep established names and mechanics precise. A contrast is useful when it distinguishes two actions or records a real disagreement. An order can be short when timing or danger calls for it. Repeated words can show a habit, a ritual or a shared rule. Read repeated lines together and decide which repetitions the player needs to hear.
>
> A hint should give the player a next action. It may refer to a teacher or ledger if the instructions are available at that point and the player can find them. Preserve the warning or exception that changes what the player does. Use the game's own term when it helps the player locate the action.
>
> Give literal agency to living or acting things where the lore supports it. If someone attributes judgment to a tool, decide what they mean and what they could know. A question can demand an answer, test a pupil, or expose a character's priorities. Reconsider questions that merely tee up a punchline.
>
> Distinguish a repeated encounter by what each speaker protects, wants or allows. State shared rules plainly. Keep ritual formulas and tested phrases where their repetition is deliberate. Read in context before marking the line ready.

This replaces the audit's universal bans on contrast, rhetorical questions, one-word orders, third-person self-reference and personification. The Lady can describe worship from her own side. A fifth-Degree Monolith can say “the stone” because the move to “I” is part of its progression. The Fungal Entity can ask a question to gauge a host or stage a revelation. Repetition such as Annetta's “Still my hands” can be integral to a specific scene. Review where it does work. Keep the current formatting and tested-text rules, with the explicit distinction between preserving semantic emphasis and approving a presentation change.

### Sheet format

Use **what they are trying to do**, **what they know and at which stage**, **what they attend to under pressure**, **how they teach or refuse**, and **two encounter contrasts**. Include one keep and one cut, with the situation and the reason, plus a short pin and jurisdiction list. Move measured rhythm statistics to a diagnostic appendix. Let ritual formulas and gameplay terminology have their own list. Drop reusable banks of gestures, furnishings and preferred words.

For example, the Hermit sheet's candles, chair, bread, worn threshold and “I can hear it in you” have already become a recipe. Lead with his condition and intention. He is suspended near a heart that can wake another person. He needs the visitor to decide, claim it and return for directions. At higher Degrees his knowledge has a limit. The Mnemonist's sheets, desk, glowing ink, jars and shelf locations are an equivalent list with a different skin. Give them a professional limit instead. They can tell a pattern is present and how it was indexed, but the Alchemist can tell them what happened in the sample. A scene about an uncertain memory can show that distinction with no prop at all. For the Court, describe what a role physically controls and what it owes the visitor under custom. The same one-move rule can then be delivered consistently even when two voices do sound similar.

## 9. Scope and unverified items

- Read all 48 Hermit pairs and sampled every supplied group. For the table I separated the component roles where the material supports it. I supplied three to five alternatives per supplied proposal pack, concentrating on the highest-value lines. I did not exhaustively trace every changed key to every display path or run a full mechanical audit of every item inquiry.
- The Hermit's 48 proposed values match the working language file, and all old snapshots checked match HEAD. The other groups' old snapshots match the working language file. The alternative text is offered for selection, with a few deliberate restorations of existing lines.
- The name-masking exercise used my own editorial judgment. It had no independent readers. I did not measure pixel widths, hear lines in play, verify delivery in multiplayer, or run Gradle, GameTests or the game. I did not validate the audit's follow-up coverage claims for Saints, teaser and trade-line totals, or the Antecedent recordings.
- The source supports Annetta's purge broadcast and particles. The presentation of the emergence and the implication of “behind me” need a native read. I did not establish that a player in every Mnemonist Chamber branch has already seen the room. The Lady's willingness to harm, and who canonically initiated each phase of the Schism, are narrative decisions beyond this prose check.
