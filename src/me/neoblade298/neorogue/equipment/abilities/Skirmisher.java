package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Skirmisher extends Equipment {
	private static final String ID = "Skirmisher";
	private static final TargetProperties props = TargetProperties.radius(3, false, TargetType.ENEMY);
	private static final SoundContainer sound = new SoundContainer(Sound.ENTITY_ALLAY_HURT, 0.8F);
	private int shields;
	
	public Skirmisher(boolean isUpgraded) {
		super(ID, "Skirmisher", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		shields = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 6;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			data.addTask(new BukkitRunnable() {
				public void run() {
					data.addTrigger(id + "-active", Trigger.PRE_BASIC_ATTACK, new SkirmisherInstance(id, data));
				}
			}.runTask(NeoRogue.inst()));

			return TriggerResult.remove();
		});
	}
	
	private class SkirmisherInstance extends PriorityAction {
		private int count = 0;
		public SkirmisherInstance(String id, PlayerFightData data) {
			super(id);
			action = (pdata, in) -> {
				Player p = data.getPlayer();
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
				GlossaryTag.POWER.tag(this) + ". Activates after basic attacking " + DescUtil.white(ACTIVATION_THRES) + " times. Every third basic attack, knock back all enemies around you, gain speed " + DescUtil.white(1) + " " + DescUtil.duration(3, false) + ","
				+ " and " + GlossaryTag.SHIELDS.tag(this, shields, true) + " " + DescUtil.duration(5, false) + ".");
	}
}
