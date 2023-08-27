package me.neoblade298.neorogue.session.chance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class GreedChance extends ChanceSet {

	public GreedChance() {
		super(AreaType.LOW_DISTRICT, "Greed");
		ChanceStage stage = new ChanceStage("You come across a thick sludge that seems to contain various coins within."
				+ " You'll take damage if you reach into it, but the extra coins may be worth the effort.");

		ChanceChoice choice = new ChanceChoice(Material.GOLD_BLOCK, "&7Be Logical",
				"&7The highest HP party member takes &c10 &7damage and gets everyone &e25 coins&7.",
				"&cNobody has enough health to do this!", (s, run) -> {
					PlayerSessionData data = Collections.max(s.getParty().values(), new Comparator<PlayerSessionData>() {
						@Override
						public int compare(PlayerSessionData a, PlayerSessionData b) {
							if (a.getHealth() != b.getHealth()) return (int) (a.getHealth() - b.getHealth());
							return NeoCore.gen.nextInt(2) == 0 ? 1 : -1; // Random choice
						}
					});

					if (data.getHealth() <= 10) return false;
					if (!run) return true;

					data.setHealth(data.getHealth() - 10);
					s.broadcast("&e" + data.getData().getDisplay() + " &7reaches in, losing &c10 &7health but "
							+ "netting everybody &e25 &7coins!");
					for (PlayerSessionData pd : s.getParty().values()) {
						pd.addCoins(25);
					}
					return true;
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.EMERALD_BLOCK, "&7Be Democratic",
				"&7Everyone takes an equal portion of &c10 &7damage rounded up, and everyone gains &e25 coins&7.",
				"&cSomebody lacks the health to do this!", (s, run) -> {
					final int HEALTH_LOSS = Math.ceilDiv(10, s.getParty().size());
					for (PlayerSessionData pd : s.getParty().values()) {
						if (pd.getHealth() <= HEALTH_LOSS) return false;
					}

					if (!run) return true;

					for (PlayerSessionData pd : s.getParty().values()) {
						pd.setHealth(pd.getHealth() - HEALTH_LOSS);
					}
					return true;
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.BARRIER, "&7Be Risk-averse",
				"&7Leave the dangerous sludge alone.",
				"", (s, run) -> {
					return true;
				});
		stage.addChoice(choice);
		stages.add(stage);
	}
}
