package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Grindstone extends Artifact {
	private static final String ID = "Grindstone";
	private final int startingShields, delayedShields, delaySeconds;

	public Grindstone() {
		super(ID, "Grindstone", Rarity.COMMON, EquipmentClass.CLASSLESS);
		startingShields = 5;
		delayedShields = 10;
		delaySeconds = 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, ArtifactInstance ai) {
		Player player = data.getPlayer();
		data.addPermanentShield(player.getUniqueId(), startingShields, this);
		ActionMeta damaged = new ActionMeta();
		data.addTrigger(id, Trigger.RECEIVE_HEALTH_DAMAGE, (pdata, in) -> {
			damaged.setBool(true);
			return TriggerResult.remove();
		});
		data.addTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (damaged.getBool()) return;
				Player currentPlayer = data.getPlayer();
				data.addPermanentShield(currentPlayer.getUniqueId(), delayedShields, Grindstone.this);
			}
		}.runTaskLater(NeoRogue.inst(), delaySeconds * 20L));
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GRINDSTONE, "Start fights with "
				+ GlossaryTag.SHIELDS.tag(this, startingShields) + ". After "
				+ DescUtil.val(delaySeconds + "s") + ", gain "
				+ GlossaryTag.SHIELDS.tag(this, delayedShields)
				+ " if you have not taken health damage.");
	}
}