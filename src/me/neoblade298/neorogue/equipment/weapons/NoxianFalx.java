package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class NoxianFalx extends Equipment {
	private static final String ID = "NoxianFalx";
	private int stacks;
	
	public NoxianFalx(boolean isUpgraded) {
		super(ID, "Noxian Falx", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 65 : 55, 1.25, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		stacks = isUpgraded ? 60 : 40;
		properties.addUpgrades(PropertyType.DAMAGE);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, data, ev.getTarget());
			
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd != null) {
				// Apply stacks if enemy already has the status
				if (fd.hasStatus(StatusType.INSANITY)) {
					FightInstance.applyStatus(ev.getTarget(), StatusType.INSANITY, data, stacks, -1);
				}
				if (fd.hasStatus(StatusType.ELECTRIFIED)) {
					FightInstance.applyStatus(ev.getTarget(), StatusType.ELECTRIFIED, data, stacks, -1);
				}
				if (fd.hasStatus(StatusType.POISON)) {
					FightInstance.applyStatus(ev.getTarget(), StatusType.POISON, data, stacks, -1);
				}
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHERITE_SWORD,
				"Applies " + GlossaryTag.INSANITY.tag(this, stacks, true) + ", " + 
				GlossaryTag.ELECTRIFIED.tag(this, stacks, true) + ", and " + 
				GlossaryTag.POISON.tag(this, stacks, true) + " on hit if the enemy already has the respective status.");
	}
}
