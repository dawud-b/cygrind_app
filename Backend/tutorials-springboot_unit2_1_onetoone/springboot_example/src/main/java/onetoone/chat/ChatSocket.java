package onetoone.chat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import onetoone.points.PremiumReaction;
import onetoone.points.PremiumReactionRepository;
import onetoone.points.UserPremiumReaction;
import onetoone.points.UserPremiumReactionRepository;
import onetoone.users.User;
import onetoone.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * WebSocket for chat feature. Uses sessionID to save different chats. ChatSession must be created before entering
 * a websocket chat room.
 *
 * @author Dawud Benedict
 */
@Controller
@ServerEndpoint(value = "/chat/{chatSession_id}/{username}")
public class ChatSocket {

	private static MessageRepository msgRepo;
	private static ChatSessionRepository chatSessionRepo;
	private static UserRepository userRepository;
	private static ReactionRepository reactionRepo;
	private static PremiumReactionRepository premiumReactionRepo;
	private static UserPremiumReactionRepository userPremiumReactionRepo;

	@Autowired
	public void setRepos(MessageRepository messageRepo,
						 ChatSessionRepository chatRepo,
						 UserRepository userRepo,
						 ReactionRepository reactRepo,
						 PremiumReactionRepository premiumReactRepo,
						 UserPremiumReactionRepository userPremiumReactRepo) {
		msgRepo = messageRepo;
		chatSessionRepo = chatRepo;
		userRepository = userRepo;
		reactionRepo = reactRepo;
		premiumReactionRepo = premiumReactRepo;
		userPremiumReactionRepo = userPremiumReactRepo;
	}

	private static Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();
	private static Map<Session, String> userMap = new ConcurrentHashMap<>();

	private final Logger logger = LoggerFactory.getLogger(ChatSocket.class);

	// used to send string in Json object format
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@OnOpen
	public void onOpen(Session session, @PathParam("chatSession_id") String sessionId, @PathParam("username") String username)
			throws IOException {

		logger.info("User: " + username + " entered into Open");

		User currentUser = userRepository.findByUsername(username);
		if (currentUser == null) {
			logger.error("User: " + username + " not found");
			return;
		}
		long userId = currentUser.getId();

		// Store the user session
		sessions.computeIfAbsent(sessionId, k -> new CopyOnWriteArraySet<>()).add(session);
		session.getUserProperties().put("userId", userId);
		session.getUserProperties().put("sessionId", sessionId);
		userMap.put(session, username);

		// Send chat history to this user
		List<Message> pastMessages = getChatHistory(sessionId);
		for (Message message : pastMessages) {
			String messageString = objectMapper.writeValueAsString(message);
			sendToUserInSession(sessionId, username, messageString);
		}

		// Broadcast to session that user is active
		String activeMsg = "[Server] " + username + " is active";
		broadcastToSession(sessionId, activeMsg);
	}

	@OnMessage
	public void onMessage(Session session, String content, @PathParam("chatSession_id") String sessionId,
						  @PathParam("username") String username) throws IOException {

		User sender = userRepository.findByUsername(username);
		if (sender == null) {
			logger.error("User: " + username + " not found");
			return;
		}

		ChatSession chatSession = chatSessionRepo.findBySessionId(sessionId);
		if (chatSession == null) {
			logger.error("ChatSession: " + sessionId + " not found");
			return;
		}

		// Typing indication. Send to other users.
		if (content.equals("[User is typing]")) {
			String typingMsg = "[Server] " + username + " is typing";
			broadcastToSession(sessionId, typingMsg);
			return;
		}

		// Typing stopped. Send to other users.
		if (content.equals("[User stopped typing]")) {
			String typingMsg = "[Server] " + username + " stopped typing";
			broadcastToSession(sessionId, typingMsg);
			return;
		}

		// Standard Reactions. Format: "[Reaction]:messageId:reactionType"
		if (content.startsWith("[Reaction]:")) {
			String[] parts = content.substring("[Reaction]:".length()).split(":");

			if (parts.length == 2) {
				long messageId = Long.parseLong(parts[0]);
				String reactionString = parts[1];
				int reactionType;

				// Check if it's a standard reaction first
				switch (reactionString) {
					case "LIKED":
						reactionType = Reaction.LIKED;
						break;
					case "DISLIKED":
						reactionType = Reaction.DISLIKED;
						break;
					case "LOVED":
						reactionType = Reaction.LOVED;
						break;
					case "LAUGHING":
						reactionType = Reaction.LAUGHING;
						break;
					case "STRONG":
						reactionType = Reaction.STRONG;
						break;
					case "COOL":
						reactionType = Reaction.COOL;
						break;
					case "CRY":
						reactionType = Reaction.CRY;
						break;
					// Check for premium reactions by name
					case "FIRE":
						reactionType = 7;
						break;
					case "MIND_BLOWN":
						reactionType = 8;
						break;
					case "FLEXING":
						reactionType = 9;
						break;
					case "TROPHY":
						reactionType = 10;
						break;
					case "CROWN":
						reactionType = 11;
						break;
					case "DIAMOND":
						reactionType = 12;
						break;
					case "ROCKET":
						reactionType = 13;
						break;
					case "UNICORN":
						reactionType = 14;
						break;
					default:
						reactionType = -1;
						break;
				}

				// Invalid reaction. Do nothing
				if (reactionType == -1) {
					return;
				}

				// Get the message
				Message message = msgRepo.findById(messageId);
				if (message == null) {
					return;
				}

				// Check if it's a premium reaction (codes 7 and above)
				if (reactionType >= 7) {
					// Find the premium reaction by code
					PremiumReaction premiumReaction = premiumReactionRepo.findByEmojiCode(reactionType);
					if (premiumReaction == null) {
						logger.error("Premium reaction with code " + reactionType + " not found");
						return;
					}

					// Check if user has purchased this premium reaction
					UserPremiumReaction userPremiumReaction = userPremiumReactionRepo.findByUserAndPremiumReaction(sender, premiumReaction);
					if (userPremiumReaction == null) {
						// User hasn't purchased this reaction
						String errorMsg = "[Server] You need to purchase the " + premiumReaction.getName() + " reaction first";
						sendToUserInSession(sessionId, username, errorMsg);
						return;
					}

					// Create reaction object with premium reaction
					Reaction reaction = new Reaction();
					reaction.setReactionType(reactionType);
					reaction.setUser(sender);
					reaction.setMessage(message);
					//reaction.setPremiumReaction(premiumReaction);

					// Save message and reaction
					reactionRepo.save(reaction);
					message.addReaction(reaction);
					msgRepo.save(message);
				} else {
					// Standard reaction processing (same as before)
					Reaction reaction = new Reaction();
					reaction.setReactionType(reactionType);
					reaction.setUser(sender);
					reaction.setMessage(message);

					// Save message and reaction
					reactionRepo.save(reaction);
					message.addReaction(reaction);
					msgRepo.save(message);
				}

				// Return updated message in the same format for both standard and premium reactions
				String returnString = "[Reaction]:" + messageId + ":" + reactionType;
				broadcastToSession(sessionId, returnString);
				return;
			}
		}

		// Premium Reactions. Format: "[PremiumReaction]:messageId:reactionId"
		if (content.startsWith("[PremiumReaction]:")) {
			String[] parts = content.substring("[PremiumReaction]:".length()).split(":");

			if (parts.length == 2) {
				long messageId = Long.parseLong(parts[0]);
				long premiumReactionId = Long.parseLong(parts[1]);

				// Get the message
				Message message = msgRepo.findById(messageId);
				if (message == null) {
					logger.error("Message with ID " + messageId + " not found");
					return;
				}

				// Get the premium reaction - using Optional properly
				Optional<PremiumReaction> premiumReactionOpt = premiumReactionRepo.findById(premiumReactionId);
				if (!premiumReactionOpt.isPresent()) {
					logger.error("Premium reaction with ID " + premiumReactionId + " not found");
					return;
				}

				PremiumReaction premiumReaction = premiumReactionOpt.get();

				// Check if user has purchased this premium reaction
				UserPremiumReaction userPremiumReaction = userPremiumReactionRepo.findByUserAndPremiumReaction(sender, premiumReaction);
				if (userPremiumReaction == null) {
					// User hasn't purchased this reaction
					String errorMsg = "[Server] You need to purchase this reaction first";
					sendToUserInSession(sessionId, username, errorMsg);
					return;
				}

				// Create reaction object with premium reaction
				Reaction reaction = new Reaction(sender, premiumReaction);
				reaction.setMessage(message);

				// Save message and reaction
				reactionRepo.save(reaction);
				message.addReaction(reaction);
				msgRepo.save(message);

				// Return updated message with premium reaction info
				Map<String, Object> reactionResponse = new HashMap<>();
				reactionResponse.put("type", "premiumReaction");
				reactionResponse.put("messageId", messageId);
				reactionResponse.put("reactionId", premiumReactionId);
				reactionResponse.put("reactionName", premiumReaction.getName());
				reactionResponse.put("emojiCode", premiumReaction.getEmojiCode());
				reactionResponse.put("username", username);

				try {
					String responseJson = objectMapper.writeValueAsString(reactionResponse);
					broadcastToSession(sessionId, responseJson);
				} catch (JsonProcessingException e) {
					logger.error("Error serializing premium reaction response", e);
				}

				return;
			}

		}

		// Regular message handling
		// Get current time
		LocalDateTime now = LocalDateTime.now();
		Date sentTime = new GregorianCalendar(
				now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth(),
				now.getHour(), now.getMinute(), now.getSecond()).getTime();
		// Save message with sender, time, and session information
		Message message = new Message(sender, content, sentTime, chatSession);
		msgRepo.save(message);
		// Save this message as the last message sent
		chatSession.setLastMessage(message);
		chatSessionRepo.save(chatSession);

		String messageString = objectMapper.writeValueAsString(message);
		broadcastToSession(sessionId, messageString);
	}

	@OnClose
	public void onClose(Session session, @PathParam("chatSession_id") String sessionId,
						@PathParam("username") String username) throws IOException {

		Set<Session> sessionSet = sessions.get(sessionId);
		if (sessionSet != null) {
			sessionSet.remove(session);
			if (sessionSet.isEmpty()) {
				sessions.remove(sessionId);
			}
		}

		userMap.remove(session);

		String leaveMsg = "[Server] " + username + " left the chat";
		broadcastToSession(sessionId, leaveMsg);
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		logger.error("WebSocket error: ", throwable);
		throwable.printStackTrace();
	}

	private void sendToUserInSession(String sessionId, String username, String message) {
		Set<Session> sessionSet = sessions.get(sessionId);
		if (sessionSet != null) {
			for (Session s : sessionSet) {
				String connectedUsername = userMap.get(s);
				if (username.equals(connectedUsername) && s.isOpen()) {
					try {
						s.getBasicRemote().sendText(message);
						return;
					} catch (IOException e) {
						logger.error("Error sending message to user", e);
					}
				}
			}
		}
	}

	private List<Message> getChatHistory(String sessionId) {
		ChatSession chatSession = chatSessionRepo.findBySessionId(sessionId);
		return msgRepo.findByChatSession(chatSession);
	}

	private void broadcastToSession(String sessionId, String sendString) {
		Set<Session> sessionSet = sessions.get(sessionId);
		if (sessionSet != null) {
			for (Session s : sessionSet) {
				if (s.isOpen()) {
					try {
						s.getBasicRemote().sendText(sendString);
					} catch (IOException e) {
						logger.error("Error broadcasting to session", e);
					}
				}
			}
		}
	}
}