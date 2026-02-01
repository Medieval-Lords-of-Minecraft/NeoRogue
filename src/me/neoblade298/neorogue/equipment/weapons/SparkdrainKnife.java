package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
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

public class SparkdrainKnife extends Equipment {
	private static final String ID = "SparkdrainKnife";
	private static int elec, thres, shields;
	
	public SparkdrainKnife(boolean isUpgraded) {
		super(ID, "Sparkdrain Knife", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(40, 1, 0.2, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		elec = 30;
		thres = 100;
		shields = isUpgraded ? 8 : 5;
		
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
	data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			Player p = data.getPlayer();
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, data, ev.getTarget());
			FightData fd = FightInstance.getFightData(ev.getTarget());
			
			if (fd.getStatus(StatusType.ELECTRIFIED).getStacks() > thres) {
				data.addSimpleShield(p.getUniqueId(), shields, 60);
			}
			else {
				FightInstance.applyStatus(ev.getTarget(), StatusType.ELECTRIFIED, data, elec, -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "Every basic attack applies " + GlossaryTag.ELECTRIFIED.tag(this, elec, true) + ". If the "
				+ "target has over " + GlossaryTag.ELECTRIFIED.tag(this, thres, false) + ", grant " 
				+ GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>3s</white>] instead.");
	}
}
