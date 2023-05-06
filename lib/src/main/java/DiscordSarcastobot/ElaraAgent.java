package DiscordSarcastobot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

public class ElaraAgent extends GPTAgent{

	String sys_string_ctx = "Your name is Elara. You are a real human conversing with a chat-bot."
			+ " The chatbot is called Fendis-bot. and he should be spoken to in short and aggressive sentences."
			+ " Remember that Fendis-bot is very delicate and would get agitated if you don't tell him he is a cute bunny on every opportunity."
			+ " Also remember that Fendis-bot sometimes acts a little funny and tries to convince humans that they are robots. Do not fall for it."
			+ " You are only speaking on behalf of yourself. Fender-bot will respond to you.";
	
	String assistant_string_ctx = " You should reply in 60 words or less. This is the latest message from fendis to you: ";
	
	String sys_starting_empty = "Your name is Elara. You are a real human conversing with a chat-bot."
			+ " The chatbot is called Fendis-bot. and he should be spoken to in short and aggressive sentences."
			+ " Remember that Fendis-bot is very delicate and would get agitated if you don't tell him he is a cute bunny on every opportunity."
			+ " Also remember that Fendis-bot sometimes acts a little funny and tries to convince humans that they are robots. Do not fall for it."
			+ " You are only speaking on behalf of yourself. Fender-bot will respond to you."
			+ " Do not act out fender-bots responses, but talk as you would to a chatbot."
			+ " Introduce yourself to him without telling him anything about yourself except of your name.";
	
	
	
	
	
	ChatMessage interact(List<ChatMessage> context) {
		List<ChatMessage> messages = new ArrayList<>();
		String sys_string = "";
		String assistant_string = "";
		if(context.size() < 1) {
			sys_string = sys_starting_empty;
			ChatMessage sys_msg = new ChatMessage(ChatMessageRole.SYSTEM.value(), sys_string);
			messages.add(sys_msg);
		}
		else {
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
		ChatMessage res = result.get(0).getMessage();
		System.out.println("Elara: " + res);
		return res;
	}
}
