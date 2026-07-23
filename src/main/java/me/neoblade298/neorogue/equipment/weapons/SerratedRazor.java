package me.neoblade298.neorogue.equipment.weapons;
import org.bukkit.Material;
import org.bukkit.Sound;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class SerratedRazor extends Equipment {
	private static final String ID = "SerratedRazor";
	private static int base = 40;
	private int bonus;
	
	public SerratedRazor(boolean isUpgraded) {
		super(ID, "Serrated Razor", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(base, 3, 0, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		bonus = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, new RazorInstance(data, sessionEq, slot, es));
	}

	private class RazorInstance extends EquipmentInstance {
		private int count = 0;
		private long windowStart = 0;

		public RazorInstance(PlayerFightData data, SessionEquipment sessionEq, int slot, EquipSlot es) {
			super(data, sessionEq, slot, es);
			action = (data2, in) -> {
				LeftClickHitEvent ev = (LeftClickHitEvent) in;
				long now = System.currentTimeMillis();
				if (count == 0 || now - windowStart > 1500L) {
					weaponSwingAndDamage(p, data, ev.getTarget());
					count = 1;
					windowStart = now;
				} else {
					count++;
					if (count >= 3) {
						FightData fd = FightInstance.getFightData(ev.getTarget());
						boolean canBonus = fd.hasStatus(StatusType.POISON) || fd.hasStatus(StatusType.INSANITY);
						weaponSwingAndDamage(p, data, ev.getTarget(), base + (canBonus ? bonus : 0));
						data.setBasicAttackCooldown(EquipSlot.HOTBAR, 1000L);
						Sounds.extinguish.play(p, p);
						this.setCooldown(1);
						count = 0;
					} else {
						weaponSwingAndDamage(p, data, ev.getTarget());
					}
				}
				return TriggerResult.keep();
			};
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WOODEN_HOE, "If you attack " + DescUtil.val(3) + " times within " + DescUtil.val("1.5s") + ", deal an additional " + DescUtil.val(bonus) + ""
				+ " damage if the target has " + GlossaryTag.POISON.tag(this) + " or " + GlossaryTag.INSANITY.tag(this) + ", and "
						+ "set your attack cooldown to " + DescUtil.val("1s") + ". Otherwise, the combo resets.");
	}
}
