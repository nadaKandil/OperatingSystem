import java.util.LinkedList;
import java.util.Queue;

public class Mutex {
	
	int value  = 1;
	 Queue<Integer> queue = new LinkedList<>();
	int ownerid ;
	
	public void semwait(int process_id) {
		
		if(value == 1) {
			value = 0 ;
			ownerid = process_id ;
		}
		else {
			//enque process in queue & block
			queue.add(process_id);
		}
		
		
	}
	
	public void semsignal(int process_id) {
		if(ownerid == process_id)
		{
			if(queue.isEmpty()) {
				value = 1 ;
				ownerid = -1 ;
			}
			else
			{
				//deque proc from s.queue
				//put proc in ready list
				//s.ownerid = pid
				ownerid = queue.remove();
				
			}
		}
		
		
		
	}
	
}
