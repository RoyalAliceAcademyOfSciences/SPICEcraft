package simelectricity.essential.client.grid.accessory;

import net.minecraft.util.math.MathHelper;
import rikka.librikka.math.MathAssitant;
import rikka.librikka.math.Vec3f;
import rikka.librikka.model.loader.EasyTextureLoader;
import rikka.librikka.model.quadbuilder.RawQuadGroup;
import simelectricity.essential.client.ResourcePaths;
import simelectricity.essential.client.grid.PowerPoleRenderHelper;
import simelectricity.essential.client.grid.pole.Models;

public class AR10kVPoleBranch implements ISEAccessoryRenderer {
	public final static ISEAccessoryRenderer instance = new AR10kVPoleBranch();
	private AR10kVPoleBranch() {}
	
	@Override
	public void renderConnection(PowerPoleRenderHelper pole, PowerPoleRenderHelper accessory) {
		if (pole.connectionList.isEmpty()) {
			if (accessory.connectionList.isEmpty())
				return;
			
			PowerPoleRenderHelper.ConnectionInfo[] accessoryConnection = accessory.connectionList.getFirst();
			
			pole.addExtraWire(accessoryConnection[0].fixedFrom, accessoryConnection[0].fixedFrom.add(0, 1, 0), 0);
			pole.addExtraWire(accessoryConnection[1].fixedFrom, accessoryConnection[1].fixedFrom.add(0, 1, 0), 0);
			pole.addExtraWire(accessoryConnection[2].fixedFrom, accessoryConnection[2].fixedFrom.add(0, 1, 0), 0);
		} else {
			PowerPoleRenderHelper.ConnectionInfo[] poleConnection = pole.connectionList.getFirst();
			
			if (accessory.connectionList.isEmpty()) {
				pole.addExtraWire(poleConnection[0].fixedFrom, poleConnection[0].fixedFrom.add(0, -1, 0), 0);
				pole.addExtraWire(poleConnection[1].fixedFrom, poleConnection[1].fixedFrom.add(0, -1.5F, 0), 0);
				pole.addExtraWire(poleConnection[2].fixedFrom, poleConnection[2].fixedFrom.add(0, -1, 0), 0);
				return;
			}
			
			PowerPoleRenderHelper.ConnectionInfo[] accessoryConnection = accessory.connectionList.getFirst();
			float angleTo = accessoryConnection[1].calcAngleFromXInDegree();
			
			boolean has2PoleCon = pole.connectionList.size() > 1;
			float angleFrom = poleConnection[1].calcAngleFromXInDegree();			
			float angleDiff = angleTo-angleFrom;
			
			if (has2PoleCon) {
				PowerPoleRenderHelper.ConnectionInfo[] secondPoleConnection = pole.connectionList.getLast();
				float angleFrom2 = secondPoleConnection[1].calcAngleFromXInDegree();		
				float angleDiff2 = angleTo-angleFrom2;
				
				float cute1 = MathHelper.abs(angleDiff);
				float cute2 = MathHelper.abs(angleDiff2);
				
				cute1 = cute1>180 ? 360-cute1 : cute1;
				cute2 = cute2>180? 360-cute2: cute2;
				
				if (cute1 > cute2) {
					poleConnection = secondPoleConnection;
					angleFrom = angleFrom2;
					angleDiff = angleDiff2;
				}
			}
			
			final float angle = angleDiff < 0 ? angleDiff + 360 : angleDiff;
			final float middle = angle<180 ? 180+(angleFrom+angleTo)/2 : (angleFrom+angleTo)/2;
		
			this.sort(poleConnection, accessoryConnection, (from, to) ->{
				if ((0<=angle && angle<=67.5F) || (292.5F<=angle && angle<=360F)) {		
					pole.addExtraWire(from[0].fixedFrom, to[0].fixedFrom, 0.1F, true);
					pole.addExtraWire(from[1].pointOnCable(0.1F), to[1].fixedFrom, 0);
					pole.addExtraWire(from[2].fixedFrom, to[2].fixedFrom, 0.6F, true);
				} else if (angle >= 112.5F && angle <= 247.5F) {
					pole.addExtraWire(from[0].pointOnCable(0.4F), to[0].fixedFrom, 0.5F, true);
					pole.addExtraWire(from[2].fixedFrom, to[2].fixedFrom, 0.5F, true);
					
					Vec3f pt = (new Vec3f(0.5F,1.5F,0.5F)).add(pole.pos);
					
					if (!has2PoleCon) {
						Models.render10kVInsulator(EasyTextureLoader.getTexture(ResourcePaths.metal), EasyTextureLoader.getTexture(ResourcePaths.glass_insulator))
						.translateCoord(0.5F, 1, 0.5F).bake(pole.quadBuffer);
						pole.addExtraWire(from[1].fixedFrom, pt, -0.3F);
					}
					
					pole.addExtraWire(pt, to[1].fixedFrom, -0.4F);
				} else {
					RawQuadGroup insulator = Models.render10kVInsulator(EasyTextureLoader.getTexture(ResourcePaths.metal), EasyTextureLoader.getTexture(ResourcePaths.glass_insulator));
					insulator.rotateAroundX(90).translateCoord(0, -1.2F, 0.125F).rotateAroundY(90 + middle).translateCoord(0.5F, 0, 0.5F).bake(pole.quadBuffer);
					
					Vec3f pt = new Vec3f(0.65F, -1.2F, 0).rotateAroundY(middle).add(0.5F, 0, 0.5F).add(pole.pos);
					
					pole.addExtraWire(from[0].fixedFrom, to[0].fixedFrom, 0.1F, true);
					pole.addExtraWire(from[2].fixedFrom, pt, 0.4F, true);
					pole.addExtraWire(pt, to[2].fixedFrom, 0.4F, true);
					
					
					pt = (new Vec3f(0.5F,1.5F,0.5F)).add(pole.pos);
					if (!has2PoleCon) {
						Models.render10kVInsulator(EasyTextureLoader.getTexture(ResourcePaths.metal), EasyTextureLoader.getTexture(ResourcePaths.glass_insulator))
							.translateCoord(0.5F, 1, 0.5F).bake(pole.quadBuffer);
						pole.addExtraWire(from[1].fixedFrom, pt, -0.3F);
					}
					
					pole.addExtraWire(pt, to[1].pointOnCable(0.4F), -0.4F);
				}
			});
    	}
	}
	
	public static void sort(PowerPoleRenderHelper.ConnectionInfo[] from, PowerPoleRenderHelper.ConnectionInfo[] to, Action p) {
		float f0t0 = from[0].fixedFrom.distanceXZ(to[0].fixedFrom);
		float f2t2 = from[from.length-1].fixedFrom.distanceXZ(to[to.length-1].fixedFrom);
		float f0t2 = from[0].fixedFrom.distanceXZ(to[to.length-1].fixedFrom);
		float f2t0 = from[from.length-1].fixedFrom.distanceXZ(to[0].fixedFrom);
		
		if (MathAssitant.isMin(f0t0, f0t2, f2t0, f2t2)) {
			p.action(from, to);
		} else if (MathAssitant.isMin(f0t2, f0t0, f2t0, f2t2)) {
			p.action(from, reverse(to));
		} else if (MathAssitant.isMin(f2t0, f0t2, f0t0, f2t2)) {
			p.action(reverse(from), to);
		} else if (MathAssitant.isMin(f2t2, f0t2, f2t0, f0t0)) {
			p.action(reverse(from), reverse(to));
		}
	}
	
	public static PowerPoleRenderHelper.ConnectionInfo[] reverse(PowerPoleRenderHelper.ConnectionInfo[] in){
		PowerPoleRenderHelper.ConnectionInfo[] ret = new PowerPoleRenderHelper.ConnectionInfo[in.length];
		for (int i=0; i<in.length; i++) {
			ret[i] = in[in.length-1-i];
		}
		return ret;
	}
	
	public static interface Action {
		public void action(PowerPoleRenderHelper.ConnectionInfo[] from, PowerPoleRenderHelper.ConnectionInfo[] to);
	}
}