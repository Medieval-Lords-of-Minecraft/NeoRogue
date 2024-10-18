package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class EnergizedRazor extends Equipment {
	private static final String ID = "energizedRazor";
	private static int base = 18;
	private int res;
	
	public EnergizedRazor(boolean isUpgraded) {
		super(ID, "Energized Razor", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(base, 3, 0, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		res = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, new RazorInstance(data, this, slot, es));
	}

	private class RazorInstance extends EquipmentInstance {
		private int count = 0;

		public RazorInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			action = (data2, in) -> {
				LeftClickHitEvent ev = (LeftClickHitEvent) in;
				weaponSwingAndDamage(p, data, ev.getTarget());
				if (++count >= 3) {
					data.setBasicAttackCooldown(EquipSlot.HOTBAR, 3000L);
					data.addStamina(res);
					Sounds.extinguish.play(p, p);
					count = 0;
					this.setCooldown(3);
				}
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WOODEN_HOE, "Every <white>3</white> basic attacks, gain <yellow>" + res + "</yellow> stamina"
						+ " and your attack cooldown is set to <white>3</white> seconds.");
	}
}
