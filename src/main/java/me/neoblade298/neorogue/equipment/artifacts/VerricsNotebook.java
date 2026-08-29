package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
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

public class VerricsNotebook extends Artifact {
	private static final String ID = "VerricsNotebook";
	private int damage;

	public VerricsNotebook() {
		super(ID, "Verric's Notebook", Rarity.COMMON, EquipmentClass.CLASSLESS);
		canStack = true;
		damage = 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		int totalDamage = damage * ai.getAmount();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			ev.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT),
					Buff.increase(data, totalDamage, StatTracker.damageBuffAlly(id, this)));
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
		item = createItem(Material.WRITABLE_BOOK,
				"Increases basic attack damage by " + DescUtil.val(damage) + " per stack.");
	}
}