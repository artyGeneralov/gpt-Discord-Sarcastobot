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
	
	final static String sarcastoBot_token = System.getenv("DISCORD_TOKEN");
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
		
		/* New Test: */
		
		
		
		
		
		
		
		
		/* Sarcastobot here: */
		InteractionsListener listener = new InteractionsListener();
		/* Boring discord stuff */
		JDA sarcastobot_jda = JDABuilder.createDefault(sarcastoBot_token)
				.addEventListeners(listener)
				.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.SCHEDULED_EVENTS)
				.build();
		try {
			sarcastobot_jda.awaitReady();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sarcastobot_jda.updateCommands().addCommands(
				Commands.slash("anal", "show analysis BETA!!!"),
				Commands.slash("clear", "This will clear the bot memory")
						.setDefaultPermissions(
								DefaultMemberPermissions.enabledFor(net.dv8tion.jda.api.Permission.ALL_PERMISSIONS))
						.addOption(OptionType.STRING, "prompt", "The User prompt"))
				.queue();
		
		
		
		// opening message
		long channel_id = 1102714747125772340l;
		TextChannel channel = sarcastobot_jda.getTextChannelById(channel_id);
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
				TextChannel channel = sarcastobot_jda.getTextChannelById(channel_id);
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

}
