package me.neoblade298.neorogue.tutorial;

import java.util.EnumSet;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment.EquipSlot;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.KeyBind;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class FirstAbilityCastTutorial implements Tutorial {
	private static final String ID = "first_ability_cast";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public EnumSet<TutorialTriggerType> getTriggerTypes() {
		return EnumSet.of(TutorialTriggerType.FIGHT);
	}

	@Override
	public void registerFight(FightInstance fight, PlayerFightData data) {
		PlayerSessionData sd = data.getSessionData();
		if (sd.getAbilitiesEquipped() <= 0) return;
		if (!TutorialManager.tryActivateFight(this, data)) return;

		Player p = data.getPlayer();
		p.showTitle(Title.title(
				Component.text(""),
				buildAbilityHint(sd)
		));

		data.addTrigger(ID, Trigger.CAST_USABLE, (pdata, in) -> {
			pdata.getSessionData().getData().addFlag(TutorialManager.getTutorialFlag(this));
			Player player = pdata.getPlayer();
			player.showTitle(Title.title(
					Component.text(""),
					Component.text("Nice! You cast your first ability!", NamedTextColor.GREEN)
			));
			return TriggerResult.remove();
		});
	}

	private Component buildAbilityHint(PlayerSessionData sd) {
		SessionEquipment[] hotbar = sd.getSessionEquipment(EquipSlot.HOTBAR);
		for (int i = 0; i < hotbar.length; i++) {
			if (hotbar[i] != null && hotbar[i].getEquipment().getType() == EquipmentType.ABILITY) {
				return Component.text("Press ", NamedTextColor.YELLOW)
						.append(Component.text(String.valueOf(i + 1), NamedTextColor.WHITE))
						.append(Component.text(" to cast ", NamedTextColor.YELLOW))
						.append(hotbar[i].getEquipment().getDisplay())
						.append(Component.text("!", NamedTextColor.YELLOW));
			}
		}
		SessionEquipment[] keybinds = sd.getSessionEquipment(EquipSlot.KEYBIND);
		for (KeyBind kb : KeyBind.values()) {
			SessionEquipment se = keybinds[kb.getDataSlot()];
			if (se != null && se.getEquipment().getType() == EquipmentType.ABILITY) {
				return Component.text("Press ", NamedTextColor.YELLOW)
						.append(kb.getDisplay().color(NamedTextColor.WHITE))
						.append(Component.text(" to cast ", NamedTextColor.YELLOW))
						.append(se.getEquipment().getDisplay())
						.append(Component.text("!", NamedTextColor.YELLOW));
			}
		}
		return Component.text("Cast your ability!", NamedTextColor.YELLOW);
	}
}
