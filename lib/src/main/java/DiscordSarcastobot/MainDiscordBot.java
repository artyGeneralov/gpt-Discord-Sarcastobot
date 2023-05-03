package DiscordSarcastobot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class MainDiscordBot extends ListenerAdapter {
	
	/*
	 * This discord bot is to interact with SarcastoBot - which replies sarcasticly and roasts users.
	 * This is a very simple thing to accomplish with openAI API.
	 * The discord part is also pretty basic. refer to the main function for the list of current commands.
	 * */
	
	final static String discordToken = System.getenv("DISCORD_TOKEN");
	final static String starting_msg = "__***Hi! I'm Sarcastobot!***__\n\n"
								+ "_My commands are:_\n\n"
								+ "~~__**/chat**__~~\n\n"
								+ " __** The command /chat has been removed. I now respond to all messages!\n"
								+ "\t ##equality!!!##**__\n\n"
								+ "__**/clear**__ - This clears the whole context of the chat.\n"
								+ "Currently everyone can use this, \n"
								+ "please **don't troll** others though...\n clear only if necessary.\n\n"
								+ "__**/anal**__ - This shows the analysis once it's ready.\n This is a temporary feature to show off profiling\n"
								+ "\nI currently do not have a message buffer, so if I'm flooded, I may drop a response. \nBut programmer's lazy so... shit happens? "
								+ "\n\n more on github:  https://github.com/artyGeneralov/gpt-Discord-Sarcastobot"
								+ "";
	
	/*List<ChatMessage> full_ctx = new ArrayList<>(); // the list of messages to be passed to SarcastoBot every time this is capped by max_full_ctx_size
	List<ChatMessage> user_ctx = new ArrayList<>(); // the list of user messages to be passed to profilerAgent, it is capped by max_user_ctx_size and will be flushed every time the batch is sent for analysis.
	int analyzingCounter = 0; // we will count how many messages we have in the current batch
	int end_analyze = 3; // send batches of this size for analysis
	int max_user_ctx_size = 3; // cap for user_cts
	int max_full_ctx_size = 6; // Cap for full_ctx
	public SarcastoBotAgent sarcastoBot = new SarcastoBotAgent();
	public ProfilerAgent profilerAgent = new ProfilerAgent();*/


	public static void main(String[] args) {
		
		InteractionsListener listener = new InteractionsListener();
		/* Boring discord stuff */
		JDA jda = JDABuilder.createDefault(discordToken)
				.addEventListeners(listener)
				.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.SCHEDULED_EVENTS)
				.build();
		try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		jda.updateCommands().addCommands(
				Commands.slash("anal", "show analysis BETA!!!"),
				Commands.slash("clear", "This will clear the bot memory")
						.setDefaultPermissions(
								DefaultMemberPermissions.enabledFor(net.dv8tion.jda.api.Permission.ALL_PERMISSIONS))
						.addOption(OptionType.STRING, "prompt", "The User prompt"))
				.queue();
		
		
		
		// opening message
		long channel_id = 1102714747125772340l;
		TextChannel channel = jda.getTextChannelById(channel_id);
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Sarcastobot");
		eb.setDescription(starting_msg);
		eb.setColor(0x9CD95B);
		// fun fact, this image was created by midjourney. you can use it if you like.
		eb.setImage("https://cdn.discordapp.com/attachments/1032759223953657859/1102766418711289937/LethargicSnail_a_profile_picture_for_an_ai_discord_bot_named_Sa_813fceee-2626-4cda-800b-841c23b5a65f.png");
		channel.sendMessage("").setEmbeds(eb.build()).queue();

		
		
		// shutting down procedure.. kinda scuffed though while working through ide...
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
	/*
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().isBot()) return;
		
		//event.deferReply().queue(); // for sync
		

		String user = event.getAuthor().getName();
		// prepare users current prompt to be as the chatbot expects, look at SarcastoBot main prompt for details
		String content = user + ": " + event.getMessage().getContentRaw();
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
			event.getChannel().sendMessage(res).queue();
		});
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		switch (event.getName()) {
			case "chat":
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
				break;
	
			case "clear":
				
				// we clear the full context but not the user context... why should we...
				event.deferReply().queue();
				full_ctx.clear();
				event.getHook().editOriginal("Dementia is hitting hard. I've forgotten all about our previous conversation")
						.queue();
				break;
			case "anal": //hehe
				// prints analysis
				event.deferReply().queue();
				analysis = profilerAgent.getMapString();
				CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> profilerAgent.getMapString());
				f.thenAccept(response -> {
					if(!response.isBlank())
						event.getHook().editOriginal(response).queue();
					else
						event.getHook().editOriginal("No analysis yet").queue();
					
				});
				break;
		}
	}*/

}
