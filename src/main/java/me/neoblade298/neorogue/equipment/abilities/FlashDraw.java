package me.neoblade298.neorogue.equipment.abilities;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class FlashDraw extends Equipment implements Power {
	private static final String ID = "FlashDraw";
	private int thres;
	
	public FlashDraw(boolean isUpgraded) {
		super(ID, "Flash Draw", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		thres = isUpgraded ? 15 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 30;
	private static final long CROUCH_DURATION_MILLIS = 1000L;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.REND)) return TriggerResult.keep();
			count.addCount(ev.getStacks());
			if (count.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		ActionMeta md = new ActionMeta();
		ActionMeta crouch = new ActionMeta();
		if (data.getPlayer().isSneaking()) crouch.setTime(System.currentTimeMillis());
		data.addTrigger(id + "-crouch", Trigger.TOGGLE_CROUCH, (pdata, in) -> {
			PlayerToggleSneakEvent ev = (PlayerToggleSneakEvent) in;
			crouch.setTime(ev.isSneaking() ? System.currentTimeMillis() : 0);
			return TriggerResult.keep();
		});

		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-rend", Trigger.APPLY_STATUS, (pdata2, in2) -> {
					ApplyStatusEvent e = (ApplyStatusEvent) in2;
					if (!e.isStatus(StatusType.REND)) return TriggerResult.keep();
					md.addCount(e.getStacks());
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));

		data.addTrigger(id + "-draw", Trigger.LAUNCH_PROJECTILE_GROUP, (pdata3, in3) -> {
			LaunchProjectileGroupEvent e = (LaunchProjectileGroupEvent) in3;
			Player p2 = data.getPlayer();
			if (!e.isBasicAttack()) return TriggerResult.keep();
			if (!p2.isSneaking() || crouch.getTime() == 0 ||
					System.currentTimeMillis() - crouch.getTime() < CROUCH_DURATION_MILLIS) return TriggerResult.keep();
			data.addAftershot(e.getGroup());
			if (md.getCount() >= thres) {
				md.addCount(-thres);
				data.addAftershot(e.getGroup());
			}
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.BEETROOT_SEEDS,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after applying " + DescUtil.val(30) + " " + GlossaryTag.REND.tag(this) + " stacks. While crouched for over " + DescUtil.white("1s") + ", your basic attacks fire an " + GlossaryTag.AFTERSHOT.tag(this) + ". For every " +
				GlossaryTag.REND.tag(this, thres) + " you apply to enemies, your next crouched basic attack fires " + DescUtil.white(2) + " " + GlossaryTag.AFTERSHOT.tagPlural(this) + " instead.");
	}
}
