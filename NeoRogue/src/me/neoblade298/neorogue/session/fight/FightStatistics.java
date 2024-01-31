package me.neoblade298.neorogue.session.fight;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class FightStatistics {
	private PlayerFightData data;
	private HashMap<DamageType, Double> damageDealt = new HashMap<DamageType, Double>(),
			damageTaken = new HashMap<DamageType, Double>(),
			damageMitigated = new HashMap<DamageType, Double>(),
			damageBuffed = new HashMap<DamageType, Double>();
	private double healingGiven, healingReceived, selfHealing, damageBarriered, damageShielded, defenseBuffed;
	private int deaths, revives;
	
	private static final DecimalFormat df = new DecimalFormat("#.##");
	private static final Component separator = Component.text(" / ", NamedTextColor.GRAY).hoverEvent(null);
	
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
	
	public void addDamageMitigated(DamageType type, double amount) {
		double amt = damageMitigated.getOrDefault(type, 0D);
		damageMitigated.put(type, amt + amount);
	}
	
	public void addDamageBuffed(DamageType type, double amount) {
		double amt = damageBuffed.getOrDefault(type, 0D);
		damageBuffed.put(type, amt + amount);
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

	public HashMap<DamageType, Double> getDamageMitigated() {
		return damageMitigated;
	}

	public HashMap<DamageType, Double> getDamageBuffed() {
		return damageBuffed;
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
	
	public Component getStatLine(String name) {
		return Component.text("").append(Component.text(name, NamedTextColor.YELLOW)
		.hoverEvent(HoverEvent.showText(getNameHoverComponent())))
		.append(Component.text(" - ", NamedTextColor.GRAY).hoverEvent(null))
		.append(getTypedComponent(damageDealt, NamedTextColor.RED)).append(separator)
		.append(getTypedComponent(damageTaken, NamedTextColor.DARK_RED)).append(separator)
		.append(getTypedComponent(damageBuffed, NamedTextColor.BLUE)).append(separator)
		.append(getTypedComponent(damageMitigated, NamedTextColor.GOLD));
	}
	
	public Component getTypedComponent(HashMap<DamageType, Double> map, NamedTextColor color) {
		if (map.isEmpty()) {
			return Component.text("0", color).hoverEvent(null);
		}
		else {
			Component hover = null;
			double total = 0;
			for (Entry<DamageType, Double> ent : map.entrySet()) {
				if (hover == null) {
					hover = getStatPiece(ent.getKey(), map);
				}
				else {
					hover = hover.appendNewline().append(getStatPiece(ent.getKey(), map));
				}
				total += ent.getValue();
			}
			return Component.text("" + total, color).hoverEvent(HoverEvent.showText(hover));
		}
	}
	
	private Component getNameHoverComponent() {
		return getStatPiece("Health", data.getSessionData().getHealth()).appendNewline()
				.append(getStatPiece("Ally Healing", healingGiven)).appendNewline()
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
