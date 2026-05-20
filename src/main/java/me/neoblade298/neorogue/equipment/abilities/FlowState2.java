package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class FlowState2 extends Equipment {
	private static final String ID = "FlowState2";
	private int thres;
	private double inc, dmgInc;
	
	public FlowState2(boolean isUpgraded) {
		super(ID, "Flow State II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		thres = 30;
		inc = 0.8;
		dmgInc = isUpgraded ? 0.5 : 0.3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (data.getStamina() < data.getMaxStamina() * 0.5) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msgRaw(p, Component.text("").append(hoverable).append(Component.text(" was activated", NamedTextColor.GRAY)));

			data.addTask(new BukkitRunnable() {
				public void run() {
					data.addTrigger(id + "-active", Trigger.PLAYER_TICK, (pdata2, in2) -> {
						if (data.getStamina() < thres) return TriggerResult.keep();
						data.addStamina(inc);
						return TriggerResult.keep();
					});
				}
			}.runTask(NeoRogue.inst()));

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_CRYSTALS,
				GlossaryTag.POWER.tag(this) + ". Activates after reaching " + DescUtil.white("50%") + " stamina. Increase stamina regen by " + DescUtil.yellow(inc) + " when above "
				+ DescUtil.yellow(thres) + " stamina, further increased by " + DescUtil.yellow(dmgInc)
				+ " if you've dealt damage within " + DescUtil.white("2s") + ".");
	}
}
