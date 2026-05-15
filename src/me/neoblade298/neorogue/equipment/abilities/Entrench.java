package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
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

public class Entrench extends Equipment {
	private static final String ID = "Entrench";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORK)
			.count(20).spread(0.5, 0.5).offsetY(1);
	
	private int shields;

	public Entrench(boolean isUpgraded) {
		super(ID, "Entrench", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		shields = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.LAY_TRAP, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			data.addTask(new BukkitRunnable() {
				public void run() {
					data.addTrigger(id + "-active", Trigger.LAY_TRAP, (pdata2, in2) -> {
						Player p2 = data.getPlayer();
						data.addPermanentShield(p2.getUniqueId(), shields);
						Sounds.equip.play(p2, p2);
						pc.play(p2, p2);
						return TriggerResult.keep();
					});
				}
			}.runTask(NeoRogue.inst()));

			data.addTrigger(id + "-deactivate", Trigger.DEACTIVATE_TRAP, (pdata3, in3) -> {
				Player p3 = data.getPlayer();
				data.addPermanentShield(p3.getUniqueId(), shields);
				Sounds.equip.play(p3, p3);
				pc.play(p3, p3);
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_BLOCK,
				GlossaryTag.POWER.tag(this) + ". Activates after placing a " + GlossaryTag.TRAP.tag(this) + ". Gain " + GlossaryTag.SHIELDS.tag(this, shields, true) + " every time you place or remove a " +
				GlossaryTag.TRAP.tag(this) + ".");
	}
}
