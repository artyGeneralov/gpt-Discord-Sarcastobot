package DiscordSarcastobot;

import com.theokanning.openai.service.OpenAiService;

public class GPTAgent {
	private String token = System.getenv("OPENAI_TOKEN");
	protected OpenAiService service;
	public GPTAgent(){
		service = new OpenAiService(token);
	}
}
