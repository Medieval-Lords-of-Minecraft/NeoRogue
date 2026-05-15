package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.IProjectileInstance;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Momentum extends Equipment {
	private static final String ID = "Momentum";
	private static final int DISTANCE = 5;
	private int damage, dur;
	
	public Momentum(boolean isUpgraded) {
		super(ID, "Momentum", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
				damage = isUpgraded ? 20 : 10;
				dur = 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta count = new ActionMeta();
		data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata, in) -> {
			if (data.getStamina() < data.getMaxStamina() * 0.5) return TriggerResult.keep();
			if (count.addCount(1) < 5) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			ActionMeta am = new ActionMeta();
			double distSq = DISTANCE * DISTANCE;
			data.addTrigger(id, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata2, in2) -> {
				Player p2 = data.getPlayer();
				if (am.getLocation() != null && am.getLocation().distanceSquared(p2.getLocation()) >= distSq) {
					LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in2;
					for (IProjectileInstance ipi : ev.getInstances()) {
						if (!(ipi instanceof ProjectileInstance)) continue;
						ProjectileInstance pi = (ProjectileInstance) ipi;
						pi.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, damage, StatTracker.damageBuffAlly(am.getId(), this)));
					}
				}
				am.setLocation(p2.getLocation());
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GLOW_ITEM_FRAME,
				GlossaryTag.POWER.tag(this) + ". Upon firing a projectile, if you are at least " + DescUtil.white(DISTANCE) + " blocks away from where you last fired a projectile, " +
				"increase your damage by " + DescUtil.yellow(damage) + " " + DescUtil.duration(dur, false) + ".");
	}
}
