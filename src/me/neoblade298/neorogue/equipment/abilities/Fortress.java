package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.offhands.ForceBracer;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Shield;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;

public class Fortress extends Equipment {
	private static final String ID = "fortress";
	private int shields, refresh;
	
	public Fortress(boolean isUpgraded) {
		super(ID, "Fortress", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		shields = isUpgraded ? 60 : 40;
		refresh = 10;
	}
	
	@Override
	public void setupReforges() {
		addSelfReforge(Inexorable.get());
		addReforge(Revenge.get(), Mahoraga.get());
		addReforge(ForceBracer.get(), BreakingPoint.get());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		am.setDouble(shields);
		data.addTrigger(id, Trigger.TOGGLE_CROUCH, (pdata, in) -> {
			PlayerToggleSneakEvent ev = (PlayerToggleSneakEvent) in;
			if (ev.isSneaking()) {
				// Refresh shield
				BukkitTask task = am.getTask();
				if (task != null) {
					task.cancel();
				}
				task = new BukkitRunnable() {
					public void run() {
						am.setDouble(shields);
						Sounds.success.play(p, p);
						Util.msg(p, display.append(Component.text(" was refreshed")));
					}
				}.runTaskLater(NeoRogue.inst(), refresh * 20);
				am.setTask(task);

				if (am.getDouble() <= 0) return TriggerResult.keep();
				Shield shield = data.addPermanentShield(p.getUniqueId(), am.getDouble(), true);
				am.setObject(shield);
			}
			else {
				if (am.getObject() == null) return TriggerResult.keep();
				Shield shield = (Shield) am.getObject();
				am.setDouble(shield.getAmount());
				shield.remove();
				am.setObject(null);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BRICK,
				"This ability holds up to " + GlossaryTag.SHIELDS.tag(this, shields, true) + ". While crouching, gain this shield. " +
				"Not crouching for " + DescUtil.white(refresh + "s") + " will restore it to full.");
	}
}
