package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class GraniteShield extends Equipment {
	private static final int HEAL_COUNT = 3;
	private ParticleContainer pc = new ParticleContainer(Particle.VILLAGER_HAPPY).count(15).spread(0.5, 0.5);
	private int heal, concuss;
	
	public GraniteShield(boolean isUpgraded) {
		super("graniteShield", "Granite Shield", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		
		heal = 3;
		concuss = isUpgraded ? 9 : 6;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		SturdyInstance inst = new SturdyInstance(p, this, slot, es);
		data.addTrigger(id, bind, inst);
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			if (inst.getCount() < HEAL_COUNT) return TriggerResult.keep();
			ReceivedDamageEvent ev = (ReceivedDamageEvent) in;
			ev.getMeta().getOwner().applyStatus(StatusType.CONCUSSED, p.getUniqueId(), concuss, -1);
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, in) -> {
			inst.resetCount();
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GRANITE_SLAB,
				"Passive. Heal for <white>" + heal + "</white> and apply <yellow>" + concuss + " " +
						GlossaryTag.CONCUSSED.tag(this) + " to enemies that damage you after <white>3</white> consecutive seconds "
								+ "of keeping your shield raised.");
	}
	
	private class SturdyInstance extends EquipmentInstance {
		private int count = 0;
		public SturdyInstance(Player p, Equipment eq, int slot, EquipSlot es) {
			super(p, eq, slot, es);
			
			action = (pdata, inputs) -> {
				if (++count < HEAL_COUNT) return TriggerResult.keep();
				pc.spawn(p);
				Util.playSound(p, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1F, 1F, false);
				pdata.addHealth(heal);
				count = 0;
				return TriggerResult.keep();
			};
		}
		
		public int getCount() {
			return count;
		}
		
		public void resetCount() {
			count = 0;
		}
	}
}
