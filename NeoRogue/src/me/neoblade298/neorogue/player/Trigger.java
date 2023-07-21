package me.neoblade298.neorogue.player;

public enum Trigger {
	SHIFT_LCLICK(true),
	SHIFT_RCLICK(true),
	SHIFT_DROP(true),
	SHIFT_SWAP(true),
	DROP(true),
	SWAP(true),
	UP_RCLICK(true),
	DOWN_RCLICK(true),
	HOTBAR_1(true),
	HOTBAR_2(true),
	HOTBAR_3(true),
	HOTBAR_4(true),
	HOTBAR_5(true),
	HOTBAR_6(true),
	HOTBAR_7(true),
	HOTBAR_8(true),
	HOTBAR_9(true),
	BASIC_ATTACK(false), // Only runs off the in-house basic attack event
	LEFT_CLICK_NO_HIT(false),
	LEFT_CLICK_HIT(false),
	RIGHT_CLICK(false),
	DEALT_DAMAGE(false),
	RECEIVED_DAMAGE(false);
	
	private boolean hasCooldownMsg;
	private Trigger(boolean hasCooldownMsg) {
		this.hasCooldownMsg = hasCooldownMsg;
	}
	
	public boolean hasCooldownMessage() {
		return hasCooldownMsg;
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
