package com.spm.arduino;

import java.util.Scanner;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class SerialArduino{

	static SerialPort serialPort;

	public static void main(String[] args) {
		
		serialPort = new SerialPort("COM3");
		
		try {
			
			serialPort.openPort();// Open port
			
			serialPort.setParams(
					SerialPort.BAUDRATE_38400, 
					SerialPort.DATABITS_8, 
					SerialPort.STOPBITS_1, 
					SerialPort.PARITY_NONE);// Set params
			
			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;// Prepare mask
		
			serialPort.setEventsMask(mask);// Set mask
			
			SerialPortReader spr = new SerialPortReader();

			serialPort.addEventListener(spr);// Add SerialPortEventListener
			
			try {

				System.out.println("Waiting for Arduino to establish comms...");
				//Thread.sleep(2000);
				serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
				System.out.println("Arduino ready for input...");

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String led = null;
			
			Scanner ledScanner = new Scanner(System.in);

			while(!"3".equals(led)){
				led = ledScanner.next();				
				serialPort.writeBytes(led.getBytes());
			}
			
			ledScanner.close();
			
			serialPort.removeEventListener();
			System.out.println("Comms with Arduino closed...");
			
			if(serialPort != null && serialPort.isOpened()){
				serialPort.closePort();
			}

		} catch (SerialPortException ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * In this class must implement the method serialEvent, through it we learn
	 * about events that happened to our port. But we will not report on all
	 * events but only those that we put in the mask. In this case the arrival
	 * of the data and change the status lines CTS and DSR
	 */
	static class SerialPortReader implements SerialPortEventListener {

		public void serialEvent(SerialPortEvent event) {
			if (event.isRXCHAR()) {// If data is available
				try {
					byte buffer[] = serialPort.readBytes(14);
					serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
					System.out.println(new String(buffer));
				} catch (SerialPortException ex) {
					ex.printStackTrace();
				}
			} else if (event.isCTS()) {// If CTS line has changed state
				if (event.getEventValue() == 1) {// If line is ON
					System.out.println("CTS - ON");
				} else {
					System.out.println("CTS - OFF");
				}
			} else if (event.isDSR()) {/// If DSR line has changed state
				if (event.getEventValue() == 1) {// If line is ON
					System.out.println("DSR - ON");
				} else {
					System.out.println("DSR - OFF");
				}
			}
		}
	}

}
