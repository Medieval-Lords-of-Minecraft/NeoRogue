package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Ammunition;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;

public class Firebird extends Ammunition {
	private static final String ID = "firebird";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	private double mult;
	
	public Firebird(boolean isUpgraded) {
		super(ID, "Firebird", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofAmmunition(20, 0.1, DamageType.FIRE));
				mult = isUpgraded ? 0.2 : 0.3;

	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void onStart(ProjectileInstance inst) {
		inst.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.BURN), Buff.multiplier(inst.getOwner(), mult, StatTracker.damageBuffAlly(this)));
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		pc.play(p, proj.getLocation());
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TIPPED_ARROW, "Increases " + GlossaryTag.BURN.tag(this) + " damage on basic attacks by " + DescUtil.yellow((mult * 100) + "%") + ".");
		PotionMeta pm = (PotionMeta) item.getItemMeta();
		pm.setColor(Color.RED);
		item.setItemMeta(pm);
	}
}
