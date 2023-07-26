package me.neoblade298.neorogue.session.fights;

import java.util.HashMap;

public class FightStatistics {
	private HashMap<DamageType, Double> damageDealt = new HashMap<DamageType, Double>(),
			damageTaken = new HashMap<DamageType, Double>();
	private double healingGiven, healingReceived, damageMitigated, damageBuffed, damageBarriered, damageShielded, defenseBuffed;
	
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
	
	public void addDamageMitigated(double amount) {
		this.damageMitigated += amount;
	}
	
	public void addDamageBuffed(double amount) {
		this.damageBuffed += amount;
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
	
	public void addHealingReceived(double amount) {
		this.healingReceived += amount;
	}
}
