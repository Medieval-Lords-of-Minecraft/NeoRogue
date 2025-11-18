package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CheckCastUsableEvent;

public class OpalHourglass extends Artifact {
	private static final String ID = "OpalHourglass";
	private static int thres = 15, reduc = 4;

	public OpalHourglass() {
		super(ID, "Opal Hourglass", Rarity.COMMON, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(ID, Trigger.PRE_CAST_USABLE, (pdata, in) -> {
			CheckCastUsableEvent ev = (CheckCastUsableEvent) in;
			if (ev.getInstance().getBaseCooldown() < thres)
				return TriggerResult.keep();
			ev.addBuff(PropertyType.COOLDOWN, id, new Buff(data, reduc, 0, BuffStatTracker.of(ID, this, "Cooldown reduced")));
			return TriggerResult.keep();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {

	}

	@Override
	public void setupItem() {
		item = createItem(Material.CLOCK, "Cooldowns that are " + DescUtil.white(thres + "s")
				+ " or more are reduced by " + DescUtil.white(reduc) + ".");
	}
}
