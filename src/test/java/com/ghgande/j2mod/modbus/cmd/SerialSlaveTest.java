/*
 * This file is part of j2mod-steve.
 *
 * j2mod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * j2mod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses
 */
package com.ghgande.j2mod.modbus.cmd;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.net.ModbusSerialListener;
import com.ghgande.j2mod.modbus.procimg.*;
import com.ghgande.j2mod.modbus.util.Logger;
import com.ghgande.j2mod.modbus.util.SerialParameters;

/**
 * Class implementing a simple Modbus slave. A simple process image is available
 * to test functionality and behavior of the implementation.
 *
 * @author Dieter Wimberger
 * @author Julie Haugh
 *         Added ability to specify the number of coils, discreates, input and
 *         holding registers.
 * @version 1.2rc1 (09/11/2004)
 */
public class SerialSlaveTest {

    private static final Logger logger = Logger.getLogger(SerialSlaveTest.class);

    public static void main(String[] args) {

        ModbusSerialListener listener;
        SimpleProcessImage spi;
        String portname = null;
        boolean hasUnit = false;
        int unit = 2;
        int coils = 2;
        int discretes = 4;
        boolean hasInputs = false;
        int inputs = 1;
        boolean hasHoldings = false;
        int holdings = 1;
        int arg;

        for (arg = 0; arg < args.length; arg++) {
            if (args[arg].equals("--port") || args[arg].equals("-p")) {
                portname = args[++arg];
            }
            else if (args[arg].equals("--unit") || args[arg].equals("-u")) {
                unit = Integer.parseInt(args[++arg]);
                hasUnit = true;
            }
            else if (args[arg].equals("--coils") || args[arg].equals("-c")) {
                coils = Integer.parseInt(args[++arg]);
            }
            else if (args[arg].equals("--discretes") || args[arg].equals("-d")) {
                discretes = Integer.parseInt(args[++arg]);
            }
            else if (args[arg].equals("--inputs") || args[arg].equals("-i")) {
                inputs = Integer.parseInt(args[++arg]);
                hasInputs = true;
            }
            else if (args[arg].equals("--holdings") || args[arg].equals("-h")) {
                holdings = Integer.parseInt(args[++arg]);
                hasHoldings = true;
            }
            else {
                break;
            }
        }

        if (arg < args.length && portname == null) {
            portname = args[arg++];
        }

        if (arg < args.length && !hasUnit) {
            unit = Integer.parseInt(args[arg++]);
        }

        if (Modbus.debug) {
            logger.debug("j2mod ModbusSerial Slave");
        }

        try {

			/*
             * Prepare a process image.
			 * 
			 * The file records from the TCP and UDP test harnesses are
			 * not included.  They can be added if there is a need to
			 * test READ FILE RECORD and WRITE FILE RECORD with a Modbus/RTU
			 * device.
			 */
            spi = new SimpleProcessImage();

            for (int i = 0; i < coils; i++) {
                spi.addDigitalOut(new SimpleDigitalOut(i % 2 == 0));
            }

            for (int i = 0; i < discretes; i++) {
                spi.addDigitalIn(new SimpleDigitalIn(i % 2 == 0));
            }

            if (hasHoldings) {
                logger.debug("Adding " + holdings + " holding registers");

                for (int i = 0; i < holdings; i++) {
                    spi.addRegister(new SimpleRegister(i));
                }
            }
            else {
                spi.addRegister(new SimpleRegister(251));
            }

            if (hasInputs) {
                logger.debug("Adding " + inputs + " input registers");

                for (int i = 0; i < inputs; i++) {
                    spi.addInputRegister(new SimpleInputRegister(i));
                }
            }
            else {
                spi.addInputRegister(new SimpleInputRegister(45));
            }

            // 2. Create the coupler and set the slave identity
            ModbusCoupler.getReference().setProcessImage(spi);
            ModbusCoupler.getReference().setMaster(false);
            ModbusCoupler.getReference().setUnitID(unit);

            // 3. Set up serial parameters
            SerialParameters params = new SerialParameters();

            params.setPortName(portname);
            params.setBaudRate(19200);
            params.setDatabits(8);
            params.setParity("None");
            params.setStopbits(1);
            params.setEncoding("rtu");
            params.setEcho(false);
            if (Modbus.debug) {
                logger.debug("Encoding [" + params.getEncoding() + "]");
            }

            // 4. Set up serial listener
            listener = new ModbusSerialListener(params);
            listener.setListening(true);

            // 5. Start the listener thread.
            new Thread(listener).start();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}