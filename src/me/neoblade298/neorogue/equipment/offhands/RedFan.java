package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class RedFan extends Equipment {
	private static final String ID = "redFan";
	private static final TargetProperties tp = TargetProperties.cone(60, 5, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	private int damage, thres;
	
	public RedFan(boolean isUpgraded) {
		super(ID, "Red Fan", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND);
		damage = isUpgraded ? 150 : 100;
		thres = 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		EquipmentInstance eqi = new EquipmentInstance(data, this, slot, es);
		ItemStack charged = item.clone().withType(Material.FIRE_CORAL);
		ItemStack icon = item.clone();
		eqi.setAction((pdata, in) -> {	
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (ev.getMeta().containsType(DamageType.BURN)) {
				am.addCount(1);
				if (am.getCount() >= thres) {
					eqi.setIcon(charged);
				}
				else {
					icon.setAmount(Math.min(thres, Math.max(1, am.getCount())));
					eqi.setIcon(icon);
				}
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.DEALT_DAMAGE, eqi);
		data.addTrigger(id, Trigger.LEFT_CLICK, (pdata, in) -> {
			if (am.getCount() >= thres) {
				am.addCount(-thres);
				Sounds.fire.play(p, p);
				cone.play(pc, p.getLocation(), LocalAxes.usingEyeLocation(p), pc);
				for (LivingEntity ent : TargetHelper.getEntitiesInCone(p, tp)) {
					FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE), ent);
				}
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FIRE_CORAL_FAN, "Every " + DescUtil.yellow(thres) + " times you deal " + GlossaryTag.FIRE.tag(this) + " damage, " +
				"left clicking will deal " + GlossaryTag.FIRE.tag(this, damage, true) + " damage to all enemies in a cone in front of you.");
	}
}
