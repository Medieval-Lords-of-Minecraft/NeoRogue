package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Audience;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.event.ClearRewardsEvent;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.reward.CoinsReward;

public class PotOfGreed extends Artifact {
	public static final String ID = "PotOfGreed";
	public static final int GOLD = 25;

	public PotOfGreed() {
		super(ID, "Pot of Greed", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CAULDRON, new String[] {"Draws two cards!"}, "For each non-coin reward you skip (by clearing), gain " + DescUtil.val(GOLD) + " gold.");
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {

	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
		data.addTrigger(id, SessionTrigger.CLEAR_REWARDS, (pdata, in) -> {
			Player p = data.getPlayer();
			ClearRewardsEvent ev = (ClearRewardsEvent) in;
			int skippedNonCoinRewards = (int) ev.getRewards().stream()
					.filter(reward -> !(reward instanceof CoinsReward))
					.count();
			if (skippedNonCoinRewards <= 0) return TriggerResult.keep();

			Sounds.success.play(p, p, Audience.ORIGIN);
			int coins = GOLD * skippedNonCoinRewards;
			data.addCoins(coins);
			Util.msgRaw(p, display.append(SharedUtil.color("<gray> gives you " + DescUtil.val(coins) + " " + PlayerSessionData.CURRENCY)));
			return TriggerResult.keep();
		});
	}
}
