package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;

import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
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
				if (ev.getMeta() == null) {
					FightInstance.dealDamage(new DamageMeta(data, stacks * damage, DamageType.FIRE), fd.getEntity());
				}
				else {
					ev.getMeta().addDamageSlice(new DamageSlice(data.getUniqueId(), stacks * damage, DamageType.FIRE));
				}
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
				"For each stack of " + GlossaryTag.SANCTIFIED.tag(this) + " you apply, also deal <white>" + damage + "</white> " +
						GlossaryTag.FIRE.tag(this) + " damage.");
	}
}
