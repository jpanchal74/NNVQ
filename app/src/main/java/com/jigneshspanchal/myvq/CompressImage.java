package com.jigneshspanchal.myvq;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by v548433 on 07/23/2017.
 */

public class CompressImage {

    public Bitmap compressedImageBitmap;

    private int win[];
    private byte wji[][];
    private byte origImgData[][];

    //private int wji[][];
    //private int origImgData[][];

    private int width = 0, height = 0, xnd = 0, ynd = 0, noout = 0, noinp = 0, npt = 0;

    public  CompressImage (Bitmap bitmap, int XND, int YND, int NOOUT, int NI) {
        xnd = XND;
        ynd = YND;
        noout = NOOUT;
        noinp = XND*YND;

        width = (int)(bitmap.getWidth()/xnd)*xnd;
        height = (int)(bitmap.getHeight()/ynd)*ynd;
        npt = (int) ((width * height) / (noinp));

        win = new int[npt];

        wji = new byte[noout][noinp];
        origImgData = new byte[npt][noinp];
        //wji = new int[noout][noinp];
        //origImgData = new int[npt][noinp]

        generateBitmapImageVectors(bitmap);
        trainKmap(NI);
        generateImageCodeBook();
        generateCompressedImageBitmap();
    }

    public  CompressImage (InputStream demoImgData, Bitmap bitmap, int XND, int YND, int NOOUT, int NI) {
        xnd = XND;
        ynd = YND;
        noout = NOOUT;
        noinp = XND*YND;

        width = (int)(bitmap.getWidth()/xnd)*xnd;
        height = (int)(bitmap.getHeight()/ynd)*ynd;
        npt = (int) ((width * height) / (noinp));

        win = new int[npt];

        wji = new byte[noout][noinp];
        origImgData = new byte[npt][noinp];
        //wji = new int[noout][noinp];
        //origImgData = new int[npt][noinp]

        generateInputStreamImageVectors(demoImgData);
        //generateBitmapImageVectors(bitmap);
        trainKmap(NI);
        generateImageCodeBook();
        generateCompressedImageBitmap();
    }

    public void generateBitmapImageVectors(Bitmap bitmapA) {
        int l, m, x, y;
        int nptIdx=0, xyIdx=0;

        for (y = 0; y < height; y += ynd) {
            for (x = 0; x < width; x += ynd) {
                for (m = 0; m < xnd; m++) {
                    for (l = 0; l < xnd; l++) {
                        try {
                            origImgData[nptIdx][xyIdx] = (byte)bitmapA.getPixel((int) (x + m), (int) (y + l));
                            //origImgData[nptIdx][xyIdx] = bitmapA.getPixel((int) (x + m), (int) (y + l));
                            if (xyIdx >= (noinp - 1)) {
                                nptIdx++;
                                xyIdx = 0;
                            } else {
                                xyIdx++;
                            }
                        } catch (IllegalArgumentException iae) {
                            System.out.println("Whoa there!");
                            System.out.printf("The error was: %s\n, x=%d, m=%d, y=%d, l=%d", iae.getMessage(), x, m, y, l);
                        }
                    }
                }
            }
        }

    }

    public void generateInputStreamImageVectors(InputStream demoImgData) {
        int l, m, x, y;
        int nptIdx=0, xyIdx=0;
        long pt=0;
        int nImageDataRead = 0;
        long nImageDataSkipped = 0;
        int InSize;
        byte data[] = new byte[xnd];

        try {
            InSize = demoImgData.available();
            demoImgData.mark(InSize);
            for(l=0;l<height;l+=ynd){
                for(x=0;x<width;x+=xnd){
                    for(y=0;y<xnd;y++){
                        demoImgData.reset();
                        pt=(long)((y+l)*width+x);
                        nImageDataSkipped = demoImgData.skip(pt);
                        nImageDataRead += demoImgData.read(data,0,xnd);
                        //origImgData[nptIdx][xyIdx] = (byte)demoImgData.read();
                        for(m=0;m<xnd;m++){
                            origImgData[nptIdx][xyIdx] = (byte)data[m];
                            //origImgData[nptIdx][xyIdx] = (int)data[m];
                            if (xyIdx >= (noinp - 1)) {
                                nptIdx++;
                                xyIdx = 0;
                            } else {
                                xyIdx++;
                            }
                        }
                    }
                }
            }
            demoImgData.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void trainKmap(int NI) {
        int i, j, m, k, winnode, eta;
        int no = 0;

        int d[] = new int[noout];
        Random r = new Random();


        for (j = 0; j < noout; j++) {
            r.nextBytes(wji[j]);
            //for (i = 0; i < noinp; i++) {
            //    wji[j][i] = r.nextInt();
            //}
        }

        eta = npt;

        for (m = 0; m < NI; m++) {
            for (k = 0; k < npt; k++) {
                no = r.nextInt(npt - 1);
                for (j = 0; j < noout; j++) {
                    d[j] = 0;
                    for (i = 0; i < noinp; i++) {
                        d[j] += Math.abs(((int)origImgData[no][i] - (int)wji[j][i]));
                    }
                }
                winnode = 0;
                for (j = 1; j < noout; j++) {
                    if ((d[winnode]) > (d[j])) winnode = j;
                }
                for (i = 0; i < noinp; i++) {
                    wji[winnode][i] += (byte)((int)origImgData[no][i] - (int)wji[winnode][i]);
                }
                eta -= 1;
            }
        }

    }


    public void generateImageCodeBook() {
        int i, j, k, winnode = 0;
        int d[] = new int[noout];

        for (k = 0; k < npt; k++) {
            for (j = 0; j < noout; j++) {
                d[j] = 0;
                for (i = 0; i < noinp; i++) {
                    d[j] += Math.abs((int) origImgData[k][i] - (int) wji[j][i]);
                }
            }
            winnode = 0;
            for (j = 1; j < noout; j++) if (d[winnode] > d[j]) winnode = j;
            win[k] = winnode;
        }
    }


    public void generateCompressedImageBitmap() {
        int i, j, x, y, ix=0, iy=0;
        int wt[] = new int[noout];

        if(compressedImageBitmap == null){
            compressedImageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        for(i=0;i<npt;i++) {
            for (j = 0; j < noinp; j++) {
                wt[j] = (int) wji[win[i]][j];
            }
            for (y = 0; y < (xnd * ynd); y += ynd) {
                for (x = 0; x < xnd; x++) {
                    //compressedImageBitmap.setPixel(ix + x, iy, wt[x + y]);
                    //if (wt[x + y] == 128) {
                    if (wt[x + y] >= 0) {
                        compressedImageBitmap.setPixel(ix + x, iy, Color.BLACK);
                    } else {
                        compressedImageBitmap.setPixel(ix + x, iy, Color.WHITE);
                    }
                }
                iy++;
            }
            ix += xnd;
            iy -= ynd;
            if (ix >= width) {
                ix = 0;
                iy += ynd;
            }
        }

    }


}
