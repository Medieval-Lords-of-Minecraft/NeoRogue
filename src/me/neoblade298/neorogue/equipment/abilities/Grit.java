package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealtDamageEvent;

public class Grit extends Equipment {
	private static final String ID = "grit";
	private int shields, inc;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final SoundContainer equip = new SoundContainer(Sound.ITEM_ARMOR_EQUIP_CHAIN);
	
	public Grit(boolean isUpgraded) {
		super(ID, "Grit", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		shields = isUpgraded ? 6 : 4;
		inc = isUpgraded ? 35 : 25;
		pc.count(30).spread(0.5, 0.5).speed(0.2).offsetY(1);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(ID, Trigger.PRE_DEALT_DAMAGE, (pdata, in) -> {
			PreDealtDamageEvent ev = (PreDealtDamageEvent) in;
			double dist = ev.getTarget().getLocation().distanceSquared(p.getLocation());
			if (dist <= 25) {
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, inc, 0));
			}
			return TriggerResult.keep();
		});

		data.addTrigger(ID, Trigger.KILL, (pdata, in) -> {
			KillEvent ev = (KillEvent) in;
			double dist = ev.getTarget().getLocation().distanceSquared(p.getLocation());
			if (dist <= 25) {
				pc.play(p, p);
				data.addSimpleShield(p.getUniqueId(), shields, 60);
				equip.play(p, p);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				"Passive. Killing an enemy within <white>5</white> blocks of you grants " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>3s</white>]. " +
				"Damage at this range is also increased by <yellow>" + inc + "</yellow>.");
	}
}
