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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Endurance extends Equipment {
	private static final int HEAL_COUNT = 3;
	private ParticleContainer pc = new ParticleContainer(Particle.VILLAGER_HAPPY).count(15).spread(0.5, 0.5).offsetY(2);;
	private int heal, berserk;
	
	public Endurance(boolean isUpgraded) {
		super("endurance", "Endurance", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		
		heal = 8;
		berserk = isUpgraded ? 3 : 2;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EnduranceInstance inst = new EnduranceInstance(data, this, slot, es);
		data.addTrigger(id, Trigger.SHIELD_TICK, inst);
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			if (inst.count < HEAL_COUNT) return TriggerResult.keep();
			data.applyStatus(StatusType.BERSERK, p.getUniqueId(), berserk, -1);
			return TriggerResult.keep();
		});
		
		data.addTrigger(id, Trigger.LOWER_SHIELD, (pdata, in) -> {
			inst.count = 0;
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GRANITE_SLAB,
				"Passive. Heal for <white>" + heal + "</white> after <white>3</white> consecutive seconds of keeping your shield raised. " +
				"Gain <yellow>" + berserk + "</yellow> " + GlossaryTag.BERSERK.tag(this) + " "
						+ "for every time you receive damage after holding your shield up for <white>3</white> seconds.");
	}
	
	private class EnduranceInstance extends EquipmentInstance {
		protected int count = 0;
		public EnduranceInstance(PlayerFightData pd, Equipment eq, int slot, EquipSlot es) {
			super(pd.getPlayer(), eq, slot, es);
			action = (pdata, inputs) -> {
				if (++count < HEAL_COUNT) return TriggerResult.keep();
				pc.spawn(p);
				if (count % HEAL_COUNT == 0) {
					Util.playSound(p, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1F, 1F, false);
					FightInstance.giveHeal(p, heal, p);
				}
				
				return TriggerResult.keep();
			};
		}
	}
}
