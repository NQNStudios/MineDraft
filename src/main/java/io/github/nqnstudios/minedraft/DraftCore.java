//package io.github.nqnstudios.minedraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class DraftCore {
    public static void main(String[] args) {
        // Create a DraftCore and debug it in a repl
        DraftCore core = new DraftCore();

        Scanner sc = new Scanner(System.in);

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            core.process(line);
            core.dumpOutput();
        }
    }


    public void clear() {
        filename = "";
        lines.clear();
    }

    public void process(String message) {
        String[] tokens = message.split(" ");
        
        switch (tokens[0]) {
            case "open":
                if (tokens.length >= 2) {
                    // Assume the file path might be all of the remaining tokens, including spaces
                    String path = homedir;
                    path += File.separator;
                    for (int i = 1; i < tokens.length; ++i) {
                        String token = tokens[i];
                        token = token.replace("/", File.separator);
                        path += token;
                        if (i < tokens.length - 1) {
                            path += " ";
                        }
                    }

                    System.out.println(path);

                    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                        String line;
                        lines.clear();
                        while ((line = br.readLine()) != null) {
                            lines.add(line);
                        }
                        filename = path;
                        selectLine(1);
                    } catch (Exception e) {
                        System.out.println("Failed: " + e.getClass().getName() + e.getMessage());
                    }
                }
                else {
                    // TODO trying to open without a filename!!
                }
                break;
            case "close":
                clear();
                break;

            default:
                if (filename.length() == 0) {
                    // TODO trying to edit without a file open! Bad!
                }
                else {
                    // TODO run the editor thing
                    editorProcess(tokens);
                }

        }
    }

    private void selectLine( int line) {
        if (line > 0 && line <= lines.size()) {
            index = line - 1;
            output = lines.get(index);
        }
    }

    private void up(int amount) {
        int newIndex = index-amount;
        if (newIndex < 0) 
            newIndex = lines.size() + newIndex;
        selectLine(newIndex+1);
    }
    private void down(int amount) {
        selectLine((index+amount)%lines.size()+1);
    }

    public void tick() {

    }

    private String output = "";
    public String takeOutput() {
        String temp = output;
        output = "";
        return temp;
    }
    public void dumpOutput() {
        if (output.length() > 0)
            System.out.println(output);
        output = "";
    }

    private void editorProcess(String[] tokens) {
        
    }

    String homedir = System.getProperty("user.home");
    String filename = "";

    // Store a list (ArrayList) of lines in the file
    ArrayList<String> lines = new ArrayList<String>();
    int index = 0;
    // Keep a line number index to the open file
    // Allow up/down [n]

    // loop through lines on a delay, printing them to chat, until user types stop

    // Allow s/foo/bar substitution
    // Allow i/insertion at start of line
    // Allow a/append at end of line
    // Allow O/ and o/ insertion
    // Allow deletion

    // After all commands, save

}