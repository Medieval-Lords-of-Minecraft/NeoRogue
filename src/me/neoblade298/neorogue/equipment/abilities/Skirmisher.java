package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Skirmisher extends Equipment {
	private static final String ID = "skirmisher";
	private static final TargetProperties props = TargetProperties.radius(5, false, TargetType.ENEMY);
	private static final SoundContainer sound = new SoundContainer(Sound.ENTITY_ALLAY_HURT, 0.8F);
	private int shields;
	
	public Skirmisher(boolean isUpgraded) {
		super(ID, "Skirmisher", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		shields = isUpgraded ? 10 : 6;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, new SkirmisherInstance(id, p, data));
	}
	
	private class SkirmisherInstance extends PriorityAction {
		private int count = 0;
		public SkirmisherInstance(String id, Player p, PlayerFightData data) {
			super(id);
			action = (pdata, in) -> {
				if (++count >= 3) {
					count = 0;
					sound.play(p, p);
					p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0));
					data.addSimpleShield(p.getUniqueId(), shields, 100);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, props)) {
						Vector v = ent.getLocation().subtract(p.getLocation()).toVector().setY(0).normalize().multiply(0.4).setY(0.3);
						FightInstance.knockback(ent, v);
					}
				}
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BAMBOO,
				"Passive. Every third basic attack, knock back all enemies around you, gain speed <white>1</white> for <white>3</white> seconds,"
				+ " and " + GlossaryTag.SHIELDS.tag(this, shields, true) + " for <white>5</white> seconds.");
	}
}
