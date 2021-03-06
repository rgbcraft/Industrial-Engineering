package com.rgbcraft.usefuladditions.tiles;

import java.util.HashMap;
import java.util.Map;

import com.rgbcraft.usefuladditions.api.IDebuggable;
import com.rgbcraft.usefuladditions.containers.ContainerOsmosisGenerator;
import com.rgbcraft.usefuladditions.items.Items;
import com.rgbcraft.usefuladditions.liquids.Liquids;
import com.rgbcraft.usefuladditions.utils.IRoteableTile;
import com.rgbcraft.usefuladditions.utils.LanguageManager;
import com.rgbcraft.usefuladditions.utils.Utils;

import buildcraft.api.core.Position;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileSourceEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;


public class TileOsmosisGenerator extends TileInventory implements ITankContainer, IEnergySource, ISidedInventory, IDebuggable, IRoteableTile {

    private boolean added = false;
    public LiquidTank tank;

    public TileOsmosisGenerator() {
        super("container.osmosisGenerator", 2);

        this.tank = new LiquidTank(10000);
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote) {
            boolean isWorking = false;
            EnergyTileSourceEvent sourceEvent = new EnergyTileSourceEvent(this, 0);

            if (!this.added) {
                EnergyTileLoadEvent loadevent = new EnergyTileLoadEvent(this);
                MinecraftForge.EVENT_BUS.post(loadevent);
                this.added = true;
            }

            if (this.getStackInSlot(0) != null && this.getStackInSlot(0).isItemEqual(new ItemStack(Items.get("canister"), 1, 5)) && this.canAddToSlot(1, 1) && (this.tank.getLiquid() != null ? this.tank.getLiquid().amount : 0) <= this.tank.getCapacity() - 1000) {
                this.fill(0, LiquidDictionary.getLiquid("usefuladditions.saltWater", 1000), true);
                this.decrStackSize(0, 1);
                this.addToSlot(1, new ItemStack(Items.get("canister"), 1, 0));
            }

            if (this.tank.getLiquid() != null && !Utils.isRedstonePowered(this.worldObj, this.xCoord, this.yCoord, this.zCoord)) {
                if (this.tank.getLiquid().amount >= 50 && this.yCoord == 69) {
                    isWorking = true;
                    this.tank.drain(50, true);
                    sourceEvent.amount = 4;
                }

                MinecraftForge.EVENT_BUS.post(sourceEvent);
            }

            byte[] data = Utils.unmergeBits((byte) this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord));
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, Utils.mergeBits((byte) (isWorking ? 1 : 0), data[1]));
        }
    }

    public void unloadTile() {
        if (this.added) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            this.added = false;
        }
    }

    @Override
    public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
        return this.fill(0, resource, doFill);
    }

    @Override
    public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
        if (tankIndex == 0) {
            if (!resource.isLiquidEqual(LiquidDictionary.getLiquid("usefuladditions.saltWater", resource.amount)))
                return 0;

            if (this.tank.getLiquid() == null && resource.amount <= this.tank.getCapacity() || this.tank.getLiquid().amount + resource.amount <= this.tank.getCapacity()) {
                this.tank.fill(resource, doFill);
                return resource.amount;
            }
        }
        return 0;
    }

    @Override
    public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
        if (tankIndex == 0)
            return this.tank.drain(maxDrain, doDrain);
        return null;
    }

    @Override
    public ILiquidTank[] getTanks(ForgeDirection direction) {
        return new ILiquidTank[] {this.tank};
    }

    @Override
    public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
        return this.tank;
    }

    @Override
    public boolean isAddedToEnergyNet() {
        return this.added;
    }

    @Override
    public int getMaxEnergyOutput() {
        return 32;
    }

    @Override
    public boolean emitsEnergyTo(TileEntity receiver, ic2.api.Direction direction) {
        return true;
    }

    public void getGUIUpdateData(int key, int value) {
        switch (key) {
            case 0:
                LiquidStack liquid = this.tank.getLiquid();

                if (liquid == null)
                    liquid = Liquids.get("saltWater");

                liquid.amount = value;
                this.tank.setLiquid(liquid);
                break;
            default:
                System.err.println("Industrial Engineering: " + this.toString() + " got unknown GUI network data for key " + key + ": " + value);
                break;
        }
    }

    public void sendGUIUpdateData(ContainerOsmosisGenerator container, ICrafting iCrafting) {
        iCrafting.sendProgressBarUpdate(container, 0, this.getTankAmount());
    }

    @Override
    public int getStartInventorySide(ForgeDirection side) {
        switch (side) {
            case UP:
                return 0;
            default:
                return 1;
        }
    }

    @Override
    public int getSizeInventorySide(ForgeDirection side) {
        return 1;
    }

    @Override
    public Map<String, Boolean> getRequirements(EntityPlayer player, HashMap<String, Boolean> requirements) {
        requirements.put(LanguageManager.getFormattedTranslation("misc.osmosisGenerator.debug.requirement1", this.yCoord), this.yCoord == 69);
        return requirements;
    }

    @Override
    public Map<String, String> getAdditionalAdvancedInfos(EntityPlayer player, HashMap<String, String> additionalInfos) {
        return null;
    }

    @Override
    public void writeToNBT(final NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (this.tank.getLiquid() != null) {
            NBTTagCompound tag = new NBTTagCompound();
            this.tank.getLiquid().writeToNBT(tag);
            compound.setCompoundTag("tank", tag);
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);

        this.tank.setLiquid(LiquidStack.loadLiquidStackFromNBT(compound.getCompoundTag("tank")));
    }

    public int getTankAmount() {
        return this.tank.getLiquid() != null ? this.tank.getLiquid().amount : 0;
    }

    @Override
    public int getRotation(World world, int x, int y, int z, EntityPlayer entityPlayer, int side) {
        byte[] data = Utils.unmergeBits((byte) world.getBlockMetadata(x, y, z));
        return Utils.mergeBits(data[0], (byte) Utils.get2dOrientation(new Position(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ), new Position(x, y, z)).getOpposite().ordinal());
    }

}
