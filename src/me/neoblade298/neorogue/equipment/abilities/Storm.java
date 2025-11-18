package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.CastType;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Storm extends Equipment {
	private static final String ID = "Storm";
	private static final TargetProperties tp = TargetProperties.radius(14, true),
			aoe = TargetProperties.radius(4, true);
	private static final ParticleContainer pc = new ParticleContainer(Particle.ANGRY_VILLAGER);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
	private static final Circle circ = new Circle(aoe.range);
	private int damage, mana = 6;
	private ItemStack activeIcon;

	public Storm(boolean isUpgraded) {
		super(ID, "Storm", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 1, tp.range, aoe.range));
		damage = isUpgraded ? 30 : 20;
		properties.setCastType(CastType.TOGGLE);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			am.toggleBool();
			if (am.getBool()) {
				Sounds.equip.play(p, p);
				inst.setIcon(activeIcon);
			} else {
				inst.setIcon(item);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (!am.getBool())
				return TriggerResult.keep();
			if (data.getMana() < mana) {
				inst.setIcon(item);
				am.setBool(false);
				return TriggerResult.keep();
			}

			Block b = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
			if (b == null)
				return TriggerResult.keep();

			Location loc = b.getLocation().add(0, 1, 0);
			circ.play(pc, loc, LocalAxes.xz(), null);
			sc.play(p, p);
			data.addMana(-mana);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, aoe)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHTNING, DamageStatTracker.of(id + slot, this)), ent);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_ROD,
				"Toggleable, off by default. While active, aim at a block to deal "
						+ GlossaryTag.LIGHTNING.tag(this, damage, true) + " damage to all enemies near it for "
						+ DescUtil.white(mana) + " mana each second.");

		activeIcon = item.withType(Material.BLAZE_POWDER);
	}
}
