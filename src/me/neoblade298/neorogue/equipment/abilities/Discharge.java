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
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Discharge extends Equipment {
	private static final String ID = "discharge";
	private static final ParticleContainer pc = new ParticleContainer(Particle.ENCHANT).count(25).spread(0.5, 0.5).speed(0.1);;
	private int intel, elec;
	
	public Discharge(boolean isUpgraded) {
		super(ID, "Discharge", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 8, 0));
		intel = isUpgraded ? 4 : 3;
		elec = isUpgraded ? 60 : 40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		data.addTrigger(id, Trigger.KILL, inst);
		inst.setAction((pdata, in) -> {
			if (am.getTime() + (properties.get(PropertyType.COOLDOWN) * 1000) > System.currentTimeMillis()) {
				return TriggerResult.keep();
			}
			am.addCount(1);
			data.applyStatus(StatusType.INTELLECT, data, intel, -1);
			Sounds.enchant.play(p, p);
			pc.play(p, p);
			icon.setAmount(am.getCount());
			inst.setIcon(icon);
			am.setTime(System.currentTimeMillis());

			data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in2) -> {
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				FightInstance.applyStatus(ev.getTarget(), StatusType.ELECTRIFIED, data, elec, -1);
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WRITABLE_BOOK,
				"Passive. On kill, gain " + GlossaryTag.INTELLECT.tag(this, intel, true) + " on kill and your next basic attack will also apply " +
				GlossaryTag.ELECTRIFIED.tag(this, elec, true) + ".");
	}
}
