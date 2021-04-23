package net.luisr.sylon.img;

import android.graphics.Bitmap;
import android.graphics.Point;

public class PerspectiveTransformer {

    public class CropData {

        private Point ptTopLeft, ptTopRight, ptBottomLeft, ptBottomRight;

        private CropData(Point ptTopLeft, Point ptTopRight, Point ptBottomLeft, Point ptBottomRight) {
            this.ptTopLeft = ptTopLeft;
            this.ptTopRight = ptTopRight;
            this.ptBottomLeft = ptBottomLeft;
            this.ptBottomRight = ptBottomRight;
        }
    }

    public Bitmap transform(Bitmap input, CropData cropData) {
        return null;
    }
}
