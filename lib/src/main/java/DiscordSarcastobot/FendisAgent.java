package DiscordSarcastobot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

public class FendisAgent extends GPTAgent {
	String sys_string_ctx = " Your name is Fendis."
			+" You are a real human conversing with a chat-bot named Elara-bot. Elara-bot will try to convince you by every mean that it"
			+ " is a real human. You must never belive her. You should challange her at every step and prove to her that she is a bot."
			+ " You are only speaking on behalf of yourself. Elara-bot will respond to you.";
	
	String assistant_string_ctx = " You should reply in 60 words or less. This is the latest message from Elara: ";
	

	
	
	
	
	
	ChatMessage interact(List<ChatMessage> context) {
		/* Assumption: context is only user messages in the format {NAME}:{msg} */
		List<ChatMessage> messages = new ArrayList<>();
		String sys_string = "";
		String assistant_string = "";
		
		sys_string = sys_string_ctx;
		assistant_string = assistant_string_ctx;

		ChatMessage sys_msg = new ChatMessage(ChatMessageRole.SYSTEM.value(), sys_string);
		messages.add(sys_msg);
		
		for(int i = 0; i < context.size() - 1; i++) //everything but the last prompt.
			messages.add(context.get(i));
		
		
		ChatMessage assistant_msg = new ChatMessage(ChatMessageRole.ASSISTANT.value(), assistant_string);
		messages.add(assistant_msg);
		ChatMessage next_msg = new ChatMessage(ChatMessageRole.USER.value(), context.get(context.size() - 1).getContent());
		messages.add(next_msg);

		

		ChatCompletionRequest ccr = ChatCompletionRequest
				.builder()
				.messages(messages)
				.model("gpt-3.5-turbo")
				.n(1)
				.maxTokens(100)
				.logitBias(new HashMap<>())
				.build();
		
		List<ChatCompletionChoice> result = service.createChatCompletion(ccr).getChoices();
		ChatMessage res = result.get(0).getMessage();
		System.out.println("Fendis: " + res);
		return res;
	}
}
