package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.util.Util;
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
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class FlashDraw extends Equipment {
	private static final String ID = "FlashDraw";
	private int thres;
	
	public FlashDraw(boolean isUpgraded) {
		super(ID, "Flash Draw", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		thres = isUpgraded ? 15 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 30;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.REND)) return TriggerResult.keep();
			count.addCount(ev.getStacks());
			if (count.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			ActionMeta md = new ActionMeta();
			data.addTrigger(id, Trigger.APPLY_STATUS, (pdata2, in2) -> {
				ApplyStatusEvent e = (ApplyStatusEvent) in2;
				if (!e.isStatus(StatusType.REND)) return TriggerResult.keep();
				md.addCount(e.getStacks());
				return TriggerResult.keep();
			});

			data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata3, in3) -> {
				LaunchProjectileGroupEvent e = (LaunchProjectileGroupEvent) in3;
				Player p2 = data.getPlayer();
				if (!p2.isSneaking()) return TriggerResult.keep();
				if (!e.isBasicAttack()) return TriggerResult.keep();
				data.addExtraShot(e.getGroup());
				if (md.getCount() >= thres) {
					md.addCount(-thres);
					data.addTask(new BukkitRunnable() {
						public void run() {
							data.addExtraShot(e.getGroup());
						}
					}.runTaskLater(NeoRogue.inst(), 10L));
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BEETROOT_SEEDS,
				GlossaryTag.POWER.tag(this) + ". Your basic attacks fire twice when you fire them while crouched for over a second. Every time you've applied over " +
				GlossaryTag.REND.tag(this, thres, true) + " to enemies, instead fire thrice.");
	}
}
