package com.cubethree.GPIOLib;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

public class GPIOPin extends GPIO {
	private static FileOutputStream exportFile = null, unexportFile = null;
	private FileOutputStream directionFile = null, valueFile = null;
	private GPIOPinDirection direction;

	public GPIOPin(){
		exported = false;
		exportedPin = -1;
	}

	protected void export( int pin ) throws GPIOException{
		//Don't export a pin if we've already exported one
		if( exported ){ throw new ObjectAlreadyExportedException( pin, exportedPin ); }

		//Make sure the exportFile is open
		if( exportFile == null ){
			try{
				exportFile = new FileOutputStream( new File("/sys/class/gpio/export") );
			}catch( Exception e ){
				throw new PinWriteException( pin );
			}
		}

		//Export the pin
		try{
			exportFile.write( ("" + pin).getBytes() );
		}catch( Exception e ){
			throw new PinWriteException( pin );
		}

		exported = true;
		exportedPin = pin;

		//Try to open the direction and value files
		try{
			directionFile = new FileOutputStream( new File("/sys/class/gpio/gpio" + pin + "/direction") );
			valueFile = new FileOutputStream( new File("/sys/class/gpio/gpio" + pin + "/value" ) );
		}catch( Exception e ){
			if( directionFile != null ){
				try{ directionFile.close(); }catch( Exception e2 ){;}
			}

			try{ unexport(); }catch( Exception e2 ){;}
			throw new PinWriteException( pin );
		}

		//Set the direction to the least damaging direction
		this.setDirection( GPIOPinDirection.OUT );
	}

	protected void unexport() throws GPIOException{
		//Make sure that the pin is actually exported before we unexport it
		if( !exported ){ return; }

		//Make sure the unexportFile is open
		if( unexportFile == null ){
			try{
				unexportFile = new FileOutputStream( new File("/sys/class/gpio/unexport") );
			}catch( Exception e ){
				throw new PinWriteException( exportedPin );
			}
		}

		//Unexport the pin
		try{
			unexportFile.write( ("" + exportedPin).getBytes() );
		}catch( Exception e ){
			throw new PinWriteException( exportedPin );
		}

		exported = false;
		exportedPin = -1;
	}

	protected void cleanup(){
		try{ unexport(); }catch( Exception e ){;}
		try{ directionFile.close(); }catch( Exception e ){;}
		try{ valueFile.close(); }catch( Exception e ){;}
	}

	protected static void staticCleanup(){
		try{ exportFile.close(); }catch( Exception e ){;}
		try{ unexportFile.close(); }catch( Exception e ){;}
	}

	public void setDirection( GPIOPinDirection dir ){
		//Make sure that the pin is exported before we try to change its direction
		if( !exported ){ return; }

		//Write to change the direction
		//If the direction file isn't already open then we have bigger problems than we can solve here
		try{
			if( dir == GPIOPinDirection.IN ){ directionFile.write( "in".getBytes() ); }
			else{ directionFile.write( "out".getBytes() ); }
		}catch( Exception e ){ return; }

		direction = dir;
	}

	public GPIOPinDirection getDirection(){ return direction; }

	public void setValue( boolean value ){
		//Make sure that the pin is exported before we try to change its direction
		//Also make sure that it's an output pin
		if( !exported || direction != GPIOPinDirection.OUT ){ return; }

		//Write to change the value
		//If the file's not open then we have bigger problems than we can solve here
		try{
			if( value ){ valueFile.write( "1".getBytes() ); }
			else{ valueFile.write( "0".getBytes() ); }
		}catch( Exception e ){;}
	}

	public boolean getValue(){
		//TODO Implement this
		return true;
	}
}
