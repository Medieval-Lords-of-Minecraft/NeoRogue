package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Revenge extends Equipment {
	private static final String ID = "revenge";
	private int strength, heal;
	private static final int CUTOFF = 20;
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).count(20).speed(0.01).offsetY(1);
	
	public Revenge(boolean isUpgraded) {
		super(ID, "Revenge", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 2, 0));
		strength = isUpgraded ? 15 : 10;
		heal = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction inst = new StandardPriorityAction(id);
		inst.setAction((pdata, in) -> {
			if (!inst.canUse()) return TriggerResult.keep();
			data.applyStatus(StatusType.STRENGTH, data, strength, 200);
			inst.setNextUse(System.currentTimeMillis() + 2000);
			if (data.getStatus(StatusType.BERSERK).getStacks() < CUTOFF) {
				data.applyStatus(StatusType.BERSERK, data, 1, -1);
			}
			else {
				data.applyStatus(GenericStatusType.BASIC, "Revenge", data, 1, 200);
				Sounds.fire.play(p, p);
				pc.play(p, p);
				return TriggerResult.keep();
			}
			return TriggerResult.keep();
			});
		data.addTrigger(ID, Trigger.RECEIVED_DAMAGE, inst);
		
		data.addTrigger(ID, Trigger.BASIC_ATTACK, (pdata, in) -> {
			if (!data.hasStatus("Revenge")) return TriggerResult.keep();
			FightInstance.giveHeal(p, heal, p);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MAGMA_CREAM,
				"Passive. Receiving damage grants " + GlossaryTag.STRENGTH.tag(this, strength, true) + " for <white>10</white> seconds. Additionally, if you're below "
				+ GlossaryTag.BERSERK.tag(this, CUTOFF, false) + " stacks, gain " + GlossaryTag.BERSERK.tag(this, 1, false) + ". Otherwise, your basic attacks heal you "
						+ "for <yellow>" + heal + "</yellow> for <white>10</white> seconds.");
	}
}
