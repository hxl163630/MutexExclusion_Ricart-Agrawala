This program implement the Ricart-Agrawala algorithm for distributed mutual exclusion with the optimization proposed by Roucairol and Carvalho, in a client-server model.

How to run this program:

1. Put the two (Mutex_Client.jar, Mutex_Server.jar) files and four configuration TXT files (clients.txt, servers.txt, files.txt, config.txt) in the same directory
2. Connect to 8 UTD lab machines. dc26 - dc28 for servers, dc21 - dc25 for clients.
3. Run servers and clients:
	-In dc26, run this line from configure: java -jar Mutex_Server.jar Server-01 servers.txt files.txt
	-In dc27, run this line from configure: java -jar Mutex_Server.jar Server-02 servers.txt files.txt
	-In dc28, run this line from configure: java -jar Mutex_Server.jar Server-03 servers.txt files.txt
	-In dc21, run this line from configure: java -jar Mutex_Client.jar Client-01 servers.txt clients.txt
	-In dc22, run this line from configure: java -jar Mutex_Client.jar Client-02 servers.txt clients.txt
	-In dc23, run this line from configure: java -jar Mutex_Client.jar Client-03 servers.txt clients.txt
	-In dc24, run this line from configure: java -jar Mutex_Client.jar Client-04 servers.txt clients.txt
	-In dc25, run this line from configure: java -jar Mutex_Client.jar Client-05 servers.txt clients.txt
4. In the five clients progrem, press 's' to start the program.
