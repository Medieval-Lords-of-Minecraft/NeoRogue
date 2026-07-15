package me.neoblade298.neorogue.session.fight;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.analytics.EquipmentContribution;
import me.neoblade298.neorogue.session.fight.buff.StatCategory;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class FightStatistics {
	private static final DecimalFormat df = new DecimalFormat("#.##");
	private static final Component separator = Component.text(" / ", NamedTextColor.GRAY).hoverEvent(null);
	private PlayerFightData data;
	private HashMap<StatTracker, Double> damageDealt = new HashMap<StatTracker, Double>();
	private HashMap<String, HashMap<DamageType, Double>> damageTaken = new HashMap<String, HashMap<DamageType, Double>>();
	private HashMap<StatusType, HashMap<String, Integer>> statusesApplied = new HashMap<StatusType, HashMap<String, Integer>>();
	// Effective poison applied per source: stacks x (duration / 20), since poison damage scales with duration
	private HashMap<String, Double> effectivePoisonApplied = new HashMap<String, Double>();
	private HashMap<StatTracker, Double> buffStats = new HashMap<StatTracker, Double>();
	private HashMap<String, Double> shieldsByEquip = new HashMap<String, Double>();
	private HashMap<String, Double> healingByEquip = new HashMap<String, Double>();
	private HashMap<String, Double> nullifiedByEquip = new HashMap<String, Double>();
	private HashMap<String, Component> sourceNames = new HashMap<String, Component>();
	private double healingGiven, healingReceived, selfHealing, damageShielded, shieldsApplied, defenseBuffed, damageBarriered, damageNullified, healthDamageTaken;
	private int deaths, revives;

	private static final String UNATTRIBUTED = "\u0000misc";

	// Etc stats
	private double evadeMitigated;

	public void addEvadeMitigated(double evadeMitigated) {
		this.evadeMitigated += evadeMitigated;
	}

	public void addDamageNullified(double damageNullified) {
		addDamageNullified((Equipment) null, damageNullified);
	}

	// Records nullified damage attributed to the equipment that cancelled it (null = misc).
	public void addDamageNullified(Equipment source, double amount) {
		this.damageNullified += amount;
		String key;
		if (source == null) {
			key = UNATTRIBUTED;
		}
		else {
			key = source.serialize();
			sourceNames.putIfAbsent(key, source.getDisplay());
		}
		nullifiedByEquip.merge(key, amount, Double::sum);
	}

	public void addDamageBarriered(double damageBarriered) {
		this.damageBarriered += damageBarriered;
	}

	public void addShieldsApplied(Equipment source, double amount) {
		if (source == null) addShieldsApplied((String) null, null, amount);
		else addShieldsApplied(source.serialize(), source.getDisplay(), amount);
	}

	// Records shields under a named (non-equipment) source, e.g. a status effect.
	public void addShieldsApplied(String key, Component display, double amount) {
		this.shieldsApplied += amount;
		if (key == null) key = UNATTRIBUTED;
		else if (display != null) sourceNames.putIfAbsent(key, display);
		shieldsByEquip.merge(key, amount, Double::sum);
	}

	private String recordSource(Equipment source) {
		if (source == null) return UNATTRIBUTED;
		String id = source.serialize();
		sourceNames.putIfAbsent(id, source.getDisplay());
		return id;
	}

	// Records per-equipment healing for analytics attribution (totals are tracked separately via
	// addSelfHealing/addHealingGiven). Null source = unattributed healing, not tracked per-equipment.
	public void addHealingDone(Equipment source, double amount) {
		if (source == null || amount <= 0) return;
		String key = source.serialize();
		sourceNames.putIfAbsent(key, source.getDisplay());
		healingByEquip.merge(key, amount, Double::sum);
	}

	public FightStatistics(PlayerFightData data) {
		this.data = data;
	}
	
	public void addDamageDealt(StatTracker tracker, double amount) {
		double amt = damageDealt.getOrDefault(tracker, 0D);
		damageDealt.put(tracker, amt + amount);
	}
	
	public void addDamageTaken(String mobId, DamageType type, double amount) {
		// Collapse alternate mob "forms" (e.g. Angvoth2 -> Angvoth) into one entity for stats
		mobId = Mob.getStatId(mobId);
		HashMap<DamageType, Double> mobMap = damageTaken.getOrDefault(mobId, new HashMap<DamageType, Double>());
		double amt = mobMap.getOrDefault(type, 0D);
		mobMap.put(type, amt + amount);
		damageTaken.put(mobId, mobMap);
	}
	
	public void addDamageShielded(double amount) {
		this.damageShielded += amount;
	}
	
	// Actual health lost after shields and mitigation, used for an accurate HP-loss display
	public void addHealthDamageTaken(double amount) {
		this.healthDamageTaken += amount;
	}
	
	public void addDefenseBuffed(double amount) {
		this.defenseBuffed += amount;
	}
	
	public void addHealingGiven(double amount) {
		this.healingGiven += amount;
	}
	
	public void addSelfHealing(double amount) {
		this.selfHealing += amount;
	}
	
	public void addHealingReceived(double amount) {
		this.healingReceived += amount;
	}
	
	public void addBuffStat(StatTracker stat, double amount) {
		double amt = buffStats.getOrDefault(stat, 0D);
		buffStats.put(stat, amt + amount);
	}

	public static Component getStatsHeader(String timer, FightScore score) {
		String scoreText = score != null ? " | Reward Rating: " + score.getMiniMessageDisplay() : "";
		return SharedUtil.color(
			"<gray>Fight Statistics [<white>" + timer + "</white>]" + scoreText + "\n== (Hoverable Stats) ==\n"
					+ "[<yellow>Name</yellow> (<green>HP</green>) - <red>Damage Dealt </red>/ <dark_red>Taken "
					+ "</dark_red>/ <gold>Statuses</gold>]"
		);
	}

	public void addStatusApplied(StatusType type, Equipment source, int amount) {
		statusesApplied.computeIfAbsent(type, k -> new HashMap<String, Integer>())
			.merge(recordSource(source), amount, Integer::sum);
	}

	// Effective poison = stacks x seconds. Tracked separately from raw stacks since a short, high-stack
	// application and a long, low-stack one can deal the same total damage.
	public void addEffectivePoison(Equipment source, double amount) {
		if (amount <= 0) return;
		effectivePoisonApplied.merge(recordSource(source), amount, Double::sum);
	}

	public HashMap<StatTracker, Double> getDamageDealt() {
		return damageDealt;
	}

	public HashMap<String, HashMap<DamageType, Double>> getDamageTaken() {
		return damageTaken;
	}

	public double getHealingGiven() {
		return healingGiven;
	}

	public double getHealingReceived() {
		return healingReceived;
	}

	public double getDamageShielded() {
		return damageShielded;
	}
	
	public double getHealthDamageTaken() {
		return healthDamageTaken;
	}
	
	public double getSelfHealing() {
		return selfHealing;
	}

	public double getDefenseBuffed() {
		return defenseBuffed;
	}
	
	public void addDeath() {
		deaths++;
	}
	
	public void addRevive() {
		revives++;
	}
	
	public int getDeaths() {
		return deaths;
	}
	
	public int getRevives() {
		return revives;
	}
	
	public HashMap<StatusType, Integer> getStatusesApplied() {
		HashMap<StatusType, Integer> totals = new HashMap<StatusType, Integer>();
		for (Entry<StatusType, HashMap<String, Integer>> ent : statusesApplied.entrySet()) {
			int sum = 0;
			for (int v : ent.getValue().values()) sum += v;
			totals.put(ent.getKey(), sum);
		}
		return totals;
	}

	public double getShieldsApplied() {
		return shieldsApplied;
	}

	public double getDamageBarriered() {
		return damageBarriered;
	}

	public HashMap<String, Double> getHealingByEquip() {
		return healingByEquip;
	}

	public double getTotalDamageDealt() {
		double total = 0;
		for (double amt : damageDealt.values()) total += amt;
		// Buff-added damage is tracked separately from its source, so include it for the full total
		for (Entry<StatTracker, Double> ent : buffStats.entrySet()) {
			StatTracker stat = ent.getKey();
			if (stat.isIgnored()) continue;
			StatCategory c = stat.getCategory() == StatCategory.OTHER ? StatCategory.DAMAGE_DEALT : stat.getCategory();
			if (c != StatCategory.DAMAGE_DEALT) continue;
			total += stat.isInverted() ? -ent.getValue() : ent.getValue();
		}
		return total;
	}

	public double getTotalDamageTaken() {
		double total = 0;
		for (HashMap<DamageType, Double> mobDmg : damageTaken.values()) {
			for (double amt : mobDmg.values()) total += amt;
		}
		return total;
	}

	// Aggregates this player's per-equipment value-added contributions for analytics. Keyed by
	// Equipment.serialize() variant key; non-equipment sources (status-driven damage, unattributed
	// shields/healing) remain in the map and are dropped by the caller when the key fails to resolve
	// to a registered Equipment.
	public HashMap<String, EquipmentContribution> exportContributions() {
		HashMap<String, EquipmentContribution> map = new HashMap<String, EquipmentContribution>();

		// Direct damage dealt, attributed per equipment (status-driven trackers have null equipmentId)
		for (Entry<StatTracker, Double> ent : damageDealt.entrySet()) {
			String key = ent.getKey().getEquipmentId();
			if (key == null) continue;
			map.computeIfAbsent(key, EquipmentContribution::new).damageDealt += ent.getValue();
		}

		// Buff value-added: damage buffs and mitigation. STATUS-category buffs are intentionally
		// omitted - the raw status stacks below already capture status effectiveness.
		for (Entry<StatTracker, Double> ent : buffStats.entrySet()) {
			StatTracker stat = ent.getKey();
			String key = stat.getEquipmentId();
			if (key == null) continue;
			StatCategory c = stat.getCategory();
			if (c == StatCategory.OTHER) c = StatCategory.DAMAGE_DEALT;
			double amt = stat.isInverted() ? -ent.getValue() : ent.getValue();
			if (c == StatCategory.DAMAGE_DEALT) {
				map.computeIfAbsent(key, EquipmentContribution::new).damageBuffAdded += amt;
			}
			else if (c == StatCategory.DAMAGE_TAKEN) {
				map.computeIfAbsent(key, EquipmentContribution::new).damageMitigated += amt;
			}
		}

		// Shields applied, per equipment (non-equipment keys resolve to null Equipment later)
		for (Entry<String, Double> ent : shieldsByEquip.entrySet()) {
			if (ent.getKey().equals(UNATTRIBUTED)) continue;
			map.computeIfAbsent(ent.getKey(), EquipmentContribution::new).shieldsApplied += ent.getValue();
		}

		// Healing done, per equipment
		for (Entry<String, Double> ent : healingByEquip.entrySet()) {
			map.computeIfAbsent(ent.getKey(), EquipmentContribution::new).healingDone += ent.getValue();
		}

		// Status stacks applied, per equipment and status type
		for (Entry<StatusType, HashMap<String, Integer>> typeEnt : statusesApplied.entrySet()) {
			StatusType type = typeEnt.getKey();
			for (Entry<String, Integer> srcEnt : typeEnt.getValue().entrySet()) {
				if (srcEnt.getKey().equals(UNATTRIBUTED)) continue;
				map.computeIfAbsent(srcEnt.getKey(), EquipmentContribution::new).addStatus(type, srcEnt.getValue());
			}
		}

		// Poison isn't tracked as raw stacks (see statusesApplied); record its effective value
		// (stacks x seconds) as POISON status stacks so analytics treats it like any other status.
		for (Entry<String, Double> ent : effectivePoisonApplied.entrySet()) {
			if (ent.getKey().equals(UNATTRIBUTED)) continue;
			map.computeIfAbsent(ent.getKey(), EquipmentContribution::new)
				.addStatus(StatusType.POISON, (int) Math.round(ent.getValue()));
		}

		return map;
	}

	public Component getStatLine(boolean lost) {
		Component line = Component.text("").append(getNameplateComponent(lost))
				.append(getDamageDealtComponent()).append(separator)
				.append(getDamageTakenComponent()).append(separator)
				.append(getStatusComponent());
		return line;
	}

	public Component getNameplateComponent(boolean lost) {
		Component nameHover = getNameHoverComponent();
		String hp = lost ? "0" : df.format(data.getPlayer().getHealth());
		return Component.text(data.getSessionData().getData().getDisplay(), NamedTextColor.YELLOW)
		.append(Component.text(" (", NamedTextColor.GRAY))
		.append(Component.text(hp, NamedTextColor.GREEN))
		.append(Component.text(")", NamedTextColor.GRAY))
		.hoverEvent(nameHover.children().isEmpty() ? null : HoverEvent.showText(getNameHoverComponent()))
		.append(Component.text(" - ", NamedTextColor.GRAY).hoverEvent(null));
	}
	
	public Component getDamageDealtComponent() {
		NamedTextColor color = NamedTextColor.RED;
		Component hover = Component.text("Damage dealt post buff and mitigation:", NamedTextColor.GRAY);
		double total = 0;
		boolean any = false;
		for (Entry<StatTracker, Double> ent : damageDealt.entrySet()) {
			StatTracker stat = ent.getKey();
			if (stat.isIgnored())
				continue;
			hover = hover.appendNewline().append(getStatPiece(stat.getDisplay(), ent.getValue()));
			total += ent.getValue();
			any = true;
		}
		// Buff-added damage is credited to the buffing equipment; include it in the headline total so it
		// still reflects the full amount dealt (source base + buffs).
		for (Entry<StatTracker, Double> ent : buffStats.entrySet()) {
			StatTracker stat = ent.getKey();
			if (stat.isIgnored()) continue;
			StatCategory c = stat.getCategory() == StatCategory.OTHER ? StatCategory.DAMAGE_DEALT : stat.getCategory();
			if (c != StatCategory.DAMAGE_DEALT) continue;
			total += stat.isInverted() ? -ent.getValue() : ent.getValue();
		}
		Component buffs = buffSection(StatCategory.DAMAGE_DEALT);
		if (buffs != null) {
			hover = hover.appendNewline().append(buffs);
			any = true;
		}
		if (!any) {
			return Component.text("0", color).hoverEvent(null);
		}
		return Component.text(df.format(total), color).hoverEvent(HoverEvent.showText(hover));
	}

	public Component getDamageTakenComponent() {
		Component hover = Component.text("Damage taken post buff and mitigation:", NamedTextColor.GRAY);
		double totalDamageTaken = 0;
		boolean hasDetail = false;
		for (Entry<String, HashMap<DamageType, Double>> mobMap : damageTaken.entrySet()) {
			Mob mob = Mob.get(mobMap.getKey());
			for (Entry<DamageType, Double> ent : mobMap.getValue().entrySet()) {
				totalDamageTaken += ent.getValue();
				hover = hover.appendNewline().append(getDamageTakenStatPiece(mob, ent.getKey(), mobMap.getValue()));
				hasDetail = true;
			}
		}
		if (damageBarriered != 0) { hover = hover.appendNewline().append(getStatPiece("Damage Barriered", damageBarriered)); hasDetail = true; }
		if (damageNullified != 0) {
			if (nullifiedByEquip.isEmpty()) {
				hover = hover.appendNewline().append(getStatPiece("Damage Nullified", damageNullified));
			}
			else {
				for (Entry<String, Double> ent : nullifiedByEquip.entrySet()) {
					hover = hover.appendNewline().append(getSourceAction(ent.getKey(), "Damage Nullified", ent.getValue()));
				}
			}
			hasDetail = true;
		}
		if (evadeMitigated != 0) { hover = hover.appendNewline().append(getStatPiece("Evade Mitigated", evadeMitigated)); hasDetail = true; }

		Component mitigation = buffSection(StatCategory.DAMAGE_TAKEN);
		if (mitigation != null) {
			hover = hover.appendNewline().append(mitigation);
			hasDetail = true;
		}
		if (!shieldsByEquip.isEmpty()) {
			for (Entry<String, Double> ent : shieldsByEquip.entrySet()) {
				hover = hover.appendNewline().append(getSourceAction(ent.getKey(), "Shields Applied", ent.getValue()));
			}
			hasDetail = true;
		}

		// Split incoming damage into what was absorbed (shields/rounding) vs. what actually hit health.
		// Uses the real post-shield health damage instead of reconstructing it, so the HP number matches
		// actual health lost even with team-applied shields or rounding.
		double shielded = Math.max(0, totalDamageTaken - healthDamageTaken);
		Component taken = Component.text("");
		if (shielded > 0) {
			taken = taken.append(Component.text(df.format(shielded) + "♥", NamedTextColor.YELLOW))
				.append(Component.text(", ", NamedTextColor.GRAY));
		}
		taken = taken.append(Component.text(df.format(healthDamageTaken) + "♥", NamedTextColor.DARK_RED));
		if (hasDetail) taken = taken.hoverEvent(hover);

		return taken;
	}

	public Component getStatusComponent() {
		double score = 0;
		boolean any = false;
		Component hover = Component.text("Statuses applied:", NamedTextColor.GRAY);
		for (Entry<StatusType, HashMap<String, Integer>> typeEnt : statusesApplied.entrySet()) {
			StatusType type = typeEnt.getKey();
			if (type.hidden) continue;
			for (Entry<String, Integer> srcEnt : typeEnt.getValue().entrySet()) {
				Component disp = sourceNames.getOrDefault(srcEnt.getKey(), Component.text("Misc", NamedTextColor.GRAY));
				hover = hover.appendNewline().append(disp.append(Component.text(" - ", NamedTextColor.GRAY))
					.append(type.ctag)
					.append(Component.text(": ", NamedTextColor.YELLOW))
					.append(Component.text(df.format(srcEnt.getValue()), NamedTextColor.WHITE)));
				score += srcEnt.getValue();
				any = true;
			}
		}
		// Effective poison (stacks x seconds) is tracked separately since poison damage scales with duration
		for (Entry<String, Double> ent : effectivePoisonApplied.entrySet()) {
			Component disp = sourceNames.getOrDefault(ent.getKey(), Component.text("Misc", NamedTextColor.GRAY));
			hover = hover.appendNewline().append(disp.append(Component.text(" - Effective ", NamedTextColor.GRAY))
				.append(StatusType.POISON.ctag)
				.append(Component.text(": ", NamedTextColor.YELLOW))
				.append(Component.text(df.format(ent.getValue()), NamedTextColor.WHITE)));
			any = true;
		}
		// Buffer contributions are credited to the buffing equipment; include them in the headline total
		// so it still reflects the full amount applied (base + buffs).
		for (Entry<StatTracker, Double> ent : buffStats.entrySet()) {
			StatTracker stat = ent.getKey();
			if (stat.isIgnored() || stat.getCategory() != StatCategory.STATUS) continue;
			score += stat.isInverted() ? -ent.getValue() : ent.getValue();
		}
		Component statusBuffs = buffSection(StatCategory.STATUS);
		if (statusBuffs != null) {
			hover = hover.appendNewline().append(statusBuffs);
			any = true;
		}
		Component cmp = Component.text(df.format(score), NamedTextColor.GOLD);
		if (any) cmp = cmp.hoverEvent(hover);
		return cmp;
	}

	// Builds a hover section for non-ignored buff trackers in the given category (OTHER folds into
	// damage dealt). Each line is "<equipment> - <suffix>: value". Returns null if no buffs match.
	private Component buffSection(StatCategory category) {
		Component section = null;
		for (Entry<StatTracker, Double> ent : buffStats.entrySet()) {
			StatTracker stat = ent.getKey();
			if (stat.isIgnored()) continue;
			StatCategory c = stat.getCategory();
			if (c == StatCategory.OTHER) c = StatCategory.DAMAGE_DEALT;
			if (c != category) continue;
			double amt = stat.isInverted() ? -ent.getValue() : ent.getValue();
			Component line = stat.getDisplay().append(Component.text(": " + df.format(amt), NamedTextColor.WHITE));
			section = section == null ? line : section.appendNewline().append(line);
		}
		return section;
	}

	// Builds an "<equipment> - <action>: <value>" line attributed to a source.
	private Component getSourceAction(String key, String action, double amount) {
		Component disp = sourceNames.getOrDefault(key, Component.text("Misc", NamedTextColor.GRAY));
		return disp.append(Component.text(" - " + action + ": ", NamedTextColor.GRAY))
			.append(Component.text(df.format(amount), NamedTextColor.WHITE));
	}

	public Component getTypedHover(HashMap<DamageType, Double> map, String preamble) {
		Component hover = Component.text(preamble, NamedTextColor.GRAY);
		for (Entry<DamageType, Double> ent : map.entrySet()) {
			hover = hover.appendNewline().append(getStatPiece(ent.getKey(), map));
		}
		return hover;
	}
	
	private Component getNameHoverComponent() {
		Component cmp = Component.text("General Stats:");
		cmp = appendIfNotEmpty(cmp, "Ally Healing", healingGiven);
		cmp = appendIfNotEmpty(cmp, "Self Healing", selfHealing);
		cmp = appendIfNotEmpty(cmp, "Healing Received", healingReceived);
		cmp = appendIfNotEmpty(cmp, "Deaths", deaths);
		cmp = appendIfNotEmpty(cmp, "Revives", revives);
		return cmp;
	}

	private Component appendIfNotEmpty(Component base, String display, double stat) {
		if (stat != 0) {
			return base.appendNewline().append(getStatPiece(display, stat));
		}
		return base;
	}

	private Component appendIfNotEmpty(Component base, String display, int stat) {
		if (stat != 0) {
			return base.appendNewline().append(getStatPiece(display, stat));
		}
		return base;
	}
	
	private Component getStatPiece(String display, int stat) {
		return Component.text(display + ": ", NamedTextColor.YELLOW).append(Component.text(stat, NamedTextColor.WHITE));
	}

	private Component getStatPiece(Component display, double stat) {
		return display.append(Component.text(": ", NamedTextColor.YELLOW)).append(Component.text(df.format(stat), NamedTextColor.WHITE));
	}
	private Component getStatPiece(DamageType type, HashMap<DamageType, Double> map) {
		return Component.text(type.getDisplay() + ": ",NamedTextColor.YELLOW)
				.append(Component.text(df.format(map.get(type)), NamedTextColor.WHITE));
	}
	private Component getDamageTakenStatPiece(Mob mob, DamageType type, HashMap<DamageType, Double> map) {
		if (mob == null) return getStatPiece(type, map);
		return mob.getDisplay().append(Component.text(" - ", NamedTextColor.GRAY)).append(Component.text(type.getDisplay() + ": ",NamedTextColor.YELLOW))
				.append(Component.text(df.format(map.get(type)), NamedTextColor.WHITE));
	}
	
	private Component getStatPiece(String display, double stat) {
		return Component.text(display + ": ", NamedTextColor.YELLOW).append(Component.text(df.format(stat), NamedTextColor.WHITE));
	}
}
