package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.session.ShrineInstance;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import net.kyori.adventure.text.Component;

public class CrossOfAntiquan extends Artifact {
	private static final String ID = "CrossOfAntiquan";
	private double mult = 0.1;
	private int multStr = (int) (mult * 100);
	
	public CrossOfAntiquan() {
		super(ID, "Cross of Antiquan", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {

	}
	
	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		data.addTrigger(id, SessionTrigger.VISIT_NODE, (pdata, in) -> {
			if (data.getSession().getInstance() instanceof ShrineInstance) {
				Util.msg(data.getPlayer(), hoverable.append(Component.text(" was activated")));
				data.healPercent(mult);
			}
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(Material.GOLD_INGOT, "Every time you visit a shrine node, heal for " + DescUtil.white(multStr + "%") + " of your max health.");
	}
}
