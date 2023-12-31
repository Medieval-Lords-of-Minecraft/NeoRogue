package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class BurningCross extends Artifact {
	private int damage;

	public BurningCross() {
		super("burningCross", "Burning Cross", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);

		damage = 5;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			String id = ev.getStatusId();
			int stacks = ev.getStacks();
			if (id.equals(StatusType.CONCUSSED.name())) {
				FightData fd = ev.getTarget();
				DamageMeta dm = new DamageMeta(stacks * damage, DamageType.FIRE, false, true, false);
				FightInstance.dealDamage(p, dm, fd.getEntity());
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void onAcquire(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR, 
				"For each stack of sanctified you apply, also deal <yellow>" + damage + "</yellow> fire damage.");
	}
}
