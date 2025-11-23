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
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.GrantShieldsEvent;

public class ThornGarden extends Equipment {
	private static final String ID = "ThornGarden";
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final int CUTOFF = 3;
	private int thorns;
	
	public ThornGarden(boolean isUpgraded) {
		super(ID, "Thorn Garden", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(isUpgraded ? 20 : 30, isUpgraded ? 50 : 70, 0, 0));
		properties.addUpgrades(PropertyType.MANA_COST, PropertyType.STAMINA_COST);
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		
		thorns = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new ThornguardInstance(data, this, slot, es));
	}
	
	private class ThornguardInstance extends EquipmentInstance {
		private int count = 0;
		public ThornguardInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			action = (pdata, inputs) -> {
				pc.play(p, p);
				Sounds.equip.play(p, p);
				p.getInventory().setItem(slot, null);
				pdata.addTrigger(id, Trigger.RECEIVE_SHIELDS, (pdata2, inputs2) -> {
					GrantShieldsEvent ev = (GrantShieldsEvent) inputs2;
					count += ev.getShield().getAmount();
					pdata.applyStatus(StatusType.THORNS, pdata, thorns * (count / CUTOFF), -1);
					count = count % CUTOFF;
					return TriggerResult.keep();
				});
				return TriggerResult.remove();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DEAD_BUSH,
				"On cast, for the rest of the fight, for every <white>" + CUTOFF +"</white> " + GlossaryTag.SHIELDS.tag + " that are granted to you, "
						+ "gain <yellow>" + thorns + "</yellow> stacks of " + GlossaryTag.THORNS.tag(this) + ".");
	}
}
