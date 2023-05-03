# gpt-Discord-Sarcastobot

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
