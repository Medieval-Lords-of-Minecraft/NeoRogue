package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.Trap;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ShardBlast extends Equipment {
	private static final String ID = "ShardBlast";
	private static TargetProperties tp = TargetProperties.radius(6, false, TargetType.ENEMY);
	private static ParticleContainer trap = new ParticleContainer(Particle.CRIT).count(50).spread(1, 0.2),
		hit = new ParticleContainer(Particle.ENCHANTED_HIT).count(50).spread(1, 1);
	private int damage, dur = 6, reduc;
	
	public ShardBlast(boolean isUpgraded) {
		super(ID, "Shard Blast", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 15, 0).add(PropertyType.AREA_OF_EFFECT, tp.range));
		
		damage = isUpgraded ? 120 : 100;
		reduc = isUpgraded ? 50 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		EquipmentInstance ei = new EquipmentInstance(data, this, slot, es);
		ItemStack icon = item.clone().withType(Material.ECHO_SHARD);
		ei.setAction((pd, in) -> {
			if (am.getTrap() != null && am.getTrap().isActive()) {
				Trap t = am.getTrap();
				Sounds.glass.play(p, t.getLocation());
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, t.getLocation(), tp)) {
					hit.play(p, ent);
					FightData fd = FightInstance.getFightData(ent);
					FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.PIERCING, DamageOrigin.TRAP), ent);
					fd.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, -reduc, StatTracker.defenseDebuffEnemy(this)), 100);
				}
				t.deactivate();
			}
			else {
				Sounds.equip.play(p, p);
				am.setTrap(initTrap(p, data, ei, icon));
				ei.setCooldown(2);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, ei);
	}

	private Trap initTrap(Player p, PlayerFightData data, EquipmentInstance ei, ItemStack icon) {
		ei.setIcon(icon);
		Location loc = p.getLocation();
		Trap t = new Trap(data, loc, 200) {
			@Override
			public void tick() {
				trap.play(p, loc);
			}

			@Override
			public void deactivate() {
				ei.setIcon(item);
				super.deactivate();
			}
		};
		data.addTrap(t);
		return t;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DARK_PRISMARINE,
				"On cast, drop a " + GlossaryTag.TRAP.tag(this) + 
				" " + DescUtil.duration(dur, false) + ". Recasting activates the trap, dealing " + GlossaryTag.PIERCING.tag(this, damage, true) +
				" damage to all nearby enemies and reducing their " + GlossaryTag.GENERAL.tag(this) + " defense by " + DescUtil.yellow(reduc) + " " + DescUtil.duration(5, false) + ".");
	}
}
