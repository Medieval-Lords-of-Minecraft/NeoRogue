package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.DamageMeta.BuffOrigin;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class StaticNecklace extends Artifact {
	private static final String ID = "staticNecklace";

	public StaticNecklace() {
		super(ID, "Static Necklace", Rarity.COMMON, EquipmentClass.THIEF);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(ID, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (!fd.hasStatus(StatusType.ELECTRIFIED)) return TriggerResult.keep();
			
			double buff = fd.getStatus(StatusType.ELECTRIFIED).getStacks() * 0.1;
			ev.getMeta().addBuff(BuffType.LIGHTNING, new Buff(pdata, buff, 0), BuffOrigin.NORMAL, true);
			return TriggerResult.keep();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEAD, 
				GlossaryTag.LIGHTNING.tag(this) + " is increased by <white>0.1</white> for each stack of " + 
				GlossaryTag.ELECTRIFIED.tag(this) + " on the target.");
	}
}