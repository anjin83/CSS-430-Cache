import java.util.Random;

public class Test4 extends Thread{

	private boolean cacheEnabled = false;
	private int test = 0;

	public Test4(String[] args){
		cacheEnabled = args[0].equals("enabled");
		test = Integer.parseInt(args[1]);
	}

	public Test4(){}

	public void run(){
		SysLib.flush();
		runTest(cacheEnabled, test);
		SysLib.exit();
	}
	
	private void runTest(boolean CE, int test){
		if(CE) System.out.println("Cache: Enabled Test#: " + test);
		else System.out.println("Cache: Disabled Test#: " + test);
		if(CE){
			if(test == 1)RAC();
			else if(test == 2) LAW();
			else if(test == 3) MAWC();
			else if(test == 4) AAWC();
		}
		else{
			if(test == 1)RANCH();
			else if(test == 2) LANW();
			else if(test == 3) MAWNC();
			else if(test == 4) AANC();
		}
	}

	//random access no cache
	private void RANCH(){
		float avgRead = 0;
		float avgWrite = 0;

		Random randy = new Random();
		for(int numWrites = 0; numWrites < 500; numWrites++){
			avgWrite += RANCHWriteHelper(randy);
		}
		
		for(int numReads = 0; numReads < 500; numReads++){
			avgRead += RANCHReadHelper(randy);
		}

		System.out.println("Average Write Time: " + ((avgWrite/500)/1000000000) + " Average Read Time: " + ((avgRead/500)/1000000000));

	}

	private float RANCHWriteHelper(Random randy){
		long st = System.nanoTime();
		int randomBlock = randy.nextInt(1000);
		byte[] temp = new byte[512];
		randy.nextBytes(temp);
		SysLib.rawwrite(randomBlock, temp);
		return (System.nanoTime() - st);
	}

	private float RANCHReadHelper(Random randy){
		long st = System.nanoTime();
		int randomBlock = randy.nextInt(1000);
		byte[] temp = new byte[512];
		randy.nextBytes(temp);
		SysLib.rawread(randomBlock, temp);
		return (System.nanoTime() - st);
	}

	//random access w/ cache
	private void RAC(){
		float avgRead = 0;
		float avgWrite = 0;

		Random randy = new Random();
		for(int numWrites = 0; numWrites < 500; numWrites++){
			avgWrite += RACWriteHelper(randy);
		}
		
		for(int numReads = 0; numReads < 500; numReads++){
			avgRead += RACReadHelper(randy);
		}

		System.out.println("Average Write Time: " + ((avgWrite/500)/1000000000) + " Average Read Time: " + ((avgRead/500)/1000000000));
	}

	private float RACWriteHelper(Random randy){
		long st = System.nanoTime();
		int randomBlock = randy.nextInt(1000);
		byte[] temp = new byte[512];
		randy.nextBytes(temp);
		SysLib.cwrite(randomBlock, temp);
		return (System.nanoTime() - st);
	}

	private float RACReadHelper(Random randy){
		long st = System.nanoTime();
		int randomBlock = randy.nextInt(1000);
		byte[] temp = new byte[512];
		randy.nextBytes(temp);
		SysLib.cread(randomBlock, temp);
		return (System.nanoTime() - st);
	}

	//local access w/ no cache
	private void LANW(){
		float avgRead = 0;
		float avgWrite = 0;

		Random randy = new Random();
		for(int numWrites = 0; numWrites < 500; numWrites++){
			avgWrite += LANWWriteHelper(randy, numWrites % 10);
		}
		
		for(int numReads = 0; numReads < 500; numReads++){
			avgRead += LANWReadHelper(randy, numReads % 10);
		}

		System.out.println("Average Write Time: " + ((avgWrite/500)/1000000000) + " Average Read Time: " + ((avgRead/500)/1000000000));
	}

	private float LANWWriteHelper(Random randy, int block){
		long st = System.nanoTime();
		byte[] temp = new byte[512];
		randy.nextBytes(temp);
		SysLib.rawwrite(block, temp);
		return (System.nanoTime() - st);
	}

	private float LANWReadHelper(Random randy, int block){
		long st = System.nanoTime();
		byte[] temp = new byte[512];
		randy.nextBytes(temp);
		SysLib.rawread(block, temp);
		return (System.nanoTime() - st);
	}

	//local access w/ cache
	private void LAW(){
		float avgRead = 0;
		float avgWrite = 0;

		Random randy = new Random();
		for(int numWrites = 0; numWrites < 500; numWrites++){
			avgWrite += LAWWriteHelper(randy, numWrites % 10);
		}
		
		for(int numReads = 0; numReads < 500; numReads++){
			avgRead += LAWReadHelper(randy, numReads % 10);
		}

		System.out.println("Average Write Time: " + ((avgWrite/500)/1000000000) + " Average Read Time: " + ((avgRead/500)/1000000000));
	}

	private float LAWWriteHelper(Random randy, int block){
		long st = System.nanoTime();
		byte[] temp = new byte[512];
		randy.nextBytes(temp);
		SysLib.cwrite(block, temp);
		return (System.nanoTime() - st);
	}

	private float LAWReadHelper(Random randy, int block){
		long st = System.nanoTime();
		byte[] temp = new byte[512];
		randy.nextBytes(temp);
		SysLib.cread(block, temp);
		return (System.nanoTime() - st);
	}

	//mixed access w/ no cache
	private void MAWNC(){
		float avgRead = 0;
		float avgWrite = 0;

		Random randy = new Random();
		
		for(int numWrites = 0; numWrites < 500; numWrites++){
			int r = randy.nextInt(100) + 1;
			if(r <= 90) avgWrite += LANWWriteHelper(randy, 0);
			else avgWrite += RANCHWriteHelper(randy);
		}
		
		for(int numReads = 0; numReads < 500; numReads++){
			int r = randy.nextInt(100) + 1;
			if(r <= 90) avgRead += LANWReadHelper(randy, 0);
			else avgRead += RANCHReadHelper(randy);
		}

		System.out.println("Average Write Time: " + ((avgWrite/500)/1000000000) + " Average Read Time: " + ((avgRead/500)/1000000000));
	}

	//mixed access w/ cache
	private void MAWC(){
		float avgRead = 0;
		float avgWrite = 0;

		Random randy = new Random();
		
		for(int numWrites = 0; numWrites < 500; numWrites++){
			int r = randy.nextInt(100) + 1;
			if(r <= 90) avgWrite += LAWWriteHelper(randy, 0);
			else avgWrite += RACWriteHelper(randy);
		}
		
		for(int numReads = 0; numReads < 500; numReads++){
			int r = randy.nextInt(100) + 1;
			if(r <= 90) avgRead += LAWReadHelper(randy, 0);
			else avgRead += RACReadHelper(randy);
		}

		System.out.println("Average Write Time: " + ((avgWrite/500)/1000000000) + " Average Read Time: " + ((avgRead/500)/1000000000));
	}

	//adversary access w/ no cache
	private void AANC(){
		float avgRead = 0;
		float avgWrite = 0;

		Random randy = new Random();
		
		for(int numWrites = 0; numWrites < 500; numWrites++){
			avgWrite += LANWWriteHelper(randy, numWrites);
		}
		
		for(int numReads = 0; numReads < 500; numReads++){
			avgRead += LANWReadHelper(randy, numReads+500);
		}

		System.out.println("Average Write Time: " + ((avgWrite/500)/1000000000) + " Average Read Time: " + ((avgRead/500)/1000000000));
	}

	//adversary access w/ cache
	private void AAWC(){
		float avgRead = 0;
		float avgWrite = 0;

		Random randy = new Random();
		
		for(int numWrites = 0; numWrites < 500; numWrites++){
			avgWrite += LAWWriteHelper(randy, numWrites);
		}
		
		for(int numReads = 0; numReads < 500; numReads++){
			avgRead += LAWReadHelper(randy, numReads+500);
		}

		System.out.println("Average Write Time: " + ((avgWrite/500)/1000000000) + " Average Read Time: " + ((avgRead/500)/1000000000));
	}

}
