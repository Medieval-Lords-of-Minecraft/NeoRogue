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
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Bulwark extends Equipment {
	private static final int HEAL_COUNT = 3;
	private ParticleContainer pc = new ParticleContainer(Particle.VILLAGER_HAPPY).count(15).spread(0.5, 0.5).offsetY(2);;
	private int heal, shield;
	
	public Bulwark(boolean isUpgraded) {
		super("bulwark", "Bulwark", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		
		heal = 3;
		shield = isUpgraded ? 6 : 3;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		BulwarkInstance inst = new BulwarkInstance(p, this, slot, es);
		data.addTrigger(id, Trigger.SHIELD_TICK, inst);
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, in) -> {
			inst.resetCount();
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				"Passive. Heal for <white>" + heal + "</white> and gain a " +
						GlossaryTag.SHIELDS.tag(this) + " of <yellow>" + shield + "</yellow> for every " + HEAL_COUNT + " consecutive seconds you keep a shield raised.");
	}
	
	private class BulwarkInstance extends EquipmentInstance {
		private int count = 0;
		public BulwarkInstance(Player p, Equipment eq, int slot, EquipSlot es) {
			super(p, eq, slot, es);
			
			action = (pdata, inputs) -> {
				if (++count < HEAL_COUNT) return TriggerResult.keep();
				pc.spawn(p);
				Util.playSound(p, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1F, 1F, false);
				pdata.addSimpleShield(p.getUniqueId(), shield, 100);
				FightInstance.giveHeal(p, heal, p);
				count = 0;
				return TriggerResult.keep();
			};
		}
		
		public void resetCount() {
			count = 0;
		}
	}
}
