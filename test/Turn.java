import com.cubethree.GPIOLib.*;

public class Turn {
	public static void main( String[] args ){
	try{
		Servo s = GPIOLib.allocateServo( 17 );
		for( double i = -90; i < 90; i += 5 ){
			s.setAngle( i );
			Thread.sleep( 100 );
		}
	}catch(Exception e){
		e.printStackTrace( System.err );
	}finally{
		GPIOLib.cleanup();
	}
	}
}
