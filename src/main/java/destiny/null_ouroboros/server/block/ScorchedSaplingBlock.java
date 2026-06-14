package destiny.null_ouroboros.server.block;

import destiny.null_ouroboros.server.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;

public class ScorchedSaplingBlock extends SaplingBlock {
    private final AbstractTreeGrower treeGrower;

    public ScorchedSaplingBlock(AbstractTreeGrower pTreeGrower, Properties pProperties) {
        super(pTreeGrower, pProperties);
        this.treeGrower = pTreeGrower;
    }

    @Override
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource randomSource) {
        this.treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, randomSource);
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pLevel.isAreaLoaded(pPos, 1) && mayPlaceOn(pLevel.getBlockState(pPos.below()), pLevel, pPos)) {
            this.advanceTree(pLevel, pPos, pState, pRandom);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()) == BlockRegistry.ASH_BLOCK.get().defaultBlockState();
    }

    @Override
    protected boolean mayPlaceOn(BlockState stateBelow, BlockGetter getter, BlockPos pos) {
        return stateBelow.is(BlockRegistry.ASH_BLOCK.get());
    }
}