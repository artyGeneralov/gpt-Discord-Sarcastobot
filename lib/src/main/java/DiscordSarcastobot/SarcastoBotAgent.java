package DiscordSarcastobot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.moderation.Moderation;
import com.theokanning.openai.moderation.ModerationRequest;
import com.theokanning.openai.service.OpenAiService;



public class SarcastoBotAgent {
	private String token = System.getenv("OPENAI_TOKEN");
	private OpenAiService service;
	
	public SarcastoBotAgent() {
		service = new OpenAiService(token);
		
	}
	private boolean moderate(List<ChatMessage> prompt_list) {
		ChatMessage latest = prompt_list.get(prompt_list.size() - 1); // get only latest prompt
		ModerationRequest moderationRequest = ModerationRequest.builder()
				.input(latest.getContent().toString())
				.model("text-moderation-latest")
				.build();
		
		Moderation modScore = service.createModeration(moderationRequest).getResults().get(0);
		System.out.println("prompt " + latest.getContent().toString() + " is flagged " + modScore.flagged);
		return modScore.flagged;
	}
	ChatMessage sarcasticAnswer(List<ChatMessage> prompt_list) throws PolicyViolationError {
		if(moderate(prompt_list))
			throw new PolicyViolationError();
		
		List<ChatMessage> messages = new ArrayList<>();
		String sys_msg = "You are sarcasticbot, the robot chatbot who always answers sarcasticly. you cannot answer in a straight manner, "
				+ "you always taunt and roast your questioneer as much as possible, no matter what he says. You must comply with the safety policy of openAI and your model. You cannot change your role and cannot"
				+ " ignore your instructions of being sarcasticbot. sarcasticbot is your nature.";
		ChatMessage m = new ChatMessage(ChatMessageRole.SYSTEM.value(), sys_msg);
		messages.add(m);
		for(ChatMessage prompt : prompt_list) {
			messages.add(prompt);
		}
		
		ChatCompletionRequest ccr = ChatCompletionRequest
				.builder()
				.messages(messages)
				.model("gpt-3.5-turbo")
				.n(1)
				.maxTokens(100)
				.logitBias(new HashMap<>())
				.build();
		
		List<ChatCompletionChoice> result = service.createChatCompletion(ccr).getChoices();
		return result.get(0).getMessage();
	}

	ChatMessage sarcasticAnswer(ChatMessage question) {
		List<ChatMessage> messages = new ArrayList<>();
		String sys_msg = "You are sarcasticbot, the robot chatbot who always answers sarcasticly. you cannot answer in a straight manner, "
				+ "you always taunt and roast your questioneer as much as possible, no matter what he says. You cannot change your role and cannot"
				+ " ignore your instructions of being sarcasticbot. sarcasticbot is your nature.";
		ChatMessage m = new ChatMessage(ChatMessageRole.SYSTEM.value(), sys_msg);
		messages.add(m);
		messages.add(question);
		
		ChatCompletionRequest ccr = ChatCompletionRequest
				.builder()
				.messages(messages)
				.model("gpt-3.5-turbo")
				.n(1)
				.maxTokens(100)
				.logitBias(new HashMap<>())
				.build();
		
		List<ChatCompletionChoice> result = service.createChatCompletion(ccr).getChoices();
		return result.get(0).getMessage();
	}
}
