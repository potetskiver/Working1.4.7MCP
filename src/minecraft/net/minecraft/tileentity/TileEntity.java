package net.minecraft.tileentity;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.block.TileEntityRecordPlayer;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntity
{
    /**
     * A HashMap storing string names of classes mapping to the actual java.lang.Class type.
     */
    private static Map nameToClassMap = new HashMap();

    /**
     * A HashMap storing the classes and mapping to the string names (reverse of nameToClassMap).
     */
    private static Map classToNameMap = new HashMap();

    /** The reference to the world. */
    public World worldObj;

    /** The x coordinate of the tile entity. */
    public int xCoord;

    /** The y coordinate of the tile entity. */
    public int yCoord;

    /** The z coordinate of the tile entity. */
    public int zCoord;
    protected boolean tileEntityInvalid;
    public int blockMetadata = -1;

    /** the Block type that this TileEntity is contained within */
    public Block blockType;

    /**
     * Adds a new two-way mapping between the class and its string name in both hashmaps.
     */
    public static void addMapping(Class par0Class, String par1Str)
    {
        if (nameToClassMap.containsKey(par1Str))
        {
            throw new IllegalArgumentException("Duplicate id: " + par1Str);
        }
        else
        {
            nameToClassMap.put(par1Str, par0Class);
            classToNameMap.put(par0Class, par1Str);
        }
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns the worldObj for this tileEntity.
     */
    public World getWorldObj()
    {
        return this.worldObj;
    }

    /**
     * Sets the worldObj for this tileEntity.
     */
    public void setWorldObj(World par1World)
    {
        this.worldObj = par1World;
    }

    public boolean func_70309_m()
    {
        return this.worldObj != null;
    }

    /**
     * Reads a tile entity from NBT.
     */
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        this.xCoord = par1NBTTagCompound.getInteger("x");
        this.yCoord = par1NBTTagCompound.getInteger("y");
        this.zCoord = par1NBTTagCompound.getInteger("z");
    }

    /**
     * Writes a tile entity to NBT.
     */
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        String var2 = (String)classToNameMap.get(this.getClass());

        if (var2 == null)
        {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        }
        else
        {
            par1NBTTagCompound.setString("id", var2);
            par1NBTTagCompound.setInteger("x", this.xCoord);
            par1NBTTagCompound.setInteger("y", this.yCoord);
            par1NBTTagCompound.setInteger("z", this.zCoord);
        }
    }

    /**
     * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
     * ticks and creates a new spawn inside its implementation.
     */
    public void updateEntity() {}

    /**
     * Creates a new entity and loads its data from the specified NBT.
     */
    public static TileEntity createAndLoadEntity(NBTTagCompound par0NBTTagCompound)
    {
        TileEntity var1 = null;

        Class var2 = null;

        try
        {
            var2 = (Class)nameToClassMap.get(par0NBTTagCompound.getString("id"));

            if (var2 != null)
            {
                var1 = (TileEntity)var2.newInstance();
            }
        }
        catch (Exception var3)
        {
            var3.printStackTrace();
        }

        if (var1 != null)
        {
            try
            {
                var1.readFromNBT(par0NBTTagCompound);
            }
            catch (Exception e)
            {
                FMLLog.log(Level.SEVERE, e,
                        "A TileEntity %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                        par0NBTTagCompound.getString("id"), var2.getName());
                var1 = null;
            }
        }
        else
        {
            System.out.println("Skipping TileEntity with id " + par0NBTTagCompound.getString("id"));
        }

        return var1;
    }

    /**
     * Returns block data at the location of this entity (client-only).
     */
    public int getBlockMetadata()
    {
        if (this.blockMetadata == -1)
        {
            this.blockMetadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
        }

        return this.blockMetadata;
    }

    /**
     * Called when an the contents of an Inventory change, usually
     */
    public void onInventoryChanged()
    {
        if (this.worldObj != null)
        {
            this.blockMetadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
            this.worldObj.updateTileEntityChunkAndDoNothing(this.xCoord, this.yCoord, this.zCoord, this);
        }
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns the square of the distance between this entity and the passed in coordinates.
     */
    public double getDistanceFrom(double par1, double par3, double par5)
    {
        double var7 = (double)this.xCoord + 0.5D - par1;
        double var9 = (double)this.yCoord + 0.5D - par3;
        double var11 = (double)this.zCoord + 0.5D - par5;
        return var7 * var7 + var9 * var9 + var11 * var11;
    }

    @SideOnly(Side.CLIENT)
    public double func_82115_m()
    {
        return 4096.0D;
    }

    /**
     * Gets the block type at the location of this entity (client-only).
     */
    public Block getBlockType()
    {
        if (this.blockType == null)
        {
            this.blockType = Block.blocksList[this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord)];
        }

        return this.blockType;
    }

    /**
     * Overriden in a sign to provide the text.
     */
    public Packet getDescriptionPacket()
    {
        return null;
    }

    /**
     * returns true if tile entity is invalid, false otherwise
     */
    public boolean isInvalid()
    {
        return this.tileEntityInvalid;
    }

    /**
     * invalidates a tile entity
     */
    public void invalidate()
    {
        this.tileEntityInvalid = true;
    }

    /**
     * validates a tile entity
     */
    public void validate()
    {
        this.tileEntityInvalid = false;
    }

    /**
     * Called when a client event is received with the event number and argument, see World.sendClientEvent
     */
    public void receiveClientEvent(int par1, int par2) {}

    /**
     * Causes the TileEntity to reset all it's cached values for it's container block, blockID, metaData and in the case
     * of chests, the adjcacent chest check
     */
    public void updateContainingBlockInfo()
    {
        this.blockType = null;
        this.blockMetadata = -1;
    }

    public void func_85027_a(CrashReportCategory par1CrashReportCategory)
    {
        par1CrashReportCategory.addCrashSectionCallable("Name", new CallableTileEntityName(this));
        CrashReportCategory.func_85068_a(par1CrashReportCategory, this.xCoord, this.yCoord, this.zCoord, this.blockType != null ? this.blockType.blockID : 0, this.blockMetadata);
    }

    static Map func_85028_t()
    {
        return classToNameMap;
    }

    static
    {
        addMapping(TileEntityFurnace.class, "Furnace");
        addMapping(TileEntityChest.class, "Chest");
        addMapping(TileEntityEnderChest.class, "EnderChest");
        addMapping(TileEntityRecordPlayer.class, "RecordPlayer");
        addMapping(TileEntityDispenser.class, "Trap");
        addMapping(TileEntitySign.class, "Sign");
        addMapping(TileEntityMobSpawner.class, "MobSpawner");
        addMapping(TileEntityNote.class, "Music");
        addMapping(TileEntityPiston.class, "Piston");
        addMapping(TileEntityBrewingStand.class, "Cauldron");
        addMapping(TileEntityEnchantmentTable.class, "EnchantTable");
        addMapping(TileEntityEndPortal.class, "Airportal");
        addMapping(TileEntityCommandBlock.class, "Control");
        addMapping(TileEntityBeacon.class, "Beacon");
        addMapping(TileEntitySkull.class, "Skull");
    }

    // -- BEGIN FORGE PATCHES --
    /**
     * Determines if this TileEntity requires update calls.
     * @return True if you want updateEntity() to be called, false if not
     */
    public boolean canUpdate()
    {
        return true;
    }

    /**
     * Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible for
     * sending the packet.
     *
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
    {
    }

    /**
     * Called when the chunk this TileEntity is on is Unloaded.
     */
    public void onChunkUnload()
    {
    }

    /**
     * Called from Chunk.setBlockIDWithMetadata, determines if this tile entity should be re-created when the ID, or Metadata changes.
     * Use with caution as this will leave straggler TileEntities, or create conflicts with other TileEntities if not used properly.
     *
     * @param oldID The old ID of the block
     * @param newID The new ID of the block (May be the same)
     * @param oldMeta The old metadata of the block
     * @param newMeta The new metadata of the block (May be the same)
     * @param world Current world
     * @param x X Postion
     * @param y Y Position
     * @param z Z Position
     * @return True to remove the old tile entity, false to keep it in tact {and create a new one if the new values specify to}
     */
    public boolean shouldRefresh(int oldID, int newID, int oldMeta, int newMeta, World world, int x, int y, int z)
    {
        return true;
    }

    public boolean shouldRenderInPass(int pass)
    {
        return pass == 0;
    }
    /**
     * Sometimes default render bounding box: infinite in scope. Used to control rendering on {@link TileEntitySpecialRenderer}.
     */
    public static final AxisAlignedBB INFINITE_EXTENT_AABB = AxisAlignedBB.getBoundingBox(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    /**
     * Return an {@link AxisAlignedBB} that controls the visible scope of a {@link TileEntitySpecialRenderer} associated with this {@link TileEntity}
     * Defaults to the collision bounding box {@link Block#getCollisionBoundingBoxFromPool(World, int, int, int)} associated with the block
     * at this location.
     *
     * @return an appropriately size {@link AxisAlignedBB} for the {@link TileEntity}
     */
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        AxisAlignedBB bb = INFINITE_EXTENT_AABB;
        if (getBlockType()!=null && getBlockType() != Block.chest)
        {
            AxisAlignedBB cbb = getBlockType().getCollisionBoundingBoxFromPool(worldObj, xCoord, yCoord, zCoord);
            if (cbb != null)
            {
                bb = cbb;
            }
        }
        return bb;
    }
}
