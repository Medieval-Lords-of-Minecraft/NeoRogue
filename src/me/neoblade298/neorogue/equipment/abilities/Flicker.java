package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class Flicker extends Equipment {
	private static final String ID = "flicker";
	private int damage;
	
	public Flicker(boolean isUpgraded) {
		super(ID, "Flicker", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 25, 15, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		damage = isUpgraded ? 100 : 70;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		FlickerInstance inst = new FlickerInstance(p, this, slot, es);
		data.addTrigger(id, bind, inst);
		data.addTrigger(ID, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			if (!inst.active) return TriggerResult.keep();
			DealtDamageEvent ev = (DealtDamageEvent) in;
			LivingEntity trg = ev.getTarget();
			if (!inst.marks.containsKey(trg)) {
				inst.marks.put(trg, inst.marks.getOrDefault(trg, 0) + 1);
			}
			else {
				inst.marks.put(trg, 1);
			}
			return TriggerResult.keep();
		});
	}
	
	private class FlickerInstance extends EquipmentInstance {
		private Location loc;
		private boolean active = false;
		private HashMap<LivingEntity, Integer> marks = new HashMap<LivingEntity, Integer>();
		public FlickerInstance(Player p, Equipment eq, int slot, EquipSlot es) {
			super(p, eq, slot, es);
			action = (pdata, in) -> {
				active = true;
				Sounds.equip.play(p, p);
				loc = p.getLocation();
				pdata.addTask(new BukkitRunnable() {
					public void run() {
						p.teleport(loc);
						Sounds.teleport.play(p, p);
						active = false;
						for (Entry<LivingEntity, Integer> ent : marks.entrySet()) {
							if (ent.getValue() < 1) continue;
							DamageMeta dm = new DamageMeta(pdata, damage * ent.getValue(), DamageType.DARK);
							FightInstance.dealDamage(dm, ent.getKey());
						}
					}
				}.runTaskLater(NeoRogue.inst(), 60L));
				return TriggerResult.keep();
			};
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"On cast, drop a marker on the ground. It stays active <white>3</white> seconds."
				+ " Dealing damage while it's active marks enemies up to 5x each. After it"
				+ " deactivates, teleport back to the mark and deal " + GlossaryTag.DARK.tag(this, damage, true)
				+ " damage per mark.");
	}
}
