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
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class ShimmeringFeather extends Artifact {
	private static final String ID = "ShimmeringFeather";
	private final int attacksRequired;

	public ShimmeringFeather() {
		super(ID, "Shimmering Feather", Rarity.UNCOMMON, EquipmentClass.ARCHER);
		attacksRequired = 8;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		ActionMeta attacks = new ActionMeta();
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			LaunchProjectileGroupEvent event = (LaunchProjectileGroupEvent) in;
			if (!event.isBasicAttack()) return TriggerResult.keep();
			if (attacks.addCount(1) >= attacksRequired) {
				attacks.addCount(-attacksRequired);
				data.addAftershot(event.getGroup());
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
		item = createItem(Material.FEATHER,
				"Every " + DescUtil.val(attacksRequired) + " basic attacks, fire an "
				+ GlossaryTag.AFTERSHOT.tag(this) + ".");
	}
}