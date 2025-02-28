package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class PoisonPowder extends Equipment {
	private static final String ID = "poisonPowder";
	private int amount;
	
	public PoisonPowder(boolean isUpgraded) {
		super(ID, "Poison Powder", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(0, 0, 15, 0));
		amount = isUpgraded ? 60 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		data.addTrigger(ID, Trigger.RIGHT_CLICK, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			inst.setCount(1);
			Sounds.equip.play(p, p);
			data.addTask(new BukkitRunnable() {
				public void run() {
					inst.setCount(0);
				}
			}.runTaskLater(NeoRogue.inst(), 100L));
			return TriggerResult.keep();
		}));
		
		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			if (inst.getCount() == 0) return TriggerResult.keep();
			DealtDamageEvent ev = (DealtDamageEvent) in;
			FightInstance.applyStatus(ev.getTarget(), StatusType.POISON, data, amount, -1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GREEN_DYE, "Right click to apply " + GlossaryTag.POISON.tag(this, amount, true) + " to enemies you"
				+ " deal " + GlossaryTag.PHYSICAL.tag(this) + " damage to for the next <white>5</white> seconds.");
	}
}
