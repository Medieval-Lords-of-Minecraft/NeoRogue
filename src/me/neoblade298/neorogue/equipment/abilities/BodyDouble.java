package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BodyDouble extends Equipment {
	private static final String ID = "bodyDouble";
	private static TargetProperties tp = TargetProperties.radius(12, false, TargetType.ENEMY);
	private int dur;
	
	public BodyDouble(boolean isUpgraded) {
		super(ID, "Body Double", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 0, 15, 0).add(PropertyType.AREA_OF_EFFECT, tp.range));
				dur = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			Sounds.equip.play(p, p);
			ArmorStand as = (ArmorStand) p.getWorld().spawnEntity(p.getLocation(), EntityType.ARMOR_STAND);
			PlayerDisguise dis = new PlayerDisguise(p);
			dis.setName(p.getName() + " Body Double");
			dis.setEntity(as);
			dis.startDisguise();
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				if (!NeoRogue.mythicApi.isMythicMob(ent)) continue;
				NeoRogue.mythicApi.addThreat(ent, as, 100000);
			}
			data.addGuaranteedTask(UUID.randomUUID(), new Runnable() {
				public void run() {
					as.remove();
				}
			}, dur * 20);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OAK_TRAPDOOR,
				"On cast, drop an armor stand " + DescUtil.duration(dur, true) + 
				" that taunts all nearby enemies.");
	}
}
