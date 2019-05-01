/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.machines.quarry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEmptyDrops;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static net.minecraft.state.properties.BlockStateProperties.*;

public class BlockFrame extends BlockEmptyDrops {

    /**
     * Whether this fence connects in the northern direction
     */
    public static final BooleanProperty DAMMING = BooleanProperty.create("damming");
    public static final VoxelShape BOX_AABB = VoxelShapes.create(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    public static final VoxelShape North_AABB = VoxelShapes.create(0.25, 0.25, 0, 0.75, 0.75, 0.25);
    public static final VoxelShape South_AABB = VoxelShapes.create(0.25, 0.25, .75, 0.75, 0.75, 1);
    public static final VoxelShape West_AABB = VoxelShapes.create(0, 0.25, 0.25, .25, 0.75, 0.75);
    public static final VoxelShape East_AABB = VoxelShapes.create(.75, 0.25, 0.25, 1, 0.75, 0.75);
    public static final VoxelShape UP_AABB = VoxelShapes.create(0.25, .75, 0.25, 0.75, 1, 0.75);
    public static final VoxelShape Down_AABB = VoxelShapes.create(0.25, 0, 0.25, 0.75, .25, 0.75);
    private static final BiPredicate<World, BlockPos> HAS_NEIGHBOUR_LIQUID = (world, pos) ->
        Stream.of(EnumFacing.values()).map(pos::offset).map(world::getBlockState)
            .anyMatch(state -> !state.isFullCube() && state.getMaterial().isLiquid());

    public final ItemBlock itemBlock;

    public BlockFrame() {
        super(Properties.create(Material.GLASS).hardnessAndResistance(0.5f));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.frame);
        this.setDefaultState(this.getStateContainer().getBaseState()
            .with(NORTH, false).with(EAST, false).with(SOUTH, false)
            .with(WEST, false).with(UP, false).with(DOWN, false)
            .with(DAMMING, false));
        itemBlock = new ItemBlock(this, new Item.Properties().group(Holder.tab()));
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.frame);
    }

    @Override
    public Item asItem() {
        return itemBlock;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, DAMMING);
    }

    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        World worldIn = context.getWorld();
        BlockPos pos = context.getPos();
        return this.getDefaultState()
            .with(NORTH, canConnectTo(worldIn, pos.north()))
            .with(EAST, canConnectTo(worldIn, pos.east()))
            .with(SOUTH, canConnectTo(worldIn, pos.south()))
            .with(WEST, canConnectTo(worldIn, pos.west()))
            .with(DOWN, canConnectTo(worldIn, pos.down()))
            .with(UP, canConnectTo(worldIn, pos.up()));
    }

    private boolean breaking = false;

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!Config.common().disableFrameChainBreak().get()) {
                boolean firstBreak;
                if (breaking)
                    firstBreak = false;
                else {
                    firstBreak = true;
                    breaking = true;
                }
                if (firstBreak) {
                    if (!HAS_NEIGHBOUR_LIQUID.test(worldIn, pos)) {
                        breakChain(worldIn, pos);
                    }
                    breaking = false;
                }
//        if (Config.content().debug())
//            QuarryPlus.LOGGER.info("Frame broken at " + pos + (d ? " neighbor Fluid" : ""));
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @SuppressWarnings("unchecked")
    private void breakChain(World world, BlockPos first) {
        if (!world.isRemote) {
            Set<BlockPos> set = new HashSet<>();
            set.add(first);
            ArrayList<BlockPos> nextCheck = new ArrayList<>();
            nextCheck.add(first);
            while (!nextCheck.isEmpty()) {
                List<BlockPos> list = (List<BlockPos>) nextCheck.clone();
                nextCheck.clear();
                for (BlockPos pos : list) {
                    for (EnumFacing dir : EnumFacing.values()) {
                        BlockPos nPos = pos.offset(dir);
                        IBlockState nBlock = world.getBlockState(nPos);
                        if (nBlock.getBlock() == this) {
                            if (!HAS_NEIGHBOUR_LIQUID.test(world, nPos) && set.add(nPos))
                                nextCheck.add(nPos);
                        }
                    }
                }
            }
            set.forEach(world::removeBlock);
        }
    }

    private boolean canConnectTo(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos).getBlock() == this;
    }

    @Override
    @SuppressWarnings("deprecation")
    @OnlyIn(Dist.CLIENT)
    public boolean isSideInvisible(IBlockState state, IBlockState adjacentBlockState, EnumFacing side) {
        return true;
    }

    public IBlockState getDammingState() {
        return getDefaultState().with(DAMMING, true);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        VoxelShape aabb = BOX_AABB;
        if (state.get(NORTH)) {
            aabb = VoxelShapes.or(aabb, North_AABB);
        }
        if (state.get(SOUTH)) {
            aabb = VoxelShapes.or(aabb, South_AABB);
        }
        if (state.get(WEST)) {
            aabb = VoxelShapes.or(aabb, West_AABB);
        }
        if (state.get(EAST)) {
            aabb = VoxelShapes.or(aabb, East_AABB);
        }
        if (state.get(UP)) {
            aabb = VoxelShapes.or(aabb, UP_AABB);
        }
        if (state.get(DOWN)) {
            aabb = VoxelShapes.or(aabb, Down_AABB);
        }
        return aabb;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!Config.common().disableFrameChainBreak().get() && state.get(DAMMING)) {
            worldIn.setBlockState(pos, state.with(DAMMING, HAS_NEIGHBOUR_LIQUID.test(worldIn, pos)), 2);
        }
    }
}
