package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.artifacts.RubyShard;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import net.kyori.adventure.text.Component;

public class SigilOfTheIronLegion extends Artifact {
	private static final String ID = "sigilOfTheIronLegion";
	private int thres;
	
	public SigilOfTheIronLegion() {
		super(ID, "Sigil of the Iron Legion", Rarity.RARE, EquipmentClass.WARRIOR);
		thres = 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.STRENGTH)) return TriggerResult.keep();
			if (data.getStatus(StatusType.STRENGTH).getStacks() >= thres) {
				Sounds.success.play(p, p, Audience.ORIGIN);
				Util.msg(p, display.append(Component.text(" was activated")));
				am.setBool(true);
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.WIN_FIGHT, (pdata, in) -> {
			if (am.getBool()) {
				data.getSessionData().giveEquipment(RubyShard.get());
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_BARS, "If you reach at least " + GlossaryTag.STRENGTH.tag(this, thres, false) + " during a fight, gain one " +
		" <green>Ruby Shard</green> after the fight ends.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {

	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}
}
