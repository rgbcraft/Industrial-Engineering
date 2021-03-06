package com.rgbcraft.usefuladditions.items;

import java.util.List;

import com.rgbcraft.usefuladditions.utils.LanguageManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;


public class ItemTPS extends ItemFood {

    public ItemTPS(int id) {
        super(id, 16, 1F, true);

        this.setItemName("toiletPaperSandwich");
        this.setTextureFile(Items.textureFile);
        this.setIconIndex(255);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List toolTip, boolean parBool) {
        if (GuiScreen.isShiftKeyDown()) {
            toolTip.add(LanguageManager.getTranslation("item.toiletPaperSandwich.desc.line1"));
            toolTip.add(LanguageManager.getTranslation("item.toiletPaperSandwich.desc.line2"));
        }
    }

}
