const scene = document.querySelector(".scene");
const content = document.querySelector("#content");
const back = document.querySelector("#back");
const parents = {
  quests: "home", inquiries: "home", conversation: "home", trade: "home", important: "home",
  first: "quests", supplies: "first", bottle: "inquiries", vial: "inquiries",
  lesson: "conversation", keepers: "lesson", clot: "lesson", gauge: "lesson"
};
const titles = {
  home: "Greeting", quests: "Quest Work", inquiries: "Inquiries", conversation: "Conversation",
  trade: "Trade", important: "Important", first: "First Draws", supplies: "First Draws",
  bottle: "Glass Bottle", vial: "Blood Vial", lesson: "Living Blood", keepers: "Keeping blood fresh",
  clot: "Clotted blood", gauge: "Checking a tank"
};
let current = "home";
let importantOrigin = false;
let accepted = false;
let unread = true;
let traded = false;

const icon = name => '<img src="assets/' + name + '.png" alt="">';
const heading = (label, title) => (label ? '<p class="eyebrow">' + label + '</p>' : "") +
  '<h2 id="page-title">' + title + '</h2>';
const speech = text => '<p class="speech">' + text + '</p>';
const rows = (...rows) => '<div class="list">' + rows.join("") + '</div>';
const action = (label, target, state = "", glyph = "") =>
  '<button class="action" ' + (target ? 'data-go="' + target + '"' : "disabled") +
  '><span>' + label + '</span><span class="status">' + (glyph ? icon(glyph) : "") +
  (state || (target ? '<span aria-hidden="true">→</span>' : "")) + '</span></button>';
const slot = (name, image, count, target, selected = false) => {
  const inner = icon(image) + '<b>' + count + '</b>';
  return target
    ? '<button class="slot" aria-label="' + name + '" title="' + name + '" data-go="' + target +
      '" aria-pressed="' + selected + '">' + inner + '</button>'
    : '<span class="slot" role="img" aria-label="' + name + '" title="' + name + '">' + inner + '</span>';
};
const lessonBranches = ["keepers", "clot", "gauge"];

const pages = {
  home: () => heading("", "Harbinger Alchemist") + speech(copy.greeting) +
    (accepted ? "" : '<hr class="divider">' + action(copy.firstOption, "first", "Available", "quests")),
  quests: () => heading("", titles.quests) + '<div class="list">' +
    action(copy.firstOption, "first", accepted ? "Active" : "Available", accepted ? "active" : "quests") +
    action(copy.separation, null, "Locked", "locked") + '</div>' +
    '<p class="status-note">First Separation requires Degree 2.</p>',
  first: () => heading(accepted ? "Active assignment" : "Assignment available", titles.first) +
    speech(accepted ? copy.firstProgress : copy.firstOffer) +
    (accepted ? '<p class="status-note">Samples 0 / 5 · Species 0 / 3</p>'
              : action(copy.accept, "supplies", "", "quests")),
  supplies: () => heading("Active assignment", titles.first) + speech(copy.firstProgress) +
    '<div class="slots">' + slot("Five empty Blood Vials", "vial", 5) +
    'Blood Vials received</div><p class="status-note">Samples 0 / 5 · Species 0 / 3</p>',
  important: () => heading("", titles.important) + '<div class="list">' +
    (accepted ? "There’s nothing waiting for you right now." : action(copy.firstOption, "first", "Available", "quests")) + '</div>',
  inquiries: () => heading("", titles.inquiries) + '<p>Choose an item from your inventory.</p>' +
    '<div class="slots">' + slot("Ask about a Glass Bottle", "bottle", 3, "bottle") +
    slot("Ask about a Blood Vial", "vial", 5, "vial") + '</div>',
  bottle: () => heading("", titles.bottle) +
    '<div class="slots">' + slot("Glass Bottle selected", "bottle", 3, "bottle", true) + '</div>' +
    copy.bottle.map(speech).join(""),
  vial: () => heading("", "Blood Vial") + '<div class="slots">' + slot("Blood Vial selected", "vial", 5, "vial", true) + '</div>' +
    copy.vial.map(speech).join(""),
  conversation: () => heading("", titles.conversation) +
    rows(action(copy.lessonOption, "lesson", unread ? "Unread" : "Read", unread ? "unread" : "lore")),
  lesson: () => heading("Lesson", titles.lesson) + copy.lesson.map(speech).join("") +
    rows(...lessonBranches.map(branch => action(copy[branch + "Option"], branch))),
  trade: () => heading("Trade", copy.tradeTitle) + speech(copy.tradeLine) +
    '<div class="slots">' + slot("250 mL of your blood", "blood_drop", "250") +
    '<span aria-hidden="true">→</span>' + slot("Two Bleeding Bulbs", "bulb", 2) +
    '<span>' + (traded ? "0" : "1") + ' in stock</span></div>' +
    action(traded ? "Sold out" : "Trade 250 mL for 2 Bleeding Bulbs", traded ? null : "exchange") +
    (traded ? '<p class="status-note">Trade complete. Restocks the next day.</p>' : "")
};
for (const branch of lessonBranches) {
  pages[branch] = () => heading("Living Blood", titles[branch]) + copy[branch].map(speech).join("");
}
function open(target, focus = true) {
  if (target === "supplies") accepted = true;
  if (target === "exchange") {
    traded = true;
    target = "trade";
  }
  if (target === "first" && current !== "supplies") importantOrigin = current === "important";
  if (target === "lesson") unread = false;
  current = target;
  const closed = target === "closed";
  document.querySelector(".frame").hidden = closed;
  const reopen = document.querySelector(".reopen");
  reopen.hidden = !closed;
  if (closed) {
    reopen.focus();
    return;
  }
  const tab = ["first", "supplies"].includes(target) ? (importantOrigin ? "important" : "quests")
    : ["bottle", "vial"].includes(target) ? "inquiries"
    : ["lesson", ...lessonBranches].includes(target) ? "conversation" : target;
  for (const button of document.querySelectorAll("nav button, .important")) {
    if (button.dataset.go === tab) button.setAttribute("aria-current", "page");
    else button.removeAttribute("aria-current");
  }
  document.querySelector(".important b").textContent = accepted ? "0" : "1";
  document.querySelector(".important").hidden = accepted && tab !== "important";
  document.querySelector("#path").textContent = target === tab ? titles[target] : titles[tab] + " / " + titles[target];
  back.disabled = target === "home";
  back.dataset.go = target === "first" && importantOrigin ? "important" : parents[target] || "home";
  content.innerHTML = pages[target]();
  content.scrollTop = 0;
  if (focus) content.focus({preventScroll: true});
}

document.addEventListener("click", event => {
  const target = event.target.closest("button[data-go]");
  if (target && !target.disabled) open(target.dataset.go);
});
document.addEventListener("keydown", event => {
  if (event.key === "Escape" && current !== "closed" && document.activeElement.id !== "theme") open("closed");
});
document.querySelector("#theme").addEventListener("change", event => scene.dataset.theme = event.target.value);
open("home", false);
