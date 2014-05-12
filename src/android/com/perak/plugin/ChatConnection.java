package com.perak.plugin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ChatConnection {

    private Handler mHandler;
    private ChatServer mChatServer;
    private ChatClient mChatClient;

    private static final String TAG = "ChatConnection";

    private Socket mSocket;
    private int mPort = -1;

    public ChatConnection(Handler handler) {
        mHandler = handler;
        mChatServer = new ChatServer(handler);
    }

    public void tearDown() {
        mChatServer.tearDown();
        mChatClient.tearDown();
    }

    public void connectToServer(InetAddress address, int port) {
        mChatClient = new ChatClient(address, port);
    }

    public void sendMessage(String msg) {
        if (mChatClient != null) {
            mChatClient.sendMessage(msg);
        }
    }
    
    public int getLocalPort() {
        return mPort;
    }
    
    public void setLocalPort(int port) {
        mPort = port;
    }
    
    public void sendNotification(String type, String msg) {
        Bundle messageBundle = new Bundle();
        messageBundle.putString("type", type);
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mHandler.sendMessage(message);
    }

    public synchronized void updateMessages(String msg, boolean local) {
		sendNotification("log", "Updating message: " + msg);

        if (local) {
            msg = "me: " + msg;
        } else {
            msg = "them: " + msg;
        }

        sendNotification("message", msg);
    }

    private synchronized void setSocket(Socket socket) {
		sendNotification("log", "setSocket being called.");
        if (socket == null) {
			sendNotification("log", "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
					sendNotification("error", "setSocket Error: " + e);
                }
            }
        }
        mSocket = socket;
    }

    private Socket getSocket() {
        sendNotification("log", "Tear down");
        return mSocket;
    }

    private class ChatServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;

        public ChatServer(Handler handler) {
			sendNotification("log", "ChatServer constructor");
            mThread = new Thread(new ServerThread());
            mThread.start();
        }

        public void tearDown() {
            mThread.interrupt();
            try {
                mServerSocket.close();
            } catch (IOException ioe) {
				sendNotification("error", "Error when closing server socket " + ioe);
            }
        }

        class ServerThread implements Runnable {

            @Override
            public void run() {
				sendNotification("log", "ServerThread run");

                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertise it via Nsd.
                    mServerSocket = new ServerSocket(0);
                    setLocalPort(mServerSocket.getLocalPort());
                    
                    while (!Thread.currentThread().isInterrupted()) {
						sendNotification("log", "ServerSocket Created, awaiting connection");
                        setSocket(mServerSocket.accept());
						sendNotification("log", "Connected.");
                        if (mChatClient == null) {
                            int port = mSocket.getPort();
                            InetAddress address = mSocket.getInetAddress();
                            connectToServer(address, port);
                        }
                    }
                } catch (IOException e) {
					sendNotification("error", "Error creating ServerSocket: " + e);
                }
            }
        }
    }

    private class ChatClient {

        private InetAddress mAddress;
        private int PORT;

        private final String CLIENT_TAG = "ChatClient";

        private Thread mSendThread;
        private Thread mRecThread;

        public ChatClient(InetAddress address, int port) {

            sendNotification("log", "Creating chatClient");
            this.mAddress = address;
            this.PORT = port;

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        class SendingThread implements Runnable {

            BlockingQueue<String> mMessageQueue;
            private int QUEUE_CAPACITY = 10;

            public SendingThread() {
				sendNotification("log", "SendingThread constructor");
                mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            }

            @Override
            public void run() {
				sendNotification("log", "SendingThread run");
                try {
                    if (getSocket() == null) {
                    	sendNotification("log", "Socket is null, creating socket...");
                        setSocket(new Socket(mAddress, PORT));
                        sendNotification("log", "Client-side socket initialized.");
                    } else {
                        sendNotification("log", "Socket already initialized. skipping!");
                    }

                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();

                } catch (UnknownHostException e) {
                    sendNotification("error", "Initializing socket failed, UHE " + e);
                } catch (IOException e) {
                    sendNotification("error", "Initializing socket failed, IOE " + e);
                }

                sendNotification("log", "OK, ready to send messages...");

                while (true) {
                    try {
                        String msg = mMessageQueue.take();
                        sendMessage(msg);
                    } catch (InterruptedException ie) {
                        sendNotification("error", "Message sending loop interrupted, exiting");
                    }
                }
            }
        }

        class ReceivingThread implements Runnable {

            @Override
            public void run() {

                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            mSocket.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {

                        String messageStr = null;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                        	sendNotification("log", "Read from the stream: " + messageStr);
                            updateMessages(messageStr, false);
                        } else {
                        	sendNotification("log", "The nulls! The nulls!");
                            break;
                        }
                    }
                    input.close();

                } catch (IOException e) {
                    sendNotification("error", "Server loop error: " + e);
                }
            }
        }

        public void tearDown() {
            try {
                sendNotification("log", "Tear down");
                getSocket().close();
            } catch (IOException ioe) {
                sendNotification("error", "Error when closing server socket " + ioe);
            }
        }

        public void sendMessage(String msg) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    sendNotification("log", "Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
					sendNotification("log", "Socket output stream is null, wtf?");
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())), true);
                out.println(msg);
                out.flush();
                updateMessages(msg, true);
            } catch (UnknownHostException e) {
                sendNotification("error", "Unknown Host" + e);
            } catch (IOException e) {
				sendNotification("error", "I/O Exception" + e);
            } catch (Exception e) {
                sendNotification("error", "Error3" + e);
            }
            sendNotification("log", "Client sent message: " + msg);
        }
    }
}
