package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class HiddenRazor extends Equipment {
	private static final String ID = "hiddenRazor";
	private static int base = 60;
	
	public HiddenRazor(boolean isUpgraded) {
		super(ID, "Hidden Razor", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(base, 3, 0, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, new RazorInstance(id));
	}
	
	private class RazorInstance extends PriorityAction {
		private int count = 0;

		public RazorInstance(String id) {
			super(id);
			action = (data, in) -> {
				Player p = data.getPlayer();
				LeftClickHitEvent ev = (LeftClickHitEvent) in;
				if (!data.hasStatus(StatusType.STEALTH)) return TriggerResult.keep();
				weaponSwingAndDamage(p, data, ev.getTarget());
				if (++count >= 5) {
					data.applyStatus(StatusType.STEALTH, data, -1, -1);
					Sounds.extinguish.play(p, p);
					count = 0;
				}
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_HOE, "Requires " + GlossaryTag.STEALTH.tag(this) + " to be used. Reduces your "
				+ GlossaryTag.STEALTH.tag(this) + " by <white>1</white> every <white>5rd</white> hit.");
	}
}
