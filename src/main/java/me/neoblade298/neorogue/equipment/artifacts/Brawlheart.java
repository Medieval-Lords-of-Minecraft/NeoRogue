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
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Brawlheart extends Artifact {
	private static final String ID = "Brawlheart";
	private final int damagePercent, durationTicks;

	public Brawlheart() {
		super(ID, "Brawlheart", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
		damagePercent = 30;
		durationTicks = 20;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(id, Trigger.RECEIVE_DAMAGE, (pdata, in) -> {
			data.addDamageBuff(DamageBuffType.of(DamageCategory.DIRECT),
					Buff.multiplier(data, damagePercent * 0.01,
							BuffStatTracker.damageBuffAlly(id, this)), durationTicks);
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
		item = createItem(Material.FERMENTED_SPIDER_EYE, "Receiving damage increases direct damage by "
				+ DescUtil.val(damagePercent + "%") + " " + DescUtil.duration(durationTicks / 20) + ".");
	}
}