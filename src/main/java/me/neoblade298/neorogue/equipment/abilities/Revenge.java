package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Revenge extends Equipment implements Power {
	private static final String ID = "Revenge";
	private int strength, heal;
	private static final int CUTOFF = 20;
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).count(20).speed(0.01).offsetY(1);
	
	public Revenge(boolean isUpgraded) {
		super(ID, "Revenge", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		strength = isUpgraded ? 15 : 10;
		heal = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.RECEIVE_DAMAGE, (pdata, in) -> {
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		StandardPriorityAction inst = new StandardPriorityAction(id);
		inst.setAction((pdata2, in2) -> {
			if (!inst.canUse()) return TriggerResult.keep();
			data.applyStatus(StatusType.STRENGTH, data, strength, 200, this);
			inst.setNextUse(System.currentTimeMillis() + 2000);
			if (data.getStatus(StatusType.BERSERK).getStacks() < CUTOFF) {
				data.applyStatus(StatusType.BERSERK, data, 1, -1, this);
			}
			else {
				Player p2 = data.getPlayer();
				data.applyStatus(Status.createByGenericType(GenericStatusType.BASIC, "Revenge", data, true), data, 1, 200, this);
				Sounds.fire.play(p2, p2);
				pc.play(p2, p2);
				return TriggerResult.keep();
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, inst);

		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata3, in3) -> {
			if (!data.hasStatus("Revenge")) return TriggerResult.keep();
			Player p3 = data.getPlayer();
			FightInstance.giveHeal(p3, heal, p3);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.MAGMA_CREAM,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after taking damage. Receiving damage grants " + GlossaryTag.STRENGTH.tag(this, strength, true) + " " + DescUtil.duration(10, false) + ". Additionally, if you're below "
				+ GlossaryTag.BERSERK.tag(this, CUTOFF, false) + " stacks, gain " + GlossaryTag.BERSERK.tag(this, 1, false) + ". Otherwise, your basic attacks heal you "
						+ "for " + DescUtil.yellow(heal) + " " + DescUtil.duration(10, false) + ".");
	}
}
