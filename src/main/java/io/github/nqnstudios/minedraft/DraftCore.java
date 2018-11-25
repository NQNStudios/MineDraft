package io.github.nqnstudios.minedraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class DraftCore {
    public static void main(String[] args) {
        // Create a DraftCore and debug it in a repl
        DraftCore core = new DraftCore();

        if (args.length > 0) {
            core.openFile(DraftCore.joinTokens(args, 0, true));
            core.dumpOutput();
        }

        Scanner sc = new Scanner(System.in);

        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            core.processLine(line);
            core.dumpOutput();
        }

        sc.close();
    }

    public void processLine(String line) {
            if (line.contains(" ") ) {
                String command = line.substring(0, line.indexOf(" "));
                String message = line.substring(line.indexOf(" ")+1);
                process(command, message);
            } else if (line.length() > 0) {
                process(line, "");
            }

    }

    public void clear() {
        filename = "";
        lines.clear();
    }

    public static String joinTokens(String[] tokens, int start, boolean toPath) {
        String joint = "";
        for (int i = start; i < tokens.length; ++i) {
            String token = tokens[i];
            if (toPath)
                token = token.replace("/", File.separator);
            joint += token;
            if (i < tokens.length - 1) {
                joint += " ";
            }
        }
        return joint;
    }

    public void openFile(String path) {
        String fullPath = homedir;
        fullPath += File.separator;
        fullPath += path;

        try (BufferedReader br = new BufferedReader(new FileReader(fullPath))) {
            String line;
            lines.clear();
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            filename = fullPath;
            selectLine(1);
        } catch (Exception e) {
            // If the file doesn't exist, try to open it for writing

            filename = fullPath;
            if (!writeToFile()) {
                filename = "";
                output = "Attempting to create new file " + filename + "failed: " + output;
            }
        }

    }

    public boolean writeToFile() {
        try {
            File fout = new File(filename);
            FileOutputStream fos = new FileOutputStream(fout);
    
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
    
            for (int i = 0; i < lines.size(); i++) {
                bw.write(lines.get(i));
                bw.newLine();
            }
    
            bw.close();
            return true;
        } catch (Exception e) {
            output = "Save to " + filename + " failed: " + e.getClass().getName() + e.getMessage();
            return false;
        }
    }

    public void process(String command, String message) {
        switch (command) {
            case "open":
                if (message.length() > 0) {
                    openFile(message);
                }
                else {
                    output = "Failed: you must specify a filename to open.";
                }
                break;
            case "close":
                clear();
                break;

                // Everything that's not "open" or "close" is an editor command
            default:
                if (filename.length() == 0) {
                    output = "Failed: Can't edit without a file open!";
                }
                else {
                    editorProcess(command, message);
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

    private void editorProcess(String command, String message) {
        boolean printTheLine = true;
        switch (command) {
            // Allow up/down [n]
            case "up":
            case "down":
            case "goto":
                int num = 1;
                if (message.length() > 0) {
                    num = Integer.parseInt(message);
                }
                if (command.equals("up")) {
                    up(num);
                } else if (command.equals("down")) {
                    down(num);
                } else {
                    selectLine(num);
                }
                break;
            // Allow vim-style substitution in the limited format s foo/bar
            case "s":
                String[] two = message.split("/");
                // TODO escape sequences should probably be possible here
                if (two.length != 2) {
                    output = "You didn't provide 2 strings (one to find/one to replace with) ";
                }
                else {
                    lines.set(index, lines.get(index).replace(two[0], two[1]));
                }
                break;
            // Allow full-scale line replacement
            case "S":
                    lines.set(index, message);
                break;
            // Allow i/insertion at start of line
            case "i":
            case "I":
                lines.set(index, message + " " + lines.get(index));
                break;
            // Allow a/append at end of line
            case "a":
            case "A":
                lines.set(index, lines.get(index) + " " + message);
                break;
            // Allow O to insert line above this one
            case "O":
                lines.add(index, message);
                break;
            // Allow o to insert line after this one
            case "o":
                int indexToAdd = Math.min(lines.size(), index+1);
                lines.add(indexToAdd, message);
                selectLine(indexToAdd+1);
                break;
            
            // Allow line deletion
            case "d":
                lines.remove(index);
                break;

                // TODO allow line joining (with a number of lines to join? (cap it as lines.size()))

            default: 
                output = "Not a command: " + command;
                printTheLine = false;
                break;
        }

        if (printTheLine) {
            //Print the current line now
            selectLine(index+1);
        }
        // After all editor commands, save
        writeToFile();
    }

    String homedir = System.getProperty("user.home");
    String filename = "";

    // Store a list (ArrayList) of lines in the file
    ArrayList<String> lines = new ArrayList<String>();
    // Keep a line number index to the open file
    int index = 0;

    // TODO command to loop through lines on a delay, printing them to chat, until user types stop



}