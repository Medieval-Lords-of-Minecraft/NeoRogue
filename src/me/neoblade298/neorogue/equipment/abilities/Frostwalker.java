package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Frostwalker extends Equipment {
	private static final String ID = "frostwalker";
	private static final TargetProperties tp = TargetProperties.radius(2, false, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD).count(25).spread(2, 0.2).offsetY(0.5);
	private int stacks, reduc;
	private ItemStack activeIcon;
	
	public Frostwalker(boolean isUpgraded) {
		super(ID, "Frostwalker", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		stacks = isUpgraded ? 4 : 3;
		reduc = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		FrostwalkerInstance inst = new FrostwalkerInstance(data);
		data.addTrigger(id, Trigger.PLAYER_TICK, inst);
		EquipmentInstance toggle = new EquipmentInstance(data, this, slot, es);
		toggle.setAction((pdata, in) -> {
			inst.active = !inst.active;
			Sounds.equip.play(p, p);
			toggle.setIcon(inst.active ? activeIcon : item);
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, toggle);
	}

	private class FrostwalkerInstance extends PriorityAction {
		private boolean active = false;
		private LinkedList<Location> pools = new LinkedList<Location>();
		public FrostwalkerInstance(PlayerFightData data) {
			super(ID);

			action = (pdata, in) -> {
				Player p = data.getPlayer();
				HashSet<UUID> hit = new HashSet<UUID>();
				for (Location loc : pools) {
					pc.play(p, loc);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
						if (hit.contains(ent.getUniqueId())) continue;
						FightData fd = FightInstance.getFightData(ent);
						fd.applyStatus(StatusType.FROST, data, stacks, -1);
						fd.addBuff(data, ID, false, false, BuffType.MAGICAL, -reduc, 100);
						hit.add(ent.getUniqueId());
					}
				}

				if (!active) return TriggerResult.keep();
				if (pdata.getMana() < 2) return TriggerResult.keep();
				pdata.addMana(-2);
				pools.add(p.getLocation());
				if (pools.size() > 3) {
					pools.removeFirst();
				}

				return TriggerResult.keep();
			};
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SNOWBALL,
				"Toggleable, off by default. Every second, use <white>2</white> mana to drop a pool of frost that lasts <white>3s</white>. It "
				+ "applies " + GlossaryTag.FROST.tag(this, stacks, true) + " and reduces " + GlossaryTag.MAGICAL.tag(this) + " resistance by " +
				DescUtil.yellow(reduc) + " [<white>5s</white>]. Only one pool of frost may apply to an enemy at a time.");

		activeIcon = item.withType(Material.SNOW_BLOCK);
	}
}
