"""Merge the Troupe's English dialogue and ledger text without replacing other entries."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[2]
path=ROOT/'src/main/resources/assets/hemomancy/lang/en_us.json'
language=json.loads(path.read_text(encoding='utf-8'))
root='hemomancy.circus.school.'
text={
 'instruction_required':'A professor must teach this shape before you can begin its Cardinal Ordeal.',
 'ask_history':'How did you join the Troupe?', 'ask_colleagues':'What do you think of the other professors?',
 'ask_preparation':'What should I prepare?', 'ask_aftermath':'What happens to the school after the final act?',
 'testimony':'Tell me about the wrong audience and the missing understudy.',
 'take_lesson':'Teach me your puppet shape.', 'practical':'Begin my practical examination.',
 'guest':'Perform a guest act using this exercise.', 'inspect':'Inspect my charged, attuned Crossbar.',
 'report':'Review my apprenticeship and investigation.', 'claim':'Collect my remaining Filaments.',
 'back':'Return to our conversation.', 'conductor':'Tell me about the school and its conductor.',
 'not_ready':'Instruction requires active blood and the professor\'s initiatory degree. Your lesson has not been recorded.',
 'inspected':'This Crossbar is attuned to you and charged. Learn a shape, defeat its ordeal, then call, command and recall a body with this same Crossbar.',
 'inspect_failed':'Hold your own attuned Crossbar with some charge. I cannot inspect someone else\'s instrument.',
 'unlock_first':'Unlock this shape through its Cardinal Ordeal first. A guest act also requires a passed practical and an available daily reward.',
 'target':'Rehearsal Threat', 'ring_blocked':'The practice space is blocked or wet. Clear it and return for the exercise.',
 'retry':'Your practical has ended. Return to the professor to retry; earned instruction and completed work remain recorded.',
 'passed':'Your professor records the completed act. Well controlled.',
 'report_complete':'Your completed work is recorded. The school recognizes your progress.',
 'report_incomplete':'There is still work to observe. Consult the Side section of your Assignment Ledger for the next step.',
 'reward':'Guest payment processed. %s Filaments remain reserved for collection from a Threadkeeper.',
 'preparation':'Attune a Marionette Crossbar to yourself, charge it at a Puppeteer\'s Spindle, and keep spare Filaments. Instruction reveals an ordeal recipe; it does not unlock a shape. The Cardinal Ordeal still requires its degree, blood, frame and reusable Crossbar. Practicals use your own known bodies, normal charge and existing commands. Two live rehearsal threats await you; stay within twenty blocks of this professor and finish within five minutes. You may recall or leave to retry.',
 'status.preparation':'The Threadhouse handles Crossbars, Filaments, inspection and reports. The Ringmaster conducts the final act.',
 'status.degree':'You have not reached the degree required for my shape. You can still study its construction and our history.',
 'status.lesson':'You are ready for personal instruction. Ask me to teach you this shape.',
 'status.ordeal':'Your instruction is recorded. Prepare the frame and defeat this shape\'s Cardinal Ordeal.',
 'status.practical':'You know the shape. Call it with your own Crossbar and prove its purpose in my rehearsal ring.',
 'status.mastered':'Your practical is complete. You may repeat the exercise as a paid guest act once per Minecraft day.',
 'aftermath.neutral':'The Ringmaster\'s bargain remains unresolved. Lessons are available without accepting either ending.',
 'aftermath.succession':'You have chosen Succession. Its final act follows the existing Ringmaster challenges; apprenticeship adds no requirement.',
 'aftermath.liberation':'You have chosen Liberation. Severing the captive strings kills the bound people; it does not release three living prisoners.',
 'aftermath.succession_complete':'Succession leaves the pavilion standing and grants its Pattern. The professors continue teaching, even those who distrust your bargain.',
 'aftermath.liberation_complete':'The captives and Ringmaster are dead, and the carousel is ruined. We teach at the surviving camps. No triumph can bring those people back.',
 'ringmaster.curriculum':'The Crimson Troupe is a school as well as a show. The Acrobat teaches pursuit, the Dagger Thrower focus, the Stilt Walker attention, the Strongman protection, the Beast Tamer rupture, the Fire Breather restraint, and the Retired Understudy memory. Their instruction prepares an ordeal. Victory earns the shape. A practical earns trust.',
 'ringmaster.faculty':'The Threadkeeper prepares your instrument. I teach coordination through the existing final act. A pupil may finish every lesson and decline my offer, or enter my act without earning every professor\'s approval. The Brined Court is our neighboring faction, not the authority that grants your Crossbar its strings.',
 'clue.stage':'A torn programme names an understudy in the role of a living person. The pencilled correction changes the role to "carousel support." Find the rigging account in the pavilion loft.',
 'clue.loft':'The rigging account records three substitutions, with the same names copied into the carousel\'s passenger tally. A margin notes that their rooms were never reassigned. Search the understudy quarters.',
 'clue.quarters':'Three unused beds and an unfinished letter contradict the announcement that the understudies left on tour. The Retired Understudy can identify the missing performers. Examine the carousel tally itself.',
 'clue.carousel':'The carousel tally confirms that the three missing performers are its bound captives. Severing the strings is lethal. Bring this evidence and the Retired Understudy\'s testimony to a Threadkeeper.',
 'ledger.title':'Crimson Troupe Apprenticeship', 'ledger.empty_ring':'An Empty Ring',
 'ledger.wrong_audience':'The Wrong Audience', 'ledger.missing_understudy':'The Missing Understudy',
 'ledger.guest':'Guest Performances',
 'ledger.guest.pending':'Your payment is reserved. Make inventory space and collect it from any Threadkeeper.',
 'ledger.guest.available':'After passing a practical, repeat that professor\'s exercise for four Filaments, once per Minecraft day.',
}
faculty={
 'circus_acrobat':('Acrobat',
  'A Vulture does not win by hovering over your shoulder. Give it a line to pursue, then a reason to return.',
  'I learned rigging before I learned blood. One snapped line taught me the difference between a body that falls and a body that chooses its descent. My Vultures still carry that lesson in their wing joints.',
  'The Strongman calls retreat a waste of a good lift. I call it the second half of a successful pursuit. The Fire Breather understands: an act needs a recovery as much as an entrance.',
  'Watch the wing spars and the scarlet tendons. The Veinwing Vulture is a constructed aerial striker, not a natural bird. At Degree 3, prepare its existing frame and Cardinal Ordeal. In the practical it must pursue a rehearsal threat from the air, hit it, and return when recalled.',
  'I will keep the ropes serviceable for the next pupil. A new conductor does not get to tell the wings how to remember flight.',
  'The wrong audience watched our hands instead of the act. I saw an understudy climb down from the loft, then saw their name disappear from the next programme.'),
 'circus_knife_thrower':('Dagger Thrower',
  'Six hands are useful only when they agree on one target. My Juggler wears three faces so you can see how often they disagree.',
  'I used to throw one knife and congratulate myself for hitting. The Juggler taught me that the empty hands between throws matter. A laughing mask, a grieving mask, and a blank one share the same suspended frame.',
  'The Stilt Walker says a distracted enemy is a defeated enemy. I say distraction buys time for a deliberate throw. We argue because both of us have seen a careless pupil harm the audience.',
  'The Marrow Juggler is the shape older records call a Marrow Spitter. Its ordeal and Crossbar identity remain the same. At Degree 3, prepare the existing offering, unlock it, then command a full three-dagger volley into one rehearsal threat. The three throws share one attack\'s strength.',
  'If the pavilion survives, I will teach in its blade ring. If it falls, a wagon floor will do. I do not intend to let the Ringmaster own the next generation of hands.',
  'That audience applauded when a puppet took a bow, but not when a performer did. I recognized the expression: they wanted obedience, not craft. Speak to the Stilt Walker and prove you can focus without surrendering control.'),
 'circus_stilt_walker':('Stilt Walker',
  'The Scarlet Mummer borrows an enemy\'s attention. Borrow it long enough for somebody else to breathe.',
  'Above the crowd, I could see who was actually watching and who was looking for a way out. I built the Mummer to give those frightened people another place to look.',
  'The Dagger Thrower wants one perfect target. I want several imperfect ones looking away from a pupil. The Beast Tamer trusts the split pack; I trust the shared glance.',
  'At Degree 4, learn the Scarlet Mummer, complete its existing Cardinal Ordeal, and bring your own known body here. The practical requires it to divert both rehearsal threats while they are pursuing you or an allied puppet. Its pulse and one evasive step remain its real abilities.',
  'A mask can still protect a pupil after a conductor loses their stage. I will not call a dead captive a liberated student.',
  'The wrong audience could not distinguish an act from a command. The missing understudies stopped choosing their entrances before they disappeared. Ask the Dagger Thrower, then show me attention used to protect somebody.'),
 'circus_strongman':('Strongman',
  'The Hulk\'s broad shoulders are a promise. Put it where the danger will arrive and let it keep that promise.',
  'My first body could lift a wagon and could not keep a door open for one frightened child. That was strength without purpose. The belt and tendon bindings make this one\'s load visible.',
  'The Acrobat knows when to leave. I know where to remain. A conductor who cannot value both will break every string they own.',
  'At Degree 4, prepare the existing Gorebound Hulk offering and finish its Cardinal Ordeal. In my practical, anchor Guard inside this ring, keep the Hulk guarding for five seconds, and have it strike a rehearsal threat while guarding. The small head, lifting belt and heavy forearms belong to a constructed strongman; its existing strength and cost remain.',
  'I can rebuild a practice ring. I cannot rebuild the people under that carousel. Any successor who forgets that can practice alone.',
  'I was told the understudies had taken a travelling engagement. Their luggage remained under their beds. Find the rooms before you repeat that lie.'),
 'circus_beast_tamer':('Beast Tamer',
  'A Hound\'s rupture changes the pack. Command the body carefully, then let its temporary Curs finish the pursuit.',
  'I joined with a cage and learned to leave it empty. A constructed Hound is bound by a Crossbar; an animal has its own needs. The Court\'s living creatures deserve more than our stage language.',
  'The Stilt Walker holds attention in one place. My pack spreads it out. The Fire Breather worries about singeing a Cur; that concern is exactly why I trust their lessons.',
  'At Degree 4, prepare the existing Sanguine Hound bridle and Cardinal Ordeal. In the practical, allow your Hound to rupture under the rehearsal threats\' attacks, then have one of its temporary Blood Curs land a hit. A Cur cannot be learned, called separately, or kept as another permanent shape.',
  'The pack does not need a pavilion to learn its route. Other camps keep teaching when this site is finished.',
  'Three names vanished from our travelling roster, yet their rations still appeared on the pavilion account. The loft\'s copied tally is more trustworthy than a farewell announced from a stage.'),
 'circus_fire_eater':('Fire Breather',
  'The coil steadies the body; the lung-sac steadies the breath. Fire is the final step, not the first.',
  'The oldest frame I inherited stood on brittle legs. I gave it a serpent\'s lower body so the gaunt upper half could spend its effort on the lungs. The open ribs let a pupil read the pressure before the mouth opens.',
  'The Beast Tamer makes me account for every allied body in the ring. The Strongman likes a conspicuous wind-up. Both are right: a dangerous act ought to announce itself.',
  'At Degree 4, build a Cinder Bellows Frame from hematic iron scraps, leather, Filaments and blaze powder. Its Dominion Lesser ordeal requires 900 mL and consumes the frame only after manifestation, preserving your Crossbar. Your Bellows inhales for one second, breathes a five-block, sixty-degree cone for two seconds, then recovers for two. The practical requires both rehearsal threats to be hit during one breath. Walls and allies must be respected; the act never ignites terrain.',
  'We can perform among ruins without setting them alight. Let the surviving camps be where pupils learn control again.',
  'I saw the three understudies rehearsing ordinary entrances. The Ringmaster later called their absence a change in casting. I did not see them leave the grounds.'),
 'circus_understudy':('Retired Understudy',
  'A Mnemonist body repeats an observed attack. First give it a real memory; then listen for its echo.',
  'An understudy learns another person\'s pauses, mistakes and recoveries. I retired before the Ringmaster decided that a perfect replacement should never need a choice of its own.',
  'The Threadkeeper keeps accounts because memory alone is easy to bend. The Juggler\'s blank face troubles me less than a conductor who insists every remembered face belongs to them.',
  'At Degree 5, complete the Mnemonist Puppet\'s existing ordeal. During the practical, strike a rehearsal threat yourself while your known puppet observes, then keep that threat in its range until it actually replays the recorded attack. Ordinary puppet melee is not an echo.',
  'The missing performers were people. A surviving school must keep their names, whether a successor takes the Pattern or the carousel ends in blood.',
  'Those three entries are the missing understudies. Their beds were not abandoned for a tour. The stage programme, loft account and quarters tell one story; the carousel tally confirms what the Ringmaster made of them.'),
 'circus_threadkeeper':('Threadkeeper',
  'Bring your own instrument. I inspect the binding; the professors teach what you put on its strings.',
  'I once counted ticket money. Now I count Crossbar charge, spare Filaments and the pupils who return to report. A good account tells you what remains owed.',
  'The Ringmaster promises a grand final act. The faculty promise a pupil can call, control and dismiss one body safely. I prefer to start with the promise we can inspect.',
  'The first assignment is An Empty Ring. Hold an attuned, charged Crossbar for inspection, learn any shape, complete its ordeal, then call, command and recall with that same Crossbar. Report here. Every professor records their own practical; story investigations and paid guest performances appear in the Side ledger.',
  'Teaching belongs to the scattered camps as well as this pavilion. Completed work and owed guest payment follow the pupil, not the building.',
  'To investigate the wrong audience, collect the Stilt Walker\'s and Dagger Thrower\'s accounts and pass their practicals. To trace the missing understudy, read the abandoned stage record, loft account, quarters letter and carousel tally, then hear the Retired Understudy. Report the facts here.')
}
shapes={'veinwing_vulture':('Veinwing Vulture','circus_acrobat',3,'pursue from the air, hit a rehearsal threat, then recall'),
 'marrow_spitter':('Marrow Juggler','circus_knife_thrower',3,'land all three daggers of one volley on one rehearsal threat'),
 'scarlet_mummer':('Scarlet Mummer','circus_stilt_walker',4,'divert both rehearsal threats away from yourself or an allied puppet'),
 'gorebound_hulk':('Gorebound Hulk','circus_strongman',4,'hold Guard in the ring for five seconds and land a guarded strike'),
 'sanguine_hound':('Sanguine Hound','circus_beast_tamer',4,'allow your Hound to rupture, then have its temporary Curs hit a rehearsal threat'),
 'cinder_bellows':('Cinder Bellows','circus_fire_eater',4,'hit both rehearsal threats in a single breath'),
 'mnemonist_puppet':('Mnemonist Puppet','circus_understudy',5,'strike a threat yourself, then have the puppet replay that observed attack')}
for role,(name,*lines) in faculty.items():
    language['entity.hemomancy.'+role]=name
    for topic,line in zip(['welcome','history','colleagues','lesson','aftermath','testimony'],lines):text[role+'.'+topic]=line
for shape,(name,role,degree,exercise) in shapes.items():
    text['exercise.'+shape]='Practical: '+exercise.capitalize()+'. Use your own known body and Crossbar; remain near the professor.'
    text['ledger.'+shape]=name+' Practical (Degree '+str(degree)+')'
    text['ledger.'+shape+'.lesson']='Receive personal instruction from the '+faculty[role][0]+'.'
    text['ledger.'+shape+'.ordeal']='Prepare the offering and complete this shape\'s Cardinal Ordeal. Your Crossbar remains the reusable medium.'
    text['ledger.'+shape+'.practical']='Return to the '+faculty[role][0]+' and '+exercise+'.'
    text['ledger.'+shape+'.mastered']='Practical passed. This professor can host a daily paid guest act.'
for quest,steps in {
 'empty_ring':{'inspect':'Ask a Threadkeeper to inspect your charged, owner-attuned Crossbar.', 'call':'Learn a shape, complete its ordeal, then call it with the inspected Crossbar.', 'command':'Issue a command with that Crossbar while its body is active.', 'recall':'Recall the called body using the same Crossbar.', 'report':'Report your observed call, command and recall to a Threadkeeper.'},
 'wrong_audience':{'testimony':'Hear the Stilt Walker and Dagger Thrower\'s accounts.', 'protect':'Pass the Mummer and Juggler practicals to demonstrate protective attention and deliberate focus.', 'report':'Report both accounts and practicals to a Threadkeeper at Degree 4.'},
 'missing_understudy':{'stage':'Read the record on an abandoned stage.', 'loft':'Read the rigging account in the pavilion\'s loft.', 'quarters':'Read the letter in the understudy quarters.', 'carousel':'Read the carousel tally beside the bound captives.', 'testimony':'Ask the Retired Understudy to identify the missing performers.', 'report':'Report the records and testimony to a Threadkeeper. The existing endings remain your choice.'}
}.items():
    for step,line in steps.items():text['ledger.'+quest+'.'+step]=line
    text['ledger.'+quest+'.complete']='Investigation or apprenticeship recorded. Your progress persists across death and travel.'
for key,value in text.items():language[root+key]=value
language.update({'entity.hemomancy.marrow_spitter':'Marrow Juggler','summon.hemomancy.marrow_spitter':'Marrow Juggler',
 'entity.hemomancy.cinder_bellows':'Cinder Bellows', 'hemomancy.summon.cinder_bellows.role':'Area Pressure', 'hemomancy.summon.cinder_bellows.lore':'A coiled lower body supports a gaunt rib frame and exposed bellows lung. Its readable inhale becomes a short cone of fire, then a long recovery. The Fire Breather teaches restraint.', 'hemomancy.summon.marrow_spitter.lore':'Six arms share a floating crimson frame, with laughing, mourning and blank masks. The Dagger Thrower teaches a deliberate three-dagger volley.','summon.hemomancy.cinder_bellows':'Cinder Bellows',
 'item.hemomancy.cinder_bellows_frame':'Cinder Bellows Frame',
 'item.hemomancy.circus_waybill.tooltip.use':'Use: next eligible untaught professor. Sneak-use: unfinished pavilion.',
 'death.attack.cinder_breath':'%1$s was consumed by a Cinder Bellows',
 'death.attack.cinder_breath.player':'%1$s was consumed by %2$s\'s Cinder Bellows',
 'death.attack.marrow_dagger':'%1$s was struck by a Marrow Juggler',
 'death.attack.marrow_dagger.player':'%1$s was struck by %2$s\'s Marrow Juggler'})
for name,title in [('laughing_mask','Laughing Mask'),('mourning_mask','Mourning Mask'),('blank_mask','Blank Mask'),('troupe_rigging','Troupe Rigging')]:language['block.hemomancy.'+name]=title
for name in ['stage','loft','quarters','carousel']:language['block.hemomancy.circus_'+name+'_record']='Troupe '+name.title()+' Record'
path.write_text(json.dumps(language,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Merged Troupe language entries.')
