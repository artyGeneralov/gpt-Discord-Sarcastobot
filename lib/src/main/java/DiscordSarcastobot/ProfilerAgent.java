package DiscordSarcastobot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

public class ProfilerAgent extends ModeratedBot {

	/*
	 * The profiler agent: This guy gets batches of conversations (user side only)
	 * He is then tasked with performing a "personality analysis" on the users.
	 * I say it as such because - 
	 * 1. I'm not attempting to claim that this is by any means reliable
	 * 2. This is just to make the chat-bot funnier.
	 * Also, we get only the users prompts here without any context of their conversation with the chatbot.
	 * But it still does some pretty cool things.
	 * Example of an analysis from my own conversation:
	 * 
	 * ;LethargicSnail: Seems curious and playful, might have a silly sense of humor.
	 * ;LethargicSnail: May have a tendency to make inappropriate or offensive jokes, lacks motivation or energy.
	 * ;LethargicSnail: seems curious and has a random sense of humor.
	 * ;LethargicSnail: Easily impressed and curious personality.
	 * ;LethargicSnail: seems curious and has a random sense of humor.
	 * ;LethargicSnail: Holds their privacy dear and likes to clarify possible misunderstandings.
	 * 
	 * Anywho, this data is then parsed and stored in a hash-table. It's also stored locally on a file on my computer, so that the bot does not forget users between sessions.
	 * If you have privacy issues, you can disable the funcionality by removing a bunch of code... 
	 * */
	
	private Map<String, ArrayList<String>> userData = new HashMap<>(); // map to store user analysis data, each key = username and the analysis is stored as arraylist
	private final int max_values_size = 10; // can't store more then 10 values per user to avoid overbloating
	//TODO: add cap on users themselves? how to decide which user to remove? tomorow i'll be smarter...
	private final String file_path = System.getenv("ANAL_FILE_SARCASTOBOT");
	
	private File file = new File(file_path);
	private FileReader fr;
	private BufferedReader br;
	private FileWriter fw;
	private PrintWriter out;
	
	
	public ProfilerAgent() {
		// set profiler with file
		String str = "";
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String cur;
			StringBuilder sb = new StringBuilder();
			while((cur = br.readLine()) != null) {
				sb.append(cur);
			}
			br.close();
			str = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(!str.isBlank()) {
			ChatMessage c = new ChatMessage(ChatMessageRole.ASSISTANT.value(), str);
			mapResponse(c, true);
		}
		
	}


	void analyzeUsers(List<ChatMessage> context) {
		/* Assumption: context is only user messages in the format {NAME}:{msg} */
		List<ChatMessage> messages = new ArrayList<>();
		String sys_string = " You are a personality analyzing robot. You use 50 words or less. You will be given a chat conversation with different users."
				+ " The users in this conversation are conversing with a chatbot."
				+ " The format of this conversation would be \"Name: text\""
				+ " Your job is to analyze the users and write a short sentence about each of them."
				+ " Your output should be in the following format:" + " Name: analysis."
				+ " Each analysis should not be more then a sentence long" + " Start every name with the character ;"
				+ " Note that the people in this conversation don't know about you, they are speaking to a different bot."
				+ " The conversation is as follows:";

		ChatMessage sys_msg = new ChatMessage(ChatMessageRole.SYSTEM.value(), sys_string);
		messages.add(sys_msg);
		for(ChatMessage msg : context) {
			ChatMessage curmsg = new ChatMessage(ChatMessageRole.USER.value(), msg.getContent());
			if(moderate(msg)) // skip a message if its inappropriate to OpenAI policy
				continue;
			messages.add(curmsg);
			System.out.println(curmsg.getContent());
		}

		ChatCompletionRequest ccr = ChatCompletionRequest
				.builder()
				.messages(messages)
				.model("gpt-3.5-turbo")
				.n(1)
				.maxTokens(50)
				.logitBias(new HashMap<>())
				.build();
		
		List<ChatCompletionChoice> result = service.createChatCompletion(ccr).getChoices();
		ChatMessage res = result.get(0).getMessage();
		mapResponse(res, false);
	}

	
	// gpt outputs its format in a predictable way, so we parse it. also write to the file here.
	void mapResponse(ChatMessage response, boolean fromFile) {
		int count = 0;
		String msg_string = response.getContent();
		
		// write to file here
		
		boolean insideName = true;
		String curName = "";
		String curMsg = "";
		for (int i = 0; i < msg_string.length(); i++) {
			if (insideName) {
				// skip ';'
				if(msg_string.charAt(i) == ';')
					continue;
				// read name
				curName += msg_string.charAt(i);
				
				if (msg_string.charAt(i) == ':') {
					// finished reading, prepare name for map
					insideName = false;
					curName = curName.trim();
					curName = curName.replace(":", "");
					curName = curName.replace("\n", "");
					
					// insert into map if does not exist
					if (!userData.containsKey(curName))
						userData.put(curName, new ArrayList<String>());
					
					// write to file (unless thats read from the file to avoid double writing). formatting of initial ai response should be preserved.
					if(!fromFile) writeToFile(";"+curName+":");
				}
			} else {
				// read message
				curMsg += msg_string.charAt(i);
				
				if (msg_string.charAt(i) == ';' || i == msg_string.length() - 1) {
					// end reading, prepare message for map as an arrayList
					ArrayList<String> curVal = userData.get(curName);
					while (curVal.size() >= max_values_size) // removes old values from the record, ge used to accomodate with files.
						curVal.remove(0);
					curMsg = curMsg.replace(";", "");
					curMsg = curMsg.replace("\n","");
					curVal.add(curMsg);
					userData.put(curName, curVal);
					if(!fromFile) writeToFile(curMsg);
					insideName = true;
					curName = "";
					curMsg = "";
				}
			}
		}
	}
	
	
	// returns a pretty map
	public String getMapString(){
		
		if(userData.isEmpty())
			return " ";
		String msg = "";
		for(String key : userData.keySet())
		{
			msg += "__**"+key+"**__" + " :: \n";
			for(String val : userData.get(key))
				msg += "* " +val + "\n";
			msg += "\n__***END_RECORD***__\n \n";
		}
		System.out.println(msg);
		return msg;
	}
	
	// returns a pretty map for a single user
	public String getMapString(String user) {
		if(userData.isEmpty() || !userData.containsKey(user) || userData.get(user).isEmpty())
			return " ";
		String msg = "";
		msg += "__**"+user+"**__" + " :: \n";
		for(String val : userData.get(user))
			msg += "* " +val + "\n";
		msg += "\n__***END_RECORD***__\n \n";
		return msg;
	}
	
	public String getUsers() {
		if(userData.isEmpty())
			return " ";
		String msg = "";
		for(String key : userData.keySet())
			msg += "  " + key + "  ";
		return msg;
	}
	
	public void writeToFile(String analysis) {
		try {
			fw = new FileWriter(file, true);
			out = new PrintWriter(fw);
			out.println(analysis);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
