package openblocks.common.block;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import openmods.block.OpenBlock;
import openmods.geometry.BlockSpaceTransform;
import openmods.geometry.Orientation;
import openmods.infobook.BookDocumentation;
import openmods.model.eval.EvalModelState;

@BookDocumentation
public class BlockSprinkler extends OpenBlock.TwoDirections {

	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.3, 0.0, 0.0, 0.7, 0.3, 1.0);

	public BlockSprinkler() {
		super(Material.IRON);
		setDefaultState(getDefaultState().withProperty(BlockLiquid.LEVEL, 1).withProperty(Properties.StaticProperty, true));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		// 1.8.9 Hack, crashes otherwise
		return new ExtendedBlockState(this,
				new IProperty[] { getPropertyOrientation(), BlockLiquid.LEVEL, Properties.StaticProperty },
				new IUnlistedProperty[] { EvalModelState.PROPERTY });
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		final Orientation orientation = getOrientation(state);
		return BlockSpaceTransform.instance.mapBlockToWorld(orientation, AABB);
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
		return isOnTopOfSolidBlock(world, pos, side);
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
}
