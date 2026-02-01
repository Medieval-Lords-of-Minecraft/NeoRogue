package me.neoblade298.neorogue.equipment.weapons;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class HibernianQuickblade extends Equipment {
	private static final String ID = "HibernianQuickblade";
	private static final TargetProperties props = TargetProperties.radius(3, false, TargetType.ENEMY);
	private int shields;
	
	public HibernianQuickblade(boolean isUpgraded) {
		super(ID, "Hibernian Quickblade", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(60, 1.6, 0, DamageType.SLASHING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
				shields = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			Player p = data.getPlayer();
			weaponSwingAndDamage(p, data, ev.getTarget());
			if (am.addCount(1) >= 3) {
				am.setCount(0);
				Sounds.flap.play(p, p);
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0));
				data.addSimpleShield(p.getUniqueId(), shields, 100);
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, props)) {
					Vector v = ent.getLocation().subtract(p.getLocation()).toVector().setY(0).normalize().multiply(0.4).setY(0.3);
					FightInstance.knockback(ent, v);
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.GOLDEN_SWORD, "Every <white>3rd</white> hit grants " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [<white>5s</white>], " 
		+ DescUtil.potion("speed", 0, 3) + ", and knocks back nearby enemies.");
	}
}
