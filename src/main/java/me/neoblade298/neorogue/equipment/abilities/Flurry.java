package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
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
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(5, 15, 8, 0));
		cutoff = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, inputs) -> {
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
				"On cast, your next " + DescUtil.yellow(cutoff) + " basic attacks have their attack speed increased by " + DescUtil.white("1.0") + ".");
	}
}
