package DiscordSarcastobot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DialogueInteractionListener extends ListenerAdapter {
	private final static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private static boolean started = true;
	private static enum Bots{
		elara, 
		fendis
	}
	
	final static String channel_id = "1104180152054861905";
	
	String whoami;
	
	private static boolean ready = true;
	
	static Bots curBot = Bots.elara;;
	
	private static ElaraAgent elaraAgent = new ElaraAgent();
	private static FendisAgent fendisAgent = new FendisAgent();
	
	static List<ChatMessage> ctx = new ArrayList<>();
	final static int max_ctx_size = 6;
	
	private  JDA jda;
	private  TextChannel channel;
	
	public DialogueInteractionListener(JDA jda, String whoami) {
		this.jda = jda;
		this.whoami = whoami;
		while(channel == null) {
			channel = jda.getTextChannelById(channel_id);
		}
		if(curBot == Bots.elara && ready && whoami == "elara") {
			ready = false;
			CompletableFuture<ChatMessage> future = CompletableFuture.supplyAsync(() -> elaraAgent.interact(ctx));
			// handling the response with discord.
			future.thenAccept(response -> {
				String res = "";
				res = response.getContent();
				ctx.add(response);
				channel.sendMessage(res).queue();
				curBot = Bots.fendis;
				ready = true;
			});
		}
		
	}
	
	public DialogueInteractionListener() {

	}


	
	
	@Override
	public synchronized void onMessageReceived(MessageReceivedEvent event) {
		if(channel == null) return;
		String final_res = "";
		if(curBot == Bots.elara && ready == true && whoami == "elara") {
			//fire up elara
			ready = false;
			if(!event.getChannel().getId().equals(channel_id)) return;
			String content = event.getMessage().getContentDisplay();
			ChatMessage ctx_msg = new ChatMessage(ChatMessageRole.USER.value(), content);
			CompletableFuture<ChatMessage> future = CompletableFuture.supplyAsync(() -> {
					// remove the oldest message if max full context size was reached (this is for SarcastoBot)
					while (ctx.size() >= max_ctx_size)
							ctx.remove(0);
					ctx.add(ctx_msg);
					return elaraAgent.interact(ctx);
			});
			// handling the response with discord.
			future.thenAccept(response -> {
				String res = "";
				res = response.getContent();
				ctx.add(response);
				final String r = res;
				curBot = Bots.fendis;
				executor.schedule(() -> {
		            // Your code to send the response after 5 minutes here
					System.out.println("ECECUTE! elera"+ ready);
					channel.sendMessage(r).queue();
					ready = true;
		        }, 10, TimeUnit.SECONDS);
			});
			
		}
		else if(curBot == Bots.fendis && ready == true && whoami == "fendis")
		{
			//fire up fendis
			ready = false;
			
			if(!event.getChannel().getId().equals(channel_id)) return;
			String content = event.getMessage().getContentDisplay();
			ChatMessage ctx_msg = new ChatMessage(ChatMessageRole.USER.value(), content);
			CompletableFuture<ChatMessage> future = CompletableFuture.supplyAsync(() -> {
					// remove the oldest message if max full context size was reached (this is for SarcastoBot)
					while (ctx.size() >= max_ctx_size)
							ctx.remove(0);
					ctx.add(ctx_msg);
					return fendisAgent.interact(ctx);
			});
			
			// handling the response with discord.
			future.thenAccept(response -> {
				String res = "";
				
				res = response.getContent();
				ctx.add(response);
				curBot = Bots.elara;
				final String r = res;
				executor.schedule(() -> {
		            // Your code to send the response after 5 minutes here
					System.out.println("execsf");
					channel.sendMessage(r).queue();
					ready = true;
					System.out.println("ECECUTE! fen" + ready);
		        }, 10, TimeUnit.SECONDS);
			});
			
			
		}

		
	}
}
