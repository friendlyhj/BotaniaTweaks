package quaternary.botaniatweaks.modules.shared.helper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import quaternary.botaniatweaks.modules.botania.block.BlockCompressedTinyPotato;
import vazkii.botania.common.block.decor.BlockTinyPotato;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class MiscHelpers {
	private static ModContainer botaniaModContainer = null;
	
	public static ModContainer getBotaniaModContainer() {
		if(botaniaModContainer != null) return botaniaModContainer;
		
		for(ModContainer container : Loader.instance().getActiveModList()) {
			if(container.getModId().equals("botania")) {
				botaniaModContainer = container; break;
			}
		}
		
		if(botaniaModContainer == null) throw new RuntimeException("Couldn't find Botania's mod container, is it not present? Whats goin on here");
		
		return botaniaModContainer;
	}
	
	public static void makeNonFinal(Field f) {
		Field fModifiers;
		try {
			fModifiers = f.getClass().getDeclaredField("modifiers");
			fModifiers.setAccessible(true);
			fModifiers.set(f, f.getModifiers() & ~Modifier.FINAL);
		} catch (Exception e) {
			throw new RuntimeException("java machine broke", e);
		}
	}
	
	//This is EXTREMELY DUMB
	//no u
	public static ItemStack stackFromState(IBlockState state) {
		if(state == null) return ItemStack.EMPTY;
		Block block = state.getBlock();
		if (block instanceof IFluidBlock) {
			return FluidUtil.getFilledBucket(new FluidStack(((IFluidBlock) block).getFluid(), Fluid.BUCKET_VOLUME));
		}
		if (block == Blocks.WATER) return new ItemStack(Items.WATER_BUCKET);
		if (block == Blocks.LAVA) return new ItemStack(Items.LAVA_BUCKET);
		
		try {
			//this is a forge added method that takes a world, hit vector, few other things
			//and we don't have any of those! so let's just null them all and hope for the best
			//(usually this delegates to a vanilla method that just returns a new itemstack
			//with item.getitemfromblock and whatever datavalue damageDropped gives)
			return block.getPickBlock(state, null, null, null, null); //Ughhhh
		} catch(Exception e) {
			//oof
		}
		
		//ok that didn't work, as a last-ditch effort try emulating vanilla block#getitem
		Item item = Item.getItemFromBlock(block);
		if(item == Items.AIR)	return ItemStack.EMPTY;
		else return new ItemStack(item, 1, block.getMetaFromState(state));
	}
	
	public static List<ItemStack> getAllSubtypes(Iterable<ItemStack> stacks) {
		ArrayList<ItemStack> ret = new ArrayList<>();
		
		for(ItemStack stack : stacks) {
			for(CreativeTabs tab : stack.getItem().getCreativeTabs()) {
				NonNullList<ItemStack> subs = NonNullList.create();
				try {
					stack.getItem().getSubItems(tab, subs);
				} catch (Exception ignore) {}
				
				ret.addAll(subs);
			}
		}
		
		return ret;
	}
	
	public static int getPotatoCompressionLevel(Block b) {
		if(b instanceof BlockCompressedTinyPotato) {
			return ((BlockCompressedTinyPotato)b).compressionLevel;
		}
		
		if(b instanceof BlockTinyPotato) {
			return 0;
		}
		return -1;
	}
	
	public static void sendMeOrMySonChat(EntityPlayer p, int clickedCompression, int heldCompression) {		
		TextComponentTranslation son = new TextComponentTranslation("botania_tweaks.son." + (heldCompression - clickedCompression));
		TextComponentTranslation meormy = new TextComponentTranslation("botania_tweaks.donttalktome", son);
		p.sendMessage(meormy);
	}
}
