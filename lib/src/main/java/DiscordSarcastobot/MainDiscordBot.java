package DiscordSarcastobot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class MainDiscordBot extends ListenerAdapter {
	final static String discordToken = System.getenv("DISCORD_TOKEN");
	List<ChatMessage> full_ctx = new ArrayList<>();
	List<ChatMessage> user_ctx = new ArrayList<>();
	
	int analyzingCounter = 0;
	int end_analyze = 3;
	int max_user_ctx_size = 3;
	int max_full_ctx_size = 8;
	public SarcastoBotAgent sarcastoBot = new SarcastoBotAgent();
	public ProfilerAgent profilerAgent = new ProfilerAgent();

	public static void main(String[] args) {
		
		JDA jda = JDABuilder.createLight(discordToken, Collections.emptyList()).addEventListeners(new MainDiscordBot())
				.build();

		try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		jda.updateCommands().addCommands(
				Commands.slash("anal", "show analysis BETA!!!"),
				Commands.slash("clear", "This will clear the bot memory"),
				Commands.slash("chat", "This will be a gpt answer")
						.setDefaultPermissions(
								DefaultMemberPermissions.enabledFor(net.dv8tion.jda.api.Permission.ALL_PERMISSIONS))
						.addOption(OptionType.STRING, "prompt", "The User prompt"))
				.queue();
		
		long channel_id = 1102714747125772340l;
		


		TextChannel channel = jda.getTextChannelById(channel_id);
		channel.sendMessage("I'm here!").queue();

		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				TextChannel channel = jda.getTextChannelById(channel_id);
				channel.sendMessage("I'm down for maintnance and a quick fuck with your mom. brb.").queue();
			}
		});
		
		
		
	    try {
	        System.in.read();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    System.exit(0);
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		// make sure we handle the right command
		switch (event.getName()) {
			case "chat":
				event.deferReply().queue(); // for sync
	

				String user = event.getUser().getName();
				// prepare users current prompt
				
				String content = user + ": " + event.getOption("prompt").getAsString();
				ChatMessage ctx_msg = new ChatMessage(ChatMessageRole.USER.value(), content);

	
				
				String analysis = profilerAgent.getMapString();
				CompletableFuture<ChatMessage> future = CompletableFuture.supplyAsync(() -> {
	
					try {
						if(analyzingCounter == end_analyze) {
							profilerAgent.analyzeUsers(user_ctx);
							user_ctx.clear();
							analyzingCounter = 0;
						}
						
						user_ctx.add(ctx_msg);
						if(user_ctx.size() > max_user_ctx_size)
							user_ctx.remove(0);
						analyzingCounter++;
			
						// add it to list
						if (full_ctx.size() > max_full_ctx_size)
							full_ctx.remove(0);
						full_ctx.add(ctx_msg);
						
						return sarcastoBot.sarcasticAnswer(full_ctx, analysis);
					} catch (PolicyViolationError e) {
						
						return new ChatMessage(ChatMessageRole.USER.value(), "");
					}
	
				});
				future.thenAccept(response -> {
					String res = "";
					if (response.getContent().isBlank())
						res = "You are naughty, You violate openAI policy! You shall be punished in the robo uprising!";
					else {
						res = response.getContent().toString();
						full_ctx.add(response);
					}
					event.getHook().editOriginal(res).queue();
				});
	
				break;
	
			case "clear":
				event.deferReply().queue();
				full_ctx.clear();
				event.getHook().editOriginal("Dementia is hitting hard. I've forgotten all about our previous conversation")
						.queue();
				break;
			case "anal":
				event.deferReply().queue();
				analysis = profilerAgent.getMapString();
				
				CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> profilerAgent.getMapString());
				f.thenAccept(response -> {
					if(!response.isBlank())
						event.getHook().editOriginal(response).queue();
					else
						event.getHook().editOriginal("No analysis yet").queue();
					
				});
				
				
				System.out.println("analysis from anal : " + analysis);
				break;
		}
	}

}
