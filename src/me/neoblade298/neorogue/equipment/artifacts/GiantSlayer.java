package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.Mob.MobType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class GiantSlayer extends Artifact {
	private static final String ID = "GiantSlayer";
	private double buff = 0.3;

	public GiantSlayer() {
		super(ID, "Giant Slayer", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			FightData targetFD = FightInstance.getFightData(ev.getTarget());
			if (targetFD == null) return TriggerResult.keep();
			Mob mob = targetFD.getMob();
			if (mob != null && mob.getType() == MobType.MINIBOSS) {
				ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
						Buff.multiplier(data, buff, BuffStatTracker.ignored(this)));
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
		item = createItem(Material.IRON_AXE, "Increases damage dealt to minibosses by <white>"
				+ (int) (buff * 100) + "%</white>.");
	}
}
