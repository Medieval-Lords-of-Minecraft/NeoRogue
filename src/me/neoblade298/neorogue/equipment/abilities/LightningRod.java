package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
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
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class LightningRod extends Equipment {
	private static final String ID = "LightningRod";
	private static final ParticleContainer part = new ParticleContainer(Particle.ELECTRIC_SPARK)
			.count(5).spread(0.3, 0.3);
	private static final TargetProperties tp = TargetProperties.line(7, 2, TargetType.ENEMY);
	private int damage, electrified, hitsRequired;

	public LightningRod(boolean isUpgraded) {
		super(ID, "Lightning Rod", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(60, 20, 4, tp.range));
		damage = isUpgraded ? 500 : 300;
		electrified = isUpgraded ? 300 : 200;
		hitsRequired = isUpgraded ? 7 : 10;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Equipment eq = this;
		ActionMeta stacks = new ActionMeta();
		ActionMeta hitCount = new ActionMeta();
		ItemStack icon = item.clone();
		ItemStack activeIcon = icon.withType(Material.LIGHTNING_ROD);
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);

		// Track lightning damage dealt to accumulate casts
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (!ev.getMeta().containsType(DamageType.LIGHTNING)) return TriggerResult.keep();
			hitCount.addCount(1);
			if (hitCount.getCount() >= hitsRequired) {
				hitCount.addCount(-hitsRequired);
				stacks.addCount(1);
				ItemStack currentIcon = activeIcon.clone();
				currentIcon.setAmount(Math.min(stacks.getCount(), 64));
				inst.setIcon(currentIcon);
			}
			return TriggerResult.keep();
		});

		// On cast: charge 1s then deal lightning damage + electrified in a line
		inst.setAction((pdata, in) -> {
			if (stacks.getCount() <= 0) return TriggerResult.keep();
			stacks.addCount(-1);

			data.channel(20).then(new Runnable() {
				public void run() {
					Player p = data.getPlayer();
					Location start = p.getLocation().add(0, 1, 0);
					Vector dir = p.getEyeLocation().getDirection();
					Location end = start.clone().add(dir.clone().multiply(properties.get(PropertyType.RANGE)));
					ParticleUtil.drawLine(p, part, start, end, 0.3);
					Sounds.thunder.play(p, p);
					for (LivingEntity ent : TargetHelper.getEntitiesInLine(p, start, end, tp)) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.LIGHTNING,
								DamageStatTracker.of(id + slot, eq)), ent);
						FightInstance.applyStatus(ent, StatusType.ELECTRIFIED, data, electrified, -1);
					}
				}
			});

			// Update icon after consuming a stack
			if (stacks.getCount() > 0) {
				ItemStack currentIcon = activeIcon.clone();
				currentIcon.setAmount(Math.min(stacks.getCount(), 64));
				inst.setIcon(currentIcon);
			} else {
				inst.setIcon(icon);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.YELLOW_DYE,
				"Can be cast every " + DescUtil.yellow(hitsRequired) + " times you deal " +
				GlossaryTag.LIGHTNING.tag(this) + " damage, stackable. On cast, " +
				GlossaryTag.CHANNEL.tag(this) + " for <white>1s</white> before dealing " +
				GlossaryTag.LIGHTNING.tag(this, damage, true) + " and applying " +
				GlossaryTag.ELECTRIFIED.tag(this, electrified, true) +
				" in a line in front of you.");
	}
}
