package me.neoblade298.neorogue.player;

public enum Trigger {
	SHIFT_RCLICK(true, false),
	SHIFT_DROP(true, false),
	SHIFT_SWAP(true, false),
	DROP(true, false),
	SWAP(true, false),
	UP_RCLICK(true, false),
	DOWN_RCLICK(true, false),
	HOTBAR_1(true, false),
	HOTBAR_2(true, false),
	HOTBAR_3(true, false),
	HOTBAR_4(true, false),
	HOTBAR_5(true, false),
	HOTBAR_6(true, false),
	HOTBAR_7(true, false),
	HOTBAR_8(true, false),
	HOTBAR_9(true, false),
	BASIC_ATTACK(false, false), // Only runs off the in-house basic attack event
	LEFT_CLICK_NO_HIT(false, true),
	LEFT_CLICK_HIT(false, true),
	RIGHT_CLICK(false, true),
	RAISE_SHIELD(false, false),
	SHIELD_TICK(false, false), // Ticks while you have your shield up
	LOWER_SHIELD(false, false),
	RECEIVED_DAMAGE_SHIELD(false, false),
	DEALT_DAMAGE(false, false),
	WIN_FIGHT(false, false),
	APPLY_STATUS(false, false), // Damageable target, Status ID, stacks, duration
	RECEIVED_DAMAGE(false, false); // Cancellable
	
	private boolean hasCooldownMsg, isSlotDependent;
	private Trigger(boolean hasCooldownMsg, boolean isSlotDependent) {
		this.hasCooldownMsg = hasCooldownMsg;
		this.isSlotDependent = isSlotDependent;
	}
	
	public boolean hasCooldownMessage() {
		return hasCooldownMsg;
	}
	
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
}
