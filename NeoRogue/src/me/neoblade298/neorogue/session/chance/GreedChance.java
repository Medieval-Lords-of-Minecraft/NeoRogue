package me.neoblade298.neorogue.session.chance;

import java.util.Collections;
import java.util.Comparator;
import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.player.PlayerSessionData;

public class GreedChance extends ChanceSet {

	public GreedChance() {
		super(AreaType.LOW_DISTRICT, Material.GOLD_INGOT, "Greed");
		ChanceStage stage = new ChanceStage("init", "You come across a thick sludge that seems to contain various coins within."
				+ " You'll take damage if you reach into it, but the extra coins may be worth the effort.");

		ChanceChoice choice = new ChanceChoice(Material.GOLD_BLOCK, "Be Logical",
				"The highest HP party member takes &c10 &7damage and gets everyone &e25 coins&7.",
				"Nobody has enough health to do this!", (s, run) -> {
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
		
		choice = new ChanceChoice(Material.EMERALD_BLOCK, "Be Democratic",
				"Everyone takes an equal portion of &c10 &7damage rounded up, and everyone gains &e25 coins&7.",
				"Somebody lacks the health to do this!", (s, run) -> {
					final int HEALTH_LOSS = (int) Math.ceil(10D / s.getParty().size());
					for (PlayerSessionData pd : s.getParty().values()) {
						if (pd.getHealth() <= HEALTH_LOSS) return false;
					}

					if (!run) return true;

					for (PlayerSessionData pd : s.getParty().values()) {
						pd.setHealth(pd.getHealth() - HEALTH_LOSS);
					}
					s.broadcast("&7Everyone takes a little damage, but they're all &e25 coins &7richer.");
					return true;
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.BARRIER, "Be Risk-averse",
				"Leave the dangerous sludge alone.",
				"", (s, run) -> {
					if (!run) return true;
					s.broadcast("&7Good health is more valuable than riches.");
					return true;
				});
		stage.addChoice(choice);
		setInitialStage(stage);
	}
}
