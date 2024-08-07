﻿# gpt-Discord-Sarcastobot

### Description:

This is a bot written using the (back then) new OpenAI GPT API.<br>
The idea was to provide a way for multiple people to converce with the AI simultaniously.<br>
This was achieved by providing an elaborate prompt that would describe the chatting environment to the bot, and
by adding the previous few messages in a predefined location in each prompt.<br><br>
To improve the immersion, a second agent was deployed and tasked with writing some kind of analysis of the communicating parties.<br>
This analysis is saved to a local file and for each individual that engages in conversation - the previous analysis is also passed as part of the prompt.<br>
<br>
This allowed conversation such as:<br>
```markdown
            Person_A: "Bot, please write a poem about @Person_B"
```

Due to the fact that Person_B was mentioned in the prompt, his analysis info would be passed to the bot and the bot would write the poem according to his "knowledge" of Person_B.<br><br>

```markdown
            Disclaimer: The "analysis" performed by the bot is based upon the users conversations, it never reflected any real
            charechtaristics of any user.
            Also, all users have been informed of the feature and each user could access his own assessment in a public channel.
```

The Sarcastic nature of the bot was made so that the users interaction would be enjoyable.<br><br>
It was.<br>

## Screenshots
### Multiplayer-chat with sarcastobot
![Chat](./assets/scr1.png)

### User analysis on different users
![Analysis](./assets/scr2.png)


### Change Log:
<h2><u><b> 01/05/2023:</b></u></h2>  
            <p>This is a rudementary discord bot that recieves user prompts, sends them to gpt-3.5-turbo model and responds with
            sarcastic answers and also makes fun of the users discord name.</p>



<h2><u><b> 02/05/2023:</b></u></h2> 
            <p>I've added a second agent that is incharge of profiling the users during the conversation<br>
            The profile is then sent to the main chatbot, thus giving him more profound knowledge of the people in the 
            conversation.</p>
            
<h2><u><b> 03/05/2023:</b></u></h2> 
            <p>
            profiler agent now only gets information about the user that called it.<br>
            for the other users - he only knows their names.
            This was due to two reasons:<br>
            <b>1.)</b> As the amount of users grew, the analysis started to take up most of the context window for gpt.<br>
            <b>2.)</b> The bot started to get confused about who he was talking to.<br><br>
            Also made the bot respond to messages instead of /chat, this seems more fun.
            </p>
            
<h2><u><b> 10/05/2023:</b></u></h2> 
            <p>
            A whole bunch of changes during the past week: <br>
            - made sarcastobot smarter by adding some regex to search for different strings<br>
              and change prompts accordingly.<br>
            - Added functionality to mention a user in your own message<br>
              and having sarcastobot startup with their analysis data as well. pretty fun thus far.
            <br><br>
            <i>Tried</i> to do an experiment with two chatbots chatting to each other, but am struggling to make this interesting...
            </p>
