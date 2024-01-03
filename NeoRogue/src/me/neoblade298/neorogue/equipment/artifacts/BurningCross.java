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

public class BurningCross extends Artifact {
	private int damage;

	public BurningCross(boolean isUpgraded) {
		super("burningCross", "Burning Cross", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);

		damage = isUpgraded ? 6 : 4;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			String id = (String) in[1];
			int stacks = (int) in[2];
			if (id.equals(StatusType.CONCUSSED.name())) {
				FightData fd = (FightData) in[0];
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
				"For each stack of concussed you apply, also deal <yellow>" + damage + "</yellow> fire damage.");
	}
}
