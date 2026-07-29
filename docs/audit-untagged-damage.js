#!/usr/bin/env node

const fs = require("fs");
const path = require("path");

const ROOT = path.resolve(__dirname, "..");
const EQUIPMENT_DIR = path.join(ROOT, "src", "main", "java", "me", "neoblade298", "neorogue", "equipment");

function findJavaFiles(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const entryPath = path.join(directory, entry.name);
    if (entry.isDirectory()) return findJavaFiles(entryPath);
    return entry.isFile() && entry.name.endsWith(".java") ? [entryPath] : [];
  });
}

function findMatchingBrace(source, openBrace) {
  let depth = 0;
  let state = "code";

  for (let index = openBrace; index < source.length; index++) {
    const char = source[index];
    const next = source[index + 1];

    if (state === "string") {
      if (char === "\\") index++;
      else if (char === '"') state = "code";
      continue;
    }
    if (state === "line-comment") {
      if (char === "\n") state = "code";
      continue;
    }
    if (state === "block-comment") {
      if (char === "*" && next === "/") {
        state = "code";
        index++;
      }
      continue;
    }

    if (char === '"') state = "string";
    else if (char === "/" && next === "/") {
      state = "line-comment";
      index++;
    } else if (char === "/" && next === "*") {
      state = "block-comment";
      index++;
    } else if (char === "{") depth++;
    else if (char === "}" && --depth === 0) return index;
  }
  return -1;
}

function getSetupItemBodies(source) {
  const bodies = [];
  const methodPattern = /\bvoid\s+setupItem\s*\([^)]*\)\s*\{/g;
  let match;
  while ((match = methodPattern.exec(source)) !== null) {
    const openBrace = source.indexOf("{", match.index);
    const closeBrace = findMatchingBrace(source, openBrace);
    if (closeBrace === -1) break;
    bodies.push({ source: source.slice(openBrace + 1, closeBrace), offset: openBrace + 1 });
    methodPattern.lastIndex = closeBrace + 1;
  }
  return bodies;
}

function decodeJavaString(value) {
  return value
    .replace(/\\n/g, "\n")
    .replace(/\\t/g, "\t")
    .replace(/\\r/g, "\r")
    .replace(/\\"/g, '"')
    .replace(/\\\\/g, "\\");
}

function buildDescriptionTemplate(body) {
  const tokenPattern = /"((?:\\.|[^"\\])*)"|GlossaryTag\.([A-Z_]+)/g;
  const positions = [];
  let template = "";
  let match;

  while ((match = tokenPattern.exec(body.source)) !== null) {
    if (match[1] !== undefined) {
      const text = decodeJavaString(match[1]);
      const start = template.length;
      template += text;
      positions.push({ start, end: template.length, sourceOffset: body.offset + match.index });
    } else {
      template += `[[TAG:${match[2]}]]`;
    }
  }
  return { template, positions };
}

function isTagged(template, damageIndex) {
  return /\[\[TAG:[A-Z_]+\]\]\s*$/.test(template.slice(0, damageIndex));
}

function cleanContext(template, damageIndex) {
  const start = Math.max(0, template.lastIndexOf(".", damageIndex - 1) + 1);
  const nextPeriod = template.indexOf(".", damageIndex);
  const end = nextPeriod === -1 ? template.length : nextPeriod + 1;
  return template.slice(start, end)
    .replace(/\[\[TAG:([A-Z_]+)\]\]/g, "<$1>")
    .replace(/\s+/g, " ")
    .trim();
}

function lineForTemplateIndex(source, positions, templateIndex) {
  const position = positions.find((entry) => templateIndex >= entry.start && templateIndex < entry.end);
  if (!position) return 1;
  return source.slice(0, position.sourceOffset).split("\n").length;
}

const findings = [];
for (const file of findJavaFiles(EQUIPMENT_DIR)) {
  const source = fs.readFileSync(file, "utf8");
  for (const body of getSetupItemBodies(source)) {
    const { template, positions } = buildDescriptionTemplate(body);
    const damagePattern = /\bdamage\b/gi;
    let match;
    while ((match = damagePattern.exec(template)) !== null) {
      if (isTagged(template, match.index)) continue;
      findings.push({
        file: path.relative(ROOT, file).replace(/\\/g, "/"),
        line: lineForTemplateIndex(source, positions, match.index),
        context: cleanContext(template, match.index)
      });
    }
  }
}

if (findings.length === 0) {
  console.log("No untagged damage wording found.");
} else {
  console.log(`Found ${findings.length} potentially untagged damage reference(s):\n`);
  for (const finding of findings) {
    console.log(`${finding.file}:${finding.line}`);
    console.log(`  ${finding.context}\n`);
  }
}

if (process.argv.includes("--strict") && findings.length > 0) process.exitCode = 1;