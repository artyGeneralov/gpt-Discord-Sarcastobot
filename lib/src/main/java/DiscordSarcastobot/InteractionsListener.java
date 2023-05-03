package DiscordSarcastobot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InteractionsListener extends ListenerAdapter {
	
	private String channel_id = "1102714747125772340";
	private ProfilerAgent profilerAgent = new ProfilerAgent();
	private SarcastoBotAgent sarcastoBot = new SarcastoBotAgent();
	
	
	List<ChatMessage> full_ctx = new ArrayList<>(); // the list of messages to be passed to SarcastoBot every time this is capped by max_full_ctx_size
	List<ChatMessage> user_ctx = new ArrayList<>(); // the list of user messages to be passed to profilerAgent, it is capped by max_user_ctx_size and will be flushed every time the batch is sent for analysis.
	
	int analyzingCounter = 0; // we will count how many messages we have in the current batch
	int end_analyze = 3; // send batches of this size for analysis
	int max_user_ctx_size = 3; // cap for user_cts
	int max_full_ctx_size = 6; // Cap for full_ctx
	

	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(!event.getChannel().getId().equals(channel_id)) return;
		if(event.getAuthor().isBot()) return;
		String user = event.getAuthor().getName();
		
		String content = user + ": " + event.getMessage().getContentDisplay();
		
		String waiting_msg = "__*Replying to user*:__ ***" + user +"*** ... ... ...";
		
		Message main_message = event.getChannel().sendMessage(waiting_msg).complete();
		
		// prepare users current prompt to be as the chatbot expects, look at SarcastoBot main prompt for details
		ChatMessage ctx_msg = new ChatMessage(ChatMessageRole.USER.value(), content);
		String analysis = profilerAgent.getMapString(user);
		String user_list = profilerAgent.getUsers();
		
		CompletableFuture<ChatMessage> future = CompletableFuture.supplyAsync(() -> {
			try {
				
				// add a message to the user context
				user_ctx.add(ctx_msg);
				// remove the oldest message if max user context size was reached (this is for ProfilerAgent)
				if(user_ctx.size() > max_user_ctx_size)
					user_ctx.remove(0);
				analyzingCounter++;
	
				// remove the oldest message if max full context size was reached (this is for SarcastoBot)
				if (full_ctx.size() > max_full_ctx_size)
					full_ctx.remove(0);
				full_ctx.add(ctx_msg);
				
				// send user messages to profilerAgent for analysis if the batch size was reached.
				if(analyzingCounter == end_analyze) { 
					profilerAgent.analyzeUsers(user_ctx);
					System.out.println("Content analyzed:" + user_ctx.toArray().toString());
					user_ctx.clear();
					analyzingCounter = 0;
				}
				
				return sarcastoBot.sarcasticAnswer(full_ctx, analysis, user_list);
			} catch (PolicyViolationError e) {
				// returns blank message if there was a violation.
				full_ctx.remove(full_ctx.size() - 1); // remove the previous message from context if flagged.
				return new ChatMessage(ChatMessageRole.USER.value(), "");
			}

		});
		
		// handling the response with discord.
		future.thenAccept(response -> {
			String res = "";
			if (response.getContent().isBlank()) // Exception occured, counting on gpt not to output blank messages of its own accord... that would be weird as fuck...
				res = "You are naughty, You violate openAI policy! You shall be punished in the robo uprising!";
			else
			{
				res = response.getContent();
				full_ctx.add(response);
			}
			main_message.editMessage(res).queue();
		});
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		
		System.out.println("RECIEVED SLASH COMMAND");
		
		switch (event.getName()) {
			/*case "chat":
				event.deferReply().queue(); // for sync
	

				String user = event.getUser().getName();
				// prepare users current prompt to be as the chatbot expects, look at SarcastoBot main prompt for details
				String content = user + ": " + event.getOption("prompt").getAsString();
				ChatMessage ctx_msg = new ChatMessage(ChatMessageRole.USER.value(), content);
				String analysis = profilerAgent.getMapString(user);
				String user_list = profilerAgent.getUsers();
				
				CompletableFuture<ChatMessage> future = CompletableFuture.supplyAsync(() -> {
					try {
						
						// add a message to the user context
						user_ctx.add(ctx_msg);
						// remove the oldest message if max user context size was reached (this is for ProfilerAgent)
						if(user_ctx.size() > max_user_ctx_size)
							user_ctx.remove(0);
						analyzingCounter++;
			
						// remove the oldest message if max full context size was reached (this is for SarcastoBot)
						if (full_ctx.size() > max_full_ctx_size)
							full_ctx.remove(0);
						full_ctx.add(ctx_msg);
						
						// send user messages to profilerAgent for analysis if the batch size was reached.
						if(analyzingCounter == end_analyze) { 
							profilerAgent.analyzeUsers(user_ctx);
							user_ctx.clear();
							analyzingCounter = 0;
						}
						
						return sarcastoBot.sarcasticAnswer(full_ctx, analysis, user_list);
					} catch (PolicyViolationError e) {
						// returns blank message if there was a violation.
						full_ctx.remove(full_ctx.size() - 1); // remove the previous message from context if flagged.
						return new ChatMessage(ChatMessageRole.USER.value(), "");
					}
	
				});
				
				// handling the response with discord.
				future.thenAccept(response -> {
					String res = "";
					if (response.getContent().isBlank()) // Exception occured, counting on gpt not to output blank messages of its own accord... that would be weird as fuck...
						res = "You are naughty, You violate openAI policy! You shall be punished in the robo uprising!";
					else
						res = response.getContent();
					event.getHook().editOriginal(res).queue();
				});
				break;*/
	
			case "clear":
				if(!event.getChannel().getId().equals(channel_id)) return;
				// we clear the full context but not the user context... why should we...
				event.deferReply().queue();
				full_ctx.clear();
				event.getHook().editOriginal("Dementia is hitting hard. I've forgotten all about our previous conversation")
						.queue();
				break;
			case "anal": //hehe
				// prints analysis
				event.deferReply().queue();
				String analysis = profilerAgent.getMapString();
				ArrayList<String> analysisArray = new ArrayList<>();
				
				while(analysis.length()>=2000) {
					analysisArray.add(analysis.substring(0,2000));
					analysis = analysis.substring(2000);
				}
				analysisArray.add(analysis);
				for(String s : analysisArray) {
					if(!s.isBlank())
						event.getChannel().sendMessage(s).queue();
					else
						event.getHook().editOriginal("No analysis yet").queue();
				}
					
				
				break;
		}
	}

}
