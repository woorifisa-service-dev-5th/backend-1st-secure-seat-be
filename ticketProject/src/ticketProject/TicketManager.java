package ticketProject;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TicketManager {
//	private static Logger log = LoggerFactory.getLogger("TicketManager");
	
	private boolean[][] seats = new boolean[Seat.X_size][Seat.Y_size];
	
	private HashSet<String> personDupliate = new HashSet<>();
	
	private Mutex mutex = new Mutex();
	public boolean issueTicket(final int x, final int y, String clientName) {		
		// 해당 좌표의 유효성 검사
		validateCoordinate(x, y);
		
		// 예매 성공/실패 체크 flag
		boolean isSuccess = false;
		
		// 해당 좌석에 대한 스레드 동시 접근 체크
		mutex.acquired(x, y);
		// 좌석 예매 로직
		if (!seats[x][y]) {
			seats[x][y] = true;
			isSuccess = true;
			personDupliate.add(clientName);
//			log.info("[스레드 ID : " + id +"] 좌석 예매 성공! 선택한 좌석 (" + x + "," + y + ")");
		}
		mutex.relase(x, y);
		
		// 좌석 예매 실패시 로그
//		if (!isSuccess) log.info("[스레드 ID : " + id + "] 좌석 예매에 실패하였습니다.");
		
		
		return isSuccess; // 실패, 성공 여부 반환
	}
	
	private void validateCoordinate(int x, int y) {
		if (!(0 <= x && x < Seat.X_size) || !(0 <= y && y < Seat.Y_size)) {
			throw new IllegalArgumentException("존재하지 않는 좌석입니다. 요청하신 좌석 위치 (" + x  + ","  + y + ")" );
		}
	}
	
	public boolean validateClientDuplicate(String clientName) {
		if(personDupliate.contains(clientName))	return true;
		return false;
	}
}
