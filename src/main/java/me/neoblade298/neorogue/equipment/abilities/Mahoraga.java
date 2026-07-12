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
import me.neoblade298.neorogue.equipment.SessionEquipment;
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
	private static final String ID = "Mahoraga";
	private int shields, refresh, berserk, thres, heal;

	public Mahoraga(boolean isUpgraded) {
		super(ID, "Mahoraga", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR, EquipmentType.ABILITY,
				EquipmentProperties.none());
		shields = 75;
		refresh = 10;
		berserk = isUpgraded ? 3 : 2;
		thres = isUpgraded ? 30 : 25;
		heal = 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		am.setDouble(shields);
		data.addTrigger(id, Trigger.TOGGLE_CROUCH, (pdata, in) -> {
			Player p = data.getPlayer();
			PlayerToggleSneakEvent ev = (PlayerToggleSneakEvent) in;
			if (ev.isSneaking()) {
				// Apply shield when crouching
				BukkitTask task = am.getTask();
				if (task != null) {
					task.cancel();
					am.setTask(null);
				}
				if (am.getDouble() <= 0)
					return TriggerResult.keep();
				Shield shield = data.addPermanentShield(p.getUniqueId(), am.getDouble(), true, this);
				am.setObject(shield);
			} else {
				// Remove shield when uncrouch and start the refresh timer
				if (am.getObject() != null) {
					Shield shield = (Shield) am.getObject();
					am.setDouble(shield.getAmount());
					shield.remove();
					am.setObject(null);
				}
				BukkitTask task = am.getTask();
				if (task != null) {
					task.cancel();
				}
				task = new BukkitRunnable() {
					public void run() {
						am.setDouble(shields);
						Sounds.success.play(p, p);
						Util.msgRaw(p, Component.text("").append(hoverable).append(Component.text(" was refreshed")));
					}
				}.runTaskLater(NeoRogue.inst(), refresh * 20);
				data.addTask(task);
				am.setTask(task);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			Player p = data.getPlayer();
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			if (!p.isSneaking())
				return TriggerResult.keep();
			EnumSet<DamageCategory> categories = ev.getMeta().getPrimarySlice().getPostBuffType().getCategories();
			if (categories.contains(DamageCategory.PHYSICAL)) {
				data.applyStatus(StatusType.PROTECT, data, 1, -1, this);
			} else if (categories.contains(DamageCategory.MAGICAL)) {
				data.applyStatus(StatusType.SHELL, data, 1, -1, this);
			}

			data.applyStatus(StatusType.BERSERK, data, berserk, -1, this);
			if (data.getStatus(StatusType.BERSERK).getStacks() >= thres) {
				Sounds.fire.play(p, p);
				data.addTask(new BukkitRunnable() {
					private int count;

					public void run() {
						data.addHealth(heal / 5, Mahoraga.this);
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
		item = createItem(Material.OAK_SAPLING, new String[] { "Adapt to anything and everything." },
				"Gain " + GlossaryTag.SHIELDS.tag(this, shields, false) + " while crouching. Refreshes after "
						+ DescUtil.white(refresh + "s") + " uncrouched. Taking " + GlossaryTag.PHYSICAL.tag(this)
						+ "/" + GlossaryTag.MAGICAL.tag(this) + " damage while crouching grants "
						+ GlossaryTag.PROTECT.tag(this, 1, false) + "/" + GlossaryTag.SHELL.tag(this, 1, false)
						+ " and " + GlossaryTag.BERSERK.tag(this, berserk, true) + ". Above "
						+ GlossaryTag.BERSERK.tag(this, thres, true) + ", heal " + DescUtil.white(heal)
						+ " over " + DescUtil.white("5s") + ".");
	}
}
