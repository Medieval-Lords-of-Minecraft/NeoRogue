package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Discharge extends Equipment implements Power {
	private static final String ID = "Discharge";
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(0.5, 0.5).speed(0.1);;
	private SessionEquipment sessionEq;
	private int intel, elec;
	
	public Discharge(boolean isUpgraded) {
		super(ID, "Discharge", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		intel = isUpgraded ? 4 : 3;
		elec = isUpgraded ? 8 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		this.sessionEq = sessionEq;
		data.addTrigger(id, Trigger.KILL, (pdata, in) -> {
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta am = new ActionMeta();
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, sessionEq, slot, es);
		data.addTrigger(id, Trigger.KILL, (pdata2, in2) -> {
			Player p2 = data.getPlayer();
			am.addCount(1);
			data.applyStatus(StatusType.INTELLECT, data, intel, -1);
			Sounds.enchant.play(p2, p2);
			pc.play(p2, p2);
			icon.setAmount(am.getCount());
			inst.setIcon(icon);

			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata3, in3) -> {
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in3;
				FightInstance.applyStatus(ev.getTarget(), StatusType.ELECTRIFIED, data, elec, -1);
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WRITABLE_BOOK,
				GlossaryTag.POWER.tag(this) + ". Activates after killing an enemy. Gain " + GlossaryTag.INTELLECT.tag(this, intel, true) + " and your next basic attack will also apply " +
				GlossaryTag.ELECTRIFIED.tag(this, elec, true) + ".");
	}
}
