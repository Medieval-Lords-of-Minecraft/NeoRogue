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
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class StoneMace extends Equipment {
	private static final String ID = "StoneMace";
	private double damage;
	private int conc;
	
	public StoneMace(boolean isUpgraded) {
		super(ID, "Stone Mace", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(isUpgraded ? 80 : 60, 0.75, 0.4, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_CRIT));
		properties.addUpgrades(PropertyType.DAMAGE);
		damage = properties.get(PropertyType.DAMAGE);
		conc = isUpgraded ? 12 : 8;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			FightInstance.applyStatus(ev.getTarget(), StatusType.CONCUSSED, p, conc, -1);
			weaponSwingAndDamage(p, pdata, ev.getTarget(), damage
					+ (FightInstance.getFightData(ev.getTarget()).getStatus(StatusType.CONCUSSED).getStacks() * 2));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SHOVEL, "Increases damage dealt by number of " + GlossaryTag.CONCUSSED.tag(this) + " "
				+ "stacks the enemy has multiplied by <white>2</white>. Also applies " + GlossaryTag.CONCUSSED.tag(this, conc, true) + " on basic attack.");
	}
}
