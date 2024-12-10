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
	private HashMap<DamageType, Double> damageDealt = new HashMap<DamageType, Double>(),
			damageTaken = new HashMap<DamageType, Double>();
	private HashMap<StatusType, Integer> statusesApplied = new HashMap<StatusType, Integer>();
	private HashMap<StatTracker, Double> buffStats = new HashMap<StatTracker, Double>();
	private double healingGiven, healingReceived, selfHealing, damageShielded, defenseBuffed;
	private int deaths, revives;

	// Etc stats
	private double evadeMitigated, sanctifiedHealing, injuryMitigated;

	public void addEvadeMitigated(double evadeMitigated) {
		this.evadeMitigated += evadeMitigated;
	}

	public void addInjuryMitigated(double injuryMitigated) {
		this.injuryMitigated += injuryMitigated;
	}

	public void addSanctifiedHealing(double sanctifiedHealing) {
		this.sanctifiedHealing += sanctifiedHealing;
	}

	public FightStatistics(PlayerFightData data) {
		this.data = data;
	}
	
	public void addDamageDealt(DamageType type, double amount) {
		double amt = damageDealt.getOrDefault(type, 0D);
		damageDealt.put(type, amt + amount);
	}
	
	public void addDamageTaken(DamageType type, double amount) {
		double amt = damageTaken.getOrDefault(type, 0D);
		damageTaken.put(type, amt + amount);
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
					+ "[<yellow>Name</yellow> (<green>HP</green>) - <red>Damage Dealt </red>/ <dark_red>Damage Received "
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

	public HashMap<DamageType, Double> getDamageTaken() {
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
		return Component.text(data.getSessionData().getData().getDisplay(), NamedTextColor.YELLOW)
		.append(Component.text(" (", NamedTextColor.GRAY))
		.append(Component.text(df.format(data.getPlayer().getHealth()), NamedTextColor.GREEN))
		.append(Component.text(")", NamedTextColor.GRAY))
		.hoverEvent(HoverEvent.showText(getNameHoverComponent()))
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
		for (Entry<DamageType, Double> ent : damageTaken.entrySet()) {
			totalDamageTaken += ent.getValue();
			hover = hover.appendNewline().append(getStatPiece(ent.getKey(), damageTaken));
		}
			
		taken = taken.append(Component.text(df.format(totalDamageTaken - damageShielded) + "♥", NamedTextColor.DARK_RED));
		if (totalDamageTaken > 0) taken.hoverEvent(hover);

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
		return Component.text(df.format(score), NamedTextColor.GOLD).hoverEvent(hover);
	}

	public Component getBuffComponent() {
		double score = 0;
		Component hover = Component.text("Impact of buffs:", NamedTextColor.GRAY);
		for (Entry<StatTracker, Double> ent : buffStats.entrySet()) {
			StatTracker stat = ent.getKey();
			double amt = ent.getValue();
			hover = hover.appendNewline().append(stat.getDisplay().append(Component.text(": " + df.format(amt), NamedTextColor.WHITE)));
			score += buffStats.get(stat);
		}
		return Component.text(df.format(score), NamedTextColor.BLUE).hoverEvent(hover);
	}

	private Component getStatusStat(StatusType type) {
		switch (type) {
		case EVADE:
			return type.ctag.append(Component.text(" Mitigation: " + df.format(evadeMitigated), NamedTextColor.WHITE));
		case INJURY:
			return type.ctag.append(Component.text(" Mitigation: " + df.format(injuryMitigated), NamedTextColor.WHITE));
		case SANCTIFIED:
			return type.ctag.append(Component.text(" Healing: " + df.format(sanctifiedHealing), NamedTextColor.WHITE));
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
		Component cmp = Component.text("");
		cmp = appendIfNotEmpty(cmp, "Ally Healing", healingGiven);
		cmp = appendIfNotEmpty(cmp, "Self Healing", selfHealing);
		cmp = appendIfNotEmpty(cmp, "Healing Received", healingReceived);
		cmp = appendIfNotEmpty(cmp, "Deaths", deaths);
		cmp = appendIfNotEmpty(cmp, "Revives", revives);
		return cmp;
	}

	private Component appendIfNotEmpty(Component base, String display, double stat) {
		if (stat != 0) {
			return base.append(getStatPiece(display, stat)).appendNewline();
		}
		return base;
	}

	private Component appendIfNotEmpty(Component base, String display, int stat) {
		if (stat != 0) {
			return base.append(getStatPiece(display, stat)).appendNewline();
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
	
	private Component getStatPiece(String display, double stat) {
		return Component.text(display + ": ", NamedTextColor.YELLOW).append(Component.text(df.format(stat), NamedTextColor.WHITE));
	}
}
