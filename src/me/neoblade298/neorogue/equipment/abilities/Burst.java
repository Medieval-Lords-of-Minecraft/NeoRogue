package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Burst extends Equipment {
	private ParticleContainer pc = new ParticleContainer(Particle.REDSTONE);
	private int stamina, buff;
	private static final int seconds = 2;
	
	public Burst(boolean isUpgraded) {
		super("burst", "Burst", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 15, 0));
		pc.count(50).spread(0.5, 0.5).dustOptions(new DustOptions(Color.RED, 1F));
		buff = isUpgraded ? 30 : 20;
		stamina = isUpgraded ? 80 : 50;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"On cast, give yourself <white>" + stamina + " </white> stamina and <yellow>" + buff +
				"</yellow> bonus " + GlossaryTag.PHYSICAL.tag(this) + " damage for <white>" + seconds + "</white> seconds. Afterwards,"
						+ " <white>" + stamina + "</white> stamina is removed.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, in) -> {
			Util.playSound(p, Sound.ENTITY_BLAZE_DEATH, 1F, 1F, false);
			pc.spawn(p);
			pdata.addStamina(stamina);
			pdata.addBuff(p.getUniqueId(), id, true, false, BuffType.PHYSICAL, buff, seconds);
			
			data.addTask(id, new BukkitRunnable() {
				public void run() {
					pdata.addStamina(-stamina);
				}
			}.runTaskLater(NeoRogue.inst(), seconds * 20L));
			return TriggerResult.keep();
		}));
	}
}
