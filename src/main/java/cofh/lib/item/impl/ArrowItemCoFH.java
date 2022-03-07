package cofh.lib.item.impl;

import cofh.lib.item.ICoFHItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static cofh.lib.util.Utils.getItemEnchantmentLevel;
import static cofh.lib.util.constants.Constants.TRUE;

public class ArrowItemCoFH extends ArrowItem implements ICoFHItem {

    protected BooleanSupplier showInGroups = TRUE;

    protected Supplier<CreativeModeTab> displayGroup;

    protected final IArrowFactory<? extends AbstractArrow> factory;
    protected boolean infinitySupport = false;

    public ArrowItemCoFH(IArrowFactory<? extends AbstractArrow> factory, Properties builder) {

        super(builder);
        this.factory = factory;

        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
    }

    public ArrowItemCoFH setDisplayGroup(Supplier<CreativeModeTab> displayGroup) {

        this.displayGroup = displayGroup;
        return this;
    }

    public ArrowItemCoFH setShowInGroups(BooleanSupplier showInGroups) {

        this.showInGroups = showInGroups;
        return this;
    }

    public ArrowItemCoFH setInfinitySupport(boolean infinitySupport) {

        this.infinitySupport = infinitySupport;
        return this;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {

        if (!showInGroups.getAsBoolean() || displayGroup != null && displayGroup.get() != null && displayGroup.get() != group) {
            return;
        }
        super.fillItemCategory(group, items);
    }

    @Override
    public Collection<CreativeModeTab> getCreativeTabs() {

        return displayGroup != null && displayGroup.get() != null ? Collections.singletonList(displayGroup.get()) : super.getCreativeTabs();
    }

    @Override
    public AbstractArrow createArrow(Level worldIn, ItemStack stack, LivingEntity shooter) {

        return factory.createArrow(worldIn, shooter);
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {

        return infinitySupport && getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0 || super.isInfinite(stack, bow, player);
    }

    // region FACTORY
    public interface IArrowFactory<T extends AbstractArrow> {

        T createArrow(Level world, LivingEntity living);

        T createArrow(Level world, double posX, double posY, double posZ);

    }
    // endregion

    // region DISPENSER BEHAVIOR
    private static final AbstractProjectileDispenseBehavior DISPENSER_BEHAVIOR = new AbstractProjectileDispenseBehavior() {

        @Override
        protected Projectile getProjectile(Level worldIn, Position position, ItemStack stackIn) {

            ArrowItemCoFH arrowItem = ((ArrowItemCoFH) stackIn.getItem());
            AbstractArrow arrow = arrowItem.factory.createArrow(worldIn, position.x(), position.y(), position.z());
            arrow.pickup = AbstractArrow.Pickup.ALLOWED;
            return arrow;
        }
    };
    // endregion
}
