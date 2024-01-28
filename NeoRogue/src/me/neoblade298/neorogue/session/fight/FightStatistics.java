package me.neoblade298.neorogue.session.fight;

import java.util.HashMap;

public class FightStatistics {
	private HashMap<DamageType, Double> damageDealt = new HashMap<DamageType, Double>(),
			damageTaken = new HashMap<DamageType, Double>(),
			damageMitigated = new HashMap<DamageType, Double>(),
			damageBuffed = new HashMap<DamageType, Double>();
	private double healingGiven, healingReceived, selfHealing, damageBarriered, damageShielded, defenseBuffed;
	private int deaths, revives;
	
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
}
