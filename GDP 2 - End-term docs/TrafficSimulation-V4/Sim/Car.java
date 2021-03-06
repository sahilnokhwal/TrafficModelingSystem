import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import time.TimeManager;
import time.Timed;
import Utils.GeoUtils;
import Utils.scale.sc;

public class Car implements Timed {
    
    public final static double timePerTick = 0.25;
    public final static double metersPerPixel = 0.2;
    public final static int carRoutes = 3; //num of paths car knows in advance.
    public static int carsEntered = 0;
    public static int carsExited = 0;
    
    //Components relevant to Car.
    protected CarGenerator carGenerator;
    protected TimeManager ticker;
    protected CarContainer[] carPath;
    
    //Car image variables.
    protected Dimension AREA;
    protected int width, length, halfwidth, halflength;
    public Shape carShape;
    private Color carColor;
    
    //Car screen position variables.
    private int endX, endY;
    protected double distanceToEnd;
    AffineTransform carTransform;
    private float[] x = new float[5];
    private float[] y = new float[5];
    
    //Car speed variables;
    protected double speed;
    protected  int topspeed;
    private double acceleration;
    private double carAngle;
    protected  double[] inFrontInfo;
    private double distanceToNext,speedOfNext;
    protected int iD;
    
    public Car(LaneSection startLane,CarGenerator parent,
						 TimeManager theTick, int topspeed) {
        
        carPath = new CarContainer[carRoutes];
        
        carPath[0] = startLane;
        carGenerator = parent;
        ticker = theTick;
        this.topspeed = topspeed;
        x[0] = carPath[0].startX;
        y[0] = carPath[0].startY;
        AREA = carGenerator.roadNetwork.roadDesigner.getSize();
        
        addCar();
    }
    
    public boolean checkSetUp() {
        
        if (carPath[0].anycollisions(this)) {
            removeCar(true);
            return false;
        } else if ((y[0]>=AREA.height)||
        (x[0]>=AREA.width)||
        (y[0]<0)||(x[0]<0)){
            System.out.println("Initial car position invalid. x,y,w,h= "+
            x[0]+","+y[0]+","+AREA.width+","+AREA.height);
            removeCar(true);
            return false;
        } else {
            endX = carPath[0].endX;
            endY = carPath[0].endY;
            
	    length = 22; halflength = length/2;
	    width = 10; halfwidth = width/2;
            
            speed = topspeed;
            acceleration = 6*metersPerPixel;
            inFrontInfo = new double[2];
            
            distanceToEnd = GeoUtils.getDistance(x[0],y[0],endX,endY);
            carAngle = carPath[0].getRadAngle();
            
            for (int i=1; i < carRoutes; i++) {
                if (carPath[i-1] != null)carPath[i] = carPath[i-1].onToNext();
            }
            
            setToRectangle();
            rotateCar(carAngle);
            setShape();
            
            iD = carsEntered++;
            return true;
        }
    }
    
    public float centerX() { return x[0]; }
    public float centerY() { return y[0]; }
    
    /* Performs all operations to add a car to the system. */
    private void addCar() {
        carPath[0].addCar(this);
        ticker.addTimed(this);
    }
    
    /* Performs all operations to remove a car from the system. */
    private void removeCar(boolean atStart) {
        removefromTimer();
        carPath[0].removeCar(this);
        if (!atStart) carsExited++;
    }
    
    public void removefromTimer() {
        ticker.removeTimed(this);
    }
    
    public void drawCar(Graphics2D g2d,sc scaleManager) {
        g2d.setColor(getSpeedColor(speed));
        //g2d.fill(carShape); this before scaleManager implementation
		scaleManager.fill(carShape,g2d);
        g2d.setColor(Color.white);
		scaleManager.drawString(getSpeedString(),x[0]-5,y[0]+4,g2d);
    }
    
    public String getDistanceToNext() {
        if (distanceToNext==metersToPixels(CarContainer.NOTHING_IN_FRONT)){
            return "?";
        }
        return ""+(int)(distanceToNext);
    }
    
    public String getSpeedOfNext() {
        return ""+(int)(speedOfNext);
    }
    
    public void drawGhostCar(Graphics2D g2d,sc scaleManager) {
        g2d.setColor(Color.gray);
		scaleManager.draw(carShape,g2d);
        //g2d.draw(carShape);
    }
    
    public void pretick() {
        implement_Basic_Car_Following_Model();
    }
    
    public void tick() {}
    
    private void changeToNextLane() {
        carPath[0].removeCar(this);
        
        if (carPath[0].isLastOne()) {
            removeCar(false);
            return;
        }
        
        for (int i=0; i < carRoutes-1; i++) carPath[i] = carPath[i+1];
        
        if (carPath[0] == null) { //if entering junction with no exits.
            ticker.removeTimed(this);
            return;
        }
        
        //work out next future path.
        if (carPath[carRoutes-2] != null)
            carPath[carRoutes-1] = carPath[carRoutes-2].onToNext();
        
        //Re-set car to new current path.
        carPath[0].addCar(this);
        
        x[0] = carPath[0].startX;
        y[0] = carPath[0].startY;
        endX = carPath[0].endX;
        endY = carPath[0].endY;
        
        carAngle = carPath[0].getRadAngle();
        rotateCar(carAngle);
    }
    
	public boolean collide (Car target) {
        return (GeoUtils.getDistance(x[0],y[0],target.x[0],target.y[0]) < 26);
    }
    
    //**********************************************************************
    //************************** Usefull Procedures ************************
    //**********************************************************************
    
    //Colour lookup based on speed value
    private Color getSpeedColor(double speed) {
        
        if (speed <= 0) return (new Color(0,0,0));
        else if (speed <= 3) return (new Color(0,0,0));
        else if (speed <= 6) return (new Color(50,0,0));
        else if (speed <= 9) return (new Color(80,0,0));
        else if (speed <= 12) return (new Color(100,0,0));
        else if (speed <= 15) return (new Color(130,0,0));
        else if (speed <= 18) return (new Color(140,0,0));
        else if (speed <= 21) return (new Color(160,0,0));
        else if (speed <= 24) return (new Color(170,0,0));
        else if (speed <= 27) return (new Color(180,0,0));
        else if (speed <= 30) return (new Color(200,0,0));
        else if (speed <= 33) return (new Color(220,0,0));
        else if (speed <= 36) return (new Color(240,0,0));
        else return (new Color(255,0,0));
    }
    
    public String getSpeedString() {
        return (""+(int)(speed));
    }
    
    //**********************************************************************
    //************************** Car shape Procedures **********************
    //**********************************************************************
    
    private void updateShapeCordinates(float xToChange, float yToChange) {
        //Move to new location
        x[1]+=xToChange; y[1]+=yToChange;
        x[2]+=xToChange; y[2]+=yToChange;
        x[3]+=xToChange; y[3]+=yToChange;
        x[4]+=xToChange; y[4]+=yToChange;
    }
    
    private void setShape() {
        //Given the path that outlines the car, this creates the shape.
        GeneralPath jShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        jShape.moveTo(x[1],y[1]);
        jShape.lineTo(x[2],y[2]);
        jShape.lineTo(x[3],y[3]);
        jShape.lineTo(x[4],y[4]);
        jShape.closePath();
        carShape = jShape;
    }
    
    private void rotateCar(double angle) {
        setToRectangle();
        AffineTransform tx = new AffineTransform();
        tx.setToIdentity();
        tx.translate(x[0],y[0]);
        tx.rotate(2*Math.PI-angle);
        tx.translate(-x[0],-y[0]);
        
        GeneralPath pathShape = collapseToPath();
        pathShape.transform(tx);
        extractFromPath(pathShape);
    }
    
    private void setToRectangle() {
        x[1] = x[0]-halflength; y[1] = y[0]-halfwidth;
        x[2] = x[0]+halflength; y[2] = y[0]-halfwidth;
        x[3] = x[0]+halflength; y[3] = y[0]+halfwidth;
        x[4] = x[0]-halflength; y[4] = y[0]+halfwidth;
    }
    
    private GeneralPath collapseToPath() {
        
        GeneralPath jShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        
        jShape.moveTo(x[1],y[1]);
        for (int i=2;i<5;i++) jShape.lineTo(x[i],y[i]);
        jShape.closePath();
        return jShape;
    }
    
    private void extractFromPath(GeneralPath path) {
        
        float coOrds[] = new float[6];
        int index = 1;
        
        for (PathIterator i=path.getPathIterator(null);!i.isDone();i.next()){
            int segType = i.currentSegment(coOrds);
            if (index <5) {
                x[index]= coOrds[0];
                y[index++] = coOrds[1];
            }
        }
    }
    
    /***********************************************************************/
    /********************** Car movement models ****************************/
    /***********************************************************************/
    
    /**
     * Gets the distance and speed of the car in front (if there is one)
     */
    private double[] getNextOb_SpeedAndDist() {
        
        double[] inFrontInfo = new double[2];
        double distSum = 0;
        
        for (int i=0; i < carRoutes; i++) {
            if (carPath[i] == null) break;
            
            if (i == 0) {
                distSum = distanceToEnd - halflength;
                //Distance from front of the car to end of the lanesection.
                inFrontInfo = carPath[i].getNextObInFront(this,distSum);
                
                if (inFrontInfo[0] != CarContainer.NOTHING_IN_FRONT) {
                    break;
                }
            } else {
                inFrontInfo =
                carPath[i].getNextObInFront(this,carPath[i].length);
                
                if (inFrontInfo[0] != CarContainer.NOTHING_IN_FRONT) {
                    inFrontInfo[0] += distSum;
                    break;
                } else {
                    distSum += carPath[i].length;
                }
            }
        }
        
        if (inFrontInfo[0] < 0) System.out.print( "<0 ");
        return inFrontInfo ;
    }
    
    public void setNewPosition(double speed, double angle) {
        x[0] += speed*Math.cos(angle);
        y[0] -= speed*Math.sin(angle);
    }
    
    private double metersToPixels(double value) {
        return value*metersPerPixel;
    }
    
    private double pixelsToMeters(double value) {
        return value/metersPerPixel;
    }
    
    private void implement_Basic_Car_Following_Model() {
        
        if (speed < topspeed) speed += acceleration;
        
        //Get the distance and speed of the object in front.
        inFrontInfo = getNextOb_SpeedAndDist();
        inFrontInfo = carPath[0].isOKToGo(this,inFrontInfo,distanceToEnd);
        
        //The distance from the front of the car...
        //...to the object in front = inFrontInfo[0] (pixels).
        
        //The object in front is either a car or a traffic-light.
        
        //The speed of the object in front is inFrontInfo[1],
        //but we're not going to use this for this model.
        
        distanceToNext = metersToPixels(inFrontInfo[0]);
        speedOfNext    = inFrontInfo[1];
        
        double L = 10;             //L = 10 (distance to consider slowing down)
        double l = 1;              //l = 1  (distance not allowed near)
        //x = distanceToNext
        double c = topspeed;       //c = topspeed
        double m = c/(L-l);        //m = c/(L-l)
        double b = (c*l)/(l-L);    //b = cl/(l-L)
        //v = speed
        //if       x >= L  => v = c
        //else if  x <= l  => v = 0
        //else             => V = mx+b;
        
        //if (distanceToNext >= L) speed = topspeed;
        if (distanceToNext < 1) speed = 0;
        else if (distanceToNext < L) speed = m*distanceToNext + b;
        
        float oldX = x[0],oldY = y[0];
        double pixelsCanMove = metersToPixels(speed);
        
        setNewPosition(pixelsCanMove,carAngle);
        distanceToEnd-=pixelsCanMove;
        
        if (distanceToEnd <= 0) {
            changeToNextLane();
            oldX = x[0];
            oldY = y[0];
            setNewPosition(Math.abs(distanceToEnd),carAngle);
            distanceToEnd = GeoUtils.getDistance(x[0],y[0],endX,endY);
        }
        
        if (pixelsCanMove == 0) speed = 0;
        else speed = pixelsToMeters(pixelsCanMove);
        
        updateShapeCordinates(x[0]-oldX,y[0]-oldY);
        setShape();
    }
    
    /********************************* Mouse events ********************/
    
    /**
     * Shows the path that the car has planned to take
     */
    public Shape getFuturePath() {
        
        GeneralPath futurePath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        
        for (int i=0;i<carRoutes;i++) {
            if (i==0) futurePath.moveTo(x[0],y[0]);
            if (carPath[i] != null)
                futurePath.lineTo(carPath[i].endX,carPath[i].endY);
        }
        return futurePath;
    }
}














