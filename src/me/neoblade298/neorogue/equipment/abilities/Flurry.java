package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.WeaponSwingEvent;

public class Flurry extends Equipment {
	private static final String ID = "Flurry";
	private int cutoff;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	
	public Flurry(boolean isUpgraded) {
		super(ID, "Flurry", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 25, 10, 0));
		cutoff = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Player p = data.getPlayer();
			Sounds.equip.play(p, p);
			pc.play(p, p);
			data.addTrigger(id, Trigger.WEAPON_SWING, new FlurryInstance(id, p, this));
			return TriggerResult.keep();
		}));
	}
	
	private class FlurryInstance extends PriorityAction {
		int count = 0;
		public FlurryInstance(String id, Player p, Equipment eq) {
			super(id);
			action = (pdata, in) -> {
				if (++count > cutoff) return TriggerResult.remove();
				WeaponSwingEvent ev = (WeaponSwingEvent) in;
				ev.getAttackSpeedBuffList().add(new Buff(pdata, 0, 1, BuffStatTracker.ignored(eq)));
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD,
				"On cast, your next <yellow>" + cutoff + "</yellow> basic attacks have their attack speed increased by <white>1.0</white>.");
	}
}
