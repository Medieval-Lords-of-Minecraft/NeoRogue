package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Challenge extends Equipment {
	private static final TargetProperties tp = TargetProperties.radius(15, false, TargetType.ENEMY);
	private int threat, shield;
	private ParticleContainer taunt = new ParticleContainer(Particle.VILLAGER_ANGRY).count(15).spread(0.1, 0.1).offsetY(2);
	
	public Challenge(boolean isUpgraded) {
		super("challenge", "challenge", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 20, isUpgraded ? 8 : 10, tp.range));
		properties.addUpgrades(PropertyType.COOLDOWN);
		threat = 1000;
		shield = isUpgraded ? 40 : 30;
		taunt.count(50).spread(0.5, 0.5).speed(0.2);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pd, in) -> {
			data.addSimpleShield(p.getUniqueId(), shield, 100);
			for (LivingEntity ent : TargetHelper.getEntitiesInSight(p, tp)) {
				ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 0));
				NeoRogue.mythicApi.addThreat(ent, p, threat);
				taunt.spawn(ent);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				"On cast, " + GlossaryTag.THREATEN.tag(this) + " all enemies you're looking at for <white>" + threat + "</white> "
						+ ", slow them for <white>1s</white>, and gain a shield of <yellow>" 
						+ shield + "</yellow> for <white>10</white> seconds.");
	}
}
