package me.neoblade298.neorogue.equipment.abilities;

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
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class AbsoluteZero extends Equipment {
	private static final String ID = "AbsoluteZero";
	private static final ParticleContainer pc = new ParticleContainer(Particle.SNOWFLAKE).count(100).spread(5, 1).speed(0.3);
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private int thres, frost;
	
	public AbsoluteZero(boolean isUpgraded) {
		super(ID, "Absolute Zero", isUpgraded, Rarity.RARE, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.none());
		thres = isUpgraded ? 8 : 12;
		frost = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		ItemStack charged = item.clone().withType(Material.PACKED_ICE);
		ActionMeta am = new ActionMeta();
		am.setCount(0);
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es);
		
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			am.addCount(1);
			
			if (am.getCount() >= thres) {
				Player p = data.getPlayer();
				am.setCount(0);
				icon.setAmount(1);
				inst.setIcon(icon);
				
				// Play effects
				pc.play(p, p);
				Sounds.glass.play(p, p);
				
				// Apply frost and double existing frost in radius
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
					FightData fd = FightInstance.getFightData(ent);
					
					// Get current frost stacks
					int currentFrost = fd.getStatus(StatusType.FROST).getStacks();
					
					// Apply new frost and double existing frost
					int totalFrostToApply = frost + currentFrost;
					FightInstance.applyStatus(ent, StatusType.FROST, data, totalFrostToApply, -1);
				}
			} else {
				// Update icon count
				int count = am.getCount();
				if (count >= thres - 1) {
					// Show charged version
					charged.setAmount(count);
					inst.setIcon(charged);
				} else {
					icon.setAmount(count);
					inst.setIcon(icon);
				}
			}
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.ICE,
				"Passive. Every " + DescUtil.yellow(thres) + " times you deal damage, apply " + 
				GlossaryTag.FROST.tag(this, frost, true) + " in a wide radius around you and double " +
				GlossaryTag.FROST.tag(this) + " on all affected enemies.");
	}
}
