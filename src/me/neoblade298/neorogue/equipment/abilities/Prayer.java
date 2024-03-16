package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Prayer extends Equipment {
	private static final String ID = "prayer";
	private int heal;
	private double healPct;
	
	public Prayer(boolean isUpgraded) {
		super(ID, "Prayer", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		heal = isUpgraded ? 8 : 5;
		healPct = heal * 0.01;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(p, this, slot, es);
		inst.setAction((pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.getStatusId().equals(StatusType.SANCTIFIED.name())) return TriggerResult.keep();
			inst.addCount(ev.getStacks());
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.APPLY_STATUS, inst);
		
		data.addTrigger(id, Trigger.WIN_FIGHT, (pdata, in) -> {
			for (UUID uuid : data.getInstance().getParty()) {
				Player trg = Bukkit.getPlayer(uuid);
				if (trg == null) continue;
				FightInstance.giveHeal(p, inst.getCount() * healPct, trg);
			}
			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				"Upon winning a fight, add up all " + GlossaryTag.SANCTIFIED.tag(this) + " stacks you applied during the fight, and heal"
				+ " the party for <yellow>" + heal + "%</yellow> of that, split evenly amonst party members.");
	}
}
