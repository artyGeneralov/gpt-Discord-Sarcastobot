package DiscordSarcastobot;

import java.io.IOException;
import java.util.Scanner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
	
	//TODO:
	final static String elaraBot_token = System.getenv("ELERA_TOKEN");
	final static String fendisBot_token = System.getenv("FENDIS_TOKEN");

	final static String sarcastoBot_token = System.getenv("SARCASTOBOT_TOKEN");
	final static String starting_msg = "__***Hi! I'm Sarcastobot!***__\n\n"
								+ "__My commands are:__\n\n"
								+ "__**/clear**__ - This clears the whole context of the chat.\n"
								+ "Currently everyone can use this, \n"
								+ "please **don't troll** others though...\n clear only if necessary.\n\n"
								+ "__**/anal**__ - This shows the analysis once it's ready.\n This is a temporary feature to show off profiling\n"
								+ "You can get analysis by username now,\n so please avoid cluttering the whole screen...\n\n"
								+ "\nI currently do not have a message buffer, so if I'm flooded, I may drop a response. \nBut programmer's lazy so... shit happens?\n "
								+ " \n__** Im GPT-4 Now Bitches!** __"
								+ "\n\n more on github:  https://github.com/artyGeneralov/gpt-Discord-Sarcastobot"
								+ "";
	
	static boolean started = true;

	public static void main(String[] args) {
//		Scanner input = new Scanner(System.in);
//
//		/* New Test: */
//		
//		/*Bot elara*/
//		JDA elara_jda = JDABuilder.createDefault(elaraBot_token)
//				.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.SCHEDULED_EVENTS)
//				.build();
//		
//
//		elara_jda.addEventListener(new DialogueInteractionListener(elara_jda, "elara"));
//		
//		/*Bot Fendis*/
//		JDA fendis_jda = JDABuilder.createDefault(fendisBot_token)
//				
//				.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.SCHEDULED_EVENTS)
//				.build();
//		fendis_jda.addEventListener(new DialogueInteractionListener(fendis_jda, "fendis"));
		
		/* Sarcastobot here: */
		SarcastobotInteractionsListener listener = new SarcastobotInteractionsListener();
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
				Commands.slash("anal", "show analysis BETA!!!").addOption(OptionType.USER, "user", "the user to analyze", false),
				Commands.slash("clear", "This will clear the bot memory")
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(net.dv8tion.jda.api.Permission.ALL_PERMISSIONS)))
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

//		Thread consoleListener = new Thread() {
//			public void run(){
//				while(true) {
//					int i = input.nextInt();
//					if(i == 0)
//					{
//						final String cid = "1104180152054861905";
//						elara_jda.getTextChannelById(cid).sendMessage(".").queue();
//					}
//						
//				}
//			}
//		};
		
		// shutting down procedure.. kinda scuffed though while working through ide...
		/*8Runtime.getRuntime().addShutdownHook(new Thread() {
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
	    System.exit(0);*/
	}

}
