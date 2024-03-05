package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.buff.Buff;

public class WeaponSwingEvent {
	private Equipment weapon;
	private double attackSpeed;
	private Buff buff = new Buff();
	public WeaponSwingEvent(Equipment weapon, double attackSpeed) {
		this.weapon = weapon;
		this.attackSpeed = attackSpeed;
	}
	public Equipment getWeapon() {
		return weapon;
	}
	public double getAttackSpeed() {
		return attackSpeed;
	}
	public Buff getAttackSpeedBuff() {
		return buff;
	}
}
