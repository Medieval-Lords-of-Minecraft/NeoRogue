package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Shield;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BreakingPoint extends Equipment {
	private static final String ID = "breakingPoint";
	private int shields, refresh, multStr;
	private double mult;
	
	public BreakingPoint(boolean isUpgraded) {
		super(ID, "Breaking Point", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		shields = 60;
		refresh = 10;
		mult = isUpgraded ? 1.5 : 1;
		multStr = (int) (mult * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		am.setDouble(shields);
		data.addTrigger(id, Trigger.TOGGLE_CROUCH, (pdata, in) -> {
			PlayerToggleSneakEvent ev = (PlayerToggleSneakEvent) in;
			if (ev.isSneaking()) {
				// Refresh shield
				BukkitTask task = am.getTask();
				if (task != null) {
					task.cancel();
				}
				task = new BukkitRunnable() {
					public void run() {
						am.setDouble(shields);
						Sounds.success.play(p, p);
						Util.msg(p, hoverable.append(Component.text(" was refreshed", NamedTextColor.GRAY)));
					}
				}.runTaskLater(NeoRogue.inst(), refresh * 20);
				data.addTask(task);
				am.setTask(task);

				if (am.getDouble() <= 0)
					return TriggerResult.keep();
				Shield shield = data.addPermanentShield(p.getUniqueId(), am.getDouble(), true);
				am.setObject(shield);
			}
			else {
				if (am.getObject() == null) return TriggerResult.keep();
				Shield shield = (Shield) am.getObject();
				am.setDouble(shield.getAmount());
				shield.remove();
				am.setObject(null);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.RECEIVE_DAMAGE, (pdata, in) -> {
			if (am.getObject() == null) return TriggerResult.keep();
			Shield shield = (Shield) am.getObject();
			if (shield.getAmount() <= shields / 2) {
				// Gain damage buff
				data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.multiplier(data, mult, BuffStatTracker.damageBuffAlly(id + slot, this)));
				Sounds.blazeDeath.play(p, p);
				Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BRICK,
				"This ability holds up to " + GlossaryTag.SHIELDS.tag(this, shields, true) + ". While crouching, gain this shield. " +
				"Not crouching for " + DescUtil.white(refresh + "s") + " will restore it to full. If the shield is reduced to half, gain a " +
				DescUtil.yellow(multStr + "%") + " damage buff for the rest of the fight.");
	}
}
