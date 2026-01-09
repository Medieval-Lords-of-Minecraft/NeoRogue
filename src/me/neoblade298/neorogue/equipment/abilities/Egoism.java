package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Egoism extends Equipment {
	private static final String ID = "Egoism";
	private int healthRegen, stealth;
	
	public Egoism(boolean isUpgraded) {
		super(ID, "Egoism", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		healthRegen = isUpgraded ? 6 : 4;
		stealth = isUpgraded ? 2 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			// Regen health over 10 seconds (10 player ticks)
			data.addTask(new BukkitRunnable() {
				private int count = 0;
				
				public void run() {
					data.addHealth(healthRegen / 10.0);
					count++;
					if (count >= 10) {
						this.cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 0L, 20L)); // Every 1 second for 10 seconds
			
			// Gain stealth for 10 seconds
			data.applyStatus(StatusType.STEALTH, data, stealth, 200);
			
			// Gain Speed 1 for 5 seconds
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_APPLE,
				"Passive. On " + GlossaryTag.EVADE.tag(this) + ", regen <yellow>" + healthRegen + "</yellow> health over <white>10s</white>, " +
				"gain " + GlossaryTag.STEALTH.tag(this, stealth, true) + " [<white>10s</white>], and gain " + DescUtil.potion("Speed", 0, 5) + ".");
	}
}
