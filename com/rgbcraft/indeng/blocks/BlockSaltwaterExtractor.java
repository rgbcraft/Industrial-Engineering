package com.rgbcraft.indeng.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSaltwaterExtractor extends BlockContainer {

	protected BlockSaltwaterExtractor(int id) {
		super(id, Material.iron);
		
		setBlockName("blockSaltwaterExtractor");
	}
	
	@Override
	public int getBlockTextureFromSide(int side) {
		switch(side) {
			case 5:
				return 2;
			case 4:
				return 2;
			case 3:
				return 2;
			case 2:
				return 2;
			default:
				return 0;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return null;
	}
	
}
