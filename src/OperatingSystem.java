import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class OperatingSystem {
	String[] Memory = new String[40]; 	// 0 : ID   1:State   2:PC  3:Boundary  4:a  5:b   6:c   7-19: Instructions    ||  20:ID  21:State ........  
	public static int process_count = 0 ;
	int current_process;
	Mutex userInput = new Mutex();
	Mutex userOutput = new Mutex();
	Mutex File = new Mutex();
	 Queue<Integer> Readyqueue = new LinkedList<>();
	 int[] Blockedueue = new int[4];
	 Queue<Integer> Finishedqueue = new LinkedList<>();
	 CycleManager[] cyclemanager = new CycleManager[4];
	int programindisk ;
	String[] varnames = new String[10];
	public OperatingSystem(int timep1, int timep2, int timep3 , int timeslice,boolean print) throws IOException, InterruptedException {
		for (int i = 1 ; i<=3 ; i++) {
		CycleManager cm = new CycleManager();
		 cyclemanager[i] = cm;
		 Blockedueue[i] = -1 ;
	}
				Scheduler(timep1,timep2,timep3,timeslice,print);
				printMemory();
		}
	
	
	public String getProgramfromdisk() throws IOException {
		String text = "";
		String filepath ="Program_In_Disk.txt";
		BufferedReader br = new BufferedReader(new FileReader(new File(filepath)));
		String line = "";
		while((line=br.readLine())!= null) {
			text+=line+"\n";
		}
		br.close();
		return text.substring(0, text.length()-1); // to remove the last \n
	}
	
	public void printreadyqueue() {
		System.out.println("Processes in READY QUEUE");
		for(int ij : Readyqueue) {
			System.out.print(ij+" , ");
		}
		System.out.println();
	}

	public void printblockedqueue() {
		System.out.println("Processes in Blocked QUEUE");
		for(int i = 1 ; i <=3 ; i++) {
			if(Blockedueue[i] != -1)
				System.out.print(i+" , ");
		}
		System.out.println();
	}

	public void printfinishedqueue() {
		System.out.println("Processes in FINISH QUEUE");
		for(int ij : Finishedqueue) {
			System.out.println(ij);
		}
	}
	
	
	public void Scheduler(int timep1,int timep2 ,int timep3,int timeslice,boolean print) throws IOException, InterruptedException {
		boolean prog_finish = false; 
		boolean prog_blocked = false ;
		int clkcycles = 0;
		if(timep1 == 0)
			Interpret("Program_1");
		if(timep2 == 0)
			Interpret("Program_2");
		if(timep3 == 0)
			Interpret("Program_3");
		System.out.println("Welcome :) Operating System will Start Now");
		TimeUnit.SECONDS.sleep(3);

		System.out.println("Ready Queue before Starting");
		printreadyqueue() ;
		
		System.out.println("        Time "+ clkcycles);
		while(Finishedqueue.size()!= 3) {
			prog_finish = false;
			prog_blocked = false ;
			current_process = Readyqueue.remove();
			System.out.println("Current process removed from ready = "+current_process);
			printreadyqueue();
			
			for (int i = 1 ; i <= timeslice ; i++) {
							System.out.println("Time Slice "+i);
			int starting_index = ( Integer.parseInt(Memory[0]) == current_process ? 0 : 20 );
			
			//check if this is the correct program, if not switch from disk
			if(Integer.parseInt(Memory[starting_index])!= current_process) {
				System.out.println("Switching Program "+programindisk+" in Disk with Program "+Memory[starting_index]+" in Memory");
				int starting_index_of_switch_program = starting_index; //get index of the wrong program
				String datafromdisk = getProgramfromdisk(); //save data from disk
				programindisk = Integer.parseInt(Memory[starting_index]);
				MemorytoDisk(starting_index_of_switch_program); //write memory to disk
				String[] line2 = datafromdisk.split("\n");

				for(int joe = 0 ; joe < 20 ;joe++) {
					Memory[starting_index+joe] = line2[joe];    //disk succefully written to memory
				}
				Memory[starting_index+1] = "Ready";
				
			
			}
			
			
			
			int PC = Integer.parseInt(Memory[starting_index+2]);
			int instructionnumber = starting_index+7+PC;
			

			
			System.out.println("Program "+current_process+" Executing Instruction : "+Memory[instructionnumber]);
			String[] line = Memory[instructionnumber].split(" ");
			String actual_instruction ;
			if(line.length>2 && line[2].equals("input") && !cyclemanager[current_process].flag) {
				cyclemanager[current_process].flag = true ;
				actual_instruction = line[2];
				--PC;
			}
			else if (line.length>2 &&line[2].equals("readFile")&& !cyclemanager[current_process].flag) {
				cyclemanager[current_process].flag = true ;
				actual_instruction = line[2]+" "+line[3];
				--PC;
			}
			else {
				actual_instruction = Memory[instructionnumber];
				cyclemanager[current_process].flag = false ;
			}
			
			Execute(actual_instruction, current_process);
			
			

			
			
			Memory[starting_index+2] = String.valueOf(++PC);
			if((PC) == Integer.parseInt(Memory[starting_index+3])) { //check if i finshed all instructions
				
				System.out.println("Program "+Memory[starting_index]+" FINISHED");
				
				Memory[starting_index+1] = "Finished";
				Finishedqueue.add(current_process);
				 prog_finish = true ;
				printfinishedqueue();
				 //if process finishes then switch with disk
				 if(Finishedqueue.size() ==1) {
					System.out.println("Switching Program "+programindisk+" in Disk with Program "+Memory[starting_index]+" in Memory That has been finished");
					int starting_index_of_switch_program = starting_index; //get index of the wrong program
					String datafromdisk = getProgramfromdisk(); //save data from disk
					programindisk = Integer.parseInt(Memory[starting_index]);
					MemorytoDisk(starting_index_of_switch_program); //write memory to disk
					String[] line2 = datafromdisk.split("\n");

					for(int joe = 0 ; joe < 20 ;joe++) {
						Memory[starting_index+joe] = line2[joe];    //disk succefully written to memory
					}
					
					if(Readyqueue.contains(   Integer.parseInt(Memory[starting_index])  )   )
						Memory[starting_index+1] = "Ready";
				 }
				 
				 
				 
													}
			else if(Memory[starting_index+1].equals("Blocked")) // if im blocked exit the for loop
					{
				printblockedqueue();
				prog_blocked = true ;
					}

			if(Finishedqueue.size()== 3)
				return ;
			
			if(print)
				printMemory();
			
			++clkcycles;
			System.out.println("        Time "+ clkcycles);
			
			if(clkcycles == timep2)
				Interpret("Program_2");
			if(clkcycles == timep3)
				Interpret("Program_3");

			if(prog_finish || prog_blocked)
				break ;
			
			
			 if(i == timeslice)
				Readyqueue.add(current_process);

			}//for loop
		}//while
	}//function
	
	public void MemorytoDisk(int starting_index) throws IOException {
		String text = "";
		int i = starting_index ;
		for(int j = 0 ; j<20 ; j++) {
			text+=Memory[starting_index+j]+"\n";
		}
		text.substring(0, text.length()-1) ;
		FileWriter writef = new FileWriter("Program_In_Disk.txt");
		writef.write(text);
		writef.close();
		System.out.println("Program "+programindisk+" Succefully Moved to Disk");
	}
	
	
public void Interpret(String filename) throws IOException {
		process_count++;
		int starting_index_of_switch_program =-1;
		if(process_count== 3) {
		
		if( Blockedueue[1]==1) // this means program 1 is blocked
		{
		 starting_index_of_switch_program = 0 ;
		 programindisk = 1 ;
		 MemorytoDisk(starting_index_of_switch_program);
		}
		else {
			starting_index_of_switch_program = 20 ;
			programindisk = 2 ;
			 MemorytoDisk(starting_index_of_switch_program);
		}
		}
		
		
		int starting_index ; //index of the 1st word of the process
		if(process_count == 1)
			starting_index = 0 ;
		else if(process_count == 2)
			starting_index = 20 ;
		else
			starting_index = starting_index_of_switch_program;
	
		Readyqueue.add(process_count);
		BufferedReader br = new BufferedReader(new FileReader(new File(filename+".txt")));
		Memory[starting_index+0] =  String.valueOf(process_count); //set ID
		Memory[starting_index+1] = "Ready"; //set State
		Memory[starting_index+2] = String.valueOf(0); //set PC
		Memory[starting_index+3]  = String.valueOf((int) br.lines().count()); //set Boundary (Index of last instruction)
		br.close();
		br = new BufferedReader(new FileReader(new File(filename+".txt")));
		String line = "";
		int i = starting_index+7 ; // index of 1st instruction
		while((line = br.readLine()) != null) {
			Memory[i++]= line; //add instruction to Memory
		}
		br.close();
	}
	
public int getstartingindex(int processid) {
		if (Integer.parseInt(Memory[0]) == processid)
			return 0;
		else 
			return 20 ;
	}
	
public String getvariablevalue(String varname,int starting_index) {
	//	return ( varname.equals("a")   )?  Memory[starting_index+4] : (    ( varname.equals("b")) ? Memory[starting_index+5] :  Memory[starting_index+6]    ) ;

	int varnum = -1 ;
	
	if(current_process == 1) {
		
		if(varnames[1].equals(varname))
			varnum = 4;
		else if(varnames[2].equals(varname))
			varnum = 5 ;
		else 
			varnum = 6 ;
		
		
	}
	else if(current_process == 2) {
		if(varnames[4].equals(varname))
			varnum = 4;
		else if(varnames[5].equals(varname))
			varnum = 5 ;
		else 
			varnum = 6 ;
	}
	else {
		if(varnames[7].equals(varname))
			varnum = 4;
		else if(varnames[8].equals(varname))
			varnum = 5 ;
		else 
			varnum = 6 ;
	}

	return Memory[starting_index+varnum];


}

public void setvariable(String varname , String value , int starting_index) {
	
	int varnumber = -1;
	
	if(current_process == 1) {
		
		if( varnames[1] == null || varnames[1].equals(varname))
		{
			varnames[1] = varname ;
			varnumber = 4;
		}
		else if( varnames[2] == null || varnames[2].equals(varname) ) {
			varnames[2] = varname ;
			varnumber = 5;
		}
		else {
			varnames[3] = varname ;
			varnumber = 6;
		}
		
		
	}
	else if(current_process == 2 ) {
		
		if( varnames[4] == null || varnames[4].equals(varname))
		{
			varnames[4] = varname ;
			varnumber = 4;
		}
		else if( varnames[5] == null || varnames[5].equals(varname) ) {
			varnames[5] = varname ;
			varnumber = 5;
		}
		else {
			varnames[6] = varname ;
			varnumber = 6;
		}
	}
	else {
		if( varnames[7] == null || varnames[7].equals(varname))
		{
			varnames[7] = varname ;
			varnumber = 4;
		}
		else if( varnames[8] == null || varnames[8].equals(varname) ) {
			varnames[8] = varname ;
			varnumber = 5;
		}
		else {
			varnames[9] = varname ;
			varnumber = 6;
		}
		
	}
	
	
	
	Memory[starting_index+varnumber] = value ;
	
	
//	if(varname.equals("a"))
//		Memory[starting_index+4] = value ;
//	else
//		if(varname.equals("b"))
//			Memory[starting_index+5] = value ;
//		else
//			Memory[starting_index+6] = value ;
	
	
	
}

public String Execute(String instruction,int processid) throws IOException {
		int starting_index = getstartingindex(processid) ;	
		String line[] = instruction.split(" ");
		String word1 = line[0];
		switch(word1) {
		
		case"input":{
			cyclemanager[processid].tempvalue = input();return"";
		}
		
		case "print":{    
			String value = getvariablevalue(line[1], starting_index) ;
			System.out.println(value);   
			return "" ;}
		
		case "assign":{ 
			String value ="";
			if(line.length > 3) { // assign a readfile b
				 value = cyclemanager[processid].tempvalue;
			}
			else
				if(line[2].equals("input")) { //assign a input
					value = cyclemanager[processid].tempvalue;				
				}
				else { //assign a b
					value = getvariablevalue(line[2], starting_index);
				}
			
			setvariable(line[1], value, starting_index);
			return "";
			
		}
		
		case "writeFile":{ 
			String value = "";
			if(line.length > 3) { // writefile a readfile b
				 value = Execute(line[2]+" "+line[3], processid);
			}
			else
				if(line[2].equals("input")) { //write a input
					value = input();
				}
				else { //write a b
					value = getvariablevalue(line[2], starting_index);
				}
			FileWriter write = new FileWriter(getvariablevalue(line[1], starting_index)+".txt");
			write.write(value);
			write.close();
			return "";}
	
		case "readFile":{
			
			  cyclemanager[processid].tempvalue  = (readfile(line[1], starting_index));
			
			  return "";
		}
			
		case "printFromTo":{ 
				int start = Integer.parseInt(getvariablevalue(line[1], starting_index)) ;
				int end = Integer.parseInt(getvariablevalue(line[2], starting_index)) ;
				for(int i = start ; i<= end; i++ ) {
					System.out.println(i);
				}
			
			return "";}
		
		case "semWait":{
			switch (line[1]) {
			case "userInput": {
				userInput.semwait(processid);
				
				if(userInput.queue.contains(processid)) // resource currently busy , so block
				{
					Blockedueue[processid] = processid ;
					System.out.println("Program "+processid+" Blocked");
					Memory[starting_index+1] = "Blocked";
				}
				break ;	}
			case "userOutput": {
				userOutput.semwait(processid);
				if(userOutput.queue.contains(processid)) // resource currently busy , so block
				{
					Blockedueue[processid] = processid ;
					System.out.println("Program "+processid+" Blocked");
					Memory[starting_index+1] = "Blocked";
				}
				break;	}
			case "file": {
				File.semwait( processid);	
				if(File.queue.contains(processid)) // resource currently busy , so block
				{
					Blockedueue[processid] = processid ;
					System.out.println("Program "+processid+" Blocked");
					Memory[starting_index+1] = "Blocked";
				}
				break;	}
			
			default : System.out.println("Invalid mutex");		}
			return "";   }
		
		case "semSignal":{ 
			switch (line[1]) {
			case "userInput": {
				userInput.semsignal( processid);	
				if(userInput.ownerid > 0) //owner id Positive therefore a process was blocked and needs to be added to ready quer 
				{
					
					Readyqueue.add(Blockedueue[userInput.ownerid]);
					Blockedueue[userInput.ownerid] = -1 ;
					int blockedstartingindex = starting_index == 0 ? 20 : 0 ;
					if(Integer.parseInt(Memory[blockedstartingindex]) == userInput.ownerid )
						Memory[blockedstartingindex+1] = "Ready" ;
					
					
				}
				
				
				break ;	}
			case "userOutput": {
				userOutput.semsignal( processid);
				if(userOutput.ownerid > 0) //owner id Positive therefore a process was blocked and needs to be added to ready query
				{
					Readyqueue.add(Blockedueue[userOutput.ownerid]);
					Blockedueue[userOutput.ownerid] = -1 ;
					int blockedstartingindex = starting_index == 0 ? 20 : 0 ;
					if(Integer.parseInt(Memory[blockedstartingindex]) == userOutput.ownerid )
						Memory[blockedstartingindex+1] = "Ready" ;

					
				}
				
				
				break ;	}
			case "file": {
				File.semsignal( processid);
				
				if(File.ownerid > 0) //owner id Positive therefore a process was blocked and needs to be added to ready quer 
				{
					Readyqueue.add(Blockedueue[File.ownerid]);
					Blockedueue[File.ownerid] = -1 ;
					int blockedstartingindex = starting_index == 0 ? 20 : 0 ;
					if(Integer.parseInt(Memory[blockedstartingindex]) == File.ownerid )
						Memory[blockedstartingindex+1] = "Ready" ;				}

				break ;	}
			
			default : System.out.println("Invalid mutex");		}
			return "";  }
		
		
		
		
		
		default : System.out.println("Invalid Instruction"); return "";
		}
	}
	
	public String input() {
		Scanner input =  new Scanner(System.in);
		System.out.println("Please enter a value");
		return (input.nextLine()) ;
	}
	
public String readfile(String varname , int starting_index) throws IOException {
		String text = "";
		String filepath =getvariablevalue(varname, starting_index)+".txt";
		BufferedReader br = new BufferedReader(new FileReader(new File(filepath)));
		String line = "";
		while((line=br.readLine())!= null) {
			text+=line+"\n";
		}
		br.close();
		return text.substring(0, text.length()-1); // to remove the last \n
	}
	
public void printMemory() {
	int instrnum = 1 ;
	System.out.println("--------------------------Memory------------------------");

	for(int i = 0 ; i<40 ; i++) {
		if(i == 20)
			instrnum = 1;
		String var ;
		if(i == 0 || i== 20)
			var = "ID : ";
		else
			if(i==1 || i == 21)
				var = "State : ";
			else
				if(i==2 || i == 22)
					var = "PC : ";
				else
					if(i==3 || i == 23)
						var = "Boundary : ";
					else
						if(i==4 || i == 24)
							var = "Variable Number 1 : ";
						else
							if(i==5 || i == 25)
								var = "Variable Number 2 : ";
							else
								if(i==6 || i == 26)
									var = "Variable Number 3 : ";
								else
										var = "Instruction "+(instrnum++)+" : " ;
		if(i == 3) // boundary print
			System.out.println(var+"From "+"0 "+ "to "+(Integer.parseInt(Memory[i])+7));
		else if(i==23 && Memory[i] != null) // boundary print
			System.out.println(var+"From "+"20 "+ "to "+(Integer.parseInt(Memory[i])+27));
		else if(i == 23)
			System.out.println(var+"null");
		else
		System.out.println(var+Memory[i]);
		
		
	}
}

	public static void main(String[] args) throws IOException, InterruptedException {
		
//		os.Memory[4] = "5";
//		os.Memory[5] = "8";
//		os.Memory[6] = "10";
		//os.Interpret("Program_2");
		//os.Execute("printFromTo a b", 1);
		//	os.Interpret("Program_1");
		//os.Execute("assign a input", 1);
		//os.Execute("assign b input", 1);
		//os.Execute("writeFile b readFile a", 1);
		boolean print_memory_each_time = false ;
		 new OperatingSystem(0,1,4,2,print_memory_each_time);
		//os.Scheduler(1,3,2);
	//	os.printMemory();
	//	System.out.println(os.getProgramfromdisk());
		
//		System.out.println("--------------------------Memory------------------------");
//		for(String s : os.Memory) {
//			System.out.println(s);
//		}
//		
		
		
	}

	
	
}
