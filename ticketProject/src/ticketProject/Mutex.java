package ticketProject;

public class Mutex {
	private boolean[][] locks = new boolean[Seat.X_size][Seat.Y_size];
	
	// 좌석 위치에 대한 정보를 받음
	public synchronized void acquired(int x, int y) {
		while (locks[x][y]) {
			try {
				wait();
			} catch (InterruptedException e) {
		        e.printStackTrace();
		    }
		}
		
		this.locks[x][y]= true; 
	}
	
	public synchronized void relase(int x, int y) {
		notify();
		locks[x][y] = false;
	}
}