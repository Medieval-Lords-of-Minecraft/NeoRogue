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

public class CharmOfGallus extends Artifact {
	private int stamina;

	public CharmOfGallus(boolean isUpgraded) {
		super("charmOfGallus", "Charm Of Gallus", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR);

		stamina = isUpgraded ? 25 : 15;
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
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
		item = createItem(Material.GOLD_NUGGET, 
				"Casting a skill that costs over 15 stamina grants you <yellow>" + stamina + "</yellow> stamina. Can be used 5x per fight.");
	}
}
