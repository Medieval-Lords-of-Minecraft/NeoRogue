package me.neoblade298.neorogue.equipment.abilities;

import java.util.EnumSet;

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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;
import net.kyori.adventure.text.Component;

public class Mahoraga extends Equipment {
	private static final String ID = "mahoraga";
	private int shields, refresh, berserk, thres, heal;
	
	public Mahoraga(boolean isUpgraded) {
		super(ID, "Mahoraga", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		shields = 75;
		refresh = 10;
		berserk = isUpgraded ? 3 : 2;
		thres = isUpgraded ? 30 : 25;
		heal = 10;
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
						Util.msg(p, hoverable.append(Component.text(" was refreshed")));
					}
				}.runTaskLater(NeoRogue.inst(), refresh * 20);
				am.setTask(task);

				if (am.getDouble() <= 0) return TriggerResult.keep();
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

		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			if (!p.isSneaking()) return TriggerResult.keep();
			EnumSet<DamageCategory> categories = ev.getMeta().getPrimarySlice().getPostBuffType().getCategories();
			if (categories.contains(DamageCategory.PHYSICAL)) {
				data.applyStatus(StatusType.PROTECT, data, 1, -1);
			}
			else if (categories.contains(DamageCategory.MAGICAL)) {
				data.applyStatus(StatusType.SHELL, data, 1, -1);
			}

			data.applyStatus(StatusType.BERSERK, data, berserk, -1);
			if (data.getStatus(StatusType.BERSERK).getStacks() >= thres) {
				Sounds.fire.play(p, p);
				data.addTask(new BukkitRunnable() {
					private int count;
					public void run() {
						data.addHealth(heal / 5);
						if (++count >= 5) {
							cancel();
						}
					}
				}.runTaskTimer(NeoRogue.inst(), 0, 20));
			}
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.OAK_SAPLING,
				new String[] { "Adapt to anything and everything." },
				"This ability holds up to " + GlossaryTag.SHIELDS.tag(this, shields, false) + ". While crouching, gain this shield. " +
				"Not crouching for " + DescUtil.white(refresh) + " will restore it to full. Receiving damage while crouching grants " + GlossaryTag.PROTECT.tag(this, 1, false) +
				" or " + GlossaryTag.SHELL.tag(this, 1, false) + " if the damage was " + GlossaryTag.PHYSICAL.tag(this) + " or " + GlossaryTag.MAGICAL.tag(this) + " respectively. " +
				"Additionally, gain " + GlossaryTag.BERSERK.tag(this, berserk, true) + " and heal for " + DescUtil.white(heal) + " over <white>5s</white> if above " +
				GlossaryTag.BERSERK.tag(this, thres, true) + ".");
	}
}
