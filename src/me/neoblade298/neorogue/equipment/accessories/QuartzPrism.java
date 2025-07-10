package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusClass;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class QuartzPrism extends Artifact {
	private static final String ID = "quartzPrism";
	private int duration = 20, inc = 2;
	
	public QuartzPrism() {
		super(ID, "Quartz Prism", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		am.setBool(true);

		data.addTask(new BukkitRunnable() {
			public void run() {
				am.setBool(false);
			}
		}.runTaskLater(NeoRogue.inst(), duration * 20));

		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			if (!am.getBool()) return TriggerResult.remove();
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			if (ev.getStatusClass() != StatusClass.POSITIVE) return TriggerResult.keep();
			ev.getStacksBuffList().add(Buff.increase(data, inc, BuffStatTracker.statusBuff(id, this)));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.QUARTZ, "For the first " + DescUtil.white(duration + "s") +
		" of a fight, any positive status effects applied to you are increased by " + DescUtil.white(inc) + ".");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {

	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}
}
