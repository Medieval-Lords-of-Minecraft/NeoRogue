package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class FlashDraw extends Equipment {
	private static final String ID = "FlashDraw";
	private int thres;
	
	public FlashDraw(boolean isUpgraded) {
		super(ID, "Flash Draw", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		thres = isUpgraded ? 20 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Player p = data.getPlayer();
		ActionMeta md = new ActionMeta();
		md.setTime(-1);
		data.addTrigger(id, Trigger.TOGGLE_CROUCH, (pdata, in) -> {
			PlayerToggleSneakEvent e = (PlayerToggleSneakEvent) in;
			md.setTime(e.isSneaking() ? System.currentTimeMillis() : -1);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent e = (ApplyStatusEvent) in;
			if (!e.isStatus(StatusType.REND)) return TriggerResult.keep();
			md.addCount(e.getStacks());
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent e = (LaunchProjectileGroupEvent) in;
			if (md.getTime() == -1 || md.getTime() + 1000 > System.currentTimeMillis()) return TriggerResult.keep();
			if (!e.isBasicAttack()) return TriggerResult.keep();
			data.addTask(new BukkitRunnable() {
				public void run() {
					e.getGroup().startWithoutEvent(data);
				}
			}.runTaskLater(NeoRogue.inst(), 5L));
			if (md.getCount() >= thres) {
				md.addCount(-thres);
				data.addTask(new BukkitRunnable() {
					public void run() {
						e.getGroup().startWithoutEvent(data);
					}
				}.runTaskLater(NeoRogue.inst(), 10L));
			}
			return TriggerResult.keep();
		});

		if (p.isSneaking()) {
			md.setTime(System.currentTimeMillis());
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BEETROOT_SEEDS,
				"Passive. Your basic attacks fire twice when you fire them while crouched for over a second. Every time you've applied over " +
				GlossaryTag.REND.tag(this, thres, true) + " to enemies, instead fire thrice.");
	}
}
