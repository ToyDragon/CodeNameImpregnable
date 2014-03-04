package com.wwiftam.bluetoothtesting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.UUID;

import com.example.android.BluetoothChat.BluetoothChatService;
import com.example.android.BluetoothChat.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BluetoothModule {

	private static final UUID UUID_SECURE = UUID.fromString("06062eb0-a34b-11e3-a5e2-0800200c9a66");
	private static final UUID UUID_INSECURE = UUID.fromString("09e3de10-a34b-11e3-a5e2-0800200c9a66");
	private static final String NAME_INSECURE = "BModuleInsecure";
	private static final String NAME_SECURE = "BModuleSecure";
	private static final String TAG = "BluetoothModule";
	
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    private final Handler handler = new Handler(){
    	public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                //if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
	                case BluetoothModule.STATE_CONNECTED:
	                    //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
	                    mConversationArrayAdapter.clear();
	                    break;
	                case BluetoothModule.STATE_CONNECTING:
	                    setStatus(R.string.title_connecting);
	                    break;
	                case BluetoothModule.STATE_LISTEN:
	                case BluetoothModule.STATE_NONE:
	                    setStatus(R.string.title_not_connected);
	                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
	
	LinkedList<BluetoothDevice> bluetooth_devices = new LinkedList<BluetoothDevice>();
	BluetoothAdapter bt_adapter;
	private AcceptThread secure_accept_thread;
	private AcceptThread insecure_accept_thread;
	private ConnectThread connect_thread;
	private ConnectedThread connected_thread;
	private int mState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;	   // we're doing nothing
	public static final int STATE_LISTEN = 1;	 // now listening for incoming connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
	public static final int STATE_CONNECTED = 3;  // now connected to a remote device
	
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
	
    public BluetoothModule(){
    	BluetoothAdapter bt_adapter = BluetoothAdapter.getDefaultAdapter();
    	for(BluetoothDevice device : bt_adapter.getBondedDevices()){
    		bluetooth_devices.add(device);
    	}
    
    }
	
	public void listenForDevices(){
		// Cancel any thread attempting to make a connection
        if (connect_thread != null) {connect_thread.cancel(); connect_thread = null;}

        // Cancel any thread currently running a connection
        if (connected_thread != null) {connected_thread.cancel(); connected_thread = null;}

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (secure_accept_thread == null) {
        	secure_accept_thread = new AcceptThread(true);
        	secure_accept_thread.start();
        }
        if (insecure_accept_thread == null) {
        	insecure_accept_thread = new AcceptThread(false);
        	insecure_accept_thread.start();
        }
	}
	
	private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, "Unable to connect device");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // Start the service over to restart listening mode
        listenForDevices();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, "Device connection was lost");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // Start the service over to restart listening mode
        listenForDevices();
    }
	
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket, String socketType) {
			Log.d(TAG, "create ConnectedThread: " + socketType);
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);

					// Send the obtained bytes to the UI Activity
					//handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
					//		.sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					// Start the service over to restart listening mode
					BluetoothModule.this.listenForDevices();
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * @param buffer  The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);

				// Share the sent message back to the UI Activity
				//handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
				//		.sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
	
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType) {
		//if (D) Log.d(TAG, "connected, Socket Type:" + socketType);
		
		// Cancel the thread that completed the connection
		if (connect_thread != null) {connect_thread.cancel(); connect_thread = null;}
		
		// Cancel any thread currently running a connection
		if (connected_thread != null) {connected_thread.cancel(); connected_thread = null;}
		
		// Cancel the accept thread because we only want to connect to one device
		if (secure_accept_thread != null) {
			secure_accept_thread.cancel();
			secure_accept_thread = null;
		}
		if (insecure_accept_thread != null) {
			insecure_accept_thread.cancel();
			insecure_accept_thread = null;
		}
		
		// Start the thread to manage the connection and perform transmissions
		connected_thread = new ConnectedThread(socket, socketType);
		connected_thread.start();
		
		// Send the name of the connected device back to the UI Activity
		Message msg = handler.obtainMessage(MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(DEVICE_NAME, device.getName());
		msg.setData(bundle);
		handler.sendMessage(msg);
		
		setState(STATE_CONNECTED);
	}
	 
	 private void setState(int stateConnected) {
		// TODO Auto-generated method stub
		
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private String mSocketType;

		public ConnectThread(BluetoothDevice device, boolean secure) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				if (secure) {
					tmp = device.createRfcommSocketToServiceRecord(UUID_SECURE);
				} else {
					tmp = device.createInsecureRfcommSocketToServiceRecord(UUID_INSECURE);
				}
			} catch (IOException e) {
				Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
			setName("ConnectThread" + mSocketType);

			// Always cancel discovery because it will slow down a connection
			bt_adapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() " + mSocketType +
							" socket during connection failure", e2);
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothModule.this) {
				connect_thread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice, mSocketType);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
			}
		}
	}
	
	private class AcceptThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket mmServerSocket;
		private String mSocketType;

		public AcceptThread(boolean secure) {
			BluetoothServerSocket tmp = null;
			mSocketType = secure ? "Secure":"Insecure";

			// Create a new listening server socket
			try {
				if (secure) {
					tmp = bt_adapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,UUID_SECURE);
				} else {
					tmp = bt_adapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, UUID_INSECURE);
				}
			} catch (IOException e) {
				Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
			}
			mmServerSocket = tmp;
		}

		public void run() {
			//if (D) Log.d(TAG, "Socket Type: " + mSocketType +
			//		"BEGIN mAcceptThread" + this);
			setName("AcceptThread" + mSocketType);

			BluetoothSocket socket = null;

			// Listen to the server socket if we're not connected
			while (mState != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
					break;
				}

				// If a connection was accepted
				if (socket != null) {
					synchronized (BluetoothModule.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Situation normal. Start the connected thread.
							connected(socket, socket.getRemoteDevice(), mSocketType);
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate new socket.
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
			//if (D) Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

		}

		public void cancel() {
			//if (D) Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
			}
		}
	}
}
