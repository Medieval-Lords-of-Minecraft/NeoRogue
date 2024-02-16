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
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.GrantShieldsEvent;

public class Thornguard extends Equipment {
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final int CUTOFF = 10;
	
	public Thornguard(boolean isUpgraded) {
		super("thornguard", "Thornguard", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(isUpgraded ? 20 : 50, isUpgraded ? 30 : 65, 0, 0));
		properties.addUpgrades(PropertyType.MANA_COST, PropertyType.STAMINA_COST);
		pc.count(50).spread(0.5, 0.5).speed(0.2);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new ThornguardInstance(p, this, slot, es));
	}
	
	private class ThornguardInstance extends EquipmentInstance {
		private int count = 0;
		public ThornguardInstance(Player p, Equipment eq, int slot, EquipSlot es) {
			super(p, eq, slot, es);
			action = (pdata, inputs) -> {
				pc.spawn(p);
				Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
				p.getInventory().setItem(slot, null);
				pdata.addTrigger(id, Trigger.RECEIVE_SHIELDS, (pdata2, inputs2) -> {
					GrantShieldsEvent ev = (GrantShieldsEvent) inputs2;
					count += ev.getShield().getAmount();
					System.out.println("Giving thorns " + (count / CUTOFF));
					pdata.applyStatus(StatusType.THORNS, p.getUniqueId(), count / CUTOFF, -1);
					count = count % CUTOFF;
					System.out.println("Leftover " + count);
					return TriggerResult.keep();
				});
				return TriggerResult.remove();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DEAD_BUSH,
				"On cast, for the rest of the fight, for every <white>" + CUTOFF +"</white> " + GlossaryTag.SHIELDS.tag + " that are granted to you, gain <white>1</white>"
						+ " stack of " + GlossaryTag.THORNS.tag(this) + ".");
	}
}
