package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Egoism extends Equipment {
	private static final String ID = "Egoism";
	private int healthRegen, stealth;
	
	public Egoism(boolean isUpgraded) {
		super(ID, "Egoism", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		healthRegen = isUpgraded ? 3 : 2;
		stealth = isUpgraded ? 2 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.EVADE, (pdata, in) -> {
			if (data.getMana() < data.getMaxMana() * 0.6) return TriggerResult.keep();
			if (data.getStamina() < data.getMaxStamina() * 0.6) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 1) return TriggerResult.keep();
			Sounds.fire.play(data.getPlayer(), data.getPlayer());
			Util.msg(data.getPlayer(), hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			data.addTrigger(id, Trigger.EVADE, (pdata2, in2) -> {
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
				}.runTaskTimer(NeoRogue.inst(), 0L, 20L));
				// Gain stealth for 10 seconds
				data.applyStatus(StatusType.STEALTH, data, stealth, 200);
				// Gain Speed 1 for 5 seconds
				Player p = data.getPlayer();
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_APPLE,
				GlossaryTag.POWER.tag(this) + ". On " + GlossaryTag.EVADE.tag(this) + ", regen " + DescUtil.yellow(healthRegen) + " health over " + DescUtil.white("10s") + ", " +
				"gain " + GlossaryTag.STEALTH.tag(this, stealth, true) + " [<white>10s</white>], and gain " + DescUtil.potion("Speed", 0, 5) + ".");
	}
}
