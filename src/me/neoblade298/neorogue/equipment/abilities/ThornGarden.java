package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.GrantShieldsEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ThornGarden extends Equipment {
	private static final String ID = "ThornGarden";
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final int CUTOFF = 3;
	private int thorns;
	
	public ThornGarden(boolean isUpgraded) {
		super(ID, "Thorn Garden", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		
		thorns = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVE_SHIELDS, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msgRaw(p, Component.text("").append(hoverable).append(Component.text(" was activated", NamedTextColor.GRAY)));

			int[] shieldCount = {0};
			data.addTask(new BukkitRunnable() {
				public void run() {
					data.addTrigger(id + "-active", Trigger.RECEIVE_SHIELDS, (pdata2, in2) -> {
						GrantShieldsEvent ev = (GrantShieldsEvent) in2;
						shieldCount[0] += ev.getShield().getAmount();
						data.applyStatus(StatusType.THORNS, data, thorns * (shieldCount[0] / CUTOFF), -1);
						shieldCount[0] = shieldCount[0] % CUTOFF;
						return TriggerResult.keep();
					});
				}
			}.runTask(NeoRogue.inst()));

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DEAD_BUSH,
				GlossaryTag.POWER.tag(this) + ". Activates after receiving shields. For every " + DescUtil.white(CUTOFF) + " " + GlossaryTag.SHIELDS.tag + " that are granted to you, "
						+ "gain " + DescUtil.yellow(thorns) + " stacks of " + GlossaryTag.THORNS.tag(this) + ".");
	}
}
