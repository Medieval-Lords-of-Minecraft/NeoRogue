package me.neoblade298.neorogue.session.fight;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class FightStatistics {
	private static final DecimalFormat df = new DecimalFormat("#.##");
	private static final Component separator = Component.text(" / ", NamedTextColor.GRAY).hoverEvent(null);
	private PlayerFightData data;
	private HashMap<DamageType, Double> damageDealt = new HashMap<DamageType, Double>();
	private HashMap<String, HashMap<DamageType, Double>> damageTaken = new HashMap<String, HashMap<DamageType, Double>>();
	private HashMap<StatusType, Integer> statusesApplied = new HashMap<StatusType, Integer>();
	private HashMap<StatTracker, Double> buffStats = new HashMap<StatTracker, Double>();
	private double healingGiven, healingReceived, selfHealing, damageShielded, defenseBuffed, damageBarriered, damageNullified;
	private int deaths, revives;

	// Etc stats
	private double evadeMitigated, sanctifiedShielding, injuryMitigated;

	public void addEvadeMitigated(double evadeMitigated) {
		this.evadeMitigated += evadeMitigated;
	}

	public void addDamageNullified(double damageNullified) {
		this.damageNullified += damageNullified;
	}

	public void addDamageBarriered(double damageBarriered) {
		this.damageBarriered += damageBarriered;
	}

	public void addInjuryMitigated(double injuryMitigated) {
		this.injuryMitigated += injuryMitigated;
	}

	public void addSanctifiedShielding(double addSanctifiedShielding) {
		this.sanctifiedShielding += sanctifiedShielding;
	}

	public FightStatistics(PlayerFightData data) {
		this.data = data;
	}
	
	public void addDamageDealt(DamageType type, double amount) {
		double amt = damageDealt.getOrDefault(type, 0D);
		damageDealt.put(type, amt + amount);
	}
	
	public void addDamageTaken(String mobId, DamageType type, double amount) {
		HashMap<DamageType, Double> mobMap = damageTaken.getOrDefault(mobId, new HashMap<DamageType, Double>());
		double amt = mobMap.getOrDefault(type, 0D);
		mobMap.put(type, amt + amount);
		damageTaken.put(mobId, mobMap);
	}
	
	public void addDamageShielded(double amount) {
		this.damageShielded += amount;
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

	public static Component getStatsHeader(String timer) {
		return SharedUtil.color(
			"<gray>Fight Statistics [<white>" + timer + "</white>] (Hover for more info!)\n=====\n"
					+ "[<yellow>Name</yellow> (<green>HP</green>) - <red>Damage Dealt </red>/ <dark_red>Taken "
					+ "</dark_red>/ <gold>Statuses</gold> / <blue>Buffs</blue>]"
		);
	}

	public void addStatusApplied(StatusType type, int amount) {
		int curr = statusesApplied.getOrDefault(type, 0);
		statusesApplied.put(type, curr + amount);
	}

	public HashMap<DamageType, Double> getDamageDealt() {
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
	
	public Component getStatLine() {
		Component line = Component.text("").append(getNameplateComponent())
				.append(getDamageDealtComponent()).append(separator)
				.append(getDamageTakenComponent()).append(separator)
				.append(getStatusComponent()).append(separator)
				.append(getBuffComponent());
		return line;
	}

	public Component getNameplateComponent() {
		Component nameHover = getNameHoverComponent();
		return Component.text(data.getSessionData().getData().getDisplay(), NamedTextColor.YELLOW)
		.append(Component.text(" (", NamedTextColor.GRAY))
		.append(Component.text(df.format(data.getPlayer().getHealth()), NamedTextColor.GREEN))
		.append(Component.text(")", NamedTextColor.GRAY))
		.hoverEvent(nameHover.children().isEmpty() ? null : HoverEvent.showText(getNameHoverComponent()))
		.append(Component.text(" - ", NamedTextColor.GRAY).hoverEvent(null));
	}
	
	public Component getDamageDealtComponent() {
		NamedTextColor color = NamedTextColor.RED;
		if (damageDealt.isEmpty()) {
			return Component.text("0", color).hoverEvent(null);
		}
		else {
			Component hover = Component.text("Damage dealt post buff and mitigation:", NamedTextColor.GRAY);
			double total = 0;
			for (Entry<DamageType, Double> ent : damageDealt.entrySet()) {
				hover = hover.appendNewline().append(getStatPiece(ent.getKey(), damageDealt));
				total += ent.getValue();
			}
			return Component.text(df.format(total), color).hoverEvent(HoverEvent.showText(hover));
		}
	}

	public Component getDamageTakenComponent() {
		Component taken = Component.text("");
		if (damageShielded > 0) {
			taken = taken.append(Component.text(df.format(damageShielded) + "♥", NamedTextColor.YELLOW))
				.append(Component.text(", ", NamedTextColor.GRAY));
		}
		
		Component hover = Component.text("Damage taken post buff and mitigation:", NamedTextColor.GRAY);
		double totalDamageTaken = 0;
		for (Entry<String, HashMap<DamageType, Double>> mobMap : damageTaken.entrySet()) {
			Mob mob = Mob.get(mobMap.getKey());
			for (Entry<DamageType, Double> ent : mobMap.getValue().entrySet()) {
				totalDamageTaken += ent.getValue();
				hover = hover.appendNewline().append(getDamageTakenStatPiece(mob, ent.getKey(), mobMap.getValue()));
			}
		}
			
		taken = taken.append(Component.text(df.format(totalDamageTaken - damageShielded) + "♥", NamedTextColor.DARK_RED));
		if (totalDamageTaken > 0) taken = taken.hoverEvent(hover);

		return taken;
	}

	public Component getStatusComponent() {
		double score = 0;
		Component hover = Component.text("Statuses applied:", NamedTextColor.GRAY);
		for (StatusType type : statusesApplied.keySet()) {
			if (type.hidden) continue;
			if (!statusesApplied.containsKey(type)) continue;

			hover = hover.appendNewline().append(type.ctag.append(Component.text(" Applied: " + df.format(statusesApplied.get(type)), NamedTextColor.WHITE)));
			score += statusesApplied.get(type);
			Component extra = getStatusStat(type);
			if (extra != null) {
				hover = hover.appendNewline().append(extra);
			}
		}
		score += evadeMitigated + injuryMitigated + sanctifiedShielding;
		Component cmp = Component.text(df.format(score), NamedTextColor.GOLD);
		if (score > 0) cmp = cmp.hoverEvent(hover);
		return cmp;
	}

	public Component getBuffComponent() {
		double score = 0;
		Component hover = Component.text("Impact of buffs:", NamedTextColor.GRAY);
		for (Entry<StatTracker, Double> ent : buffStats.entrySet()) {
			StatTracker stat = ent.getKey();
			if (stat.isIgnored()) continue;
			double amt = stat.isInverted() ? -ent.getValue() : ent.getValue();
			hover = hover.appendNewline().append(stat.getDisplay().append(Component.text(": " + df.format(amt), NamedTextColor.WHITE)));
			score += amt;
		}
		Component cmp = Component.text(df.format(score), NamedTextColor.BLUE).hoverEvent(hover);
		if (score > 0) cmp = cmp.hoverEvent(hover);
		return cmp;
	}

	private Component getStatusStat(StatusType type) {
		switch (type) {
		case EVADE:
			return type.ctag.append(Component.text(" Mitigation: " + df.format(evadeMitigated), NamedTextColor.WHITE));
		case INJURY:
			return type.ctag.append(Component.text(" Mitigation: " + df.format(injuryMitigated), NamedTextColor.WHITE));
		case SANCTIFIED:
			return type.ctag.append(Component.text(" Healing: " + df.format(sanctifiedShielding), NamedTextColor.WHITE));
		default:
			return null;
		}
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
		cmp = appendIfNotEmpty(cmp, "Damage Nullified", damageNullified);
		cmp = appendIfNotEmpty(cmp, "Damage Barriered", damageBarriered);
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
