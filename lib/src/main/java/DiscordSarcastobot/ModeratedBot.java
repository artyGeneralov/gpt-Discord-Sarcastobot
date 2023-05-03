package DiscordSarcastobot;

import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.moderation.Moderation;
import com.theokanning.openai.moderation.ModerationRequest;

public class ModeratedBot extends GPTAgent {

	
	public ModeratedBot() {
		super();
	}

	protected boolean moderate(List<ChatMessage> prompt_list) {
		ChatMessage latest = prompt_list.get(prompt_list.size() - 1); // get only latest prompt
		ModerationRequest moderationRequest = ModerationRequest.builder()
				.input(latest.getContent().toString())
				.model("text-moderation-latest")
				.build();
		
		Moderation modScore = service.createModeration(moderationRequest).getResults().get(0);
		if(!modScore.isFlagged())
			System.out.println("prompt:: " + latest.getContent().toString() + " ~~ is flagged " + modScore.flagged);
		else
			System.err.println("prompt:: " + latest.getContent().toString() + " ~~ is flagged " + modScore.flagged);
		return modScore.flagged;
	}
	
	protected boolean moderate(ChatMessage prompt) {
		
		ModerationRequest moderationRequest = ModerationRequest.builder()
				.input(prompt.getContent().toString())
				.model("text-moderation-latest")
				.build();
		
		Moderation modScore = service.createModeration(moderationRequest).getResults().get(0);
		if(!modScore.isFlagged())
			System.out.println("prompt:: " + prompt.getContent().toString() + " ~~ is flagged " + modScore.flagged);
		else
			System.err.println("prompt:: " + prompt.getContent().toString() + " ~~ is flagged " + modScore.flagged);
		return modScore.flagged;
	}
}
