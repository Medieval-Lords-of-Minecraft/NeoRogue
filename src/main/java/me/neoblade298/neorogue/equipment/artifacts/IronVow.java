package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class IronVow extends Artifact {
	private static final String ID = "IronVow";
	private int shields = 3;
	private int seconds = 3;

	public IronVow() {
		super(ID, "Iron Vow", Rarity.RARE, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		int[] ticksNotSprinting = {0};

		data.addTrigger(id, Trigger.TOGGLE_SPRINT, (pdata, in) -> {
			PlayerToggleSprintEvent ev = (PlayerToggleSprintEvent) in;
			if (ev.isSprinting()) {
				ticksNotSprinting[0] = 0;
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			Player p = data.getPlayer();
			if (!p.isSprinting()) {
				ticksNotSprinting[0]++;
				if (ticksNotSprinting[0] >= seconds) {
					ticksNotSprinting[0] = 0;
					data.addPermanentShield(p.getUniqueId(), shields, this);
				}
			} else {
				ticksNotSprinting[0] = 0;
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
		item = createItem(Material.IRON_NUGGET, "Grants " + GlossaryTag.SHIELDS.tag(this, shields, false)
				+ " for every " + DescUtil.white(seconds) + " seconds you don't sprint.");
	}
}
