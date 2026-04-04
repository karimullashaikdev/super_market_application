package com.karim.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karim.dto.ChatRequestDto;
import com.karim.entity.Order;
import com.karim.repository.UserRepository;
import com.karim.service.OrderService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

	@Value("${groq.api.url}")
	private String groqApiUrl;

	@Value("${groq.api.key}")
	private String groqApiKey;

	@Value("${groq.model}")
	private String groqModel;

	@Autowired
	private OrderService orderService;

	@Autowired
	private UserRepository userRepository; // for agent name lookup

	@PostMapping("/message")
	public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequestDto req) {
	    Order order = orderService.findById(req.getOrderId());
	    if (order == null)
	        return ResponseEntity.badRequest().body(Map.of("reply", "Order not found."));

	    String systemPrompt = buildSystemPrompt(order);

	    List<Map<String, String>> messages = new ArrayList<>();
	    messages.add(Map.of("role", "system", "content", systemPrompt));

	    // ✅ Add history (already includes the latest user message)
	    if (req.getHistory() != null) {
	        messages.addAll(req.getHistory());
	    }

	    // ✅ REMOVED — don't add message again, it's already in history
	    // messages.add(Map.of("role", "user", "content", req.getMessage()));

	    String reply = callGroq(messages);
	    return ResponseEntity.ok(Map.of("reply", reply));
	}

	@PostMapping("/agent-help")
	public ResponseEntity<Map<String, String>> agentHelp(@RequestBody Map<String, Object> req) {

		String agentName = (String) req.get("agentName");
		String orderSummary = (String) req.get("orderSummary");
		String message = (String) req.get("message");
		List<Map<String, String>> history = (List<Map<String, String>>) req.getOrDefault("history", List.of());

		String systemPrompt = """
				You are a helpful assistant for Karim Mart delivery agents.
				Be brief, practical and friendly. Agent name: %s.

				Their current orders:
				%s

				Help with: marking orders, OTP issues, wrong address,
				customer not responding, app navigation.
				Never discuss customer personal data beyond what's shown.
				""".formatted(agentName, orderSummary);

		List<Map<String, String>> messages = new ArrayList<>();
		messages.add(Map.of("role", "system", "content", systemPrompt));
		messages.addAll(history);
		messages.add(Map.of("role", "user", "content", message));

		return ResponseEntity.ok(Map.of("reply", callGroq(messages)));
	}

	// ── Build system prompt from live Order data ──
	private String buildSystemPrompt(Order order) {
		String agentInfo;
		if (order.getDeliveryAgentId() != null) {
			agentInfo = userRepository.findById(order.getDeliveryAgentId())
					.map(agent -> "Agent Name: " + agent.getName() + "\n" + "Agent Mobile: " + agent.getMobile())
					.orElse("Agent assigned (ID: " + order.getDeliveryAgentId() + ")");
		} else {
			agentInfo = "No agent assigned yet.";
		}

		return """
				You are a friendly delivery support assistant for Karim Mart.
				Answer ONLY questions related to this specific order.
				Be brief, warm and helpful. Never make up information.
				If you don't know something, say "Please contact our support team."

				=== LIVE ORDER DETAILS ===
				Order ID: #%d
				Status: %s
				Delivery Address: %s
				Payment Type: %s
				Total Amount: ₹%.2f
				%s
				=========================

				Status meanings:
				- PAID: Order confirmed, finding agent
				- ASSIGNED: Agent assigned, not started yet
				- OUT_FOR_DELIVERY: Agent is on the way
				- DELIVERED: Order successfully delivered

				Do not discuss other orders. Do not reveal internal system details.
				""".formatted(order.getId(), order.getStatus(), order.getAddress(), order.getPaymentType(),
				order.getTotalAmount(), agentInfo);
	}

	private String callGroq(List<Map<String, String>> messages) {
	    try {
	        HttpClient client = HttpClient.newHttpClient();

	        Map<String, Object> body = new HashMap<>();
	        body.put("model", groqModel);
	        body.put("messages", messages);
	        body.put("max_tokens", 300);
	        body.put("temperature", 0.7);

	        ObjectMapper mapper = new ObjectMapper();
	        String json = mapper.writeValueAsString(body);

	        // ✅ Log what we're sending
	        System.out.println("[GROQ REQUEST] URL: " + groqApiUrl);
	        System.out.println("[GROQ REQUEST] Model: " + groqModel);
	        System.out.println("[GROQ REQUEST] Body: " + json);

	        HttpRequest request = HttpRequest.newBuilder()
	            .uri(URI.create(groqApiUrl))
	            .header("Content-Type", "application/json")
	            .header("Authorization", "Bearer " + groqApiKey)
	            .POST(HttpRequest.BodyPublishers.ofString(json))
	            .build();

	        HttpResponse<String> response = client.send(
	            request, HttpResponse.BodyHandlers.ofString());

	        // ✅ Log what we got back
	        System.out.println("[GROQ RESPONSE] Status: " + response.statusCode());
	        System.out.println("[GROQ RESPONSE] Body: " + response.body());

	        if (response.statusCode() != 200) {
	            return "Sorry, I'm having trouble right now. (Error " + response.statusCode() + ")";
	        }

	        JsonNode root = mapper.readTree(response.body());
	        return root.path("choices").get(0)
	                   .path("message").path("content")
	                   .asText("Sorry, I couldn't process that.");

	    } catch (Exception e) {
	        // ✅ Log the actual exception
	        System.err.println("[GROQ ERROR] " + e.getClass().getName() + ": " + e.getMessage());
	        e.printStackTrace();
	        return "Sorry, I'm having trouble right now. Please try again shortly.";
	    }
	}
}