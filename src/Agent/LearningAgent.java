package Agent;

import adx.variants.twodaysgame.TwoDaysBidBundle;
import adx.variants.twodaysgame.TwoDaysTwoCampaignsAgent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class LearningAgent extends TwoDaysTwoCampaignsAgent {

  private static final int PORT = 7627;

  public static void main(String[] args) {

    try (ServerSocket server = new ServerSocket(PORT)) {
      System.out.printf("[+] TCPServer started on port %d...\n", PORT);

      Socket ss = server.accept();
      PrintWriter pw = new PrintWriter(ss.getOutputStream(), true);
      BufferedReader brIn = new BufferedReader(new InputStreamReader(System.in));
      BufferedReader brSocket = new BufferedReader(new InputStreamReader(ss.getInputStream()));
      System.out.printf("[+] Client %s connected...\n", ss.toString());

      while (true) {
        System.out.println("[*] Enter command:");
        pw.println(brIn.readLine());
        System.out.printf("[+] Client responded \"%s\"\n", brSocket.readLine());
      }
    } catch (Exception e) {
      System.err.printf("[-] Error: %s\n", e.getMessage());
    }
  }

  public LearningAgent(String host, int port) {
    super(host, port);
  }

  @Override
  protected TwoDaysBidBundle getBidBundle(int i) {
    return null;
  }
}
