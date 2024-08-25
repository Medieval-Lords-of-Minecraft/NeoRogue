package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class Razor extends Equipment {
	private static final String ID = "razor";
	
	public Razor(boolean isUpgraded) {
		super(ID, "Razor", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 10 : 7, 3, 0.2, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		properties.addUpgrades(PropertyType.DAMAGE);
	}

	@Override
	public void setupReforges() {
		addSelfReforge(SerratedRazor.get(), EnergizedRazor.get(), HiddenRazor.get());
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
				weaponSwingAndDamage(p, data, ev.getTarget());
				if (++count >= 5) {
					data.setBasicAttackCooldown(EquipSlot.HOTBAR, 3000L);
					Sounds.extinguish.play(p, p);
					count = 0;
				}
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WOODEN_HOE, "Every <white>5</white> basic attacks, your attack cooldown is set to <white>3</white> seconds.");
	}
}
