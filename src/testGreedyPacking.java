import java.util.ArrayList;


public class testGreedyPacking {
	static float y[][];
	static ZSlot z[][];
	static int num_key;
	static int num_machine;
	static int[] machine_slots;

	public static void main(String[] args) {
        
		//Read input
		try {
            
        	In in = new In("./input_y.txt");
            
            num_key = in.readInt();
            num_machine = in.readInt();
    
            y = new float[num_key][num_machine];
            for(int i=0; i<num_key; i++){
            	for (int j=0; j<num_machine; j++){
            		y[i][j]=in.readFloat();
            		System.out.print(""+y[i][j]+" ");
            	}
            	System.out.println("key"+i);
            }
            
        }catch (Exception e) { System.out.println(e); }
		
		machine_slots = findSlotNumber(y, num_key, num_machine);
		z = testGreedyPacking(y, num_key, num_machine, machine_slots);
		
		
	}
	
	
	/**
	 * Wrap up and find the slot number for each machine
	 * @param y
	 * @param num_key
	 * @param num_machine
	 * @return r_j
	 */
	public static int[] findSlotNumber(float[][] y, int num_key,
			int num_machine) {
			int[] r = new int[num_machine];
			
            for(int j=0; j<num_machine; j++){
            	float temp=0;
            	for (int i=0; i<num_key; i++){
            		temp += y[i][j];
            	}
            	r[j]=(int)Math.ceil(temp);
            	System.out.println("Machine "+j+" : "+r[j]);
            }
            
		return r;
	}


	
	
	public static ZSlot[][] testGreedyPacking(float y[][],  int num_key,
			int num_machine, int[] machine_slots){
		
		ZSlot[][] z = new ZSlot[num_key][num_machine];
		//z initialization
		for (int i=0; i<num_key; i++){
			for(int j=0; j<num_machine; j++){
				z[i][j] = new ZSlot(machine_slots[j]);
			}
		}

		for (int i=0; i<num_key; i++){
			for(int j=0; j<num_machine; j++){
				int current_slot=0;
				float slot_remaining=1;
				if (y[i][j] <= slot_remaining){
					z[i][j].slot.add(current_slot, new Float(y[i][j]));
					slot_remaining = slot_remaining - y[i][j];
					if (slot_remaining==0){
						current_slot=current_slot+1;
						slot_remaining = 1;
					}
				}else{
					//y[i][j] > slot_remaining
					z[i][j].slot.add(current_slot, new Float(slot_remaining));
					z[i][j].slot.add(current_slot+1, new Float(y[i][j]-slot_remaining));
					slot_remaining = 1 - (y[i][j]-slot_remaining);
					current_slot = current_slot+1;
				}
			}
			
		}
		
		for (int i=0; i<num_key; i++){
			for(int j=0; j<num_machine; j++){
				System.out.print(z[i][j].toString());
			}
			System.out.println();
		}
		
		return z;
	}
	
	
}



class ZSlot {
	public ArrayList<Float> slot;
	
	public ZSlot(int p){
		slot = new ArrayList<Float>(p);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return slot.toString();
	}
	
		
	
}
