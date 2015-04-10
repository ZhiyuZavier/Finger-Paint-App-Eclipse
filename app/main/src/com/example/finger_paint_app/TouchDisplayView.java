package com.example.finger_paint_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import com.example.finger_paint_app.Pools.SimplePool;

/**
 * Created by Zhiyu Lei on 4/04/2015.
 */
public class TouchDisplayView extends View{
    public static int color;
    public static int shape;    
    public boolean cc = false;

   /*
    * Below are variables required for drawing.
   */
    public final int[] COLORS = {
            0xFFFF2538, 0xFFE3ff2A, 0xFF6DFF81, 0xFF402FFF, 0xFFB556FF,
            0xFFFFC526, 0xFF9933CC, 0xFF669900, 0xFFFF8800, 0xFFCC0000
    };

    private Paint mCirclePaint = new Paint();
    private Paint mTrianglePaint = new Paint();
    private Paint mSquarePaint = new Paint();

    // calculated radiuses in px
    private float mCircleRadius;
    private float mCircleCurrentRadius;
    private float mTriangleRadius;
    private float mTriangleCurrentRadius;
    private float mSquareRadius;
    private float mSquareCurrentRadius;
    
    // radius of active touch circle in dp
    private static final float CIRCLE_RADIUS_DP = 35f;
    

    // Hold data for active touch pointer IDs
    private SparseArray<TouchHistory> mTouches;

    // Is there an active touch?
    private boolean mHasTouch = false;

    /**
     * Holds data related to a touch pointer, including its current position,
     * pressure and historical positions. Objects are allocated through an
     * object pool using {@link #obtain()} and {@link #recycle()} to reuse
     * existing objects.
     */
    static final class TouchHistory {

        // number of historical points to store
        public static final int HISTORY_COUNT = 100;

        public float x;
        public float y;
        public float pressure = 0f;
        public String label = null;

        // current position in history array
        public int historyIndex = 0;
        public int historyCount = 0;

        // arrray of pointer position history
        public PointF[] history = new PointF[HISTORY_COUNT];

        private static final int MAX_POOL_SIZE = 10;
        private static final SimplePool<TouchHistory> sPool =
                new Pools.SimplePool<TouchHistory>(MAX_POOL_SIZE);

        public static TouchHistory obtain(float x, float y, float pressure) {
            TouchHistory data = sPool.acquire();
            if (data == null) {
                data = new TouchHistory();
            }

            data.setTouch(x, y, pressure);

            return data;
        }

        public TouchHistory() {

            // initialise history array
            for (int i = 0; i < HISTORY_COUNT; i++) {
                history[i] = new PointF();
            }
        }

        public void setTouch(float x, float y, float pressure) {
            this.x = x;
            this.y = y;
            this.pressure = pressure;
        }

        public void recycle() {
            this.historyIndex = 0;
            this.historyCount = 0;
            sPool.release(this);
        }

        /**
         * Add a point to its history. Overwrites oldest point if the maximum
         * number of historical points is already stored.
         *
         * @param point
         */
        public void addHistory(float x, float y) {
            PointF p = history[historyIndex];
            p.x = x;
            p.y = y;

            historyIndex = (historyIndex + 1) % history.length;

            if (historyCount < HISTORY_COUNT) {
                historyCount++;
            }
        }

    }

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public TouchDisplayView(Context context) {
        super(context);

        // SparseArray for touch events, indexed by touch id
        mTouches = new SparseArray<TouchHistory>(10);

        initialisePaint();
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     * <p/>
     * <p/>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     * @see View(android.content.Context, AttributeSet, int)
     */
    public TouchDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // SparseArray for touch events, indexed by touch id
        mTouches = new SparseArray<TouchHistory>(10);

        initialisePaint();
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    public boolean onTouchEvent (MotionEvent event) {

        final int action = event.getAction();

         /*
         * Switch on the action. The action is extracted from the event by
         * applying the MotionEvent.ACTION_MASK. Alternatively a call to
         * event.getActionMasked() would yield in the action as well.
         */
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: {
                // first pressed gesture has started

                /*
                 * Only one touch event is stored in the MotionEvent. Extract
                 * the pointer identifier of this touch from the first index
                 * within the MotionEvent object.
                 */
                int id = event.getPointerId(0);

                TouchHistory data = TouchHistory.obtain(event.getX(0), event.getY(0),
                        event.getPressure(0));
                data.label = "id:" + 0;

                /*
                 * Store the data under its pointer identifier. The pointer
                 * number stays consistent for the duration of a gesture,
                 * accounting for other pointers going up or down.
                 */
                mTouches.put(id,data);

                mHasTouch = true;

                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                /*
                 * A non-primary pointer has gone down, after an event for the
                 * primary pointer (ACTION_DOWN) has already been received.
                 */

                /*
                 * The MotionEvent object contains multiple pointers. Need to
                 * extract the index at which the data for this particular event
                 * is stored.
                 */
                int index = event.getActionIndex();
                int id = event.getPointerId(index);

                TouchHistory data = TouchHistory.obtain(event.getX(index),event.getY(index),
                        event.getPressure(index));
                data.label = "id: " + id;

                /*
                 * Store the data under its pointer identifier. The index of
                 * this pointer can change over multiple events, but this
                 * pointer is always identified by the same identifier for this
                 * active gesture.
                 */
                mTouches.put(id, data);

                break;
            }           

            case MotionEvent.ACTION_MOVE: {
                /*
                 * A change event happened during a pressed gesture. (Between
                 * ACTION_DOWN and ACTION_UP or ACTION_POINTER_DOWN and
                 * ACTION_POINTER_UP)
                 */

                /*
                 * Loop through all active pointers contained within this event.
                 * Data for each pointer is stored in a MotionEvent at an index
                 * (starting from 0 up to the number of active pointers). This
                 * loop goes through each of these active pointers, extracts its
                 * data (position and pressure) and updates its stored data. A
                 * pointer is identified by its pointer number which stays
                 * constant across touch events as long as it remains active.
                 * This identifier is used to keep track of a pointer across
                 * events.
                 */
                for (int index = 0; index < event.getPointerCount(); index++) {
                    // get pointer id for data stored at this index
                    int id = event.getPointerId(index);

                    // get the data stored externally about this pointer.
                    TouchHistory data = mTouches.get(id);

                    // add previous position to history and add new values
                    data.addHistory(data.x, data.y);
                    data.setTouch(event.getX(index), event.getY(index),
                            event.getPressure(index));

                }

                break;
            }

        }

        // trigger redraw on UI thread
        this.postInvalidate();

        return true;
    }

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */  
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        
        if (cc) {
        	Paint clearPaint = new Paint();
        	clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        	canvas.drawRect(0, 0, 0, 0, clearPaint);
        	cc = false;
			
		} else {
			// loop through all active touches and draw them
        	for (int i = 0; i < mTouches.size(); i++) {

                // get the pointer id and associated data for this index
                int id = mTouches.keyAt(i);
                TouchHistory data = mTouches.valueAt(i);
                
                // draw the data and its history to the canvas
                switch (shape) {
    			case 0:
    				drawTriangle(canvas, id, data);
    				break;
    			case 1:
    				drawSquare(canvas, id, data);
    				break;
    			case 2:
    				drawCircle(canvas, id, data);
    				break;
    			default:
    				break;
    			}

            }
		}                 
			      
        
    }
    
    // Method for Reset Button
    public void clearCanvas()
    {
    	cc = true;
    	invalidate();
    }
    
    private void initialisePaint() {

        // Calculate radiuses in px from dp based on screen density
        float density = getResources().getDisplayMetrics().density;
        mCircleRadius = CIRCLE_RADIUS_DP * density;
        mCircleCurrentRadius = 2 * CIRCLE_RADIUS_DP * density;
        mTriangleRadius = CIRCLE_RADIUS_DP * density;
        mTriangleCurrentRadius = 2 * CIRCLE_RADIUS_DP * density;
        mSquareRadius = CIRCLE_RADIUS_DP * density;
        mSquareCurrentRadius = 2 * CIRCLE_RADIUS_DP * density;
        
    }

    public void drawCircle(Canvas canvas, int id, TouchHistory data){
        mCirclePaint.setColor(COLORS[color]);
        Log.e("color index","color="+color+COLORS[color]);
            /*
         * Draw the circle, size scaled to its pressure. Pressure is clamped to
         * 1.0 max to ensure proper drawing. (Reported pressure values can
         * exceed 1.0, depending on the calibration of the touch screen).
         */
        float pressure = Math.min(data.pressure, 1f);
        float radius = pressure * mCircleCurrentRadius;

        canvas.drawCircle(data.x, data.y, radius, mCirclePaint);

        for (int j = 0; j < data.history.length && j < data.historyCount; j++) {
            PointF p = data.history[j];
            canvas.drawCircle(p.x, p.y, mCircleRadius, mCirclePaint);
        }
    }

    public void drawTriangle (Canvas canvas, int id, TouchHistory data){
    	mTrianglePaint.setColor(COLORS[color]);
    	Log.e("color index","color="+color+COLORS[color]);
    	
    	/*
         * Draw the circle, size scaled to its pressure. Pressure is clamped to
         * 1.0 max to ensure proper drawing. (Reported pressure values can
         * exceed 1.0, depending on the calibration of the touch screen).
         */
        float pressure = Math.min(data.pressure, 1f);
        float radius = pressure * mTriangleCurrentRadius;
        
        canvas.drawLine(data.x, data.y+radius, 
        		data.x-0.707f*radius,
        		data.y-0.707f*radius, mTrianglePaint);
        canvas.drawLine(data.x-0.707f*radius, 
        		data.y-0.707f*radius, 
        		data.x+0.707f*radius,
        		data.y-0.707f*radius, mTrianglePaint);
        canvas.drawLine(data.x, data.y+mTriangleRadius, 
        		data.x+0.707f*radius,
        		data.y-0.707f*radius, mTrianglePaint);
        
        for (int j = 0; j < data.history.length && j < data.historyCount; j++) {
            PointF p = data.history[j];
            canvas.drawLine(p.x, p.y+mTriangleRadius, 
            		p.x-0.707f*mSquareRadius,
            		p.y-0.707f*mSquareRadius, mTrianglePaint);
            canvas.drawLine(p.x-0.707f*mSquareRadius, 
            		p.y-0.707f*mSquareRadius, 
            		p.x+0.707f*mSquareRadius,
            		p.y-0.707f*mSquareRadius, mTrianglePaint);
            canvas.drawLine(p.x, p.y+mTriangleRadius, 
            		p.x+0.707f*mSquareRadius,
            		p.y-0.707f*mSquareRadius, mTrianglePaint);
        }
        
        
    }

    public void drawSquare (Canvas canvas, int id, TouchHistory data){
    	mSquarePaint.setColor(COLORS[color]);
    	Log.e("color index","color="+color+COLORS[color]);
    	
    	/*
         * Draw the circle, size scaled to its pressure. Pressure is clamped to
         * 1.0 max to ensure proper drawing. (Reported pressure values can
         * exceed 1.0, depending on the calibration of the touch screen).
         */
        float pressure = Math.min(data.pressure, 1f);
        float radius = pressure * mSquareCurrentRadius;
        
        canvas.drawRect(data.x-0.707f*radius, data.y-0.707f*radius, 
        		data.x+0.707f*radius, data.y+0.707f*radius, mSquarePaint);
        
        for (int j = 0; j < data.history.length && j < data.historyCount; j++) {
            PointF p = data.history[j];
            canvas.drawRect(p.x-0.707f*mSquareRadius, p.y-0.707f*mSquareRadius, 
            		p.x+0.707f*mSquareRadius, p.y+0.707f*mSquareRadius, mSquarePaint);
        }
    }    
        
     
}




























