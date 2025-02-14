package cofh.lib.block.impl.crops;

import cofh.lib.util.helpers.MathHelper;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.PlantType;

import java.util.Random;
import java.util.function.Supplier;

public class StemBlockCoFH extends StemBlock {

    protected final PlantType type;
    protected int growLight;
    protected float growMod;

    protected Supplier<Block> cropBlock = () -> Blocks.MELON;
    protected Supplier<Item> seed = () -> Items.MELON_SEEDS;

    public StemBlockCoFH(Properties builder, PlantType type, int growLight, float growMod) {

        super((StemGrownBlock) Blocks.MELON, builder);
        this.type = type;
        this.growLight = growLight;
        this.growMod = growMod;
    }

    public StemBlockCoFH(Properties builder, int growLight, float growMod) {

        this(builder, PlantType.CROP, growLight, growMod);
    }

    public StemBlockCoFH(Properties builder) {

        this(builder, PlantType.CROP, 9, 1.0F);
    }

    public StemBlockCoFH crop(Supplier<Block> crop) {

        this.cropBlock = crop;
        return this;
    }

    public StemBlockCoFH seed(Supplier<Item> seed) {

        this.seed = seed;
        return this;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {

        if (!worldIn.isAreaLoaded(pos, 1)) {
            return;
        }
        if (worldIn.getRawBrightness(pos, 0) >= growLight) {
            float growthChance = MathHelper.maxF(CropsBlockCoFH.getGrowthChanceProxy(this, worldIn, pos) * growMod, 0.1F);
            if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, random.nextInt((int) (25.0F / growthChance) + 1) == 0)) {
                int i = state.getValue(AGE);
                if (i < 7) {
                    worldIn.setBlock(pos, state.setValue(AGE, i + 1), 2);
                } else {
                    Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    BlockPos blockpos = pos.relative(direction);
                    BlockState soil = worldIn.getBlockState(blockpos.below());
                    Block block = soil.getBlock();
                    if (worldIn.isEmptyBlock(blockpos) && (soil.canSustainPlant(worldIn, blockpos.below(), Direction.UP, this) || block == Blocks.FARMLAND || block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.PODZOL || block == Blocks.GRASS_BLOCK)) {
                        worldIn.setBlockAndUpdate(blockpos, this.cropBlock.get().defaultBlockState());
                        worldIn.setBlockAndUpdate(pos, ((StemGrownBlock) this.cropBlock.get()).getAttachedStem().defaultBlockState().setValue(HorizontalBlock.FACING, direction));
                    }
                }
                ForgeHooks.onCropsGrowPost(worldIn, pos, state);
            }
        }
    }

    @Override
    protected Item getSeedItem() {

        return seed.get();
    }

}
