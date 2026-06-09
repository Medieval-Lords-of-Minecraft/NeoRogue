package me.neoblade298.neorogue.tutorial;

import java.util.EnumSet;

import org.bukkit.entity.Player;

import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
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
		if (data.getSessionData().getAbilitiesEquipped() <= 0) return;

		Player p = data.getPlayer();
		p.showTitle(Title.title(
				Component.text(""),
				Component.text("Select an ability on your hotbar to cast it!", NamedTextColor.YELLOW)
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
}
