package me.neoblade298.neorogue.session.fight.trigger.event;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.session.fight.buff.BuffList;

public class WeaponSwingEvent {
	private Equipment weapon;
	private double attackSpeed;
	private BuffList buff = new BuffList();
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
	public BuffList getAttackSpeedBuffList() {
		return buff;
	}
}
