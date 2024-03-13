package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Sturdy extends Equipment {
	private static final String ID = "sturdy";
	private static final int HEAL_COUNT = 6;
	private static final ParticleContainer pc = new ParticleContainer(Particle.VILLAGER_HAPPY).count(15).spread(0.5, 0.5).offsetY(2);
	private int heal;
	
	public Sturdy(boolean isUpgraded) {
		super(ID, "Sturdy", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		
		heal = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		SturdyInstance inst = new SturdyInstance(id, p);
		data.addTrigger(id, Trigger.SHIELD_TICK, inst);
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, in) -> {
			inst.resetCount();
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GREEN_DYE,
				"Passive. Heal for <yellow>" + heal + "</yellow> every <white>" + HEAL_COUNT + "</white> consecutive seconds you keep a shield raised.");
	}
	
	private class SturdyInstance extends PriorityAction {
		private int count = 0;
		public SturdyInstance(String id, Player p) {
			super(id);
			action = (pdata, inputs) -> {
				if (++count % HEAL_COUNT != 0 || count < HEAL_COUNT) return TriggerResult.keep();
				pc.play(p, p);
				Sounds.enchant.play(p, p);
				FightInstance.giveHeal(p, heal, p);
				return TriggerResult.keep();
			};
		}
		
		public void resetCount() {
			count = 0;
		}
	}
}
