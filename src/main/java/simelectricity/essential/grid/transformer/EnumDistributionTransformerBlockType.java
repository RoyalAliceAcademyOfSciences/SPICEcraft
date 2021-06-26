package simelectricity.essential.grid.transformer;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import rikka.librikka.ITileMeta;
import simelectricity.essential.grid.TileMultiBlockPlaceHolder;

public enum EnumDistributionTransformerBlockType implements ITileMeta, IStringSerializable {
	Pole10kVNormal,
	Pole10kVSpec,
	Pole10kVAux,
	Pole415VNormal,
	Transformer,
	
	Pole10kV(TileDistributionTransformer.Pole10kV.class),
	PlaceHolder(TileMultiBlockPlaceHolder.class),
	Pole415V(TileDistributionTransformer.Pole415V.class);

	public final Class<? extends TileEntity> teCls;
    public final boolean formed;

    EnumDistributionTransformerBlockType() {
    	this.teCls = null;
    	this.formed = false;
    }
    
    EnumDistributionTransformerBlockType(Class<? extends TileEntity> teCls) {
        this.teCls = teCls;
        this.formed = true;
    }

	@Override
	public String toString() {
		return getString();
	}

	@Override
	public String getString() {
		return name().toLowerCase();
	}
    
	@Override
	public Class<? extends TileEntity> teCls() {
		return teCls;
	}
	
	public static EnumDistributionTransformerBlockType forName(String name) {
		for(EnumDistributionTransformerBlockType type: EnumDistributionTransformerBlockType.values()) {
			if (type.getString().equals(name.toLowerCase()))
				return type;
		}
		return null;
	}
}
