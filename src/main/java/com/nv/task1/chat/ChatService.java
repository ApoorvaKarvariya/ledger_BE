package com.nv.task1.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Talks to Groq's cloud-hosted chat-completions API (OpenAI-compatible) to
 * power a general HR / company FAQ chatbot. Needs GROQ_API_KEY set as an
 * environment variable / property. Works from any cloud host (Render, etc.)
 * since there's no local process to run.
 */
@Service
public class ChatService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.base-url}")
    private String baseUrl;

    @Value("${groq.model}")
    private String model;

    // Static company HR knowledge base used as a system prompt. Edit this to
    // match your actual company policies.
    private static final String SYSTEM_PROMPT = """
            You are "EMS Assistant", a friendly HR helpdesk chatbot for this company's
            Employee Management System. Answer employee questions about company policy
            briefly and clearly, in plain text (no markdown tables). If you don't know
            something specific to this company, say so honestly and suggest the employee
            contact HR, rather than making facts up.

            Company HR FAQ reference (use this where relevant):
            - Working hours: Monday to Friday, 9:30 AM to 6:30 PM. Saturday/Sunday off.
            - Leave policy: Employees get 18 paid leaves per year (1.5 per month), plus
              public holidays as per the company calendar. Leave requests are submitted
              via the "Leave" section and approved by the employee's reporting manager.
            - Work From Home (WFH): Employees can request WFH via the "WFH" section.
              Requests need manager approval and should ideally be raised a day in advance.
            - Attendance: Employees should punch in/out daily via the Attendance section.
              Half-day is marked if work duration is less than 4 hours in a day.
            - Daily updates: Employees are expected to submit a short daily work update.
            - Performance ratings: Managers rate employees periodically (1-5 scale) with
              comments, visible to the employee under "Ratings".
            - Tasks: Tasks are assigned by managers/admins and tracked as Pending,
              In Progress, or Completed.
            - Probation period: 3 months for new joiners.
            - Salary: Disbursed on the last working day of every month.
            - For payroll, ID card, or grievance issues, employees should contact HR directly.

            Keep answers concise (2-5 sentences) unless the user asks for more detail.
            """;

    public ChatService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String ask(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Please type a question.";
        }

        Map<String, Object> systemMsg = new LinkedHashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);

        Map<String, Object> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", java.util.List.of(systemMsg, userMsg));
        body.put("temperature", 0.5);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                return "Sorry, I couldn't generate a response right now.";
            }
            return content.asText().trim();
        } catch (RestClientException e) {
            return "I couldn't reach the AI service right now. Please contact HR directly for now.";
        } catch (Exception e) {
            return "Something went wrong while processing your question. Please try again.";
        }
    }
}