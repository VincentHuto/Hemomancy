# Hematic Succession and Manifested Successors

## Design Status

Implementation contract, updated September 17, 2026. The current implementation covers the five professions below. See `wiki/Hematic-Succession.md` for assembly and recovery, and `docs/TESTING.md` for automated and live validation boundaries.

## Purpose

Outpost NPCs must remain persistent teachers and contacts rather than exhaustible resources that players abduct from shared structures. Hematic Succession permits an established bloodline to create new local professional NPCs without removing the original donor from an outpost.

The system must:

- Protect shared outposts and their quest progression.
- Support worlds with limited structure generation or inaccessible outposts.
- Require the player to discover and obtain consent from an original professional before reproducing that profession locally.
- Make consent, blood identity, memory, flesh, and life mechanically distinct.
- Turn D5 bloodline founding into an immediately useful institutional progression step.
- Permit successors to die without permanently erasing their developed identity.

## Core Fiction

A manifested successor is a new person formed from five contributions:

| Component | Metaphysical contribution |
| --- | --- |
| Vacant Effigy | Form |
| Officiant's blood | Bloodline affiliation and governing will |
| Consenting donor's blood | Vocation and inherited professional identity |
| Sanguine Quintessence | Life |
| Mnemonic Ambergris | Memory and continuity |

The successor inherits the donor's professional Hematic Memories but is not a duplicate of the donor. Each successor receives a new name, appearance, personality seed, entity identity, and personal history.

## Progression Placement

Hematic Succession is a D5 bloodline system. It becomes available after a player:

1. Reaches D5.
2. Founds or joins a bloodline.
3. Establishes a recognised fane.
4. Receives an original professional's explicit Mnemonic Bequest through dialogue. Ordinary pool recruitment is independent and is not a prerequisite.

The system supplements exploration rather than replacing it. A bloodline cannot manifest a profession until it has located an original representative and earned that NPC's consent.

Knowledge is distributed by discipline:

- The **Alchemist** explains blood compatibility, donated samples, and Sanguine Quintessence.
- The **Mnemonist** explains Bequests, memory inheritance, mnemonic rejection, and restoration.
- The **Artificer** teaches fabrication of the Vacant Effigy through a blood-structure recipe.
- Bloodline progression combines those teachings into the Rite of Hematic Succession.

## Mnemonic Bequests and Consent

An original outpost NPC may explicitly pledge their professional blood-memory to a bloodline. This creates a persistent **Mnemonic Bequest**.

The Bequest records at minimum:

- Donor entity UUID.
- Donor profession or NPC type.
- Recipient bloodline UUID.
- Valid consent state.
- Eligibility for repeated successor creation.

Consent belongs to the bloodline rather than the individual player who earned it. Eligible D5+ members may obtain repeated donations by exchanging an empty vial with the original donor. Both rites require a D5+ member. There is no donation fee or cooldown; successors cannot grant Bequests.

A Bequest is persistent and authorises multiple successors over time. Each successor still requires a donor blood vial carrying valid donor and bloodline provenance. Other bloodlines must independently earn the same NPC's consent.

If revocation is later supported, it prevents future incarnations but does not invalidate or injure existing successors.

## Vacant Effigy

The **Vacant Effigy** is a generic, portable artificial body produced before the Cardinal Rite.

### Manufacture

- D5 blood-structure craft using Sanguine Formation and 250 mL personal blood. The horizontal 3×3 pattern has a Venous Stone center, four Conscious Mass corners, and four Hematic Iron edge blocks.
- The manufacturing structure is external to the Cardinal Rite and does not occupy its ritual floor.
- Its model should resemble the existing Bog Body: prone, preserved, human-shaped, and visibly organic.
- The completed effigy can be carried as an item and placed on a Venous Stone plinth beside the Cardinal Focus, two blocks from the Focus horizontally and two blocks above its floor level.

### Initial state

The effigy contains no identity, profession, memory, bloodline affiliation, or life. Its neutral biological construction permits future systems to reuse it for other incarnations, projected bodies, mnemonic reconstruction, simulacra, or more profane host rites.

### Consumption

The effigy is wholly consumed by successful incarnation, mnemonic rejection, or restoration. It is never a reusable station.

## Vocational POI Capacity

Every manifested successor requires one compatible, unclaimed vocational POI inside the registered bounds of the bloodline's fane.

Representative mappings include:

| Profession | Compatible POI examples |
| --- | --- |
| Alchemist | Ghastly Alembic or Vial Centrifuge |
| Mnemonist | Somatic Loom or Mnemonic Reliquary |
| Artificer | Hematic Armature |
| Cicatrix | Mason's Effigy or Fungal Implantation Pylon |
| Vicar | Cardinal Focus |

The donor always determines the successor's profession. A POI proves that the fane can support that profession; it never changes one profession into another.

### Capacity rules

- One physical POI supports one active successor.
- The rite reserves a compatible POI before accepting or consuming offerings.
- Concurrent rites cannot reserve the same POI.
- POIs outside the registered fane cannot satisfy the rite.
- A successor claims the reserved POI upon successful incarnation.
- Death or permanent dismissal releases the claim.

### Displacement

If a claimed station is destroyed or moved outside the fane, its successor becomes **Displaced**.

- The NPC remains alive and retains ordinary dialogue.
- Profession-specific services pause while displaced.
- The NPC periodically searches for another compatible, unclaimed POI within the fane.
- Unloaded chunks pause searches rather than producing replacement entities or duplicate claims.

## Rite of Hematic Succession

### Eligibility

Any D5+ current member of a bloodline may conduct either rite. Professional services remain available to other current members under their normal progression gates. There are no additional officer permissions for officiants.

The rite requires:

- Membership in the bloodline associated with the fane.
- A Greater Dominion floor, with the Vacant Effigy on the adjacent Venous Stone plinth and a Living Staff for activation.
- A blood vial from a consenting professional donor pledged to that bloodline.
- A blood vial belonging to the actual player officiating the rite.
- One Sanguine Quintessence.
- One Mnemonic Ambergris.
- One available vocational POI compatible with the donor's profession.

Membership and POI reservation are locked when the rite begins. A player leaving or being removed from the bloodline midway does not transfer ownership or confuse the result.

The six-phase body sequence lasts 1,200 server ticks (60 seconds), following the normal staff planting. There are no scheduled combat waves. Seat Quintessence in the Focus. The first three marked floor sockets hold donor blood, officiant blood, and Ambergris respectively; restoration uses the first two for remnant and officiant blood.

### Ritual phases

1. **Form:** The Vacant Effigy softens and develops living anatomy.
2. **Identity:** Donor blood enters the head and hands; officiant blood enters the heart.
3. **Life:** Sanguine Quintessence establishes breath, pulse, and movement.
4. **Memory:** Mnemonic Ambergris dissolves into golden-crimson particles and descends through the skull, spine, and vessels.
5. **Recognition:** The inherited memories test the body's blood identities and consent.
6. **Resolution:** The vessel either stabilises as a legitimate successor or suffers mnemonic rejection.

### Successful result

A legitimate successor receives:

- A unique entity UUID.
- A generated name and appearance.
- The donor's profession and core professional services.
- A curated set of inherited professional memories.
- A persistent personality seed.
- Membership in the officiant's bloodline.
- A claim upon the reserved vocational POI.
- A genealogical record naming donor, officiant, and bloodline.
- Persistent dialogue and relationship history of their own.

After awakening, the successor travels to the reserved POI and begins normal professional behaviour.

## Mnemonic Rejection

Mnemonic rejection is a specific consequence of counterfeit consent or blood identity. Ordinary construction errors and incomplete offerings use standard Cardinal Rite failure behaviour instead.

### Rejection conditions

Rejection occurs if:

- The donor blood lacks a valid Mnemonic Bequest for the officiant's bloodline.
- The alleged officiant blood does not belong to the player performing the rite.
- Donor provenance, bloodline identity, or consent data is falsified or invalid.
- The donor profession does not match the provenance encoded in the donated sample.

The rite evaluates blood provenance at the Identity consumption checkpoint and reveals mnemonic rejection at Resolution after Memory. By this point the body, both blood samples, Sanguine Quintessence, and Mnemonic Ambergris have been consumed. The vessel already possesses form, identity, and life; rejecting memory corrupts the living body rather than merely cancelling the ritual.

### Misbegotten Successor

Mnemonic rejection creates a hostile **Misbegotten Successor**.

Its visual design combines:

- The silhouette and professional dress of the intended NPC role.
- Patches of the officiant's or stolen player's skin stretched across the body.
- Conflicting limb proportions and asymmetrical attire.
- Hematic seams where identities meet.
- Brief texture flickering during attacks.
- Professional implements fused improperly into flesh.

Its behaviour must:

- Prioritise the fraudulent officiant.
- Include one malformed attack derived from the intended profession.
- Occasionally imitate the officiant's stance or held-item pose.
- Use fractured donor dialogue, names, or vocational phrases as ambient audio or subtitles.
- Expose portions of the Vacant Effigy anatomy when damaged.
- Remain unrecruitable and unrestorable.
- Never yield a valid Bequest or refund consumed ritual ingredients.

Representative attacks include unstable mixtures for Alchemists, malformed tool-limbs for Artificers, disorientation or false memory effects for Mnemonists, and temporary counterfeit scar debuffs for Cicatrices.

To reduce griefing and persistent server clutter, initial aggression and cleanup tracking are bound to the responsible officiant. Ordinary hostile-mob despawning may occur after that player is absent.

## Successor Mortality and Mnemonic Remnants

Manifested successors are mortal. Their death destroys the current body but does not automatically erase the person.

On death, a legitimate successor releases a bloodline-bound **Bound Mnemonic Remnant** containing:

- Successor UUID.
- Name and appearance seed.
- Profession.
- Personality state.
- Donor ancestry.
- Bloodline UUID.
- Relationships and dialogue history.
- Other explicitly supported persistent development data.

The successor's POI claim is released on death.

## Rite of Mnemonic Restoration

The restoration rite reconstructs the same successor rather than creating a new person.

It requires:

- One Vacant Effigy for form.
- The successor's Bound Mnemonic Remnant for identity and memory.
- One Sanguine Quintessence for renewed life.
- One blood vial belonging to the officiating bloodline member as the will calling the successor home.
- One available POI compatible with the preserved profession.

Restoration does not require donor blood, renewed donor consent, or additional Mnemonic Ambergris. Donor inheritance and formed memory already exist within the remnant.

Successful restoration preserves the successor's UUID-level identity, name, appearance, profession, personality, relationships, and dialogue history. The old remnant becomes inert while the identity is alive. Each later death advances its authoritative generation, so old copies cannot be reused. Failed restoration releases the identity lock and returns the consumed remnant; unconsumed remnants remain in their braziers. Misbegotten bodies never receive the legitimate identity UUID.

Using player blood that does not belong to the officiant causes mnemonic rejection. This failure creates a malformed version of the dead successor rather than the donor-player hybrid produced by failed first incarnation.

## Ownership and Bloodline Dissolution

Successors, Bequests, and Mnemonic Remnants belong to the bloodline UUID rather than an individual player.

- Any current member may use successor services under their normal progression gates; officiating requires D5+.
- Departing members lose access to the bloodline's Bequests and bound remnants.
- Successors remain with the bloodline if their officiant leaves.
- Bequests cannot be carried into another bloodline.
- Bloodline dissolution leaves successors dormant or unaligned rather than deleting them.

The exact reactivation or adoption process for successors orphaned by bloodline dissolution is outside this feature's initial implementation scope.

## Server Authority and Persistence

All identity and ownership validation occurs server-side.

Required protections include:

- Validate donor UUID, profession, bloodline UUID, and consent state from authoritative saved data.
- Validate the officiant vial against the performing player's UUID.
- Validate fane boundaries and POI compatibility before consumption.
- Reserve POIs atomically for the duration of a rite.
- Store successor UUIDs separately from item-carried remnant data.
- Mark restored remnants inert through authoritative saved state so copied item data cannot duplicate an NPC.
- Reconcile POI claims when chunks load, stations move, or entities return from unloading.
- Bind Misbegotten aggression and cleanup metadata to the responsible officiant.

Optional server configuration may impose per-bloodline or global successor caps. These are safeguards rather than the normal progression mechanism; vocational POIs remain the ordinary population limit.

## Intended Gameplay Loop

The D5 institutional loop is:

1. Discover a professional master at an outpost.
2. Reach D5 and establish or join a bloodline with a registered fane.
3. Receive a bloodline-wide Mnemonic Bequest.
4. Construct an appropriate workplace inside the fane.
5. Manufacture and place a Vacant Effigy.
6. Assemble and perform the Rite of Hematic Succession.
7. Welcome a new professional resident into the bloodline.
8. Expand the fane with further stations and professional lineages.

This turns D5 from personal apprenticeship into institutional authorship: the practitioner no longer merely borrows knowledge from old outposts, but establishes a living tradition of their own.

## Initial Implementation Scope

The first implementation should include:

- Persistent bloodline-wide Mnemonic Bequests.
- The Vacant Effigy and its blood-structure manufacturing recipe.
- Profession-to-POI compatibility data.
- Rite of Hematic Succession.
- One generic successor framework supporting existing professional NPC roles.
- Misbegotten Successor base entity with profession-specific attack hooks.
- Bound Mnemonic Remnants.
- Rite of Mnemonic Restoration.
- Server-side ownership, consent, provenance, POI reservation, and duplication protection.

Future expansion may add affection, personal quests, adoption, revocation, additional professions, and further effigy applications. These are not requirements of this implementation.

## Runtime and compatibility rules

Each living successor contributes 1,000 mL pool capacity and the existing 1,000 mL helper reserve. Death removes membership and capacity. Restoration restores the saved reserve and exhaustion deadline, including the existing timed recovery behavior; it does not refill the reserve as a resurrection reward. Displacement suspends professional services and physical helper work, while the resident remains alive.

Original teachers retain ordinary recruitment and pool benefits but do not take physical rite-helper roles. Recorded outpost provenance is used to return previously relocated originals only to a loaded safe destination. Unknown origins are preserved.

The Residents view in the Bloodline Pool shows identity, ancestry, and workplace state. At the leader's registered fane bloodwell, any member may recover a lost remnant for an inactive, unlocked identity for free. Reissue increments the remnant generation and invalidates earlier copies. Permanent dismissal is leader-only: the NPC stays alive and unaligned, loses its claim and benefits, and cannot be restored by the former bloodline.

Server-issued blood receipts supplement existing sample data. Legacy samples without individual identity are refused before ritual consumption. Injection, identification, centrifuges, and storage retain their existing behavior. Sneak-use an empty vial to take 100 mL of personal blood; ordinary player-target sampling also captures individual identity.

`hemomancy_succession` overworld saved data owns consent, issued sample receipts, person records, life state, remnant generations, workplace claims, and reservations. Active Cardinal Rite NBT owns captured bloodline, officiant, offering snapshots, body position, consumption flags, elapsed phase time, and resolution state. Required unloaded chunks pause a rite; ordinary disconnect grace applies once required chunks are available. Terminal cleanup releases reservations. Entity absence alone never marks death or creates a replacement.

Profession workplaces are reloadable under `data/hemomancy/succession_profession/`. Optional per-bloodline and global caps default to zero (disabled). Physical workplace capacity remains the default population limit. Player quests and rewards stay in their existing player-owned records; inherited professional knowledge never copies another player's completion.
