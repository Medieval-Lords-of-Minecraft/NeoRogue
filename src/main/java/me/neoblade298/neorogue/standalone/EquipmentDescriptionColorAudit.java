package me.neoblade298.neorogue.standalone;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Audits equipment description color consistency rules.
 *
 * Rule R1 (hard): numbers should not be gray.
 * Rule R2 (hard): values that change with upgrades should be yellow.
 * Rule R3H (heuristic, optional): upgraded values should trend in a better direction.
 *
 * Usage (run from bin directory):
 * java me.neoblade298.neorogue.standalone.EquipmentDescriptionColorAudit
 * java me.neoblade298.neorogue.standalone.EquipmentDescriptionColorAudit --include-heuristic
 */
public class EquipmentDescriptionColorAudit {

    private static final Pattern NEW_PATTERN = Pattern.compile("new\\s+(\\w+)\\(b\\);");
    private static final Pattern ASSIGN_TERNARY_PATTERN = Pattern.compile(
            "(?:this\\.)?(\\w+)\\s*=\\s*isUpgraded\\s*\\?\\s*([^:;]+)\\s*:\\s*([^;]+);");
    private static final Pattern ASSIGN_TERNARY_NEGATED_PATTERN = Pattern.compile(
            "(?:this\\.)?(\\w+)\\s*=\\s*!isUpgraded\\s*\\?\\s*([^:;]+)\\s*:\\s*([^;]+);");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(?:\\.\\d+)?");
    private static final Pattern GRAY_SEGMENT_PATTERN = Pattern.compile("<gray>(.*?)</gray>", Pattern.DOTALL);
    private static final Pattern TAGGED_NUMBER_PATTERN = Pattern.compile("<(?:white|yellow)>\\s*-?\\d+(?:\\.\\d+)?[a-zA-Z%]*\\s*</(?:white|yellow)>", Pattern.CASE_INSENSITIVE);

    private static class UpgradableValue {
        final String var;
        final String upgradedExpr;
        final String baseExpr;
        final int line;

        UpgradableValue(String var, String upgradedExpr, String baseExpr, int line) {
            this.var = var;
            this.upgradedExpr = upgradedExpr;
            this.baseExpr = baseExpr;
            this.line = line;
        }
    }

    private static class CreateItemCall {
        final String expression;
        final int startLine;

        CreateItemCall(String expression, int startLine) {
            this.expression = expression;
            this.startLine = startLine;
        }
    }

    private static class Finding {
        final String rule;
        final String path;
        final int line;
        final String message;

        Finding(String rule, String path, int line, String message) {
            this.rule = rule;
            this.path = path;
            this.line = line;
            this.message = message;
        }
    }

    public static void main(String[] args) throws Exception {
        boolean includeHeuristic = false;
        for (String arg : args) {
            if ("--include-heuristic".equals(arg)) {
                includeHeuristic = true;
            }
        }

        Path projectRoot = Paths.get("..").toAbsolutePath().normalize();
        Path srcRoot = projectRoot.resolve("src");
        Path equipmentRoot = srcRoot.resolve(Paths.get("me", "neoblade298", "neorogue", "equipment"));

        List<String> equipmentClassNames = parseEquipmentLoad(equipmentRoot.resolve("Equipment.java"));
        if (equipmentClassNames.isEmpty()) {
            System.err.println("No equipment classes found from Equipment.load().");
            return;
        }

        Map<String, Path> equipmentFiles = locateEquipmentFiles(equipmentRoot, equipmentClassNames);
        List<Finding> findings = new ArrayList<Finding>();

        for (Map.Entry<String, Path> entry : equipmentFiles.entrySet()) {
            String className = entry.getKey();
            Path file = entry.getValue();
            List<String> lines = Files.readAllLines(file);

            Map<String, UpgradableValue> upgradableVars = findUpgradableValues(lines);
            List<CreateItemCall> createItemCalls = extractCreateItemCalls(lines);

            findings.addAll(checkGrayNumbers(file, createItemCalls));
            findings.addAll(checkChangedVarsNotYellow(file, upgradableVars, createItemCalls));
            if (includeHeuristic) {
                findings.addAll(checkUpgradeDirectionHeuristic(file, className, upgradableVars));
            }
        }

        printFindings(findings);
    }

    private static List<String> parseEquipmentLoad(Path equipmentJava) throws IOException {
        List<String> names = new ArrayList<String>();
        boolean inLoadMethod = false;
        BufferedReader reader = new BufferedReader(new FileReader(equipmentJava.toFile()));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("public static void load()")) {
                    inLoadMethod = true;
                    continue;
                }
                if (!inLoadMethod) {
                    continue;
                }
                if (line.trim().startsWith("}") && !line.contains("new ")) {
                    break;
                }
                Matcher matcher = NEW_PATTERN.matcher(line);
                if (matcher.find()) {
                    String className = matcher.group(1);
                    if (!names.contains(className)) {
                        names.add(className);
                    }
                }
            }
        } finally {
            reader.close();
        }
        return names;
    }

    private static Map<String, Path> locateEquipmentFiles(Path equipmentRoot, List<String> names) {
        String[] subdirs = new String[] {
                "abilities", "accessories", "armor", "weapons", "offhands",
                "artifacts", "consumables", "cursed", "materials"
        };

        Map<String, Path> result = new LinkedHashMap<String, Path>();
        Set<String> unresolved = new HashSet<String>();

        for (String name : names) {
            boolean found = false;
            for (String subdir : subdirs) {
                Path p = equipmentRoot.resolve(Paths.get(subdir, name + ".java"));
                if (Files.exists(p)) {
                    result.put(name, p);
                    found = true;
                    break;
                }
            }
            if (!found) {
                unresolved.add(name);
            }
        }

        for (String missing : unresolved) {
            System.err.println("[WARN] Could not locate source file for equipment class: " + missing);
        }

        return result;
    }

    private static Map<String, UpgradableValue> findUpgradableValues(List<String> lines) {
        Map<String, UpgradableValue> result = new HashMap<String, UpgradableValue>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            Matcher m = ASSIGN_TERNARY_PATTERN.matcher(line);
            if (m.find()) {
                String var = m.group(1);
                String upgradedExpr = m.group(2).trim();
                String baseExpr = m.group(3).trim();
                result.put(var, new UpgradableValue(var, upgradedExpr, baseExpr, i + 1));
            }

            Matcher neg = ASSIGN_TERNARY_NEGATED_PATTERN.matcher(line);
            if (neg.find()) {
                String var = neg.group(1);
                String baseExpr = neg.group(2).trim();
                String upgradedExpr = neg.group(3).trim();
                result.put(var, new UpgradableValue(var, upgradedExpr, baseExpr, i + 1));
            }
        }

        return result;
    }

    private static List<CreateItemCall> extractCreateItemCalls(List<String> lines) {
        List<CreateItemCall> calls = new ArrayList<CreateItemCall>();

        boolean capturing = false;
        StringBuilder current = new StringBuilder();
        int parenDepth = 0;
        int startLine = -1;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (!capturing) {
                int idx = line.indexOf("createItem(");
                if (idx >= 0) {
                    capturing = true;
                    startLine = i + 1;
                    String part = line.substring(idx);
                    current.append(part).append('\n');
                    parenDepth = countParenDelta(part);
                    if (parenDepth <= 0 && part.contains(");")) {
                        calls.add(new CreateItemCall(current.toString(), startLine));
                        current.setLength(0);
                        capturing = false;
                        startLine = -1;
                    }
                }
            }
            else {
                current.append(line).append('\n');
                parenDepth += countParenDelta(line);
                if (parenDepth <= 0 && line.contains(");")) {
                    calls.add(new CreateItemCall(current.toString(), startLine));
                    current.setLength(0);
                    capturing = false;
                    startLine = -1;
                }
            }
        }

        return calls;
    }

    private static int countParenDelta(String s) {
        int d = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') d++;
            else if (c == ')') d--;
        }
        return d;
    }

    private static List<Finding> checkGrayNumbers(Path file, List<CreateItemCall> calls) {
        List<Finding> findings = new ArrayList<Finding>();
        String relPath = toRelPath(file);

        for (CreateItemCall call : calls) {
            Matcher grayMatcher = GRAY_SEGMENT_PATTERN.matcher(call.expression);
            while (grayMatcher.find()) {
                String segment = grayMatcher.group(1);

                String withoutTaggedNumbers = TAGGED_NUMBER_PATTERN.matcher(segment).replaceAll("");
                Matcher num = NUMBER_PATTERN.matcher(withoutTaggedNumbers);
                if (num.find()) {
                    findings.add(new Finding(
                            "R1",
                            relPath,
                            call.startLine,
                            "Possible gray-colored number in description segment: <gray>..." + preview(withoutTaggedNumbers) + "...</gray>"));
                }
            }
        }

        return findings;
    }

    private static List<Finding> checkChangedVarsNotYellow(Path file, Map<String, UpgradableValue> upgradableVars,
            List<CreateItemCall> calls) {
        List<Finding> findings = new ArrayList<Finding>();
        String relPath = toRelPath(file);

        for (Map.Entry<String, UpgradableValue> e : upgradableVars.entrySet()) {
            String var = e.getKey();
            UpgradableValue uv = e.getValue();

            for (CreateItemCall call : calls) {
                if (!containsWord(call.expression, var)) {
                    continue;
                }
                if (isVarRenderedYellow(call.expression, var)) {
                    continue;
                }

                findings.add(new Finding(
                        "R2",
                        relPath,
                        call.startLine,
                        "Upgradable value '" + var + "' appears in description but may not be yellow (defined line " + uv.line + ")."));
            }
        }

        return findings;
    }

    private static List<Finding> checkUpgradeDirectionHeuristic(Path file, String className,
            Map<String, UpgradableValue> upgradableVars) {
        List<Finding> findings = new ArrayList<Finding>();
        String relPath = toRelPath(file);

        for (UpgradableValue uv : upgradableVars.values()) {
            Double up = parseFirstNumber(uv.upgradedExpr);
            Double base = parseFirstNumber(uv.baseExpr);
            if (up == null || base == null) {
                continue;
            }

            Trend trend = expectedTrend(uv.var);
            if (trend == Trend.UNKNOWN) {
                continue;
            }

            boolean suspicious = false;
            if (trend == Trend.INCREASE_BETTER && up < base) {
                suspicious = true;
            }
            if (trend == Trend.DECREASE_BETTER && up > base) {
                suspicious = true;
            }

            if (suspicious) {
                findings.add(new Finding(
                        "R3H",
                        relPath,
                        uv.line,
                        "Heuristic: upgraded '" + uv.var + "' may be worse in " + className
                                + " (base=" + base + ", upgraded=" + up + ")."));
            }
        }

        return findings;
    }

    private static boolean containsWord(String text, String word) {
        Pattern p = Pattern.compile("\\b" + Pattern.quote(word) + "\\b");
        return p.matcher(text).find();
    }

    private static boolean isVarRenderedYellow(String expr, String var) {
        String q = Pattern.quote(var);

        if (callArgumentsContainVar(expr, "DescUtil.yellow", var)) {
            return true;
        }
        if (isVarYellowViaPotionCall(expr, var)) {
            return true;
        }

        Pattern[] yellowPatterns = new Pattern[] {
            Pattern.compile("DescUtil\\.duration\\s*\\(\\s*" + q + "\\s*,\\s*(?:true|isUpgraded)\\s*\\)"),
                Pattern.compile("\\.tag\\s*\\(\\s*this\\s*,\\s*" + q + "\\s*,\\s*true\\s*\\)"),
                Pattern.compile("<yellow>[^\"]*\"\\s*\\+\\s*" + q),
                Pattern.compile("\\+\\s*" + q + "\\s*\\+\\s*\"[^\"]*</yellow>")
        };

        for (Pattern p : yellowPatterns) {
            if (p.matcher(expr).find()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isVarYellowViaPotionCall(String expr, String var) {
        int idx = 0;
        while (true) {
            int m = expr.indexOf("DescUtil.potion", idx);
            if (m < 0) {
                return false;
            }

            int open = expr.indexOf('(', m + "DescUtil.potion".length());
            if (open < 0) {
                return false;
            }

            int depth = 0;
            int close = -1;
            for (int i = open; i < expr.length(); i++) {
                char c = expr.charAt(i);
                if (c == '(') {
                    depth++;
                }
                else if (c == ')') {
                    depth--;
                    if (depth == 0) {
                        close = i;
                        break;
                    }
                }
            }

            if (close < 0) {
                return false;
            }

            String argsRaw = expr.substring(open + 1, close);
            List<String> args = splitTopLevelArgs(argsRaw);
            if (args.size() >= 5) {
                String potency = args.get(1);
                String duration = args.get(2);
                String upgradePotency = args.get(3).trim();
                String upgradeDuration = args.get(4).trim();

                boolean varInPotency = containsWord(potency, var);
                boolean varInDuration = containsWord(duration, var);
                boolean potencyYellow = "true".equals(upgradePotency) || "isUpgraded".equals(upgradePotency);
                boolean durationYellow = "true".equals(upgradeDuration) || "isUpgraded".equals(upgradeDuration);

                if ((varInPotency && potencyYellow) || (varInDuration && durationYellow)) {
                    return true;
                }
            }

            idx = close + 1;
        }
    }

    private static List<String> splitTopLevelArgs(String raw) {
        List<String> out = new ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        int parenDepth = 0;
        int bracketDepth = 0;
        int braceDepth = 0;
        boolean inString = false;

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);

            if (c == '"' && (i == 0 || raw.charAt(i - 1) != '\\')) {
                inString = !inString;
                cur.append(c);
                continue;
            }

            if (!inString) {
                if (c == '(') parenDepth++;
                else if (c == ')') parenDepth--;
                else if (c == '[') bracketDepth++;
                else if (c == ']') bracketDepth--;
                else if (c == '{') braceDepth++;
                else if (c == '}') braceDepth--;

                if (c == ',' && parenDepth == 0 && bracketDepth == 0 && braceDepth == 0) {
                    out.add(cur.toString().trim());
                    cur.setLength(0);
                    continue;
                }
            }

            cur.append(c);
        }

        if (cur.length() > 0) {
            out.add(cur.toString().trim());
        }

        return out;
    }

    private static boolean callArgumentsContainVar(String expr, String methodName, String var) {
        int idx = 0;
        while (true) {
            int m = expr.indexOf(methodName, idx);
            if (m < 0) {
                return false;
            }

            int open = expr.indexOf('(', m + methodName.length());
            if (open < 0) {
                return false;
            }

            int depth = 0;
            int close = -1;
            for (int i = open; i < expr.length(); i++) {
                char c = expr.charAt(i);
                if (c == '(') {
                    depth++;
                }
                else if (c == ')') {
                    depth--;
                    if (depth == 0) {
                        close = i;
                        break;
                    }
                }
            }

            if (close < 0) {
                return false;
            }

            String args = expr.substring(open + 1, close);
            if (containsWord(args, var)) {
                return true;
            }

            idx = close + 1;
        }
    }

    private enum Trend {
        INCREASE_BETTER,
        DECREASE_BETTER,
        UNKNOWN
    }

    private static Trend expectedTrend(String varName) {
        String v = varName.toLowerCase();

        if (containsAny(v, new String[] { "cooldown", "cost", "delay", "interval", "period", "casttime", "cd" })) {
            return Trend.DECREASE_BETTER;
        }
        if (containsAny(v, new String[] { "damage", "dmg", "heal", "shield", "stack", "chance", "duration", "range",
                "count", "charge", "projectile", "mult", "multiplier", "bonus", "gain", "amount", "speed" })) {
            return Trend.INCREASE_BETTER;
        }
        return Trend.UNKNOWN;
    }

    private static boolean containsAny(String target, String[] needles) {
        for (String n : needles) {
            if (target.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private static Double parseFirstNumber(String expr) {
        Matcher m = NUMBER_PATTERN.matcher(expr);
        if (!m.find()) {
            return null;
        }
        try {
            return Double.parseDouble(m.group());
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String toRelPath(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        String p = normalized.toString().replace('\\', '/');
        int idx = p.indexOf("/src/");
        if (idx >= 0) {
            return p.substring(idx + 1);
        }
        return p;
    }

    private static String preview(String text) {
        String t = text.replace('\n', ' ').trim();
        if (t.length() > 60) {
            return t.substring(0, 60) + "...";
        }
        return t;
    }

    private static void printFindings(List<Finding> findings) {
        int r1 = 0;
        int r2 = 0;
        int r3 = 0;

        for (Finding f : findings) {
            if ("R1".equals(f.rule)) r1++;
            else if ("R2".equals(f.rule)) r2++;
            else if ("R3H".equals(f.rule)) r3++;
        }

        System.out.println("=== Equipment Description Color Audit ===");
        for (Finding f : findings) {
            System.out.println("[" + f.rule + "] " + f.path + ":" + f.line + " - " + f.message);
        }

        System.out.println();
        System.out.println("Summary:");
        System.out.println("  R1 (numbers in gray): " + r1);
        System.out.println("  R2 (changed value not yellow): " + r2);
        System.out.println("  R3H (heuristic worse upgrade): " + r3);
        System.out.println("  Total findings: " + findings.size());
    }
}
