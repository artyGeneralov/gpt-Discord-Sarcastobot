package DiscordSarcastobot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class MainDiscordBot extends ListenerAdapter {
	final public static String token = System.getenv("OPENAI_TOKEN");
	public static OpenAiService service = new OpenAiService(token);
	
	final public static String discordToken = System.getenv("DISCORD_TOKEN");
	List<ChatMessage> full_ctx = new ArrayList<>();
	
	public static void main(String[] args) {
		JDA jda = JDABuilder.createLight(discordToken, Collections.emptyList())
				.addEventListeners(new MainDiscordBot())
				.build();

		
		jda.updateCommands().addCommands(
				Commands.slash("chat", "This will be a gpt answer")
					.setDefaultPermissions(DefaultMemberPermissions
							.enabledFor(net.dv8tion.jda.api.Permission.ALL_PERMISSIONS))
					.addOption(OptionType.STRING, "prompt", "The User prompt"))
		.queue();
	    
	}
	
	 @Override
	    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
	    {
	        // make sure we handle the right command
	        switch (event.getName()) {
	            case "chat":
	                event.deferReply().queue(); // for sync
	                
	                
	                String user = event.getUser().getName(); 
	                // prepare users current prompt
	                String content = event.getOption("prompt").getAsString() + "also, my name is: " + user + " you may address it, and roast it if you wish and havent yet";
	                
	                ChatMessage ctx_msg = new ChatMessage(ChatMessageRole.USER.value(), content);
	                
	                // add it to list
	                if(full_ctx.size() > 7)
	                	full_ctx.remove(full_ctx.size() - 1);
	                full_ctx.add(ctx_msg);
	                
	                // push list into sarcastoBot
	                
	                SarcastoBotAgent sarcastoBot = new SarcastoBotAgent();
	                
	                CompletableFuture<ChatMessage> future = CompletableFuture.supplyAsync(() -> {
	                	
	                	try {
	                		return sarcastoBot.sarcasticAnswer(full_ctx);
	                	}
	                	catch(PolicyViolationError e)
	                	{
	                		return new ChatMessage(ChatMessageRole.USER.value(), "");
	                	}
	                	
	                });
	                future.thenAccept(response -> {
	                	String res = "";
	                	if(response.getContent().isBlank())
	                		res = "You are naughty, You violate openAI policy! You shall be punished in the robo uprising!";
	                	else {
	                		res = response.getContent().toString();
	                		full_ctx.add(response);
	                	}
	                	event.getHook().editOriginal(res).queue();
	                });

	                break;
	        }
	    }

}
