package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Meditate extends Equipment {
	private static final String ID = "brace";
	private int regen;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	
	public Meditate(boolean isUpgraded) {
		super(ID, "Meditate", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 12, 0));
		regen = isUpgraded ? 5 : 3;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.RECEIVED_HEALTH_DAMAGE, (pdata, in) -> {
			if (am.getBool()) {
				p.removePotionEffect(PotionEffectType.SLOWNESS);
				data.removeStatus(StatusType.STOPPED);
				am.setBool(false);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(p, p);
			pc.play(p, p);
			am.setBool(true);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
			data.applyStatus(StatusType.STOPPED, data, 1, 100);
			data.addTask(new BukkitRunnable() {
				int count = 0;
				public void run() {
					if (!am.getBool()) {
						cancel();
						return;
					}
					data.addMana(regen);
					Sounds.enchant.play(p, p);
					if (++count >= 5) {
						am.setBool(false);
						cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 20, 20));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLUE_DYE,
				"On cast, slow yourself and become unable to cast abilities or use weapons for <white>5s</white>. " +
				"During this time, gain " + DescUtil.yellow(regen) + " mana per second. Effect ends early if you receive health damage.");
	}
}
