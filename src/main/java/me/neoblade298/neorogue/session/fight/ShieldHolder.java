package me.neoblade298.neorogue.session.fight;

import java.util.Collections;
import java.util.LinkedList;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ShieldHolder {
	private LinkedList<Shield> shields = new LinkedList<Shield>();
	private double max;
	private double amount;
	private FightData data;

	public ShieldHolder(FightData data) {
		this.data = data;
		this.max = 0;
		this.amount = 0;
	}
	
	public boolean isEmpty() {
		return shields.isEmpty() || amount == 0;
	}
	
	public void addShield(Shield shield) {
		shield.initialize(data);
		shields.add(shield);
		Collections.sort(shields);
		if (amount <= 0) {
			max = shield.getTotal();
			amount = shield.getTotal();
		}
		else if (amount + shield.getTotal() > max) {
			max = amount + shield.getTotal();
			amount += shield.getTotal();
		}
		else {
			amount += shield.getTotal();
		}
		update();
	}
	
	public double useShields(double damage) {
		while (!shields.isEmpty() && damage > 0) {
			Shield curr = shields.getFirst();
			damage = curr.useShield(damage);

			// Move to next shield if this one is used up
			if (!curr.isUsable()) {
				shields.pollFirst();
			}
		}
		update();
		return damage;
	}
	
	protected void update() {
		double pct = 0;
		Attributable entity = (Attributable) data.getEntity();
		if (max > entity.getAttribute(Attribute.MAX_HEALTH).getValue()) {
			pct = amount / max;
		}
		else {
			pct = amount / entity.getAttribute(Attribute.MAX_HEALTH).getValue();
		}
		pct = Math.max(0, pct);
		double absorb = Math.round(pct * 20);
		if (absorb < 1 && pct > 0) absorb = 1;
		
		if (data.getEntity() instanceof Player) {
			Player p = (Player) data.getEntity();
			if (!p.hasPotionEffect(PotionEffectType.ABSORPTION)) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, PotionEffect.INFINITE_DURATION, 4, false, false, false));
			}
		}
		data.getEntity().setAbsorptionAmount(absorb);
		if (data instanceof PlayerFightData) {
			((PlayerFightData) data).updateActionBar();
		}
	}
	
	public double getMax() {
		return max;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public FightData getData() {
		return data;
	}
	
	public void subtractShields(double difference) {
		this.amount -= difference;
	}

	public void removeShield(Shield shield) {
		this.amount -= shield.getAmount();
		shields.remove(shield);
	}
}
