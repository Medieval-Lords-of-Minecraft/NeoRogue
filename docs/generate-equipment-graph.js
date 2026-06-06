const fs = require('fs');
const path = require('path');

const jsonPath = path.join(__dirname, '..', 'equipments.json');
const outputPath = path.join(__dirname, 'equipment-graph.html');

const data = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));
const equipments = data.equipments;

// Build lookup map by id
const byId = {};
equipments.forEach(e => { byId[e.id] = e; });

// Build reforge tree data: for each item with reforgeResults, recursively gather descendants
function buildTree(item) {
  if (!item.reforgeResults || item.reforgeResults.length === 0) return null;
  return item.reforgeResults.map(r => {
    const child = byId[r.result];
    return {
      id: r.result,
      name: child ? child.name : r.result,
      rarity: child ? child.rarity : 'UNKNOWN',
      catalyst: r.pairedWith,
      catalystName: byId[r.pairedWith] ? byId[r.pairedWith].name : r.pairedWith,
      children: child ? buildTree(child) : null
    };
  });
}

// Group equipment: class -> type -> items
const classOrder = ['WARRIOR', 'MAGE', 'ARCHER', 'THIEF', 'CLASSLESS', 'SHOP'];
const typeOrder = ['ABILITY', 'WEAPON', 'OFFHAND', 'ARMOR', 'ACCESSORY', 'ARTIFACT', 'CONSUMABLE'];

const grouped = {};
classOrder.forEach(c => { grouped[c] = {}; typeOrder.forEach(t => { grouped[c][t] = []; }); });

equipments.forEach(e => {
  const cls = classOrder.includes(e.class) ? e.class : 'CLASSLESS';
  const typ = typeOrder.includes(e.type) ? e.type : 'ABILITY';
  if (!grouped[cls]) { grouped[cls] = {}; typeOrder.forEach(t => { grouped[cls][t] = []; }); }
  if (!grouped[cls][typ]) grouped[cls][typ] = [];
  grouped[cls][typ].push({
    id: e.id,
    name: e.name,
    rarity: e.rarity,
    subtype: e.subtype,
    hasTree: e.reforgeResults && e.reforgeResults.length > 0,
    tree: buildTree(e),
    reforgeParents: e.reforgeParents || []
  });
});

// Sort items within each group alphabetically
Object.values(grouped).forEach(types => {
  Object.values(types).forEach(items => {
    items.sort((a, b) => a.name.localeCompare(b.name));
  });
});

// Generate HTML
const html = `<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>NeoRogue Equipment &amp; Reforge Trees</title>
<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body {
  font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
  background: #1a1a2e;
  color: #e0e0e0;
  padding: 20px;
  line-height: 1.4;
}
h1 {
  text-align: center;
  margin-bottom: 20px;
  color: #fff;
  font-size: 1.8em;
}
.filters {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 24px;
  padding: 16px;
  background: #16213e;
  border-radius: 8px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 12px rgba(0,0,0,0.4);
}
.filters label {
  font-size: 0.85em;
  color: #aaa;
  margin-right: 4px;
}
.filters select, .filters input {
  background: #0f3460;
  color: #e0e0e0;
  border: 1px solid #1a4a7a;
  border-radius: 4px;
  padding: 6px 10px;
  font-size: 0.9em;
}
.filters input { width: 200px; }
.stats {
  text-align: center;
  margin-bottom: 16px;
  font-size: 0.85em;
  color: #888;
}
.class-section {
  margin-bottom: 24px;
}
.class-header {
  font-size: 1.3em;
  font-weight: bold;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  gap: 8px;
}
.class-header:hover { opacity: 0.85; }
.class-header .toggle { transition: transform 0.2s; }
.class-header.collapsed .toggle { transform: rotate(-90deg); }
.class-WARRIOR { background: #4a1a1a; color: #ff6b6b; }
.class-MAGE { background: #1a1a4a; color: #6bcfff; }
.class-ARCHER { background: #1a4a1a; color: #6bff6b; }
.class-THIEF { background: #3a1a4a; color: #d06bff; }
.class-CLASSLESS { background: #3a3a3a; color: #ccc; }
.class-SHOP { background: #4a3a1a; color: #ffd06b; }
.class-content { padding-left: 8px; }
.class-content.hidden { display: none; }
.type-section { margin: 12px 0 12px 8px; }
.type-header {
  font-size: 1em;
  font-weight: 600;
  padding: 6px 12px;
  background: #222244;
  border-radius: 4px;
  margin-bottom: 6px;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  gap: 6px;
}
.type-header:hover { background: #2a2a50; }
.type-header .toggle { transition: transform 0.2s; font-size: 0.8em; }
.type-header.collapsed .toggle { transform: rotate(-90deg); }
.type-content.hidden { display: none; }
.type-count {
  font-size: 0.8em;
  color: #888;
  margin-left: 4px;
}
.item-list { padding-left: 8px; }
.item-row {
  display: flex;
  align-items: center;
  padding: 3px 8px;
  border-radius: 3px;
  gap: 6px;
  font-size: 0.9em;
}
.item-row:hover { background: #ffffff08; }
.item-toggle {
  cursor: pointer;
  width: 16px;
  text-align: center;
  font-size: 0.75em;
  color: #888;
  flex-shrink: 0;
  user-select: none;
}
.item-toggle:hover { color: #fff; }
.item-toggle.empty { visibility: hidden; }
.item-name { flex: 1; }
.rarity-badge {
  font-size: 0.7em;
  padding: 1px 6px;
  border-radius: 3px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.rarity-COMMON { background: #555; color: #ccc; }
.rarity-UNCOMMON { background: #1a5c1a; color: #6bff6b; }
.rarity-RARE { background: #1a3a6b; color: #6bb5ff; }
.rarity-EPIC { background: #4a1a6b; color: #c86bff; }
.rarity-LEGENDARY { background: #6b4a00; color: #ffd700; }
.rarity-UNKNOWN { background: #333; color: #999; }
.reforge-parent {
  font-size: 0.75em;
  color: #888;
  margin-left: 4px;
  font-style: italic;
}
.tree-container {
  margin-left: 30px;
  padding: 4px 0 4px 12px;
  border-left: 2px solid #333;
}
.tree-container.hidden { display: none; }
.tree-node {
  display: flex;
  align-items: center;
  padding: 2px 0;
  gap: 6px;
  font-size: 0.85em;
}
.tree-connector {
  color: #555;
  margin-right: 4px;
}
.tree-catalyst {
  font-size: 0.75em;
  color: #aa8833;
  font-style: italic;
}
.tree-children {
  margin-left: 16px;
  padding-left: 12px;
  border-left: 1px solid #333;
}
.highlight { background: #6b4a00; border-radius: 2px; }
.hidden-by-filter { display: none !important; }
</style>
</head>
<body>
<h1>NeoRogue Equipment &amp; Reforge Trees</h1>
<div class="filters">
  <div>
    <label for="classFilter">Class:</label>
    <select id="classFilter">
      <option value="ALL">All Classes</option>
      ${classOrder.map(c => `<option value="${c}">${c.charAt(0) + c.slice(1).toLowerCase()}</option>`).join('\n      ')}
    </select>
  </div>
  <div>
    <label for="typeFilter">Type:</label>
    <select id="typeFilter">
      <option value="ALL">All Types</option>
      ${typeOrder.map(t => `<option value="${t}">${t.charAt(0) + t.slice(1).toLowerCase()}</option>`).join('\n      ')}
    </select>
  </div>
  <div>
    <label for="searchInput">Search:</label>
    <input type="text" id="searchInput" placeholder="Filter by name...">
  </div>
  <div>
    <label>
      <input type="checkbox" id="reforgeOnly"> Reforge items only
    </label>
  </div>
</div>
<div class="stats" id="stats"></div>
<div id="content">
${generateContent()}
</div>
<script>
${generateScript()}
</script>
</body>
</html>`;

function generateContent() {
  let html = '';
  for (const cls of classOrder) {
    const types = grouped[cls];
    const totalInClass = Object.values(types).reduce((sum, items) => sum + items.length, 0);
    if (totalInClass === 0) continue;

    html += `<div class="class-section" data-class="${cls}">
  <div class="class-header class-${cls}" onclick="toggleClass(this)">
    <span class="toggle">▼</span> ${cls.charAt(0) + cls.slice(1).toLowerCase()} <span class="type-count">(${totalInClass})</span>
  </div>
  <div class="class-content">\n`;

    for (const typ of typeOrder) {
      const items = types[typ];
      if (!items || items.length === 0) continue;

      html += `    <div class="type-section" data-type="${typ}">
      <div class="type-header" onclick="toggleType(this)">
        <span class="toggle">▼</span> ${typ.charAt(0) + typ.slice(1).toLowerCase()} <span class="type-count">(${items.length})</span>
      </div>
      <div class="type-content">
        <div class="item-list">\n`;

      for (const item of items) {
        const toggleClass = item.hasTree ? '' : ' empty';
        const parentInfo = item.reforgeParents.length > 0
          ? `<span class="reforge-parent">(from: ${item.reforgeParents.map(p => {
              const parentItem = byId[p.parent];
              const catalystItem = byId[p.pairedWith];
              return `${parentItem ? parentItem.name : p.parent} via ${catalystItem ? catalystItem.name : p.pairedWith}`;
            }).join(', ')})</span>`
          : '';

        html += `          <div class="item-row" data-name="${escapeAttr(item.name)}" data-id="${item.id}" data-has-tree="${item.hasTree}" data-has-parents="${item.reforgeParents.length > 0}">
            <span class="item-toggle${toggleClass}" onclick="toggleTree(this)">${item.hasTree ? '▶' : ''}</span>
            <span class="item-name">${escapeHtml(item.name)}</span>
            ${parentInfo}
            <span class="rarity-badge rarity-${item.rarity}">${item.rarity}</span>
          </div>\n`;

        if (item.hasTree) {
          html += `          <div class="tree-container hidden" data-tree-for="${item.id}">\n`;
          html += renderTree(item.tree, 12);
          html += `          </div>\n`;
        }
      }

      html += `        </div>
      </div>
    </div>\n`;
    }

    html += `  </div>
</div>\n`;
  }
  return html;
}

function renderTree(nodes, indent) {
  if (!nodes) return '';
  let html = '';
  const prefix = ' '.repeat(indent);
  for (let i = 0; i < nodes.length; i++) {
    const node = nodes[i];
    const isLast = i === nodes.length - 1;
    const connector = isLast ? '└─' : '├─';
    html += `${prefix}<div class="tree-node">
${prefix}  <span class="tree-connector">${connector}</span>
${prefix}  <span class="item-name">${escapeHtml(node.name)}</span>
${prefix}  <span class="rarity-badge rarity-${node.rarity}">${node.rarity}</span>
${prefix}  <span class="tree-catalyst">via ${escapeHtml(node.catalystName)}</span>
${prefix}</div>\n`;
    if (node.children && node.children.length > 0) {
      html += `${prefix}<div class="tree-children">\n`;
      html += renderTree(node.children, indent + 2);
      html += `${prefix}</div>\n`;
    }
  }
  return html;
}

function escapeHtml(str) {
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function escapeAttr(str) {
  return str.replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function generateScript() {
  return `
const classFilter = document.getElementById('classFilter');
const typeFilter = document.getElementById('typeFilter');
const searchInput = document.getElementById('searchInput');
const reforgeOnly = document.getElementById('reforgeOnly');
const statsEl = document.getElementById('stats');

function toggleClass(header) {
  header.classList.toggle('collapsed');
  const content = header.nextElementSibling;
  content.classList.toggle('hidden');
}

function toggleType(header) {
  header.classList.toggle('collapsed');
  const content = header.nextElementSibling;
  content.classList.toggle('hidden');
}

function toggleTree(toggle) {
  if (toggle.classList.contains('empty')) return;
  const row = toggle.closest('.item-row');
  const tree = row.nextElementSibling;
  if (tree && tree.classList.contains('tree-container')) {
    tree.classList.toggle('hidden');
    toggle.textContent = tree.classList.contains('hidden') ? '▶' : '▼';
  }
}

function applyFilters() {
  const cls = classFilter.value;
  const typ = typeFilter.value;
  const search = searchInput.value.toLowerCase().trim();
  const reforgeOnlyChecked = reforgeOnly.checked;

  let visibleCount = 0;
  let totalCount = 0;

  document.querySelectorAll('.class-section').forEach(section => {
    const sectionClass = section.dataset.class;
    if (cls !== 'ALL' && sectionClass !== cls) {
      section.classList.add('hidden-by-filter');
      return;
    }
    section.classList.remove('hidden-by-filter');

    let classSectionVisible = false;
    section.querySelectorAll('.type-section').forEach(typeSection => {
      const sectionType = typeSection.dataset.type;
      if (typ !== 'ALL' && sectionType !== typ) {
        typeSection.classList.add('hidden-by-filter');
        return;
      }
      typeSection.classList.remove('hidden-by-filter');

      let typeSectionVisible = false;
      typeSection.querySelectorAll('.item-row').forEach(row => {
        totalCount++;
        const name = row.dataset.name.toLowerCase();
        const hasTree = row.dataset.hasTree === 'true';
        const hasParents = row.dataset.hasParents === 'true';
        const matchesSearch = !search || name.includes(search);
        const matchesReforge = !reforgeOnlyChecked || hasTree || hasParents;

        if (matchesSearch && matchesReforge) {
          row.classList.remove('hidden-by-filter');
          // Also show tree container if expanded
          const next = row.nextElementSibling;
          if (next && next.classList.contains('tree-container')) {
            next.classList.remove('hidden-by-filter');
          }
          visibleCount++;
          typeSectionVisible = true;
          classSectionVisible = true;

          // Highlight search match
          const nameEl = row.querySelector('.item-name');
          if (search && name.includes(search)) {
            const orig = row.dataset.name;
            const idx = name.indexOf(search);
            const before = orig.substring(0, idx);
            const match = orig.substring(idx, idx + search.length);
            const after = orig.substring(idx + search.length);
            nameEl.innerHTML = before + '<span class="highlight">' + match + '</span>' + after;
          } else {
            nameEl.textContent = row.dataset.name;
          }
        } else {
          row.classList.add('hidden-by-filter');
          const next = row.nextElementSibling;
          if (next && next.classList.contains('tree-container')) {
            next.classList.add('hidden-by-filter');
          }
        }
      });

      if (!typeSectionVisible && (search || reforgeOnlyChecked)) {
        typeSection.classList.add('hidden-by-filter');
      }
    });

    if (!classSectionVisible && (search || reforgeOnlyChecked)) {
      section.classList.add('hidden-by-filter');
    }
  });

  statsEl.textContent = search || cls !== 'ALL' || typ !== 'ALL' || reforgeOnlyChecked
    ? 'Showing ' + visibleCount + ' items'
    : totalCount + ' total items';
}

classFilter.addEventListener('change', applyFilters);
typeFilter.addEventListener('change', applyFilters);
searchInput.addEventListener('input', applyFilters);
reforgeOnly.addEventListener('change', applyFilters);

// Initial stats
applyFilters();

// Expand all trees matching search
searchInput.addEventListener('input', () => {
  if (searchInput.value.trim()) {
    document.querySelectorAll('.item-row:not(.hidden-by-filter)').forEach(row => {
      if (row.dataset.hasTree === 'true') {
        const tree = row.nextElementSibling;
        if (tree && tree.classList.contains('tree-container')) {
          // Check if any tree node matches
          const treeNames = tree.querySelectorAll('.item-name');
          let treeMatch = false;
          treeNames.forEach(n => {
            if (n.textContent.toLowerCase().includes(searchInput.value.toLowerCase())) {
              treeMatch = true;
            }
          });
          if (treeMatch) {
            tree.classList.remove('hidden');
            row.querySelector('.item-toggle').textContent = '▼';
          }
        }
      }
    });
  }
});
`;
}

fs.writeFileSync(outputPath, html, 'utf8');
console.log(`Generated ${outputPath}`);
console.log(`Total equipment: ${equipments.length}`);
console.log(`Classes: ${classOrder.join(', ')}`);
console.log(`Types: ${typeOrder.join(', ')}`);
