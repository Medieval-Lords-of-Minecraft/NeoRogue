package me.neoblade298.neorogue.session.chance.builtin;

import java.util.Collections;
import java.util.Comparator;
import org.bukkit.Material;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.area.AreaType;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;

public class GreedChance extends ChanceSet {

	public GreedChance() {
		super(AreaType.LOW_DISTRICT, Material.GOLD_INGOT, "Greed");
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You come across a thick sludge that seems to contain various coins within."
				+ " You'll take damage if you reach into it, but the extra coins may be worth the effort.");

		ChanceChoice choice = new ChanceChoice(Material.GOLD_BLOCK, "Be Logical",
				"The highest HP party member takes <red>10 </red>damage and gets everyone <yellow>25 coins</yellow>.",
				"Nobody has enough health to do this!", (s, inst, unused) -> {
					return getHighestHealth(s).getHealth() >= 10;
				}, (s, inst, unused) -> {
					PlayerSessionData data = getHighestHealth(s);
					data.setHealth(data.getHealth() - 10);
					s.broadcast("<yellow>" + data.getData().getDisplay() + "</yellow><gray> reaches in, losing <red>10 </red>health but "
							+ "netting everybody <yellow>25 coins</yellow>!");
					for (PlayerSessionData pd : s.getParty().values()) {
						pd.addCoins(25);
					}
					return null;
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.EMERALD_BLOCK, "Be Democratic",
				"Everyone takes an equal portion of <red>10 </red>damage rounded up, and everyone gains <yellow>25 coins</yellow>.",
				"Somebody lacks the health to do this!", (s, inst, unused) -> {
					final int HEALTH_LOSS = (int) Math.ceil(10D / s.getParty().size());
					for (PlayerSessionData pd : s.getParty().values()) {
						if (pd.getHealth() <= HEALTH_LOSS) return false;
					}

					return true;
				}, (s, inst, data) -> {
					final int HEALTH_LOSS = (int) Math.ceil(10D / s.getParty().size());
					for (PlayerSessionData pd : s.getParty().values()) {
						pd.setHealth(pd.getHealth() - HEALTH_LOSS);
						pd.addCoins(25);
					}
					s.broadcast("Everyone takes a little damage, but they're all <yellow>25 coins </yellow>richer.");
					return null;
				});
		stage.addChoice(choice);
		
		choice = new ChanceChoice(Material.BARRIER, "Be Risk-averse",
				"Leave the dangerous sludge alone.", (s, inst, data) -> {
					s.broadcast("Good health is more valuable than riches.");
					return null;
				});
		stage.addChoice(choice);
	}
	
	private PlayerSessionData getHighestHealth(Session s) {
		return Collections.max(s.getParty().values(), new Comparator<PlayerSessionData>() {
			@Override
			public int compare(PlayerSessionData a, PlayerSessionData b) {
				if (a.getHealth() != b.getHealth()) return (int) (a.getHealth() - b.getHealth());
				return NeoRogue.gen.nextInt(2) == 0 ? 1 : -1; // Random choice
			}
		});
	}
}
