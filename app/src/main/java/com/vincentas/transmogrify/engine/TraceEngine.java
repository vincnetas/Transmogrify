package com.vincentas.transmogrify.engine;

import android.graphics.Bitmap;

import com.vincentas.transmogrify.engine.trace.Trace;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import java.util.Arrays;


public class TraceEngine {

    private static GeometryFactory geometryFactory = new GeometryFactory();

    public static TraceHandle render(final Bitmap source, final Trace from, final Trace to, final Bitmap result, final TraceProgress progress) {
        final TraceHandle handle = new TraceHandle();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Geometry sourceGeometry = geometryFactory.createLinearRing(new Coordinate[]{
                        new Coordinate(0, 0),
                        new Coordinate(source.getWidth(), 0),
                        new Coordinate(source.getWidth(), source.getHeight()),
                        new Coordinate(0, source.getHeight()),
                        new Coordinate(0, 0)

                });

                from.setSteps(result.getWidth());
                to.setSteps(result.getWidth());

                int[] line = new int[result.getHeight()];
                for (int i = 0; i < result.getWidth(); i++) {
                    progress.onProgress(i, result.getWidth(), result);
                    if (handle.isCanceled()) {
                        return;
                    }

                    int[] f = from.getXY(i);
                    int[] t = to.getXY(i);
                    scanLine(source, sourceGeometry, f[0], f[1], t[0], t[1], line);
                    result.setPixels(line, 0, 1, i, 0, 1, result.getHeight());
                }

                progress.done();
            }
        }).start();

        return handle;
    }

    private static void scanLine(Bitmap image, Geometry imageGeometry, int x1, int y1, int x2, int y2, int[] result) {
        LineString lineString = geometryFactory.createLineString(new Coordinate[]{new Coordinate(x1, y1), new Coordinate(x2, y2)});
        if (!lineString.intersects(imageGeometry)) {
            Arrays.fill(result, 0);
            return;
        }

        float dx = (float) (x2 - x1) / result.length;
        float dy = (float) (y2 - y1) / result.length;
        float x = x1;
        float y = y1;

        for (int i = 0; i < result.length; i++) {
            if (x >= 0 && (int) x < image.getWidth() && y >= 0 && (int) y < image.getHeight()) {
                result[i] = image.getPixel((int) x, (int) y);
            } else {
                result[i] = 0;
            }

            x += dx;
            y += dy;
        }
    }

    public interface TraceProgress {

        void done();

        void onProgress(int current, int total, Bitmap bitmap);

    }

}
