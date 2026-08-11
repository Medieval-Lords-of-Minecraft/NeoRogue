package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
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
	private static final String ID = "BurningCross";
	private int damage, thres;

	public BurningCross() {
		super(ID, "Burning Cross", Rarity.UNCOMMON, EquipmentClass.WARRIOR);

		damage = 15;
		thres = 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		ActionMeta applied = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			String statusId = ev.getStatusId();
			int stacks = ev.getStacks();
			if (statusId.equals(StatusType.SANCTIFIED.name())) {
				int total = applied.getCount() + stacks;
				int triggers = total / thres;
				applied.setCount(total % thres);
				if (triggers == 0) return TriggerResult.keep();
				int totalDamage = triggers * damage;
				FightData fd = ev.getTarget();
				if (ev.getMeta() == null) {
					FightInstance.dealDamage(new DamageMeta(data, totalDamage, DamageType.FIRE,
							DamageStatTracker.of(ID, this)), fd.getEntity());
				}
				else {
					ev.getMeta().addDamageSlice(new DamageSlice(data, totalDamage, DamageType.FIRE,
							DamageStatTracker.of(ID, this)));
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
				"For every " + DescUtil.val(thres) + " stacks of " + GlossaryTag.SANCTIFIED.tag(this) + " you apply, also deal " + DescUtil.val(damage) + " " +
						GlossaryTag.FIRE.tag(this) + " damage.");
	}
}
