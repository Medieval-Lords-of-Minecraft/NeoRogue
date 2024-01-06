package me.neoblade298.neorogue.session.fight.trigger;

public enum Trigger {
	SHIFT_RCLICK,
	SHIFT_DROP,
	SHIFT_SWAP,
	DROP,
	SWAP,
	UP_RCLICK,
	DOWN_RCLICK,
	HOTBAR_1,
	HOTBAR_2,
	HOTBAR_3,
	HOTBAR_4,
	HOTBAR_5,
	HOTBAR_6,
	HOTBAR_7,
	HOTBAR_8,
	HOTBAR_9,
	BASIC_ATTACK, // { LivingEntity target, double damage, double knockback, DamageType, Weapon }
	CAST_USABLE, // { EquipmentInstance being used } 
	LEFT_CLICK,
	LEFT_CLICK_NO_HIT,
	LEFT_CLICK_HIT,
	RIGHT_CLICK,
	RIGHT_CLICK_HIT,
	RAISE_SHIELD,
	SHIELD_TICK, // Ticks while you have your shield up
	LOWER_SHIELD,
	RECEIVED_DAMAGE_BARRIER, // FightData projectile owner, double damageBefore, double damageAfter
	DEALT_DAMAGE,
	WIN_FIGHT,
	APPLY_STATUS, // FightData target, Status ID, stacks, duration
	RECEIVED_DAMAGE; // Cancellable
	
	private boolean isSlotDependent = this.name().startsWith("LEFT_CLICK");
	
	public boolean isSlotDependent() {
		return isSlotDependent;
	}
	
	public static Trigger getFromHotbarSlot(int hotbar) {
		switch (hotbar) {
		case 0: return Trigger.HOTBAR_1;
		case 1: return Trigger.HOTBAR_2;
		case 2: return Trigger.HOTBAR_3;
		case 3: return Trigger.HOTBAR_4;
		case 4: return Trigger.HOTBAR_5;
		case 5: return Trigger.HOTBAR_6;
		case 6: return Trigger.HOTBAR_7;
		case 7: return Trigger.HOTBAR_8;
		case 8: return Trigger.HOTBAR_9;
		default: return null;
		}
	}
	
	public static int toHotbarSlot(Trigger tr) {
		switch (tr) {
		case HOTBAR_1: return 0;
		case HOTBAR_2: return 1;
		case HOTBAR_3: return 2;
		case HOTBAR_4: return 3;
		case HOTBAR_5: return 4;
		case HOTBAR_6: return 5;
		case HOTBAR_7: return 6;
		case HOTBAR_8: return 7;
		case HOTBAR_9: return 8;
		default: return -1;
		}
	}
}
