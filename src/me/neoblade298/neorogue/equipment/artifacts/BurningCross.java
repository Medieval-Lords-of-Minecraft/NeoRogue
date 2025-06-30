package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class BurningCross extends Artifact {
	private static final String ID = "burningCross";
	private int damage;

	public BurningCross() {
		super(ID, "Burning Cross", Rarity.UNCOMMON, EquipmentClass.WARRIOR);

		damage = 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			String id = ev.getStatusId();
			int stacks = ev.getStacks();
			if (id.equals(StatusType.CONCUSSED.name())) {
				FightData fd = ev.getTarget();
				if (ev.getMeta() == null) {
					FightInstance.dealDamage(new DamageMeta(data, stacks * damage, DamageType.FIRE,
							DamageStatTracker.of(id, this)), fd.getEntity());
				}
				else {
					ev.getMeta().addDamageSlice(new DamageSlice(data, stacks * damage, DamageType.FIRE, DamageStatTracker.of(id, this)));
				}
			}
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
		item = createItem(Material.NETHER_STAR, 
				"For each stack of " + GlossaryTag.SANCTIFIED.tag(this) + " you apply, also deal <white>" + damage + "</white> " +
						GlossaryTag.FIRE.tag(this) + " damage.");
	}
}
