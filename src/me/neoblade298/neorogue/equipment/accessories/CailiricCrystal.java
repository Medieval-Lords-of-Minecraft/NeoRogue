package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.ShrineInstance;
import me.neoblade298.neorogue.session.event.SessionTrigger;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class CailiricCrystal extends Artifact {
	private static final String ID = "CailiricCrystal";
	private static final int inc = 10;
	
	public CailiricCrystal() {
		super(ID, "Cailiric Crystal", Rarity.UNCOMMON, EquipmentClass.CLASSLESS);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_CLUSTER, "Visiting a shrine will increase your starting " +
		GlossaryTag.STRENGTH.tag(this) + " and " + GlossaryTag.INTELLECT.tag(this) + " by " + DescUtil.white(inc) + ".");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.applyStatus(StatusType.STRENGTH, data, inc * ai.getAmount(), -1);
		data.applyStatus(StatusType.INTELLECT, data, inc * ai.getAmount(), -1);
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {

	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		data.addTrigger(id, SessionTrigger.VISIT_NODE, (pdata, in) -> {
			if (data.getSession().getInstance() instanceof ShrineInstance) {
				// Must be done with runnable otherwise concurrent exception, adding new artifact while iterating through artifacts
				new BukkitRunnable() {
					public void run() {
						data.giveEquipment(CailiricCrystal.get(), null, null);
						Util.msg(data.getPlayer(), hoverable.append(
							SharedUtil.color(
							"<gray> potency has increased to " + DescUtil.white(inc * (data.getArtifacts().get(id).getAmount() - 1)
						)).decoration(TextDecoration.UNDERLINED, State.FALSE)));
					}
				}.runTaskLater(NeoRogue.inst(), 10); // Must be delayed else this will trigger twice for some reason
			}
		});
	}
}
