/*
Copyright 2014 David Morrissey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.vincentas.transmogrify.util;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.vincentas.transmogrify.engine.trace.MultiPointTrace;
import com.vincentas.transmogrify.engine.trace.Trace;
import com.vincentas.transmogrify.engine.trace.TracePoint;

import java.util.ArrayList;
import java.util.List;

public class FreehandView extends SubsamplingScaleImageView implements OnTouchListener {

    private PointF vPrevious;
    private TracePoint vStart;
    private boolean drawing = false;

    private boolean panning = true;

    private int strokeWidth;

    private List<TracePoint> sPoints;

    private List<List<TracePoint>> curves = new ArrayList<>();

    private OnTraceAvailableListener listener;

    public FreehandView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();
    }

    public FreehandView(Context context) {
        this(context, null);
    }

    private void initialise() {
        setOnTouchListener(this);
        float density = getResources().getDisplayMetrics().densityDpi;
        strokeWidth = (int) (density / 60f);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (panning || curves.size() >= 2 && !drawing) {
            return super.onTouchEvent(event);
        }
        boolean consumed = false;
        int touchCount = event.getPointerCount();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_1_DOWN:
                PointF pointF = viewToSourceCoord(event.getX(), event.getY());
                vStart = new TracePoint(pointF.x, pointF.y);
                vPrevious = new PointF(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_POINTER_2_DOWN:
                // Abort any current drawing, user is zooming
                vStart = null;
                vPrevious = null;
                break;
            case MotionEvent.ACTION_MOVE:
                PointF sCurrentF = viewToSourceCoord(event.getX(), event.getY());
                TracePoint sCurrent = new TracePoint(sCurrentF.x, sCurrentF.y);

                if (touchCount == 1 && vStart != null) {
                    float vDX = Math.abs(event.getX() - vPrevious.x);
                    float vDY = Math.abs(event.getY() - vPrevious.y);
                    if (vDX >= strokeWidth * 5 || vDY >= strokeWidth * 5) {
                        if (sPoints == null) {
                            sPoints = new ArrayList<TracePoint>();
                            sPoints.add(vStart);

                            curves.add(sPoints);
                        }
                        sPoints.add(sCurrent);
                        vPrevious.x = event.getX();
                        vPrevious.y = event.getY();
                        drawing = true;
                        invalidate();
                    }
                    consumed = true;
                    invalidate();
                } else if (touchCount == 1) {
                    // Consume all one touch drags to prevent odd panning effects handled by the superclass.
                    consumed = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                invalidate();
                drawing = false;
                vPrevious = null;
                vStart = null;
                sPoints = null;

                if (curves.size() == 2 && listener != null) {
                    listener.traceAvailable(getFrom(), getTo());
                }
        }
        // Use parent to handle pinch and two-finger pan.
        return consumed || super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Don't draw anything before image is ready.
        if (!isReady()) {
            return;
        }

        for (List<TracePoint> curve : curves) {
            drawCurve(canvas, curve);
        }

    }

    private void drawCurve(Canvas canvas, List<TracePoint> curve) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Path vPath = new Path();
        PointF vPrev = sourceToViewCoord(curve.get(0).x, curve.get(0).y);
        vPath.moveTo(vPrev.x, vPrev.y);
        for (int i = 1; i < curve.size(); i++) {
            PointF vPoint = sourceToViewCoord(curve.get(i).x, curve.get(i).y);
            vPath.quadTo(vPrev.x, vPrev.y, (vPoint.x + vPrev.x) / 2, (vPoint.y + vPrev.y) / 2);
            //vPath.lineTo(vPoint.x, vPoint.y);
            vPrev = vPoint;
        }
        paint.setStyle(Style.STROKE);
        paint.setStrokeCap(Cap.ROUND);
        paint.setStrokeWidth(strokeWidth * 2);
        paint.setColor(Color.BLACK);
        canvas.drawPath(vPath, paint);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.WHITE);
        canvas.drawPath(vPath, paint);

    }

    public Trace getFrom() {
        Trace result = null;
        if (curves.size() > 0) {
            result = new MultiPointTrace(curves.get(0));
        }

        return result;
    }

    public Trace getTo() {
        Trace result = null;
        if (curves.size() > 1) {
            result = new MultiPointTrace(curves.get(1));
        }

        return result;
    }

    public void reset() {
        curves.clear();
        this.sPoints = null;
        invalidate();
    }

    public boolean isPanning() {
        return panning;
    }

    public void setPanning(boolean panning) {
        this.panning = panning;
    }

    public void onTraceAvailable(OnTraceAvailableListener listener) {
        this.listener = listener;
    }

    public interface OnTraceAvailableListener {

        public void traceAvailable(Trace from, Trace to);
    }

}