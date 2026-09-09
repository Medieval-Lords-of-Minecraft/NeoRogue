package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class TNT extends Artifact {
	private static final String ID = "TNT";
	private final int activations, damagePercent;

	public TNT() {
		super(ID, "TNT", Rarity.COMMON, EquipmentClass.CLASSLESS);
		activations = 5;
		damagePercent = 100;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		ActionMeta counter = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent event = (PreDealDamageEvent) in;
			event.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.ALL),
					Buff.multiplier(data, damagePercent * 0.01,
							BuffStatTracker.damageBuffAlly(id, this)));
			return counter.addCount(1) >= activations ? TriggerResult.remove() : TriggerResult.keep();
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
		item = createItem(Material.TNT, "Increase damage dealt by " + DescUtil.val(damagePercent + "%")
				+ " the first " + DescUtil.val(activations) + " times you deal damage each fight.");
	}
}