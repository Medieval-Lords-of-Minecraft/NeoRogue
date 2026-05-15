package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

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
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PointBlank extends Equipment {
	private static final String ID = "PointBlank";
	private int thres, damage;
	
	public PointBlank(boolean isUpgraded) {
		super(ID, "Point Blank", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		thres = isUpgraded ? 7 : 5;
		damage = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			if (data.getPlayer().getLocation().distance(ev.getTarget().getLocation()) > 5) return TriggerResult.keep();
			if (data.getStamina() < data.getMaxStamina() * 0.5) return TriggerResult.keep();
			if (am.addCount(1) < 2) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			String buffId = UUID.randomUUID().toString();
			data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
				PreDealDamageEvent ev2 = (PreDealDamageEvent) in2;
				DamageMeta dm = ev2.getMeta();
				if (!dm.hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
				IProjectileInstance ip = dm.getProjectile();
				if (ip.getOrigin().distanceSquared(ev2.getTarget().getLocation()) > thres * thres) return TriggerResult.keep();
				dm.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, damage, 0, StatTracker.damageBuffAlly(buffId, this)));
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLACKSTONE_SLAB,
				GlossaryTag.POWER.tag(this) + ". Dealing damage via projectile from at most " + DescUtil.yellow(thres) + " blocks away increases " + GlossaryTag.GENERAL.tag(this) + " damage by " +
				DescUtil.yellow(damage) + ".");
	}
}
