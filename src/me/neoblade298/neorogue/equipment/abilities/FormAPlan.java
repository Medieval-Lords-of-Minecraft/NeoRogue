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
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class FormAPlan extends Equipment {
	private static final String ID = "formAPlan";
	private static final ParticleContainer pc = new ParticleContainer(Particle.SMOKE_NORMAL).count(50).spread(1, 2).offsetY(1);
	private int buff;
	
	public FormAPlan(boolean isUpgraded) {
		super(ID, "Form A Plan", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		buff = isUpgraded ? 50 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		FormAPlanInstance inst = new FormAPlanInstance(data);
		data.addTrigger(ID, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			if (inst.isActive) return TriggerResult.remove();
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (!ev.getMeta().containsType(BuffType.GENERAL)) return TriggerResult.keep();
			inst.timer--;
			return TriggerResult.keep();
		});
	}
	
	private class FormAPlanInstance {
		private int timer = 0;
		private boolean isActive = false;
		public FormAPlanInstance(PlayerFightData data) {
			Player p = data.getPlayer();
			data.addTask(new BukkitRunnable() {
				public void run() {
					if (++timer >= 10) {
						Sounds.fire.play(p, p);
						pc.play(p, p);
						data.applyStatus(StatusType.STEALTH, data, 3, 100);
						Util.msg(p, item.displayName().append(Component.text(" was activated", NamedTextColor.GRAY)));
						data.addBuff(data, true, true, BuffType.GENERAL, buff * 0.01);
						this.cancel();
					}
				}
			}.runTaskTimer(NeoRogue.inst(), 20L, 20L));
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE,
				"Passive. After <white>10</white> seconds (+<white>1s</white> for every time you deal "
				+ GlossaryTag.GENERAL + " damage), gain " + GlossaryTag.STEALTH.tag(this, 3, false) +
				" [<white>5s</white>] and increase your damage by <yellow>" + buff + "%</yellow>.");
	}
}