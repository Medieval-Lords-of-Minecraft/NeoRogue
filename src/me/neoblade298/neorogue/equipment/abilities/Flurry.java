package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.WeaponSwingEvent;

public class Flurry extends Equipment {
	private int cutoff;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	
	public Flurry(boolean isUpgraded) {
		super("flurry", "Flurry", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, isUpgraded ? 5 : 7, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		cutoff = isUpgraded ? 6 : 4;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, inputs) -> {
			Sounds.equip.play(p, p);
			pc.play(p, p);
			data.addTrigger(id, Trigger.WEAPON_SWING, new FlurryInstance(id, p));
			return TriggerResult.keep();
		}));
	}
	
	private class FlurryInstance extends PriorityAction {
		int count = 0;
		public FlurryInstance(String id, Player p) {
			super(id);
			action = (pdata, in) -> {
				if (++count > cutoff) return TriggerResult.remove();
				WeaponSwingEvent ev = (WeaponSwingEvent) in;
				ev.getAttackSpeedBuff().addMultiplier(p.getUniqueId(), 1);
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD,
				"On cast, your next <yellow>" + cutoff + "</yellow> basic attacks have their attack cooldown reduced by <white>50%</white>.");
	}
}
