package cofh.lib.item.impl;

import cofh.lib.item.ICoFHItem;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static cofh.lib.util.Utils.getItemEnchantmentLevel;
import static cofh.lib.util.constants.Constants.TRUE;

public class ArrowItemCoFH extends ArrowItem implements ICoFHItem {

    protected BooleanSupplier showInGroups = TRUE;

    protected Supplier<ItemGroup> displayGroup;

    protected final IArrowFactory<? extends AbstractArrowEntity> factory;
    protected boolean infinitySupport = false;

    public ArrowItemCoFH(IArrowFactory<? extends AbstractArrowEntity> factory, Properties builder) {

        super(builder);
        this.factory = factory;

        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
    }

    public ArrowItemCoFH setDisplayGroup(Supplier<ItemGroup> displayGroup) {

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
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {

        if (!showInGroups.getAsBoolean() || displayGroup != null && displayGroup.get() != null && displayGroup.get() != group) {
            return;
        }
        super.fillItemCategory(group, items);
    }

    @Override
    public Collection<ItemGroup> getCreativeTabs() {

        return displayGroup != null && displayGroup.get() != null ? Collections.singletonList(displayGroup.get()) : super.getCreativeTabs();
    }

    @Override
    public AbstractArrowEntity createArrow(World worldIn, ItemStack stack, LivingEntity shooter) {

        return factory.createArrow(worldIn, shooter);
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, PlayerEntity player) {

        return infinitySupport && getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0 || super.isInfinite(stack, bow, player);
    }

    // region FACTORY
    public interface IArrowFactory<T extends AbstractArrowEntity> {

        T createArrow(World world, LivingEntity living);

        T createArrow(World world, double posX, double posY, double posZ);

    }
    // endregion

    // region DISPENSER BEHAVIOR
    private static final ProjectileDispenseBehavior DISPENSER_BEHAVIOR = new ProjectileDispenseBehavior() {

        @Override
        protected ProjectileEntity getProjectile(World worldIn, IPosition position, ItemStack stackIn) {

            ArrowItemCoFH arrowItem = ((ArrowItemCoFH) stackIn.getItem());
            AbstractArrowEntity arrow = arrowItem.factory.createArrow(worldIn, position.x(), position.y(), position.z());
            arrow.pickup = AbstractArrowEntity.PickupStatus.ALLOWED;
            return arrow;
        }
    };
    // endregion
}
