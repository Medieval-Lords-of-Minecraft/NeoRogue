package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Provoke extends Equipment {
	private static final String ID = "provoke";
	private static final TargetProperties tp = TargetProperties.radius(15, false, TargetType.ENEMY);
	private int threat, shield;
	private static final ParticleContainer taunt = new ParticleContainer(Particle.VILLAGER_ANGRY).count(15).spread(0.1, 0.1).offsetY(2);
	
	public Provoke(boolean isUpgraded) {
		super(ID, "Provoke", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 10, isUpgraded ? 7 : 10, tp.range));
		threat = 1000;
		shield = isUpgraded ? 15 : 8;
		taunt.count(50).spread(0.5, 0.5).speed(0.2);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			data.addSimpleShield(p.getUniqueId(), shield, 100);
			for (LivingEntity ent : TargetHelper.getEntitiesInSight(p, tp)) {
				ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 0));
				NeoRogue.mythicApi.addThreat(ent, p, threat);
				taunt.play(p, ent);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_TORCH,
				"On cast, " + GlossaryTag.THREATEN.tag(this) + " all enemies you're looking at for <white>" + threat + "</white>"
						+ ", slow them for <white>1s</white>, and gain a shield of <yellow>" 
						+ shield + "</yellow> for <white>5</white> seconds.");
	}
}
