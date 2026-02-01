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
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Dread extends Equipment {
	private static final String ID = "Dread";
	private int stealthGained, thres = 10;
	private double damageIncrease;
	
	public Dread(boolean isUpgraded) {
		super(ID, "Dread", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		stealthGained = isUpgraded ? 3 : 2;
		damageIncrease = isUpgraded ? 0.60 : 0.40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		StandardPriorityAction act = new StandardPriorityAction(id);
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			Player p = data.getPlayer();
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.STEALTH)) return TriggerResult.keep();
			act.addCount(ev.getStacks());
			if (act.getCount() >= thres) {
				data.addTask(new BukkitRunnable() {
					public void run() {
						// Apply permanent stealth
						data.applyStatus(StatusType.STEALTH, data, stealthGained, -1);

						// Apply permanent Speed 1
						p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0));

						// Apply permanent damage buff
						data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
								new Buff(data, 0, damageIncrease, StatTracker.damageBuffAlly(id + slot, eq)));
						Sounds.wither.play(p, p);
						Util.msg(p, Component.text("").append(hoverable).append(Component.text(" was activated", NamedTextColor.GRAY)));
					}
				}.runTaskLater(NeoRogue.inst(), 1));
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WITHER_SKELETON_SKULL,
				"Passive. Upon applying " + GlossaryTag.STEALTH.tag(this, thres, false) + ", " +
				"gain " + GlossaryTag.STEALTH.tag(this, stealthGained, true) + ", " + DescUtil.white("Speed 1") + ", and " +
				DescUtil.yellow((int)(damageIncrease * 100) + "%") + " " + GlossaryTag.GENERAL.tag(this) + " damage permanently.");
	}
}
