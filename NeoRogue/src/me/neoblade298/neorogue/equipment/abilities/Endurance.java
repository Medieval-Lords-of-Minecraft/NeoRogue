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
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Endurance extends Equipment {
	private static final int HEAL_COUNT = 3;
	private ParticleContainer pc = new ParticleContainer(Particle.VILLAGER_HAPPY).count(15).spread(0.5, 0.5);
	private int heal, numToStack;
	
	public Endurance(boolean isUpgraded) {
		super("endurance", "Endurance", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		
		heal = 3;
		numToStack = isUpgraded ? 2 : 3;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		EnduranceInstance inst = new EnduranceInstance(data, this, slot, es);
		data.addTrigger(id, bind, inst);
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			if (inst.getCount() < HEAL_COUNT) return TriggerResult.keep();
			inst.calculateBerserkCount();
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
				"Passive. Heal for <white>" + heal + "</white> and gain <white>1</white> stack of " + GlossaryTag.BERSERK.tag(this) +
				" for every <yellow>" + numToStack + "</yellow> times you receive damage "
						+ "after <white>3</white> consecutive seconds of keeping your shield raised. ");
	}
	
	private class EnduranceInstance extends EquipmentInstance {
		private int count = 0;
		private int berserkCount = 0;
		private PlayerFightData pd;
		public EnduranceInstance(PlayerFightData pd, Equipment eq, int slot, EquipSlot es) {
			super(pd.getPlayer(), eq, slot, es);
			
			this.pd = pd;
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
		
		public void calculateBerserkCount() {
			if (++berserkCount <= numToStack) return;
			berserkCount = 0;
			pd.applyStatus(GenericStatusType.BASIC, "BERSERK", p.getUniqueId(), 1, -1);
		}
	}
}
