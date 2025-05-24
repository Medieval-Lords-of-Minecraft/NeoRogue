package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class PiercingVenom extends Equipment {
	private static final String ID = "piercingVenom";
	private int poison;
	private double mult;
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST).dustOptions(new DustOptions(Color.GREEN, 1)).count(50).spread(0.5, 0.5).speed(0.2);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_GENERIC_SWIM);
	
	public PiercingVenom(boolean isUpgraded) {
		super(ID, "Piercing Venom", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 10, 12, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		poison = 30;
		mult = isUpgraded ? 0.5 : 0.3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			inst.addCount(3);
			sc.play(p, p);
			pc.play(p, p);
			return TriggerResult.keep();
		});
		data.addTrigger(ID, bind, inst);
		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in) -> {
			if (inst.getCount() == 0) return TriggerResult.keep();
			BasicAttackEvent ev = (BasicAttackEvent) in;
			FightInstance.applyStatus(ev.getTarget(), StatusType.POISON, data, poison + (int) (ev.getMeta().getTotalDamage() * mult), -1);
			inst.addCount(-1);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GREEN_DYE,
				"On cast, your next <white>3</white> basic attacks apply " + GlossaryTag.POISON.tag(this, poison, true) + " plus " + DescUtil.yellow(mult + "%") +
				" of the damage that your basic attack deals after buffs and debuffs are applied.");
	}
}
