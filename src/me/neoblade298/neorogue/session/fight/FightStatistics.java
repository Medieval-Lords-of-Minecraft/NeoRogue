package me.neoblade298.neorogue.session.fight;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;

import me.neoblade298.neocore.shared.util.SharedUtil;
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
	private HashMap<StatusType, Integer> statuses = new HashMap<StatusType, Integer>();
	private double healingGiven, healingReceived, selfHealing, damageBarriered, damageShielded, defenseBuffed;
	private int deaths, revives;

	// Status stats
	private double burnDamage, concussedMitigated, electrifiedDamage,
		evadeMitigated, frostMitigated, frostDamage, injuryMitigated, insanityDamage,
		poisonDamage, reflectDamage, sanctifiedHealing, thornsDamage;
	
	
	public void addBurnDamage(double burnDamage) {
		this.burnDamage += burnDamage;
	}

	public void addConcussedMitigated(double concussedMitigated) {
		this.concussedMitigated += concussedMitigated;
	}

	public void addElectrifiedDamage(double electrifiedDamage) {
		this.electrifiedDamage += electrifiedDamage;
	}

	public void addEvadeMitigated(double evadeMitigated) {
		this.evadeMitigated += evadeMitigated;
	}

	public void addFrostDamage(double frostDamage) {
		this.frostDamage += frostDamage;
	}

	public void addFrostMitigated(double frostMitigated) {
		this.frostMitigated += frostMitigated;
	}

	public void addInjuryMitigated(double injuryMitigated) {
		this.injuryMitigated += injuryMitigated;
	}

	public void addInsanityDamage(double insanityDamage) {
		this.insanityDamage += insanityDamage;
	}

	public void addPoisonDamage(double poisonDamage) {
		this.poisonDamage += poisonDamage;
	}

	public void addReflectDamage(double reflectDamage) {
		this.reflectDamage += reflectDamage;
	}

	public void addSanctifiedHealing(double sanctifiedHealing) {
		this.sanctifiedHealing += sanctifiedHealing;
	}

	public void addThornsDamage(double thornsDamage) {
		this.thornsDamage += thornsDamage;
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
	
	public void addDamageBarriered(double amount) {
		this.damageBarriered += amount;
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

	public static Component getStatsHeader(String timer) {
		return SharedUtil.color(
			"<gray>Fight Statistics [<white>" + timer + "</white>] (Hover for more info!)\n=====\n"
					+ "[<yellow>Name</yellow> (<green>HP</green>) - <red>Damage Dealt </red>/ <dark_red>Damage Received "
					+ "</dark_red>/ <gold>Status Impact</gold>]"
		);
	}

	public void addStatusApplied(StatusType type, int amount) {
		int curr = statuses.getOrDefault(type, 0);
		statuses.put(type, curr + amount);
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

	public double getDamageBarriered() {
		return damageBarriered;
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
				.append(getStatusComponent());
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
		double score = calculateStatusValue();
		Component hover = Component.text("Statuses applied, with additional stats:", NamedTextColor.GRAY);
		for (StatusType type : statuses.keySet()) {
			if (type.hidden) continue;
			if (!statuses.containsKey(type)) continue;

			hover = hover.appendNewline().append(type.ctag.append(Component.text(" Applied: " + df.format(statuses.get(type)), NamedTextColor.WHITE)));
			score += statuses.get(type);
			Component extra = getStatusStat(type);
			if (extra != null) {
				hover = hover.appendNewline().append(extra);
			}
		}
		return Component.text(df.format(score), NamedTextColor.GOLD).hoverEvent(hover);
	}

	// Maybe add multipliers later
	private double calculateStatusValue() {
		return burnDamage + concussedMitigated + electrifiedDamage + evadeMitigated + frostMitigated + injuryMitigated
				+ insanityDamage + poisonDamage + reflectDamage + sanctifiedHealing + thornsDamage;
	}

	private Component getStatusStat(StatusType type) {
		switch (type) {
		case BURN:
			return type.ctag.append(Component.text(" Damage: " + df.format(burnDamage), NamedTextColor.WHITE));
		case CONCUSSED:
			return type.ctag.append(Component.text(" Mitigation: " + df.format(concussedMitigated), NamedTextColor.WHITE));
		case ELECTRIFIED:
			return type.ctag.append(Component.text(" Damage: " + df.format(electrifiedDamage), NamedTextColor.WHITE));
		case EVADE:
			return type.ctag.append(Component.text(" Mitigation: " + df.format(evadeMitigated), NamedTextColor.WHITE));
		case FROST:
			return type.ctag.append(Component.text(" Damage: " + df.format(frostDamage), NamedTextColor.WHITE)).appendNewline()
					.append(type.ctag.append(Component.text(" Mitigated: " + df.format(frostMitigated), NamedTextColor.WHITE)));
		case INJURY:
			return type.ctag.append(Component.text(" Mitigation: " + df.format(injuryMitigated), NamedTextColor.WHITE));
		case INSANITY:
			return type.ctag.append(Component.text(" Damage: " + df.format(insanityDamage), NamedTextColor.WHITE));
		case POISON:
			return type.ctag.append(Component.text(" Damage: " + df.format(poisonDamage), NamedTextColor.WHITE));
		case REFLECT:
			return type.ctag.append(Component.text(" Damage: " + df.format(reflectDamage), NamedTextColor.WHITE));
		case SANCTIFIED:
			return type.ctag.append(Component.text(" Healing: " + df.format(sanctifiedHealing), NamedTextColor.WHITE));
		case THORNS:
			return type.ctag.append(Component.text(" Damage: " + df.format(thornsDamage), NamedTextColor.WHITE));
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
		return getStatPiece("Ally Healing", healingGiven).appendNewline()
				.append(getStatPiece("Self Healing", selfHealing)).appendNewline()
				.append(getStatPiece("Healing Received", healingReceived)).appendNewline()
				.append(getStatPiece("Damage Barriered", damageBarriered)).appendNewline()
				.append(getStatPiece("Damage Shielded", damageShielded)).appendNewline()
				.append(getStatPiece("Defense Buffed", defenseBuffed)).appendNewline()
				.append(getStatPiece("Deaths", deaths)).appendNewline()
				.append(getStatPiece("Revives", revives));
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
