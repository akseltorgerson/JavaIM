import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

public class Client extends JFrame {

    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    private String serverIP;
    private Socket connection;

    // constructor
    public Client(String host) {
        super("Client");
        serverIP = host;
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
            new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    sendMessage(e.getActionCommand());
                    userText.setText("");
                }
            }
        );
        add(userText, BorderLayout.SOUTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        chatWindow.setEditable(false);
        setSize(300, 150);
        setVisible(true);
    }

    // connect to server
    public void start() {
        try {
            connectToServer();
            setupStreams();
            whileChatting();
        } catch (EOFException e) {
            showMessage("\nClient terminated connection");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    // connecting to a server with specified serverIP
    private void connectToServer() throws IOException {
        showMessage("Waiting to connect with server... \n");
        connection = new Socket(InetAddress.getByName(serverIP), 8080);
        showMessage("Connected to: " + connection.getInetAddress().getHostName());
    }

    // setting up the streams to send and receive data
    private void setupStreams() throws IOException {
        // setting up the output stream
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush(); // good house keeping

        // setting up the input stream
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\nCommunication streams now established! \n");
    }

    // while the chat is going on
    private void whileChatting() throws IOException {
        ableToType(true);
        do {
            try {
                message = (String) input.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException e) {
                showMessage("\nERROR: UNKNOWN MESSAGE FROM SERVER! ");
            }
        } while (!message.equals("SERVER - END"));
    }

    // sends the message to the server
    private void sendMessage(String message) {
        try {
            output.writeObject("CLIENT - " + message);
            output.flush();
            showMessage("\nCLIENT - " + message);
        } catch (IOException e) {
            chatWindow.append("\nERROR: CANNOT SEND MESSAGE! ");
        }
    }

    // updates the chatWindow
    private void showMessage(final String text) {
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override public void run() {
                    chatWindow.append(text);
                }
            }
        );
    }

    // close streams and sockets after done chatting
    private void stop() {
        showMessage("\nClosing connections... \n");
        ableToType(false);
        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showMessage("\nConnections terminated!\n");
    }

    // sets the text field to be editable or not
    private void ableToType(final boolean flag) {
        SwingUtilities.invokeLater(
            new Runnable() {
                @Override public void run() {
                    userText.setEditable(flag);
                }
            }
        );
    }

}
