package ticketProject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketServer {
	private static TicketManager ticketManager = new TicketManager();

	public static final int PORT = 8080;
	private static final Logger log = LoggerFactory.getLogger(TicketServer.class);

	private static BlockingQueue<ClientRequest> requestQueue = new LinkedBlockingQueue<>();

	private static final LocalDateTime ServerTime = LocalDateTime.of(2025, 8, 8, 15, 45, 0);

	public static void main(String[] args) throws IOException {
		handleStartConsumer();

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			log.info("서버 포트 " + PORT + "에서 대기 중");

			while (true) {
				Socket clientSocket = serverSocket.accept();

				new Thread(() -> handleSocket(clientSocket)).start();
			}
		}
	}

	// 서버 시간 후에 들어온 요청 처리
	private static void handleSocket(Socket clientSocket) {
		try {
			LocalDateTime nowTime = LocalDateTime.now();

			InputStream in = clientSocket.getInputStream();

			byte[] dataFromClient = new byte[100];
			int readCount = in.read(dataFromClient);

			if (readCount == -1) {
				log.warn("클라이언트 연결 끊김 감지");
				return;
			}

			String message = new String(dataFromClient, 0, readCount, "UTF-8");

			String[] parts = message.split("\\|");

			if (parts.length == 4 && parts[0].equals("RESERVATION")) {
				if (nowTime.isAfter(ServerTime)) {
					handlePutReservation(clientSocket, parts);
				} else {
					handleBeforeTime(clientSocket);
				}
			} else {
				handleSendServerTime(clientSocket);
			}

		} catch (Exception e) {
			log.error("요청 처리 오류", e);
		}
	}

	// 들어온 Client 정보를 순서대로 Blocking Queue에 저장
	private static void handlePutReservation(Socket clientSocket, String[] parts) {
		try {
			OutputStream out = clientSocket.getOutputStream();

			int x = Integer.parseInt(parts[1]);
			int y = Integer.parseInt(parts[2]);
			String clientName = parts[3];
			
			// 사용자 중복 처리 로직
			if (ticketManager.validateClientDuplicate(clientName))
				handlePersonDuplicate(clientSocket, clientName);
			else {
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());

				ClientRequest req = new ClientRequest(clientName, x, y, out, clientSocket, timestamp);
				requestQueue.put(req);
			}

		} catch (Exception e) {
			log.error("요청 처리 오류", e);
		}
	}

	// 중복 회원 발생 시 검증 하는 로직
	private static void handlePersonDuplicate(Socket clientSocket, String clientName) {
		try {
			OutputStream out = clientSocket.getOutputStream();
			log.info("중복회원 발생: " + clientName);

			// 응답 전송
			String message = "NAME_DUPLICATED|FAIL";
			message += "\n";

			out.write(message.getBytes("UTF-8"));
			out.flush();
			clientSocket.close();
		} catch (Exception e) {
			log.error("요청 처리 오류", e);
		}
	}

	// 서버 시간 전에 받은 요청을 "BeforeStartTime"로 응답
	private static void handleBeforeTime(Socket clientSocket) {
		try {
			OutputStream out = clientSocket.getOutputStream();
			log.info("서버 시간 전에 시작된 실행된 요청");

			// 응답 전송
			String message = "BEFORE_START_TIME|FAIL";
			message += "\n";

			out.write(message.getBytes("UTF-8"));
			out.flush();
			clientSocket.close();
		} catch (Exception e) {
			log.error("요청 처리 오류", e);
		}
	}

	// 서버 시간을 클라이언트로 보내는 로직
	private static void handleSendServerTime(Socket clientSocket) {
		try {
			OutputStream out = clientSocket.getOutputStream();
//			log.info("서버 시간 전송");

			String serverTime = String.valueOf(ServerTime.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond());
			serverTime += "\n";

			out.write(serverTime.getBytes("UTF-8"));
			out.flush();
			clientSocket.close();
		} catch (Exception e) {
			log.error("요청 처리 오류", e);
		}
	}

	private static void handleStartConsumer() {
		Thread consumerThread = new Thread(() -> {
			while (true) {
				try {
					ClientRequest request = requestQueue.take();

					// 티켓 발급 로직
					boolean success = ticketManager.issueTicket(request.getX(), request.getY(),
							request.getClientName());

					log.info((success ? "티켓 발급 성공!" : "티켓 발급 실패!") + " - ClientName:{} - 좌표({}, {}) - 타임: {}",
							request.getClientName(), request.getX(), request.getY(), request.getTimestamp());

					// 응답 전송
					String response = success ? "|SUCCESS" : "ALREADY_OCCUPIED|FAIL";
					response += "\n";
					request.getOut().write(response.getBytes("UTF-8"));
					request.getOut().flush();
					request.getSocket().close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		consumerThread.setDaemon(true);
		consumerThread.start();
	}
}
