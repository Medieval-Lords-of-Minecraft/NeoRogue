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
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class VerricsTrainingSet extends Artifact {
	private static final String ID = "VerricsTrainingSet";
	private final int attacks, damage;

	public VerricsTrainingSet() {
		super(ID, "Verric's Training Set", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		attacks = 5;
		damage = 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		ActionMeta counter = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (counter.addCount(1) >= attacks) {
				counter.setCount(0);
				counter.addInt(damage);
			}
			if (counter.getInt() > 0) {
				PreBasicAttackEvent event = (PreBasicAttackEvent) in;
				event.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT),
						Buff.increase(data, counter.getInt(), StatTracker.damageBuffAlly(id, this)));
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
		item = createItem(Material.ARMOR_STAND, "Every " + DescUtil.val(attacks)
				+ " basic attacks permanently increase basic attack damage by " + DescUtil.val(damage)
				+ " for the fight.");
	}
}