package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.PotionProjectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class Blind extends Equipment {
	private static final String ID = "blind";
	private int injure;
	
	public Blind(boolean isUpgraded) {
		super(ID, "Blind", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 15, 0));
		injure = isUpgraded ? 45 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		HashSet<UUID> blinded = new HashSet<UUID>();
		PotionProjectile pot = new PotionProjectile((loc, hit) -> {
			for (LivingEntity ent : hit) {
				if (ent instanceof Player || !(ent instanceof LivingEntity)) continue;
				ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
				blinded.add(ent.getUniqueId());
			}

			data.addTask(new BukkitRunnable() {
				public void run() {
					blinded.clear();
				}
			}.runTaskLater(NeoRogue.inst(), 60));
		});
		ProjectileGroup grp = new ProjectileGroup(pot);
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Sounds.threw.play(p, p);
			grp.start(data);
			return TriggerResult.keep();
		}));

		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (blinded.contains(ev.getTarget().getUniqueId())) {
				FightInstance.applyStatus(ev.getTarget(), StatusType.INJURY, data, injure, -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LIGHT,
				"On cast, throw an orb of light that applies Slowness 1 [<white>3s</white>]. During this time, dealing damage to enemies hit " +
				"applies " + GlossaryTag.INJURY.tag(this, injure, true) + ".");
	}
}
