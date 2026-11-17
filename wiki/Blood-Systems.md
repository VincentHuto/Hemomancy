# Blood Systems

Manipulations use [school and rank casting animations](Casting-Animations.md), with distinct first- and third-person motion, blood anatomy and a local development preview. These gestures leave casting costs, targeting and movement unchanged.

Use a [Hematic Microscope](Hematic-Microscope.md) in your main hand with a filled Blood Vial in your off hand. Hold use for two seconds to identify that vial and reveal its traits and injection prediction. Unknown samples remain injectable; examination does not increase potency.

## Learning the core loop

Everything the 2026-11 core loop added is taught in the Liber Sanguinium: The Two Verbs, Living Blood, The Body and
the Day, The Draw and the Mark, The Ground Network, Fed Ground and Chum open at the first Degree; Temper, Unread
Humours and The World Answers at the second; The Menagerie, The Brood, Strain Beds and Living Veins and Basins at the third. A memory's page
opens when you pick the memory up, and the Staff, vat, instruments, feeder, gourd seeds and cleats open theirs the
same way, and so does learning the memory. Twenty-seven advancements mark the firsts (Sculpted, Set Through, Meat Is
Blood, Carried, Kept, Of Your Own Keeping, A Bed of Strains, Cut and Lit, Whipcrack, Clinging, The First Ring, The First
Drop, Fed and Reaped, Something Else Came Up, Something Bit, In Glass, Studied, The Ground Rose, A Full Night, Brought
On, What the Dead Give, Both Hands, Along the Red, Bound and Branded, Snuffed, Carried by the Vein, Stoked), and each
opens the page about what you just did.

The older pages are readable too (round two P9). The manipulation overview, Blood Temperament and the Sanguine
Condenser open at the first Degree; each school's page in the Manipulations and Temperament chapters, Thelemic
memories, the morphling pages and shared mob materials at the second; memory weaving, the Resonant Forge and the
bloodwork stations at the third; Cradle and Nectar at the fifth, Annetta Knowles at the sixth and Canon Memories at the
seventh. A school's enzyme, or learning one of its memories, opens its pages sooner. Each school page lists every
memory of the school with its type, what it does on R, and what it does to a held verb. Liber Immaculatus's pages open
by purity and clarity stage.

Since 2026-11-16 the book also has a Hematic Armature chapter (the rack from the second Degree, and a lesson for each of
its two rites), a page for every Working craft and for the Dried Gourd, Hematic Stake and Mycelial Synapse, each opening
at its craft's Degree, and pages for Vesper, the Mycophant and Naeglerophaeon. A Brined Court chapter opens when you
learn of the Court. A boss's page opens when its fight begins.

The outpost Harbingers teach the same systems aloud (round two P10). Each lesson is a lore card in a speaker's
Conversation box, and asking any question in it opens its Liber pages: the Hermit (the two verbs, the land's answer;
while your blood is awake and you are at the fourth Degree or below), the Vicar (the verbs, the day, the world's count
and chum, keeping creatures), the Alchemist (living blood, fed ground, Temper, reading a creature, strain beds), the
Mnemonist (Sculptings, strands, the ground memories), the Artificer (hot gouge runs, the Staff, the strand skills,
climbing) and the Vein-Mason from the fourth Degree (ground marks, carried crossings, walls and scars).

## Injectable blood samples

Hold use with a loose filled Blood Vial for 0.8 seconds to inject it. Most responses last 10 seconds; Transfusion Saturation prevents another injection for 20 seconds. Survival use returns the empty vial. Creative use retains the sample and still applies saturation. Releasing early consumes nothing.

Blood can be injected before identification. Unknown samples show their source, but hide their exact responses. The shared identification support is ready for the companion Hematic Microscope; that block is a separate implementation. Identified tooltips show the selected benefits and drawbacks. Up to three benefits apply, with duplicate effects counted once and Lux's reveal counting separately.

This is an optional use for a field sample. It does not refill blood or permanently change tempers. The rack carries samples, and centrifuging them still produces enzymes. Missing-mod and malformed samples stay filled and cannot be overwritten. See the [implementation and balance notes](Injectable-Blood-Vials.md) for all mappings.

Lux manipulations form flowing white light, including a luminous Beacon, thin beams and the Unclosing Eye. White Verdict leaves a slightly thicker beam that fades over two seconds after release; the lingering light causes no additional hits. Tenebris forms black liquid that stretches into smoke. Umbral Step's smoke surrounds its departure and arrival positions. Charged manipulations keep their buildup visible through the gauge's full charge duration. Their costs, targeting, damage and gameplay durations are unchanged. [Visual implementation and verification](../docs/archive/done/manipulation_work_plan/08-lux-umbra-flow.md).

Hematic Flare requires a visible permitted target before charging blood; Funeral Bell respects owned allies and disabled PvP for damage and statuses. [Manipulation acceptance results and remaining checks](../docs/archive/done/manipulation_work_plan/06-acceptance-results.md).

Memory and progression tooltips distinguish **base cast**, **upkeep per second**, **reactive trigger** and **Living Staff switch** costs. A staff form's registry-zero cost does not make switching free: the base is 250 mL, reduced by Weapons Master to a minimum of 50 mL. Weapon attacks have their own costs, shown on weapon tooltips. Restoration is free, and loaded crossbow ammunition is retained through restoration and re-forming, including a save/reload.

Family mastery is shared at 10, 35, 85 and 185 cumulative credits. Absorb each chosen form's memory after reaching its threshold; intermediate forms are optional. Sustained channels earn a credit after five seconds, rather than on every one-second upkeep pulse. Learning degree and casting alignment are separate gates.

Use this page as the mechanics reference: blood volume, manipulations, tempers, vascular state, skills, and routing.

---

## Living blood

Blood is only fuel while something alive holds it: your body, a worn gourd, a Gourdvine Tap. Blood in a machine or a
Bloody Flask stays fresh for about five minutes after it was last fed, then begins to clot (a tenth of what is left each
minute; nothing is fresh an hour later). Clot is not fuel and fills the tank. Hold Blood Absorption on the machine: the
fresh blood comes out first, then the clot comes away as Sanguine Formations (one for every 400 mL, far fewer than a
Sanguine Condenser gives). Drinking an old flask gives its fresh share as blood and its clot as Formations. A flask
starts its clock when it is made: bottled, looted, given, or poured from a machine's input slot. Distilled at the
Alembic, a flask gives vials only for its fresh share. Drink Blood Rocks, flasks or jugs in quick succession and each
gives less: all of it, then 80, 60, 40 and 25%, until three minutes pass without a drink.

No recipe makes a Sanguine Formation. Every Formation is blood that set: in a Condenser, from the Formation key, as a
neglected tank's clot or an old flask's, and now and then from a bleeding creature. A gourd works only in the Scarlet
Vanity's gourd slot: carried in the pack or held, it keeps its blood but does not grow, feed you, siphon kills or feed a
suture.

- **Fill and fire.** Fill a machine by projection and use it inside the window. A top-up does not make old blood new:
  the tank's age blends by volume, so a little fresh blood barely changes it.
- **Wake the clot.** While a tank holds clot, half of what you project in goes to the clot and is used up, and each mL
  spent wakes 2 mL of clot back to fresh blood; the rest goes in fresh. A tank with no room left spends the whole pour
  on its clot. Flasks, gourds, gutters and Blood Thralls only add fresh blood where there is room, and a Thrall's blood
  is as old as the tank it came from. A station held by an upgrade rite keeps its blood as old as it went in.
- **Suture it (Degree 3).** A sutured machine drinks from you while you are within 16 blocks. Its working buffer clots
  like any tank, but the suture wakes the clot from your blood at 4 mL of clot for each mL it draws, so an idle 600 mL
  buffer costs you about 15 mL a minute. At Degree 5 the Fane lets it draw from anywhere in your Fane, and from your bloodline's pool. If you leave,
  log out, die or are below Degree 3, it stalls and clots. While you are offline, a member of your bloodline within 16
  blocks of it, at Degree 3 or past it with their blood awake, stands in and feeds it from their own blood.
- **Living vessels** (the Gourdvine Tap) never clot.
- **Ownership.** Only the player who placed a machine and their bloodline may draw from it, unless the server allows
  anyone (`foreignReservoirDraw`).
- **Read it.** Station blood bars show fresh blood over a dark band of clot, and the blood dulls as it ages. Use the
  Vascular Status Gauge on a machine to read its tank and whether its suture is feeding it. A stalled machine shows a
  small broken stitch on its own blood gauge; hover over the gauge to see why it stalled.
- **Machines do not make blood.** The Centrifuge and the Alembic no longer add blood to their tanks.

## The ground network

Blood marks the world in three tiers.

- **Stains.** Blood you project that nothing takes lands on the face you aimed at as a stain (4 mL). Stains dry from wet
  to tacky to a brown crust and wear away; rain or water washes them. Absorption takes part of a wet stain back and
  cleans an old one. Creatures with blood sometimes leave one where they die or bleed hard.
- **Gouges.** With Vascular Dowsing selected, sweep Projection along a surface to cut a gouge into each face. It stays
  until Absorption fills it in, or until water runs into its cell or is poured onto it and washes it out. Rain does not. Project into any part of the run (15 mL) and a pulse lights every connected cell, up to
  64. Hold a catalyst in your off hand as you pulse to give the run a use: glowstone or Lux catalysts light it for real,
  coal or Flammeus catalysts make it hot (it smelts what lies on it and burns up to four furnaces, smokers or blast
  furnaces beside it that have something to cook, nearest your pulse first), a compass makes its far end flare, packed ice or
  Congeatio catalysts make it cold, iron or Ferric catalysts pull dropped items along it, bone meal or Animus catalysts
  feed the crops beside it (one stage every 30 seconds), ink or Tenebris catalysts make it dark. A Catalyst Feeder beside a lit run keeps it lit
  while stocked, for 15 mL of your blood each catalyst, and only while you are online and alive in that world; it
  never lights a dark run.
- **Living veins (Degree 3).** Set a Vein Node at a run's end and project into it to grow tissue along the gouge (16
  cells at Degree 3, more each Degree). Between two nodes a vein carries a little blood toward a tank that is drawing
  (something took blood from it in the last 5 seconds, or it is a machine under 600 mL; a basin, Tap or vat never
  pulls just for being low), while you are within 32 blocks, for 1 mL a second while blood moves. Water runs over a
  vein without breaking it; away, it stops, and after a day untended it withers back to gouge. A living
  run that joins two of your Earthen Veins carries you between them for 125 mL, however far apart.
- **Basins and gutters.** A Venous Basin you place holds 2,000 mL. Put a Blood Gutter under its spout: while the blood
  is fresh it runs down the gutters to the machines and farmland they reach, then the bowl clots and the stream stops.
  Scrape the clot with Absorption for Formations. A Saint's hall gutters are not yours to fill.
- **Light without torches.** Hematic Beacon's Sculpting spreads fading lines of light over a surface; Blood Eclipse's
  draws the light out of candles, campfires and lit runs and hangs it there as a field of dark (creatures inside lose
  their sight, and while you stand in your own field you are hidden from anything more than four blocks off; it hides no one else); Sanguine Magnetism's draws
  dropped items to you.

## Farming and water

Blood grows things, and baits water.

- **Fed ground.** A crop on ground blood reaches grows faster. A scab spike hanging under a ceiling drips when fresh
  blood stands just above it (a basin, a machine's tank or a wet gutter on the block it hangs from) and its placer may
  draw that blood; a spike nobody placed drips only from tanks nobody owns; each drop feeds the
  soil below and round it for about five minutes. A wet stain beside a crop's soil feeds it too, briefly.
- **Gourds need blood.** The gourd vine grows only on fed ground, and bone meal will not wake it on dry soil.
- **Planting Basin.** A bed of soil over a small tank. Fill it by hand or run a worked gutter past it; while the blood
  is fresh the plant in it grows much faster. It clots like any tank; scrape the clot with Absorption.
- **The cloche.** Put seeds instead of spores in a Mycelial Lantern and the crop grows under its glass bell, a stage
  every 30 seconds for 25 mL, and is harvested into the output. Spores still make enzymes.
- **Vital Effusion.** With it selected, hold Projection on a plant to soak its soil and grow it as bone meal would.
- **Chum.** Project into water with nothing selected to pour a pool of blood (up to 12 blocks in radius). Fishing inside it
  is faster and sometimes brings up pelagic herring, coral or leeches, far more often near the reef and the pelagic
  seas. An off-hand catalyst colours the pool (Lux brings chromatophores). A strong pool in deep water draws herring.
  At night, or where much blood has been spilled, a strong pool also draws one hunter: a Brined Votary in the reef and
  the pelagic seas, a Bog Revenant out of a swamp, a drowned anywhere else.
- **Strain beds.** Plant a spore on its own wood and it grows as a strain: Vivacious, Fervent and Ruinous on Ravenous
  Heartwood; Neurotic, Frigid and Ferric on Salt-Stained Timber; Incandescent and Umbral on podzol or mycelium. A strain
  grows only where blood reaches it (a drip slowly, a planting basin beside its wood properly) and, cut ripe, gives back
  its spore and often a second. Salt-Stained Timber is crafted from eight logs and a Salt-Stained Voyager Log, then from
  one timber, a dried kelp and four logs.
- **Catalyst crops.** Glowroot (carrot and glowstone dust) and emberpod (nether wart and blaze powder) grow only on fed
  ground and are their own seed; each is a weak catalyst, Lux and Flammeus.
- **Tainted Harvest.** A ripe crop on over-fed ground (a fresh planting basin or a feeding run) sometimes drops a Tainted
  Harvest: blood to a Harbinger, sickness to anyone else, and feed for a gouge run.
- **Floor spikes.** Drops striking bare scab or stone now and then raise a scab spike from the floor, which grows toward
  the spike above but always stops short of it.

## The menagerie

A creature's blood is worth more alive. Throw the Tendon Line into a creature: if it is passive,
or worn down to a third of its health, the line roots in it and follows you home, placid and never despawning, for a little
blood each second (a quarter of its maximum health in mL). Let the upkeep lapse and it breaks free, angry if it was
hostile. You may lead one creature at the Third Degree, two at the Fifth, three at the Seventh. Bosses, people, Wills,
Saints, the Warden and anything owned cannot be kept.

Lead it to a **Specimen Vat** (or a small one to a Specimen Jar) and use the vessel with an empty hand to put it in. A
kept creature lives on **Vigor**: it falls about a point a minute, and the vessel's 800 mL living buffer (200 in a jar),
fed by Projection or a suture, feeds it back. Only the vessel's owner and their bloodline may take out a husk, lead a
creature in or pick the vessel up. Hold Absorption on the glass to draw Temper in its schools and a little
blood; the more Vigor, the more each draw gives, and acclimation to a kind cuts vats of it as it cuts wild creatures.
Starved to zero, it leaves a husk. A strong hostile creature low on Vigor may break out. A Sanguine Vigil with a
catalyst in it keeps nearby kept creatures from fading while you are close.

Use a kept creature's vessel with an empty hand and its screen opens: the creature in its glass, its Vigor, the buffer,
what its blood is as far as you have read it, and its brood. Once you have studied a kind (three individuals examined)
the screen gives what a draw yields and the kind's habits. A thick-hided kind (a blaze, a chiton) refuses a vial in
glass as in the wild; the Living Syringe draws any kept creature for 3 Vigor, and only its keeper or their bloodline may
use it.

**The brood.** Two kept creatures of one kind in vessels side by side, with one keeper, may breed. Press "Let them
breed" on either screen. While both hold 75 Vigor or more, the brood takes blood from their buffers each second, 30 mL
per point of a parent's health (at least 150; a cow 300) over two minutes of feeding, and a point of Vigor from each
parent every ten seconds. Then the young, a baby where its kind has one, goes into an empty vessel beside either parent;
with none, it waits. A living thread of blood arcs between the two lids while they breed. Part them, break or lift a
vessel, or let a parent die, and what was paid is lost. The parents lose 20 Vigor at the birth and rest five minutes.
The dead do not breed, nor does the Morphling Polyp. Nothing is ever dropped or set loose.

The **Living Bestiary** keeps a page for every kind you have sampled, kept, jarred, bred or seen through a vessel's
screen, under Met Creatures: its humours are unread marks until you examine a sample, and its yield and habits stay
hidden until you have studied it. Until then a draw from that kind gives you half its Temper.

Plain Projection into a passive animal doses it: it goes into season at once, but it is tainted. Its meat comes off
tainted (blood for a Harbinger, sickness for anyone else), each further dose risks it going feral for a minute, and
its young may carry your strongest school.

## Strands

Some combat memories turn held Projection into a weapon you swing. With one selected, hold Projection at open air, a
creature or bare ground and blood stays out of your hand as a **strand** until you let go or run short of blood. How you
move it decides what it does.

- **Lash** (Searing Lash, Gloam Laceration, and Blood Lash, a weak Animus lash with nothing to cast): click with one
  selected and it strikes once, straight ahead. Hold Projection and it gathers in your hand, stronger the longer you
  wait; flick the mouse and it is thrown along that stroke, cracking on the first thing it meets and laying the school's
  state for a long while. Then it follows your hand while your view turns only a little. It hurts only while it moves, a
  stroke tires after a quarter turn, and a lash left still runs back into the hand. Click while holding to send it down
  the crosshair. Each lash is its own weapon: Blood Lash is the tether (a gathered crack holds its target, landed sweeps give blood back, slow to fade), Searing Lash the heavy whip (long and lagging, a gathered crack bursts wide and ignites, cracks in a row build heat), Gloam Laceration the razor (short, three-tailed, three cuts a blow, deeper from ambush, gone when the hand stops).

Other memories do something of their own under held Projection. They are not strands and share no shape:

- **Blood Eclipse**: a dome of dark opens round you, growing from a radius of two blocks to eight as you hold and moving with you.
  What you may harm inside is Obscured and what stands in it is hidden from afar. Click to end it in a flash.
- **Glacial Grasp**: a hand of ice forms round one creature over a second and holds it where it stands, driving Rime
  in. Click to squeeze; the longer it held, the harder it closes. Bosses are only slowed. On water it still freezes a path.
- **Cruor Javelin**: hold to draw a javelin through three stages, let go to throw it. It breaks on a body and sticks
  in a block for ten seconds; Absorption on your own stuck javelin gives half its blood back.
- **Ferric Spikes**: a run of blades travels through the ground toward wherever you look. Steer it with your aim; click
  to stand it up where it is as blades that block the way.
- **Hematic Flare**: a searchlight. The beam Illuminates and outlines what it passes over; held on one body it builds
  heat, and a click spends the heat as a flash that wounds and blinds.
- **Synaptic Jolt**: held on water, iron, copper or your own Ferric constructs it keeps the whole connected run live;
  held on a creature it jolts it each pulse.
- **Blood Needle Lance**: barbed needles gather round you, up to six. Let go to loose them all; click to loose one.
- **Crimson Flame Conjuration**: flame is painted on the ground where you look and burns five seconds. Click to flare
  the whole line. It still kindles unlit lights first.

Click while a lash is out to **snap** it: it cracks along your aim. A snap costs extra blood; a reversal crack is free.
A lash slows you a little and never takes blood a machine, a rite or a Will could take. R still casts each of these
memories the old way (Blood Lash has no R cast).

**Absorption reads the mark.** Draining now names you: the creature turns on you and its death is yours, though a kill
by draw pays no kill blood or Temperament. The draw changes with the school state on the creature: Pressure pulls it to
you and gives more back, Searing draws harder but boils some off, Rime holds it still, Lodestone strips its armour,
Necrosis spreads when the draw kills, Disrupted arcs to the next marked body, Illuminated can be drawn from further, and
Obscured loses its quarry and veils you. Your own brand is drawn first, from twice your reach.

**No held form.** Blood Needle Fan, Prismatic Reproof, Hemorrhage, Conductive Mark, Insatiable Hunger and Grave Debt
sculpt no held Projection since 2026-11-12. They cast on R only. A Blood Binding that holds a creature still brands it.

**What waits.** Since 2026-11-12 only the three lashes are strands. No memory puts out a jet, a spray, a plant or a
brand. Those kits and the wet-stain run stay in the code for later, while blood on the ground is explored.

**Training the strands.** The Living Staff branch has a strand line. Long Strand lengthens a lash and reaches every
held memory further. Taut Line tightens a lash's tip. Free Hand gives back your pace while a lash or a held memory is
out. Whipcrack makes a lash's crack and a held memory's click hit harder and last longer. Deep Brand keeps a Blood
Binding's brand longer (45, then 60 seconds). Second Strand, at Degree 6, pours a trailing second lash from your off
hand when it is empty or wears a cell hand, at half damage, for half again the blood.

## Living Land and Venous Cling

**Living Land** (Animus, with a Ferric edge) has nothing to cast. Hold Projection on a floor, a wall or a ceiling and
that patch of ground wakes. After a second it starts tearing short columns of its own stone out of the surface and
driving them into the nearest enemy in front of it: teeth from a floor, lances from a wall, a fall from a ceiling. Each
column goes back into its socket after it lands. With nothing near, the patch rises a block at a time into a step, a
ledge or a hanging spur. Let go and everything settles where it came from; no block is ever gained or lost, even if the
column is lost, the chunk unloads or the server stops. It will not wake ground under a Saint's ward, in a rite space or
a Saint's chamber, or anything that holds contents, and it tells you why before it takes any blood.

**Venous Cling** (Animus) has nothing to cast. Hold Projection on a wall or the underside of a ceiling you are touching
and veins run from your hands and feet into the stone: you stop falling. Walk to crawl. On a wall, toward it climbs and
away from it descends; under a ceiling you shuffle along it. Your free hand can still cast. It costs a little blood
each second you hang, and it lets go when you jump, crawl down to the ground, the stone gives way or your blood runs
short. **Hematic Cleats** are bone spurs on a hematic iron sole, made from scrap, bone and a Sinew Cord. You do not
wear them: put them in a Hematic Armature bowl and stand in it wearing hematic iron boots, or any boots the Armature
makes from them, and it sets the cleats into those boots for 250 blood. Fitted and on, they halve that upkeep and
carry you across a wall near walking pace. A slice on the radial's inner ring turns them off and on. They stay through
repair and Armature upgrades, and do nothing without the roots.

## The body and the world's answer

**Food and sleep.** Your blood comes back about a millilitre a second, at the pace your body sets. Hungry (under three
shanks) it makes half as much; starving, none. With a full food bar and a hearty meal still in you it makes half as much
again. Meat gives blood straight back when you eat it: a few millilitres for each point of nutrition, more cooked than
raw, 48 for a steak. The Order's own meats give more and carry a little temper (venison and gourd stew Animus, herring
Ductilis); tainted meat gives the most of any meat for each point (10 mL a point, 30 an item). A full night's sleep leaves you **Well Rested** for five minutes, and your blood
comes back by the base rate again on top of what food gives. Casting never makes you hungry.

**The world keeps count.** Blood you leave on the ground, stains, chum and what pours out of the things you kill, is
counted for the land around it (about four chunks square) and fades by half every two days. Blood put to work does not
count. Kills alone lift the count only to uneasy (600 mL); past that, only spilled blood raises it. Take a stain back up with Absorption and it comes off the count. The count brings the **Blood Moon** more often
(it keeps to the full moon and never comes at the new moon), draws **Wills** to where the work was messy, sends
**vampire bats** down to drink wet stains at night or underground, and draws a **hunter** to a strong pool of chum (a
Brined Votary in the Court's waters, a Bog Revenant in a swamp, the drowned elsewhere).
Where it stays heavy, grass under old stains turns to **Stained Sod** until the land quiets. Read it on a **Humoral
Barometer** (its needle runs from still to seething) and an **Ossuary Clock** (its lunette goes half red when tonight's
moon may turn, and it counts the nights to the full moon). Hang **Suspended Vivianite** to halve what is counted near it
and make it fade faster, up to three in one place. Server owners can turn the scavengers and the stained sod off.

## Blood Volume

**Blood Volume** is the resource every blood-magic action spends.

The HUD vessel shows current blood relative to maximum, inside the frame for your degree. Its liquid and effects follow the vessel artwork's interior; degree and Pome progress retain their existing colour changes, and Apotheos retains its animated helix. A thin living binding of iron lattice and red tendril grows around whatever the HUD is showing, and its red fills in with your degree just as the frame's does. That red beats slowly, darkening on each beat, a little faster and fainter as your blood runs low. The equipped gourd, horn or rib is grafted onto the vessel's inner side, and an equipped Morphling rests in a jar above it; while the jar shows, the Morphling's effect icon leaves the top-right effect row, though your inventory still lists it. In the client config, `powergen.showBloodVolumeHud`, `showGourdHud` and `showTemperHud` independently control the three displays; each defaults to on. Hiding a display does not change its mechanics or unlock requirements. The entire HUD stays in your selected corner, mirrors itself in the right-hand corners and fits smaller viewports as one piece.

### Key Stats
- **Starting Volume:** 0 mL. Waking the blood at the Mortal Display makes it active but fills nothing; the Vicar's initiation hands over four Bloody Flasks
- **Human Maximum:** ~5000 mL (natural)
- **Extended Maximum:** `5000 + Capacity + Spleen + Eternal Covenant - scars`
- **Average Adult:** 5000 mL

### Maximum Blood Diagnostics

Player maximum blood is resolved server-side by the max-blood ledger. Capacity, Spleen, and Eternal Covenant stack additively, then scar penalties subtract from the final bonus total. The Scrying Podium **Blood** tab shows the computed maximum, net modifier, and exact positive/negative modifier totals; hovering `Mods` lists the source of each modifier.

### Regeneration
- **Base Rate:** about 2 mL a second, set by the body: none starving, half hungry, half again well fed, and the base once more on top while Well Rested
- **Meals:** meat gives blood on eating (see "The body and the world's answer")
- **Enhanced by:**
  - Animus-aligned manipulations
  - Blood Lust armor
  - Founding Fane buffs
  - Charm of Vascularium through Hemomancy's current equipment surfaces
  - Certain foods and potions
- **Reduced by:**
  - Combat damage
  - Overuse of manipulations
  - Certain status effects
  - Environmental hazards

### Blood Flow Diagnostics

The **Scrying Podium** shows server-truth blood flow in `mL/t` rather than estimating regeneration from circulation bandwidth. Its **Blood** tab displays Blood Volume, Vascular Health, positive flow, negative flow, net flow, circulation bandwidth used/cap/available, and source rows with applied/requested rates. Hovering the Blood Flow modifier area opens the full active source breakdown. Every blood payment is listed too: one-off costs such as a cast, a Living Weapon strike or a rite offering appear briefly as a negative row, and upkeep costs stay listed while they recur.

Living weapons and scars respect Sanguine Reserve. A Living Axe, Blade, Crossbow or Tool charges only active blood, and only in full: with dormant blood it is free, and when you cannot cover its price it recoils to the Staff or crumbles without taking anything. Scar upkeep stops at the reserve floor, and a worn scar's passive effect returns within a second after milk.

Player/body sources such as base regeneration, **Sanguine Surge**, **Last Wind**, and **Mnemonic Candle Aura** stack directly. External passive income such as armor set regeneration, Sanguine Siphon, fungal scar income, Qliphoth Bloom, Morphling Cradle feed, and Open Blood Gourd transfer shares the circulation bandwidth cap where applicable. Active costs, kill rewards, weapon hits, manual draws, and structure feeding are not treated as passive `mL/t` flow.

### Death and Volume
- Configurable: volume may reset on death
- Can be set to partial loss or full reset
- Death changes how much blood you have available.

---

## Blood Manipulations

**Manipulations** are the abilities you trigger, charge, toggle, or keep active with blood.

Manipulation effects share material and movement within each temper: circulating blood, drained residue, nerve impulses, blood-derived iron, heated glass, arrested frost, luminous fibers and inward shadow. Readiness counts and field boundaries continue to follow gameplay state. Brief blood-cell and school-colored glow accents accompany successful activation and hits, with sparse motes around sustained forms. Reducing particles removes decoration while retaining the main spell subjects. [Visual pass and evidence](../docs/archive/done/manipulation_work_plan/07-visceral-visuals.md).

Flammeus now uses red-orange flame, thin smoke, boiling blood and tumbling sanguine glass. Congeatio forms dark frozen blood beneath pale frost, with powder, heavy cruor fragments and cracks that follow affected bodies and surfaces. These effects retain the existing spell timing, range and combat behavior. [Fire and Cold materials](Flammeus-Congeatio-Visuals.md).

### Manipulation Types

**QUICK**
- Instant cast
- Single action
- Short cooldown
- Example: Blood Bolt (projectile attack)

**CHARGED**
- Hold to charge
- More powerful when fully charged
- Longer cooldown
- Example: Crimson Lance (charged spear throw)

**PASSIVE**
- Always active once learned
- Constant low cost or free
- Background benefits
- Example: Sanguine Regeneration (constant healing)

**CONTINUOUS**
- Toggle on/off
- Drains blood while active
- Sustained effects
- Example: Blood Barrier (active shield)

### Manipulation Ranks

Manipulations are organized into four power tiers:

**HUMILIS** (Humble)
- Degree 1 (Neophyte) access
- Low blood cost (100-500 mL)
- Basic effects
- Foundation of blood magic

**MEDIOCRITAS** (Moderate)
- Degree 2 (Illuminatus) access
- Medium blood cost (500-1500 mL)
- Enhanced effects
- Specialized applications

**SUMMA** (Superior)
- Degree 3 (Initiate) access
- High blood cost (1500-3500 mL)
- Powerful effects
- Tactical importance

**PERFECTUS** (Perfect)
- Degree 4 (Adept) and higher
- Very high blood cost (3500-7000 mL)
- Elite effects
- Game-changing abilities

### Learning Manipulations

**Method 1: Somatic Loom (Memory Weaving)**
1. Craft **Blank Hematic Memory**
2. Gather temper-specific **Enzyme** (Vivacious, Fervent, Neurotic, etc.)
3. Collect **Catalyst** materials for the manipulation
4. Place items in **Somatic Loom** following pattern
5. Project blood into the Loom
6. Wrestle colored memory-orbs into shape
7. Receive completed **Hematic Memory** item
8. Use Memory to permanently learn manipulation

**Method 2: Field Notes Discovery**
- Some manipulations can be discovered through gameplay
- Experimentation with blood tempers
- NPC teachings
- Rare drops from infected creatures

**Method 3: Bloodline Teaching**
- High-degree Harbingers can teach lower-degree members
- Requires ritual at Cardinal Altar
- Costs blood from both parties

### Using Manipulations

**Selection:**
- Press `V` (default) to open manipulation wheel
- Move mouse to desired manipulation
- Release to select

The selected manipulation has a thin, continuous animated vein border with tapered branches around its wheel slice. A red slice fill indicates cooldown, with the remaining time shown on hover. A selected manipulation that is cooling down displays both the border and the red fill.

Conjuring Blood Absorption, Blood Projection, or Living Staff does not produce the red formation strands or gold glow. Blood-transfer visuals still appear when using Absorption or Projection.

**The two verbs:**
- Blood Projection and Blood Absorption are verbs, not memories: they never take an equip slot or the cycle key, but both sit on the wheel's inner ring, and picking one there sets your verb without changing the memory you have selected. Press the verb key (`Y` default) with an empty hand to conjure the hand of your active verb; press it again to turn Projection to Absorption and back; sneak and press it to let the hand go (with the usual refund).
- The Living Staff channels whichever verb is set; the verb key switches it. A quick tap of the Staff that moves no blood calls your Primal Morphling.
- Reach is 5.5 blocks bare and the Staff's focus range on the Staff; Sanguine Reach extends both.
- While you channel a verb, a small label beside the crosshair names it. A memory that can sculpt a verb adds its icon, name and price, and four ticks round the crosshair brighten when it takes what you aim at. When it cannot work there (ground you may not build on, a furnace bank with nothing to cook, too many iron walls, no blood), the ticks are crossed in pale bone and the label reads Refused; a refusal for the place costs nothing. A rite, a Saint or a faltering Will always takes the blood first.

**Sculptings (what a selected memory makes of a verb):**
- **Lignum Mortis** (and Canopy Mortis, Worked Lignum): hold Projection on a log or mushroom stem. Rot crawls through the wood while blood flows; let go and it falls. A tree the rot has wholly reached falls at once.
- **Pyretic Forge**: hold Projection on items lying on the ground to smelt them where they lie, or on sand, cobblestone, stone, clay, a wet sponge or a raw ore block to fire it in place.
- **Tendon Line** (a memory now, not an item): hold Projection on a solid face to throw the line and root it; hold Absorption to haul yourself along it. It snaps if you stray too far.
- **Blood Binding** (and its lingering, chain and lattice forms): hold Projection on a creature. It cannot move while blood flows and only crawls for a moment after.
- **Sanguine Mending**: hold Projection on a dropped tool or piece of armour to mend it a little each pulse, on an injured iron or snow golem to heal it (unless it is fighting you), or on a worn anvil to beat it back a stage.
- **Crimson Flame Conjuration**: hold Projection near unlit campfires and candles to kindle them, two a pulse. Hold it on TNT to light it.
- **Furnace Veins**: hold Projection on a furnace, smoker or blast furnace and your blood burns as its fuel, and as the fuel of every furnace beside it with something to cook, at 6 mL a pulse for each furnace fed. The fires die when you let go.
- **Glacial Grasp**: hold Projection on open water and it freezes into a path that melts back after ten seconds; on a creature it forms the hand of ice (see Strands). Hold Absorption on Frozen Cruor or ice to thaw it.
- **Glacial Rampart** and **Ferric Rampart**: hold Projection and sweep along the ground to raise an ice wall, or an iron wall a block at a time, wherever the stream passes. Neither closes over anyone standing in its path. Twelve blocks of iron wall stand at most.
- **Carrion Communion**: hold Absorption toward rotten flesh, bones, skulls or a bone block to draw Mortem Temper and a little health from them. The dead give no blood.
- Each of these memories says what it shapes on its tooltip and in its Liber page. Their R casts still work for now.

**Brazier transmutation:** set an offering in an Iron Brazier, light it with Projection and hold Absorption on it. If no memory, scar or graft rite wants the offering, the fire turns it into something else, chosen by the memory you have selected: Pyretic Forge gives twice the charcoal from a log and twice the glass from sand, Pyretic Forge also turns gravel to flint; Glacial Grasp packs ice and cools magma to obsidian, and packing ice takes three more of the same in your off hand; Vital Effusion roots dirt, grows moss and grinds a bone to bone meal. Some offerings want a catalyst in your off hand. JEI lists every recipe.

**Activation:**
- Depends on manipulation type
- Quick: Single press of manipulation key (`R` default)
- Charged: Hold manipulation key until charged
- Passive: Equip and toggle on with the manipulation key; activation still requires active blood and the stated alignment
- Continuous: Hold the manipulation key to channel; release to stop

**Management:**
- Check Field Notes (`B`) for known manipulations
- Organize by temper or frequency of use
- Monitor cooldowns via HUD

### Targeting and failed casts

Aimed rays stop at walls and glass; openings and partial cover follow collision shapes. Nearest-victim powers such as Hemorrhage and Exsanguinate require a visible enemy and can select behind you. Area pulses retain their area behavior. The audited hostile area powers protect teammates and owned/allied creatures, and respect disabled PvP.

Living Circuit links up to three nearby teammates and runs through the caster when none are in reach. Lumen Suture prioritizes the nearest injured teammate and falls back to yourself. Predictably invalid Forge, command, execution or shadow-teleport casts spend no blood, cooldown or mastery credit. A valid cast can spend exactly the blood you have; no additional 1 mL reserve is required.

Sovereign Instinct (now a Ductilis passive) turns commandable attackers, those of 80 maximum health or less, on one another for five seconds when at least four blood-bearing enemies are targeting you; bosses only gain Sanguine Pressure. Blackhearted's rupture clears its saturation and starts its existing refractory period. Phoenix Debt uses the shared Last Rite gate and cannot fire repeatedly. The gate is one save per 10 minutes across Phoenix Debt, Silent Refusal, Ink Reprieve, Cryptobiosis and the Curved Horn; the Horn is armed only when no other save is. A hit costs blood for the damage that lands: armour, the avatar and Iron Will lower it, a zeroed hit costs nothing, and a saved hit costs only what got through. These passives must be equipped, enabled, and meet their active-blood and alignment requirements.

See the [casting implementation report](../docs/archive/done/manipulation_work_plan/02-targeting-and-casting-results.md) for exact scope and remaining live acceptance.

### Manipulation Diagnostics

The **Scrying Podium** **Manipulations** tab is synced from the server. It shows how many manipulations you know, how many are currently equipped, the current equipped-slot cap, and the selected manipulation's base blood cost versus effective blood cost.

Hover the equipped-slot modifier row to see the active slot formula: `min(9, 3 + degree / 2 + Manip Slots skill)`. Hover the selected manipulation's cost modifier row to see each active discount or surcharge, including skills, manipulation mastery, temper-match bonuses, purity penalties, potion effects, world effects, rite effects, and equipped tools.

### Synaptic Loadouts

At Degree 5, the **Dendritic Distributor** becomes a Synaptic Loadout station. It stores whole manipulation arrangements as named neural patterns:

- 3 remembered patterns by default
- `skill_synaptic_memory` adds 1 pattern per level, up to 7 total
- Saving or overwriting costs 100 blood and 25 raw XP
- Applying and renaming are free
- Conjure Staff stays automatic and is not saved inside patterns; Blood Absorption and Blood Projection are verbs and never sit in a pattern at all. Once learned, Conjure Staff cannot be dememorized at the Reliquary, costs no memorized slot, and remains available in the radial's utility section.

Use these patterns for combat, travel, rituals, and fane work. You do not need to relearn the manipulations.

The station has seven dendrites, one per pattern slot, each ending in an ambergris bouton. Its selector axon sweeps to the pattern last applied or saved there and lights that bouton yellow-white. Renaming leaves the selector where it is.

---

## The Eight Tempers

**Temper** (also called **Kinship**) represents the quality or affinity of your blood. Each temper unlocks different manipulations and playstyles.

The **Scrying Podium** **Temper** tab shows your dominant and latent tempers, a full temper-value profile, and rite readiness.

### Animus (Life/Vitality)

**Theme:** Healing, regeneration, vitality, growth

**Color:** Crimson-red with warm undertones

**Associated Enzyme:** Vivacious Enzyme

**Philosophy:** Blood is life itself. Master Animus to sustain yourself and others.

**Manipulations by rank** (counted from `ManipulationInit` on 2026-11-16; the weapon forms and the two verbs are left out): Humilis: Blood Shot, Guided Blood Shot, Blood Binding, Lingering Blood Binding, Chain Blood Binding, Tendon Line, Blood Lash, Venous Cling, Blood Rush, Vital Effusion; Mediocritas: Hematic Mortar, Sanguine Halo, Living Land, Blood Lattice, Hematic Riposte, Summon Avatar, Blood Avatar: Arms; Summa: Blood Avatar: Armor, Blood Avatar: Legs, Blood Cloud, Expansive Blood Cloud, Pursuing Blood Cloud, Blood Aneurysm; Magister: Blood Avatar: Complete, Sanguine Tempest; Perfectus: Crimson Coronation, Crimson Tithe.

**Playstyle:** Support, sustain, endurance

**Synergies:**
- Pairs well with tank builds
- Essential for group play
- Countered by Mortem temper

---

### Flammeus (Fire/Heat)

**Theme:** Combustion, heat, burning, explosive power

**Color:** Orange-red with flickering highlights

**Associated Enzyme:** Fervent Enzyme

**Philosophy:** Blood burns hot. Ignite it and unleash destruction.

**Manipulations by rank** (counted from `ManipulationInit` on 2026-11-16; the weapon forms and the two verbs are left out): Humilis: Crimson Flame Conjuration, Pyretic Forge, Sanguine Ignition, Scalding Updraft, Soaring Updraft; Mediocritas: Cauterizing Rebuke, Suspended Updraft, Expulsive Updraft, Searing Lash; Summa: Vitric Combustion; Magister: Furnace Veins; Perfectus: Phoenix Debt.

**Playstyle:** Offensive, area damage, crowd control

**Synergies:**
- High damage output
- Environmental destruction
- Countered by Congeatio temper

---

### Ductilis (Lightning / Nerves)

**Theme:** Nerves, impulses, conduction and command of bodies

**Color:** Pale electric yellow along crimson nerves

**Associated Enzyme:** Neurotic Enzyme

**Philosophy:** Blood carries the nerve's signal. Command the impulse and you command the body.

**Manipulations by rank** (counted from `ManipulationInit` on 2026-11-16; the weapon forms and the two verbs are left out): Humilis: Sanguine Marionette, Deadly Gaze, Synaptic Jolt, Hemolymphal Pulse; Mediocritas: Hematic Rebuke, Activation Potential, Conductive Mark, Sanguine Ward; Summa: Hematic Impressment, Thread Ripper; Magister: Sovereign Instinct, Living Circuit; Perfectus: Axonal Transduction, Synaptic Storm.

**Playstyle:** Versatile, offense and defense, tactical

**Synergies:**
- Paralysis, conduction and puppetry
- Opposed by Ferric temper

---

### Lux (Light/Radiance)

**Theme:** Illumination, purification, holy power

**Color:** Bright crimson with gold highlights

**Associated Enzyme:** Incandescent Enzyme

**Philosophy:** Blood can shine. Let it illuminate the darkness.

**Manipulations by rank** (counted from `ManipulationInit` on 2026-11-16; the weapon forms and the two verbs are left out): Humilis: Hematic Flare; Mediocritas: Prismatic Reproof, Hematic Beacon, Lumen Suture; Summa: none; Magister: Radiant Wake; Perfectus: White Verdict, Unclosing Eye.

**Playstyle:** Support, anti-undead, utility

**Synergies:**
- Strong against undead and corrupted
- Vision control
- Countered by Tenebris temper

**Current implemented combat notes:** Lux now has `hematic_flare` as a Humilis true-offense ray: it deals magic damage and applies Illuminated, which exposes concealed enemies. `prismatic_reproof` remains the larger cone follow-up: 4 magic damage, doubled against Illuminated targets. `radiant_wake` is the Lux Magister: a channelled glide that mends nearby allies and Illuminates enemies. Lux attacks dynamically oppose Tenebris-school Rogue Wills by their synced Will school.

---

### Mortem (Death/Decay)

**Theme:** Necrosis, withering, death magic

**Color:** Dark crimson with black veins

**Associated Enzyme:** Ruinous Enzyme

**Philosophy:** Blood knows death intimately. Wield that knowledge.

**Manipulations by rank** (counted from `ManipulationInit` on 2026-11-16; the weapon forms and the two verbs are left out): Humilis: Hemorrhage, Lignum Mortis, Canopy Mortis, Insatiable Hunger; Mediocritas: Worked Lignum, Exsanguinate, Grave Debt; Summa: Fruiting Contagion; Magister: Blackhearted, Carrion Communion; Perfectus: Funeral Bell, Bloom of Rot.

**Playstyle:** Debuff, damage-over-time, execution

**Synergies:**
- Strong against living creatures
- Combos with damage builds
- Countered by Animus temper

---

### Congeatio (Ice/Cold)

**Theme:** Freezing, preservation, cold magic

**Color:** Pale blue-red with frost

**Associated Enzyme:** Frigid Enzyme

**Philosophy:** Blood can be frozen, preserved, weaponized as ice.

**Manipulations by rank** (counted from `ManipulationInit` on 2026-11-16; the weapon forms and the two verbs are left out): Humilis: Glacial Grasp, Cryogenic Pulse; Mediocritas: Glacial Rampart, Cruor Javelin; Summa: Crystalline Bloom; Magister: Rimebound Sentence; Perfectus: Absolute Stillness, Endless Hour.

**Playstyle:** Control, defense, slow

**Synergies:**
- Excellent crowd control
- Defensive builds
- Countered by Flammeus temper

---

### Ferric (Magnetic/Ferrous)

**Theme:** Magnetism, attraction/repulsion, iron control

**Color:** Dark metallic red with magnetic distortion

**Associated Enzyme:** Ferric Enzyme (same name as temper)

**Philosophy:** Blood iron responds to magnetic force. Command it.

**Manipulations by rank** (counted from `ManipulationInit` on 2026-11-16; the weapon forms and the two verbs are left out): Humilis: Blood Needle, Blood Needle Fan, Blood Needle Lance, Sanguine Mending, Vascular Dowsing; Mediocritas: Lode Draw, Iron Retort; Summa: Hematic Hewing, Sanguine Magnetism, Ferric Rampart, Ferric Spikes; Magister: Ironhearted, Iron Choir; Perfectus: Lodestone Collapse.

**Playstyle:** Utility, positioning, environmental control

**Synergies:**
- Strong battlefield control
- Opposed by Ductilis temper

---

### Tenebris (Shadow/Darkness)

**Theme:** Concealment, stealth, shadow magic

**Color:** Black-red with shadowy wisps

**Associated Enzyme:** Umbral Enzyme

**Philosophy:** Blood flows in darkness. Embrace the shadow.

**Manipulations by rank** (counted from `ManipulationInit` on 2026-11-16; the weapon forms and the two verbs are left out): Humilis: Void Shroud, Gloam Laceration; Mediocritas: Umbral Step, Blood Eclipse, Black Veil Covenant, Umbral Reversal; Summa: Umbral Rend; Magister: Penumbral Drift; Perfectus: Eclipse Well.

**Playstyle:** Stealth, mobility, ambush

**Synergies:**
- Rogue/stealth builds
- Escape and infiltration
- Countered by Lux temper

**Current implemented combat notes:** Tenebris now has `gloam_laceration` as a Humilis ambush slash: it deals magic damage, applies Blood Loss to harmable targets it actually hits, hits harder while the caster is invisible (an un-Illuminated Veil counts) or standing in natural/synthetic darkness, and uses a thin liquid-dark slash with dissolving smoky edges. `void_shroud`, `black_veil_covenant`, `umbral_step`, and `blood_eclipse` remain the core stealth/darkness chain. Tenebris attacks dynamically oppose Lux-school Rogue Wills by their synced Will school.

---

## Temperament

**Alignment** measures how attuned your blood is to each temper. Higher alignment = more powerful manipulations of that temper.

### Building Alignment

**Method 1: Use Manipulations**
- Using a manipulation increases alignment with its temper
- More powerful manipulations give more alignment
- Passive gain over time with frequent use

**Method 2: Kill**
- A kill moves alignment toward the victim's schools (bosses much more)
- Enzymes do not move alignment: they are Temper catalysts (below)

**Method 3: Environmental Exposure**
- Certain biomes/areas boost specific tempers
- Fungal areas boost Mortem and Tenebris
- Bright areas boost Lux
- Cold areas boost Congeatio

**Method 4: Equipment**
- Armor and items can boost temper alignment
- Scars provide passive temper bonuses
- Founding Fane can be tuned to specific tempers

### Temper (the spendable pool)

Alignment is what you have done and is never spent. **Temper** is what you carry of each school right now, and every
cast and Sculpting burns a little of its own school: Humilis 1, Mediocritas 2, the higher ranks 3 to 5, a held Sculpting
0.5 a pulse. You can carry 20 of any school plus 0.6 per point of alignment in it, up to 80.

- **Fill it** by drawing blood from a living creature: its schools come with the blood (a creeper gives Ductilis and
  Flammeus, a parrot Animus and Lux, a cow Animus). Bloodless things (blazes, endermen, skeletons, golems) give none
  to the hand. A faltering Will you take whole by Absorption leaves 10 Temper per tier in its school.
- **Catalysts** cover a short pool: the off hand first, then your pack. An enzyme is worth 10, its spores 5, the
  school's old reagent 3 (blaze powder for Flammeus; rotten flesh, spider eye, glowstone dust, bone, packed ice,
  hematic iron scrap, ink sac for the others), coal or charcoal 1. A reagent thrown in a rendering pool gives
  its enzyme a quarter of the time (blaze powder, scrap, packed ice, spider eye) or a tenth (the rest), and a Lantern
  fruits an enzyme for 150 mL. One dissolves; what it brings beyond the cost stays.
- **Dry:** with neither, the cast still goes, but it costs a quarter more blood and lands a tenth softer; a dry
  Sculpting costs half again and works at three quarters. The action bar says so.
- **Scars** of a school slow its burn (a quarter per tier) and slowly refill a little (to 5 per tier).
- **Death** takes half. The Rite of Sanguine Attunement resets alignment, so every cap falls to 20.
- Temper shows on the HUD while you wear a Titration Bandolier in its Scarlet Vanity slot. The Alchemist gives you one,
  once, when you first ask about Temper at the Second Degree; a lost one is crafted from two leather, a Sinew Cord
  and three glass bottles. It shows as eight small medical vials tipped toward the blood vessel, four down each side. Each vial holding
  usable Temper pours a thin, constant trickle into the vessel; an empty vial, or one holding too little for a
  Humilis cast, stops pouring. Without the vessel the vials sit around your gourd or on a small spine instead, and
  do not pour. A school you have never touched shows a closed socket. Hover a vial with chat open, or outside your
  inventory panel, to read the school, held amount, cap and full/dry state.

### Alignment Levels

- **0-25:** Novice: Basic effectiveness
- **26-50:** Practiced: Improved effectiveness
- **51-75:** Aligned: Strong effectiveness
- **76-99:** Mastered: Very strong effectiveness
- **100:** Perfected: Maximum effectiveness

### Specialization vs Generalization

**Specialist Build:**
- Focus on 1-2 tempers
- Max alignment in chosen tempers
- Very powerful in niche
- Limited versatility

**Generalist Build:**
- Spread across 3-4 tempers
- Medium alignment in all
- Versatile toolkit
- Less specialized power

**Recommendation:** Start as a generalist. Specialize after you know which tempers and manipulations you actually use.

---

## Vascular System

The **Vascular System** represents your blood's internal structure and efficiency.

### Vein Sections

Every manipulation draws from one of the body's five **vein sections** (`EnumVeinSections`): **Head**, **Arms**, **Heart**, **Body** and **Legs**. The section is listed with each manipulation in the reference (section 8.2).

### Vascular Health

Debuffs from damaged sections are off by default (server config `vascularDebuffsEnabled`, default `false`). The Rite of Vascular Mending (Degree 2, a Lesser rite with one Glistering Melon Slice offering) heals all five sections at once.

---

## Skill Tree

The **Skill Tree** provides passive bonuses and unlocks. See the full tree in Field Notes.

### Key Skills

The tree has six branches (`common/init/skills/`): **Core** (blood capacity, regeneration, efficiency and combat basics), **Living Staff** (the Staff's forms, channels and rings), **Scars** (Scar Affinity, Scar Resonance, Scar Mastery and the deeper inscriptions), **Covenant** (bloodline and pool skills), **Mycelial** (the fungal skills of the late game) and **Summons** (puppet capacity, tethers and commands). The Skills tab of the Sanguine Conduit shows every skill with its Degree and parent.

### Skill Points

**Earning Points:**
- Degree advancement
- Completing rituals
- Discovering new manipulations
- Achievements and milestones

**Spending Points:**
- Open Field Notes skill tree
- Select desired skill
- Confirm purchase (irreversible)
- Skill activates immediately

---

## Advanced Techniques

### Manipulation Combos

Combine manipulations for enhanced effects:

**Flame + Iron = Molten Projectiles**
- Use Ductilis to create iron
- Apply Flammeus to superheat it
- Launch as devastating projectile

**Ice + Shadow = Frozen Ambush**
- Use Tenebris to conceal
- Use Congeatio to freeze when revealed
- Escape with Shadow Step

**Life + Light = Holy Healing**
- Combine Animus and Lux
- Enhanced healing with purification
- Effective against undead

### Blood Routing

**Blood Routing** allows direct transfer of blood between sources:

**From Vial to Self**
- Quick refill in combat
- Emergency reserve
- Portable blood storage

**From Self to Structure**
- Fuel rituals
- Power blood machines
- Activate constructs

**From Structure to Structure**
- Automate blood networks
- Create blood infrastructure
- Advanced base building

### Drudge Actions

> **Post-alpha WIP:** Drudge automation is documented for development testing and is not first-release progression.

Teach your Drudges specific manipulations:

1. Learn manipulation yourself
2. Craft Drudge Core with memory
3. Assign to Drudge via ritual
4. Drudge can now use that manipulation

**Strategic Uses:**
- Defensive Drudges (barriers, healing)
- Offensive Drudges (projectiles, debuffs)
- Utility Drudges (light, telekinesis)

---

## Blood Constructs

**Blood Constructs** are entities made from your blood by manipulations: the Blood Avatar and its forms, blood clouds and the like. Each is described with the manipulation that makes it in **[[School Combat]]** and the reference (section 27.1). The Sanguilith went with the Mana and Artifice integration.

---

## Status Effects

Hemomancy's effects are registered in `EffectInit` and listed in the reference (section 14). The ones a Harbinger meets most:

- **Blood Rush**: faster movement and attacks, a little healing while wounded (the Blood Rush manipulation).
- **Blood Loss**: slower movement (bleeding attacks and Barbed armour).
- **Blood Drunkenness**: the backlash of drinking blood that is not yours (flasks and Blood Rocks).
- **Hemolysis**: blood destroyed in the body (Unstained weapons and rites).
- **Paralysis**: Synaptic Storm's nerve lock.
- **School states**: each school's direct hits leave its state on the target (Pressure, Necrosis, Lodestone, Searing, Rime, Disrupted, Illuminated, Obscured), and the school's payoffs read it (**[[School Combat]]**).
- **Well Rested**: five minutes of extra blood regeneration after a full night's sleep.

---

## Tips for Blood Management

1. **Monitor Your Volume**: Never drain completely. Keep 500-1000 mL reserve.

2. **Carry Vials**: Pre-filled blood vials are emergency refills.

3. **Know Your Costs**: High-rank manipulations can drain you instantly. Plan accordingly.

4. **Regeneration Stacking**: Combine multiple regen sources for fast recovery.

5. **Temper Focus**: Pick 2-3 tempers and master them. Spreading thin weakens you.

6. **Manipulation Loadouts**: Customize which manipulations you have quick access to.

7. **Cooldown Awareness**: Don't spam. Cooldowns exist for balance.

8. **Practice Combos**: Some manipulations become stronger when used together.

9. **Environment Matters**: Use terrain and situation to your advantage.

10. **Upgrade Path**: Progress through degrees to unlock higher-rank manipulations.

---

## Integration with Other Systems

### With Mana and Artifice
Mana and Artifice compatibility was removed from the NeoForge 1.21.1 branch.

See: **[[Mod Compatibility]]**

### With Curios
Curios compatibility was removed on 2026-11-03. The Charm of Vascularium has its own slot (default key B).

---

## Next Steps

- **[[Harbinger Path]]**: Learn how to progress through degrees
- **[[Advanced Mechanics]]**: Master morphlings and puppeteering; review post-alpha Drudge WIP notes
- **[[World Content]]**: Find enzymes, catalysts, and rare materials

---

*"Blood is not merely a resource. It is memory, identity, power, and price. Master it, and you master yourself. Lose control, and it will consume you."*

*From the Liber Sanguinium, Chapter on Fundamentals*

## Manipulation balance pass — 7 September 2026

Ironhearted now stores up to 4 HP per fully charged cast. Iron Choir intercepts up to three hostile projectiles per paid second, and Absolute Stillness slows hostile projectiles every channel tick with reduced boss slowing. Carrion heals from health actually drained. Furnace grants allied fire resistance and respects pets and summons.

Base costs are now Hemorrhage 75 mL, Coronation 600 mL, Conductive Mark 150 mL and Crimson Tithe 100 mL; channel upkeep is Furnace 150, Drift 75 and Dowsing 75 mL/s. Mark and Hemorrhage last twelve seconds. Full-charge Coronation counterattacks gain stronger piercing needles. Tithe provides a 500 mL advance with its existing repayment and damage risk. What your blood cannot repay at the deadline stays owed, even through death, and is taken each second as your blood returns; no new Tithe is lent while it is owed.

Dowsing reports nearest ore coordinates, Forge preserves multi-output recipe counts and stack limits, and Lignum-family channels automatically harvest permitted blocks when selection completes. Earlier release and forced cancellation retain their separate behaviors. Memory recipes and progression gates are unchanged. Live balance acceptance and authored effects remain open.

See [area 4 comparisons and verification](../docs/archive/done/manipulation_work_plan/04-balance-results.md).

## Manipulation balance pass 2 — 10 October 2026

Charged manipulations now fizzle before payment below half charge ("Not enough charge"); a release still pays only its charged share, and alignment and skill points follow the released charge. Spells with nothing to act on, such as Hemorrhage, Blood Aneurysm or Rimebound Sentence with no target, refuse without spending blood or cooldown. Every boss cap uses one shared boss list, which now includes the Warden and Elder Guardian.

Sanguine Surge regenerates 1 mL/s per level and Last Wind 10 mL/s per level below 10% blood. A secondary temper can lift a manipulation's affinity but never weakens it. Applying a Synaptic pattern locks further applies for ten seconds. Vein sections now matter: higher-rank casts strain harder, a clotted section makes its manipulations 25% dearer, and a dead section refuses them. Kills of mobs with no blood profile no longer feed Animus.

Several Animus forms were re-ranked (Mortar, Halo, Lattice, Summon Avatar and Avatar Arms are learnable from Degree I; Tempest and Avatar Complete need Degree V), and Sanguine Marionette and Sovereign Instinct moved to Ductilis. Every school received tuning; the [developer reference](../docs/HEMOMANCY_REFERENCE.md) §8.2 lists current values, and the [balance plan](../docs/archive/audits/MANIPULATION_BALANCE_PLAN.md#11-pass-2) records what changed and why. Osseous Bloom is now called **Crystalline Bloom**.

### Specimen storage

The [Phlebotomist’s Cabinet](Phlebotomists-Cabinet.md) stores nine groups of up to 64 exactly matching full vials. Loose vials stay unstackable. Identified and unknown blood stay separate; ordinary extraction always produces individual vials.

Scar Resonance adds one cerebral loadout slot per purchased level to the shared Effigy/Brazier capacity: base D4/D5/D6+ capacity is 1/2/4, with up to three bonus slots and seven stored scar IDs. Purchase does not bypass D4. Recipe decoding retains no static Scar or memory-weaving cache; templates and Loom previews resolve through the current level recipe manager. Removed recipes and missing catalysts cannot retain a preview.

### Centrifuge fraction output

The Fractionating Rotor recovers its secondary fraction into the same eight output slots as primary fractions. Matching items and components stack first; remaining fractions use empty slots. The whole batch must fit before samples are consumed. Blocked completion retains its rolled results for retry. Legacy extra-bank contents are saved and recovered through these slots as space opens, with their original processing-stage credit.

## The Living Staff grows and mines (core-loop Track 8)

Everything you move through the Living Staff stays in it. At 5,000, 25,000, 100,000, 300,000 and 750,000 mL it lays
down a growth ring, a bone band on its shaft: more reach for the verbs through the Staff (half a block, then a block,
then a block more), a tenth faster channel from the third ring, a harder dig at the second and fourth, and 25 mL off
every form swap at the fifth. The Staff's tooltip says how many rings it has and how far the next one is. Planted in a running rite, the Staff still shows its bands.

The bond now also teaches **Conjure Pickaxe**, the Staff's ninth form. It takes iron ore from the first day, diamonds
from the second ring and obsidian from the fourth, and never matches netherite. No Living form takes an enchantment or
wears down; instead each block an axe or pickaxe form breaks costs blood by its hardness (3 mL for stone, 6 for an
ore), and with too little blood it digs like your bare hand. A Fortune or Silk Touch pickaxe is still worth carrying.

Two Ferric memories mine by held Projection, from the Staff or a cell hand: **Lode Draw** runs through an ore lode a
block at a time and breaks what it reached when you let go, and **Hematic Hewing** cuts a tunnel two tall each second,
three by three while sneaking, or a stair's step when aimed steeply into a wall. Both work at the Staff's ring tier,
give plain drops and leave chests and machines alone. The Talaromyces Minus scar's sneaking vein mine follows a lode
the same way.
