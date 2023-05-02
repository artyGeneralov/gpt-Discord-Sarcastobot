package DiscordSarcastobot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

public class ProfilerAgent extends GPTAgent {

	private Map<String, ArrayList<String>> userData = new HashMap<>();
	private final int max_values_size = 10;

	void analyzeUsers(List<ChatMessage> context) {
		/* Assumption: context is only user messages in the format {NAME}:{msg} */
		List<ChatMessage> messages = new ArrayList<>();
		String sys_string = " You are a personality analyzing robot. You will be given a chat conversation with different users"
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
			ChatMessage curmsg = new ChatMessage(ChatMessageRole.USER.value(), msg.getContent().toString());
			messages.add(curmsg);
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
		mapResponse(res);
	}

	void mapResponse(ChatMessage response) {
		int count = 0;
		String msg_string = response.getContent().toString();
		boolean insideName = true;
		String curName = "";
		String curMsg = "";
		for (int i = 0; i < msg_string.length(); i++) {
			if (insideName) {
				if(msg_string.charAt(i) == ';')
					continue;
				curName += msg_string.charAt(i);
				if (msg_string.charAt(i) == ':') {
					insideName = false;
					// set name in map
					curName = curName.trim();
					curName = curName.replace(":", "");
					if (!userData.containsKey(curName))
						userData.put(curName, new ArrayList<String>());
				}
			} else {
				curMsg += msg_string.charAt(i);
				
				if (msg_string.charAt(i) == ';' || i == msg_string.length() - 1) {
					ArrayList<String> curVal = userData.get(curName);
					if (curVal.size() >= max_values_size)
						curVal.remove(0);
					curMsg = curMsg.replace(";", "");
					curMsg = curMsg.replace("\n","");
					curVal.add(curMsg);
					userData.put(curName, curVal);
					insideName = true;
					curName = "";
					curMsg = "";
					
				}
			}
		}
	}
	
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
		return msg;
	}

}
