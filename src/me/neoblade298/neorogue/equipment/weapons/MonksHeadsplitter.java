package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
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

public class MonksHeadsplitter extends Equipment {
	private static final String ID = "MonksHeadsplitter";
	private int bonus;
	
	public MonksHeadsplitter(boolean isUpgraded) {
		super(ID, "Monk's Headsplitter", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE ,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(0, 2, 65, 1, 0.4, DamageType.BLUNT, new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5F)));
		properties.addUpgrades(PropertyType.DAMAGE);
		bonus = isUpgraded ? 40 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			Player p = data.getPlayer();
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			boolean isConc = FightInstance.getFightData(ev.getTarget()).hasStatus(StatusType.CONCUSSED);
			weaponSwingAndDamage(p, data, ev.getTarget(), properties.get(PropertyType.DAMAGE) + (isConc ? bonus : 0));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STICK, "Deals an additional " + DescUtil.yellow(bonus) + " damage to " + GlossaryTag.CONCUSSED.tag(this) + " enemies.");
	}
}
